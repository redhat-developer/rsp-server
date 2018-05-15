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

		writeJsonDAOSchemas();
		
		writeTypescriptSchemas();
		
		// Write MD docs
//		final StringBuffer sb = new StringBuffer();
//		printFileDocumentation(getClientInterfaceFile(), sb);
//		printFileDocumentation(getServerInterfaceFile(), sb);
//		System.out.println(sb.toString());
	}
	
	private static File getClientInterfaceFile() throws IOException {
		File f2 = new File(".");
		File f = new File(f2, "../api/src/main/java/org/jboss/tools/ssp/api/ServerManagementClient.java").getCanonicalFile();
		return f;
	}

	private static File getServerInterfaceFile() throws IOException {
		File f2 = new File(".");
		File f = new File(f2, "../api/src/main/java/org/jboss/tools/ssp/api/ServerManagementServer.java").getCanonicalFile();
		return f;
	}

	private static void printFileDocumentation(File f, StringBuffer sb) {
		VoidVisitorAdapter adapter = 
            new VoidVisitorAdapter<Object>() {
                @Override
                public void visit(JavadocComment comment, Object arg) {
                    super.visit(comment, arg);
                    Optional<Node> o = comment.getCommentedNode();
                    if( o.get() != null ) {
                    	if( !(o.get() instanceof CompilationUnit)) {
                    		Node n = o.get();
                    		if( n instanceof MethodDeclaration ) {
                        		String jdoc = comment.toString();
                        		jdoc = jdoc.replaceAll("/\\*", "").replaceAll("\\*/", "").replaceAll("\\*","").replaceAll("\\s+", " ");
                        		sb.append(jdoc);
                        		sb.append("\n");
                        		sb.append("This endpoint takes the following json schemas as parameters: ");
                    			MethodDeclaration md = (MethodDeclaration)n;
                    			NodeList<Parameter> params = md.getParameters();
                    			for( Parameter p : params ) {
                    				Type t = p.getType();
                    				String typeName = t.toString();
                    				Path daoFile = getDaoJsonFile(typeName);
                    				if( daoFile.toFile().exists()) {
	                    				try {
		                    				String content = new String(Files.readAllBytes(daoFile));
		                    				sb.append(content);
	                    				} catch(IOException ioe) {
	                    					ioe.printStackTrace();
	                    				}
                    				} else {
                    					System.out.println("This should fail probably");
                    				}
                    			}
                    		}
                    	}
                    }
                }
            };
        try {
    		CompilationUnit cu = JavaParser.parse(f);
        	adapter.visit(cu, null);
        } catch (IOException e) {
            new RuntimeException(e);
        }
	}
	
	private static void writeTypescriptSchemas() throws Exception {
		Class[] daoClasses = getDAOClasses();
		File daoFolder = getDaoTypescriptFolder().toFile();
		if( !daoFolder.exists()) {
			daoFolder.mkdirs();
		}
		
		for( int i = 0; i < daoClasses.length; i++ ) {
			Class c = daoClasses[i];
			Path p = getDaoTypescriptFile(c.getSimpleName());
			File output = p.toFile();
			List<String> classes = Arrays.asList(new String[] { c.getName()});
			final Settings settings = new Settings();
			settings.outputKind = TypeScriptOutputKind.module;
			settings.jsonLibrary = JsonLibrary.jackson2;
			
			new TypeScriptGenerator(settings).generateTypeScript(
	                Input.fromClassNamesAndJaxrsApplication(classes, null, null, false, null, (URLClassLoader)c.getClassLoader(), true),
	                Output.to(output)
					);			
		}
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
	private static void writeJsonDAOSchemas() throws Exception {
		
		Class[] daoClasses = getDAOClasses();
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
	
	private static Path getDaoJsonFile(String simpleClassName) {
		Path folder = getDaoJsonFolder();
		Path out = folder.resolve(simpleClassName + ".json");
		return out;
	}
	
	private static Path getDaoJsonFolder() {
		return new File(".").toPath().resolve("src").resolve("main")
				.resolve("resources").resolve("schema").resolve("json");
	}

	private static Path getDaoTypescriptFile(String simpleClassName) {
		Path folder = getDaoTypescriptFolder();
		Path out = folder.resolve(simpleClassName + ".d.ts");
		return out;
	}
	private static Path getDaoTypescriptFolder() {
		return new File(".").toPath().resolve("src").resolve("main")
				.resolve("resources").resolve("schema").resolve("typescript");
	}

    private static class IgnoreURNSchemaFactoryWrapper extends SchemaFactoryWrapper {
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
    	
    private static final String printJSONSchema(Class c) {
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
}
