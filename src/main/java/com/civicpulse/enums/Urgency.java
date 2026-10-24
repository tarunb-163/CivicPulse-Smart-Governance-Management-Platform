package com.civicpulse.enums;

public enum Urgency {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int weight;

    Urgency(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    @com.fasterxml.jackson.annotation.JsonCreator
    public static Urgency fromValue(Object value) {
        if (value == null) return Urgency.MEDIUM;
        String str = value.toString().trim();
        return switch (str) {
            case "1" -> LOW;
            case "2" -> MEDIUM;
            case "3" -> HIGH;
            case "4" -> CRITICAL;
            default -> {
                try {
                    yield Urgency.valueOf(str.toUpperCase());
                } catch (Exception e) {
                    yield MEDIUM;
                }
            }
        };
    }
}
