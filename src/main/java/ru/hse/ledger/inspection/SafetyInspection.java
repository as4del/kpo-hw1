package ru.hse.ledger.inspection;


import java.util.ArrayList;
import java.util.List;
import ru.hse.ledger.domain.Vehicle;

public final class SafetyInspection implements ServiceCenter {
    @Override
    public Inspection examine(Vehicle vehicle) {
        List<String> faults = new ArrayList<>();
        if (!vehicle.condition().brakesWorking()) {
            faults.add("не работают тормоза");
        }
        if (!vehicle.condition().wheelsIntact()) {
            faults.add("повреждены колёса");
        }
        return faults.isEmpty()
                ? new Inspection(true, "Техосмотр пройден")
                : new Inspection(false, String.join("; ", faults));
    }
}
