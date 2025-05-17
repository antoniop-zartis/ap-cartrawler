package com.cartrawler.assessment.enums;

public enum Category {

    MINI("Mini", 'M'),
    ECONOMY("Economy", 'E'),
    COMPACT("Compact", 'C'),
    OTHER("Other", null);

    private final String label;
    private final Character sippPrefix;

    Category(String label, Character sippPrefix) {
        this.label = label;
        this.sippPrefix = sippPrefix;
    }

    public String label() {
        return label;
    }

    /**
     * maps first SIPP letter → Category
     */
    public static Category fromSipp(String sipp) {
        if (sipp == null || sipp.isBlank()) return OTHER;
        char c = Character.toUpperCase(sipp.charAt(0));
        for (Category cat : values()) {
            if (cat.sippPrefix != null && cat.sippPrefix == c) return cat;
        }
        return OTHER;
    }
}