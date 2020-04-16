package com.avio.customlogger.internal;

import com.avio.customlogger.internal.engine.CustomLoggerNotificationListener;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Export;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;


/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "avio-core")
@Extension(name = "AVIO Core")
@Configurations(CustomLoggerConfiguration.class)
@Export(classes = CustomLoggerNotificationListener.class)
public class CustomLoggerExtension {

}
