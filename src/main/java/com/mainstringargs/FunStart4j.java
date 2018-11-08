package com.mainstringargs;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class FunStart4j.
 */
public class FunStart4j {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(FunStart4j.class);

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		String jvmProperty = "-J";
		String javaHomeOverride = "-JavaHome-";

		List<String> argumentsForJVM = new ArrayList<String>();
		String javaHome = System.getProperty("java.home");

		for (String arg : args) {
			if (arg.startsWith(javaHomeOverride)) {
				javaHome = javaHomeOverride;
			} else if (arg.startsWith(jvmProperty)) {
				argumentsForJVM.add(arg.replace(jvmProperty, ""));
			}
		}

		if (logger.isInfoEnabled())
			logger.info("Found JVM Properties: " + argumentsForJVM);

		URL website = null;
		try {
			if (args.length != 0) {
				website = new URL(args[args.length - 1]);
			} else {
				website = new URL("https://worldwind.arc.nasa.gov/java/latest/webstart/AirspaceBuilder.jnlp");
			}

		} catch (MalformedURLException e) {
			if (logger.isInfoEnabled())
				logger.info("Exception while creating URL", e);
		}

		try {
			if (logger.isInfoEnabled())
				logger.info("Grabbing JNLP from: " + website.toURI());

			JNLPHandler jnlpHandler = new JNLPHandler(website.toURI());

			jnlpHandler.parseJNLP();

			jnlpHandler.runApplication(javaHome, argumentsForJVM);
		} catch (URISyntaxException e) {
			if (logger.isInfoEnabled())
				logger.info("Exception while creating URI", e);
		}

	}

}
