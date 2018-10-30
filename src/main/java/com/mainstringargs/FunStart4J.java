package com.mainstringargs;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FunStart4J {

	public static void main(String[] args) {

		String jvmProperty = "-J";

		List<String> argumentsForJVM = new ArrayList<String>();

		for (String arg : args) {
			if (arg.startsWith(jvmProperty)) {
				argumentsForJVM.add(arg.replace(jvmProperty, ""));
			}
		}

		System.out.println("Found JVM Properties: " + argumentsForJVM);

		URL website = null;
		try {
			if (args.length != 0) {
				website = new URL(args[args.length - 1]);
			} else {
				website = new URL("https://worldwind.arc.nasa.gov/java/latest/webstart/AirspaceBuilder.jnlp");
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			JNLPHandler jnlpHandler = new JNLPHandler(website.toURI());

			jnlpHandler.parseJNLP();

			jnlpHandler.runApplication(argumentsForJVM);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
