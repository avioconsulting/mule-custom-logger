package com.avioconsulting.mule.logger.utils;

import com.avioconsulting.mule.logger.internal.CustomLoggerTimerScopeOperations;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import com.avioconsulting.mule.logger.internal.utils.CustomLoggerUtils;
import org.junit.Assert;
import org.junit.Test;

public class CustomLoggerUtilsTest {

    @Test
    public void initLogger() {

        // Default category with no suffix
        String logger = CustomLoggerUtils.initLogger(CustomLoggerConfiguration.DEFAULT_CATEGORY, "", "").getName();
        Assert.assertEquals(CustomLoggerConfiguration.DEFAULT_CATEGORY, logger);

        // Default category and suffix with no leading or trailing periods
        logger = CustomLoggerUtils.initLogger(CustomLoggerConfiguration.DEFAULT_CATEGORY, "", CustomLoggerTimerScopeOperations.DEFAULT_CATEGORY_SUFFIX).getName();
        Assert.assertEquals(CustomLoggerConfiguration.DEFAULT_CATEGORY + "." + CustomLoggerTimerScopeOperations.DEFAULT_CATEGORY_SUFFIX, logger);

        // Default category and suffix with both leading and trailing periods to trim
        logger = CustomLoggerUtils.initLogger(CustomLoggerConfiguration.DEFAULT_CATEGORY + ".", "", "." + CustomLoggerTimerScopeOperations.DEFAULT_CATEGORY_SUFFIX).getName();
        Assert.assertEquals(CustomLoggerConfiguration.DEFAULT_CATEGORY + "." + CustomLoggerTimerScopeOperations.DEFAULT_CATEGORY_SUFFIX, logger);
        logger = CustomLoggerUtils.initLogger(".." + CustomLoggerConfiguration.DEFAULT_CATEGORY + "..", "", ".." + CustomLoggerTimerScopeOperations.DEFAULT_CATEGORY_SUFFIX + ".").getName();
        Assert.assertEquals(CustomLoggerConfiguration.DEFAULT_CATEGORY + "." + CustomLoggerTimerScopeOperations.DEFAULT_CATEGORY_SUFFIX, logger);

        // Default category with override category = override category
        logger = CustomLoggerUtils.initLogger(CustomLoggerConfiguration.DEFAULT_CATEGORY + ".", "com.test", "").getName();
        Assert.assertEquals("com.test", logger);

        // Default category with suffix and override category = override category
        logger = CustomLoggerUtils.initLogger(CustomLoggerConfiguration.DEFAULT_CATEGORY + ".", "com.test", "." + CustomLoggerTimerScopeOperations.DEFAULT_CATEGORY_SUFFIX).getName();
        Assert.assertEquals("com.test", logger);
    }
}