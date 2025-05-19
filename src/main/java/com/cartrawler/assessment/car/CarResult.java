package com.cartrawler.assessment.car;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"supplierName","description","sippCode","fuelPolicy"})
public class CarResult {
    private final String description;
    private final String supplierName;
    private final String sippCode;
    private final double rentalCost;
    private final FuelPolicy fuelPolicy;

	public enum FuelPolicy {
        FULLFULL,
        FULLEMPTY}

    public CarResult(String description, String supplierName, String sipp, double cost, FuelPolicy fuelPolicy) {
        this.description = description;
        this.supplierName = supplierName;
        this.sippCode = sipp;
        this.rentalCost = cost;
        this.fuelPolicy = fuelPolicy;
    }

    public String toString() {
        return this.supplierName + " : " +
            this.description + " : " +
            this.sippCode + " : " +
            this.rentalCost + " : " +
            this.fuelPolicy;
    }
}
