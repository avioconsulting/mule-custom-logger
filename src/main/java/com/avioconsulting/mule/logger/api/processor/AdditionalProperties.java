package com.avioconsulting.mule.logger.api.processor;

import lombok.Getter;
import lombok.Setter;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

/**
 * Class to represent AVIO's Custom Logger Additional properties fields
 * for Parameters Group
 */
@Getter
@Setter
public class AdditionalProperties {

  @Parameter
  @DisplayName("Include Location Information")
  @Optional(defaultValue = "false")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private boolean includeLocationInfo;

}
