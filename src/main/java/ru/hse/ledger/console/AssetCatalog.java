package ru.hse.ledger.console;


import java.util.List;
import ru.hse.ledger.domain.Asset;
import ru.hse.ledger.domain.Bicycle;
import ru.hse.ledger.domain.ChargingCabinet;
import ru.hse.ledger.domain.ElectricBicycle;
import ru.hse.ledger.domain.Helmet;
import ru.hse.ledger.domain.Roadworthiness;
import ru.hse.ledger.domain.Scooter;

public final class AssetCatalog {
    @FunctionalInterface
    public interface Factory {
        Asset create(TerminalInput input);
    }

    public record Entry(String key, String title, Factory factory) {
    }

    private final List<Entry> entries = List.of(
            new Entry("scooter", "Электросамокат", input -> new Scooter(
                    input.text("Номер"), input.text("Название"), input.ease(),
                    condition(input), input.energy())),
            new Entry("bike", "Велосипед", input -> new Bicycle(
                    input.text("Номер"), input.text("Название"), input.ease(), condition(input))),
            new Entry("ebike", "Электровелосипед", input -> new ElectricBicycle(
                    input.text("Номер"), input.text("Название"), input.ease(),
                    condition(input), input.energy())),
            new Entry("helmet", "Шлем", input -> new Helmet(
                    input.text("Номер"), input.text("Название"))),
            new Entry("cabinet", "Зарядный шкаф", input -> new ChargingCabinet(
                    input.text("Номер"), input.text("Название"), input.energy())));

    private static Roadworthiness condition(TerminalInput input) {
        return new Roadworthiness(input.yesNo("Тормоза исправны"), input.yesNo("Колёса целы"));
    }

    public List<Entry> entries() {
        return entries;
    }
}
