/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mikhail Kalkov - Bug 414285, On systems with large RAM, evaluateSystemProperties and generateLibraryInfo fail for 64-bit JREs
 *******************************************************************************/
package org.jboss.tools.rsp.eclipse.jdt.launching;


import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.internal.launching.java.RunningVMSyspropCache;
import org.jboss.tools.rsp.internal.launching.java.util.LaunchingSupportUtils;
/**
 * Abstract implementation of a VM install.
 * <p>
 * Clients implementing VM installs must subclass this class.
 * </p>
 */
public abstract class AbstractVMInstall implements IVMInstall {

	private static final String VM_INSTALL_ASSERT_ID_NOT_NULL = "id cannot be null";
	private static final String VM_INSTALL_ASSERT_TYPE_NOT_NULL = "VM type cannot be null";
	// system properties are cached in user preferences prefixed with this key, followed
	// by VM type, VM id, and system property name
	private static final String PREF_VM_INSTALL_SYSTEM_PROPERTY = "PREF_VM_INSTALL_SYSTEM_PROPERTY"; //$NON-NLS-1$

	private IVMInstallRegistry registry;
	private IVMInstallType fType;
	private String fId;
	private String fName;
	private File fInstallLocation;
	private LibraryLocation[] fSystemLibraryDescriptions;
	private URL fJavadocLocation;
	private String fVMArgs;

	/**
	 * Map VM specific attributes that are persisted restored with a VM install.
	 * @since 3.4
	 */
	private Map<String, String> fAttributeMap = new HashMap<>();

	// whether change events should be fired
	private boolean fNotify = true;

