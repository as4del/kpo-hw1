package ru.hse.ledger.domain;


import java.math.BigDecimal;

public final class Scooter extends ElectricVehicle {
    public Scooter(String number, String name, int ease,
                   Roadworthiness condition, BigDecimal energy) {
        super(number, name, ease, condition, energy);
    }
}
