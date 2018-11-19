package io.github.mainstringargs.funstart4j;

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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mainstringargs.funstart4j.schema.ApplicationDesc;
import io.github.mainstringargs.funstart4j.schema.Argument;
import io.github.mainstringargs.funstart4j.schema.Extension;
import io.github.mainstringargs.funstart4j.schema.Information;
import io.github.mainstringargs.funstart4j.schema.Jar;
import io.github.mainstringargs.funstart4j.schema.Jnlp;
import io.github.mainstringargs.funstart4j.schema.Nativelib;
import io.github.mainstringargs.funstart4j.schema.Property;
import io.github.mainstringargs.funstart4j.schema.Resources;

// TODO: Auto-generated Javadoc
/**
 * The Class JNLPHandler.
 */
public class JNLPHandler {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(JNLPHandler.class);

	/** The jnlp uri. */
	private URI jnlpUri;

	/** The folder location. */
	private String folderLocation;

	/** The absolute classpath references. */
	private Map<URI, File> absoluteClasspathReferences = new ConcurrentHashMap<>();

	/** The relative classpath references. */
	private Map<URI, String> relativeClasspathReferences = new ConcurrentHashMap<>();

	/** The main method. */
	private String mainMethod = "";

	/** The properties. */
	private LinkedHashMap<String, String> properties = new LinkedHashMap<>();

	/** The arguments. */
	private List<Argument> arguments = new ArrayList<Argument>();

	/** The executor. */
	private ExecutorService executor = Executors.newCachedThreadPool();

	private List<JNLPStatusObserver> observers = new ArrayList<JNLPStatusObserver>();

	/**
	 * Instantiates a new JNLP handler.
	 *
	 * @param jnlpUri the jnlp uri
	 */
	public JNLPHandler(URI jnlpUri) {
		this.jnlpUri = jnlpUri;
	}

	/**
	 * Instantiates a new JNLP handler.
	 *
	 * @param jnlpUri        the jnlp uri
	 * @param folderLocation the folder location
	 */
	public JNLPHandler(URI jnlpUri, String folderLocation) {
		this.jnlpUri = jnlpUri;
		this.folderLocation = folderLocation;
	}

	public void addJNLPStatusObserver(JNLPStatusObserver jnlpStatusObserver) {
		observers.add(jnlpStatusObserver);
	}

	public void notifyObservers(Downloader jnlpDownloader, JNLPStatus jnlpStatus) {
//		synchronized (JNLPHandler.class) {
		for (JNLPStatusObserver observer : observers) {
			observer.statusChange(jnlpDownloader, jnlpStatus);
		}
//		}
	}

	/**
	 * Parses the JNLP.
	 */
	public void parseJNLP() {

		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance(Jnlp.class);
		} catch (JAXBException e) {
			if (logger.isInfoEnabled())
				logger.info("JAXBException", e);
		}

		Unmarshaller um = null;
		try {
			um = jc.createUnmarshaller();
		} catch (JAXBException e) {
			if (logger.isInfoEnabled())
				logger.info("Exception creating Unmarshaller", e);
		}

