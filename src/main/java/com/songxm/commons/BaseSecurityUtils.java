package com.songxm.commons;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
@Slf4j
public class BaseSecurityUtils {
    private static final int BLOCK_SIZE = 117;
    private static final int OUTPUT_BLOCK_SIZE = 128;
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private static LoadingCache<String, Key> rsaPrivateKeyCache = CacheBuilder.newBuilder().maximumSize(10L).build(new CacheLoader<String, Key>() {
    	@Override
        public Key load(String key) throws Exception {
            try {
                byte[] e = Base64.getDecoder().decode(key);
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(e);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
                return privateKey;
            } catch (Exception var6) {
                throw new RuntimeException("获取rsa私钥异常", var6);
            }
        }


    });
    private static LoadingCache<String, Key> rsaPublicKeyCache = CacheBuilder.newBuilder().maximumSize(10L).build(new CacheLoader<String, Key>() {
        @Override
        public Key load(String key) throws Exception {
            try {
                byte[] e = Base64.getDecoder().decode(key);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(e);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey publicKey = keyFactory.generatePublic(keySpec);
                return publicKey;
            } catch (Exception ex) {
            	log.error("获取RSA公钥异常:{}",ex);
                throw new RuntimeException("获取rsa公钥异常", ex);
            }
        }
    });

    public BaseSecurityUtils() {
    }

    public static String md5(String src) {
        if(StringUtils.isBlank(src)) {
            return src;
        } else {
            try {
                MessageDigest e = MessageDigest.getInstance("MD5");
                e.update(src.getBytes("UTF-8"));
                byte[] digest = e.digest();
                StringBuffer hexString = new StringBuffer();

                for(int i = 0; i < digest.length; ++i) {
                    String strTemp = Integer.toHexString(digest[i] & 255 | -256).substring(6);
                    hexString.append(strTemp);
                }

                return hexString.toString();
            } catch (Throwable var6) {
                throw new RuntimeException("md5异常", var6);
            }
        }
    }

    public static String md5(boolean digital, Object... args) {
        if(args != null && args.length != 0) {
            String result = md5(BaseJsonUtils.writeValue(args));
            if(digital) {
                result = (new BigInteger(result, 16)).toString();
            }

            return result;
        } else {
            return "";
        }
    }