	/**
	 * Constructs a new VM install.
	 *
	 * @param	type	The type of this VM install.
	 * 					Must not be <code>null</code>
	 * @param	id		The unique identifier of this VM instance
	 * 					Must not be <code>null</code>.
	 * @throws	IllegalArgumentException	if any of the required
	 * 					parameters are <code>null</code>.
	 */
	public AbstractVMInstall(IVMInstallType type, String id) {
		if (type == null) {
			throw new IllegalArgumentException(VM_INSTALL_ASSERT_TYPE_NOT_NULL);
		}
		if (id == null) {
			throw new IllegalArgumentException(VM_INSTALL_ASSERT_ID_NOT_NULL);
		}
		fType= type;
		fId= id;
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMInstall#getId()
	 */
	@Override
	public String getId() {
		return fId;
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMInstall#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMInstall#setName(String)
	 */
	@Override
	public void setName(String name) {
		if (!name.equals(fName)) {
			PropertyChangeEvent event = new PropertyChangeEvent(this, IVMInstallChangedListener.PROPERTY_NAME, fName, name);
			fName= name;
			if (fNotify) {
				fireVMChanged(event);
			}
		}
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMInstall#getInstallLocation()
	 */
	@Override
	public File getInstallLocation() {
		return fInstallLocation;
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMInstall#setInstallLocation(File)
	 */
	@Override
	public void setInstallLocation(File installLocation) {
		if (!installLocation.equals(fInstallLocation)) {
			PropertyChangeEvent event = new PropertyChangeEvent(this, IVMInstallChangedListener.PROPERTY_INSTALL_LOCATION, fInstallLocation, installLocation);
			fInstallLocation= installLocation;
			if (fNotify) {
				fireVMChanged(event);
			}
		}
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMInstall#getVMInstallType()
	 */
	@Override
	public IVMInstallType getVMInstallType() {
		return fType;
	}

	/* (non-Javadoc)
	 * @see IVMInstall#getVMRunner(String)
	 */
	@Override
	public IVMRunner getVMRunner(String mode) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstall#getLibraryLocations()
	 */
	@Override
	public LibraryLocation[] getLibraryLocations() {
		return fSystemLibraryDescriptions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstall#setLibraryLocations(org.eclipse.jdt.launching.LibraryLocation[])
	 */
	@Override
	public void setLibraryLocations(LibraryLocation[] locations) {
		if (locations == fSystemLibraryDescriptions) {
			return;
		}
		LibraryLocation[] newLocations = locations;
		if (newLocations == null) {
			newLocations = getVMInstallType().getDefaultLibraryLocations(getInstallLocation());
		}
		LibraryLocation[] prevLocations = fSystemLibraryDescriptions;
		if (prevLocations == null) {
			prevLocations = getVMInstallType().getDefaultLibraryLocations(getInstallLocation());
		}

		if (newLocations.length == prevLocations.length) {
			int i = 0;
			boolean equal = true;
			while (i < newLocations.length && equal) {
				equal = newLocations[i].equals(prevLocations[i]);
				i++;
			}
			if (equal) {
				// no change
				return;
			}
		}

		PropertyChangeEvent event = new PropertyChangeEvent(this, IVMInstallChangedListener.PROPERTY_LIBRARY_LOCATIONS, prevLocations, newLocations);
		fSystemLibraryDescriptions = locations;
		if (fNotify) {
			fireVMChanged(event);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstall#getJavadocLocation()
	 */
	@Override
	public URL getJavadocLocation() {
		return fJavadocLocation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstall#setJavadocLocation(java.net.URL)
	 */
	@Override
	public void setJavadocLocation(URL url) {
		if (url == fJavadocLocation) {
			return;
		}
		if (url != null && fJavadocLocation != null) {
			if (url.toExternalForm().equals(fJavadocLocation.toExternalForm())) {
				// no change
				return;
			}
		}

		PropertyChangeEvent event = new PropertyChangeEvent(this, IVMInstallChangedListener.PROPERTY_JAVADOC_LOCATION, fJavadocLocation, url);
		fJavadocLocation = url;
		if (fNotify) {
			fireVMChanged(event);
		}
	}

	/**
	 * Whether this VM should fire property change notifications.
	 *
	 * @param notify if this VM should fire property change notifications.
	 * @since 2.1
	 */
	protected void setNotify(boolean notify) {
		fNotify = notify;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
     * @since 2.1
	 */
	@Override
	public boolean equals(Object object) {
		if (object instanceof IVMInstall) {
			IVMInstall vm = (IVMInstall)object;
			return getVMInstallType().equals(vm.getVMInstallType()) &&
				getId().equals(vm.getId());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 * @since 2.1
	 */
	@Override
	public int hashCode() {
		return getVMInstallType().hashCode() + getId().hashCode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstall#getDefaultVMArguments()
	 * @since 3.0
	 */
	@Override
	public String[] getVMArguments() {
		String args = getVMArgs();
		if (args == null) {
		    return null;
		}
		ExecutionArguments ex = new ExecutionArguments(args, ""); //$NON-NLS-1$
		return ex.getVMArgumentsArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstall#setDefaultVMArguments(java.lang.String[])
	 * @since 3.0
	 */
	@Override
	public void setVMArguments(String[] vmArgs) {
		if (vmArgs == null) {
			setVMArgs(null);
		} else {
		    StringBuffer buf = new StringBuffer();
		    for (int i = 0; i < vmArgs.length; i++) {
	            String string = vmArgs[i];
	            buf.append(string);
	            buf.append(" "); //$NON-NLS-1$
	        }
			setVMArgs(buf.toString().trim());
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.jdt.launching.IVMInstall2#getVMArgs()
     */
    @Override
	public String getVMArgs() {
        return fVMArgs;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.launching.IVMInstall2#setVMArgs(java.lang.String)
     */
    @Override
	public void setVMArgs(String vmArgs) {
        if (fVMArgs == null) {
            if (vmArgs == null) {
                // No change
                return;
            }
        } else if (fVMArgs.equals(vmArgs)) {
    		// No change
    		return;
    	}
        PropertyChangeEvent event = new PropertyChangeEvent(this, IVMInstallChangedListener.PROPERTY_VM_ARGUMENTS, fVMArgs, vmArgs);
        fVMArgs = vmArgs;
		if (fNotify) {
			fireVMChanged(event);
		}
    }

    /* (non-Javadoc)
     * Subclasses should override.
     * @see org.eclipse.jdt.launching.IVMInstall2#getJavaVersion()
     */
    @Override
	public String getJavaVersion() {
        return null;
    }

    
    private Map<String, String> loadSyspropsFromCache(String[] properties) {
    	return RunningVMSyspropCache.getDefault().getCachedValues(properties);
    }
    
    private void saveSyspropsInCache(Map<String, String> map) {
    	RunningVMSyspropCache.getDefault().setCachedValues(map);
    }

    	
    
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstall3#evaluateSystemProperties(java.lang.String[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Map<String, String> evaluateSystemProperties(String[] properties, IProgressMonitor monitor) throws CoreException {
		//locate the launching support jar - it contains the main program to run
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		Map<String, String> map = loadSyspropsFromCache(properties);
		if (map == null) {
			IVMRunner runner = getVMRunner("run");
			LaunchingSupportUtils util = new LaunchingSupportUtils();
			map = util.runAndParseLaunchingSupportSysprops(
					runner, properties, monitor);
			saveSyspropsInCache(map);
		}
		monitor.done();
		return map;
	}

	/**
	 * Generates a key used to cache system property for this VM in this plug-ins
	 * preference store.
	 *
	 * @param property system property name
	 * @return preference store key
	 */
	private String getSystemPropertyKey(String property) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(PREF_VM_INSTALL_SYSTEM_PROPERTY);
		buffer.append("."); //$NON-NLS-1$
		buffer.append(getVMInstallType().getId());
		buffer.append("."); //$NON-NLS-1$
		buffer.append(getId());
		buffer.append("."); //$NON-NLS-1$
		buffer.append(property);
		return buffer.toString();
	}

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 *
	 * @param message the status message
	 * @param exception lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code error code
	 * @throws CoreException the "abort" core exception
	 * @since 3.2
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, IVMInstallChangedListener.LAUNCHING_ID_PLUGIN,
				code, message, exception));
	}

	/**
	 * Sets a VM specific attribute. Attributes are persisted and restored with VM installs.
	 * Specifying a value of <code>null</code> as a value removes the attribute. Change
	 * notification is provided to {@link IVMInstallChangedListener} for VM attributes.
	 *
	 * @param key attribute key, cannot be <code>null</code>
	 * @param value attribute value or <code>null</code> to remove the attribute
	 * @since 3.4
	 */
	public void setAttribute(String key, String value) {
		String prevValue = fAttributeMap.remove(key);
		boolean notify = false;
		if (value == null) {
			if (prevValue != null && fNotify) {
				notify = true;
			}
		} else {
			fAttributeMap.put(key, value);
			if (fNotify && (prevValue == null || !prevValue.equals(value))) {
				notify = true;
			}
		}
		if (notify) {
			PropertyChangeEvent event = new PropertyChangeEvent(this, key, prevValue, value);
			fireVMChanged(event);
		}
	}

	/**
	 * Returns a VM specific attribute associated with the given key or <code>null</code>
	 * if none.
	 *
	 * @param key attribute key, cannot be <code>null</code>
	 * @return attribute value, or <code>null</code> if none
	 * @since 3.4
	 */
	public String getAttribute(String key) {
		return fAttributeMap.get(key);
	}

	/**
	 * Returns a map of VM specific attributes stored with this VM install. Keys
	 * and values are strings. Modifying the map does not modify the attributes
	 * associated with this VM install.
	 *
	 * @return map of VM attributes
	 * @since 3.4
	 */
	public Map<String, String> getAttributes() {
		return new HashMap<>(fAttributeMap);
	}

	private void fireVMChanged(PropertyChangeEvent event) {
		if (getRegistry() != null)
			getRegistry().fireVMChanged(event);
	}
	
	protected void setRegistry(IVMInstallRegistry reg) {
		this.registry = reg;
	}
	
	public IVMInstallRegistry getRegistry() {
		return registry;
	}
}
