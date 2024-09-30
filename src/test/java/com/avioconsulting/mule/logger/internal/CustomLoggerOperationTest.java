package com.avioconsulting.mule.logger.internal;

import static com.avioconsulting.mule.logger.api.processor.EncryptionAlgorithm.PBEWithHmacSHA256AndAES_128;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.avioconsulting.mule.logger.api.processor.*;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import com.mulesoft.modules.cryptography.api.jce.config.JceEncryptionPbeAlgorithm;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.notification.NotificationListenerRegistry;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.core.internal.streaming.bytes.ByteArrayCursorStreamProvider;
import org.mule.runtime.extension.api.client.DefaultOperationParameters;
import org.mule.runtime.extension.api.client.ExtensionsClient;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.LoggerFactory;

public class CustomLoggerOperationTest {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(CustomLoggerOperationTest.class);
  private String examplePayload;
  private static ListAppender appender;

  @BeforeClass
  public static void init() {
    LoggerContext loggerContext = LoggerContext.getContext(false);
    Logger logger = loggerContext.getLogger("com.avioconsulting.api");
    logger.setLevel(Level.DEBUG);
    appender = new ListAppender("list");
    appender.start();
    loggerContext.getConfiguration().addLoggerAppender(logger, appender);
  }

  @Before
  public void clear() {
    appender.clear();
  }

  @Test
  public void log_verifyNoPayloadTransformation_test() throws MuleException {
    CustomLoggerOperation customLoggerOperation = new CustomLoggerOperation();
    ExtensionsClient extensionsClient = mock(ExtensionsClient.class);
    NotificationListenerRegistry notificationListenerRegistry = mock(NotificationListenerRegistry.class);
    CustomLoggerConfiguration loggerConfig = new CustomLoggerConfiguration(new CustomLoggerRegistrationService(),
        notificationListenerRegistry, extensionsClient);
    loggerConfig.setApplicationName("test-application");
    loggerConfig.setApplicationVersion("1.0-test");
    loggerConfig.setEnvironment("test");
    loggerConfig.start();
    LogProperties lp = new LogProperties();
    lp.setLevel(LogProperties.LogLevel.ERROR);
    lp.setCategory("com.avioconsulting.api");
    lp.setMessage("Some test message");
    customLoggerOperation.log(lp,
        mock(MessageAttributes.class),
        mock(ExceptionProperties.class),
        mock(AdditionalProperties.class),
        loggerConfig,
        mock(ComponentLocation.class),
        mock(CorrelationInfo.class),
        mock(StreamingHelper.class));
    List<String> loggedStrings = appender.getEvents().stream().map(event -> event.getMessage().toString())
        .collect(Collectors.toList());
    assertThat(loggedStrings).hasSize(1)
        .element(0)
        .asInstanceOf(InstanceOfAssertFactories.STRING)
        .containsSubsequence("message=Some test message")
        .containsSubsequence("env=test")
        .containsSubsequence("appName=test-application")
        .containsSubsequence("appVersion=1.0-test");
    verifyZeroInteractions(extensionsClient);
  }

  @Test
  public void log_compressedPayloadIsSet_test() throws MuleException {

    String payloadText = "PayloadText";
    CustomLoggerOperation customLoggerOperation = new CustomLoggerOperation();
    ExtensionsClient extensionsClient = mock(ExtensionsClient.class);
    ArgumentCaptor<DefaultOperationParameters> captor = ArgumentCaptor.forClass(DefaultOperationParameters.class);
    when(extensionsClient.execute(eq("Compression"), eq("compress"),
        captor.capture())).thenReturn(Result.builder().output(payloadText).build());
    NotificationListenerRegistry notificationListenerRegistry = mock(NotificationListenerRegistry.class);
    CustomLoggerConfiguration loggerConfig = new CustomLoggerConfiguration(new CustomLoggerRegistrationService(),
        notificationListenerRegistry, extensionsClient);
    loggerConfig.setApplicationName("test-application");
    loggerConfig.setApplicationVersion("1.0-test");
    loggerConfig.setEnvironment("test");
    loggerConfig.setCompressor(Compressor.GZIP);
    loggerConfig.start();
    LogProperties lp = new LogProperties();
    lp.setLevel(LogProperties.LogLevel.ERROR);
    lp.setCategory("com.avioconsulting.api");
    lp.setMessage("Some test message");
    lp.setPayload(TestParameterResolver.of(payloadText));
    StreamingHelper streamingHelper = mock(StreamingHelper.class);
    when(streamingHelper.resolveCursorProvider(payloadText))
        .thenReturn(new ByteArrayCursorStreamProvider(payloadText.getBytes()));
    customLoggerOperation.log(lp,
        mock(MessageAttributes.class),
        mock(ExceptionProperties.class),
        mock(AdditionalProperties.class),
        loggerConfig,
        mock(ComponentLocation.class),
        mock(CorrelationInfo.class),
        streamingHelper);
    List<String> loggedStrings = appender.getEvents().stream().map(event -> event.getMessage().toString())
        .collect(Collectors.toList());
    assertThat(loggedStrings).hasSize(1)
        .element(0)
        .asInstanceOf(InstanceOfAssertFactories.STRING)
        .containsSubsequence("message=Some test message")
        .containsSubsequence("env=test")
        .containsSubsequence("appName=test-application")
        .containsSubsequence("appVersion=1.0-test")
        .containsSubsequence("payload=UGF5bG9hZFRleHQ=");
    assertThat(captor.getValue())
        .as("Operation config with extension client invocation")
        .isNotNull()
        .asInstanceOf(InstanceOfAssertFactories.type(DefaultOperationParameters.class))
        .extracting(DefaultOperationParameters::get)
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .extractingByKey("content")
        .asInstanceOf(InstanceOfAssertFactories.type(InputStream.class))
        .as("Actual Payload text sent to the Compress operation")
        .satisfies(o -> assertThat(IOUtils.toString(o)).isEqualTo("PayloadText"));
  }

