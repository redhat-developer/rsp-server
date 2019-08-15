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
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.fasterxml.jackson.module.jsonSchema.factories.WrapperFactory;

public class JSONUtility {
	
	private String baseDir;

	public JSONUtility(String baseDir) {
		this.baseDir = baseDir;
	}

	public boolean cleanFolder() {
		boolean ret = true;
		Path folder = getDaoJsonFolder();
		File[] jsons = folder.toFile().listFiles();
		for( int i = 0; i < jsons.length; i++ ) {
			if( !jsons[i].delete() ) {
				ret = false;
			}
		}
		return ret;
	}

	public void writeJsonDAOSchemas(Class<?>[] daoClasses) throws IOException {
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

    public static final String printJSONSchema(Class<?> c) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        IgnoreURNSchemaFactoryWrapper visitor = new IgnoreURNSchemaFactoryWrapper();
        mapper.acceptJsonFormatVisitor(c, visitor);
        JsonSchema schema = visitor.finalSchema();
        schema.setId(null);
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        String asString = writer.writeValueAsString(schema);
        return asString;
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
           	this.visitorContext = new VisitorContext() {
           		
           		@Override
           		public String javaTypeToUrn(JavaType jt) {
           			return null;
           		}
           	};
    	}
    }

    public Path getDaoJsonFolder() {
		return new File(baseDir).toPath()
				.resolve("src").resolve("main").resolve("resources").resolve("schema").resolve("json");
	}

	public Path getDaoJsonFile(String simpleClassName) {
		Path folder = getDaoJsonFolder();
		Path out = folder.resolve(simpleClassName + ".json");
		return out;
	}
}
