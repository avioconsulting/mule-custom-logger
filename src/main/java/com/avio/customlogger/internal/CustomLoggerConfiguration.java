package com.avio.customlogger.internal;

import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

//import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations(CustomLoggerOperation.class)
public class CustomLoggerConfiguration {

    @Parameter
    @DisplayName("App Name")
    @Summary("Name of the Mule Application")
    @Example("#[app.name]")
    private String app_name;
    @Parameter
    @DisplayName("App Version")
    @Summary("Version of the Mule Application")
    @Example("${pomVersion}")
    private String app_version;
    @Parameter
    @DisplayName("Environment")
    @Summary("Mule Application Environment")
    @Example("${env}")
    private String env;

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }
}