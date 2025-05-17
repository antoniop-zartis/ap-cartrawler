package com.cartrawler.assessment.car;

import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import com.cartrawler.assessment.util.CarsUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cartrawler.assessment.car.AssessmentRunner.*;
import static com.cartrawler.assessment.data.CarDataProvider.loadAllCars;
import static com.cartrawler.assessment.util.CarsUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AssessmentRunnerTest {

    @Test
    public void testDuplicatesRemovedAndUniqueKeys() {
        Set<CarResult> rawCars = loadAllCars();
        List<CarResult> processed = process(rawCars);

        assertThat(processed).hasSizeLessThan(rawCars.size());

        Function<CarResult, String> keyFn = CarsUtils::compositeKey;
        Set<String> keys = processed.stream().map(keyFn).collect(Collectors.toSet());
        assertThat(keys).hasSize(processed.size());

        Map<String, Long> dupCounts = rawCars.stream()
                .collect(Collectors.groupingBy(keyFn, Collectors.counting()));
        dupCounts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .forEach(e -> {
                    long cnt = processed.stream().filter(c -> keyFn.apply(c).equals(e.getKey())).count();
                    assertThat(cnt).isEqualTo(1);
                });
    }

    @Test
    public void testOrderingRulesRespected() {
        List<CarResult> sorted = process(loadAllCars());

        int firstNonCorpIdx = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (!CORPORATE_SUPPLIERS.contains(sorted.get(i).getSupplierName())) {
                firstNonCorpIdx = i;
                break;
            }
        }
        assertThat(firstNonCorpIdx).isPositive();
        sorted.subList(0, firstNonCorpIdx)
                .forEach(c -> assertThat(CORPORATE_SUPPLIERS).contains(c.getSupplierName()));

        sorted.subList(firstNonCorpIdx, sorted.size())
                .forEach(c -> assertThat(CORPORATE_SUPPLIERS).doesNotContain(c.getSupplierName()));

        boolean inCorporate = true;
        int lastCatOrdinal = -1;
        double lastCost = -1.0;

        for (CarResult car : sorted) {
            boolean isCorporate = CORPORATE_SUPPLIERS.contains(car.getSupplierName());

            if (inCorporate && !isCorporate) {
                inCorporate = false;
                lastCatOrdinal = -1;
                lastCost = -1.0;
            }

            int ord = CATEGORIES.indexOf(categoryOf(car));
            assertThat(ord).isGreaterThanOrEqualTo(lastCatOrdinal);

            if (ord > lastCatOrdinal) {
                lastCatOrdinal = ord;
                lastCost = -1.0;
            }

            assertThat(car.getRentalCost()).isGreaterThanOrEqualTo(lastCost);
            lastCost = car.getRentalCost();
        }
    }

    @Test
    public void testMedianFilterByGroup() {
        List<CarResult> sorted = process(loadAllCars());

        List<CarResult> filtered = filterFullAboveMedianPrice(sorted);

        double medianCorp = median(sorted.stream()
                .filter(c -> CORPORATE_SUPPLIERS.contains(c.getSupplierName()))
                .map(CarResult::getRentalCost)
                .toList());

        double medianNonCorp = median(sorted.stream()
                .filter(c -> !CORPORATE_SUPPLIERS.contains(c.getSupplierName()))
                .map(CarResult::getRentalCost)
                .toList());

        filtered.forEach(car -> {
            if (car.getFuelPolicy() == CarResult.FuelPolicy.FULLFULL) {
                double thresh = CORPORATE_SUPPLIERS.contains(car.getSupplierName())
                        ? medianCorp
                        : medianNonCorp;
                assertThat(car.getRentalCost()).isLessThanOrEqualTo(thresh);
            }
        });
    }
}