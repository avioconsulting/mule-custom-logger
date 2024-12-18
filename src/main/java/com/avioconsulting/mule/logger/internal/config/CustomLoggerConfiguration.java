package com.avioconsulting.mule.logger.internal.config;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.avioconsulting.mule.logger.api.processor.Compressor;
import com.avioconsulting.mule.logger.api.processor.EncryptionAlgorithm;
import com.avioconsulting.mule.logger.api.processor.FlowLogConfig;
import com.avioconsulting.mule.logger.api.processor.LogProperties;
import com.avioconsulting.mule.logger.internal.CustomLogger;
import com.avioconsulting.mule.logger.internal.CustomLoggerOperation;
import com.avioconsulting.mule.logger.internal.CustomLoggerRegistrationService;
import com.avioconsulting.mule.logger.internal.CustomLoggerTimerScopeOperations;
import com.avioconsulting.mule.logger.internal.listeners.CustomLoggerFlowRefNotificationListener;
import com.avioconsulting.mule.logger.internal.listeners.CustomLoggerPipelineNotificationListener;
import javax.inject.Inject;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.notification.NotificationListenerRegistry;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.*;
import org.mule.runtime.extension.api.client.ExtensionsClient;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class represents an extension configuration, values set in this class
 * are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations({ CustomLoggerOperation.class, CustomLoggerTimerScopeOperations.class })
public class CustomLoggerConfiguration implements Startable, Initialisable {