		Jnlp data = null;
		try {
			data = (Jnlp) um.unmarshal(jnlpUri.toURL());
		} catch (JAXBException | MalformedURLException e) {
			if (logger.isInfoEnabled())
				logger.info("Exception Unmarshalling", e);
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

		if (logger.isInfoEnabled())
			logger.info("All work submitted; waiting for finish.");
		try {
			executor.awaitTermination(10, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			if (logger.isInfoEnabled())
				logger.info("InterruptedException", e);
		}

		if (logger.isInfoEnabled())
			logger.info("Finished.");
	}

	/**
	 * Parses the JNLP.
	 *
	 * @param jnlpUri the jnlp uri
	 */
	public void parseJNLP(URI jnlpUri) {

		if (logger.isInfoEnabled())
			logger.info("Parsing... " + jnlpUri);

		String parentUri = getParentUri(jnlpUri);

		if (logger.isInfoEnabled())
			logger.info("Parent URI " + parentUri);

		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance(Jnlp.class);
		} catch (JAXBException e) {
			if (logger.isInfoEnabled())
				logger.info("JAXBException", e);
		}

		Unmarshaller um = null;
		try {
			um = jc.createUnmarshaller();
		} catch (JAXBException e) {
			if (logger.isInfoEnabled())
				logger.info("JAXBException", e);
		}

		Jnlp data = null;
		try {
			data = (Jnlp) um.unmarshal(jnlpUri.toURL());
		} catch (JAXBException | MalformedURLException e) {
			if (logger.isInfoEnabled())
				logger.info("JAXBException | MalformedURLException", e);
		}

		if (logger.isInfoEnabled()) {
			logger.info(data.getCodebase());
			logger.info(data.getHref());
		}

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
			logger.info(resource.getArch() + " " + resource.getOs());
			for (Object libRef : resource.getJavaOrJ2SeOrJarOrNativelibOrExtensionOrPropertyOrPackage()) {

				if (libRef instanceof Jar) {
					Jar jarRef = (Jar) libRef;

					if (logger.isDebugEnabled())
						logger.debug(jarRef.getDownload() + " " + jarRef.getHref() + " " + jarRef.getMain() + " "
								+ jarRef.getPart() + " " + jarRef.getSize() + " " + jarRef.getVersion());

					final URI uri = getURIReference(data.getCodebase(), parentUri, jarRef.getHref());

					final Downloader dLoader = new Downloader(uri,
							folderLocation + File.separator + getFileNameFromUri(uri));

					executor.submit(new Callable<Void>() {

						@Override
						public Void call() throws Exception {

							if (!absoluteClasspathReferences.containsKey(uri)) {

								notifyObservers(dLoader, JNLPStatus.START);

								// placeholder until we get the real file
								absoluteClasspathReferences.put(uri, new File("."));
								File file = dLoader.getFile(true);
								absoluteClasspathReferences.put(uri, file);

								relativeClasspathReferences.put(uri, dLoader.getRelativeFileReference());

								notifyObservers(dLoader, JNLPStatus.FINISH);

							}

							return null;
						}
					});

				} else if (libRef instanceof Extension) {
					Extension extensionRef = (Extension) libRef;

					if (logger.isDebugEnabled())
						logger.debug(extensionRef.getHref() + " " + extensionRef.getName() + " "
								+ extensionRef.getVersion() + " " + extensionRef.getExtDownload());

					parseJNLP(getURIReference(data.getCodebase(), parentUri, extensionRef.getHref()));

				} else if (libRef instanceof Nativelib) {
					Nativelib nativeLibRef = (Nativelib) libRef;

					if (logger.isDebugEnabled())
						logger.debug(
								nativeLibRef.getDownload() + " " + nativeLibRef.getHref() + " " + nativeLibRef.getPart()
										+ " " + nativeLibRef.getSize() + " " + nativeLibRef.getVersion());

					final URI uri = getURIReference(data.getCodebase(), parentUri, nativeLibRef.getHref());

					try {
						final Downloader dLoader = new Downloader(uri,
								folderLocation + File.separator + getFileNameFromUri(uri));

						executor.submit(new Callable<Void>() {

							@Override
							public Void call() throws Exception {

								if (!absoluteClasspathReferences.containsKey(uri)) {

									notifyObservers(dLoader, JNLPStatus.START);

									// placeholder until we get the real file
									absoluteClasspathReferences.put(uri, new File("."));
									File file = dLoader.getFile(true);
									absoluteClasspathReferences.put(uri, file);

									relativeClasspathReferences.put(uri, dLoader.getRelativeFileReference());

									notifyObservers(dLoader, JNLPStatus.FINISH);

								}

								return null;
							}
						});

					} catch (Exception e) {
						if (logger.isInfoEnabled()) {
							logger.info("Exception", e);
						}
					}

				} else if (libRef instanceof Property) {
					Property propertyRef = (Property) libRef;

					if (logger.isDebugEnabled())
						logger.debug(propertyRef.getName() + " " + propertyRef.getValue());

					properties.put(propertyRef.getName(), propertyRef.getValue());
				}
			}
		}
	}

	/**
	 * Generate scripts.
	 */
	public void generateScripts() {

	}

	/**
	 * Run application.
	 *
	 * @param javaHome        the java home
	 * @param argumentsForJVM the arguments for JVM
	 */
	public void runApplication(String javaHome, List<String> argumentsForJVM) {
		String classpath = "";

		for (String file : relativeClasspathReferences.values()) {
			classpath += file + File.pathSeparator;
		}

		List<String> passedInProps = new ArrayList<String>();

		for (Entry<String, String> prop : properties.entrySet()) {
			String propString = "-D" + prop.getKey() + "=" + prop.getValue();
			passedInProps.add(propString);
		}

		List<String> fullCommand = new ArrayList<>();
		fullCommand.add(getJavaLocation(javaHome));
		fullCommand.addAll(passedInProps);
		if (argumentsForJVM != null) {
			fullCommand.addAll(argumentsForJVM);
		}
		fullCommand.add("-classpath");
		fullCommand.add(classpath);
		fullCommand.add(mainMethod);

		if (logger.isInfoEnabled())
			logger.info("Executing " + fullCommand);

		ProcessBuilder pb = new ProcessBuilder(fullCommand.toArray(new String[] {}));
		pb.redirectErrorStream(true);

		Process p = null;

		try {
			p = pb.start();
		} catch (IOException e) {
			if (logger.isInfoEnabled())
				logger.info("IOException", e);
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String readline;
		int i = 0;
		try {
			while ((readline = reader.readLine()) != null) {
				logger.info(++i + " " + readline);
			}
		} catch (IOException e) {
			if (logger.isInfoEnabled())
				logger.info("IOException", e);
		}
	}

	/**
	 * Gets the java location.
	 *
	 * @param javaHome the java home
	 * @return the java location
	 */
	private String getJavaLocation(String javaHome) {

		File javaHomeFile = new File(javaHome + File.separator + "bin" + File.separator + "java");
		if (logger.isInfoEnabled())
			logger.info("Looking for java in " + javaHomeFile.getAbsolutePath());

		if (javaHomeFile.exists()) {
			if (logger.isInfoEnabled())
				logger.info("Found Java @ " + javaHomeFile.getAbsolutePath());
			return javaHomeFile.getAbsolutePath();
		}

		// Append .exe for Windows
		javaHomeFile = new File(javaHome + File.separator + "bin" + File.separator + "java.exe");
		logger.info("Looking for java in " + javaHomeFile.getAbsolutePath());

		if (javaHomeFile.exists()) {
			if (logger.isInfoEnabled())
				logger.info("Found Java @ " + javaHomeFile.getAbsolutePath());
			return javaHomeFile.getAbsolutePath();
		}

		// if they added bin to java.home specifier
		javaHomeFile = new File(javaHome + File.separator + "java");
		if (logger.isInfoEnabled())
			logger.info("Looking for java in " + javaHomeFile.getAbsolutePath());

		if (javaHomeFile.exists()) {
			if (logger.isInfoEnabled())
				logger.info("Found Java @ " + javaHomeFile.getAbsolutePath());
			return javaHomeFile.getAbsolutePath();
		}

		// Append .exe for Windows
		javaHomeFile = new File(javaHome + File.separator + "java.exe");
		if (logger.isInfoEnabled())
			logger.info("Looking for java in " + javaHomeFile.getAbsolutePath());

		if (javaHomeFile.exists()) {
			if (logger.isInfoEnabled())
				logger.info("Found Java @ " + javaHomeFile.getAbsolutePath());
			return javaHomeFile.getAbsolutePath();
		}

		// if we can't find it, hope its on the PATH
		if (logger.isInfoEnabled())
			logger.info("Using java on the PATH");

		return "java";
	}

	/**
	 * Gets the URI reference.
	 *
	 * @param codeBase  the code base
	 * @param parentRef the parent ref
	 * @param fileName  the file name
	 * @return the URI reference
	 */
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
			if (logger.isInfoEnabled())
				logger.info("URISyntaxException", e);
		}

		return theUri.normalize();

	}

	/**
	 * Gets the file name from uri.
	 *
	 * @param uri the uri
	 * @return the file name from uri
	 */
	public static String getFileNameFromUri(URI uri) {

		String uriString = uri.toString();

		return uriString.substring(uriString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
	}

	/**
	 * Gets the parent uri.
	 *
	 * @param uri the uri
	 * @return the parent uri
	 */
	private String getParentUri(URI uri) {
		String uriString = uri.toString();

		return uriString.substring(0, uriString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
	}

}
