/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.schema;


import java.io.File;
import java.io.IOException;

public class GenerateSchemaMain {

	private static final String TYPESCRIPT_CLIENT_DIR = "generate.typescript.client.output";
	
	public static void main(String[] args) throws Exception {
		String baseDir = args.length > 0 ? args[0] : ".";
		Class<?>[] daos = new DaoClasses().getAll();
		JSONUtility json = generateJson(baseDir, daos);
		TypescriptUtility ts = generateTypescript(baseDir, daos);
		generateSpecifications(baseDir, json, ts);
		generateTypescriptClient(ts);
	}

	private static void generateTypescriptClient(TypescriptUtility ts) {
		String clientDir = System.getProperty(TYPESCRIPT_CLIENT_DIR);
		if (exists(clientDir)) {
			ts.generateTypescriptClient(clientDir);
		}
	}

	private static boolean exists(String clientDir) {
		return clientDir != null
				&& !clientDir.isEmpty()
				&& new File(clientDir).exists();
	}

	private static void generateSpecifications(String baseDir, JSONUtility json, TypescriptUtility ts)
			throws IOException {
		SpecificationGenerator generator = new SpecificationGenerator(json, ts, baseDir);
		generator.generate();
	}

	private static TypescriptUtility generateTypescript(String baseDir, Class<?>[] daos) throws IOException {
		TypescriptUtility ts = new TypescriptUtility(baseDir);
		SchemaIOUtil.cleanFolder(ts.getDaoTypescriptFolder());
		ts.writeTypescriptSchemas(daos);
		return ts;
	}

	private static JSONUtility generateJson(String baseDir, Class<?>[] daos) throws IOException {
		JSONUtility json = new JSONUtility(baseDir);
		json.cleanFolder();
		json.writeJsonDAOSchemas(daos);
		return json;
	}

}
