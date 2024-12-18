package com.avioconsulting.mule.logger.api.processor;

import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class ExpressionText {

  @Parameter
  @Optional
  @Summary("A valid dataweave expression that resolves to a Map object with key-value pairs")
  private String attributesExpressionText;

  @Parameter
  @Optional
  @Summary("A valid dataweave expression that results in a String to append to default flow start message")
  private String messageExpressionText;

  public String getAttributesExpressionText() {
    return attributesExpressionText;
  }

  public void setAttributesExpressionText(String attributesExpressionText) {
    this.attributesExpressionText = attributesExpressionText;
  }

  public String getMessageExpressionText() {
    return messageExpressionText;
  }

  public void setMessageExpressionText(String messageExpressionText) {
    this.messageExpressionText = messageExpressionText;
  }
}
