package org.dkf.jed2k.util;

public class RC4 {
	/**
	 * Sbox
	 */
    private static final int SBOX_LENGTH = 256;
	private int[] sbox = null;
    private int idx = 0;
    private int jdx = 0;

	public RC4() {
		reset();
	}

	public RC4(byte[] key, boolean skipDiscard) {
		createKey(key, skipDiscard);
	}

	public void reset() {
		sbox = null;
        idx = jdx = 0;
	}

	/**
	 * Crypt given byte array. Be aware, that you must init key, before using
	 * crypt.
	 *
	 * @param msg
	 *            array to be crypt
	 * @return crypted byte array
	 * @see <a
	 *      href="http://en.wikipedia.org/wiki/RC4#Pseudo-random_generation_algorithm_.28PRGA.29">Pseudo-random
	 *      generation algorithm</a>
	 */
	public void crypt(final byte[] msg, final byte[] code, final int len) {
        assert sbox != null;
		int i = idx;
		int j = jdx;
		for (int n = 0; n < len; n++) {
			i = (i + 1) % SBOX_LENGTH;
			j = (j + sbox[i]) % SBOX_LENGTH;
			swap(i, j, sbox);
			int rand = sbox[(sbox[i] + sbox[j]) % SBOX_LENGTH];
            if (msg != null && code != null) {
                code[n] = (byte) (rand ^ msg[n]);
            }
		}

        idx = i;
        jdx = j;
        assert jdx >= 0 && jdx < sbox.length;
        assert idx >= 0 && idx < sbox.length;
	}

    public byte[] crypt(final byte[] msg) {
        assert msg != null;
        byte code[] = new byte[msg.length];
        crypt(msg, code, msg.length);
        return code;
    }

	/**
	 * Initialize SBOX with given key. Key-scheduling algorithm
	 *
	 * @param key
	 *            key
	 * @return sbox int array
	 * @see <a
	 *      href="http://en.wikipedia.org/wiki/RC4#Key-scheduling_algorithm_.28KSA.29">Wikipedia.
	 *      Init sbox</a>
	 */
	private void createKey(byte[] key, boolean skipDiscard) {
        assert sbox == null;

		sbox = new int[SBOX_LENGTH];
		int j = 0;

		for (int i = 0; i < SBOX_LENGTH; i++) {
			sbox[i] = i;
		}

		for (int i = 0; i < SBOX_LENGTH; i++) {
			j = (j + sbox[i] + (0xFF & (int)key[i % key.length])) % SBOX_LENGTH;
			swap(i, j, sbox);
		}

		if (!skipDiscard) crypt(null, null, 1024);
	}

	private void swap(int i, int j, int[] sbox) {
		int temp = sbox[i];
		sbox[i] = sbox[j];
		sbox[j] = temp;
	}
}
