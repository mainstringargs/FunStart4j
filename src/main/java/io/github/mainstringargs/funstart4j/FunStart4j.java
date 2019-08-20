package io.github.mainstringargs.funstart4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

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

		boolean urlRefSuccess = false;
		final FunStart4JConfiguration configuration = FunStart4JConfiguration.getConfigurationFromArguments(args);

		URL url = null;
		try {
			if (args.length != 0) {
				String urlRef = args[args.length - 1];
				try {

					File file = new File(urlRef);
					if (file.isFile()) {
						url = file.toURI().toURL();
						urlRefSuccess = true;
					} 
					else if(urlRef.toLowerCase().startsWith("file")) {
						Path path = Paths.get(urlRef.replaceAll("file://", ""));
						path = path.normalize();
						url = path.toFile().toURI().toURL();
						urlRefSuccess = true;
					}
					else {
						try {
							url = new URL(urlRef);
							urlRefSuccess = true;
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				} catch (Exception e) {
					logger.info("File loading failed, attempting to read as web URL");
					e.printStackTrace();
					try {
						url = new URL(urlRef);
						urlRefSuccess = true;
					} catch (Exception e1) {
						e1.printStackTrace();
					}

				}
			} else {
				url = new URL("https://worldwind.arc.nasa.gov/java/latest/webstart/AirspaceBuilder.jnlp");
				urlRefSuccess = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
			if (logger.isInfoEnabled())
				logger.info("Exception while creating URL", e);
		}

		if (urlRefSuccess) {
			try {
				if (logger.isInfoEnabled())
					logger.info("Grabbing JNLP from: " + url.toURI());

//			JNLPHandler jnlpHandler = new JNLPHandler(website.toURI());
//
//			jnlpHandler.parseJNLP();
//
//			jnlpHandler.runApplication(configuration);

				final URI jnlpLocation = url.toURI();

				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						FunStart4jGUI.createAndShowGUI(jnlpLocation, configuration);
					}
				});

			} catch (URISyntaxException e) {
				if (logger.isInfoEnabled())
					logger.info("Exception while creating URI", e);
			}
		} else {
			logger.error("Loading " + url + " failed!");
		}

	}

}
