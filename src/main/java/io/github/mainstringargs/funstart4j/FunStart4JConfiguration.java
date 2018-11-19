package io.github.mainstringargs.funstart4j;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class FunStart4JConfiguration.
 */
public class FunStart4JConfiguration {

	/** The arguments for JVM. */
	private List<String> argumentsForJVM = new ArrayList<String>();

	/** The java home. */
	private String javaHome = System.getProperty("java.home");

	/**
	 * Gets the configuration from arguments.
	 *
	 * @param args the args
	 * @return the configuration from arguments
	 */
	public static FunStart4JConfiguration getConfigurationFromArguments(String[] args) {
		String jvmProperty = "-J";
		String javaHomeOverride = "-JavaHome-";

		FunStart4JConfiguration config = new FunStart4JConfiguration();

		for (String arg : args) {
			if (arg.startsWith(javaHomeOverride)) {
				config.setJavaHome(javaHomeOverride);
			} else if (arg.startsWith(jvmProperty)) {
				config.getArgumentsForJVM().add(arg.replace(jvmProperty, ""));
			}
		}

		return config;

	}

	/**
	 * Gets the arguments for JVM.
	 *
	 * @return the arguments for JVM
	 */
	public List<String> getArgumentsForJVM() {
		return argumentsForJVM;
	}

	/**
	 * Sets the arguments for JVM.
	 *
	 * @param argumentsForJVM the new arguments for JVM
	 */
	public void setArgumentsForJVM(List<String> argumentsForJVM) {
		this.argumentsForJVM = argumentsForJVM;
	}

	/**
	 * Gets the java home.
	 *
	 * @return the java home
	 */
	public String getJavaHome() {
		return javaHome;
	}

	/**
	 * Sets the java home.
	 *
	 * @param javaHome the new java home
	 */
	public void setJavaHome(String javaHome) {
		this.javaHome = javaHome;
	}
}
