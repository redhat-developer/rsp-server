/*************************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		 JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.foundation.core.digest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Digest utility class to compute hashes.
 *
 * @author Fred Bricon
 * @since 1.2
 */
public class DigestUtils {

	private DigestUtils() {
	}

	/**
	 * Returns the SHA1 hash of a {@link File}
	 */
	public static String sha1(File file) throws IOException {
		if (file == null) {
			return null;
		}
		return sha1(file.toPath());
	}

	/**
	 * Returns the SHA1 hash of a file referenced by a {@link Path}
	 */
	public static String sha1(Path pathToFile) throws IOException {
		if (pathToFile != null) {
			try (InputStream input = Files.newInputStream(pathToFile)){
				return sha1(input);
			}
		}
		return null;

	}

	/**
	 * Returns the SHA1 hash of a {@link String}
	 */
	public static String sha1(String text) throws IOException {
		if (text == null) {
			return null;
		}
		return sha1(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Returns the SHA1 hash of an {@link InputStream}.
	 */
	public static String sha1(InputStream input) throws IOException {
		if (input == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			byte[] bytes = new byte[16 * 1024];
			int count = 0;
			while ((count = input.read(bytes)) != -1) {
				md.update(bytes, 0, count);
			}
			byte[] digestBytes = md.digest();
			for (int i = 0; i < digestBytes.length; i++) {
				sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1));
			}
		} catch (NoSuchAlgorithmException cantHappen) {
		}
		return sb.toString();
	}

}
