/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.jboss.tools.rsp.server.persistence.DataLocationCore;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 *
 * @author jrichter
 */
public class RSPServerHandler {

    private static final int WAIT_SERVER_STARTED = 3000;
    private static final String DISTRIBUTION_FILENAME = System.getProperty("rsp-distribution.filename") + ".zip";
    private static final String DISTRIBUTION_PATH = "../distribution/target/";
    
    
    private static final String SERVER_ROOT_SYSPROP = "rsp.distro.root";
    private static final String SERVER_ROOT_SYSPROP_RESOLVED = System.getProperty(SERVER_ROOT_SYSPROP);
    private static final String SERVER_ROOT = 
    		SERVER_ROOT_SYSPROP_RESOLVED != null ? SERVER_ROOT_SYSPROP_RESOLVED : 
    		(DISTRIBUTION_PATH + "/rsp-distribution");
    
    private static final File SERVER_DATA = new DataLocationCore().getDataLocation();
    private static final String DATA_BACKUP = SERVER_DATA + ".backup";

    private static Process serverProcess;
	private static InputStream serverErr;
	private static InputStream serverOut;

    public static void prepareServer() throws ZipException {
    	if( SERVER_ROOT_SYSPROP_RESOLVED == null || !(new File(SERVER_ROOT_SYSPROP_RESOLVED).exists())) {
	    	ZipFile zipFile = new ZipFile(new File(DISTRIBUTION_PATH, DISTRIBUTION_FILENAME));
	        zipFile.extractAll(DISTRIBUTION_PATH);
    	}
	}

	public static void startServer() throws Exception {
		// Debug on port 8001
		ProcessBuilder builder = new ProcessBuilder("java",
		// debug flags
//	        		"-Xdebug", 
//	        		"-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8001",
//	        		"-Xnoagent",
				"-Drsp.log.level=1", "-jar", "bin/felix.jar");

		// No debugging
		// ProcessBuilder builder = new ProcessBuilder("java", "-jar", "bin/felix.jar");

		builder.directory(new File(SERVER_ROOT));
		serverProcess = builder.start();
		serverOut = serverProcess.getInputStream();
		serverErr = serverProcess.getErrorStream();
		readForever(serverOut);
		readForever(serverErr);
		Thread.sleep(WAIT_SERVER_STARTED);
	}

	private static void readForever(InputStream is) {
    	new Thread("Read Server streams") {
    		public void run() {
    			BufferedReader br = null;
    			 try {
    		            br = new BufferedReader(new InputStreamReader(is));
    		            String line = null;
    		            while ((line = br.readLine()) != null) {
    		            }
    		        } catch (IOException ioe) {
    		        } finally {
    		            // close the streams using close method
    		            try {
    		                if (br != null) {
    		                    br.close();
    		                }
    		            } catch (IOException ioe) {
    		            }
    		        }
    		         
    		    }
    	}.start();
	}

    public static void stopServer() {

    	try {
    		if( serverOut != null )
    			serverOut.close();
    	} catch(IOException ioe) {
    	}
    	try {
    		if( serverErr != null )
    			serverErr.close();
    	} catch(IOException ioe) {
    	}
    	if (serverProcess != null) {
    		serverProcess.destroy();
    	}
    }

    public static void clearServerData(boolean backup) throws IOException {
        File dataFolder = SERVER_DATA.getAbsoluteFile();
        if (dataFolder.exists()) {
            if (backup) {
                dataFolder.renameTo(new File(DATA_BACKUP));
            } else {
                Files.walk(dataFolder.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
                dataFolder.delete();
            }
        }
    }

    public static void restoreBackupData(boolean force) throws IOException {        
        File backupFolder = new File(DATA_BACKUP);
        if (backupFolder.exists()) {
            if (force && SERVER_DATA.exists()) {
                clearServerData(false);
            }
            backupFolder.renameTo(SERVER_DATA);
        }
    }
}
