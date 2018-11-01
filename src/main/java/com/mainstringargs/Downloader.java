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

	public File getFile(boolean retryIfFailed) {

		File file = new File(downloadedFile);

		long newFileSize = getFileSize();
		long newFileModified = getLastModified();

		if (file.exists()) {
			long existingFileSize = file.length();
			long existingFileModified = file.lastModified();

			System.out.println(downloadedFile + " " + existingFileSize + ":" + newFileSize + " " + existingFileModified
					+ ":" + newFileModified);

			if (existingFileSize == newFileSize && existingFileModified == newFileModified) {
				System.out.println(downloadedFile + " is cached, no need to re-download");
				return file;
			}
		}

		if (this.fileReference == null) {
			URLConnection connection;

			InputStream in = null;

			FileOutputStream fos = null;
			try {
				System.err.println("Downloading " + hostedFileUrl);
				connection = hostedFileUrl.toURL().openConnection();

				in = connection.getInputStream();

				fos = new FileOutputStream(downloadedFile);
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

				file.setLastModified(getLastModified());
				this.fileReference = file;

				System.err
						.println("Download Finished " + hostedFileUrl + " to " + this.fileReference.getAbsolutePath());

			} catch (IOException e) {
				e.printStackTrace();

				if (in != null) {
					try {
						in.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

				if (retryIfFailed) {
					// only retry once
					getFile(false);
				}
			}

			if (this.fileReference == null) {
				// only retry once
				getFile(false);
			} else if (this.fileReference.length() != newFileSize) {
				// only retry once
				getFile(false);
			}

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
