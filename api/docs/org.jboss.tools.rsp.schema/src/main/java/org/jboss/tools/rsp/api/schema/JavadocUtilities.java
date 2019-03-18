package org.jboss.tools.rsp.api.schema;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class JavadocUtilities {

	public static HashMap<String, JavadocComment> methodToJavadocMap(File f) {
		HashMap<String, JavadocComment> map = new LinkedHashMap<>();
		VoidVisitorAdapter adapter = new VoidVisitorAdapter<Object>() {
			@Override
			public void visit(JavadocComment comment, Object arg) {
				super.visit(comment, arg);
				Optional<Node> o = comment.getCommentedNode();
				if (o.get() != null) {
					if (!(o.get() instanceof CompilationUnit)) {
						Node n = o.get();
						if (n instanceof MethodDeclaration) {
							map.put(((MethodDeclaration)n).getNameAsString(), comment);
						}
					}
				}
			}
		};
		try {
			CompilationUnit cu = JavaParser.parse(f);
			adapter.visit(cu, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return map;
	}
	
	public static boolean isNotification(MethodDeclaration dec) {
		NodeList<AnnotationExpr> annotations = dec.getAnnotations();
		for( AnnotationExpr a : annotations) {
			String annotName = annotations.get(0).getNameAsString();
			if( annotName.equalsIgnoreCase("JsonNotification")) {
				return true;
			}
		}
		return false;
	}

	public static boolean isRequest(MethodDeclaration dec) {
		NodeList<AnnotationExpr> annotations = dec.getAnnotations();
		for( AnnotationExpr a : annotations) {
			String annotName = annotations.get(0).getNameAsString();
			if( annotName.equalsIgnoreCase("JsonRequest")) {
				return true;
			}
		}
		return false;
	}

}
