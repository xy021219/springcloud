package com.ruoyi.gateway.seckill;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.ruoyi.common.core.exception.ServiceException;

public final class SeckillSignature
{
    public static final String BAD = "\u7b7e\u540d\u4e0d\u6b63\u786e";

    public static final String EXPIRED = "\u8bf7\u6c42\u5df2\u8fc7\u671f";

    public static final long WINDOW_MILLIS = 60_000L;

    private SeckillSignature()
    {
    }

    public static String sign(String requestId, String activityId, String userId, long timestampMillis, String secret)
    {
        String payload = requestId + "\n" + activityId + "\n" + userId + "\n" + timestampMillis;
        return hmacHex(secret, payload);
    }

    public static void verify(String requestId, String activityId, String userId, long timestampMillis, String sign,
            String secret, long nowMillis)
    {
        if (blank(requestId) || blank(activityId) || blank(userId) || blank(sign) || blank(secret))
        {
            throw new ServiceException(BAD);
        }
        if (Math.abs(nowMillis - timestampMillis) > WINDOW_MILLIS)
        {
            throw new ServiceException(EXPIRED);
        }
        String expected = sign(requestId, activityId, userId, timestampMillis, secret);
        byte[] actual = sign.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
        byte[] wanted = expected.getBytes(StandardCharsets.US_ASCII);
        if (!MessageDigest.isEqual(wanted, actual))
        {
            throw new ServiceException(BAD);
        }
    }

    private static boolean blank(String value)
    {
        return value == null || value.isEmpty();
    }

    private static String hmacHex(String secret, String payload)
    {
        try
        {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(raw.length * 2);
            for (byte value : raw)
            {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        }
        catch (GeneralSecurityException ex)
        {
            throw new ServiceException(BAD);
        }
    }
}
