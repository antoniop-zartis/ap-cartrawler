package com.cartrawler.assessment.util;

import com.cartrawler.assessment.car.CarResult;
import org.testng.annotations.Test;

import java.util.List;

import static com.cartrawler.assessment.car.CarResult.FuelPolicy.FULLFULL;
import static com.cartrawler.assessment.enums.Category.COMPACT;
import static com.cartrawler.assessment.enums.Category.ECONOMY;
import static com.cartrawler.assessment.enums.Category.MINI;
import static com.cartrawler.assessment.enums.Category.OTHER;
import static com.cartrawler.assessment.util.CarsUtils.categoryOf;
import static com.cartrawler.assessment.util.CarsUtils.filterFullAboveMedianPrice;
import static com.cartrawler.assessment.util.CarsUtils.median;
import static com.cartrawler.assessment.util.CarsUtils.removeDuplicates;
import static org.assertj.core.api.Assertions.assertThat;

public class CarsUtilsTest {

    @Test
    public void testRemoveDuplicates() {
        CarResult car1 = new CarResult("A", "SUP", "CDMR", 10, FULLFULL);
        CarResult duplicate = new CarResult("A", "SUP", "CDMR", 20, FULLFULL);
        CarResult car2 = new CarResult("B", "OTH", "EDMR", 30, CarResult.FuelPolicy.FULLEMPTY);

        List<CarResult> out = removeDuplicates(List.of(car1, duplicate, car2));

        assertThat(out).hasSize(2)
                .containsExactly(car1, car2);
    }

    @Test
    public void testCategoryOf() {
        assertThat(categoryOf(new CarResult("", "", "MDMR", 0, FULLFULL))).isEqualTo(MINI);
        assertThat(categoryOf(new CarResult("", "", "EDMR", 0, FULLFULL))).isEqualTo(ECONOMY);
        assertThat(categoryOf(new CarResult("", "", "CDMR", 0, FULLFULL))).isEqualTo(COMPACT);
        assertThat(categoryOf(new CarResult("", "", "XDMR", 0, FULLFULL))).isEqualTo(OTHER);
    }

    @Test
    public void testMedianOddAndEven() {
        assertThat(median(List.of(1d, 3d, 2d))).isEqualTo(2d);
        assertThat(median(List.of(1d, 4d, 2d, 3d))).isEqualTo(2.5d);
    }

    @Test
    public void testFilterFullAboveMedianPrice_byGroup() {
        CarResult corpLow = new CarResult("A", "SIXT", "MDMR", 10, FULLFULL);
        CarResult corpHigh = new CarResult("B", "SIXT", "MDMR", 40, FULLFULL);

        CarResult nonCorp = new CarResult("C", "NIZA", "EDMR", 30, FULLFULL);

        List<CarResult> filtered = filterFullAboveMedianPrice(List.of(corpLow, corpHigh, nonCorp));

        assertThat(filtered).containsExactly(corpLow, nonCorp);
    }

    @Test
    public void testFilterFullAboveMedianPrice_keepsNonFullFull() {
        CarResult corpEmpty = new CarResult("A", "AVIS", "MDMR", 100, CarResult.FuelPolicy.FULLEMPTY);
        CarResult corpFull = new CarResult("B", "AVIS", "MDMR", 50, FULLFULL);

        List<CarResult> out = filterFullAboveMedianPrice(List.of(corpEmpty, corpFull));

        assertThat(out).containsExactly(corpEmpty, corpFull);
    }

    @Test
    public void testFilterFullAboveMedianPrice_noCorporate() {
        CarResult n1 = new CarResult("A", "NIZA", "MDMR", 20, FULLFULL);
        CarResult n2 = new CarResult("B", "NIZA", "MDMR", 25, FULLFULL);

        assertThat(filterFullAboveMedianPrice(List.of(n1, n2)))
                .containsExactly(n1);
    }
}