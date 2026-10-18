package ru.hse.ledger.domain;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Energy {
    private static final BigDecimal MAX = new BigDecimal("1000000");

    private Energy() {
    }

    public static BigDecimal validate(BigDecimal value) {
        Objects.requireNonNull(value, "Расход энергии");
        if (value.signum() < 0 || value.compareTo(MAX) > 0) {
            throw new IllegalArgumentException("Расход должен быть от 0 до 1000000 кВт·ч");
        }
        // Reject extreme exponents before any scale conversion or text formatting.
        if (value.signum() == 0) {
            return BigDecimal.ZERO.setScale(3);
        }
        if (value.scale() > 20 || value.scale() < -6 || value.precision() > 32) {
            throw new IllegalArgumentException("Слишком длинная запись расхода");
        }
        try {
            return value.setScale(3, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException error) {
            throw new IllegalArgumentException("Расход: не более трёх знаков после запятой", error);
        }
    }
}
