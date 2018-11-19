package io.github.mainstringargs.funstart4j;

// TODO: Auto-generated Javadoc
/**
 * An asynchronous update interface for receiving notifications
 * about JNLPStatus information as the JNLPStatus is constructed.
 */
public interface JNLPStatusObserver {

	/**
	 * This method is called when information about an JNLPStatus
	 * which was previously requested using an asynchronous
	 * interface becomes available.
	 *
	 * @param jnlpFile the jnlp file
	 * @param jnlpStatus the jnlp status
	 */
	public void statusChange(Downloader jnlpFile, JNLPStatus jnlpStatus);
}
