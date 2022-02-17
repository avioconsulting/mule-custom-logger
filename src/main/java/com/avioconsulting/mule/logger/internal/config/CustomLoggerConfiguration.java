package com.avioconsulting.mule.logger.internal.config;

import com.avioconsulting.mule.logger.internal.CustomLoggerOperation;
import com.avioconsulting.mule.logger.internal.CustomLoggerTimerScopeOperations;
import com.avioconsulting.mule.logger.internal.listeners.CustomLoggerNotificationListener;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.notification.NotificationListenerRegistry;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import javax.inject.Inject;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations({CustomLoggerOperation.class, CustomLoggerTimerScopeOperations.class})
public class CustomLoggerConfiguration implements Startable {

    public static final String DEFAULT_CATEGORY_PREFIX = "com.avioconsulting.api";
    public static final String DEFAULT_APP_NAME = "#[app.name]";
    public static final String DEFAULT_ENV = "#[p('env')]";
    public static final String EXAMPLE_APP_VERSION = "#[p('appVersion')]";

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
    @Optional(defaultValue = DEFAULT_CATEGORY_PREFIX)
    private String defaultCategory;

    @Inject
    NotificationListenerRegistry notificationListenerRegistry;

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

    @Override
    public void start() throws MuleException {
        notificationListenerRegistry.registerListener(new CustomLoggerNotificationListener(this));
    }
}