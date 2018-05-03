package schema;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.tools.ssp.api.beans.CreateServerAttribute;
import org.jboss.tools.ssp.api.beans.CreateServerAttributes;
import org.jboss.tools.ssp.api.beans.ServerAttributes;
import org.jboss.tools.ssp.api.beans.DiscoveryPath;
import org.jboss.tools.ssp.api.beans.ServerBean;
import org.jboss.tools.ssp.api.beans.ServerHandle;
import org.jboss.tools.ssp.api.beans.ServerProcess;
import org.jboss.tools.ssp.api.beans.ServerProcessOutput;
import org.jboss.tools.ssp.api.beans.ServerStateChange;
import org.jboss.tools.ssp.api.beans.StartServerAttributes;
import org.jboss.tools.ssp.api.beans.Status;
import org.jboss.tools.ssp.api.beans.StopServerAttributes;
import org.jboss.tools.ssp.api.beans.VMDescription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

public class GenerateSchemaMain {
	public static void main(String[] args) throws Exception {
		Class[] classes = new Class[] {
				Status.class,
				VMDescription.class,
				DiscoveryPath.class, ServerBean.class, 

				ServerHandle.class,
				ServerAttributes.class,
				CreateServerAttributes.class, CreateServerAttribute.class,
				ServerProcess.class, ServerProcessOutput.class,
				ServerStateChange.class, 
				StartServerAttributes.class, StopServerAttributes.class
		};
		Path folder = new File(".").toPath().resolve("src").resolve("main").resolve("resources");
		System.out.println(new File(".").getAbsolutePath());
		
		for( int i = 0; i < classes.length; i++ ) {
			Path out = folder.resolve(classes[i].getSimpleName() + ".json");
			String content = printSchema(classes[i]);
			Files.write(out, content.getBytes());
		}
		
	}
	
	
	private static final String printSchema(Class c) {
		try {
	        ObjectMapper mapper = new ObjectMapper();
	        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
	        mapper.acceptJsonFormatVisitor(c, visitor);
	        JsonSchema schema = visitor.finalSchema();
	        String asString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
	        return asString;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
