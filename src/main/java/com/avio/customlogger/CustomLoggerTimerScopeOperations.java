package com.avio.customlogger;

import com.avio.customlogger.model.LogLocationInfoProperty;
import com.avio.customlogger.utils.CustomLoggerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectMessage;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.route.Chain;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.avio.customlogger.utils.CustomLoggerConstants.*;
import static com.avio.customlogger.utils.CustomLoggerConstants.DEFAULT_CATEGORY_PREFIX;


public class CustomLoggerTimerScopeOperations {
    public static final String DEFAULT_CATEGORY_SUFFIX = ".timer";
    private final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerTimerScopeOperations.class);

    @Inject
    Registry registry;

    private Logger logger;
    private CustomLoggerUtils customLoggerUtils;

    public void timerScope(String timerName,
                           @Optional(defaultValue = DEFAULT_CATEGORY_SUFFIX) String categorySuffix,
                           @ParameterGroup(name = "Options") LogLocationInfoProperty logLocationInfoProperty,
                           ComponentLocation location,
                           Chain operations,
                           CompletionCallback<Object, Object> callback) {
        if (customLoggerUtils == null) {
            customLoggerUtils = new CustomLoggerUtils(registry);
        } else {
            classLogger.info("Registry was found");
        }
        this.logger = LogManager.getLogger(customLoggerUtils.retrieveValueFromGlobalConfig("category_prefix") + categorySuffix);
        Map<String, Object> logContext = new HashMap<>();
        logContext.put("timer_name", timerName);
        logContext.put("app_name", customLoggerUtils.retrieveValueFromGlobalConfig("app_name"));
        logContext.put("app_version", customLoggerUtils.retrieveValueFromGlobalConfig("app_version"));
        logContext.put("env", customLoggerUtils.retrieveValueFromGlobalConfig("env"));
        if (logLocationInfoProperty.logLocationInfo) {
            logContext.put("location", CustomLoggerUtils.getLocationInformation(location));
        }
        Map<String, Object> beforeScope = new HashMap<>();
        beforeScope.put("message", timerName + " timer scope starting");
        beforeScope.put("trace_point", "TIMER_START");
        logContext.put("log", beforeScope);
        logContext.put("timestamp", Instant.now().toString());
        logger.debug(new ObjectMessage(logContext));

        long startTime = System.currentTimeMillis();
        operations.process(
                result -> {
                    Map<String, Object> afterScope = new HashMap<>();
                    long elapsedMilliseconds = System.currentTimeMillis() - startTime;
                    afterScope.put("message", timerName + " timer scope completed with milliseconds elapsed: "  + elapsedMilliseconds);
                    afterScope.put("elapsed_milliseconds", elapsedMilliseconds);
                    afterScope.put("trace_point", "TIMER_END");
                    logContext.put("log", afterScope);
                    logContext.put("timestamp", Instant.now().toString());
                    logger.info(new ObjectMessage(logContext));
                    callback.success(result);
                },
                (error, previous) -> {
                    Map<String, Object> afterScope = new HashMap<>();
                    long elapsedMilliseconds = System.currentTimeMillis() - startTime;
                    afterScope.put("message", timerName + " timer scope errored out with milliseconds elapsed: "  + elapsedMilliseconds);
                    afterScope.put("elapsed_milliseconds", elapsedMilliseconds);
                    afterScope.put("trace_point", "TIMER_EXCEPTION");
                    logContext.put("log", afterScope);
                    logContext.put("timestamp", Instant.now().toString());
                    logger.info(new ObjectMessage(logContext));
                    callback.error(error);
                });
    }

}
