package com.avio.customlogger.internal.model;


public class LoggerLevelProperty {

    /**
     * Log levels
     */
    public enum LogLevel {

        FATAL("01"),
        ERROR("02"),
        WARN("03"),
        INFO("04"),
        DEBUG("05"),
        TRACE("06");

        private final String logLevel;

        LogLevel(String logLevel) {
            this.logLevel = logLevel;
        }

        public String logLevel() {
            return logLevel;
        }
    }

}
