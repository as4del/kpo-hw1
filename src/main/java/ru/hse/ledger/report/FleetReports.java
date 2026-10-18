package ru.hse.ledger.report;


import java.math.BigDecimal;
import java.util.List;
import ru.hse.ledger.domain.Equipment;
import ru.hse.ledger.domain.Inventoried;
import ru.hse.ledger.domain.Powered;
import ru.hse.ledger.domain.Vehicle;
import ru.hse.ledger.registry.AssetRegister;

public final class FleetReports {
    private static final int BEGINNER_EASE = 6;
    private final AssetRegister register;

    public FleetReports(AssetRegister register) {
        this.register = register;
    }

    public FleetTotals totals() {
        var assets = register.snapshot();
        long vehicles = assets.stream().filter(Vehicle.class::isInstance).count();
        long equipment = assets.stream().filter(Equipment.class::isInstance).count();
        BigDecimal energy = assets.stream().filter(Powered.class::isInstance)
                .map(Powered.class::cast).map(Powered::dailyEnergy)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new FleetTotals(vehicles, equipment, energy);
    }

    public List<Vehicle> beginners() {
        return register.snapshot().stream().filter(Vehicle.class::isInstance)
                .map(Vehicle.class::cast).filter(vehicle -> vehicle.ease() >= BEGINNER_EASE).toList();
    }

    public List<Inventoried> inventory() {
        return List.copyOf(register.snapshot());
    }
}
