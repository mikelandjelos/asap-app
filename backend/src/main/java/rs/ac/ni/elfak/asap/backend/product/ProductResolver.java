package rs.ac.ni.elfak.asap.backend.product;

import rs.ac.ni.elfak.asap.backend.barcode.Barcode;

public interface ProductResolver {

    ProductOutcome resolve(Barcode barcode);
}
