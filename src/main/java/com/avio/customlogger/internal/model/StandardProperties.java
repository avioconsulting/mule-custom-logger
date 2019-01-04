package com.avio.customlogger.internal.model;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;


public class StandardProperties {

    @Parameter
    @DisplayName("Timestamp")
    @Summary("Timestamp")
    @Example("#[now()]")
    private String timestamp;

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


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

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