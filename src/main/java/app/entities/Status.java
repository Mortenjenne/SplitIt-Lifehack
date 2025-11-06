package app.entities;

public enum Status
{
    PENDING("pending"),
    REJECTED("rejected"),
    ACCEPTED("accepted");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
