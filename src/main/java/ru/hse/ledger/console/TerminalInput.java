package ru.hse.ledger.console;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Optional;
import ru.hse.ledger.domain.Energy;

public final class TerminalInput {
    private final BufferedReader reader;
    private final PrintWriter output;

    public TerminalInput(Reader reader, PrintWriter output) {
        this.reader = new BufferedReader(reader);
        this.output = output;
    }

    public Optional<String> command() {
        output.print("ВышКат> ");
        output.flush();
        return read();
    }

    private Optional<String> read() {
        try {
            return Optional.ofNullable(reader.readLine()).map(String::strip);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    public String text(String prompt) {
        while (true) {
            output.print(prompt + ": ");
            output.flush();
            String value = read().orElseThrow(EndOfInput::new);
            if (!value.isBlank()) {
                return value;
            }
            output.println("Введите непустое значение.");
        }
    }

    public int ease() {
        while (true) {
            try {
                int value = Integer.parseInt(text("Простота (1–10)"));
                if (value >= 1 && value <= 10) {
                    return value;
                }
            } catch (NumberFormatException error) {
                // Repeat only this field.
            }
            output.println("Нужно целое число от 1 до 10.");
        }
    }

    public boolean yesNo(String prompt) {
        while (true) {
            String value = text(prompt + " (да/нет)");
            if (value.equalsIgnoreCase("да") || value.equals("1")) {
                return true;
            }
            if (value.equalsIgnoreCase("нет") || value.equals("0")) {
                return false;
            }
            output.println("Введите да или нет.");
        }
    }

    public BigDecimal energy() {
        while (true) {
            String value = text("Энергия за сутки, кВт·ч (до 3 знаков после запятой)");
            try {
                if (value.length() > 40) {
                    throw new IllegalArgumentException("Слишком длинная запись расхода");
                }
                return Energy.validate(new BigDecimal(value.replace(',', '.')));
            } catch (IllegalArgumentException error) {
                output.println("Некорректный расход: " + error.getMessage());
            }
        }
    }

    public static final class EndOfInput extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