    public static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] base64DecodeToByte(String src) {
        return Base64.getDecoder().decode(src);
    }

    public static String base64Encode(String src) {
        if(StringUtils.isBlank(src)) {
            return src;
        } else {
            try {
                return Base64.getEncoder().encodeToString(src.getBytes("UTF-8"));
            } catch (Throwable var2) {
                throw new RuntimeException("base64 encode异常", var2);
            }
        }
    }

    public static String base64Decode(String src) {
        if(StringUtils.isBlank(src)) {
            return src;
        } else {
            try {
                return new String(Base64.getDecoder().decode(src), "UTF-8");
            } catch (Throwable var2) {
                throw new RuntimeException("base64 decode异常", var2);
            }
        }
    }

    public static String aesEncrypt(String src, String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "密钥不能为空");
        if(StringUtils.isBlank(src)) {
            return src;
        } else {
            try {
                KeyGenerator e = KeyGenerator.getInstance("AES");
                SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
                secureRandom.setSeed(key.getBytes(DEFAULT_CHARSET));
                e.init(128, secureRandom);
                SecretKey secretKey = e.generateKey();
                byte[] enCodeFormat = secretKey.getEncoded();
                SecretKeySpec keySpec = new SecretKeySpec(enCodeFormat, "AES");
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(1, keySpec);
                return byte2Hex(cipher.doFinal(src.getBytes("UTF-8")));
            } catch (Throwable var8) {
                throw new RuntimeException("aes加密异常", var8);
            }
        }
    }

    public static String aesEncrypt2(String src, String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "密钥不能为空");
        if(StringUtils.isBlank(src)) {
            return src;
        } else {
            try {
                KeyGenerator e = KeyGenerator.getInstance("AES");
                SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
                secureRandom.setSeed(key.getBytes(DEFAULT_CHARSET));
                e.init(128, secureRandom);
                SecretKey secretKey = e.generateKey();
                byte[] enCodeFormat = secretKey.getEncoded();
                SecretKeySpec keySpec = new SecretKeySpec(enCodeFormat, "AES");
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(1, keySpec);
                return byte2Hex2(cipher.doFinal(src.getBytes("UTF-8")));
            } catch (Throwable var8) {
                throw new RuntimeException("aes加密异常", var8);
            }
        }
    }

    public static String aesDecrypt(String src, String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "密钥不能为空");
        if(StringUtils.isBlank(src)) {
            return src;
        } else {
            try {
                KeyGenerator e = KeyGenerator.getInstance("AES");
                SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
                secureRandom.setSeed(key.getBytes(DEFAULT_CHARSET));
                e.init(128, secureRandom);
                SecretKey secretKey = e.generateKey();
                byte[] enCodeFormat = secretKey.getEncoded();
                SecretKeySpec keySpec = new SecretKeySpec(enCodeFormat, "AES");
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(2, keySpec);
                return new String(cipher.doFinal(hex2Bytes(src)), DEFAULT_CHARSET);
            } catch (Throwable var8) {
                throw new RuntimeException("aes解密异常", var8);
            }
        }
    }

    public static String aesDecrypt2(String src, String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "密钥不能为空");
        if(StringUtils.isBlank(src)) {
            return src;
        } else {
            try {
                KeyGenerator e = KeyGenerator.getInstance("AES");
                SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
                secureRandom.setSeed(key.getBytes(DEFAULT_CHARSET));
                e.init(128, secureRandom);
                SecretKey secretKey = e.generateKey();
                byte[] enCodeFormat = secretKey.getEncoded();
                SecretKeySpec keySpec = new SecretKeySpec(enCodeFormat, "AES");
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(2, keySpec);
                return new String(cipher.doFinal(hex2Bytes2(src)), DEFAULT_CHARSET);
            } catch (Throwable var8) {
                throw new RuntimeException("aes解密异常", var8);
            }
        }
    }

    public static String rsaEncryptByPrivateKey(String src, String key) {
        try {
            Key e = (Key)rsaPrivateKeyCache.get(key);
            return rsaEncrypt(src, e);
        } catch (Exception var3) {
            throw new RuntimeException("RSA加密异常", var3);
        }
    }

    public static String rsaDecryptByPublicKey(String src, String key) {
        try {
            Key e = (Key)rsaPublicKeyCache.get(key);
            return rsaDecrypt(src, e);
        } catch (Exception var3) {
            throw new RuntimeException("RSA解密异常", var3);
        }
    }

    public static String rsaEncryptByPublicKey(String src, String key) {
        try {
            return rsaEncrypt(src, (Key)rsaPublicKeyCache.get(key));
        } catch (Exception var3) {
            throw new RuntimeException("RSA加密异常", var3);
        }
    }

    public static String rsaDecryptByPrivateKey(String src, String key) {
        try {
            return rsaDecrypt(src, (Key)rsaPrivateKeyCache.get(key));
        } catch (Exception var3) {
            throw new RuntimeException("RSA解密异常", var3);
        }
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder hexRetSB = new StringBuilder();
        byte[] var2 = bytes;
        int var3 = bytes.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            String hexString = Integer.toHexString(255 & b);
            hexRetSB.append(hexString.length() == 1?Integer.valueOf(0):"").append(hexString);
        }

        return hexRetSB.toString();
    }

    private static String byte2Hex2(byte[] bytes) {
        return base64Encode(bytes);
    }

    private static byte[] hex2Bytes(String src) {
        byte[] sourceBytes = new byte[src.length() / 2];

        for(int i = 0; i < sourceBytes.length; ++i) {
            sourceBytes[i] = (byte)Integer.parseInt(src.substring(i * 2, i * 2 + 2), 16);
        }

        return sourceBytes;
    }

    private static byte[] hex2Bytes2(String src) {
        return base64DecodeToByte(src);
    }

    private static String rsaEncrypt(String src, Key key) {
        try {
            Cipher e = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
            e.init(1, key);
            byte[] data = src.getBytes("UTF-8");
            int blocks = data.length / 117;
            int lastBlockSize = data.length % 117;
            byte[] encryptedData = new byte[(lastBlockSize == 0?blocks:blocks + 1) * 128];

            for(int i = 0; i < blocks; ++i) {
                e.doFinal(data, i * 117, 117, encryptedData, i * 128);
            }

            if(lastBlockSize != 0) {
                e.doFinal(data, blocks * 117, lastBlockSize, encryptedData, blocks * 128);
            }

            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception var8) {
            throw new RuntimeException("RSA加密异常", var8);
        }
    }

    private static String rsaDecrypt(String src, Key key) {
        try {
            Cipher e = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
            e.init(2, key);
            byte[] decoded = Base64.getDecoder().decode(src);
            int blocks = decoded.length / 128;
            ByteArrayOutputStream decodedStream = new ByteArrayOutputStream(decoded.length);

            for(int i = 0; i < blocks; ++i) {
                decodedStream.write(e.doFinal(decoded, i * 128, 128));
            }

            return new String(decodedStream.toByteArray(), "UTF-8");
        } catch (Exception var7) {
            throw new RuntimeException("RSA解密异常", var7);
        }
    }
}
