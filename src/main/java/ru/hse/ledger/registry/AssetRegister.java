package ru.hse.ledger.registry;


import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ru.hse.ledger.domain.Inventoried;

public final class AssetRegister {
    private final Map<String, Inventoried> entries = new LinkedHashMap<>();

    public boolean contains(String number) {
        return entries.containsKey(number.strip());
    }

    // Package access keeps all writes behind AdmissionDesk and its inspection.
    void append(Inventoried asset) {
        Objects.requireNonNull(asset, "Объект");
        if (entries.putIfAbsent(asset.number(), asset) != null) {
            throw new IllegalArgumentException("Номер уже занят: " + asset.number());
        }
    }

    public Collection<Inventoried> snapshot() {
        return List.copyOf(entries.values());
    }
}
