package com.cartrawler.assessment.car;

import com.cartrawler.assessment.enums.Category;
import com.cartrawler.assessment.enums.Supplier;
import com.cartrawler.assessment.util.CarsUtils;
import com.cartrawler.assessment.view.Display;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cartrawler.assessment.data.CarDataProvider.loadAllCars;
import static com.cartrawler.assessment.enums.Category.fromSipp;
import static com.cartrawler.assessment.util.CarsUtils.filterFullAboveMedianPrice;
import static com.cartrawler.assessment.util.CarsUtils.removeDuplicates;

@Slf4j
public class AssessmentRunner {
    // TODO: make this an enum?
    private static final List<Category> CATEGORIES = List.of(Category.values());

    /**
     * Processes the given car results by removing duplicates,
     * segmenting corporate vs non-corporate, grouping by SIPP category,
     * and sorting by rental cost within each category.
     */
    public static List<CarResult> process(Set<CarResult> cars) {
        // 1. Remove duplicates
        List<CarResult> uniqueCars = removeDuplicates(cars, CarsUtils::compositeKey);

        // 2. Partition corporate vs non-corporate
        List<CarResult> corporate = uniqueCars.stream()
                .filter(car -> Supplier.isCorporate(car.getSupplierName()))
                .toList();
        List<CarResult> nonCorporate = uniqueCars.stream()
                .filter(car -> !Supplier.isCorporate(car.getSupplierName()))
                .toList();

        // 3. Sort each partition by category and cost
        Stream<CarResult> sortedCorporate   = CATEGORIES.stream()
                .flatMap(cat -> corporate.stream()
                        .filter(car -> fromSipp(car.getSippCode()) == cat)
                        .sorted(Comparator.comparingDouble(CarResult::getRentalCost)));

        Stream<CarResult> sortedNonCorporate = CATEGORIES.stream()
                .flatMap(cat -> nonCorporate.stream()
                        .filter(car -> fromSipp(car.getSippCode()) == cat)
                        .sorted(Comparator.comparingDouble(CarResult::getRentalCost)));
        // 4. Combine and return
        return Stream.concat(sortedCorporate, sortedNonCorporate)
                .collect(Collectors.toList());
    }

    public static void process() {
        Display display = new Display();
        HashSet<CarResult> cars = loadAllCars();
        log.atInfo()
                .addArgument(cars.size())
                .log("Original list size: {}");
        // Invoke processing before rendering
        List<CarResult> processed = process(cars);
        log.atInfo()
                .addArgument(processed.size())
                .log("DeDuplicated, sorted and group list size: {}");
        display.render(new LinkedHashSet<>(processed));
        List<CarResult> filtered = filterFullAboveMedianPrice(processed);
        log.atInfo()
                .addArgument(filtered.size())
                .log("Over priced vehicles removed list size: {}");
        // Use LinkedHashSet to preserve order
        display.render(new LinkedHashSet<>(filtered));
    }
}
