package com.avio.customlogger.model;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

public class LogLocationInfoProperty {

    @Parameter
    @DisplayName("Log Location Info")
    @Optional(defaultValue = "False")
    public boolean logLocationInfo;
}
