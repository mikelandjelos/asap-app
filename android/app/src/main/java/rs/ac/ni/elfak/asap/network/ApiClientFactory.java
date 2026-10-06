package rs.ac.ni.elfak.asap.network;

import rs.ac.ni.elfak.asap.BuildConfig;

public final class ApiClientFactory {

    private ApiClientFactory() {
    }

    public static ScanQueryClient createDefault() {
        return RetrofitScanQueryClient.create(BuildConfig.I1_BASE_URL);
    }

    /** AI v2 client on the same backend base URL. */
    public static V2Client createV2() {
        return V2Client.create(BuildConfig.I1_BASE_URL);
    }
}
