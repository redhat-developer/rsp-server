/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.generic.GenericServerExtensionModel;
import org.jboss.tools.rsp.server.model.ServerManagementModel;
import org.jboss.tools.rsp.server.persistence.DataLocationCore;
import org.jboss.tools.rsp.server.spi.model.IDataStoreModel;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.junit.Test;

public class ExtensionModelDownloadTest {
	
	public String getDownloadRuntimeJson(String stypeId, String dlProviderId, String inner) {
		return "{\n" + 
				"	\"serverTypes\": {\n" + 
				"		\"" + stypeId + "\": {\n" + 
				"			\"downloads\": {\n" + 
				"				\"downloadProviderId\": \"" + dlProviderId + "\",\n" + inner + 
				"			}\n" + 
				"		}\n" + 
				"	}\n" + 
				"}";
	}
	
	public String getSingleDlrtString(String dlrtId, String name, String version, String dlUrl, 
			String licenseUrl, String install, String size, boolean comma) {
		return  "				\"" + dlrtId + "\": {\n" + 
				"					\"name\": \"" + name + "\",\n" + 
				"					\"fullVersion\": \"" + version + "\",\n" + 
				"					\"downloadUrl\": \"" + dlUrl + "\",\n" + 
				"					\"licenseUrl\": \"" + licenseUrl + "\",\n" + 
				"					\"installationMethod\": \"" + install + "\",\n" + 
				"					\"size\": \"" + size + "\"\n" + 
				"				}" + (comma ? "," : "") + "\n";
	}
	

	protected IServerManagementModel createAndRegister(String serverJson) throws IOException {
		File tmpFolder = Files.createTempDirectory(getClass().getName() + System.currentTimeMillis()).toFile();
		IDataStoreModel dsm = new DataLocationCore(tmpFolder, "blank");
		IServerManagementModel rspModel = new ServerManagementModel(dsm);
		GenericServerExtensionModel extModel = new GenericServerExtensionModel(rspModel, null, new ByteArrayInputStream(serverJson.getBytes()));
		
		Map<String, DownloadRuntime> dlrts = rspModel.getDownloadRuntimeModel().getOrLoadDownloadRuntimes(new NullProgressMonitor());
		assertNotNull(dlrts);
		assertEquals(dlrts.size(), 0);
		extModel.registerExtensions();
		
		dlrts = rspModel.getDownloadRuntimeModel().getOrLoadDownloadRuntimes(new NullProgressMonitor());
		assertNotNull(dlrts);
		return rspModel;
	}
	
	@Test
	public void testSingleDlrt() throws IOException {
		String dlrt = getSingleDlrtString("wonka5.dlrt", "Wonka 5.0.20", "5.0.20", 
				"http://www.wonka.com", 
				"http://www.apache.org/licenses/LICENSE-2.0.txt", 
				"archive", "1000000", false);
		String all = getDownloadRuntimeJson("wonka.server.type.5x", "wonka.5x.dlrt", dlrt);
		IServerManagementModel rspModel = createAndRegister(all);
		IDownloadRuntimesProvider[] providers = rspModel.getDownloadRuntimeModel().getDownloadRuntimeProviders();
		assertNotNull(providers);
		assertEquals(1, providers.length);
		assertEquals(1, rspModel.getDownloadRuntimeModel().getOrLoadDownloadRuntimes(new NullProgressMonitor()).size());
	}

	@Test
	public void testMultipleDlrt() throws IOException {
		String dlrt1 = getSingleDlrtString("wonka501.dlrt", "Wonka 5.0.1", "5.0.1", 
				"http://www.wonka.com", 
				"http://www.apache.org/licenses/LICENSE-2.0.txt", 
				"archive", "1000000", false);

		
		String dlrt2 = getSingleDlrtString("wonka502.dlrt", "Wonka 5.0.2", "5.0.2", 
				"http://www.wonka.com", 
				"http://www.apache.org/licenses/LICENSE-2.0.txt", 
				"archive", "1000000", false);

		String dlrt3 = getSingleDlrtString("wonka520.dlrt", "Wonka 5.2.0", "5.2.0", 
				"http://www.wonka.com", 
				"http://www.apache.org/licenses/LICENSE-2.0.txt", 
				"archive", "1000000", false);
		
		String inner = dlrt1 + ",\n" + dlrt2 + ",\n" + dlrt3 + "\n";
		String all = getDownloadRuntimeJson("wonka.server.type.5x", "wonka.5x.dlrt", inner);
		IServerManagementModel rspModel = createAndRegister(all);
		IDownloadRuntimesProvider[] providers = rspModel.getDownloadRuntimeModel().getDownloadRuntimeProviders();
		assertNotNull(providers);
		assertEquals(1, providers.length);
		Map<String, DownloadRuntime> dlrtMap = rspModel.getDownloadRuntimeModel().getOrLoadDownloadRuntimes(new NullProgressMonitor());
		assertEquals(3, dlrtMap.size());
		DownloadRuntime dlrt1a = dlrtMap.get("wonka501.dlrt");
		DownloadRuntime dlrt2a = dlrtMap.get("wonka502.dlrt");
		DownloadRuntime dlrt3a = dlrtMap.get("wonka520.dlrt");
		assertNotNull(dlrt1a);
		assertNotNull(dlrt2a);
		assertNotNull(dlrt3a);
		
		assertEquals(dlrt1a.getLicenseURL(), "http://www.apache.org/licenses/LICENSE-2.0.txt");
		assertEquals(dlrt2a.getLicenseURL(), "http://www.apache.org/licenses/LICENSE-2.0.txt");
		assertEquals(dlrt3a.getLicenseURL(), "http://www.apache.org/licenses/LICENSE-2.0.txt");

		assertEquals(dlrt1a.getUrl(), "http://www.wonka.com");
		assertEquals(dlrt2a.getUrl(), "http://www.wonka.com");
		assertEquals(dlrt3a.getUrl(), "http://www.wonka.com");

		assertEquals(dlrt1a.getSize(), "1000000");
		assertEquals(dlrt2a.getSize(), "1000000");
		assertEquals(dlrt3a.getSize(), "1000000");
		
		
	}

}
