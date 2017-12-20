//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.songxm.commons;

import com.auth0.jwt.JWTExpiredException;
import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.google.common.base.Preconditions;
import com.songxm.commons.exception.TokenExpiredException;
import com.songxm.commons.model.JwtToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@SuppressWarnings("unchecked")
public class BaseJwtUtils {
    private static final Logger log = LoggerFactory.getLogger(BaseJwtUtils.class);
    public static final String ISSUE_AT = "iat";
    public static final String EXPIRE_AT = "exp";
    public static final String CONTENT = "content";

    public BaseJwtUtils() {
    }

    public static JwtToken getToken(Object content, long expireInSecond, String secret) {
        return getToken(content, new Date(), expireInSecond, secret);
    }

    public static JwtToken getToken(Object content, Date validTime, long expireInSecond, String secret) {
        Preconditions.checkArgument(content != null, "加密内容不能为空");
        Preconditions.checkArgument(expireInSecond > 0L, "token有效时间必须为正整数,实际值[" + expireInSecond + "]");
        Preconditions.checkArgument(StringUtils.isNotBlank(secret), "jwt密钥不能为空");
        long iat = validTime == null?System.currentTimeMillis() / 1000L:validTime.getTime() / 1000L;
        long exp = iat + expireInSecond;
        HashMap claims = new HashMap();
        claims.put("iat", Long.valueOf(iat));
        claims.put("exp", Long.valueOf(exp));
        claims.put("content", content);
        JWTSigner signer = new JWTSigner(secret);
        String token = signer.sign(claims);
        JwtToken jwtToken = new JwtToken();
        jwtToken.setToken(token);
        jwtToken.setExpireAt(Long.valueOf(exp * 1000L));
        return jwtToken;
    }

    public static <T> T verifyToken(String jwtToken, String secret, Class<T> resultCls) {
        jwtToken = BaseStringUtils.substringAfter(jwtToken, " ").trim();
        Preconditions.checkArgument(StringUtils.isNotBlank(jwtToken), "token不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(secret), "jwt密钥不能为空");
        Preconditions.checkArgument(resultCls != null, "jwt内容类型不能为空");

        try {
            JWTVerifier e = new JWTVerifier(secret);
            Map claims = e.verify(jwtToken);
            return resultCls == Map.class?(T)claims.get("content"):(T)BaseJsonUtils.readValue((Map)claims.get("content"), resultCls);
        } catch (Throwable var5) {
            if(var5 instanceof JWTExpiredException) {
                throw new TokenExpiredException();
            } else {
                log.error("token[{}]校验异常: {}", jwtToken, ExceptionUtils.getStackTrace(var5));
                return null;
            }
        }
    }

    public static Map<String, Object> getTokenInfo(String jwtToken) {
        Preconditions.checkArgument(StringUtils.isNotBlank(jwtToken), "token不能为空");
        jwtToken = jwtToken.trim();
        jwtToken = BaseStringUtils.substringAfter(jwtToken, " ").trim();
        Preconditions.checkArgument(StringUtils.isNotBlank(jwtToken), "token不能为空");

        try {
            String e = StringUtils.substringBetween(jwtToken, ".", ".");
            return (Map)BaseJsonUtils.readValue(BaseSecurityUtils.base64Decode(e), Map.class);
        } catch (Throwable var2) {
            log.error("token[{}]格式不正确", jwtToken);
            return null;
        }
    }
}
