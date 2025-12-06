package com.training.demo.helpers;

import lombok.extern.slf4j.Slf4j;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class VnPayHelper {

    private static final String ALGORITHM = "HmacSHA512";

    /**
     * Calculate HMAC SHA512 hash
     *
     * @param key  the secret key
     * @param data the data to be hashed
     * @return the HMAC SHA512 hash in hexadecimal format
     */
    public static String hmacSHA512(String key, String data) {
        log.info("HMAC SHA512 RUN");
        try {
            Mac hmac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            hmac.init(secretKey);
            byte[] rawHmac = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(rawHmac.length * 2);
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating HMAC SHA512", e);
        }
    }

    /**
     * Build URL query string from parameters
     *
     * @param params    the map of parameters
     * @param urlEncode whether to URL-encode the parameter values
     * @return the constructed query string
     */
    public static String buildQuery(Map<String, String> params, boolean urlEncode) {
        log.info("Building Query - URL Encode: {}", urlEncode);

        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys); // ✅ luôn sort theo alphabet như spec

        StringBuilder query = new StringBuilder();
        int count = 0;

        for (String key : keys) {
            String value = params.get(key);
            if (value == null || value.isEmpty()) continue;

            if (query.length() > 0) {
                query.append('&');
            }

            query.append(key).append('=');

            if (urlEncode) {
                query.append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
            } else {
                query.append(value);
            }

            count++;
            log.debug("  Param {}: {}={}", count, key, value);
        }

        if (query.length() > 0) {
            log.info("Built query with {} params: {}", count,
                    query.substring(0, Math.min(200, query.length())));
        } else {
            log.info("Built query with 0 params");
        }

        return query.toString();
    }

    /**
     * Validate the signature of VNPay parameters
     *
     * @param vnpParams  the map of VNPay parameters
     * @param secret the secret key for hashing
     * @return true if the signature is valid, false otherwise
     */
    public static boolean validateSignature(Map<String, String> vnpParams, String secret) {
        String receivedHash = vnpParams.get("vnp_SecureHash");

        Map<String, String> filtered = new HashMap<>();
        for (Map.Entry<String, String> e : vnpParams.entrySet()) {
            if (!"vnp_SecureHash".equals(e.getKey()) &&
                    !"vnp_SecureHashType".equals(e.getKey())) {
                filtered.put(e.getKey(), e.getValue());
            }
        }

        // ❗ HASH TRÊN CHUỖI ĐÃ ENCODE, đã sort trong buildQuery
        String data = buildQuery(filtered, true);
        String calculated = hmacSHA512(secret, data);

        log.info("VNPay validateSignature - data      : {}", data);
        log.info("VNPay validateSignature - calculated: {}", calculated);
        log.info("VNPay validateSignature - received  : {}", receivedHash);

        return calculated.equalsIgnoreCase(receivedHash);
    }
}