  @Test
  public void log_encryptedPayloadIsSet_compressionNull_test() throws MuleException {

    String payloadText = "PayloadText";
    CustomLoggerOperation customLoggerOperation = new CustomLoggerOperation();
    ExtensionsClient extensionsClient = mock(ExtensionsClient.class);
    ArgumentCaptor<DefaultOperationParameters> captor = ArgumentCaptor.forClass(DefaultOperationParameters.class);
    String encryptedText = "EncryptedText";
    when(extensionsClient.execute(eq("Crypto"), eq("jceEncryptPbe"),
        captor.capture())).thenReturn(Result.builder().output(encryptedText).build());
    NotificationListenerRegistry notificationListenerRegistry = mock(NotificationListenerRegistry.class);
    CustomLoggerConfiguration loggerConfig = new CustomLoggerConfiguration(new CustomLoggerRegistrationService(),
        notificationListenerRegistry, extensionsClient);
    loggerConfig.setApplicationName("test-application");
    loggerConfig.setApplicationVersion("1.0-test");
    loggerConfig.setEnvironment("test");
    loggerConfig.setEncryptionAlgorithm(PBEWithHmacSHA256AndAES_128);
    loggerConfig.setEncryptionPassword("something123");
    loggerConfig.start();
    LogProperties lp = new LogProperties();
    lp.setLevel(LogProperties.LogLevel.ERROR);
    lp.setCategory("com.avioconsulting.api");
    lp.setMessage("Some test message");
    lp.setPayload(TestParameterResolver.of(payloadText));
    StreamingHelper streamingHelper = mock(StreamingHelper.class);
    when(streamingHelper.resolveCursorProvider(encryptedText))
        .thenReturn(new ByteArrayCursorStreamProvider(encryptedText.getBytes()));
    customLoggerOperation.log(lp,
        mock(MessageAttributes.class),
        mock(ExceptionProperties.class),
        mock(AdditionalProperties.class),
        loggerConfig,
        mock(ComponentLocation.class),
        mock(CorrelationInfo.class),
        streamingHelper);
    List<String> loggedStrings = appender.getEvents().stream().map(event -> event.getMessage().toString())
        .collect(Collectors.toList());
    assertThat(loggedStrings).hasSize(1)
        .element(0)
        .asInstanceOf(InstanceOfAssertFactories.STRING)
        .containsSubsequence("message=Some test message")
        .containsSubsequence("env=test")
        .containsSubsequence("appName=test-application")
        .containsSubsequence("appVersion=1.0-test")
        .containsSubsequence("payload=RW5jcnlwdGVkVGV4dA==");
    assertThat(captor.getValue())
        .as("Operation config with extension client invocation")
        .isNotNull()
        .asInstanceOf(InstanceOfAssertFactories.type(DefaultOperationParameters.class))
        .extracting(DefaultOperationParameters::get)
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsEntry("algorithm", JceEncryptionPbeAlgorithm.PBEWithHmacSHA256AndAES_128)
        .containsEntry("password", "something123")
        .extractingByKey("content")
        .asInstanceOf(InstanceOfAssertFactories.type(InputStream.class))
        .as("Actual Payload text sent to the Encryption operation")
        .satisfies(o -> assertThat(IOUtils.toString(o)).isEqualTo("PayloadText"));

  }

