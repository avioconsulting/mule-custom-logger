package com.avio.customlogger.utils;

import com.avio.customlogger.CustomLoggerTimerScopeOperations;
import com.avio.customlogger.engine.CustomLoggerNotificationListener;
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

import static com.avio.customlogger.utils.CustomLoggerConstants.DEFAULT_CATEGORY_PREFIX;
import static com.avio.customlogger.utils.CustomLoggerConstants.DEFAULT_CATEGORY_SUFFIX;

public class CustomLoggerUtils {

    private Registry registry;

    private static final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerUtils.class);

    private ExpressionManager expressionManager = null;
    private Map<String, String> globalConfig = null;

    public CustomLoggerUtils(Registry registry) {
        this.registry = registry;
        if (registry != null) {
            classLogger.debug("Locating avio-core:config global element");
            Optional<ConfigurationComponentLocator> configurationComponentLocator = registry.lookupByType(ConfigurationComponentLocator.class);
            if (configurationComponentLocator.isPresent()) {
                List<Component> components = configurationComponentLocator.get().find(ComponentIdentifier.builder().namespace("avio-core").name("config").build());
                if (components.size() == 1) {
                    try {
                        classLogger.debug("Located avio-core:config global element, attempting to retrieve properties");
                        globalConfig = (Map<String, String>) components.get(0).getAnnotations().get(QName.valueOf("{config}componentParameters"));
                    } catch (Exception e) {
                        classLogger.error("Could not retrieve properties from avio-core:config global element");
                    }
                }
                classLogger.debug("Retrieved properties from avio-core:config global element, attempting to parse and store in logContext");
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
                categorySuffix.charAt(0) != '.' &&
                        !(DEFAULT_CATEGORY_SUFFIX.equals(categorySuffix) ||
                        CustomLoggerTimerScopeOperations.DEFAULT_CATEGORY_SUFFIX.equals(categorySuffix) ||
                        CustomLoggerNotificationListener.DEFAULT_CATEGORY_SUFFIX.equals(categorySuffix)))
        ) {
            return LogManager.getLogger(categorySuffix);
        }
        if (categoryPrefix.charAt(categoryPrefix.length()-1) == '.') {
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
        } else if (globalConfig != null) {
            classLogger.trace("Used global-config: " + globalConfig.get(globalConfigFieldName));
            return safeEvaluate(globalConfig.get(globalConfigFieldName));
        } else {
            classLogger.trace("Used default: " + parameterValue);
            return safeEvaluate(parameterValue);
        }
    }

    public String retrieveValueFromGlobalConfig(String globalConfigFieldName) {
        if (globalConfig != null) {
            return safeEvaluate(globalConfig.get(globalConfigFieldName));
        } else {
            return null;
        }
    }

    public static Map<String, String> getLocationInformation(ComponentLocation location) {
        Map<String, String> locationInfo = new HashMap<>();
        locationInfo.put("location", location.getLocation());
        locationInfo.put("root_container", location.getRootContainerName());
        locationInfo.put("component", location.getComponentIdentifier().getIdentifier().toString());
        locationInfo.put("file_name", location.getFileName().orElse(""));
        locationInfo.put("line_in_file", String.valueOf(location.getLineInFile().orElse(null)));
        return locationInfo;
    }
}