package com.avio.customlogger.internal;

import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
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
    @Optional(defaultValue = "#[app.name]")
    private String app_name;
    @Parameter
    @DisplayName("App Version")
    @Summary("Version of the Mule Application")
    @Optional(defaultValue = "${appVersion}")
    private String app_version;
    @Parameter
    @DisplayName("Environment")
    @Summary("Mule Application Environment")
    @Optional(defaultValue = "${env}")
    private String env;

    public String getApp_name() {
        return app_name;
    }

    public String getApp_version() {
        return app_version;
    }

    public String getEnv() {
        return env;
    }

}