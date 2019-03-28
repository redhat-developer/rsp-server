/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.schema;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

/**
 * Scans for (dao) classes that match the package "org.jboss.tools.rsp.api.dao".
 * 
 * @author Andre Dietisheim
 * @author Rob Stryker
 */
public class DaoClasses {

	private static final Logger LOG = Logger.getLogger(DaoClasses.class);

	private static final String PROTOCOL_JAR = "jar";
	private static final String PROTOCOL_FILE = "file";
	private static final String DAO_PACKAGE = "org.jboss.tools.rsp.api.dao";
	private static final char SUBPACKAGE_SEPARATOR = '.';
	private static final String CLASSFILE_SUFFIX = ".class";
	
	private String packageName;

	public DaoClasses() {
		this(DAO_PACKAGE);
	}

	protected DaoClasses(String daoPackage) {
		this.packageName = daoPackage;
	}

	public Class<?>[] getAll() throws IOException {
		List<Class<?>> classes = getClasses(packageName);
		return classes.toArray(new Class<?>[classes.size()]);
	}

	/**
	 * Returns the classes that exist in the given package. Sub-packages are not
	 * taken into account. Returns an empty list if no classes were found or an
	 * error occurred while trying to retrieve them.
	 * 
	 * @param packageName The package
	 * @return The classes that are found in the given package
	 * @throws IOException if the class files cannot be read
	 */
	private List<Class<?>> getClasses(final String packageName) throws IOException {
	    ClassLoader classLoader = getClassloader();
	    if (classLoader == null) {
	    	throw new IllegalArgumentException("Could not get classloader from current thread.");
	    }
	    String path = getPath(packageName);
	    Enumeration<URL> resources = classLoader.getResources(path);
	    return toStream(resources)
	    	.flatMap(url -> 
		    	(Stream<Class<?>>) findClasses(packageName, url).stream())
		    .filter(Objects::nonNull)
		    .collect(Collectors.toList());
	}

	private List<Class<?>> findClasses(final String packageName, URL url) {
		String filePath = url.getFile();
		String protocol = url.getProtocol();
		try {
			if (PROTOCOL_FILE.equals(protocol)) {
				File directory = new File(filePath);
				if (directory.isDirectory()) {
					return findClassesInDirectory(directory, packageName);
				}
			} else if (PROTOCOL_JAR.equals(protocol)) {
				return findClassesInJar(toURI(url), packageName);
			}
		} catch(IOException e) {
			LOG.error(MessageFormat.format("Could not scan dao classes in package {0}", packageName), e);
		}
		return Collections.<Class<?>>emptyList();
	}

	private List<Class<?>> findClassesInJar(URI uri, String packageName) throws IOException {
		int exclamation = uri.toString().indexOf('!');
		String substr = uri.toString().substring(0, exclamation);
		String fileLoc = substr.substring("jar:file:".length());
		String nestedPath = uri.toString().substring(exclamation + 2);
		try (ZipFile zipFile = new ZipFile(fileLoc)) {
			return toStream(zipFile.entries())
					.map(zipEntry -> {
						String entryName = zipEntry.getName();
						if (entryName.startsWith(nestedPath) 
								&& entryName.endsWith(CLASSFILE_SUFFIX)) {
							String entryClassName = entryName.substring(nestedPath.length() + 1);
							String className = getClassName(entryClassName, packageName);
							if (!className.contains("/")) {
								return className;
							}
						}
						return null;
					})
					.filter(Objects::nonNull)
					.sorted()
					.map(className -> {
						try {
							return Class.forName(className, true, getClassloader());
						} catch (ClassNotFoundException e) {
							LOG.error("Could not instantiate class " + className + " that we found in jar " + uri.toString());
							e.printStackTrace();
							return null;
						}
					})
					.filter(Objects::nonNull).collect(Collectors.toList());
		}
	}

	private URI toURI(URL url) throws IOException {
		URI uri = null;
		try {
			uri = url.toURI();
		} catch(URISyntaxException urise) {
			throw new IOException(urise);
		}
		return uri;
	}


	/**
	 * Returns the classes that exist in the given package.
	 *
	 * @param directory The directory to look into
	 * @param packageName The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException 
	 */
	private List<Class<?>> findClassesInDirectory(File directory, String packageName) throws IOException {
	    try(Stream<Path> files = Files.walk(directory.toPath(), FileVisitOption.FOLLOW_LINKS)) {
	    	// no sub-packages
	    	return files
	    			.filter(path -> {
			    		File file = path.toFile();
			    		return !file.exists()
			    				|| !file.isDirectory();
			    		})
	    			.filter(path -> path.toFile().getName().endsWith(CLASSFILE_SUFFIX))
			    	.map(path -> {
			    		String className = getClassName(path.getFileName().toString(), packageName);
						try {
							return Class.forName(className);
						} catch (ClassNotFoundException e) {
							LOG.error(MessageFormat.format(
									"Could not instantiate class {0} in file {1}", 
									className, path.getFileName()));
							return null;
						}
					})
			    	.filter(Objects::nonNull)
		   			.collect(Collectors.toList());
		}
	}

	protected ClassLoader getClassloader() {
		return Thread.currentThread().getContextClassLoader();
	}

	private String getPath(final String packageName) {
		return packageName.replace(SUBPACKAGE_SEPARATOR, '/');
	}

	private String getClassName(String filename, String packageName) {
		return packageName + SUBPACKAGE_SEPARATOR + filename.substring(0, filename.length() - 6);
	}

	private <T> Stream<T> toStream(final Enumeration<T> enumeration) {
		return StreamSupport.stream(
	    	    Spliterators.spliteratorUnknownSize(
	    	    		new Iterator<T>() {

	    	    			@Override
	    	    			public T next() {
    	    					return enumeration.nextElement();
	    	    	        }

	    	    			@Override
	    	    			public boolean hasNext() {
	    	                    return enumeration.hasMoreElements();
	    	                }
	    	    		}, 
	    	    		Spliterator.ORDERED), false);
	}

	public static void main(String[] args) throws IOException {
		Class<?>[] daos = new DaoClasses().getAll();
		Arrays.stream(daos)
			.sorted((clazz1, clazz2) -> Collator.getInstance().compare(clazz1.getSimpleName(), clazz2.getSimpleName()))
			.forEach(dao -> System.err.println(dao.getSimpleName() + ", "));
	}
	
}
