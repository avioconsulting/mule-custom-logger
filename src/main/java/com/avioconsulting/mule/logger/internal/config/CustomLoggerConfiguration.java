package com.avioconsulting.mule.logger.internal.config;

import com.avioconsulting.mule.logger.api.processor.LogProperties;
import com.avioconsulting.mule.logger.internal.CustomLogger;
import com.avioconsulting.mule.logger.internal.CustomLoggerOperation;
import com.avioconsulting.mule.logger.internal.CustomLoggerRegistrationService;
import com.avioconsulting.mule.logger.internal.CustomLoggerTimerScopeOperations;
import com.avioconsulting.mule.logger.internal.listeners.CustomLoggerNotificationListener;
import javax.inject.Inject;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.notification.NotificationListenerRegistry;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.LoggerFactory;

/**
 * This class represents an extension configuration, values set in this class
 * are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations({ CustomLoggerOperation.class, CustomLoggerTimerScopeOperations.class })
public class CustomLoggerConfiguration implements Startable {

  private final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerConfiguration.class);
  public static final String DEFAULT_CATEGORY = "com.avioconsulting.api";
  public static final String DEFAULT_APP_NAME = "#[app.name]";
  public static final String DEFAULT_ENV = "#[p('env')]";
  public static final String EXAMPLE_APP_VERSION = "#[p('appVersion')]";
  public static final String DEFAULT_FLOW_CATEGORY = "flow";

  @Parameter
  @DisplayName("Application Name")
  @Summary("Name of the MuleSoft Application")
  @Optional(defaultValue = DEFAULT_APP_NAME)
  private String applicationName;

  @Parameter
  @DisplayName("Application Version")
  @Summary("Version of the MuleSoft Application")
  @Example(EXAMPLE_APP_VERSION)
  private String applicationVersion;

  @Parameter
  @DisplayName("Environment")
  @Summary("MuleSoft Environment")
  @Optional(defaultValue = DEFAULT_ENV)
  private String environment;

  @Parameter
  @DisplayName("Default Log Category")
  @Summary("A string which will be prefixed to all log category suffixes defined in the loggers")
  @Optional(defaultValue = DEFAULT_CATEGORY)
  private String defaultCategory;

  @Parameter
  @DisplayName("Enable Flow Logs")
  @Summary("Enable flow start and stop logs which will be logged to the default category with .flow appended")
  @Optional(defaultValue = "false")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private boolean enableFlowLogs;

  @Parameter
  @DisplayName("Flow Log Level")
  @Summary("The level flow logs will be logged at if enabled")
  @Optional(defaultValue = "DEBUG")
  private LogProperties.LogLevel flowLogLevel;

  @Parameter
  @DisplayName("Flow Log Category Suffix")
  @Summary("This category will be appended to the default logger category and used for all flow logs")
  @Optional(defaultValue = DEFAULT_FLOW_CATEGORY)
  private String flowCategorySuffix;

  @Parameter
  @DisplayName("Enable AVIO Logger V1 Compatibility")
  @Summary("This will print all log messages using the AVIO Logger V1 output format and attribute names.")
  @Optional(defaultValue = "false")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private boolean enableV1Compatibility;

  @Inject
  NotificationListenerRegistry notificationListenerRegistry;

  @Inject
  CustomLoggerRegistrationService customLoggerRegistrationService;

  private CustomLogger logger;
  private CustomLoggerNotificationListener notificationListener;

  public String getApplicationName() {
    return applicationName;
  }

  public String getApplicationVersion() {
    return applicationVersion;
  }

  public String getEnvironment() {
    return environment;
  }

  public String getDefaultCategory() {
    return defaultCategory;
  }

  public CustomLogger getLogger() {
    return logger;
  }

  public boolean isEnableFlowLogs() {
    return enableFlowLogs;
  }

  public LogProperties.LogLevel getFlowLogLevel() {
    return flowLogLevel;
  }

  public String getFlowCategorySuffix() {
    return flowCategorySuffix;
  }

  public boolean isEnableV1Compatibility() {
    return enableV1Compatibility;
  }

  @Override
  public void start() throws MuleException {
    classLogger.info("Starting CustomerLoggerConfiguration");
    this.logger = new CustomLogger();
    classLogger.info("Setting config reference on CustomLoggerRegistrationService");
    customLoggerRegistrationService.setConfig(this);
    if (isEnableFlowLogs()) {
      classLogger.info("Flow logs enabled, creating notification listener");
      notificationListener = new CustomLoggerNotificationListener(this);
      notificationListenerRegistry.registerListener(notificationListener);
    } else {
      classLogger.info("Flow logs disabled");
    }
  }
}
