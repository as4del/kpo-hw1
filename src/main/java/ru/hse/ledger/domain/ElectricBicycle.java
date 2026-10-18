package ru.hse.ledger.domain;


import java.math.BigDecimal;

public final class ElectricBicycle extends ElectricVehicle {
    public ElectricBicycle(String number, String name, int ease,
                   Roadworthiness condition, BigDecimal energy) {
        super(number, name, ease, condition, energy);
    }
}
