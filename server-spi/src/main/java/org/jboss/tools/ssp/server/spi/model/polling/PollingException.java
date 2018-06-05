package org.jboss.tools.ssp.server.spi.model.polling;

public class PollingException extends Exception {

	private static final long serialVersionUID = -7830978018908940551L;

	public PollingException(String message) {
		super(message);
	}

	public PollingException(String message, Throwable t) {
		super(message, t);
	}
}