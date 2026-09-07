package rs.ac.ni.elfak.asap.network;

public interface ScanQueryClient {

    CallHandle query(String value, String format, Callback callback);

    interface CallHandle {
        void cancel();
    }

    interface Callback {
        void onSuccess(I1ApiModels.ScanQueryResponse response);

        void onFailure(Failure failure);
    }

    final class Failure {
        public enum Kind {
            TRANSPORT,
            HTTP,
            INVALID_RESPONSE
        }

        private final Kind kind;
        private final Integer httpStatus;

        private Failure(Kind kind, Integer httpStatus) {
            this.kind = kind;
            this.httpStatus = httpStatus;
        }

        public static Failure transport() {
            return new Failure(Kind.TRANSPORT, null);
        }

        public static Failure http(int status) {
            return new Failure(Kind.HTTP, status);
        }

        public static Failure invalidResponse() {
            return new Failure(Kind.INVALID_RESPONSE, null);
        }

        public Kind kind() {
            return kind;
        }

        public Integer httpStatus() {
            return httpStatus;
        }
    }
}
