package com.ruoyi.web.core.sheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import com.ruoyi.common.core.exception.ServiceException;

class SheetJobsTest
{
    private static final String BUSY = "\u5f53\u524d\u540c\u65f6\u5904\u7406\u7684\u8868\u683c\u592a\u591a\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5";

    @Test
    void thirdEnterFailsWhileTwoAreOpen() throws Exception
    {
        SheetJobs.Job first = SheetJobs.enter();
        SheetJobs.Job second = SheetJobs.enter();
        try
        {
            ServiceException ex = assertThrows(ServiceException.class, SheetJobs::enter);
            assertEquals(BUSY, ex.getMessage());
            first.close();
            SheetJobs.Job third = SheetJobs.enter();
            third.close();
        }
        finally
        {
            first.close();
            second.close();
        }
    }
}
