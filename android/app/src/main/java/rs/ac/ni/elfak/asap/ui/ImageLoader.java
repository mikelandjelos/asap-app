package rs.ac.ni.elfak.asap.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rs.ac.ni.elfak.asap.network.V2ApiModels;

/**
 * Minimal product-image loader (S7c.1): HTTPS images from the Open Food Facts family only, fetched directly by the
 * phone, decoded off the main thread and kept in a small memory cache. A view shows its placeholder until its own
 * request completes; a recycled view never receives a stale image.
 */
public final class ImageLoader {

    private static final ImageLoader INSTANCE = new ImageLoader();
    private static final int MAX_BYTES = 512 * 1024;

    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(false)
            .build();
    private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(8 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };
    private final Handler main = new Handler(Looper.getMainLooper());

    private ImageLoader() {
    }

    public static ImageLoader get() {
        return INSTANCE;
    }

    public void load(String url, ImageView view, int placeholderRes) {
        view.setTag(url);
        if (!V2ApiModels.isAllowedImage(url)) {
            view.setImageResource(placeholderRes);
            return;
        }
        Bitmap cached = cache.get(url);
        if (cached != null) {
            view.setImageBitmap(cached);
            return;
        }
        view.setImageResource(placeholderRes);
        http.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // keep the placeholder
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody body = response.body()) {
                    if (!response.isSuccessful() || body == null || body.contentLength() > MAX_BYTES) {
                        return;
                    }
                    byte[] bytes = body.bytes();
                    Bitmap bitmap = bytes.length > MAX_BYTES ? null : BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bitmap == null) {
                        return;
                    }
                    cache.put(url, bitmap);
                    main.post(() -> {
                        if (url.equals(view.getTag())) {
                            view.setImageBitmap(bitmap);
                        }
                    });
                } catch (IOException | RuntimeException e) {
                    // keep the placeholder
                }
            }
        });
    }
}
