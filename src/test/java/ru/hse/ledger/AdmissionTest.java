package ru.hse.ledger;


import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.hse.ledger.domain.Bicycle;
import ru.hse.ledger.domain.ChargingCabinet;
import ru.hse.ledger.domain.Helmet;
import ru.hse.ledger.domain.Roadworthiness;
import ru.hse.ledger.domain.Scooter;
import ru.hse.ledger.inspection.Inspection;
import ru.hse.ledger.inspection.ServiceCenter;
import ru.hse.ledger.registry.AdmissionDesk;
import ru.hse.ledger.registry.AssetRegister;
import ru.hse.ledger.report.FleetReports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdmissionTest {
    private static final Roadworthiness GOOD = new Roadworthiness(true, true);

    @Test
    void containerInjectsSubstituteAndItsDecisionIsFinal() {
        AtomicInteger calls = new AtomicInteger();
        ServiceCenter stub = vehicle -> {
            calls.incrementAndGet();
            return new Inspection(vehicle.number().equals("YES"), "Решение тестовой станции");
        };
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(Bootstrap.class);
            context.registerBean("substitute", ServiceCenter.class, () -> stub,
                    definition -> definition.setPrimary(true));
            context.refresh();
            var desk = context.getBean(AdmissionDesk.class);
            // An unhealthy vehicle is accepted because the injected center approves it.
            assertTrue(desk.accept(new Bicycle("YES", "Тестовый", 6,
                    new Roadworthiness(false, false))).accepted());
            assertFalse(desk.accept(new Bicycle("NO", "Исправный", 10, GOOD)).accepted());
            assertEquals(2, calls.get());
            assertEquals(1, context.getBean(FleetReports.class).totals().total());
        }
    }

    @Test
    void rejectionLeavesNoInventoryOrEnergyAndNumberCanBeReused() {
        var register = new AssetRegister();
        ServiceCenter stub = vehicle -> new Inspection(vehicle.name().equals("Исправлен"), "Проверка");
        var desk = new AdmissionDesk(register, stub);
        var reports = new FleetReports(register);
        assertFalse(desk.accept(new Scooter("X", "Сломан", 10, GOOD, BigDecimal.TEN)).accepted());
        assertTrue(reports.inventory().isEmpty());
        assertTrue(reports.beginners().isEmpty());
        assertEquals(0, reports.totals().energy().signum());
        assertTrue(desk.accept(new Bicycle("X", "Исправлен", 6, GOOD)).accepted());
    }

    @Test
    void allKindsShareUniqueNumbersAndDuplicateSkipsInspection() {
        var register = new AssetRegister();
        AtomicInteger calls = new AtomicInteger();
        var desk = new AdmissionDesk(register, vehicle -> {
            calls.incrementAndGet();
            return new Inspection(true, "OK");
        });
        assertTrue(desk.accept(new Helmet(" 001 ", "Шлем")).accepted());
        assertFalse(desk.accept(new Bicycle("001", "Велосипед", 5, GOOD)).accepted());
        assertEquals(0, calls.get());
        assertFalse(desk.accept(new ChargingCabinet("001", "Шкаф", BigDecimal.ONE)).accepted());
        assertEquals("Шлем", register.snapshot().iterator().next().name());
    }

    @Test
    void reportsUseExactEnergyAndBeginnerBoundaryWithImmutableSnapshots() {
        var register = new AssetRegister();
        var desk = new AdmissionDesk(register, vehicle -> new Inspection(true, "OK"));
        var reports = new FleetReports(register);
        var before = reports.inventory();
        desk.accept(new Bicycle("A", "Сложный", 5, GOOD));
        desk.accept(new Scooter("B", "Порог", 6, GOOD, new BigDecimal("0.1")));
        desk.accept(new Scooter("C", "Простой", 10, GOOD, new BigDecimal("0.2")));
        desk.accept(new Helmet("D", "Шлем"));
        desk.accept(new ChargingCabinet("E", "Шкаф", new BigDecimal("0.3")));
        assertTrue(before.isEmpty());
        assertEquals(3, reports.totals().vehicles());
        assertEquals(2, reports.totals().equipment());
        assertEquals(5, reports.totals().total());
        assertEquals(new BigDecimal("0.600"), reports.totals().energy());
        assertEquals(java.util.List.of("B", "C"),
                reports.beginners().stream().map(vehicle -> vehicle.number()).toList());
        assertThrows(UnsupportedOperationException.class, () -> reports.inventory().clear());
        assertThrows(UnsupportedOperationException.class, () -> reports.beginners().clear());
    }

    @Test
    void standardContainerInspectsBeforeAdmission() {
        try (var context = new AnnotationConfigApplicationContext(Bootstrap.class)) {
            var desk = context.getBean(AdmissionDesk.class);
            assertFalse(desk.accept(new Bicycle("BAD", "Сломан", 10,
                    new Roadworthiness(false, true))).accepted());
            assertTrue(desk.accept(new Bicycle("OK", "Целый", 6, GOOD)).accepted());
            assertEquals(1, context.getBean(FleetReports.class).inventory().size());
        }
    }
    @Test
    void newElectricVehicleParticipatesInAllReportsWithoutSpecialCases() {
        class CargoTrike extends ru.hse.ledger.domain.ElectricVehicle {
            CargoTrike() {
                super("NEW", "Грузовой трицикл", 7, GOOD, new BigDecimal("0.4"));
            }
        }
        var register = new AssetRegister();
        var desk = new AdmissionDesk(register, vehicle -> new Inspection(true, "OK"));
        var reports = new FleetReports(register);
        assertTrue(desk.accept(new CargoTrike()).accepted());
        assertEquals(1, reports.totals().vehicles());
        assertEquals(new BigDecimal("0.400"), reports.totals().energy());
        assertEquals("NEW", reports.beginners().getFirst().number());
        assertEquals("Грузовой трицикл", reports.inventory().getFirst().name());
    }

}
