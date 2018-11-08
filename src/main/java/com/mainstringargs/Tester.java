package com.mainstringargs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.mainstringargs.schema.Extension;
import com.mainstringargs.schema.Jar;
import com.mainstringargs.schema.Jnlp;
import com.mainstringargs.schema.Nativelib;
import com.mainstringargs.schema.Property;
import com.mainstringargs.schema.Resources;

// TODO: Auto-generated Javadoc
/**
 * The Class Tester.
 */
public class Tester {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
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

//		System.out.println(getFileSize(website));
//		System.out.println(getLastModified(website));
//
//		File file = new File("worldwind.jnlp");
//
//		if (file.exists()) {
//			System.out.println(file.length());
//		}
//
//		URI fileUrl = file.toURI();
//
//		try (InputStream inputStream = website.openStream();
//				ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
//				FileOutputStream fileOutputStream = new FileOutputStream("worldwind.jnlp")) {
//			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		try {
			parseJNLP(website.toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Parses the JNLP.
	 *
	 * @param fileUrl the file url
	 */
	private static void parseJNLP(URI fileUrl) {

		System.err.println("Parsing... " + fileUrl);

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
			data = (Jnlp) um.unmarshal(fileUrl.toURL());
		} catch (JAXBException | MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(data.getCodebase());
		System.out.println(data.getHref());

		List<Resources> resources = data.getResources();

		for (Resources resource : resources) {
			System.out.println(resource.getArch() + " " + resource.getOs());
			for (Object libRef : resource.getJavaOrJ2SeOrJarOrNativelibOrExtensionOrPropertyOrPackage()) {

				if (libRef instanceof Jar) {
					Jar jarRef = (Jar) libRef;

					System.out.println(jarRef.getDownload() + " " + jarRef.getHref() + " " + jarRef.getMain() + " "
							+ jarRef.getPart() + " " + jarRef.getSize() + " " + jarRef.getVersion());

					URL url = getURLReference(data.getCodebase(), jarRef.getHref());
					getFile(url, "lib" + File.separator + getFileNameFromUrl(url));

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
					getFile(url, "lib" + File.separator + getFileNameFromUrl(url));

				} else if (libRef instanceof Property) {
					Property propertyRef = (Property) libRef;

					System.out.println(propertyRef.getName() + " " + propertyRef.getValue());
				}
			}
		}
	}

	/**
	 * Gets the file.
	 *
	 * @param url      the url
	 * @param fileName the file name
	 * @return the file
	 */
	private static File getFile(URL url, String fileName) {

		System.out.println("Downloading " + url + " size " + getFileSize(url));

		File downloadedFile = null;
		try {
			URLConnection connection = url.openConnection();
			InputStream in = connection.getInputStream();
			downloadedFile = new File(fileName);
			FileOutputStream fos = new FileOutputStream(downloadedFile);
			byte[] buf = new byte[512];
			while (true) {
				int len = in.read(buf);
				if (len == -1) {
					break;
				}
				fos.write(buf, 0, len);
			}
			in.close();
			fos.flush();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return downloadedFile;
	}

	/**
	 * Gets the file size.
	 *
	 * @param url the url
	 * @return the file size
	 */
	private static int getFileSize(URL url) {
		URLConnection conn = null;
		try {
			conn = url.openConnection();
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).setRequestMethod("HEAD");
			}
			conn.getInputStream();
			return conn.getContentLength();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).disconnect();
			}
		}
	}

	/**
	 * Gets the last modified.
	 *
	 * @param url the url
	 * @return the last modified
	 */
	private static long getLastModified(URL url) {
		URLConnection conn = null;
		try {
			conn = url.openConnection();
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).setRequestMethod("HEAD");
			}
			conn.getInputStream();
			return conn.getLastModified();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).disconnect();
			}
		}
	}

	/**
	 * Gets the URL reference.
	 *
	 * @param codeBase the code base
	 * @param fileName the file name
	 * @return the URL reference
	 */
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

	/**
	 * Gets the file name from url.
	 *
	 * @param url the url
	 * @return the file name from url
	 */
	public static String getFileNameFromUrl(URL url) {

		String urlString = url.toString();

		return urlString.substring(urlString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
	}
}
