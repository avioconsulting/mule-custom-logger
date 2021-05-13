package com.avio.customlogger;

import com.avio.customlogger.utils.CustomLoggerConstants;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

//import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations({CustomLoggerOperation.class, CustomLoggerTimerScopeOperations.class})
public class CustomLoggerConfiguration {

    @Parameter
    @DisplayName("App Name")
    @Summary("Name of the Mule Application")
    @Optional(defaultValue = CustomLoggerConstants.DEFAULT_APP_NAME)
    private String app_name;
    @Parameter
    @DisplayName("App Version")
    @Summary("Version of the Mule Application")
    @Example(CustomLoggerConstants.EXAMPLE_APP_VERSION)
    private String app_version;
    @Parameter
    @DisplayName("Environment")
    @Summary("Mule Application Environment")
    @Optional(defaultValue = CustomLoggerConstants.DEFAULT_ENV)
    private String env;
    @Parameter
    @DisplayName("Category Prefix")
    @Summary("A string which will be prefixed to all log category suffixes defined in the loggers")
    @Optional(defaultValue = CustomLoggerConstants.DEFAULT_CATEGORY_PREFIX)
    private String category_prefix;

    public String getApp_name() {
        return app_name;
    }

    public String getApp_version() {
        return app_version;
    }

    public String getEnv() {
        return env;
    }

    public String getCategory_prefix() {
        return category_prefix;
    }
}