/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.launching;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jdt.internal.launching.LibraryInfo;

public class LibraryInfoCache {
	private static LibraryInfoCache instance = new LibraryInfoCache();
	public static LibraryInfoCache getDefault() {
		return instance;
	}
	
	

	/**
	 * Mapping of top-level VM installation directories to library info for that
	 * VM.
	 */
	private static Map<String, LibraryInfo> fgLibraryInfoMap = null;

	/**
	 * Mapping of the last time the directory of a given SDK was modified.
	 * <br><br>
	 * Mapping: <code>Map&lt;String,Long&gt;</code>
	 * @since 3.7
	 */
	private static Map<String, Long> fgInstallTimeMap = null;
	/**
	 * List of install locations that have been detected to have changed
	 *
	 * @since 3.7
	 */
	private static HashSet<String> fgHasChanged = new HashSet<>();
	/**
	 * Mutex for checking the time stamp of an install location
	 *
	 * @since 3.7
	 */
	private static Object installLock = new Object();

	
	
	
	/**
	 * Returns the library info that corresponds to the specified JRE install
	 * path, or <code>null</code> if none.
	 *
	 * @param javaInstallPath the absolute path to the java executable
	 * @return the library info that corresponds to the specified JRE install
	 * path, or <code>null</code> if none
	 */
	public static LibraryInfo getLibraryInfo(String javaInstallPath) {
		if (fgLibraryInfoMap == null) {
			restoreLibraryInfo();
		}
		return fgLibraryInfoMap.get(javaInstallPath);
	}
	

	/**
	 * Checks to see if the time stamp of the file describe by the given location string
	 * has been modified since the last recorded time stamp. If there is no last recorded
	 * time stamp we assume it has changed. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=266651 for more information
	 *
	 * @param location the location of the SDK we want to check the time stamp for
	 * @return <code>true</code> if the time stamp has changed compared to the cached one or if there is
	 * no recorded time stamp, <code>false</code> otherwise.
	 *
	 * @since 3.7
	 */
	public static boolean timeStampChanged(String location) {
		synchronized (installLock) {
			if(fgHasChanged.contains(location)) {
				return true;
			}
			File file = new File(location);
			if(file.exists()) {
				if(fgInstallTimeMap == null) {
					readInstallInfo();
				}
				Long stamp = fgInstallTimeMap.get(location);
				long fstamp = file.lastModified();
				if(stamp != null) {
					if(stamp.longValue() == fstamp) {
						return false;
					}
				}
				//if there is no recorded stamp we have to assume it is new
				stamp = new Long(fstamp);
				fgInstallTimeMap.put(location, stamp);
				writeInstallInfo();
				fgHasChanged.add(location);
				return true;
			}
		}
		return false;
	}


	/**
	 * Sets the library info that corresponds to the specified JRE install
	 * path.
	 *
	 * @param javaInstallPath home location for a JRE
	 * @param info the library information, or <code>null</code> to remove
	 */
	public static void setLibraryInfo(String javaInstallPath, LibraryInfo info) {
		if (fgLibraryInfoMap == null) {
			restoreLibraryInfo();
		}
		if (info == null) {
			fgLibraryInfoMap.remove(javaInstallPath);
			if(fgInstallTimeMap != null) {
				fgInstallTimeMap.remove(javaInstallPath);
				writeInstallInfo();
			}

		} else {
			fgLibraryInfoMap.put(javaInstallPath, info);
		}
		//once the library info has been set we can forget it has changed
		fgHasChanged.remove(javaInstallPath);
		saveLibraryInfo();
	}

	/**
	 * Saves the library info in a local workspace state location
	 */
	private static void saveLibraryInfo() {
//		try {
//			String xml = getLibraryInfoAsXML();
//			IPath libPath = getDefault().getStateLocation();
//			libPath = libPath.append("libraryInfos.xml"); //$NON-NLS-1$
//			File file = libPath.toFile();
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//			try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
//				stream.write(xml.getBytes("UTF8")); //$NON-NLS-1$
//			}
//		} catch (IOException e) {
//			log(e);
//		}  catch (CoreException e) {
//			log(e);
//		}
	}

