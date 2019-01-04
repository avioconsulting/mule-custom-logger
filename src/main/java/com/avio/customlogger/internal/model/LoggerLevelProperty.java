package com.avio.customlogger.internal.model;


public class LoggerLevelProperty {

    /**
     * Log levels
     */
    public enum LogLevel {

        ERROR("01"),
        INFO("02"),
        DEBUG("03"),
        WARN("04"),
        TRACE("05");

        private String logLevel;

        LogLevel(String logLevel) {
            this.logLevel = logLevel;
        }

        public String logLevel() {
            return logLevel;
        }
    }

}
