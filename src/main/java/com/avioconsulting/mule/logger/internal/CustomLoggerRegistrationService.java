package com.avioconsulting.mule.logger.internal;

import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class CustomLoggerRegistrationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomLoggerRegistrationService.class);

  private CustomLoggerConfiguration config;

  public CustomLoggerRegistrationService() {
    LOGGER.info("Creating logger registration service...");
  }

}
