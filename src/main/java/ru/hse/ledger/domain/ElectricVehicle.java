package ru.hse.ledger.domain;


import java.math.BigDecimal;

public abstract class ElectricVehicle extends Vehicle implements Powered {
    private final BigDecimal energy;

    protected ElectricVehicle(String number, String name, int ease,
                              Roadworthiness condition, BigDecimal energy) {
        super(number, name, ease, condition);
        this.energy = Energy.validate(energy);
    }

    @Override
    public final BigDecimal dailyEnergy() {
        return energy;
    }
}
