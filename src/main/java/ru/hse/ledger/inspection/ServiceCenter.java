package ru.hse.ledger.inspection;


import ru.hse.ledger.domain.Vehicle;

@FunctionalInterface
public interface ServiceCenter {
    Inspection examine(Vehicle vehicle);
}
