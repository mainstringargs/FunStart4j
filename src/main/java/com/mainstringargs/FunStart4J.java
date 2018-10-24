package com.mainstringargs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class FunStart4J {

	public static void main(String[] args) {

		File folder = new File("lib");
		folder.mkdir();

		URL website = null;
		try {
			website = new URL("https://worldwind.arc.nasa.gov/java/latest/webstart/AirspaceBuilder.jnlp");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			JNLPHandler jnlpHandler = new JNLPHandler(website.toURI(), folder.getAbsolutePath());
			jnlpHandler.parseJNLP();
			
			jnlpHandler.runApplication();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
