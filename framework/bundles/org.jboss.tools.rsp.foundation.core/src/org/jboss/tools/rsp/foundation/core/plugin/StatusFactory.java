/*******************************************************************************
 * Copyright (c) 2013 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.foundation.core.plugin;

import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.MultiStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;

/**
 * This class assists in making status objects.
 * 
 * The methods may be called in a static fashion if desired, 
 * or you may instantiate your own StatusFactory with a plugin-id
 * for easier use. 
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class StatusFactory {
	public final static String EMPTY_MESSAGE = ""; //$NON-NLS-1$
	private static final String NO_MESSAGE1 = ": <no message>";
	private static final String NO_MESSAGE2 = "<no message>";
	

	public static IStatus getInstance(int severity, String pluginId, int code,
			String message, Throwable t) {
		return new Status(severity, pluginId, code, checkMessage(message, t), t);
	}

	public static IStatus getInstance(int severity, String pluginId,
			String message, Throwable t) {
		return getInstance(severity, pluginId, 0, message, t);
	}

	public static IStatus getInstance(int severity, String pluginId,
			String message) {
		return getInstance(severity, pluginId, 0, message, null);
	}

	public static IStatus getInstance(int severity, String pluginId, Throwable t) {
		return getInstance(severity, pluginId, 0, EMPTY_MESSAGE, t);
	}

	public static IStatus getInstance(int severity, String pluginId, int code,
			Throwable t) {
		return getInstance(severity, pluginId, code, EMPTY_MESSAGE, t);
	}

	public static IStatus getInstance(int severity, String pluginId, int code,
			String message) {
		return getInstance(severity, pluginId, code, message, null);
	}
	
	public static IStatus errorStatus(String pluginId, String message) {
		return getInstance(IStatus.ERROR, pluginId, 0, message, null);
	}
	public static IStatus errorStatus(String pluginId, String message, Throwable throwable) {
		return getInstance(IStatus.ERROR, pluginId, 0, message, throwable);
	}
	public static IStatus errorStatus(String pluginId, String message, Throwable throwable, int code) {
		return getInstance(IStatus.ERROR, pluginId, code, message, throwable);
	}

	public static IStatus warningStatus(String pluginId, String message) {
		return getInstance(IStatus.WARNING, pluginId, 0, message, null);
	}
	public static IStatus warningStatus(String pluginId, String message, Throwable throwable) {
		return getInstance(IStatus.WARNING, pluginId, 0, message, throwable);
	}
	public static IStatus warningStatus(String pluginId, String message, Throwable throwable, int code) {
		return getInstance(IStatus.WARNING, pluginId, code, message, throwable);
	}
	
	public static IStatus infoStatus(String pluginId, String message) {
		return getInstance(IStatus.INFO, pluginId, 0, message, null);
	}
	public static IStatus infoStatus(String pluginId, String message, Throwable throwable) {
		return getInstance(IStatus.INFO, pluginId, 0, message, throwable);
	}
	public static IStatus infoStatus(String pluginId, String message, Throwable throwable, int code) {
		return getInstance(IStatus.INFO, pluginId, code, message, throwable);
	}

	public static IStatus cancelStatus(String pluginId, String message) {
		return getInstance(IStatus.CANCEL, pluginId, 0, message, null);
	}
	public static IStatus cancelStatus(String pluginId, String message, Throwable throwable) {
		return getInstance(IStatus.CANCEL, pluginId, 0, message, throwable);
	}
	public static IStatus cancelStatus(String pluginId, String message, Throwable throwable, int code) {
		return getInstance(IStatus.CANCEL, pluginId, code, message, throwable);
	}

	
	
	public static IStatus throwableToStatus(String pluginId, Throwable t) {
		return throwableToStatus(pluginId, t, 0);
	}
	

	/**
	 * Creates a status for each level of an exception, if it is 
	 * a nested exception. If the status is more than one level
	 * 'deep', it will return a MultiStatus
	 * @param pluginId
	 * @param t
	 * @param code
	 * @return
	 */
	public static IStatus throwableToStatus(String pluginId, Throwable t, int code) {
		return throwableToStatus(IStatus.ERROR, pluginId, t, code);
	}

	/**
	 * 
	 * Creates a status for each level of an exception, if it is 
	 * a nested exception. If the status is more than one level
	 * 'deep', it will return a MultiStatus
	 * @param severity
	 * @param pluginId
	 * @param t
	 * @return
	 */
	public static IStatus throwableToStatus(int severity, String pluginId, Throwable t) {
		return throwableToStatus(severity, pluginId, t, 0);
	}
	
	/**
	 * Creates a status for each level of an exception, if it is 
	 * a nested exception. If the status is more than one level
	 * 'deep', it will return a MultiStatus
	 * 
	 * @param severity
	 * @param pluginId
	 * @param t
	 * @param code
	 * @return
	 */
	public static IStatus throwableToStatus(int severity, String pluginId, Throwable t, int code) {
		List<IStatus> causes = new ArrayList<IStatus>();
		Throwable temp = t;
		String msg = null;
		while (temp != null && temp.getCause() != temp) {
			causes.add(new Status(
					severity, pluginId, code,
					temp.getMessage() == null ? temp.toString()
							+ NO_MESSAGE1
							: temp.toString(), temp));
			temp = temp.getCause();
			if( msg == null && temp != null) {
				msg = temp.getMessage();
			}
		}
		msg = (msg == null ? NO_MESSAGE2 : msg);
		if (t != null && t.getMessage() != null) {
			msg = t.toString();
		}

		if (causes.isEmpty()) {
			return new Status(severity, pluginId, code, msg, t);
		} else {
			return new MultiStatus(pluginId, code, causes.toArray(
					new IStatus[causes.size()]), msg, t);
		}
	}

	/**
	 * Returns a multi status with the given severity, plugin id, error code,
	 * message, cause and child status instances. This implementation
	 * is not fully recursive. 
	 * 
	 * @param severity
	 * @param pluginId
	 * @param message
	 * @param t
	 * @param status
	 * @return a multi status
	 */
	public static IStatus getMultiStatusInstance(int severity, String pluginId,
			String message, Throwable t, IStatus... status) {
		return new MultiStatus(pluginId, 0, status, message, t);
	}

	private static String checkMessage(String message, Throwable t) {
		if (message == null) {
			if (t != null && t.getMessage() != null) {
				return t.getMessage();
			}
			return NO_MESSAGE2;
		}
		return message;
	}
	
	
	
	private String pluginId;
	
	/**
	 * A public constructor so that plugins may instantiate with their
	 * plugin ID in the constructor to minimize logging parameters.
	 */
	public StatusFactory(String pluginId) {
		this.pluginId = pluginId;
	}

	/*
	 * Instance methods to aid in simpler logging without requiring
	 * a plugin or plugin-id to be passed in to each method signature.
	 */
	public IStatus errorStatus(String message) {
		return errorStatus(pluginId, checkMessage(message,null));
	}
	public IStatus errorStatus(String message, Throwable t) {
		return errorStatus(pluginId, checkMessage(message,t), t);
	}
	public IStatus errorStatus(Throwable t) {
		return throwableToStatus(IStatus.ERROR, pluginId, t);
	}

	public IStatus warningStatus(String message) {
		return warningStatus(pluginId, checkMessage(message,null));
	}
	public IStatus warningStatus(String message, Throwable t) {
		return warningStatus(pluginId, checkMessage(message,t), t);
	}
	public IStatus warningStatus(Throwable t) {
		return throwableToStatus(IStatus.WARNING, pluginId, t);
	}
	public IStatus cancelStatus(String message) {
		return cancelStatus(pluginId, checkMessage(message,null));
	}
	public IStatus cancelStatus(String message, Throwable t) {
		return cancelStatus(pluginId, checkMessage(message,t), t);
	}
	public IStatus cancelStatus(Throwable t) {
		return cancelStatus(pluginId, checkMessage(null,t));
	}
	
}
