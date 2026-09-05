package rs.ac.ni.elfak.asap.backend.api;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.ScanQueryRequest;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.ScanQueryResponse;
import rs.ac.ni.elfak.asap.backend.application.ScanQueryCoordinator;
import rs.ac.ni.elfak.asap.backend.barcode.BarcodeRules;

@RestController
@RequestMapping("/api/v1/scan-queries")
public class ScanQueryController {

    private final ScanQueryCoordinator coordinator;
    private final ScanQueryResponseMapper responseMapper;

    public ScanQueryController(
            ScanQueryCoordinator coordinator,
            ScanQueryResponseMapper responseMapper) {
        this.coordinator = coordinator;
        this.responseMapper = responseMapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanQueryResponse query(@Valid @RequestBody ScanQueryRequest request) {
        var barcode = BarcodeRules.validatedBarcode(
                request.barcode().value(), request.barcode().format());
        return responseMapper.map(coordinator.query(barcode));
    }
}
