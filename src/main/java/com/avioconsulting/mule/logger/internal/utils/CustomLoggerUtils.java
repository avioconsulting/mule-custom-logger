package com.avioconsulting.mule.logger.internal.utils;

import com.avioconsulting.mule.logger.internal.CustomLoggerTimerScopeOperations;
import com.avioconsulting.mule.logger.internal.listeners.CustomLoggerNotificationListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.expression.ExpressionRuntimeException;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration.DEFAULT_CATEGORY_PREFIX;
import static com.avioconsulting.mule.logger.internal.utils.CustomLoggerConstants.DEFAULT_CATEGORY_SUFFIX;

public class CustomLoggerUtils {

    private Registry registry;

    private static final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerUtils.class);

    private ExpressionManager expressionManager = null;
    private Map<String, String> globalConfigurationProperties = null;

    public CustomLoggerUtils(Registry registry) {
        this(registry, null);
    }

    public CustomLoggerUtils(Registry registry, String globalConfigurationName) {
        this.registry = registry;
        if (registry != null) {
            if (globalConfigurationName != null && !"".equals(globalConfigurationName)) {
                classLogger.debug("Retrieving avio-core:config global element directly.");
                Optional<Object> config = registry.lookupByName(globalConfigurationName);
                if (config.isPresent()) {
                    try {
                        classLogger.debug("Retrieved the avio-core:config element directly.");
                        globalConfigurationProperties = (Map<String, String>) ((Component) config.get()).getAnnotation(QName.valueOf("{config}componentParameters"));
                    } catch (ClassCastException exception) {
                        classLogger.error("There was an error when retrieving the global configuration's properties.");
                    }
                } else {
                    classLogger.debug("Could not retrieve the avio-core:config element directly.");
                }
            }
            if (globalConfigurationProperties == null) {
                classLogger.debug("Locating avio-core:config global element from the registry.");
                Optional<ConfigurationComponentLocator> configurationComponentLocator = registry.lookupByType(ConfigurationComponentLocator.class);
                if (configurationComponentLocator.isPresent()) {
                    List<Component> globalConfigurations = configurationComponentLocator.get().find(ComponentIdentifier.builder().namespace("avio-core").name("config").build());
                    if (globalConfigurations.size() == 1) {
                        try {
                            classLogger.debug("Located avio-core:config global element, attempting to retrieve properties.");
                            globalConfigurationProperties = (Map<String, String>) globalConfigurations.get(0).getAnnotation(QName.valueOf("{config}componentParameters"));
                        } catch (Exception e) {
                            classLogger.error("Could not retrieve properties from avio-core:config global element.");
                        }
                    } else if (globalConfigurations.size() > 1) {
                        classLogger.debug("Found more than one avio-core:config, using the last loaded configuration.");
                        globalConfigurationProperties = (Map<String, String>) globalConfigurations.get(globalConfigurations.size()-1).getAnnotation(QName.valueOf("{config}componentParameters"));
                    }
                }
            }
            if (globalConfigurationProperties != null) {
                classLogger.debug("Successfully retrieved properties from avio-core:config global element.");
            } else {
                classLogger.debug("Could not retrieve properties from avio-core:config global element.");
            }
        }
    }

    public static Logger initLogger(String categoryPrefix, String categorySuffix) {
        if (categoryPrefix == null || categoryPrefix.length() == 0) {
            categoryPrefix = DEFAULT_CATEGORY_PREFIX;
        }
        if (categorySuffix == null || categorySuffix.length() == 0) {
            categorySuffix = DEFAULT_CATEGORY_SUFFIX;
        }
        if (categorySuffix.contains(categoryPrefix) ||
                (DEFAULT_CATEGORY_PREFIX.equals(categoryPrefix) &&
                        !(categorySuffix.charAt(0) == '.' || !categorySuffix.contains(".")) &&
                        !(DEFAULT_CATEGORY_SUFFIX.equals(categorySuffix) ||
                                CustomLoggerTimerScopeOperations.DEFAULT_CATEGORY_SUFFIX.equals(categorySuffix) ||
                                CustomLoggerNotificationListener.DEFAULT_CATEGORY_SUFFIX.equals(categorySuffix)))
        ) {
            return LogManager.getLogger(categorySuffix);
        }
        if (categoryPrefix.charAt(categoryPrefix.length() - 1) == '.') {
            if (categorySuffix.charAt(0) == '.') {
                return LogManager.getLogger(categoryPrefix + categorySuffix.substring(1));
            } else {
                return LogManager.getLogger(categoryPrefix + categorySuffix);
            }
        } else {
            if (categorySuffix.charAt(0) == '.') {
                return LogManager.getLogger(categoryPrefix + categorySuffix);
            } else {
                return LogManager.getLogger(categoryPrefix + "." + categorySuffix);
            }
        }
    }

    private String safeEvaluate(String expression) {
        if (expression == null) {
            return "null";
        }
        if (expressionManager == null) {
            classLogger.debug("ExpressionManager has not been set, attempting to retrieve");
            if (registry == null) {
                classLogger.debug("Registry is null, skipping evaluation");
            } else {
                Optional<ExpressionManager> optionalExpressionManager = registry.lookupByType(ExpressionManager.class);
                if (optionalExpressionManager.isPresent()) {
                    classLogger.debug("ExpressionManager was found");
                    expressionManager = optionalExpressionManager.get();
                } else {
                    classLogger.debug("ExpressionManager was not found.");
                }
            }
        }
        if (expressionManager != null &&
                expression.charAt(0) == '#' &&
                expression.charAt(1) == '[' &&
                expression.charAt(expression.length() - 1) == ']') {
            try {
                Object value = expressionManager.evaluate(expression).getValue();
                classLogger.debug(expression + " evaluated to: " + value);
                return String.valueOf(value);
            } catch (ExpressionRuntimeException error) {
                classLogger.error("There was an error evaluating the following expression: " + expression);
            }
        }
        return expression;
    }

    public String decideOnValue(String defaultConstant, String parameterValue, String globalConfigFieldName) {
        if (!defaultConstant.equals(parameterValue)) {
            classLogger.trace("Used parameter: " + parameterValue);
            return safeEvaluate(parameterValue);
        } else if (globalConfigurationProperties != null) {
            classLogger.trace("Used global-config: " + globalConfigurationProperties.get(globalConfigFieldName));
            return safeEvaluate(globalConfigurationProperties.get(globalConfigFieldName));
        } else {
            classLogger.trace("Used default: " + parameterValue);
            return safeEvaluate(parameterValue);
        }
    }

    public String retrieveValueFromGlobalConfig(String globalConfigFieldName) {
        if (globalConfigurationProperties != null) {
            return safeEvaluate(globalConfigurationProperties.get(globalConfigFieldName));
        } else {
            return null;
        }
    }

}