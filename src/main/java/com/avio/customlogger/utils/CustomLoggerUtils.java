package com.avio.customlogger.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.expression.ExpressionRuntimeException;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.avio.customlogger.utils.CustomLoggerConstants.DEFAULT_CATEGORY_PREFIX;
import static com.avio.customlogger.utils.CustomLoggerConstants.DEFAULT_CATEGORY_SUFFIX;

public class CustomLoggerUtils {

    private Registry registry;

    private static final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerUtils.class);


    private ExpressionManager expressionManager;
    private int expressionManagerRetrievalAttempts = 1;

    public CustomLoggerUtils(Registry registry) {
        this.registry = registry;
    }

    public static Logger initLogger(String categoryPrefix, String categorySuffix) {
        if (categoryPrefix == null) {
            categoryPrefix = DEFAULT_CATEGORY_PREFIX;
        }
        if (categorySuffix == null) {
            categorySuffix = DEFAULT_CATEGORY_SUFFIX;
        }
        if (categorySuffix.contains(categoryPrefix)) {
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

    public String safeEvaluate(String expression) {
        if (expressionManager == null && expressionManagerRetrievalAttempts <= 10) {
            classLogger.debug("ExpressionManager has not been set, attempting to retrieve");
            if (registry == null) {
                classLogger.debug("Registry is null, skipping evaluation");
            } else {
                Optional<ExpressionManager> optionalExpressionManager = registry.lookupByType(ExpressionManager.class);
                if (optionalExpressionManager.isPresent()) {
                    classLogger.debug("ExpressionManager was found");
                    expressionManager = optionalExpressionManager.get();
                } else {
                    classLogger.debug("ExpressionManager was not found during retrieval on attempt " + expressionManagerRetrievalAttempts + "/10");
                    expressionManagerRetrievalAttempts++;
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

}
