package org.jboss.tools.rsp.server.model;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.server.model.internal.Server;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.junit.Test;

public class ServerLoadSaveDefectTest {
	@Test
	public void testLoadDeploymentOptionDefectIssue406() {
		String contents = getLoadString();
		try {
			File f = Files.createTempFile("ServerLoadSaveDefectTest_1", ""+System.currentTimeMillis()).toFile();
			Files.write(f.toPath(), contents.getBytes());
			
			Server s = new Server(f, createMockMgmtModel());
			s.load(new NullProgressMonitor());
		} catch(IOException ioe) {
			fail();
		} catch(CoreException ce) {
			fail();
		}
	}
	
	private IServerManagementModel createMockMgmtModel() {
		IServerManagementModel mgmtModel = mock(IServerManagementModel.class);
		return mgmtModel;
	}
	
	private String getLoadString() {
		return "{\n" + 
				"  \"server.home.dir\": \"/home/rob/apps/jboss/unzipped/wildfly-17.0.0.Final.zip.expanded/\",\n" + 
				"  \"id-set\": \"true\",\n" + 
				"  \"org.jboss.tools.rsp.server.typeId\": \"org.jboss.ide.eclipse.as.wildfly.170\",\n" + 
				"  \"id\": \"wf17xabcde\",\n" + 
				"  \"deployables\": {\n" + 
				"    \"deployable\": {\n" + 
				"      \"label\": \"/home/rob/tmp/sample/sample.war\",\n" + 
				"      \"path\": \"/home/rob/tmp/sample/sample.war\",\n" + 
				"      \"options\": {\n" + 
				"        \"option\": {\n" + 
				"          \"deployment.output.name\": \"test5.war\"\n" + 
				"        }\n" + 
				"      }\n" + 
				"    }\n" + 
				"  }\n" + 
				"}";
	}
}
