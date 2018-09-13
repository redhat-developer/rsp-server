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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class SpecificationGenerator {
	private JSONUtility json;
	private TypescriptUtility ts;

	public SpecificationGenerator(JSONUtility json, TypescriptUtility ts) {
		this.json = json;
		this.ts = ts;
	}

	private static File getClientInterfaceFile() throws IOException {
		File f2 = new File(".");
		File f = new File(f2, "../api/src/main/java/org/jboss/tools/rsp/api/RSPClient.java").getCanonicalFile();
		return f;
	}

	private static File getServerInterfaceFile() throws IOException {
		File f2 = new File(".");
		File f = new File(f2, "../api/src/main/java/org/jboss/tools/rsp/api/RSPServer.java").getCanonicalFile();
		return f;
	}

	public void printFileDocumentation(File f, StringBuffer sb, String segment) {
		VoidVisitorAdapter adapter = new VoidVisitorAdapter<Object>() {
			@Override
			public void visit(JavadocComment comment, Object arg) {
				super.visit(comment, arg);
				Optional<Node> o = comment.getCommentedNode();
				if (o.get() != null) {
					if (!(o.get() instanceof CompilationUnit)) {
						Node n = o.get();
						if (n instanceof MethodDeclaration) {
							MethodDeclaration md = (MethodDeclaration) n;
							String methodName = md.getNameAsString();
							sb.append("#### " + segment + "/" + methodName + "\n\n");
							String jdoc = comment.toString();
							jdoc = jdoc.replaceAll("/\\*", "").replaceAll("\\*/", "").replaceAll("\\*", "")
									.replaceAll("\\s+", " ");
							sb.append(jdoc);
							sb.append("\n\n");

							NodeList<Parameter> params = md.getParameters();
							if (params.size() == 0) {
								sb.append("This endpoint takes no parameters. \n\n");
							} else if (params.size() > 0) {
								sb.append("This endpoint takes the following json schemas as parameters: \n\n");

								sb.append("<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>");
								int i = 0;

								for (Parameter p : params) {

									Type t = p.getType();
									String typeName = t.toString();
									String jsonString = safeReadFile(json.getDaoJsonFile(typeName));
									String tsString = safeReadFile(ts.getDaoTypescriptFile(typeName));

									if (jsonString.isEmpty() || tsString.isEmpty()) {
										throw new RuntimeException(
												"Endpoints should have a single object parameter if possible.");
									}

									sb.append("\n<tr>");
									sb.append("<td>" + i + "</td>");
									i++;
									sb.append("<td><pre>");
									sb.append(jsonString);
									sb.append("</pre></td>");
									sb.append("<td><pre>");
									sb.append(tsString);
									sb.append("</pre></td>");

									sb.append("</tr>");
								}
								sb.append("</table>\n\n");
							}

							Type return2 = md.getType();
							String typeName = return2.toString();
							boolean completableFuture = false;
							if( typeName.startsWith("CompletableFuture<") && typeName.endsWith(">")) {
								typeName = typeName.substring("CompletableFuture<".length(), typeName.length() - 1);
								completableFuture = true;
							}
							
							boolean list = false;
							if( typeName.startsWith("List<") && typeName.endsWith(">")) {
								typeName = typeName.substring("List<".length(), typeName.length() - 1);
								list = true;
							}
							
							if (return2 == null || typeName.equals("void")) {
								sb.append("This endpoint returns no value");
							} else {
								String msg = "This endpoint returns the following schema as a return value: \n\n"; 
								if( list ) {
									msg = "This endpoint returns a list of the following schema as a return value: \n\n";
								}
								
								sb.append(msg);
								sb.append("<table><tr><th>json</th><th>typescript</th></tr>");
								String jsonString = safeReadFile(json.getDaoJsonFile(typeName));
								String tsString = safeReadFile(ts.getDaoTypescriptFile(typeName));
								sb.append("\n<tr>");
								sb.append("<td><pre>");
								sb.append(jsonString);
								sb.append("</pre></td>");
								sb.append("<td><pre>");
								sb.append(tsString);
								sb.append("</pre></td>");
								sb.append("</tr>");
								sb.append("</table>\n\n");
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

	private String safeReadFile(Path p) {
		if (p.toFile().exists()) {
			try {
				String content = new String(Files.readAllBytes(p));
				return content;
			} catch (IOException ioe) {
			}
		}
		return "";
	}

	public void generate() throws IOException {
		final StringBuffer sb = new StringBuffer();
		sb.append("\n\n### The Server Interface\n\n");
		printFileDocumentation(getServerInterfaceFile(), sb, "server");

		sb.append("\n\n### The Client Interface\n\n");
		printFileDocumentation(getClientInterfaceFile(), sb, "client");

		String protocol1 = readHeader();
		String protocol2 = sb.toString();

		String protocolDocs = protocol1 + protocol2;
		Path out = getMdFile("specification.md");
		Files.write(out, protocolDocs.getBytes());
		System.out.println("Wrote specification to " + out.toFile().getAbsolutePath());
	}

	private String readHeader() {
		Path p = getMdFile("lsp_base_protocol.md");
		if (p.toFile().exists()) {
			try {
				return new String(Files.readAllBytes(p));
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		return "";
	}

	public static Path getMdFolder() {
		return new File(".").toPath().resolve("src").resolve("main").resolve("resources").resolve("schemaMD");
	}

	public static Path getMdFile(String fileName) {
		Path folder = getMdFolder();
		Path out = folder.resolve(fileName);
		return out;
	}
}
