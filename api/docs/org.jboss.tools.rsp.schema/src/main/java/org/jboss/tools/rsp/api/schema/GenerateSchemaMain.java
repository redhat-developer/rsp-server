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
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;

public class GenerateSchemaMain {

	private static final String TYPESCRIPT_CLIENT_DIR = "generate.typescript.client.output";
	
	public static void main(String[] args) throws Exception {
		String baseDir = args.length > 0 ? args[0] : ".";
		Class<?>[] daos = new DaoClasses().getAll();
		
		validateDaos(daos);
		
		JSONUtility json = generateJson(baseDir, daos);
		TypescriptUtility ts = generateTypescript(baseDir, daos);
		generateSpecifications(baseDir, json, ts);
		generateTypescriptClient(baseDir, ts);
	}

	private static void validateDaos(Class<?>[] daos) {
		ArrayList<Class<?>> invalid = new ArrayList<>();
		for( int i = 0; i < daos.length; i++ ) {
			Constructor<?>[] constructors = daos[i].getConstructors();
			boolean zeroArgFound = false;
			for( int j = 0; j < constructors.length; j++ ) {
				if( constructors[j].getParameterCount() == 0 )
					zeroArgFound = true;
			}
			if( !zeroArgFound) {
				invalid.add(daos[i]);
			}
		}
		if(!invalid.isEmpty() ) {
			System.out.println("Invalid DAOs found: Missing 0-arg constructor");
			for( Class<?> c : invalid ) {
				System.out.println(c.getName());
			}
			System.exit(1);
		}
	}

	private static void generateTypescriptClient(String baseDir, TypescriptUtility ts) {
		String localClientLocation = getJsonClientFolder(baseDir).toFile().getAbsolutePath();
		if (exists(localClientLocation)) {
			ts.generateTypescriptClient(localClientLocation);
		}
		
		
		String clientDir = System.getProperty(TYPESCRIPT_CLIENT_DIR);
		if (exists(clientDir)) {
			ts.generateTypescriptClient(clientDir);
		}
	}
	

    private static Path getJsonClientFolder(String baseDir) {
		return new File(baseDir).toPath()
				.resolve("src").resolve("main").resolve("resources")
				.resolve("client").resolve("typescript");
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
