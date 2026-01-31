package com.movies2.auth;

import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

final class Jwks {
    static RSAKey fromPem(String publicPem, String privatePem) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPublicKey pub = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(decodePem(publicPem)));
            RSAPrivateKey priv = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(decodePem(privatePem)));
            return new RSAKey.Builder(pub).privateKey(priv).keyID(UUID.randomUUID().toString()).build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA keys from PEM", e);
        }
    }

    private static byte[] decodePem(String pem) {
        String clean = pem
                .replace("-----BEGINPRIVATEKEY-----", "")
                .replace("-----ENDPRIVATEKEY-----", "")
                .replace("-----BEGINPUBLICKEY-----", "")
                .replace("-----ENDPUBLICKEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(clean);
    }

    static RSAKey generateRsa() {
        try {
            KeyPair pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            RSAPublicKey pub = (RSAPublicKey) pair.getPublic();
            RSAPrivateKey priv = (RSAPrivateKey) pair.getPrivate();
            return new RSAKey.Builder(pub)
                    .privateKey(priv)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA algorithm not available", e);
        }
    }
}
