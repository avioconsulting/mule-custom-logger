package com.lamar.customlogger.internal;

import org.mule.runtime.extension.api.annotation.Operations;
//import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import com.lamar.customlogger.internal.operation.LamarLoggerOperations;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations(LamarLoggerOperations.class)
//@ConnectionProviders(LamarLoggerConnectionProvider.class)
public class LamarLoggerConfiguration {

  @Parameter
  private String configId;

  public String getConfigId(){
    return configId;
  }
}
