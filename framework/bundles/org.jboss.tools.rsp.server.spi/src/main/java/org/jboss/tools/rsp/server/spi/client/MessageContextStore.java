/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.client;

/*
 * A custom class for storing the context for any given message
 */
public class MessageContextStore<T> {
	private ThreadLocal<MessageContext<T>> messageContext = new ThreadLocal<>();

	public void setContext(MessageContext<T> context) {
		messageContext.set(context);
	}
	
	/**
	 * Get the context for the current request
	 * @return
	 */
	public MessageContext<T> getContext() {
		return messageContext.get();
	}
	
	/**
	 * Remove the context for this request. 
	 * Any new requests will need to set their context anew.
	 */
	public void clear() {
		messageContext.remove();
	}
	
	/**
	 * This object can be extended to include whatever other context
	 * from the raw message we may consider making available to implementations.
	 * At a minimum, it should make available the remote proxy, so a given
	 * request knows which remote proxy is making the request. 
	 */
	public static class MessageContext<T> {
		T remoteProxy;
		public MessageContext(T remoteProxy) {
			this.remoteProxy = remoteProxy;
		}
		
		public T getRemoteProxy() {
			return this.remoteProxy;
		}
	};
}