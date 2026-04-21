package dev.xuya.ldcshop.util;

import dev.xuya.ldcshop.common.util.Ed25519Util;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Ed25519UtilTest {

    @Test
    void signAndVerify_roundTrip() {
        KeyPair keyPair = Ed25519Util.generateKeyPair();
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("client_id", "test_client");
        params.put("money", "10.00");
        params.put("order_name", "Test Order");
        String signString = Ed25519Util.buildSignString(params, "test_secret");

        String signature = Ed25519Util.sign(privateKey, signString);
        assertNotNull(signature);
        assertTrue(Ed25519Util.verify(publicKey, signString, signature));
    }

    @Test
    void verify_wrongSignature_returnsFalse() {
        KeyPair keyPair = Ed25519Util.generateKeyPair();
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        assertFalse(Ed25519Util.verify(publicKey, "test_data", "invalid_signature_base64"));
    }
}
