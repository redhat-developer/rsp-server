package org.jboss.tools.rsp.secure.crypto;

public class NotInitializedCryptoException extends CryptoException {
	 
    public NotInitializedCryptoException() {
    }
 
    public NotInitializedCryptoException(String message, Throwable throwable) {
        super(message, throwable);
    }
}