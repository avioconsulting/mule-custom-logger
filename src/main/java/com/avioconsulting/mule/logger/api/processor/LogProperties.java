package com.avioconsulting.mule.logger.api.processor;

import lombok.Getter;
import lombok.Setter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;

/**
 * This class contains the configuration fields that user will fill to set up a AVIO's logger component instance
 */
@Getter
@Setter
public class LogProperties {

  @Parameter
  @Optional
  @DisplayName("Correlation ID")
  @Summary("Correlation UUID. Defaults to current event's correlation id.")
  @Example("#[correlationId]")
  private String correlationId;

  @Parameter
  @Optional
  @DisplayName("Message")
  @Summary("Message to be logged")
  private String message;

  @Parameter
  @Optional
  @DisplayName("Payload")
  @Summary("Payload to be logged")
  @Example("#[write(payload, \"application/json\")]")
  private ParameterResolver<String> payload;

  @Parameter
  @DisplayName("Level")
  @Optional(defaultValue = "INFO")
  private LogLevel level;

  @Parameter
  @Optional
  @DisplayName("Category")
  @Summary("Override the globally configured log category")
  private String category;

  @Parameter
  @Optional
  @DisplayName("Category Suffix")
  @Summary("Append this suffix to the globally configured log category")
  private String categorySuffix;

  public enum LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL
  }

}
