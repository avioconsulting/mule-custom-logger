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

        private String tracePoint;

        tracePoint(String tracePoint) {
            this.tracePoint = tracePoint;
        }

        public String tracePoint() {
            return tracePoint;
        }
    }
}
