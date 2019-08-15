/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc. IBM Corporation - Initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.server.core.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.launching.memento.IMemento;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.ServerCoreActivator;

/**
 * Helper class for storing runtime and server attributes.
 */
public abstract class Base {
	protected static final String PROP_LOCKED = "locked";
	protected static final String PROP_PRIVATE = "private";
	protected static final String PROP_NAME = "name";
	protected static final String PROP_ID = "id";
	protected static final String PROP_ID_SET = "id-set";
	protected static final String PROP_TIMESTAMP = "timestamp";
	
	private static final String MAP_PROPERTIES_KEY = "mapProperties";
	private static final String LIST_PROPERTIES_KEY = "listProperties";
	private static final String PROPERTY_KEY_VALUE_PREFIX = "value";
	
	protected Map<String, Object> map = new HashMap<>();
	protected File file;
	private transient List<PropertyChangeListener> propertyListeners;
	/**
	 * Create a new object.
	 * 
	 * @param file
	 */
	public Base(File file) {
		this.file = file;
	}

	/**
	 * Create a new object.
	 * 
	 * @param file
	 * @param id
	 */
	public Base(File file, String id) {
		this(file);
		if (id != null && id.length() > 0) {
			map.put(PROP_ID, id);
			map.put(PROP_ID_SET, Boolean.toString(true));
		}
	}

	/**
	 * Returns the timestamp of this object.
	 * Timestamps are monotonically increased each time the object is saved
	 * and can be used to determine if any changes have been made on disk
	 * since the object was loaded.
	 * 
	 * @return the object's timestamp
	 */
	public int getTimestamp() {
		return getAttribute(PROP_TIMESTAMP, -1);
	}

	/**
	 * Returns <code>true</code> if the attribute is currently set, and <code>false</code>
	 * otherwise.
	 * 
	 * @param attributeName
	 * @return <code>true</code> if the attribute is currently set, and <code>false</code>
	 *    otherwise
	 */
	public boolean isAttributeSet(String attributeName) {
		try {
			Object obj = map.get(attributeName);
			if (obj != null)
				return true;
		} catch (Exception e) {
			// ignore
		}
		return false;
	}

	public String getAttribute(String attributeName, String defaultValue) {
		try {
			Object obj = map.get(attributeName);
			if (obj == null)
				return defaultValue;
			return (String) obj;
		} catch (Exception e) {
			// ignore
		}
		return defaultValue;
	}

	public int getAttribute(String attributeName, int defaultValue) {
		try {
			Object obj = map.get(attributeName);
			if (obj == null)
				return defaultValue;
			return Integer.parseInt((String) obj);
		} catch (Exception e) {
			// ignore
		}
		return defaultValue;
	}

	public boolean getAttribute(String attributeName, boolean defaultValue) {
		try {
			Object obj = map.get(attributeName);
			if (obj == null)
				return defaultValue;
			return Boolean.parseBoolean((String) obj);
		} catch (Exception e) {
			// ignore
		}
		return defaultValue;
	}

	@SuppressWarnings("unchecked")
	public List<String> getAttribute(String attributeName, List<String> defaultValue) {
		try {
			Object obj = map.get(attributeName);
			if (obj == null)
				return defaultValue;
			return (List<String>) obj;
		} catch (Exception e) {
			// ignore
		}
		return defaultValue;
	}

	public Map<?, ?> getAttribute(String attributeName, Map<?, ?> defaultValue) {
		try {
			Object obj = map.get(attributeName);
			if (obj == null)
				return defaultValue;
			return (Map<?, ?>) obj;
		} catch (Exception e) {
			// ignore
		}
		return defaultValue;
	}

	public void setAttribute(String attributeName, int value) {
		int current = getAttribute(attributeName, 0);
		if (isAttributeSet(attributeName) && current == value)
			return;
		map.put(attributeName, Integer.toString(value));
		firePropertyChangeEvent(attributeName, Integer.valueOf(current), Integer.valueOf(value));
	}

	public void setAttribute(String attributeName, boolean value) {
		boolean current = getAttribute(attributeName, false);
		if (isAttributeSet(attributeName) && current == value)
			return;
		map.put(attributeName, Boolean.toString(value));
		firePropertyChangeEvent(attributeName, Boolean.valueOf(current), Boolean.valueOf(value));
	}

	public void setAttribute(String attributeName, String value) {
		String current = getAttribute(attributeName, (String)null);
		if (isAttributeSet(attributeName) && current != null && current.equals(value))
			return;
		
		if (value == null)
			map.remove(attributeName);
		else
			map.put(attributeName, value);
		firePropertyChangeEvent(attributeName, current, value);
	}

