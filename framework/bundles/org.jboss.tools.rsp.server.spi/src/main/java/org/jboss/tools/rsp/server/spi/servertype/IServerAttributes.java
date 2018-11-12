/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.jboss.tools.rsp.server.spi.servertype;

import java.util.List;
import java.util.Map;
/**
 * Represents a server instance. Every server is an instance of a
 * particular, fixed server type.
 * <p>
 * Not surprisingly, the notion of <b>server</b> is central in the web tools
 * server infrastructure. In this context, understand that a server is
 * a web server of some ilk. It could be a simple web server lacking Java
 * support, or an J2EE based server, or perhaps even some kind of database
 * server. A more exact definition is not required for the purposes of this API.
 * From a tool-centric point of view, a server
 * is something that the developer is writing "content" for.
 * The unit of content is termed a deployable.
 * In a sense, the server exists, but lacks useful content. The
 * development task is to provide that content. The content can include
 * anything from simple, static HTML web pages to complex, highly dynamic
 * web applications.
 * In the course of writing and debugging this content,
 * the developer will want to test their content on a web server, to see how it
 * gets served up. For this they will need to launch a server process running on
 * some host machine (often the local host on which the IDE is running), or
 * attach to a server that's already running on a remote (or local) host. 
 * The newly developed content sitting in the developer's workspace needs to
 * end up in a location and format that the running server can use for its
 * serving purposes.
 * </p>
 * <p>
 * In this picture, an <code>IServer</code> object is a proxy for the real web
 * server. Through this proxy, a client can configure the server, and start,
 * stop, and restart it.
 * </p>
 * <p>
 * IServerAttributes implements IAdaptable to allow users to obtain a
 * server-type-specific class. By casting the runtime extension to the type
 * prescribed in the API documentation for that particular server type, the
 * client can access server-type-specific properties and methods.
 * getAdapter() may involve plugin loading, and should not be called from
 * popup menus, etc.
 * </p>
 * <p>
 * The server framework maintains a global list of all known server instances
 * ({@link ServerCore#getServers()}).
 * </p>
 * <p>
 * [rough notes:
 * Server has a state.
 * Server can be started, stopped, and restarted.
 * To modify server attributes, get a working copy, modify it, and then save it
 * to commit the changes.
 * Server attributes. Serialization.
 * Chained working copies for runtime, server configuration.
 * Server has a set of root deployable.
 * Deployabes have state wrt a server.
 * Restarting deployables.]
 * </p>
 * <p>
 * Two servers are identical if and only if they have the same id.
 * </p>
 * 
 * <p>This interface is not intended to be implemented by clients.</p>
 * 
 * @since 1.0
 */
public interface IServerAttributes {
	/**
	 * Returns the displayable name for this server.
	 * <p>
	 * Note that this name is appropriate for the current locale.
	 * </p>
	 *
	 * @return a displayable name
	 */
	public String getName();
	
	/**
	 * Returns the id of this server.
	 * Each server (of a given type) has a distinct id, fixed for
	 * its lifetime. Ids are intended to be used internally as keys;
	 * they are not intended to be shown to end users.
	 * 
	 * @return the server id
	 */
	public String getId();

	/**
	 * Get the server attribute value that is stored in this server attribute object.
	 * @param attributeName name of the attribute that is being queried.
	 * @param defaultValue the default value if the given attribute is not defined.
	 * @return the value of the given attribute.
	 */
	public int getAttribute(String attributeName, int defaultValue);

	/**
	 * Get the server attribute value that is stored in this server attribute object.
	 * @param attributeName name of the attribute that is being queried.
	 * @param defaultValue the default value if the given attribute is not defined.
	 * @return the value of the given attribute.
	 */
	public boolean getAttribute(String attributeName, boolean defaultValue);

	/**
	 * Get the server attribute value that is stored in this server attribute object.
	 * @param attributeName name of the attribute that is being queried.
	 * @param defaultValue the default value if the given attribute is not defined.
	 * @return the value of the given attribute.
	 */
	public String getAttribute(String attributeName, String defaultValue);

	/**
	 * Get the server attribute value that is stored in this server attribute object.
	 * @param attributeName name of the attribute that is being queried.
	 * @param defaultValue the default value if the given attribute is not defined.
	 * @return the value of the given attribute.
	 */
	public List<String> getAttribute(String attributeName, List<String> defaultValue);

	/**
	 * Get the server attribute value that is stored in this server attribute object.
	 * @param attributeName name of the attribute that is being queried.
	 * @param defaultValue the default value if the given attribute is not defined.
	 * @return the value of the given attribute.
	 */
	public Map getAttribute(String attributeName, Map defaultValue);

}