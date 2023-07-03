package edu.npu.common;

/**
 * @author : [wangminan]
 * @description : [Redis在module中的常量]
 */
public class RedisConstants {

    private RedisConstants(){
        throw new IllegalStateException("Utility class");
    }

    public static final String SMS_CODE_PREFIX = "code:sms:";

    public static final String MAIL_CODE_PREFIX = "code:mail:";

    public static final Long CODE_EXPIRE_TIME = 60 * 5L;

    public static final String LOGIN_ACCOUNT_KEY_PREFIX = "login:account:";

    public static final Long LOGIN_ACCOUNT_EXPIRE_TTL = 180000000L;

    public static final String HASH_TOKEN_KEY = "token";

    public static final String HASH_LOGIN_ACCOUNT_KEY = "loginAccount";

    public static final String CACHE_USER_KEY = "cache:user:";

    public static final Long CACHE_USER_TTL = 30L;

    public static final String LOCK_USER_KEY = "lock:user:";

    public static final String LOCK_PAYMENT_USER_KEY = "lock:paymentUser:";

    public static final String CACHE_APARTMENT_KEY = "cache:apartment:";

    public static final Long CACHE_APARTMENT_TTL = 30L;

    public static final String LOCK_APARTMENT_KEY = "lock:apartment:";

    public static final String LOGIN_LOCK_KEY = "login:lock";
}
