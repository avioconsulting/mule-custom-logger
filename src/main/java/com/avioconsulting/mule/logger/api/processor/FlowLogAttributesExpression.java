package com.avioconsulting.mule.logger.api.processor;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.util.Objects;

@Alias("flow-attributes-expression")
public class FlowLogAttributesExpression {

  @Parameter
  @Summary("Name of the flow to associate given expression as attributes")
  private String flowName;
  @Parameter
  @Summary("A valid dataweave expression that resolves to a Map object with key-value pairs")
  private String expressionText;

  public String getFlowName() {
    return flowName;
  }

  public FlowLogAttributesExpression setFlowName(String flowName) {
    this.flowName = flowName;
    return this;
  }

  public String getExpressionText() {
    return expressionText;
  }

  public FlowLogAttributesExpression setExpressionText(String expressionText) {
    this.expressionText = expressionText;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    FlowLogAttributesExpression that = (FlowLogAttributesExpression) o;
    return Objects.equals(getFlowName(), that.getFlowName())
        && Objects.equals(getExpressionText(), that.getExpressionText());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFlowName(), getExpressionText());
  }

  @Override
  public String toString() {
    return "FlowLogAttributesExpression{" +
        "flowName='" + flowName + '\'' +
        ", attributeExpression='" + expressionText + '\'' +
        '}';
  }
}
