package com.ruoyi.gateway.seckill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import com.ruoyi.common.core.exception.ServiceException;

class SeckillSignatureTest
{
    @Test
    void wrongSignIsRejected()
    {
        long now = 1_700_000_000_000L;
        ServiceException ex = assertThrows(ServiceException.class,
                () -> SeckillSignature.verify("req", "1", "1", now, "deadbeef", "secret", now));
        assertEquals(SeckillSignature.BAD, ex.getMessage());
    }

    @Test
    void knownSignIsAccepted()
    {
        long now = 1_700_000_000_000L;
        String sign = "d50d0c151a3a07c8bf6705e35f1ce25a176c051e448d832695d753570414b1f0";
        assertEquals(sign, SeckillSignature.sign("req", "1", "1", now, "secret"));
        SeckillSignature.verify("req", "1", "1", now, sign, "secret", now);
    }

    @Test
    void oldTimestampIsExpired()
    {
        long now = 1_700_000_000_000L;
        ServiceException ex = assertThrows(ServiceException.class,
                () -> SeckillSignature.verify("req", "1", "1", now - 61_000L, "abc", "secret", now));
        assertEquals(SeckillSignature.EXPIRED, ex.getMessage());
    }
}
