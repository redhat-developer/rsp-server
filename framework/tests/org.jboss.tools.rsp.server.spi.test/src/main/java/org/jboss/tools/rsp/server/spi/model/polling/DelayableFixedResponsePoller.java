/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model.polling;

import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class DelayableFixedResponsePoller extends AbstractPoller {

	public static final long NO_DELAY = -1;

	private SERVER_STATE onePingResult;
	private long delay;

	public DelayableFixedResponsePoller(SERVER_STATE onePingResult) {
		this(onePingResult, NO_DELAY);
	}

	public DelayableFixedResponsePoller(SERVER_STATE onePingResult, long delay) {
		this.onePingResult = onePingResult;
		this.delay = delay;
	}

	@Override
	protected String getThreadName() {
		return DelayableFixedResponsePoller.class.getSimpleName();
	}

	@Override
	protected SERVER_STATE onePing(IServer server) {
		sleep(delay);
		return onePingResult;
	}

	private void sleep(long delay) {
		if (delay > NO_DELAY) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
			}
		}
	}
}