	public void setAttribute(String attributeName, List<String> value) {
		List<?> current = getAttribute(attributeName, (List<String>)null);
		if (isAttributeSet(attributeName) && current != null && current.equals(value))
			return;
		if (value == null)
			map.remove(attributeName);
		else
			map.put(attributeName, value);
		firePropertyChangeEvent(attributeName, current, value);
	}

	public void setAttribute(String attributeName, Map<?,?> value) {
		Map<?,?> current = getAttribute(attributeName, (Map<?, ?>)null);
		if (isAttributeSet(attributeName) && current != null && current.equals(value))
			return;
		if (value == null)
			map.remove(attributeName);
		else
			map.put(attributeName, value);
		firePropertyChangeEvent(attributeName, current, value);
	}

	public String getId() {
		return getAttribute(PROP_ID, "");
	}

	public String getName() {
		return getAttribute(PROP_NAME, "");
	}

	public boolean isReadOnly() {
		return getAttribute(PROP_LOCKED, false);
	}

	/**
	 * Returns <code>true</code> if this runtime is private (not shown
	 * in the UI to the users), and <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this runtime is private,
	 *    and <code>false</code> otherwise
	 */
	public boolean isPrivate() {
		return getAttribute(PROP_PRIVATE, false);
	}
	
	public boolean isWorkingCopy() {
		return false;
	}
	
	protected abstract String getXMLRoot();
	
