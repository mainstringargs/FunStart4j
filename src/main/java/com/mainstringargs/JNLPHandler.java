package com.mainstringargs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.mainstringargs.schema.ApplicationDesc;
import com.mainstringargs.schema.Argument;
import com.mainstringargs.schema.Extension;
import com.mainstringargs.schema.Jar;
import com.mainstringargs.schema.Jnlp;
import com.mainstringargs.schema.Nativelib;
import com.mainstringargs.schema.Property;
import com.mainstringargs.schema.Resources;

public class JNLPHandler {

	private URI jnlpUri;
	private String folderLocation = "lib";

	private LinkedHashSet<File> classPathJars = new LinkedHashSet<>();
	private String mainMethod = "";
	private LinkedHashMap<String, String> properties = new LinkedHashMap<>();
	private List<Argument> arguments = new ArrayList<Argument>();

	public JNLPHandler(URI jnlpUri) {
		this.jnlpUri = jnlpUri;
	}

	public JNLPHandler(URI jnlpUri, String folderLocation) {
		this.jnlpUri = jnlpUri;
		this.folderLocation = folderLocation;
	}

	public void parseJNLP() {
		parseJNLP(jnlpUri);
	}

	public void parseJNLP(URI uri) {

		System.err.println("Parsing... " + uri);

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
			data = (Jnlp) um.unmarshal(uri.toURL());
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

					URL url = getURLReference(data.getCodebase(), jarRef.getHref());

					Downloader dLoader = new Downloader(url, folderLocation + File.separator + getFileNameFromUrl(url));

					System.out.println("Download " + dLoader.getFile() + " " + new Date(dLoader.getLastModified()) + " "
							+ dLoader.getFileSize());
					classPathJars.add(dLoader.getFile());

				} else if (libRef instanceof Extension) {
					Extension extensionRef = (Extension) libRef;

					System.out.println(extensionRef.getHref() + " " + extensionRef.getName() + " "
							+ extensionRef.getVersion() + " " + extensionRef.getExtDownload());

					try {
						parseJNLP(new URI(data.getCodebase() + "/" + extensionRef.getHref()));
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else if (libRef instanceof Nativelib) {
					Nativelib nativeLibRef = (Nativelib) libRef;
					System.out.println(nativeLibRef.getDownload() + " " + nativeLibRef.getHref() + " "
							+ nativeLibRef.getPart() + " " + nativeLibRef.getSize() + " " + nativeLibRef.getVersion());

					URL url = getURLReference(data.getCodebase(), nativeLibRef.getHref());

					Downloader dLoader = new Downloader(url, folderLocation + File.separator + getFileNameFromUrl(url));

					System.out.println("Download " + dLoader.getFile() + " " + new Date(dLoader.getLastModified()) + " "
							+ dLoader.getFileSize());

					classPathJars.add(dLoader.getFile());

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

	public void runApplication() {
		String classpath = "";

		for (File file : classPathJars) {
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

	public static URL getURLReference(String codeBase, String fileName) {
		String fullUrl = codeBase + "/" + fileName;

		if (fileName.toUpperCase().startsWith("HTTP")) {
			fullUrl = fileName;
		}

		URL theUrl = null;
		try {
			theUrl = new URL(fullUrl);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return theUrl;

	}

	public static String getFileNameFromUrl(URL url) {

		String urlString = url.toString();

		return urlString.substring(urlString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
	}
}
