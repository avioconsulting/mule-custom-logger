package com.avioconsulting.mule.logger.api.processor;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

@Alias("flow-logs-config")
public class FlowLogConfig {

  @Parameter
  @Summary("Name of the flow to associate given expression as attributes")
  private String flowName;

  @ParameterGroup(name = "Flow Attributes")
  private ExpressionText expressionText;

  public String getFlowName() {
    return flowName;
  }

  public FlowLogConfig setFlowName(String flowName) {
    this.flowName = flowName;
    return this;
  }

  public ExpressionText getExpressionText() {
    return expressionText;
  }

  public void setExpressionText(ExpressionText expressionText) {
    this.expressionText = expressionText;
  }
}
