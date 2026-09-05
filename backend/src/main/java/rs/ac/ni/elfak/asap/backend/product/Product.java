package rs.ac.ni.elfak.asap.backend.product;

import java.util.List;
import rs.ac.ni.elfak.asap.backend.barcode.Barcode;

public record Product(
        String id,
        Barcode barcode,
        String name,
        String brand,
        String category,
        String description,
        List<String> tags) {

    public Product {
        tags = List.copyOf(tags);
    }
}
