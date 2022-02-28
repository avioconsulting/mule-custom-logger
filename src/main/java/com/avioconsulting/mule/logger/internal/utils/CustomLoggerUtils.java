package com.avioconsulting.mule.logger.internal.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.expression.ExpressionRuntimeException;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration.DEFAULT_CATEGORY;

public class CustomLoggerUtils {

    private static final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerUtils.class);
    private final ExpressionManager expressionManager = null;

    public static Logger initLogger(String globalCategory, String category, String categorySuffix) {
        String c = DEFAULT_CATEGORY;
        if (globalCategory != null && globalCategory != "") {
            c = trimCategory(globalCategory);
        }

        if (categorySuffix != null && categorySuffix != "") {
            c = c + "." + trimCategory(categorySuffix);
        }

        if (category != null && category != "") {
            c = trimCategory(category);
        }

        return LogManager.getLogger(c);
    }

    private static String trimCategory(String category) {
        if (category == null || category == "") {
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

        Optional<ConfigurationComponentLocator> configurationComponentLocator = registry.lookupByType(ConfigurationComponentLocator.class);
        if (configurationComponentLocator.isPresent()) {
            List<Component> globalConfigurations = configurationComponentLocator.get().find(ComponentIdentifier.builder().namespace(prefix).name(name).build());
            if (globalConfigurations.size() == 1) {
                try {
                    classLogger.debug("Located " + configName + " global element, attempting to retrieve properties.");
                    globalConfigurationProperties = (Map<String, String>) globalConfigurations.get(0).getAnnotation(QName.valueOf("{config}componentParameters"));
                } catch (Exception e) {
                    classLogger.error("Could not retrieve properties from " + configName + " global element.");
                }
            } else if (globalConfigurations.size() > 1) {
                classLogger.debug("Found more than one " + configName + ", using the last loaded configuration.");
                globalConfigurationProperties = (Map<String, String>) globalConfigurations.get(globalConfigurations.size() - 1).getAnnotation(QName.valueOf("{config}componentParameters"));
            } else {
                classLogger.error("Unable to find " + configName + " global configuration to use.  Please add an " + configName + " global configuration to your application.");
                throw new NoSuchElementException("Unable to find " + configName + " global configuration to use.  Please add an " + configName + " global configuration to your application.");
            }
        } else {
            classLogger.error("Unable to find ConfigurationComponentLocator");
            throw new NoSuchElementException("Unable to find ConfigurationComponentLocator");
        }

        return globalConfigurationProperties;
    }

    public static String retrieveValueFromGlobalConfig(ExpressionManager expressionManager, Map<String, String> globalConfigAttributes, String attributeName) throws ExpressionRuntimeException {
        if (globalConfigAttributes != null) {
            return safeEvaluate(expressionManager, globalConfigAttributes.get(attributeName));
        } else {
            return null;
        }
    }

    public static ExpressionManager getExpressionManager(Registry registry) {
        Optional<ExpressionManager> optionalExpressionManager = registry.lookupByType(ExpressionManager.class);
        if (optionalExpressionManager.isPresent()) {
            classLogger.debug("ExpressionManager was found");
            return optionalExpressionManager.get();
        } else {
            classLogger.error("ExpressionManager was not found.");
            throw new NoSuchElementException("ExpressionManager was not found.");
        }
    }

    private static String safeEvaluate(ExpressionManager expressionManager, String expression) throws ExpressionRuntimeException {
        if (expression == null) {
            return "null";
        }

        if(expression.startsWith("#[")) {
            try {
                Object value = expressionManager.evaluate(expression).getValue();
                classLogger.debug(expression + " evaluated to: " + value);
                return String.valueOf(value);
            } catch (ExpressionRuntimeException error) {
                classLogger.error("There was an error evaluating the following expression: " + expression);
                throw error;
            }
        } else {
            return expression;
        }
    }

}