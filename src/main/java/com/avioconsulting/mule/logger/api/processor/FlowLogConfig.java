package com.avioconsulting.mule.logger.api.processor;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

@Alias("flow-log-config")
public class FlowLogConfig {

  @Parameter
  @Summary("Name of the flow to associate given expression as attributes")
  private String flowName;

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

  public String getFlowName() {
    return flowName;
  }

  public FlowLogConfig setFlowName(String flowName) {
    this.flowName = flowName;
    return this;
  }

}
