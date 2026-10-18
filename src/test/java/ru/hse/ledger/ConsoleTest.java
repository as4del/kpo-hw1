package ru.hse.ledger;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import ru.hse.ledger.console.AssetCatalog;
import ru.hse.ledger.console.ConsoleApplication;
import ru.hse.ledger.console.TerminalInput;
import ru.hse.ledger.inspection.SafetyInspection;
import ru.hse.ledger.registry.AdmissionDesk;
import ru.hse.ledger.registry.AssetRegister;
import ru.hse.ledger.report.FleetReports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleTest {
    private record Session(String text, FleetReports reports) {
    }

    private Session run(String script) {
        var register = new AssetRegister();
        var reports = new FleetReports(register);
        var buffer = new StringWriter();
        var output = new PrintWriter(buffer);
        new ConsoleApplication(new TerminalInput(new StringReader(script), output), output,
                new AdmissionDesk(register, new SafetyInspection()), reports, new AssetCatalog()).run();
        return new Session(buffer.toString(), reports);
    }

    @Test
    void walkthroughExercisesAllCommandsAndKinds() throws IOException {
        try (var stream = getClass().getResourceAsStream("/walkthrough.txt")) {
            var result = run(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
            assertEquals(4, result.reports().totals().vehicles());
            assertEquals(2, result.reports().totals().equipment());
            assertEquals("2.400", result.reports().totals().energy().toPlainString());
            assertEquals(3, result.reports().beginners().size());
            assertTrue(result.text().contains("не работают тормоза; повреждены колёса"));
            assertTrue(result.text().contains("Номер уже занят: SC-17"));
            assertTrue(result.text().contains("Неизвестный вид"));
            assertTrue(result.text().contains("Неизвестная команда"));
            assertTrue(result.text().contains("Баланс пуст"));
            assertTrue(result.text().contains("Подходящей техники пока нет"));
            assertTrue(result.text().contains("До встречи"));
        }
    }

    @Test
    void retriesInvalidFieldsWithoutLosingDraft() {
        var result = run("""
                add scooter

                S

                Пульс
                abc
                0
                11
                6
                maybe
                ДА
                1
                NaN
                -1
                0.0001
                1E+2147483647
                1E-2147483647
                12345678901234567890123456789012345678901.11
                0,125
                summary
                exit
                """);
        assertEquals(1, result.reports().totals().total());
        assertEquals("0.125", result.reports().totals().energy().toPlainString());
        assertTrue(result.text().contains("непустое"));
        assertTrue(result.text().contains("Введите да или нет"));
        assertTrue(result.text().contains("Нужно целое"));
        assertTrue(result.text().contains("Некорректный расход"));
    }

    @Test
    void falseAnswersRejectVehicleAndEofCancelsDraft() {
        var result = run("add bike\nB\nСломан\n1\n0\nНЕТ\nadd helmet\nH\n");
        assertTrue(result.reports().inventory().isEmpty());
        assertTrue(result.text().contains("Отказ"));
        assertTrue(result.text().contains("Незавершённый объект не добавлен"));
    }

    @Test
    void normalEofAndNumericExitAreHandled() {
        assertTrue(run("").text().contains("Конец ввода"));
        assertTrue(run("0\n").text().contains("До встречи"));
        assertFalse(run("0\nadd helmet\nH\nЛишний\n").reports().totals().total() > 0);
    }

    @Test
    void inputReadFailureIsReportedAsIoError() {
        Reader broken = new Reader() {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("disk");
            }

            @Override
            public void close() {
            }
        };
        var input = new TerminalInput(broken, new PrintWriter(new StringWriter()));
        assertThrows(UncheckedIOException.class, input::command);
    }
}
