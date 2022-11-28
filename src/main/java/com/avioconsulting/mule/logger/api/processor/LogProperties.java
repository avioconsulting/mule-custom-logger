package com.avioconsulting.mule.logger.api.processor;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;

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

  private String compressedPayload;

  private String encryptedPayload;

  public String getEncryptedPayload() {
    return encryptedPayload;
  }

  public String getCompressedPayload() {
    return compressedPayload;
  }

  public void setCompressedPayload(String compressedPayload) {
    this.compressedPayload = compressedPayload;
  }

  public void setEncryptedPayload(String encryptedPayload) {
    this.encryptedPayload = encryptedPayload;
  }

  public enum LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL
  }

  public String getMessage() {
    return message;
  }

  public LogLevel getLevel() {
    return level;
  }

  public String getCategory() {
    return category;
  }

  public String getCategorySuffix() {
    return categorySuffix;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public ParameterResolver<String> getPayload() {
    return payload;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setPayload(ParameterResolver<String> payload) {
    this.payload = payload;
  }

  public void setLevel(LogLevel level) {
    this.level = level;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public void setCategorySuffix(String categorySuffix) {
    this.categorySuffix = categorySuffix;
  }

  // public void setMessageAttributes(List<MessageAttribute> messageAttributes) {
  // this.messageAttributes = messageAttributes;
  // }
}
