package com.avioconsulting.mule.logger.internal;

import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomLoggerRegistrationService {

  private static final Logger logger = LoggerFactory.getLogger(CustomLoggerRegistrationService.class);

  private CustomLoggerConfiguration config;

  public CustomLoggerRegistrationService() {
    logger.info("Creating logger registration service...");
  }

  public CustomLoggerConfiguration getConfig() {
    return config;
  }

  public void setConfig(CustomLoggerConfiguration config) {
    this.config = config;
  }
}
