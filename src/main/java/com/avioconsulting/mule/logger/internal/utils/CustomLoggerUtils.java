package com.avioconsulting.mule.logger.internal.utils;

import static com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration.DEFAULT_CATEGORY;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.expression.ExpressionRuntimeException;
import org.slf4j.LoggerFactory;

public class CustomLoggerUtils {
  private CustomLoggerUtils() {
  }

  private static final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerUtils.class);

  public static Logger initLogger(String globalCategory, String category, String categorySuffix) {
    String c = DEFAULT_CATEGORY;
    if (StringUtils.isNotBlank(globalCategory)) {
      c = trimCategory(globalCategory);
    }

    if (StringUtils.isNotBlank(categorySuffix)) {
      c = c + "." + trimCategory(categorySuffix);
    }

    if (StringUtils.isNotBlank(category)) {
      c = trimCategory(category);
    }

    return LogManager.getLogger(c);
  }

  private static String trimCategory(String category) {
    if (category == null || category.equals("")) {
      return category;
    }
    while (category.startsWith(".")) {
      category = category.substring(1);
    }

    while (category.endsWith(".")) {
      category = category.substring(0, category.length() - 1);
    }
    return category;
  }

  public static Map<String, String> getGlobalConfigAttributes(Registry registry, String prefix, String name) {
    Map<String, String> globalConfigurationProperties = null;
    String configName = prefix + ":" + name;

    Optional<ConfigurationComponentLocator> configurationComponentLocator = registry
        .lookupByType(ConfigurationComponentLocator.class);
    if (configurationComponentLocator.isPresent()) {
      List<Component> globalConfigurations = configurationComponentLocator.get()
          .find(ComponentIdentifier.builder().namespace(prefix).name(name).build());
      if (globalConfigurations.size() == 1) {
        try {
          classLogger.debug("Located {} global element, attempting to retrieve properties.", configName);
          globalConfigurationProperties = (Map<String, String>) globalConfigurations.get(0)
              .getAnnotation(QName.valueOf("{config}componentParameters"));
        } catch (Exception e) {
          classLogger.error("Could not retrieve properties from {} global element.", configName);
        }
      } else if (globalConfigurations.size() > 1) {
        classLogger.debug("Found more than one {}, using the last loaded configuration.", configName);
        globalConfigurationProperties = (Map<String, String>) globalConfigurations
            .get(globalConfigurations.size() - 1)
            .getAnnotation(QName.valueOf("{config}componentParameters"));
      } else {
        classLogger.error("Unable to find {} global configuration to use.  Please add an {} global configuration to your application.", configName);
        throw new NoSuchElementException(
            "Unable to find " + configName + " global configuration to use.  Please add an " + configName
                + " global configuration to your application.");
      }
    } else {
      classLogger.error("Unable to find ConfigurationComponentLocator");
      throw new NoSuchElementException("Unable to find ConfigurationComponentLocator");
    }

    return globalConfigurationProperties;
  }

  public static String retrieveValueFromGlobalConfig(ExpressionManager expressionManager,
      Map<String, String> globalConfigAttributes, String attributeName) throws ExpressionRuntimeException {
    if (globalConfigAttributes != null) {
      return safeEvaluate(expressionManager, globalConfigAttributes.get(attributeName));
    } else {
      return null;
    }
  }

  private static String safeEvaluate(ExpressionManager expressionManager, String expression)
      throws ExpressionRuntimeException {
    if (expression == null) {
      return "null";
    }

    if (expression.startsWith("#[")) {
      try {
        Object value = expressionManager.evaluate(expression).getValue();
        classLogger.debug("{} evaluated to: {}" ,expression,value);
        return String.valueOf(value);
      } catch (ExpressionRuntimeException error) {
        classLogger.error("There was an error evaluating the following expression: {}", expression);
        throw error;
      }
    } else {
      return expression;
    }
  }

}
