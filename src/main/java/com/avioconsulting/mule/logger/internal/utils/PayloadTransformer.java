package com.avioconsulting.mule.logger.internal.utils;

import com.avioconsulting.mule.logger.api.processor.Compressor;
import com.avioconsulting.mule.logger.api.processor.EncryptionAlgorithm;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import com.mulesoft.modules.cryptography.api.jce.config.JceEncryptionPbeAlgorithm;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.mule.extension.compression.api.strategy.CompressorStrategy;
import org.mule.extension.compression.api.strategy.gzip.GzipCompressorStrategy;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStream;
import org.mule.runtime.core.api.util.Base64;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.client.DefaultOperationParameters;
import org.mule.runtime.extension.api.client.DefaultOperationParametersBuilder;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;

public class PayloadTransformer {

  /**
   * Convert payload object to Byte Array when payload resolves to a
   * CursorProvider instance
   * otherwise, return null
   *
   * @since 2.1.0
   * @return byte array or null
   */
  public byte[] convertToByteArray(Object payload, StreamingHelper streamingHelper) {
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
   * Will returned the transformed string value
   *
   * @since 2.1.0
   * @return String
   */
  public String transformPayload(CustomLoggerConfiguration loggerConfig,
      StreamingHelper streamingHelper, String payloadString) {
    Objects.requireNonNull(streamingHelper, "StreamingHelper cannot be null");
    if (payloadString == null)
      return payloadString;
    Compressor compressor = loggerConfig.getCompressor();
    Result<InputStream, Void> executeCompress = null;
    if (compressor != null) {
      executeCompress = compressPayload(loggerConfig, payloadString);
      try {
        payloadString = Base64.encodeBytes(convertToByteArray(executeCompress.getOutput(), streamingHelper));
      } catch (IOException e) {
        throw new RuntimeException("Exception while transforming payload", e);
      }
    }

    EncryptionAlgorithm encryptionAlgorithm = loggerConfig.getEncryptionAlgorithm();
    if (encryptionAlgorithm != null) {
      payloadString = encryptPayload(loggerConfig, streamingHelper, executeCompress, encryptionAlgorithm,
          payloadString);
    }

    return payloadString;
  }

  /**
   * Build request to mule compression
   * module
   * utilizing extensionsClient. Return the output of the compression module
   * Current GZIP is all that is supported
   *
   * @since 2.1.0
   * @return Compressed stream
   */
  public Result<InputStream, Void> compressPayload(CustomLoggerConfiguration loggerConfig,
      String payload) {
    Result<InputStream, Void> executeCompress = null;
    CompressorStrategy compressorStrategy = new GzipCompressorStrategy();
    DefaultOperationParametersBuilder parametersBuilder = DefaultOperationParameters.builder()
        .addParameter("content", new ByteArrayInputStream(payload.getBytes()))
        .addParameter("compressor", compressorStrategy);
    try {
      executeCompress = loggerConfig.getExtensionsClient().execute("Compression", "compress",
          parametersBuilder.build());
    } catch (Exception e) {
      throw new RuntimeException("Compression Exception", e);
    }
    return executeCompress;
  }

  /**
   * Build request to mule
   * crypto module utilizing extensionsClient and return the encrypted string. If
   * executeCompress (output of compression module)
   * is not null, it will be used
   * base64 string
   *
   * @since 2.1.0
   * @return encrypted string
   */
  public String encryptPayload(CustomLoggerConfiguration loggerConfig,
      StreamingHelper streamingHelper, Result<InputStream, Void> executeCompress,
      EncryptionAlgorithm encryptionAlgorithm, String payload) {
    /*
     * Encrypt the payload. Use the output of the compression (executeCompress) if
     * it's not empty. if it is empty,
     */
    JceEncryptionPbeAlgorithm jceEncryptionPbeAlgorithm = JceEncryptionPbeAlgorithm
        .valueOf(encryptionAlgorithm.toString());
    InputStream content;
    if (executeCompress != null) {
      content = new ByteArrayInputStream(
          convertToByteArray(executeCompress.getOutput(), streamingHelper));
    } else {
      content = new ByteArrayInputStream(payload.getBytes());
    }
    DefaultOperationParametersBuilder encryptionParametersBuilder = DefaultOperationParameters.builder()
        .addParameter("content", content)
        .addParameter("algorithm", jceEncryptionPbeAlgorithm)
        .addParameter("password", loggerConfig.getEncryptionPassword());
    try {
      Result<InputStream, Void> executeEncrypt = loggerConfig.getExtensionsClient().execute("Crypto",
          "jceEncryptPbe",
          encryptionParametersBuilder.build());
      return Base64
          .encodeBytes(convertToByteArray(executeEncrypt.getOutput(), streamingHelper));
    } catch (Exception e) {
      throw new RuntimeException("Encryption Error", e);
    }
  }
}
