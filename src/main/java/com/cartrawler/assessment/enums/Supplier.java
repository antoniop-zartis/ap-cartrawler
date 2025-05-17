package com.cartrawler.assessment.enums;

import lombok.Getter;

@Getter
public enum Supplier {

    AVIS(true),
    BUDGET(true),
    ENTERPRISE(true),
    FIREFLY(true),
    HERTZ(true),
    SIXT(true),
    THRIFTY(true);

    private final boolean corporate;

    Supplier(boolean corporate) {
        this.corporate = corporate;
    }

    public static boolean isCorporate(String supplierName) {
        if (supplierName == null) {
            return false;
        }
        try {
            return Supplier.valueOf(supplierName.toUpperCase()).isCorporate();
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}