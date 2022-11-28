package com.avioconsulting.mule.logger.internal;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;
import org.mule.runtime.extension.api.client.ExtensionsClient;

public class CustomLoggerOperationTest {

  @Test
  public void log() {
    CustomLoggerOperation customLoggerOperation = new CustomLoggerOperation();
    ExtensionsClient mock = Mockito.mock(ExtensionsClient.class);
    customLoggerOperation.extensionsClient = mock;
    // customLoggerOperation.log();
    // Mockito.verify(mock).execute();

  }
}
