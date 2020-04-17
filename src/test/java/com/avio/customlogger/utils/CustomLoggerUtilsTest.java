package com.avio.customlogger.utils;

import com.avio.customlogger.CustomLoggerTimerScopeOperations;
import org.junit.Assert;
import org.junit.Test;

import static com.avio.customlogger.utils.CustomLoggerConstants.DEFAULT_CATEGORY_PREFIX;

public class CustomLoggerUtilsTest {

    @Test
    public void initLogger() {
        // If the default prefix is still set and the default category suffix is still set, concatenate
        String logger = CustomLoggerUtils.initLogger(DEFAULT_CATEGORY_PREFIX, CustomLoggerTimerScopeOperations.DEFAULT_CATEGORY_SUFFIX).getName();
        Assert.assertEquals(DEFAULT_CATEGORY_PREFIX + CustomLoggerTimerScopeOperations.DEFAULT_CATEGORY_SUFFIX, logger);

        // If the default prefix is still set and a custom category suffix is defined, concatenate
        logger = CustomLoggerUtils.initLogger(DEFAULT_CATEGORY_PREFIX, ".customcategory").getName();
        Assert.assertEquals(DEFAULT_CATEGORY_PREFIX + ".customcategory", logger);

        // If there's a custom prefix but a custom suffix, concatenate
        logger = CustomLoggerUtils.initLogger("com.mycompany", CustomLoggerTimerScopeOperations.DEFAULT_CATEGORY_SUFFIX).getName();
        Assert.assertEquals("com.mycompany" + CustomLoggerTimerScopeOperations.DEFAULT_CATEGORY_SUFFIX, logger);

        // If both are custom, concatenate
        logger = CustomLoggerUtils.initLogger("com.mycompany", ".customcategory").getName();
        Assert.assertEquals("com.mycompany.customcategory", logger);

        // If both are custom (but both have . at the ends), concatenate
        logger = CustomLoggerUtils.initLogger("com.mycompany.", ".customcategory").getName();
        Assert.assertEquals("com.mycompany.customcategory", logger);

        // If both are custom (but neither have . at the ends), concatenate
        logger = CustomLoggerUtils.initLogger("com.mycompany", "customcategory").getName();
        Assert.assertEquals("com.mycompany.customcategory", logger);

        // If default prefix is still set and a custom suffix is defined (BUT it does not start with a .), use the suffix
        logger = CustomLoggerUtils.initLogger(DEFAULT_CATEGORY_PREFIX, "com.mycompany.customcategory").getName();
        Assert.assertEquals("com.mycompany.customcategory", logger);

        // If both are custom but the suffix contains the prefix, use the suffix
        logger = CustomLoggerUtils.initLogger("com.mycompany", "com.mycompany.customcategory").getName();
        Assert.assertEquals("com.mycompany.customcategory", logger);
    }
}