	protected void save(IMemento memento) {
		IMemento child = memento;
		Iterator<String> iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Object obj = map.get(key);
			if (obj instanceof String)
				child.putString(key, (String) obj);
			else if (obj instanceof Integer) {
				Integer in = (Integer) obj;
				child.putInteger(key, in.intValue());
			} else if (obj instanceof Boolean) {
				Boolean bool = (Boolean) obj;
				child.putBoolean(key, bool.booleanValue());
			} else if (obj instanceof List) {
				List<String> list = (List<String>) obj;
				saveList(child, key, list);
			} else if (obj instanceof Map) {
				Map<String, String> map2 = (Map<String, String>) obj;
				saveMap(child, key, map2);
			}
		}
		saveState(child);
	}

	protected void saveMap(IMemento memento, String key, Map<String,String> map2) {
		IMemento child = memento.getChild(MAP_PROPERTIES_KEY);
		if( child == null )
			child = memento.createChild(MAP_PROPERTIES_KEY);
		
		IMemento keyChild = child.createChild(key);
		Iterator<String> iterator = map2.keySet().iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			keyChild.putString(s, map2.get(s));
		}
	}
	
	protected void saveList(IMemento memento, String key, List<String> list) {
		IMemento child = memento.getChild(LIST_PROPERTIES_KEY);
		if( child == null )
			child = memento.createChild(LIST_PROPERTIES_KEY);
		
		IMemento keyChild = child.createChild(key);
		int i = 0;
		Iterator<String> iterator = list.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			keyChild.putString(PROPERTY_KEY_VALUE_PREFIX + (i++), s);
		}
	}

	protected void saveToFile(IProgressMonitor monitor) throws CoreException {
		try {
			byte[] bytes = saveToBytes(monitor);
			if (file.exists()) {
				Files.delete(file.toPath());
			}
			Files.write(file.toPath(), bytes);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 0, 
					NLS.bind("Could not save server to file {0}", file.getAbsolutePath()), e));
		}
	}
	
	protected byte[] saveToBytes(IProgressMonitor monitor) throws CoreException {
		try {
			JSONMemento memento = JSONMemento.createWriteRoot();
			save(memento);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			memento.save(out);
			return out.toByteArray();
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, 
					ServerCoreActivator.BUNDLE_ID, 0, 
					"Could not save server to stream", e));
		}
	}

	protected void saveToMetadata(IProgressMonitor monitor) {
		// do nothing
	}

	protected abstract void saveState(IMemento memento);

	protected void load(IMemento memento) {
		map = new HashMap<>();
		Iterator<String> iterator = memento.getNames().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			map.put(key, memento.getString(key));
		}
		IMemento[] children = memento.getChildren(LIST_PROPERTIES_KEY);
		if (children != null) {
			for (IMemento child : children)
				loadList(child);
		}
		IMemento[] maps = memento.getChildren(MAP_PROPERTIES_KEY);
		if (maps != null) {
			for (IMemento m : maps)
				loadMap(m);
		}
		
		loadState(memento);
	}

	protected void loadMap(IMemento memento) {	
		IMemento[] kids = memento.getChildren();
		if( kids != null ) {
			for( int i = 0; i < kids.length; i++ ) {
				String name = kids[i].getNodeName();
				map.put(name, getMapFromMemento(kids[i]));
			}
		}
	}

	protected Map<String, String> getMapFromMemento(IMemento memento) {
		Map<String, String> vMap = new HashMap<>();
		Iterator<String> iterator = memento.getNames().iterator();
		while(iterator.hasNext()) {
			String s = iterator.next();
			String v = memento.getString(s);
			vMap.put(s,v);
		}
		return vMap;
	}

	protected void loadList(IMemento memento) {
		IMemento[] kids = memento.getChildren();
		if( kids != null ) {
			for( int i = 0; i < kids.length; i++ ) {
				String name = kids[i].getNodeName();
				map.put(name, getListFromMemento(kids[i]));
			}
		}
	}

	protected List<String> getListFromMemento(IMemento memento) {
		List<String> list = new ArrayList<>();
		int i = 0;
		String key2 = memento.getString(PROPERTY_KEY_VALUE_PREFIX + (i++));
		while (key2 != null) {
			list.add(key2);
			key2 = memento.getString(PROPERTY_KEY_VALUE_PREFIX + (i++));
		}
		return list;
	}
	
	protected abstract void loadState(IMemento memento);
	
	protected void resolve() {
		// do nothing
	}
	
	public void delete() throws CoreException {
		if (isWorkingCopy())
			throw new CoreException(new Status(IStatus.ERROR, 
					ServerCoreActivator.BUNDLE_ID, 0, "Cannot delete a working copy", null));
		
		if (file != null)
			deleteFromFile();
		else
			deleteFromMetadata();
	}

	protected void deleteFromFile() throws CoreException {
		try {
			Files.delete(file.toPath());
		} catch(IOException e) {
			IStatus status = new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Could not delete file " + file.getAbsolutePath());
			throw new CoreException(status);
		}
	}

	protected void deleteFromMetadata() {
		// do nothing
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Base))
			return false;
		
		Base base = (Base) obj;
		if (getId() == null)
			return false;
		if (!getId().equals(base.getId()))
			return false;
		
		if (isWorkingCopy() != base.isWorkingCopy())
			return false;
		
		if (isWorkingCopy() && this != base)
			return false;
		
		return true;
	}
	
	@Override 
	public int hashCode() {
		StringBuffer sb = new StringBuffer();
		sb.append("Base:");
		sb.append(getId());
		sb.append(":");
		sb.append(isWorkingCopy());
		return sb.toString().hashCode();
	}

	protected void loadFromFile(IProgressMonitor monitor) throws CoreException {
		try(InputStream in = new ByteArrayInputStream(Files.readAllBytes(file.toPath()))) {
			IMemento memento = loadMemento(in); 
			load(memento);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 0, 
					NLS.bind("Could not load server from file {0}", file.getAbsolutePath()), e));
		}
	}
	
	protected void loadFromMemento(IMemento memento, IProgressMonitor monitor) {
		load(memento);
	}
	
	protected void loadFromPath(IPath path, IProgressMonitor monitor) throws CoreException {
		try(InputStream in = new BufferedInputStream(new FileInputStream(path.toFile()))) {
			IMemento memento = loadMemento(in);
			load(memento);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 0, 
					NLS.bind("Error loading server from file {0}", path.toString()), e));
		}
	}

	protected IMemento loadMemento(InputStream in) throws IOException {
		return JSONMemento.loadMemento(in);
	}

	/**
	 * Fire a property change event.
	 * 
	 * @param propertyName a property name
	 * @param oldValue the old value
	 * @param newValue the new value
	 */
	public void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
		if (propertyListeners == null)
			return;
	
		PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
		try {
			Iterator<PropertyChangeListener> iterator = propertyListeners.iterator();
			while (iterator.hasNext()) {
				try {
					PropertyChangeListener listener = iterator.next();
					listener.propertyChange(event);
				} catch (Exception e) {
//					if (Trace.SEVERE) {
//						Trace.trace(Trace.STRING_SEVERE, "Error firing property change event", e);
//					}
				}
			}
		} catch (Exception e) {
//			if (Trace.SEVERE) {
//				Trace.trace(Trace.STRING_SEVERE, "Error in property event", e);
//			}
		}
	}

	
}
