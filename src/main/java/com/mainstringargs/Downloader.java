package com.mainstringargs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;

public class Downloader {

	private URI hostedFileUrl;
	private String downloadedFile;
	private File fileReference;

	public Downloader(URI url, String downloadedFile) {
		this.hostedFileUrl = url;
		this.downloadedFile = downloadedFile;
	}

	public File getFile() {

		if (this.fileReference == null) {
			File file = null;
			try {
				System.err.println("Downloading " +hostedFileUrl);
				URLConnection connection = hostedFileUrl.toURL().openConnection();
//				connection.setRequestProperty("User-Agent",
//						"JNLP/1.7.0 javaws/11.181.2.13 (internal) Java/1.8.0_181");

				InputStream in = connection.getInputStream();
				file = new File(downloadedFile);
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

			file.setLastModified(getLastModified());
			this.fileReference = file;

		}

		return this.fileReference;
	}

	public int getFileSize() {
		URLConnection conn = null;
		try {
			conn = hostedFileUrl.toURL().openConnection();
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

	public long getLastModified() {
		URLConnection conn = null;
		try {
			conn = hostedFileUrl.toURL().openConnection();
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

}
