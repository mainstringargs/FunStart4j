package com.mainstringargs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.mainstringargs.schema.ApplicationDesc;
import com.mainstringargs.schema.Argument;
import com.mainstringargs.schema.Extension;
import com.mainstringargs.schema.Information;
import com.mainstringargs.schema.Jar;
import com.mainstringargs.schema.Jnlp;
import com.mainstringargs.schema.Nativelib;
import com.mainstringargs.schema.Property;
import com.mainstringargs.schema.Resources;

public class JNLPHandler {

	private URI jnlpUri;
	private String folderLocation;

	private Map<URI, File> classPathJars = new ConcurrentHashMap<>();
	private String mainMethod = "";
	private LinkedHashMap<String, String> properties = new LinkedHashMap<>();
	private List<Argument> arguments = new ArrayList<Argument>();
	private ExecutorService executor = Executors.newCachedThreadPool();

	public JNLPHandler(URI jnlpUri) {
		this.jnlpUri = jnlpUri;
	}

	public JNLPHandler(URI jnlpUri, String folderLocation) {
		this.jnlpUri = jnlpUri;
		this.folderLocation = folderLocation;
	}

	public void parseJNLP() {

		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance(Jnlp.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Unmarshaller um = null;
		try {
			um = jc.createUnmarshaller();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Jnlp data = null;
		try {
			data = (Jnlp) um.unmarshal(jnlpUri.toURL());
		} catch (JAXBException | MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (folderLocation == null || folderLocation.isEmpty()) {
			for (Information information : data.getInformation()) {
				if (information.getTitle() != null && !information.getTitle().isEmpty()) {
					String scrubbedTitle = information.getTitle().replaceAll("[^A-Za-z0-9]", "").replaceAll("\\s", "");
					folderLocation = scrubbedTitle;

					File folder = new File(folderLocation);
					folder.mkdir();

				}
			}
		}

		if (folderLocation == null || folderLocation.isEmpty()) {
			folderLocation = data.hashCode() + "";

			File folder = new File(folderLocation);
			folder.mkdir();
		}

		parseJNLP(jnlpUri);

		executor.shutdownNow();

		System.out.println("All work submitted; waiting for finish.");
		try {
			executor.awaitTermination(10, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Finished.");
	}

	public void parseJNLP(URI jnlpUri) {

		System.err.println("Parsing... " + jnlpUri);

		String parentUri = getParentUri(jnlpUri);

		System.out.println("Parent URI " + parentUri);

		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance(Jnlp.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Unmarshaller um = null;
		try {
			um = jc.createUnmarshaller();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Jnlp data = null;
		try {
			data = (Jnlp) um.unmarshal(jnlpUri.toURL());
		} catch (JAXBException | MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(data.getCodebase());
		System.out.println(data.getHref());

		List<Resources> resources = data.getResources();

		for (Object desc : data.getApplicationDescOrAppletDescOrComponentDescOrInstallerDesc()) {
			if (desc instanceof ApplicationDesc) {
				mainMethod = ((ApplicationDesc) desc).getMainClass();

				if (((ApplicationDesc) desc).getArgument() != null) {
					arguments.addAll(((ApplicationDesc) desc).getArgument());
				}
			}
		}

		for (Resources resource : resources) {
			System.out.println(resource.getArch() + " " + resource.getOs());
			for (Object libRef : resource.getJavaOrJ2SeOrJarOrNativelibOrExtensionOrPropertyOrPackage()) {

				if (libRef instanceof Jar) {
					Jar jarRef = (Jar) libRef;

					System.out.println(jarRef.getDownload() + " " + jarRef.getHref() + " " + jarRef.getMain() + " "
							+ jarRef.getPart() + " " + jarRef.getSize() + " " + jarRef.getVersion());

					final URI uri = getURIReference(data.getCodebase(), parentUri, jarRef.getHref());

					final Downloader dLoader = new Downloader(uri,
							folderLocation + File.separator + getFileNameFromUri(uri));

					executor.execute(new Runnable() {

						@Override
						public void run() {

							if (!classPathJars.containsKey(uri)) {
								// placeholder until we get the real file
								classPathJars.put(uri, new File("."));
								File file = dLoader.getFile(true);
								classPathJars.put(uri, file);
							}

						}
					});

				} else if (libRef instanceof Extension) {
					Extension extensionRef = (Extension) libRef;

					System.out.println(extensionRef.getHref() + " " + extensionRef.getName() + " "
							+ extensionRef.getVersion() + " " + extensionRef.getExtDownload());

					parseJNLP(getURIReference(data.getCodebase(), parentUri, extensionRef.getHref()));

				} else if (libRef instanceof Nativelib) {
					Nativelib nativeLibRef = (Nativelib) libRef;
					System.out.println(nativeLibRef.getDownload() + " " + nativeLibRef.getHref() + " "
							+ nativeLibRef.getPart() + " " + nativeLibRef.getSize() + " " + nativeLibRef.getVersion());

					final URI uri = getURIReference(data.getCodebase(), parentUri, nativeLibRef.getHref());

					try {
						final Downloader dLoader = new Downloader(uri,
								folderLocation + File.separator + getFileNameFromUri(uri));

						executor.execute(new Runnable() {
							@Override
							public void run() {
								if (!classPathJars.containsKey(uri)) {
									// placeholder until we get the real file
									classPathJars.put(uri, new File("."));
									File file = dLoader.getFile(true);
									classPathJars.put(uri, file);
								}
							}
						});

					} catch (Exception e) {
						e.printStackTrace();
					}

				} else if (libRef instanceof Property) {
					Property propertyRef = (Property) libRef;

					System.out.println(propertyRef.getName() + " " + propertyRef.getValue());

					properties.put(propertyRef.getName(), propertyRef.getValue());
				}
			}
		}
	}

	public void generateScripts() {

	}

	public void runApplication(List<String> argumentsForJVM) {
		String classpath = "";

		for (File file : classPathJars.values()) {
			classpath += file.getAbsolutePath() + File.pathSeparator;
		}

		List<String> passedInProps = new ArrayList<String>();

		for (Entry<String, String> prop : properties.entrySet()) {
			String propString = "-D" + prop.getKey() + "=" + prop.getValue();
			passedInProps.add(propString);
		}

		List<String> fullCommand = new ArrayList<>();
		fullCommand.add("java");
		fullCommand.addAll(passedInProps);
		fullCommand.addAll(argumentsForJVM);
		fullCommand.add("-classpath");
		fullCommand.add(classpath);
		fullCommand.add(mainMethod);

		System.out.println("Executing " + fullCommand);

		ProcessBuilder pb = new ProcessBuilder(fullCommand.toArray(new String[] {}));
		pb.redirectErrorStream(true);

		Process p = null;
		try {
			p = pb.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String readline;
		int i = 0;
		try {
			while ((readline = reader.readLine()) != null) {
				System.out.println(++i + " " + readline);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static URI getURIReference(String codeBase, String parentRef, String fileName) {
		String fullUri = codeBase + "/" + fileName;

		if (codeBase.toUpperCase().startsWith("HTTP")) {
			// no-op
		} else if (fileName.toUpperCase().startsWith("HTTP")) {
			fullUri = fileName;
		} else if (parentRef.toUpperCase().startsWith("HTTP")) {
			fullUri = parentRef + "/" + fileName;
		}

		URI theUri = null;

		try {
			theUri = new URI(fullUri);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return theUri.normalize();

	}

	public static String getFileNameFromUri(URI uri) {

		String uriString = uri.toString();

		return uriString.substring(uriString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
	}

	private String getParentUri(URI uri) {
		String uriString = uri.toString();

		return uriString.substring(0, uriString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
	}

}
