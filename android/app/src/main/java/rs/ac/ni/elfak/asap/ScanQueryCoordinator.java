package rs.ac.ni.elfak.asap;

import java.util.concurrent.Executor;
import rs.ac.ni.elfak.asap.network.BarcodeFormatMapper;
import rs.ac.ni.elfak.asap.network.I1ApiModels;
import rs.ac.ni.elfak.asap.network.ScanQueryClient;

final class ScanQueryCoordinator {

    interface View {
        void showEmptyBarcode();

        void showUnsupportedBarcode(String value);

        void showLoading(String value);

        void showResponse(String value, I1ApiModels.ScanQueryResponse response);

        void showFailure(String value, ScanQueryClient.Failure failure);
    }

    private final ScanQueryClient client;
    private final Executor mainThreadExecutor;
    private final View view;
    private RequestToken activeRequest;
    private boolean closed;

    ScanQueryCoordinator(ScanQueryClient client, Executor mainThreadExecutor, View view) {
        this.client = client;
        this.mainThreadExecutor = mainThreadExecutor;
        this.view = view;
    }

    void onScanStarted() {
        if (closed) {
            return;
        }
        cancelActiveRequest();
    }

    void onScanSucceeded(String rawValue, String displayValue, int scannerFormat) {
        if (closed) {
            return;
        }
        cancelActiveRequest();

        String value = BarcodeResultFormatter.selectValue(rawValue, displayValue);
        if (value == null) {
            view.showEmptyBarcode();
            return;
        }

        String format = BarcodeFormatMapper.toContractFormat(scannerFormat);
        if (format == null) {
            view.showUnsupportedBarcode(value);
            return;
        }

        view.showLoading(value);
        RequestToken request = new RequestToken();
        activeRequest = request;
        ScanQueryClient.CallHandle handle = client.query(
                value,
                format,
                new ScanQueryClient.Callback() {
                    @Override
                    public void onSuccess(I1ApiModels.ScanQueryResponse response) {
                        mainThreadExecutor.execute(() -> deliverSuccess(request, value, response));
                    }

                    @Override
                    public void onFailure(ScanQueryClient.Failure failure) {
                        mainThreadExecutor.execute(() -> deliverFailure(request, value, failure));
                    }
                });
        request.handle = handle;
        if (activeRequest != request && !request.completed) {
            handle.cancel();
        }
    }

    void close() {
        closed = true;
        cancelActiveRequest();
    }

    private void deliverSuccess(
            RequestToken request, String value, I1ApiModels.ScanQueryResponse response) {
        if (!canDeliver(request)) {
            return;
        }
        finish(request);
        view.showResponse(value, response);
    }

    private void deliverFailure(
            RequestToken request, String value, ScanQueryClient.Failure failure) {
        if (!canDeliver(request)) {
            return;
        }
        finish(request);
        view.showFailure(value, failure);
    }

    private boolean canDeliver(RequestToken request) {
        return !closed && activeRequest == request;
    }

    private void finish(RequestToken request) {
        request.completed = true;
        activeRequest = null;
    }

    private void cancelActiveRequest() {
        RequestToken request = activeRequest;
        activeRequest = null;
        if (request != null && request.handle != null) {
            request.handle.cancel();
        }
    }

    private static final class RequestToken {
        private ScanQueryClient.CallHandle handle;
        private boolean completed;
    }
}
