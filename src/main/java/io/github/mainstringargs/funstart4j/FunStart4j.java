package io.github.mainstringargs.funstart4j;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

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

		FunStart4JConfiguration configuration = FunStart4JConfiguration.getConfigurationFromArguments(args);

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

			jnlpHandler.runApplication(configuration);
		} catch (URISyntaxException e) {
			if (logger.isInfoEnabled())
				logger.info("Exception while creating URI", e);
		}

	}

}
