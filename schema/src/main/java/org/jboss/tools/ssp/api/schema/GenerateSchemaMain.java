/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.api.schema;


import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jboss.tools.ssp.api.beans.CreateServerAttribute;
import org.jboss.tools.ssp.api.beans.CreateServerAttributes;
import org.jboss.tools.ssp.api.beans.DiscoveryPath;
import org.jboss.tools.ssp.api.beans.ServerAttributes;
import org.jboss.tools.ssp.api.beans.ServerBean;
import org.jboss.tools.ssp.api.beans.ServerHandle;
import org.jboss.tools.ssp.api.beans.ServerProcess;
import org.jboss.tools.ssp.api.beans.ServerProcessOutput;
import org.jboss.tools.ssp.api.beans.ServerStateChange;
import org.jboss.tools.ssp.api.beans.ServerType;
import org.jboss.tools.ssp.api.beans.StartServerAttributes;
import org.jboss.tools.ssp.api.beans.Status;
import org.jboss.tools.ssp.api.beans.StopServerAttributes;
import org.jboss.tools.ssp.api.beans.VMDescription;
import org.jboss.tools.ssp.api.beans.VMHandle;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.fasterxml.jackson.module.jsonSchema.factories.WrapperFactory;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.JsonLibrary;
import cz.habarta.typescript.generator.Output;
import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TypeScriptGenerator;
import cz.habarta.typescript.generator.TypeScriptOutputKind;

public class GenerateSchemaMain {
	public static void main(String[] args) throws Exception {
		Class[] daos = getDAOClasses();
		JSONUtility json = new JSONUtility();
		json.writeJsonDAOSchemas(daos);
		
		TypescriptUtility ts = new TypescriptUtility();
		ts.writeTypescriptSchemas(daos);

		SpecificationGenerator generator = new SpecificationGenerator(json, ts);
		generator.generate();
		// Write MD docs
	}
	
	private static Class[] getDAOClasses() {

		Class[] daoClasses = new Class[] {
				Status.class,
				VMDescription.class, VMHandle.class,
				DiscoveryPath.class, ServerBean.class, 

				ServerType.class,ServerHandle.class,
				ServerAttributes.class,
				CreateServerAttributes.class, CreateServerAttribute.class,
				ServerProcess.class, ServerProcessOutput.class,
				ServerStateChange.class, 
				StartServerAttributes.class, StopServerAttributes.class
		};
		return daoClasses;
	}

	

}
