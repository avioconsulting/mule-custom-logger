package com.avioconsulting.mule.logger.api.processor;

import com.avioconsulting.mule.logger.internal.utils.CustomLoggerConstants;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

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

  public String getStatusCode() {
    return statusCode;
  }

  public String getType() {
    return type;
  }

  public String getDetail() {
    return detail;
  }

  public ExceptionProperties setStatusCode(String statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  public ExceptionProperties setType(String type) {
    this.type = type;
    return this;
  }

  public ExceptionProperties setDetail(String detail) {
    this.detail = detail;
    return this;
  }
}