  @Test
  public void log_encryptedPayloadIsSet_compressionNotNull_test() throws MuleException {

    String payloadText = "PayloadText";
    CustomLoggerOperation customLoggerOperation = new CustomLoggerOperation();
    ExtensionsClient extensionsClient = mock(ExtensionsClient.class);
    ArgumentCaptor<DefaultOperationParameters> compression = ArgumentCaptor
        .forClass(DefaultOperationParameters.class);
    String compressedPayload = "CompressedPayload";
    when(extensionsClient.execute(eq("Compression"), eq("compress"),
        compression.capture())).thenReturn(Result.builder().output(compressedPayload).build());

    ArgumentCaptor<DefaultOperationParameters> encryption = ArgumentCaptor
        .forClass(DefaultOperationParameters.class);
    String encryptedResult = "CompressedEncryptedResult";
    when(extensionsClient.execute(eq("Crypto"), eq("jceEncryptPbe"),
        encryption.capture())).thenReturn(Result.builder().output(encryptedResult).build());

    NotificationListenerRegistry notificationListenerRegistry = mock(NotificationListenerRegistry.class);
    CustomLoggerConfiguration loggerConfig = new CustomLoggerConfiguration(new CustomLoggerRegistrationService(),
        notificationListenerRegistry, extensionsClient);
    loggerConfig.setApplicationName("test-application");
    loggerConfig.setApplicationVersion("1.0-test");
    loggerConfig.setEnvironment("test");
    loggerConfig.setCompressor(Compressor.GZIP);
    loggerConfig.setEncryptionAlgorithm(PBEWithHmacSHA256AndAES_128);
    loggerConfig.setEncryptionPassword("something123");
    loggerConfig.start();
    LogProperties lp = new LogProperties();
    lp.setLevel(LogProperties.LogLevel.ERROR);
    lp.setCategory("com.avioconsulting.api");
    lp.setMessage("Some test message");
    lp.setPayload(TestParameterResolver.of(payloadText));
    StreamingHelper streamingHelper = mock(StreamingHelper.class);

    when(streamingHelper.resolveCursorProvider(compressedPayload))
        .thenReturn(new ByteArrayCursorStreamProvider(compressedPayload.getBytes()));

    when(streamingHelper.resolveCursorProvider(encryptedResult))
        .thenReturn(new ByteArrayCursorStreamProvider(encryptedResult.getBytes()));

    customLoggerOperation.log(lp,
        mock(MessageAttributes.class),
        mock(ExceptionProperties.class),
        mock(AdditionalProperties.class),
        loggerConfig,
        mock(ComponentLocation.class),
        mock(CorrelationInfo.class),
        streamingHelper);

    List<String> loggedStrings = appender.getEvents().stream().map(event -> event.getMessage().toString())
        .collect(Collectors.toList());
    assertThat(loggedStrings).hasSize(1)
        .element(0)
        .asInstanceOf(InstanceOfAssertFactories.STRING)
        .containsSubsequence("message=Some test message")
        .containsSubsequence("env=test")
        .containsSubsequence("appName=test-application")
        .containsSubsequence("appVersion=1.0-test")
        .containsSubsequence("payload=Q29tcHJlc3NlZEVuY3J5cHRlZFJlc3VsdA==");

    assertThat(compression.getValue())
        .as("Operation config with extension client invocation")
        .isNotNull()
        .asInstanceOf(InstanceOfAssertFactories.type(DefaultOperationParameters.class))
        .extracting(DefaultOperationParameters::get)
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .extractingByKey("content")
        .asInstanceOf(InstanceOfAssertFactories.type(InputStream.class))
        .as("Actual Payload text sent to the Compress operation")
        .satisfies(o -> assertThat(IOUtils.toString(o)).isEqualTo(payloadText));

    assertThat(encryption.getValue())
        .as("Operation config with extension client invocation")
        .isNotNull()
        .asInstanceOf(InstanceOfAssertFactories.type(DefaultOperationParameters.class))
        .extracting(DefaultOperationParameters::get)
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsEntry("algorithm", JceEncryptionPbeAlgorithm.PBEWithHmacSHA256AndAES_128)
        .containsEntry("password", "something123")
        .extractingByKey("content")
        .asInstanceOf(InstanceOfAssertFactories.type(InputStream.class))
        .as("Compressed Payload text sent to the Encryption operation")
        .satisfies(o -> assertThat(IOUtils.toString(o)).isEqualTo(compressedPayload));

  }

  public static class TestParameterResolver implements ParameterResolver<String> {

    private final String value;

    public TestParameterResolver(String value) {
      this.value = value;
    }

    public static TestParameterResolver of(String value) {
      return new TestParameterResolver(value);
    }

    @Override
    public String resolve() {
      return value;
    }

    @Override
    public Optional<String> getExpression() {
      return Optional.empty();
    }
  }
}
