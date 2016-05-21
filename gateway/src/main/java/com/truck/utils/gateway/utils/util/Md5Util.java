package com.truck.utils.gateway.utils.util;

import com.truck.utils.gateway.utils.annotation.ThreadSafe;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by truck on 2015/11/30.
 */

@ThreadSafe
public class Md5Util {
    public static final byte[] compute(byte[] content) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return md5.digest(content);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String computeToHex(byte[] content) {
        return HexStringUtil.toHexString(compute(content));
    }

    public static final String computeToBase64(byte[] content) {
        return Base64Util.encodeToString(compute(content));
    }
}

