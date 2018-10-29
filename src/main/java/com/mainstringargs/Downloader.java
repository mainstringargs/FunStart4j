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

		File file = new File(downloadedFile);

		if (file.exists()) {
			long existingFileSize = file.length();
			long existingFileModified = file.lastModified();

			long newFileSize = getFileSize();
			long newFileModified = getLastModified();

			System.out.println(downloadedFile + " " + existingFileSize + ":" + newFileSize + " "
					+ existingFileModified + ":" + newFileModified);

			if (existingFileSize == newFileSize && existingFileModified == newFileModified) {
				System.out.println(downloadedFile + " is cached, no need to re-download");
				return file;
			}
		}

		if (this.fileReference == null) {
			try {
				System.err.println("Downloading " + hostedFileUrl);
				URLConnection connection = hostedFileUrl.toURL().openConnection();

				InputStream in = connection.getInputStream();

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

	public long getFileSize() {
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
