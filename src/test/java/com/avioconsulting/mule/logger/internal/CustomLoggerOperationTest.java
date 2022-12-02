package com.avioconsulting.mule.logger.internal;

import static org.mockito.Mockito.*;

import com.avioconsulting.mule.logger.api.processor.*;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.client.ExtensionsClient;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;

public class CustomLoggerOperationTest {

  private String examplePayload;

  @Before
  public void init() {

    examplePayload = "Example Payload Text";

  }

  @Test
  public void log_verifyNoPayloadTransformation_test() {
    CustomLoggerOperation customLoggerOperation = new CustomLoggerOperation();
    ExtensionsClient extensionsClient = mock(ExtensionsClient.class);
    customLoggerOperation.extensionsClient = extensionsClient;

    // create mock for custom logger and mock log() method so it's never called
    CustomLoggerConfiguration loggerConfig = mock(CustomLoggerConfiguration.class);
    CustomLogger logger = mock(CustomLogger.class);
    doNothing().when(logger).log(any(), any(), any(), any(), any(), any(), any());
    when(loggerConfig.getLogger()).thenReturn(logger);

    LogProperties logProperties = mock(LogProperties.class);
    when(logProperties.getPayload()).thenReturn(null);

    customLoggerOperation.log(logProperties,
        mock(MessageAttributes.class),
        mock(ExceptionProperties.class),
        mock(AdditionalProperties.class),
        loggerConfig,
        mock(ComponentLocation.class),
        mock(CorrelationInfo.class),
        mock(StreamingHelper.class));

    verifyZeroInteractions(extensionsClient);
  }

  @Test
  public void log_compressedPayloadIsSet_test() throws MuleException {
    // mock and spy inits
    CustomLoggerOperation customLoggerOperation = new CustomLoggerOperation();
    CustomLoggerOperation spyCustomLoggerOperation = spy(customLoggerOperation);
    LogProperties logProperties = new LogProperties();
    LogProperties spyLogProperties = spy(logProperties);
    ExtensionsClient extensionsClient = mock(ExtensionsClient.class);
    spyCustomLoggerOperation.extensionsClient = extensionsClient;
    ParameterResolver<String> resolver = mock(ParameterResolver.class);

    // create mock for custom logger and mock log() method so it's never called
    CustomLoggerConfiguration loggerConfig = mock(CustomLoggerConfiguration.class);
    CustomLogger logger = mock(CustomLogger.class);
    doNothing().when(logger).log(any(), any(), any(), any(), any(), any(), any());
    when(loggerConfig.getLogger()).thenReturn(logger);

    // Mock payload and compressor values
    when(resolver.resolve()).thenReturn(examplePayload);
    when(spyLogProperties.getPayload()).thenReturn(resolver);
    when(loggerConfig.getCompressor()).thenReturn(Compressor.GZIP);

    // mock response from extensions client
    Result<InputStream, Void> mockResult = mock(Result.class);
    doReturn(new ByteArrayInputStream(examplePayload.getBytes())).when(mockResult).getOutput();
    doReturn(mockResult).when(extensionsClient).execute(eq("Compression"), eq("compress"), any());

    // mock response from convertToByteArray
    doReturn(examplePayload.getBytes()).when(spyCustomLoggerOperation)
        .convertToByteArray(any(), any());

    // call method
    spyCustomLoggerOperation.setCompressedPayloadIfNeeded(spyLogProperties,
        mock(StreamingHelper.class),
        loggerConfig);

    // assertions
    verify(extensionsClient, atMost(1)).execute(any(), any(), any());
    Assert.assertEquals("RXhhbXBsZSBQYXlsb2FkIFRleHQ=", spyLogProperties.getCompressedPayload());
  }

