package com.avioconsulting.mule.logger.api.processor;

import com.avioconsulting.mule.logger.internal.utils.CustomLoggerConstants;
import lombok.Getter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * Class to represent AVIO's Custom Logger Exception properties fields
 * for Parameters Group
 */
@Getter
public class ExceptionProperties {

  @Parameter
  @DisplayName("Status Code")
  @Summary("Exception Status Code")
  @Optional
  private String statusCode;

  @Parameter
  @DisplayName("Type")
  @Summary("Type of Exception")
  @Optional(defaultValue = CustomLoggerConstants.DEFAULT_EXCEPTION_TYPE)
  private String type;

  @Parameter
  @DisplayName("Detail")
  @Summary("Exception Detail")
  @Optional(defaultValue = CustomLoggerConstants.DEFAULT_EXCEPTION_DETAIL)
  private String detail;

}
