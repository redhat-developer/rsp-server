/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model.polling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller.CANCELATION_CAUSE;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller.SERVER_STATE;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AbstractPollerTest {

	private ExecutorService executor;

	@Before
	public void before() {
		this.executor = Executors.newSingleThreadExecutor();
	}

	@After
	public void after() {
		this.executor.shutdownNow();
	}

	@Test
	public void isNotCompleteIfHasNotPinged() throws PollingException, RequiresInfoException {
		DelayableFixedResponsePoller poller = new DelayableFixedResponsePoller(null);

		assertThat(poller.isComplete()).isFalse();
	}
	
	@Test
	public void isCompleteIfHasPinged() throws PollingException, RequiresInfoException {
		DelayableFixedResponsePoller poller = new DelayableFixedResponsePoller(null);
		poller.beginPolling(null, null);
		
		assertThat(waitForComplete(poller)).isTrue();
	}

	@Test
	public void isNotCompleteIfWasCancelled() throws PollingException, RequiresInfoException {
		DelayableFixedResponsePoller poller = new DelayableFixedResponsePoller(null, 10000);
		poller.beginPolling(null, null);
		poller.cancel(CANCELATION_CAUSE.CANCEL);

		assertThat(poller.isComplete()).isFalse();
	}

	@Test
	public void stateIsNullIfWasCancelled() throws PollingException, RequiresInfoException {
		DelayableFixedResponsePoller poller = new DelayableFixedResponsePoller(null, 10000);
		poller.beginPolling(null, null);
		poller.cancel(CANCELATION_CAUSE.CANCEL);

		assertThat(poller.getState()).isNull();
	}

	@Test
	public void getStateReturnsNullIfHasNotPinged() throws PollingException, RequiresInfoException {
		DelayableFixedResponsePoller poller = new DelayableFixedResponsePoller(null);
		
		assertThat(poller.getState()).isNull();
	}

	@Test
	public void getStateUNKNOWNIfPingingIsOngoing() throws PollingException, RequiresInfoException {
		DelayableFixedResponsePoller poller = new DelayableFixedResponsePoller(SERVER_STATE.UP, 1000); // delay pingOne
		poller.beginPolling(null, SERVER_STATE.UP);
		
		assertThat(poller.getState()).isEqualTo(SERVER_STATE.UNKNOWN);
	}

	@Test
	public void getStateReturnsCorrectStateAfterPinging() throws PollingException, RequiresInfoException {
		DelayableFixedResponsePoller poller = new DelayableFixedResponsePoller(SERVER_STATE.UP);
		poller.beginPolling(null, SERVER_STATE.UP);
		
		assertThat(waitForComplete(poller)).isEqualTo(true);
		assertThat(poller.getState()).isEqualTo(SERVER_STATE.UP);
	}

	private boolean waitForComplete(IServerStatePoller poller) {
		Future<Boolean> isComplete = executor.submit(() -> {
			while(!poller.isComplete()) {
				Thread.sleep(100);
			}
			return true;
		});
		
		try {
			return isComplete.get(10, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return false;
		}
	}


	
}

