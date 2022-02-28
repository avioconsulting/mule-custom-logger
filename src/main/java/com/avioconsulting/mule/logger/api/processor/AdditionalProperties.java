package com.avioconsulting.mule.logger.api.processor;

import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

public class AdditionalProperties {

    @Parameter
    @DisplayName("Include Location Information")
    @Optional(defaultValue = "false")
    @Expression(ExpressionSupport.NOT_SUPPORTED)
    private boolean includeLocationInfo = false;

    public boolean isIncludeLocationInfo() {
        return includeLocationInfo;
    }

    public void setIncludeLocationInfo(boolean includeLocationInfo) {
        this.includeLocationInfo = includeLocationInfo;
    }

}
