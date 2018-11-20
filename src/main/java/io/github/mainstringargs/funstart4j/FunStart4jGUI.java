package io.github.mainstringargs.funstart4j;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// TODO: Auto-generated Javadoc
/**
 * The Class FunStart4jGUI.
 */
public class FunStart4jGUI extends JPanel implements ActionListener, PropertyChangeListener {

	/** The url field. */
	private JTextField urlField;

	/** The progress bar. */
	private JProgressBar progressBar;

	/** The start button. */
	private JButton startButton;

	/** The run button. */
	private JButton runButton;

	/** The task output. */
	private JTextField taskOutput;

	/** The task. */
	private Task task;

	/** The jnlp handler. */
	private JNLPHandler jnlpHandler = null;

	/** The initial uri. */
	private URI initialUri;

	/** The configuration. */
	private static FunStart4JConfiguration configuration;

	/**
	 * The Class Task.
	 */
	class Task extends SwingWorker<Void, Void> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		/*
		 * Main task. Executed in background thread.
		 */
		@Override
		public Void doInBackground() {

			try {
				jnlpHandler = new JNLPHandler(new URI(urlField.getText()));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			jnlpHandler.addJNLPStatusObserver(new JNLPStatusObserver() {

				Map<Downloader, JNLPStatus> statuses = new ConcurrentHashMap<>();

				@Override
				public void statusChange(final Downloader jnlpFile, final JNLPStatus jnlpStatus) {

					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							if (jnlpStatus == JNLPStatus.START) {
								taskOutput.setText("Loading..." + jnlpFile.getRelativeFileReference());
							} else if (jnlpStatus == JNLPStatus.FINISH) {
								taskOutput.setText("Finished..." + jnlpFile.getRelativeFileReference());
							}

						}
					});

					statuses.put(jnlpFile, jnlpStatus);

					int total = statuses.size();

					int numFinished = 0;

					for (Entry<Downloader, JNLPStatus> entry : statuses.entrySet()) {
						if (entry.getValue() == JNLPStatus.FINISH) {
							numFinished++;
						}
					}

					final double percentComplete = 100 * (double) numFinished / (double) total;

					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							setProgress((int) percentComplete);
						}
					});

				}
			});

			jnlpHandler.parseJNLP();

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#done()
		 */
		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {

			setProgress(100);
			startButton.setEnabled(true);
			if (initialUri == null) {
				urlField.setEditable(true);
			}
			setCursor(null); // turn off the wait cursor
			taskOutput.setText("Done!\n");
			runButton.setEnabled(true);

			if (initialUri != null) {

				runApplication();

			}
		}
	}

	/**
	 * Instantiates a new fun start 4 j GUI.
	 *
	 * @param uri the uri
	 */
	public FunStart4jGUI(URI uri) {
		super(new BorderLayout());

		initialUri = uri;

		urlField = new JTextField(uri != null ? uri.toString() : "");
		urlField.setPreferredSize(new Dimension(300, 20));

		urlField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				validInput();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				validInput();
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				validInput();
			}

			public void validInput() {
				String text = urlField.getText();
				boolean isValid = !text.trim().isEmpty() && text.toLowerCase().endsWith(".jnlp");

				startButton.setEnabled(isValid);

				if (!isValid && runButton.isEnabled()) {
					runButton.setEnabled(false);
				}

			}
		});

		if (uri != null) {
			urlField.setEditable(false);
		} else {
			urlField.setEditable(true);
		}

		startButton = new JButton("Start");
		startButton.setActionCommand("start");
		startButton.setEnabled(false);
		startButton.addActionListener(this);

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		taskOutput = new JTextField();
		taskOutput.setHorizontalAlignment(JTextField.CENTER);
		taskOutput.setEditable(false);

		runButton = new JButton("Run");
		runButton.setActionCommand("run");
		runButton.addActionListener(this);
		runButton.setEnabled(false);

		JPanel panel = new JPanel();
		panel.add(urlField);

		if (uri == null) {
			panel.add(startButton);
		}

		add(panel, BorderLayout.NORTH);
		add(progressBar, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add((taskOutput), BorderLayout.CENTER);

		JPanel bottomButtonPanel = new JPanel();

		if (uri == null) {
			bottomButtonPanel.add(runButton);
		}

		bottomPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

		add(bottomPanel, BorderLayout.SOUTH);

		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

	}

	/**
	 * Invoked when the user presses the start button.
	 *
	 * @param evt the evt
	 */
	public void actionPerformed(ActionEvent evt) {

		if (evt.getActionCommand().equals("start")) {
			startDownload();
		} else if (evt.getActionCommand().equals("run")) {
			runApplication();
		}

	}

	/**
	 * Run application.
	 */
	private void runApplication() {

		taskOutput.setText("Starting Application!\n");

		new Thread(new Runnable() {

			@Override
			public void run() {

				hideFrameInFuture(3000L);

				Runnable jnlpRunnable = new Runnable() {

					@Override
					public void run() {

						jnlpHandler.runApplication(configuration);
					}
				};

				Thread jnlpThread = new Thread(jnlpRunnable);
				jnlpThread.start();

				try {
					jnlpThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.exit(0);

			}

		}).start();

	}

	/**
	 * Hide frame in future.
	 *
	 * @param futureTimeInMs the future time in ms
	 */
	protected void hideFrameInFuture(final long futureTimeInMs) {

		Runnable frameWaitRunnable = new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(futureTimeInMs);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {

						JFrame mainFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, FunStart4jGUI.this);

						mainFrame.setVisible(false);

					}
				});

			}
		};

		Thread frameWaitThread = new Thread(frameWaitRunnable);
		frameWaitThread.start();
	}

	/**
	 * Start download.
	 */
	private void startDownload() {
		runButton.setEnabled(false);
		progressBar.setValue(0);
		startButton.setEnabled(false);
		urlField.setEditable(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		// Instances of javax.swing.SwingWorker are not reusuable, so
		// we create new instances as needed.
		task = new Task();
		task.addPropertyChangeListener(this);
		task.execute();
	}

	/**
	 * Invoked when task's progress property changes.
	 *
	 * @param evt the evt
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
		}
	}

	/**
	 * Create the GUI and show it. As with all GUI code, this must run on the
	 * event-dispatching thread.
	 *
	 * @param uri           the uri
	 * @param configuration the configuration
	 */
	public static void createAndShowGUI(URI uri, FunStart4JConfiguration configuration) {

		FunStart4jGUI.configuration = configuration;

		// Create and set up the window.
		JFrame frame = new JFrame("FunStart4j");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		FunStart4jGUI newContentPane = new FunStart4jGUI(uri);
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		// centers frame on screen
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		if (uri != null) {
			newContentPane.startDownload();
		}
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(final String[] args) {

		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(null, FunStart4JConfiguration.getConfigurationFromArguments(args));
			}
		});
	}
}