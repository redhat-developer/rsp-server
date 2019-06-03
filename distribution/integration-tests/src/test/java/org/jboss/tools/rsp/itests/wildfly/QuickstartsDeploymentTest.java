/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.wildfly;

import static org.jboss.tools.rsp.itests.util.ServerStateUtil.waitForDeployablePublishState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.ServerDeployableReference;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.itests.RSPCase;
import org.jboss.tools.rsp.itests.util.DummyClient;
import org.jboss.tools.rsp.itests.util.HttpUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * Parametrized test class for verification that particular applications in form
 * of war/ear archives or exploded archives can be deployed on the server.
 * 
 * @author odockal
 *
 */
@RunWith(Parameterized.class)
public class QuickstartsDeploymentTest extends RSPCase {

	private final DummyClient client = launcher.getClient();

	private static Logger log = Logger.getLogger(QuickstartsDeploymentTest.class.getName());

	private static final List<String> projects = Arrays.asList("kitchensink", "kitchensink-ear", "ejb-in-war",
			"ejb-in-ear");

	private static final List<String> deploymentTypes = Arrays.asList("war", "exploded");

	private static final Path QUICKSTARTS_ROOT = Paths.get(getProperty("user.dir"), "target", "quickstarts");

	private static final Map<String, String> deployments = deploymentsmapping();

	private static final String QUICKSTARTS_SERVER = getProperty("quickstarts.server");

	private static String SERVER_ID = "wildfly";

	private URL url;

	private String project;
	private String deploymentType;
	private Path projectPath;
	private ServerHandle handle;
	private DeployableReference reference;

	@Parameters(name = "{0} {1} {2}")
	public static Collection<String[]> data() {
		return projects.stream().flatMap(project -> deploymentTypes.stream().map(type -> {
			return new String[] { QUICKSTARTS_SERVER, project, type };
		})).collect(Collectors.toList());
	}

	@BeforeClass
	public static void configureSkippingTests() {
		// Skip test if requirements are not fulfilled
		assumeTrue("Could not find quickstarts project on the path: " + QUICKSTARTS_ROOT.toString(),
				Files.exists(QUICKSTARTS_ROOT, LinkOption.NOFOLLOW_LINKS));
	}

	private static Map<String, String> deploymentsmapping() {
		Map<String, String> map = new HashMap<>();
		map.put("kitchensink-ear", "kitchensink-ear-web");
		return map;
	}

	public QuickstartsDeploymentTest(String server, String project, String type) {
		this.project = project;
		this.deploymentType = type;
		setProjectPath();
	}

	@Before
	public void before() throws Exception {
		createServer(WILDFLY_ROOT, SERVER_ID);
		startServer(client, SERVER_ID);
		handle = new ServerHandle(SERVER_ID, wildflyType);
		reference = new DeployableReference(projectPath.toFile().getName(), projectPath.toFile().getAbsolutePath());
		url = new URL("http://localhost:8080" + "/" + deployments.getOrDefault(this.project, this.project));
	}

	@After
	public void after() throws Exception {
		stopServer(client, SERVER_ID);
		deleteServer(SERVER_ID);
	}

	@Test
	public void testAppDeploymentToServer() throws Exception {
		// Verify project path
		assertTrue("File " + projectPath + " does not exist", Files.exists(projectPath, LinkOption.NOFOLLOW_LINKS));
		// No deployable avaiable at the moment
		assertTrue(serverProxy.getDeployables(handle).get().isEmpty());
		Status status = serverProxy.addDeployable(new ServerDeployableReference(handle, reference)).get();
		assertEquals("Expected request status is 'ok' but was " + status, Status.OK, status.getSeverity());
		waitForDeployablePublishState(ServerManagementAPIConstants.PUBLISH_STATE_ADD, 10, client);
		verifyURL(url.toString(), 404);

		// Deployable available, but not deployed yet until publish full request send to
		// the server
		DeployableState state = getDeployableStateByReference(handle, reference);
		assertNotNull("Deployable reference was not found", state);
		assertDeployableReference(state);
		verifyURL(url.toString(), 404);

		// send publish request with publish full kind
		sendPublishRequest(handle, ServerManagementAPIConstants.PUBLISH_FULL);

		// check for the publishing is done, then it requires no publish change
		waitForDeployablePublishState(ServerManagementAPIConstants.PUBLISH_STATE_NONE, 10, client);
		state = getDeployableStateByReference(handle, reference);
		assertNotNull("Deployable reference was not found", state);
		assertDeployableReference(state);
		// even though publishing is done, app is not yet deployed, thus waiting takes
		// place
		HttpUtility.waitForUrlEndpoint(url, 200, 30);
		verifyURL(url.toString(), 200);

		// Remove deployment
		Status statusRemove = serverProxy.removeDeployable(new ServerDeployableReference(handle, reference)).get();
		assertEquals("Publish request status OK check failed, was: " + statusRemove.getMessage(), Status.OK,
				statusRemove.getSeverity());
		state = getDeployableStateByReference(handle, reference);
		// Deployable state of the depl. is still present
		assertNotNull("Deployable reference was not found", state);
		assertDeployableReference(state);
		verifyURL(url.toString(), 200);

		// Another publish full request must be sent
		sendPublishRequest(handle, ServerManagementAPIConstants.PUBLISH_FULL);

		// Deployment is no more
		assertTrue(serverProxy.getDeployables(handle).get().isEmpty());
		state = getDeployableStateByReference(handle, reference);
		assertNull("Deployable reference was still found", state);
		HttpUtility.waitForUrlEndpoint(url, 404, 30);
		verifyURL(url.toString(), 404);
	}

	private DeployableState getDeployableStateByReference(ServerHandle handle, DeployableReference reference) {
		List<DeployableState> deployables = new ArrayList<>();
		try {
			deployables = serverProxy.getDeployables(handle).get();
		} catch (InterruptedException | ExecutionException e) {
			log.log(Level.SEVERE, "Failed to get deployables", e);
			e.printStackTrace();
		}
		return deployables.stream().filter(x -> x.getReference().equals(reference)).findFirst().orElse(null);
	}

	private void assertDeployableReference(DeployableState state) {
		assertEquals(projectPath.toFile().getName(), state.getReference().getLabel());
		assertEquals(projectPath.toFile().getAbsolutePath(), state.getReference().getPath());
	}

	private void verifyURL(String uri, int expectedStatusCode) throws IOException {
		assertEquals("Http response status code for " + uri + " does not match", expectedStatusCode,
				HttpUtility.getHttpStatusCode(uri));
	}

	private void setProjectPath() {
		String projectRelativePath = this.project;
		if (this.project.contains("ear")) {
			projectRelativePath += "/ear/target/" + this.project + ".ear";
		} else {
			projectRelativePath += "/target/" + this.project + ".war";
		}
		this.projectPath = Paths.get(QUICKSTARTS_ROOT.toString(), projectRelativePath);

		if (deploymentType.equals("exploded")) {
			try {
				this.projectPath = extractWARExploded(projectPath);
			} catch (ZipException e) {
				e.printStackTrace();
				log.log(Level.SEVERE, "Failed to extract archive " + projectPath, e);
			}
		}
	}

	private Path extractWARExploded(Path warFile) throws ZipException {
		ZipFile file = new ZipFile(warFile.toFile());
		Path exploded = Paths.get(warFile.getParent().toFile().toString() + "/exploded/" + warFile.toFile().getName());
		log.log(Level.INFO, "Extracting " + warFile);
		file.extractAll(exploded.toString());
		log.log(Level.INFO, "Extracted to " + exploded);
		return exploded;
	}
}
