package ru.hse.ledger.inspection;


public record Inspection(boolean approved, String explanation) {
    public Inspection {
        if (explanation == null || explanation.isBlank()) {
            throw new IllegalArgumentException("Нужно пояснение результата техосмотра");
        }
    }
}
