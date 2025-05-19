package com.cartrawler.assessment.util;

import com.cartrawler.assessment.car.CarResult;
import com.cartrawler.assessment.enums.Category;
import com.cartrawler.assessment.enums.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;


@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CarsUtils {

    public static List<CarResult> removeDuplicates(List<CarResult> cars) {
        return new ArrayList<>(new LinkedHashSet<>(cars));
    }

    /**
     * Derives a SIPP category label for a CarResult based on the first SIPP character.
     */
    public static Category categoryOf(CarResult car) {
        return Category.fromSipp(car.getSippCode());
    }

    /**
     * Optional step – remove every FULLFULL car whose rentalCost is
     * *strictly above* the median price of its own group
     * (corporate vs non-corporate).
     * <p>
     * The input list order is preserved.
     */
    public static List<CarResult> filterFullAboveMedianPrice(List<CarResult> cars) {

        List<CarResult> corporate = cars.stream()
                .filter(car -> Supplier.isCorporate(car.getSupplierName()))
                .toList();
        List<CarResult> nonCorporate = cars.stream()
                .filter(car -> !Supplier.isCorporate(car.getSupplierName()))
                .toList();


        double medianCorporate = corporate.isEmpty()
                ? Double.POSITIVE_INFINITY
                : median(corporate.stream()
                .map(CarResult::getRentalCost)
                .toList());

        double medianNonCorporate = nonCorporate.isEmpty()
                ? Double.POSITIVE_INFINITY
                : median(nonCorporate.stream()
                .map(CarResult::getRentalCost)
                .toList());

        // Single pass over the already-ordered list
        return cars.stream()
                .filter(car -> {
                    boolean fullFull = car.getFuelPolicy() == CarResult.FuelPolicy.FULLFULL;
                    boolean corporateFlag = Supplier.isCorporate(car.getSupplierName());
                    double threshold = corporateFlag ? medianCorporate : medianNonCorporate;
                    boolean isRemoved = fullFull && car.getRentalCost() > threshold;
                    if (isRemoved) {
                        log.atInfo()
                                .addArgument(car)
                                .log("{} will be skipped");
                    }
                    return !isRemoved;
                })
                .toList();
    }

    /**
     * Computes the median of a list of values. If the list has an even size,
     * the median is the average of the two middle elements.
     */
    public static double median(List<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("List must not be null or empty");
        }
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int n = sorted.size();
        if (n % 2 == 1) {
            return sorted.get(n / 2);
        } else {
            return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
        }
    }


}
