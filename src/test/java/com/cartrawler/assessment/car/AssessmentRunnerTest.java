package com.cartrawler.assessment.car;

import com.cartrawler.assessment.enums.Category;
import com.cartrawler.assessment.enums.Supplier;
import com.cartrawler.assessment.util.CarsUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cartrawler.assessment.car.AssessmentRunner.process;
import static com.cartrawler.assessment.data.CarDataProvider.loadAllCars;
import static com.cartrawler.assessment.util.CarsUtils.filterFullAboveMedianPrice;
import static com.cartrawler.assessment.util.CarsUtils.median;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AssessmentRunnerTest {

    @Test
    public void testDuplicatesRemovedAndUniqueKeys() {
        Set<CarResult> raw = loadAllCars();
        List<CarResult> out = process(raw);

        assertThat(out).hasSizeLessThan(raw.size());

        Function<CarResult, String> keyFn = CarsUtils::compositeKey;
        assertThat(out.stream().map(keyFn).collect(Collectors.toSet()))
                .hasSize(out.size());
    }

    @Test
    public void testOrderingRulesRespected() {
        List<CarResult> list = process(loadAllCars());

        int firstNonCorp = -1;
        for (int i = 0; i < list.size(); i++) {
            if (!Supplier.isCorporate(list.get(i).getSupplierName())) {
                firstNonCorp = i;
                break;
            }
        }
        assertThat(firstNonCorp).isPositive();

        list.subList(0, firstNonCorp)
                .forEach(c -> assertThat(Supplier.isCorporate(c.getSupplierName())).isTrue());
        list.subList(firstNonCorp, list.size())
                .forEach(c -> assertThat(Supplier.isCorporate(c.getSupplierName())).isFalse());

        List<Category> order = List.of(Category.values());
        boolean inCorporate = true;
        int lastCatOrd = -1;
        double lastCost = -1;

        for (CarResult car : list) {
            boolean corp = Supplier.isCorporate(car.getSupplierName());

            if (inCorporate && !corp) {
                inCorporate = false;
                lastCatOrd = -1;
                lastCost = -1;
            }

            int ord = Category.fromSipp(car.getSippCode()).ordinal();
            assertThat(ord).isGreaterThanOrEqualTo(lastCatOrd);

            if (ord > lastCatOrd) {
                lastCatOrd = ord;
                lastCost = -1;
            }

            assertThat(car.getRentalCost()).isGreaterThanOrEqualTo(lastCost);
            lastCost = car.getRentalCost();
        }
    }

    @Test
    public void testMedianFilterByGroup() {
        List<CarResult> sorted = process(loadAllCars());
        List<CarResult> filtered = filterFullAboveMedianPrice(sorted);

        double medCorp = median(sorted.stream()
                .filter(c -> Supplier.isCorporate(c.getSupplierName()))
                .map(CarResult::getRentalCost)
                .toList());

        double medNon = median(sorted.stream()
                .filter(c -> !Supplier.isCorporate(c.getSupplierName()))
                .map(CarResult::getRentalCost)
                .toList());

        filtered.forEach(car -> {
            if (car.getFuelPolicy() == CarResult.FuelPolicy.FULLFULL) {
                double limit = Supplier.isCorporate(car.getSupplierName()) ? medCorp : medNon;
                assertThat(car.getRentalCost()).isLessThanOrEqualTo(limit);
            }
        });
    }

}