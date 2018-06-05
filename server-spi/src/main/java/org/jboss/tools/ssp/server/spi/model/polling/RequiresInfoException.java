package org.jboss.tools.ssp.server.spi.model.polling;

public class RequiresInfoException extends Exception {

	private static final long serialVersionUID = 5050044329807740335L;
	private boolean checked = false;

	public RequiresInfoException(String msg) {
		super(msg);
	}

	public void setChecked() { 
		this.checked = true; 
	}

	public boolean getChecked() { 
		return this.checked; 
	}
}