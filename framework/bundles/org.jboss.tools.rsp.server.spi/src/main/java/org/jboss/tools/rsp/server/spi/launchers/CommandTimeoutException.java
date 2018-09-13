package org.jboss.tools.rsp.server.spi.launchers;

import java.util.List;
import java.util.concurrent.TimeoutException;

public class CommandTimeoutException extends TimeoutException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 37067116146846743L;

	private static String getTimeoutError(List<String> output, List<String> err) {
		StringBuilder msg = new StringBuilder();
		msg.append("Process output:\n");
		output.forEach(line -> msg.append("   ").append(line));
		err.forEach(line -> msg.append("   ").append(line));
		return msg.toString();
	}

	private List<String> inLines;
	private List<String> errLines;

	public CommandTimeoutException(List<String> inLines, List<String> errLines) {
		super(getTimeoutError(inLines, errLines));
		this.inLines = inLines;
		this.errLines = errLines;
	}

	public List<String> getInLines() {
		return inLines;
	}

	public List<String> getErrLines() {
		return errLines;
	}
}
