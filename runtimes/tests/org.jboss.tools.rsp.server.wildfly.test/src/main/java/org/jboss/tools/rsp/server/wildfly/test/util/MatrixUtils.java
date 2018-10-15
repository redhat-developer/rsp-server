/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.test.util;

import java.util.ArrayList;
import java.util.Collection;

public class MatrixUtils {
	public static Object[] createPath(Object[][] blocks, int[] vals) {
		if( blocks.length == 0 )
			return new Object[0];
		
		Object[] ret = new Object[blocks.length];
		for( int i = 0; i < blocks.length; i++ ) {
			if( blocks[i].length > 0 )
				ret[i] = blocks[i][vals[i]];
		}
		return ret;
	}
	
	
	/**
	 * Accept an array of the following style:
	 *    Object[][] { 
	 *       new Object[] { "server1, server2"},
	 *       new Object[] { "zipped", "unzipped"}
	 *    };
	 * and return values of:
	 *    Object[][] {
	 *       new Object[] {"server1", "zipped"},
	 *       new Object[] {"server1", "unzipped"},
	 *       new Object[] {"server2", "zipped"},
	 *       new Object[] {"server2", "unzipped"}
	 *    }
	 * @param params
	 * @return
	 */
	public static ArrayList<Object[]> toMatrix(Object[][] blocks) {
		ArrayList<Object[]> paths = new ArrayList<Object[]>();
    	int depth = blocks.length;
    	int[] depthCount = new int[depth];
    	for( int i = 0; i < depth; i++ ) {
    		depthCount[i] = 0;
    		if( blocks[i] == null || blocks[i].length == 0) { 
    			// Abort, one of our options is a choice of 0
    			return paths;
    		}
    	}
    	boolean done = false;
    	while(!done ) {
    		paths.add(createPath(blocks, depthCount));
    		depthCount[depth-1]++;
    		for( int i = depth-1; i > 0; i-- ) {
    			if(depthCount[i] == blocks[i].length) {
    				depthCount[i] = 0;
    				depthCount[i-1]++;
    			} 
    		}
    		if( depthCount[0] == blocks[0].length)
    			done = true;
    	}
    	return paths;
	}
	

	// Turn an array [item1, item2, item3] into a collection of 1-length items
	// ie new Collection<Object[]>() { new Object[]{item1}, new Object[]{item2}, new Object[]{item3}};
	
	public static Collection<Object[]> asCollection(Object[] items) {
		ArrayList<Object[]> ret = new ArrayList<Object[]>();
		for( int i = 0; i < items.length; i++ ) {
			ret.add(new Object[]{items[i]});
		}
		return ret;
	}

}
