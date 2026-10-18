package ru.hse.ledger.registry;


import java.util.Objects;
import ru.hse.ledger.domain.Equipment;
import ru.hse.ledger.domain.Vehicle;
import ru.hse.ledger.inspection.Inspection;
import ru.hse.ledger.inspection.ServiceCenter;

public final class AdmissionDesk {
    private final AssetRegister register;
    private final ServiceCenter serviceCenter;

    public AdmissionDesk(AssetRegister register, ServiceCenter serviceCenter) {
        this.register = Objects.requireNonNull(register);
        this.serviceCenter = Objects.requireNonNull(serviceCenter);
    }

    public Admission accept(Vehicle vehicle) {
        Objects.requireNonNull(vehicle);
        if (register.contains(vehicle.number())) {
            return new Admission(false, "Номер уже занят: " + vehicle.number());
        }
        Inspection inspection = serviceCenter.examine(vehicle);
        if (!inspection.approved()) {
            return new Admission(false, "Отказ: " + inspection.explanation());
        }
        register.append(vehicle);
        return new Admission(true, "Принят транспорт: " + vehicle.number());
    }

    public Admission accept(Equipment equipment) {
        Objects.requireNonNull(equipment);
        if (register.contains(equipment.number())) {
            return new Admission(false, "Номер уже занят: " + equipment.number());
        }
        register.append(equipment);
        return new Admission(true, "Принята вещь: " + equipment.number());
    }
}