  @Test
  public void log_encryptedPayloadIsSet_compressionNull_test() throws MuleException {
    // mock and spy inits
    CustomLoggerOperation customLoggerOperation = new CustomLoggerOperation();
    CustomLoggerOperation spyCustomLoggerOperation = spy(customLoggerOperation);
    LogProperties logProperties = new LogProperties();
    LogProperties spyLogProperties = spy(logProperties);
    ExtensionsClient extensionsClient = mock(ExtensionsClient.class);
    spyCustomLoggerOperation.extensionsClient = extensionsClient;
    Result<InputStream, Void> executeCompress = null;
    ParameterResolver<String> resolver = mock(ParameterResolver.class);
    String password = "example-password";

    // create mock for custom logger and mock log() method so it's never called
    CustomLoggerConfiguration loggerConfig = mock(CustomLoggerConfiguration.class);
    CustomLogger logger = mock(CustomLogger.class);
    doNothing().when(logger).log(any(), any(), any(), any(), any(), any(), any());
    when(loggerConfig.getLogger()).thenReturn(logger);

    // Mock payload and encryption algorithm values, compression is null
    when(resolver.resolve()).thenReturn(examplePayload);
    when(spyLogProperties.getPayload()).thenReturn(resolver);
    when(loggerConfig.getEncryptionAlgorithm()).thenReturn(EncryptionAlgorithm.PBEWithHmacSHA1AndAES_128);
    when(loggerConfig.getEncryptionPassword()).thenReturn(password);

    // mock response from extensions client
    Result<InputStream, Void> mockResult = mock(Result.class);
    doReturn(new ByteArrayInputStream(examplePayload.getBytes())).when(mockResult).getOutput();
    doReturn(mockResult).when(extensionsClient).execute(eq("Crypto"), eq("jceEncryptPbe"), any());

    // mock response from convertToByteArray
    doReturn(examplePayload.getBytes()).when(spyCustomLoggerOperation)
        .convertToByteArray(any(), any());

    // call method
    spyCustomLoggerOperation.setEncryptedPayloadIfNeeded(spyLogProperties,
        loggerConfig,
        mock(StreamingHelper.class),
        executeCompress);

    // assertions
    verify(extensionsClient, atMost(1)).execute(any(), any(), any());
    Assert.assertEquals("RXhhbXBsZSBQYXlsb2FkIFRleHQ=", spyLogProperties.getEncryptedPayload());
  }

  @Test
  public void log_encryptedPayloadIsSet_compressionNotNull_test() throws MuleException {
    // mock and spy inits
    CustomLoggerOperation customLoggerOperation = new CustomLoggerOperation();
    CustomLoggerOperation spyCustomLoggerOperation = spy(customLoggerOperation);
    LogProperties logProperties = new LogProperties();
    LogProperties spyLogProperties = spy(logProperties);
    ExtensionsClient extensionsClient = mock(ExtensionsClient.class);
    spyCustomLoggerOperation.extensionsClient = extensionsClient;
    ParameterResolver<String> resolver = mock(ParameterResolver.class);
    String password = "example-password";

    // create mock for custom logger and mock log() method so it's never called
    CustomLoggerConfiguration loggerConfig = mock(CustomLoggerConfiguration.class);
    CustomLogger logger = mock(CustomLogger.class);
    doNothing().when(logger).log(any(), any(), any(), any(), any(), any(), any());
    when(loggerConfig.getLogger()).thenReturn(logger);

    // Mock payload and encryption algorithm values, compression is null
    when(resolver.resolve()).thenReturn(examplePayload);
    when(spyLogProperties.getPayload()).thenReturn(resolver);
    when(loggerConfig.getEncryptionAlgorithm()).thenReturn(EncryptionAlgorithm.PBEWithHmacSHA1AndAES_128);
    when(loggerConfig.getEncryptionPassword()).thenReturn(password);

    // mock response from extensions client
    Result<InputStream, Void> executeCompress = mock(Result.class);
    doReturn(new ByteArrayInputStream(examplePayload.getBytes())).when(executeCompress).getOutput();
    doReturn(executeCompress).when(extensionsClient).execute(eq("Crypto"), eq("jceEncryptPbe"), any());

    // mock response from convertToByteArray
    doReturn(examplePayload.getBytes()).when(spyCustomLoggerOperation)
        .convertToByteArray(any(), any());

    // call method
    spyCustomLoggerOperation.setEncryptedPayloadIfNeeded(spyLogProperties,
        loggerConfig,
        mock(StreamingHelper.class),
        executeCompress);

    // assertions
    verify(extensionsClient, atMost(1)).execute(any(), any(), any());
    Assert.assertEquals("RXhhbXBsZSBQYXlsb2FkIFRleHQ=", spyLogProperties.getEncryptedPayload());
  }
}
