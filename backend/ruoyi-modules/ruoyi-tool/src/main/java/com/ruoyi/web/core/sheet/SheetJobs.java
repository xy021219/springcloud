package com.ruoyi.web.core.sheet;

import java.util.concurrent.Semaphore;
import com.ruoyi.common.core.exception.ServiceException;

public final class SheetJobs
{
    static final int LIMIT = 2;

    private static final Semaphore PERMITS = new Semaphore(LIMIT);

    private static final String BUSY = "\u5f53\u524d\u540c\u65f6\u5904\u7406\u7684\u8868\u683c\u592a\u591a\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5";

    private SheetJobs()
    {
    }

    public static Job enter()
    {
        if (!PERMITS.tryAcquire())
        {
            throw new ServiceException(BUSY);
        }
        return new Job();
    }

    public static final class Job implements AutoCloseable
    {
        private boolean open = true;

        @Override
        public void close()
        {
            if (open)
            {
                open = false;
                PERMITS.release();
            }
        }
    }
}
