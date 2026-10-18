package ru.hse.ledger;


import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.hse.ledger.console.AssetCatalog;
import ru.hse.ledger.console.ConsoleApplication;
import ru.hse.ledger.console.TerminalInput;
import ru.hse.ledger.registry.AdmissionDesk;
import ru.hse.ledger.report.FleetReports;

public final class Launcher {
    private Launcher() {
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(Bootstrap.class)) {
            PrintWriter output = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
            var input = new TerminalInput(new InputStreamReader(System.in, StandardCharsets.UTF_8), output);
            new ConsoleApplication(input, output, context.getBean(AdmissionDesk.class),
                    context.getBean(FleetReports.class), new AssetCatalog()).run();
        }
    }
}
