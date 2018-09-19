package org.jboss.tools.rsp.secure.test.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jboss.tools.rsp.secure.crypto.CryptoException;
import org.jboss.tools.rsp.secure.crypto.CryptoUtils;
import org.junit.Test;

public class CryptoUtilsTest {
	
	@Test
	public void testBadKey() {
		byte[] key = "i am very smrt".getBytes();
		String plain = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque ut massa gravida, suscipit velit sed, fermentum ex. Interdum et malesuada fames ac ante ipsum primis in faucibus. Maecenas scelerisque diam in quam luctus dignissim. Suspendisse sit amet condimentum elit. Nulla ex enim, euismod faucibus risus nec, efficitur accumsan eros. Nunc sodales pharetra risus. Sed sit amet leo in tellus volutpat rhoncus. Nulla sem ex, consequat at felis eget, fringilla euismod libero. Vestibulum pretium purus non viverra mollis. Curabitur ut lorem non arcu commodo elementum et venenatis enim.";
		CryptoUtils util = new CryptoUtils();
		try {
			byte[] encrypted = util.encrypt(key, plain.getBytes());
			fail();
		} catch(CryptoException ce) {
			return;
		}
	}
	
	@Test
	public void testPadding() {
		byte[] key = "i am very smrt".getBytes();
		String plain = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque ut massa gravida, suscipit velit sed, fermentum ex. Interdum et malesuada fames ac ante ipsum primis in faucibus. Maecenas scelerisque diam in quam luctus dignissim. Suspendisse sit amet condimentum elit. Nulla ex enim, euismod faucibus risus nec, efficitur accumsan eros. Nunc sodales pharetra risus. Sed sit amet leo in tellus volutpat rhoncus. Nulla sem ex, consequat at felis eget, fringilla euismod libero. Vestibulum pretium purus non viverra mollis. Curabitur ut lorem non arcu commodo elementum et venenatis enim.";
		CryptoUtils util = new CryptoUtils();
		try {
			byte[] encrypted = util.encrypt(key, plain.getBytes());
			fail();
		} catch(CryptoException ce) {
			return;
		}
		
		byte[] key2 = util.keyTo16(key);
		try {
			byte[] encrypted = util.encrypt(key2, plain.getBytes());
			byte[] decrypted = util.decrypt(key, encrypted);
			assertEquals(plain, new String(decrypted));
		} catch(CryptoException ce) {
			ce.printStackTrace();
			fail();
		}
	}

	
	
	@Test
	public void testEncryptDecrypt() {
		byte[] key = "i am very smrt!!".getBytes();
		String plain = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque ut massa gravida, suscipit velit sed, fermentum ex. Interdum et malesuada fames ac ante ipsum primis in faucibus. Maecenas scelerisque diam in quam luctus dignissim. Suspendisse sit amet condimentum elit. Nulla ex enim, euismod faucibus risus nec, efficitur accumsan eros. Nunc sodales pharetra risus. Sed sit amet leo in tellus volutpat rhoncus. Nulla sem ex, consequat at felis eget, fringilla euismod libero. Vestibulum pretium purus non viverra mollis. Curabitur ut lorem non arcu commodo elementum et venenatis enim.";
		CryptoUtils util = new CryptoUtils();
		try {
			byte[] encrypted = util.encrypt(key, plain.getBytes());
			byte[] decrypted = util.decrypt(key, encrypted);
			assertEquals(plain, new String(decrypted));
		} catch(CryptoException ce) {
			ce.printStackTrace();
			fail();
		}
	}
}
