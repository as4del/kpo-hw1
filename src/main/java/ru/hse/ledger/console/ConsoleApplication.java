package ru.hse.ledger.console;


import java.io.PrintWriter;
import java.util.Locale;
import ru.hse.ledger.domain.Asset;
import ru.hse.ledger.domain.Equipment;
import ru.hse.ledger.domain.Vehicle;
import ru.hse.ledger.registry.AdmissionDesk;
import ru.hse.ledger.report.FleetReports;
import ru.hse.ledger.report.FleetTotals;

public final class ConsoleApplication {
    private final TerminalInput input;
    private final PrintWriter output;
    private final AdmissionDesk admissions;
    private final FleetReports reports;
    private final AssetCatalog catalog;

    public ConsoleApplication(TerminalInput input, PrintWriter output,
                              AdmissionDesk admissions, FleetReports reports, AssetCatalog catalog) {
        this.input = input;
        this.output = output;
        this.admissions = admissions;
        this.reports = reports;
        this.catalog = catalog;
    }

    public void run() {
        output.println("ВышКат — журнал приёмки. Данные хранятся до выхода.");
        help();
        while (true) {
            var line = input.command();
            if (line.isEmpty()) {
                output.println("Конец ввода. До встречи!");
                break;
            }
            String command = line.get().toLowerCase(Locale.ROOT);
            if (command.equals("exit") || command.equals("0")) {
                output.println("До встречи!");
                break;
            }
            try {
                execute(command);
            } catch (TerminalInput.EndOfInput error) {
                output.println("Ввод прерван. Незавершённый объект не добавлен.");
                break;
            } catch (IllegalArgumentException error) {
                output.println("Ошибка: " + error.getMessage());
            }
        }
        output.flush();
    }

    private void execute(String command) {
        if (command.startsWith("add ")) {
            add(command.substring(4).strip());
            return;
        }
        switch (command) {
            case "help" -> help();
            case "types" -> catalog.entries().forEach(entry ->
                    output.println(entry.key() + " — " + entry.title()));
            case "summary" -> summary();
            case "beginners" -> {
                var vehicles = reports.beginners();
                if (vehicles.isEmpty()) {
                    output.println("Подходящей техники пока нет.");
                }
                vehicles.forEach(vehicle -> output.println(vehicle.number() + " | "
                        + vehicle.name() + " | простота " + vehicle.ease()));
            }
            case "inventory" -> {
                var assets = reports.inventory();
                if (assets.isEmpty()) {
                    output.println("Баланс пуст.");
                }
                assets.forEach(asset -> output.println(asset.number() + " | " + asset.name()));
            }
            default -> output.println("Неизвестная команда. Введите help.");
        }
    }

    private void add(String key) {
        var entry = catalog.entries().stream().filter(item -> item.key().equals(key)).findFirst();
        if (entry.isEmpty()) {
            output.println("Неизвестный вид. Список: types.");
            return;
        }
        Asset asset = entry.get().factory().create(input);
        if (asset instanceof Vehicle vehicle) {
            output.println(admissions.accept(vehicle).message());
        } else if (asset instanceof Equipment equipment) {
            output.println(admissions.accept(equipment).message());
        } else {
            throw new IllegalArgumentException("Каталог содержит неподдерживаемый объект");
        }
    }

    private void summary() {
        FleetTotals totals = reports.totals();
        output.println("Транспорт: " + totals.vehicles() + "; вещи: " + totals.equipment()
                + "; всего: " + totals.total());
        output.println("Энергия: " + totals.energy().toPlainString() + " кВт·ч/сутки");
    }

    private void help() {
        output.println("add <вид> — приёмка; types — виды; summary — сводка;");
        output.println("beginners — новичкам; inventory — баланс; help — справка; exit (0) — выход.");
    }
}
