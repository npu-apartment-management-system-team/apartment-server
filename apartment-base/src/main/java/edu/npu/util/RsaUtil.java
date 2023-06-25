package edu.npu.util;

import cn.hutool.crypto.CryptoException;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;

/**
 * @author : [wangminan]
 * @description : [加密与解密]
 */
public class RsaUtil {

    private RsaUtil() {
    }

    public static String decrypt(String privateKey, String encryptPassword) {

        try {
            RSA rsa = new RSA(privateKey, null);
            return new String(rsa.decrypt(encryptPassword, KeyType.PrivateKey));
        } catch (CryptoException e) {
            throw new ApartmentException(ApartmentError.PARAMS_ERROR, "密码无法用RSA解密");
        }
    }
}
