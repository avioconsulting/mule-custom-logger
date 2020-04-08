package com.avio.customlogger.internal;

import com.avio.customlogger.internal.model.LogLocationInfoProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectMessage;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.route.Chain;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


public class CustomLoggerTimerFrameOperations {

    private Logger logger;

    public void timerFrame(String timerName,
                           String category,
                           @ParameterGroup(name = "App Level") CustomLoggerConfiguration customLoggerConfiguration,
                           @ParameterGroup(name = "Options") LogLocationInfoProperty logLocationInfoProperty,
                           ComponentLocation location,
                           Chain operations,
                           CompletionCallback<Object, Object> callback) {
        this.logger = LogManager.getLogger(category);
        Map<String, Object> logContent = new HashMap<>();
        logContent.put("timer_name", timerName);
        logContent.put("app_name", customLoggerConfiguration.getApp_name());
        logContent.put("app_version", customLoggerConfiguration.getApp_version());
        logContent.put("env", customLoggerConfiguration.getEnv());

        if (logLocationInfoProperty.logLocationInfo) {
            Map<String, String> locationInfo = new HashMap<>();
            locationInfo.put("location", location.getLocation());
            locationInfo.put("root_container", location.getRootContainerName());
            locationInfo.put("component", location.getComponentIdentifier().getIdentifier().toString());
            locationInfo.put("file_name", location.getFileName().orElse(""));
            locationInfo.put("line_in_file", String.valueOf(location.getLineInFile().orElse(null)));

            logContent.put("location", locationInfo);
        }


        Map<String, Object> beforeFrame = new HashMap<>();
        beforeFrame.put("message", timerName + " timer frame starting");
        beforeFrame.put("trace_point", "TIMER_START");
        logContent.put("log", beforeFrame);
        logContent.put("timestamp", Instant.now().toString());
        logger.debug(new ObjectMessage(logContent));

        long startTime = System.currentTimeMillis();
        operations.process(
                result -> {
                    Map<String, Object> afterFrame = new HashMap<>();
                    long elapsedMilliseconds = System.currentTimeMillis() - startTime;
                    afterFrame.put("message", timerName + " timer frame completed with milliseconds elapsed: "  + elapsedMilliseconds);
                    afterFrame.put("elapsed_milliseconds", elapsedMilliseconds);
                    afterFrame.put("trace_point", "TIMER_END");
                    logContent.put("log", afterFrame);
                    logContent.put("timestamp", Instant.now().toString());
                    logger.info(new ObjectMessage(logContent));
                    callback.success(result);
                },
                (error, previous) -> {
                    Map<String, Object> afterFrame = new HashMap<>();
                    long elapsedMilliseconds = System.currentTimeMillis() - startTime;
                    afterFrame.put("message", timerName + " timer frame errored out with milliseconds elapsed: "  + elapsedMilliseconds);
                    afterFrame.put("elapsed_milliseconds", elapsedMilliseconds);
                    afterFrame.put("trace_point", "TIMER_EXCEPTION");
                    logContent.put("log", afterFrame);
                    logContent.put("timestamp", Instant.now().toString());
                    logger.info(new ObjectMessage(logContent));
                    callback.error(error);
                });
    }

}
