package com.avioconsulting.mule.logger.internal.utils;

import com.avioconsulting.mule.logger.api.processor.LogProperties;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Map;

public class LogPropertiesMap {

	private LogPropertiesMap() {
	}

	public static final Map<LogProperties.LogLevel, Level> levelMap = new HashMap<>();

	static {
			levelMap.put(LogProperties.LogLevel.INFO, Level.INFO);
			levelMap.put(LogProperties.LogLevel.DEBUG, Level.DEBUG);
			levelMap.put(LogProperties.LogLevel.TRACE, Level.TRACE);
			levelMap.put(LogProperties.LogLevel.ERROR, Level.ERROR);
			levelMap.put(LogProperties.LogLevel.WARN, Level.WARN);
			levelMap.put(LogProperties.LogLevel.FATAL, Level.FATAL);
	}
}
