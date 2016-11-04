package org.dkf.jed2k.test;

import org.dkf.jed2k.util.RC4;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RC4Test {

	@Test
	public void testCryptMessage() {
		String message = "Hello, World!";
		String key = "This is pretty long key";
		RC4 encr = new RC4(key.getBytes(), true);
		RC4 decr = new RC4(key.getBytes(), true);
		byte[] crypt = encr.crypt(message.getBytes());
		String msg = new String(decr.crypt(crypt));
		assertEquals(message, msg);
	}

}
