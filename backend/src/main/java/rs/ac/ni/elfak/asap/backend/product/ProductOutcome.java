package rs.ac.ni.elfak.asap.backend.product;

public record ProductOutcome(Status status, Product product) {

    public enum Status {
        KNOWN,
        UNKNOWN,
        UNAVAILABLE
    }

    public static ProductOutcome known(Product product) {
        return new ProductOutcome(Status.KNOWN, product);
    }

    public static ProductOutcome unknown() {
        return new ProductOutcome(Status.UNKNOWN, null);
    }

    public static ProductOutcome unavailable() {
        return new ProductOutcome(Status.UNAVAILABLE, null);
    }
}
