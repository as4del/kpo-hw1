package ru.hse.ledger.domain;


import java.math.BigDecimal;

public final class ChargingCabinet extends Equipment implements Powered {
    private final BigDecimal energy;

    public ChargingCabinet(String number, String name, BigDecimal energy) {
        super(number, name);
        this.energy = Energy.validate(energy);
    }

    @Override
    public BigDecimal dailyEnergy() {
        return energy;
    }
}
