package ru.hse.ledger;


import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.hse.ledger.domain.Bicycle;
import ru.hse.ledger.domain.ChargingCabinet;
import ru.hse.ledger.domain.ElectricBicycle;
import ru.hse.ledger.domain.Energy;
import ru.hse.ledger.domain.Helmet;
import ru.hse.ledger.domain.Roadworthiness;
import ru.hse.ledger.domain.Scooter;
import ru.hse.ledger.inspection.Inspection;
import ru.hse.ledger.inspection.SafetyInspection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainTest {
    private static final Roadworthiness GOOD = new Roadworthiness(true, true);

    @ParameterizedTest
    @CsvSource({"true,true,true", "false,true,false", "true,false,false", "false,false,false"})
    void inspectionChecksBothSafetyConditions(boolean brakes, boolean wheels, boolean accepted) {
        var result = new SafetyInspection().examine(new Bicycle("B", "Тест", 5,
                new Roadworthiness(brakes, wheels)));
        assertEquals(accepted, result.approved());
        assertEquals(!brakes, result.explanation().contains("тормоза"));
        assertEquals(!wheels, result.explanation().contains("колёса"));
        assertFalse(result.explanation().isBlank());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 11, 100})
    void easeMustBeOnScale(int ease) {
        assertThrows(IllegalArgumentException.class, () -> new Bicycle("B", "Тест", ease, GOOD));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 6, 10})
    void easeBoundariesAreValid(int ease) {
        assertEquals(ease, new Bicycle("B", "Тест", ease, GOOD).ease());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void requiredFieldsRejectBlank(String value) {
        assertThrows(IllegalArgumentException.class, () -> new Helmet(value, "Шлем"));
        assertThrows(IllegalArgumentException.class, () -> new Helmet("H", value));
    }

    @Test
    void nullValuesAreRejected() {
        assertThrows(NullPointerException.class, () -> new Helmet(null, "H"));
        assertThrows(NullPointerException.class, () -> new Helmet("H", null));
        assertThrows(NullPointerException.class, () -> new Bicycle("B", "Тест", 1, null));
        assertThrows(NullPointerException.class, () -> Energy.validate(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.001", "1000000.001", "0.0001", "1E+2147483647",
            "1E-2147483647", "0.1234567890123456789012345678901234"})
    void energyRejectsOutOfRangeOrUnrepresentableValues(String value) {
        assertThrows(IllegalArgumentException.class, () -> Energy.validate(new BigDecimal(value)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.001", "1000000", "1.2500", "0E-2147483647"})
    void energyHasExactFixedScale(String value) {
        BigDecimal result = Energy.validate(new BigDecimal(value));
        assertEquals(3, result.scale());
        assertEquals(0, result.compareTo(new BigDecimal(value)));
    }

    @Test
    void concreteObjectsExposeTheirInventoryAndEnergy() {
        var bike = new ElectricBicycle(" EB ", " Вектор ", 7, GOOD, BigDecimal.ONE);
        assertEquals("EB", bike.number());
        assertEquals("Вектор", bike.name());
        assertEquals(GOOD, bike.condition());
        assertEquals(new BigDecimal("1.000"), bike.dailyEnergy());
        assertEquals(new BigDecimal("2.000"),
                new Scooter("S", "Самокат", 8, GOOD, new BigDecimal("2")).dailyEnergy());
        assertEquals(new BigDecimal("0.000"),
                new ChargingCabinet("C", "Шкаф", BigDecimal.ZERO).dailyEnergy());
        assertTrue(new Helmet("h", "Шлем").name().contains("Шлем"));
    }

    @Test
    void inspectionMustExplainItsResult() {
        assertThrows(IllegalArgumentException.class, () -> new Inspection(false, " "));
        assertThrows(IllegalArgumentException.class, () -> new Inspection(true, null));
    }
}
