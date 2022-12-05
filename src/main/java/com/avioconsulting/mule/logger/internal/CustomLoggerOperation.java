package com.avioconsulting.mule.logger.internal;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import com.avioconsulting.mule.logger.api.processor.*;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import com.mulesoft.modules.cryptography.api.jce.config.JceEncryptionPbeAlgorithm;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.inject.Inject;
import org.mule.extension.compression.api.strategy.CompressorStrategy;
import org.mule.extension.compression.api.strategy.gzip.GzipCompressorStrategy;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStream;
import org.mule.runtime.core.api.util.Base64;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.client.DefaultOperationParameters;
import org.mule.runtime.extension.api.client.DefaultOperationParametersBuilder;
import org.mule.runtime.extension.api.client.ExtensionsClient;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;

/**
 * Author: Chakri Bonthala
 */
public class CustomLoggerOperation {

  @Inject
  ExtensionsClient extensionsClient;

  @MediaType(value = ANY, strict = false)
  public void log(@ParameterGroup(name = "Log") LogProperties logProperties,
      @ParameterGroup(name = "Message Attributes") MessageAttributes messageAttributes,
      @ParameterGroup(name = "Exception Details") ExceptionProperties exceptionProperties,
      @ParameterGroup(name = "Additional Options") AdditionalProperties additionalProperties,
      @Config CustomLoggerConfiguration loggerConfig,
      ComponentLocation location,
      CorrelationInfo correlationInfo,
      StreamingHelper streamingHelper) {

    transformPayload(logProperties, loggerConfig, streamingHelper);
    loggerConfig.getLogger().log(logProperties, messageAttributes, exceptionProperties, additionalProperties,
        loggerConfig, location, correlationInfo.getCorrelationId());
  }

  /**
   * Convert payload object to Byte Array when payload resolves to a
   * CursorProvider instance
   * otherwise, return null
   *
   * @since 2.1.0
   * @return byte array or null
   */
  byte[] convertToByteArray(Object payload, StreamingHelper streamingHelper) {
    Object resolved = streamingHelper.resolveCursorProvider(payload);
    if (resolved instanceof CursorProvider) {
      CursorStream cursorStream = ((CursorProvider<CursorStream>) resolved).openCursor();
      return IOUtils.toByteArray(cursorStream);
    } else {
      return null;
    }
  }

  /**
   * Make transformations to payload based on logger configuration
   *
   * @since 2.1.0
   */
  void transformPayload(LogProperties logProperties, CustomLoggerConfiguration loggerConfig,
      StreamingHelper streamingHelper) {
    ParameterResolver<String> payload = logProperties.getPayload();
    if (payload == null)
      return;
    Result<InputStream, Void> executeCompress = null;
    executeCompress = setCompressedPayloadIfNeeded(logProperties, streamingHelper, loggerConfig);
    setEncryptedPayloadIfNeeded(logProperties, loggerConfig, streamingHelper, executeCompress);
  }

  /**
   * If compressor configuration is not null, build request to mule compression
   * module
   * utilizing extensionsClient and set the compressed payload in logProperties to
   * base64 string
   *
   * @since 2.1.0
   * @return Compressed stream
   */
  Result<InputStream, Void> setCompressedPayloadIfNeeded(LogProperties logProperties,
      StreamingHelper streamingHelper,
      CustomLoggerConfiguration loggerConfig) {
    /*
     * If compressor is not null, compress payload with provided compressor strategy
     */
    Result<InputStream, Void> executeCompress = null;
    Compressor compressor = loggerConfig.getCompressor();
    if (compressor != null) {
      CompressorStrategy compressorStrategy = new GzipCompressorStrategy();
      DefaultOperationParametersBuilder parametersBuilder = DefaultOperationParameters.builder()
          .addParameter("content", new ByteArrayInputStream(logProperties.getPayload().resolve().getBytes()))
          .addParameter("compressor", compressorStrategy);
      try {
        executeCompress = extensionsClient.execute("Compression", "compress",
            parametersBuilder.build());
        String compressedString = Base64
            .encodeBytes(convertToByteArray(executeCompress.getOutput(), streamingHelper));
        logProperties.setCompressedPayload(compressedString);
      } catch (Exception e) {
        throw new RuntimeException("Compression Exception", e);
      }
    }
    return executeCompress;
  }

  /**
   * If encryption algorithm configuration is not null, build request to mule
   * crypto module
   * utilizing extensionsClient and set the encryption payload in logProperties to
   * base64 string
   *
   * @since 2.1.0
   */
  void setEncryptedPayloadIfNeeded(LogProperties logProperties, CustomLoggerConfiguration loggerConfig,
      StreamingHelper streamingHelper, Result<InputStream, Void> executeCompress) {
    /*
     * If encryption algorithm is not null, encrypt the payload
     * Check if payload has already been compressed, if so encyrpt the compressed
     * string.
     * If no compression, encrypt raw payload
     */
    EncryptionAlgorithm encryptionAlgorithm = loggerConfig.getEncryptionAlgorithm();
    if (encryptionAlgorithm != null) {
      JceEncryptionPbeAlgorithm jceEncryptionPbeAlgorithm = JceEncryptionPbeAlgorithm
          .valueOf(encryptionAlgorithm.toString());
      InputStream content;
      if (executeCompress != null) {
        content = new ByteArrayInputStream(
            convertToByteArray(executeCompress.getOutput(), streamingHelper));
      } else {
        content = new ByteArrayInputStream(logProperties.getPayload().resolve().getBytes());
      }
      DefaultOperationParametersBuilder encryptionParametersBuilder = DefaultOperationParameters.builder()
          .addParameter("content", content)
          .addParameter("algorithm", jceEncryptionPbeAlgorithm)
          .addParameter("password", loggerConfig.getEncryptionPassword());
      try {
        Result<InputStream, Void> executeEncrypt = extensionsClient.execute("Crypto", "jceEncryptPbe",
            encryptionParametersBuilder.build());
        String encryptedString = Base64
            .encodeBytes(convertToByteArray(executeEncrypt.getOutput(), streamingHelper));
        logProperties.setEncryptedPayload(encryptedString);
      } catch (Exception e) {
        throw new RuntimeException("Encryption Error", e);
      }
    }
  }
}