	/**
	 * Restores library information for VMs
	 */
	private static void restoreLibraryInfo() {
		fgLibraryInfoMap = new HashMap<>(10);
//		IPath libPath = getDefault().getStateLocation();
//		libPath = libPath.append("libraryInfos.xml"); //$NON-NLS-1$
//		File file = libPath.toFile();
//		if (file.exists()) {
//			try {
//				InputStream stream = new BufferedInputStream(new FileInputStream(file));
//				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//				parser.setErrorHandler(new DefaultHandler());
//				Element root = parser.parse(new InputSource(stream)).getDocumentElement();
//				if(!root.getNodeName().equals("libraryInfos")) { //$NON-NLS-1$
//					return;
//				}
//
//				NodeList list = root.getChildNodes();
//				int length = list.getLength();
//				for (int i = 0; i < length; ++i) {
//					Node node = list.item(i);
//					short type = node.getNodeType();
//					if (type == Node.ELEMENT_NODE) {
//						Element element = (Element) node;
//						String nodeName = element.getNodeName();
//						if (nodeName.equalsIgnoreCase("libraryInfo")) { //$NON-NLS-1$
//							String version = element.getAttribute("version"); //$NON-NLS-1$
//							String location = element.getAttribute("home"); //$NON-NLS-1$
//							String[] bootpath = getPathsFromXML(element, "bootpath"); //$NON-NLS-1$
//							String[] extDirs = getPathsFromXML(element, "extensionDirs"); //$NON-NLS-1$
//							String[] endDirs = getPathsFromXML(element, "endorsedDirs"); //$NON-NLS-1$
//							if (location != null) {
//								LibraryInfo info = new LibraryInfo(version, bootpath, extDirs, endDirs);
//								fgLibraryInfoMap.put(location, info);
//							}
//						}
//					}
//				}
//			} catch (IOException e) {
//				log(e);
//			} catch (ParserConfigurationException e) {
//				log(e);
//			} catch (SAXException e) {
//				log(e);
//			}
//		}
	}

	/**
	 * Reads the file of saved time stamps and populates the {@link #fgInstallTimeMap}.
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=266651 for more information
	 *
	 * @since 3.7
	 */
	private static void readInstallInfo() {
		fgInstallTimeMap = new HashMap<>();
//		IPath libPath = getDefault().getStateLocation();
//		libPath = libPath.append(".install.xml"); //$NON-NLS-1$
//		File file = libPath.toFile();
//		if (file.exists()) {
//			try {
//				InputStream stream = new BufferedInputStream(new FileInputStream(file));
//				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//				parser.setErrorHandler(new DefaultHandler());
//				Element root = parser.parse(new InputSource(stream)).getDocumentElement();
//				if(root.getNodeName().equalsIgnoreCase("dirs")) { //$NON-NLS-1$
//					NodeList nodes = root.getChildNodes();
//					Node node = null;
//					Element element = null;
//					for (int i = 0; i < nodes.getLength(); i++) {
//						node = nodes.item(i);
//						if(node.getNodeType() == Node.ELEMENT_NODE) {
//							element = (Element) node;
//							if(element.getNodeName().equalsIgnoreCase("entry")) { //$NON-NLS-1$
//								String loc = element.getAttribute("loc"); //$NON-NLS-1$
//								String stamp = element.getAttribute("stamp"); //$NON-NLS-1$
//								try {
//									Long l = new Long(stamp);
//									fgInstallTimeMap.put(loc, l);
//								}
//								catch(NumberFormatException nfe) {
//								//do nothing
//								}
//							}
//						}
//					}
//				}
//			} catch (IOException e) {
//				log(e);
//			} catch (ParserConfigurationException e) {
//				log(e);
//			} catch (SAXException e) {
//				log(e);
//			}
//		}
	}

	/**
	 * Writes out the mappings of SDK install time stamps to disk. See
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=266651 for more information.
	 *
	 * @since 3.7
	 */
	private static void writeInstallInfo() {
//		if(fgInstallTimeMap != null) {
//			try {
//				Document doc = DebugPlugin.newDocument();
//				Element root = doc.createElement("dirs");    //$NON-NLS-1$
//				doc.appendChild(root);
//				Entry<String, Long> entry = null;
//				Element e = null;
//				String key = null;
//				for(Iterator<Entry<String, Long>> i = fgInstallTimeMap.entrySet().iterator(); i.hasNext();) {
//					entry = i.next();
//					key = entry.getKey();
//					if(fgLibraryInfoMap == null || fgLibraryInfoMap.containsKey(key)) {
//						//only persist the info if the library map also has info OR is null - prevent persisting deleted JRE information
//						e = doc.createElement("entry"); //$NON-NLS-1$
//						root.appendChild(e);
//						e.setAttribute("loc", key); //$NON-NLS-1$
//						e.setAttribute("stamp", entry.getValue().toString()); //$NON-NLS-1$
//					}
//				}
//				String xml = DebugPlugin.serializeDocument(doc);
//				IPath libPath = getDefault().getStateLocation();
//				libPath = libPath.append(".install.xml"); //$NON-NLS-1$
//				File file = libPath.toFile();
//				if (!file.exists()) {
//					file.createNewFile();
//				}
//				try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
//					stream.write(xml.getBytes("UTF8")); //$NON-NLS-1$
//				}
//			} catch (IOException e) {
//				log(e);
//			}  catch (CoreException e) {
//				log(e);
//			}
//		}
	}

}
