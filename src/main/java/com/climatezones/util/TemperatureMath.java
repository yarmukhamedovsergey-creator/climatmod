package com.climatezones.util;

public final class TemperatureMath {
    private TemperatureMath() {
    }

    public static float lerp(float from, float to, float delta) {
        if (Math.abs(from - to) < 0.001f) {
            return to;
        }
        return from + (to - from) * delta;
    }

    public static float approach(float current, float target, float maxDelta) {
        if (current < target) {
            return Math.min(current + maxDelta, target);
        }
        return Math.max(current - maxDelta, target);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static String formatTemperature(float celsius) {
        String sign = celsius >= 0 ? "+" : "";
        return sign + String.format("%.1f", celsius) + "\u00B0C";
    }

    public static float blend(float ambient, float zoneValue, float influence) {
        return ambient + (zoneValue - ambient) * influence;
    }
}