  private final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerConfiguration.class);
  public static final String DEFAULT_CATEGORY = "com.avioconsulting.api";
  public static final String DEFAULT_APP_NAME = "${app.name}";
  public static final String DEFAULT_ENV = "${env}";
  public static final String DEFAULT_APP_VERSION = "${app.version}";
  public static final String DEFAULT_FLOW_CATEGORY = "flow";

  @Parameter
  @DisplayName("Application Name")
  @Summary("Name of the MuleSoft Application")
  @Optional(defaultValue = DEFAULT_APP_NAME)
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private String applicationName;

  @Parameter
  @DisplayName("Application Version")
  @Summary("Version of the MuleSoft Application")
  @Optional(defaultValue = DEFAULT_APP_VERSION)
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private String applicationVersion;

  @Parameter
  @DisplayName("Environment")
  @Summary("MuleSoft Environment")
  @Optional(defaultValue = DEFAULT_ENV)
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private String environment;

  @Parameter
  @DisplayName("Default Log Category")
  @Summary("A string which will be prefixed to all log category suffixes defined in the loggers")
  @Optional(defaultValue = DEFAULT_CATEGORY)
  @Expression(ExpressionSupport.NOT_SUPPORTED)
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
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private LogProperties.LogLevel flowLogLevel;

  @Parameter
  @DisplayName("Flow Log Attributes")
  @Summary("The level flow logs will be logged at if enabled")
  @NullSafe
  @Optional
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private List<FlowLogConfig> flowLogConfigs;

  @Parameter
  @DisplayName("Flow Log Category Suffix")
  @Summary("This category will be appended to the default logger category and used for all flow logs")
  @Optional(defaultValue = DEFAULT_FLOW_CATEGORY)
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private String flowCategorySuffix;

  @Parameter
  @DisplayName("Enable AVIO Logger V1 Compatibility")
  @Summary("This will print all log messages using the AVIO Logger V1 output format and attribute names.")
  @Optional(defaultValue = "false")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private boolean enableV1Compatibility;

  @Parameter
  @DisplayName("Format Message as JSON String")
  @Summary("Writes the Log message as JSON String. This can be useful where appenders (eg. OpenTelemetry) does not support using layouts but backend system requires a JSON formatted message.")
  @Optional(defaultValue = "false")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private boolean formatAsJson;

  @Parameter
  @DisplayName("Compression Strategy")
  @Summary("Enumerated Compression Strategy value to compress payload before logging")
  @Optional()
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Placement(tab = "Compression", order = 1)
  private Compressor compressor;

  @Parameter
  @DisplayName("Encryption Algorithm (with JCEwithPBE)")
  @Summary("Choose an encryption algorithm from the enumerated list to use in conjunction with the password to encrypt the payload before logging")
  @Optional()
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Placement(tab = "Encryption", order = 1)
  private EncryptionAlgorithm encryptionAlgorithm;

  @Parameter
  @DisplayName("Encryption Password")
  @Summary("Password to use with encryption algorithm to encrypt payload value")
  @Optional()
  @Password
  @Placement(tab = "Encryption", order = 2)
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private String encryptionPassword;

  @Inject
  NotificationListenerRegistry notificationListenerRegistry;

  @Inject
  CustomLoggerRegistrationService customLoggerRegistrationService;

  @Inject
  ExtensionsClient extensionsClient;

  @Inject
  ExpressionManager expressionManager;
  private Map<String, FlowLogConfig> flowLogConfigMap;

  /**
   * Default constructor for auto-initialization
   */
  public CustomLoggerConfiguration() {
  }

  /**
   * Constructor for using in tests
   *
   * @param customLoggerRegistrationService
   * @param notificationListenerRegistry
   * @param extensionsClient
   */
  public CustomLoggerConfiguration(CustomLoggerRegistrationService customLoggerRegistrationService,
      NotificationListenerRegistry notificationListenerRegistry, ExtensionsClient extensionsClient) {
    this.customLoggerRegistrationService = customLoggerRegistrationService;
    this.notificationListenerRegistry = notificationListenerRegistry;
    this.extensionsClient = extensionsClient;
  }

  /**
   * Constructor for using in tests
   *
   * @param customLoggerRegistrationService
   * @param notificationListenerRegistry
   */
  public CustomLoggerConfiguration(CustomLoggerRegistrationService customLoggerRegistrationService,
      NotificationListenerRegistry notificationListenerRegistry) {
    this.customLoggerRegistrationService = customLoggerRegistrationService;
    this.notificationListenerRegistry = notificationListenerRegistry;
  }

  private CustomLogger logger;
  private CustomLoggerPipelineNotificationListener notificationListener;

  private static boolean isNotificationListenerRegistered = false;

  public Map<String, FlowLogConfig> getFlowLogConfigMap() {
    return flowLogConfigMap;
  }

  public CustomLoggerConfiguration setFlowLogConfigs(List<FlowLogConfig> flowLogConfigs) {
    this.flowLogConfigs = flowLogConfigs;
    return this;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public CustomLoggerConfiguration setApplicationName(String applicationName) {
    this.applicationName = applicationName;
    return this;
  }

  public String getApplicationVersion() {
    return applicationVersion;
  }

  public CustomLoggerConfiguration setApplicationVersion(String applicationVersion) {
    this.applicationVersion = applicationVersion;
    return this;
  }

  public String getEnvironment() {
    return environment;
  }

  public CustomLoggerConfiguration setEnvironment(String environment) {
    this.environment = environment;
    return this;
  }

  public String getDefaultCategory() {
    return defaultCategory;
  }

  public CustomLoggerConfiguration setDefaultCategory(String defaultCategory) {
    this.defaultCategory = defaultCategory;
    return this;
  }

  public boolean isEnableFlowLogs() {
    return enableFlowLogs;
  }

  public CustomLoggerConfiguration setEnableFlowLogs(boolean enableFlowLogs) {
    this.enableFlowLogs = enableFlowLogs;
    return this;
  }

  public LogProperties.LogLevel getFlowLogLevel() {
    return flowLogLevel;
  }

  public CustomLoggerConfiguration setFlowLogLevel(LogProperties.LogLevel flowLogLevel) {
    this.flowLogLevel = flowLogLevel;
    return this;
  }

  public String getFlowCategorySuffix() {
    return flowCategorySuffix;
  }

  public CustomLoggerConfiguration setFlowCategorySuffix(String flowCategorySuffix) {
    this.flowCategorySuffix = flowCategorySuffix;
    return this;
  }

  public boolean isEnableV1Compatibility() {
    return enableV1Compatibility;
  }

  public CustomLoggerConfiguration setEnableV1Compatibility(boolean enableV1Compatibility) {
    this.enableV1Compatibility = enableV1Compatibility;
    return this;
  }

  public boolean isFormatAsJson() {
    return formatAsJson;
  }

  public CustomLoggerConfiguration setFormatAsJson(boolean formatAsJson) {
    this.formatAsJson = formatAsJson;
    return this;
  }

  public Compressor getCompressor() {
    return compressor;
  }

  public CustomLoggerConfiguration setCompressor(Compressor compressor) {
    this.compressor = compressor;
    return this;
  }

  public EncryptionAlgorithm getEncryptionAlgorithm() {
    return encryptionAlgorithm;
  }

  public CustomLoggerConfiguration setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
    this.encryptionAlgorithm = encryptionAlgorithm;
    return this;
  }

  public String getEncryptionPassword() {
    return encryptionPassword;
  }

  public CustomLoggerConfiguration setEncryptionPassword(String encryptionPassword) {
    this.encryptionPassword = encryptionPassword;
    return this;
  }

  public CustomLogger getLogger() {
    return logger;
  }

  public ExtensionsClient getExtensionsClient() {
    return extensionsClient;
  }

  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  /**
   * This method is invoked by the MuleSoft application when the AVIO Custom
   * Logger is invoked to create the connection.
   * It creates a CustomLogger object based on the config properties provided by
   * configuration provided in MuleSoft application.
   * Static variables are used to prevent more than one notification listener is
   * created and registered.
   * Synchronized utilized to prevent prallelism from creating more than one
   * notification listener
   *
   * @throws MuleException
   *
   */
  @Override
  public void start() throws MuleException {
    classLogger.info("Starting CustomerLoggerConfiguration");
    this.logger = new CustomLogger();
    classLogger.info("Setting config reference on CustomLoggerRegistrationService");
    customLoggerRegistrationService.setConfig(this);
    if (isEnableFlowLogs()) {
      classLogger.info("Flow logs enabled");
      flowLogConfigs.forEach(flowLogConfig -> {
        if (flowLogConfig.getExpressionText().getMessageExpressionText() == null
            && flowLogConfig.getExpressionText().getAttributesExpressionText() == null) {
          throw new IllegalStateException(
              "One of attributesExpressionText or messageExpressionText must be defined in flow-logs-config");
        }
      });
      flowLogConfigMap = flowLogConfigs.stream().collect(
          Collectors.toMap(FlowLogConfig::getFlowName, Function.identity()));
      synchronized (CustomLoggerConfiguration.class) {
        if (!isNotificationListenerRegistered) {
          classLogger.info("Creating and registering notification listener");
          notificationListener = new CustomLoggerPipelineNotificationListener(this);
          notificationListenerRegistry.registerListener(notificationListener);
          notificationListenerRegistry.registerListener(new CustomLoggerFlowRefNotificationListener(this));
          isNotificationListenerRegistered = true;
        }
      }
    } else {
      classLogger.info("Flow logs disabled");
    }
  }

  /**
   * Validation for any parameters. Such as, two optional parameters that, when
   * used, are depdendent on each other
   *
   * @since 2.1.0
   * @throws InitialisationException
   */
  @Override
  public void initialise() throws InitialisationException {
    EncryptionAlgorithm encryptionAlgorithm = this.getEncryptionAlgorithm();
    String encryptionPassword = this.getEncryptionPassword();
    if (encryptionAlgorithm != null && encryptionPassword == null) {
      throw new InitialisationException(
          createStaticMessage("Encryption Password must be provided if encryption algorithm is being used"),
          this);
    } else if (encryptionAlgorithm == null && encryptionPassword != null) {
      throw new InitialisationException(createStaticMessage(
          "Encryption Algorithm must be provided if encryption password is being supplied"), this);
    }
  }
}
