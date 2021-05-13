package com.avio.customlogger.model;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import java.util.Map;

public class ExtendedProperties {

    @Parameter
    @Optional
    @DisplayName("Properties")
    private Map<String, String> properties;

    public Map<String, String> getProperties() {
        return properties;
    }

}
