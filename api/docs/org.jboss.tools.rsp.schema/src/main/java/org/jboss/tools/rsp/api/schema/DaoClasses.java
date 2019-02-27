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
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DaoClasses {
	
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
	    	.map(url -> new File(url.getFile()))
	    	.flatMap(file -> {
				try {
					return findClasses(file, packageName).stream();
				} catch (ClassNotFoundException | IOException e) {
					return Collections.<Class<?>>emptyList().stream();
				}
			})
	    	.collect(Collectors.toList());
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
	private List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException, IOException {
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
						try {
							String className = getClassName(path.getFileName().toString(), packageName);
							return Class.forName(className);
						} catch (ClassNotFoundException e) {
							return null;
						}
					})
			    	.filter(Objects::nonNull)
		   			.collect(Collectors.toList());
		}
	}

	private ClassLoader getClassloader() {
		return Thread.currentThread().getContextClassLoader();
	}

	private String getPath(final String packageName) {
		return packageName.replace(SUBPACKAGE_SEPARATOR, File.separatorChar);
	}

	private String getClassName(String filename, String packageName) {
		return packageName + SUBPACKAGE_SEPARATOR + filename.substring(0, filename.length() - 6);
	}

	private Stream<URL> toStream(final Enumeration<URL> enumeration) {
		return StreamSupport.stream(
	    	    Spliterators.spliteratorUnknownSize(
	    	    		new Iterator<URL>() {

	    	    			@Override
	    	    			public URL next() throws NoSuchElementException {
	    	    				try {
	    	    					return enumeration.nextElement();
	    	    				} catch(NoSuchElementException nsee) {
	    	    					throw nsee;
	    	    				}
	    	    	        }

	    	    			@Override
	    	    			public boolean hasNext() {
	    	                    return enumeration.hasMoreElements();
	    	                }

	    	    			@Override
	    	    			public void forEachRemaining(Consumer<? super URL> action) {
	    	                    while(enumeration.hasMoreElements()) {
	    	                    	action.accept(enumeration.nextElement());
	    	                    }
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
