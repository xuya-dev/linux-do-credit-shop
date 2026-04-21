package dev.xuya.ldcshop.util;

import dev.xuya.ldcshop.common.util.CryptoUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilTest {

    private final CryptoUtil cryptoUtil = new CryptoUtil("test-secret-key-for-unit-tests-32b");

    @Test
    void encryptAndDecrypt_roundTrip() {
        String original = "ABCDE-12345-FGHIJ-67890";
        String encrypted = cryptoUtil.encrypt(original);
        assertTrue(encrypted.startsWith("ENC:"));
        assertNotEquals(original, encrypted);

        String decrypted = cryptoUtil.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void decrypt_withoutEncPrefix_throws() {
        assertThrows(IllegalStateException.class, () -> cryptoUtil.decrypt("not-encrypted-content"));
    }

    @Test
    void encrypt_null_returnsNull() {
        assertNull(cryptoUtil.encrypt(null));
    }

    @Test
    void encrypt_blank_returnsSame() {
        assertEquals("", cryptoUtil.encrypt(""));
    }
}
