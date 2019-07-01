package com.avio.customlogger.internal.model;

public class tracePointProperty {
    /**
     * Log Trace points
     */
    public enum tracePoint {

        START("01"),
        END("02"),
        BEFORE_REQUEST("03"),
        AFTER_REQUEST("04"),
        BEFORE_TRANSFORM("05"),
        AFTER_TRANSFORM("06"),
        FLOW("07"),
        EXCEPTION("08");

        private final String trace_point;

        tracePoint(String trace_point) {
            this.trace_point = trace_point;
        }

        public String trace_point() {
            return trace_point;
        }
    }
}