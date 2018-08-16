/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.internal.launching.java.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.Launch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.debug.core.model.IStreamsProxy;
import org.jboss.tools.rsp.eclipse.jdt.internal.launching.LibraryInfo;
import org.jboss.tools.rsp.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallChangedListener;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMRunner;
import org.jboss.tools.rsp.eclipse.jdt.launching.VMRunnerConfiguration;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.launching.LaunchingCore;
import org.jboss.tools.rsp.launching.utils.ExecUtil;
import org.jboss.tools.rsp.launching.utils.NativeEnvironmentUtils;
import org.jboss.tools.rsp.launching.utils.OSUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LaunchingSupportUtils {
	private static final String BAR = "|"; //$NON-NLS-1$
	protected static final String JAVA_JVM_VERSION = "JAVA_JVM_VERSION"; //$NON-NLS-1$
	private static final String AbstractVMInstall_4="Exception retrieving system properties: {0}";
	private static final String LaunchingPlugin_34="Unable to create XML parser.";
	private static final String AbstractVMInstall_0="Unable to retrieve system properties: {0}";
	private static final String AbstractVMInstall_1="Evaluating system properties";
	private static final String AbstractVMInstall_3="Reading system properties";
	
	/**
	 * Shared XML parser
	 */
	private static DocumentBuilder fgXMLParser = null;
	
	
	/**
	 * The minimal -Xmx size for launching a JVM. <br>
	 * <b>Note:</b> Must be omitted for Standard11xVM! <br>
	 * <b>Note:</b> Must be at least -Xmx16m for JRockit, see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=433455">bug 433455</a>.
	 *
	 * @since 3.7.100
	 */
	private static final String MIN_VM_SIZE = "-Xmx16m"; //$NON-NLS-1$
	
	private static DocumentBuilder getParser() throws CoreException {
		if (fgXMLParser == null) {
			try {
				fgXMLParser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				fgXMLParser.setErrorHandler(new DefaultHandler());
			} catch (ParserConfigurationException e) {
				abort(LaunchingPlugin_34, e);
			} catch (FactoryConfigurationError e) {
				abort(LaunchingPlugin_34, e);
			}
		}
		return fgXMLParser;
	}
	
	
	
	public File getLaunchingSupportFile() {
		File data = LaunchingCore.getDataLocation();
		if( data == null ) {
			log("Data location is null!!");
		} else {
			File libs = new File(data, "libs");
			if( !libs.exists()) {
				libs.mkdirs();
			}
			File launchingSupport = new File(libs, "launchingsupport.jar");
			if( !launchingSupport.exists()) {
				ClassLoader classLoader = getClass().getClassLoader();
				InputStream is = classLoader.getResourceAsStream("launchingsupport.jar");
				try {
					Files.copy(is, launchingSupport.toPath());
				} catch(IOException ioe) {
					log(ioe);
				}
				
			}
			return launchingSupport;
		}
		return null;
	}

	private IProcess runLaunchingSupportSysprops(
			File launchingSupportFile, 
			IVMRunner runner, String[] properties, 
			IProgressMonitor monitor) throws CoreException {
		VMRunnerConfiguration config = new VMRunnerConfiguration("org.eclipse.jdt.internal.launching.support.LegacySystemProperties", 
				new String[] { launchingSupportFile.getAbsolutePath() });//$NON-NLS-1$
		if (runner == null) {
			abort(NLS.bind(AbstractVMInstall_0, ""), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		}
		config.setProgramArguments(properties);
		Launch launch = new Launch(null, "run", null);
		if (monitor.isCanceled()) {
			return null;
		}
		monitor.beginTask(AbstractVMInstall_1, 2);
		runner.run(config, launch, monitor);
		IProcess[] processes = launch.getProcesses();
		if (processes.length != 1) {
			abort(NLS.bind(AbstractVMInstall_0, runner), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		IProcess process = processes[0];
		return process;
	}
	
	public LibraryInfo runLaunchingSupportLibraryDetector(File javaExecutable, File launchingSupportFile) {
		LibraryInfo info = null;
		String javaExecutablePath = javaExecutable.getAbsolutePath();
		String[] cmdLine = new String[] { javaExecutablePath, MIN_VM_SIZE,
				"-classpath", launchingSupportFile.getAbsolutePath(), "org.eclipse.jdt.internal.launching.support.LibraryDetector" }; //$NON-NLS-1$ //$NON-NLS-2$
		Process p = null;
		try {
			String envp[] = getLaunchingSupportEnvironment();
			p = ExecUtil.exec(cmdLine, null, envp);
			IProcess process = ExecUtil.newProcess(new Launch(null, "run", null), p, "Library Detection"); //$NON-NLS-1$
			waitForTermination(process, 600);
			info = parseLibraryInfo(process);
		} catch (CoreException ioe) {
			log(ioe);
		} finally {
			if (p != null) {
				p.destroy();
			}
		}
		return info;
	}
	
	private static void waitForTermination(IProcess process, int maxWait) throws DebugException {
		try {
			for (int i= 0; i < maxWait; i++) {
				if (process.isTerminated()) {
					break;
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				} 
			}
		} finally {
			if( !process.isTerminated()) {
				process.terminate();
			}
		}
	}
	

	private String[] getLaunchingSupportEnvironment() {
		if (OSUtils.isMac()) {
			Map<String, String> map = NativeEnvironmentUtils.getDefault().getNativeEnvironmentCasePreserved(); 
			if (map.remove(JAVA_JVM_VERSION) != null) {
				String[] envp = new String[map.size()];
				Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
				int i = 0;
				while (iterator.hasNext()) {
					Entry<String, String> entry = iterator.next();
					envp[i] = entry.getKey() + "=" + entry.getValue(); //$NON-NLS-1$
					i++;
				}
				return envp;
			}
		}
		return null;
	}

	/**
	 * Parses the output from 'LibraryDetector'.
	 *
	 * @param process the backing {@link IProcess} that was run
	 * @return the new {@link LibraryInfo} object or <code>null</code>
	 */
	protected LibraryInfo parseLibraryInfo(IProcess process) {
		String text = getTextFromProcess(process);
		if (text != null && text.length() > 0) {
			int index = text.indexOf(BAR);
			if (index > 0) {
				String version = text.substring(0, index);
				text = text.substring(index + 1);
				index = text.indexOf(BAR);
				if (index > 0) {
					String bootPaths = text.substring(0, index);
					String[] bootPath = parsePaths(bootPaths);

					text = text.substring(index + 1);
					index = text.indexOf(BAR);

					if (index > 0) {
						String extDirPaths = text.substring(0, index);
						String endorsedDirsPath = text.substring(index + 1);
						String[] extDirs = parsePaths(extDirPaths);
						String[] endDirs = parsePaths(endorsedDirsPath);
						return new LibraryInfo(version, bootPath, extDirs, endDirs);
					}
				}
			}
		}
		return null;
	}
	
	public Map<String, String> runAndParseLaunchingSupportSysprops(
			IVMRunner runner, String[] properties, 
			IProgressMonitor monitor) throws CoreException {
		HashMap<String, String> map = new HashMap<String, String>();
		// launch VM to evaluate properties
		File file = getLaunchingSupportFile();
		if (file != null && file.exists()) {
			IProcess process = runLaunchingSupportSysprops(file, runner, properties, monitor);
			if( process == null || monitor.isCanceled()) {
				return map;
			}
			waitForTermination(process, 40);
			monitor.worked(1);
			if (monitor.isCanceled()) {
				return map;
			}

			monitor.subTask(AbstractVMInstall_3);
			String text = getTextFromProcess(process);
			if (text != null && text.length() > 0) {
				map = parseSysprops(process, text);
			} else {
				String commandLine = process.getAttribute(IProcess.ATTR_CMDLINE);
				abort(NLS.bind(AbstractVMInstall_0, commandLine), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			}
			monitor.worked(1);
		} else {
			abort(NLS.bind(AbstractVMInstall_0, file), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		return map;
	}
	
	
	private HashMap<String, String> parseSysprops(IProcess process, String text) throws CoreException {
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			DocumentBuilder parser = getParser();
			Document document = parser.parse(new ByteArrayInputStream(text.getBytes()));
			Element envs = document.getDocumentElement();
			NodeList list = envs.getChildNodes();
			int length = list.getLength();
			for (int i = 0; i < length; ++i) {
				Node node = list.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					if (element.getNodeName().equals("property")) { //$NON-NLS-1$
						String name = element.getAttribute("name"); //$NON-NLS-1$
						String value = element.getAttribute("value"); //$NON-NLS-1$
						map.put(name, value);
					}
				}
			}
		} catch (SAXException e) {
			String commandLine = process.getAttribute(IProcess.ATTR_CMDLINE);
			abort(NLS.bind(AbstractVMInstall_4, commandLine), e, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (IOException e) {
			String commandLine = process.getAttribute(IProcess.ATTR_CMDLINE);
			abort(NLS.bind(AbstractVMInstall_4, commandLine), e, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		return map;
	}

	private String getTextFromProcess(IProcess process) {
		IStreamsProxy streamsProxy = process.getStreamsProxy();
		String text = null;
		if (streamsProxy != null) {
			text = streamsProxy.getOutputStreamMonitor().getContents();
		}
		return text;
	}
	
	protected String[] parsePaths(String paths) {
		List<String> list = new ArrayList<>();
		int pos = 0;
		int index = paths.indexOf(File.pathSeparatorChar, pos);
		while (index > 0) {
			String path = paths.substring(pos, index);
			list.add(path);
			pos = index + 1;
			index = paths.indexOf(File.pathSeparatorChar, pos);
		}
		String path = paths.substring(pos);
		if (!path.equals("null")) { //$NON-NLS-1$
			list.add(path);
		}
		return list.toArray(new String[list.size()]);
	}
	
	private static void log(Throwable t) {
		LaunchingCore.log(t);
	}
	private void log(String bind) {
		LaunchingCore.log(bind);
	}

	protected static void abort(String message, Throwable exception) throws CoreException {
		abort(message, exception, 0);
	}
	protected static void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, IVMInstallChangedListener.LAUNCHING_ID_PLUGIN,
				code, message, exception));
	}
}
