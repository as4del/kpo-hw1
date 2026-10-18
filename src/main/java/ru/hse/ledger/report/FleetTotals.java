package ru.hse.ledger.report;


import java.math.BigDecimal;

public record FleetTotals(long vehicles, long equipment, BigDecimal energy) {
    public long total() {
        return vehicles + equipment;
    }
}
