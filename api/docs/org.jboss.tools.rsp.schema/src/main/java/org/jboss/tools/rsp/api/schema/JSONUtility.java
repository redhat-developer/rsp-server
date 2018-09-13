/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.schema;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.fasterxml.jackson.module.jsonSchema.factories.WrapperFactory;

public class JSONUtility {
	
	public static void cleanFolder() {
		Path folder = getDaoJsonFolder();
		File[] jsons = folder.toFile().listFiles();
		for( int i = 0; i < jsons.length; i++ ) {
			jsons[i].delete();
		}
	}
	
	public static void writeJsonDAOSchemas(Class[] daoClasses) throws Exception {
		File daoFolder = getDaoJsonFolder().toFile();
		daoFolder.mkdirs();
		System.out.println("Writing schemas to " + daoFolder.getAbsolutePath());
		for( int i = 0; i < daoClasses.length; i++ ) {
			Path out = getDaoJsonFile(daoClasses[i].getSimpleName());
			String content = printJSONSchema(daoClasses[i]);
			Files.write(out, content.getBytes());
		}
		System.out.println("Done.");
	}

    public static final String printJSONSchema(Class c) {
    	try {
            ObjectMapper mapper = new ObjectMapper();
            IgnoreURNSchemaFactoryWrapper visitor = new IgnoreURNSchemaFactoryWrapper();
            mapper.acceptJsonFormatVisitor(c, visitor);
            JsonSchema schema = visitor.finalSchema();
            schema.setId(null);
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            String asString = writer.writeValueAsString(schema);
            return asString;
    	} catch(Exception e) {
    		e.printStackTrace();
    		return null;
    	}
    }
    

    public static class IgnoreURNSchemaFactoryWrapper extends SchemaFactoryWrapper {
        public IgnoreURNSchemaFactoryWrapper() {
            this(null, new WrapperFactory());
        }
    
        public IgnoreURNSchemaFactoryWrapper(SerializerProvider p) {
            this(p, new WrapperFactory());
        }
    
        protected IgnoreURNSchemaFactoryWrapper(WrapperFactory wrapperFactory) {
            this(null, wrapperFactory);
        }
    
    	public IgnoreURNSchemaFactoryWrapper(SerializerProvider p, WrapperFactory wrapperFactory) {
    		super(p, wrapperFactory);
           	visitorContext = new VisitorContext() {
           		public String javaTypeToUrn(JavaType jt) {
           			return null;
           		}
           	};
    	}
    }
    	

	public static Path getDaoJsonFolder() {
		return new File(".").toPath().resolve("src").resolve("main")
				.resolve("resources").resolve("schema").resolve("json");
	}

	public static Path getDaoJsonFile(String simpleClassName) {
		Path folder = getDaoJsonFolder();
		Path out = folder.resolve(simpleClassName + ".json");
		return out;
	}
	
	
}
