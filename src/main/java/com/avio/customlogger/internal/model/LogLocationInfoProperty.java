package com.avio.customlogger.internal.model;

import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

public class LogLocationInfoProperty {

    @Parameter
    @DisplayName("Log Location Info")
    public boolean logLocationInfo;
}
