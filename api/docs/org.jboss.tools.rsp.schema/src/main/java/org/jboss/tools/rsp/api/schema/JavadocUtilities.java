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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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

	private JavadocUtilities() {
		// inhibit instantiation
	}
	
	public static Map<String, JavadocComment> methodToJavadocMap(File f) {
		HashMap<String, JavadocComment> map = new LinkedHashMap<>();
		VoidVisitorAdapter<Object> adapter = new VoidVisitorAdapter<Object>() {
			@Override
			public void visit(JavadocComment comment, Object arg) {
				super.visit(comment, arg);
				Optional<Node> o = comment.getCommentedNode();
				if (o.isPresent()
					&& (!(o.get() instanceof CompilationUnit))) {
					Node n = o.get();
					if (n instanceof MethodDeclaration) {
						map.put(((MethodDeclaration)n).getNameAsString(), comment);
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
		if (dec == null) {
			return false;
		}
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
		if (dec == null) {
			return false;
		}
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
