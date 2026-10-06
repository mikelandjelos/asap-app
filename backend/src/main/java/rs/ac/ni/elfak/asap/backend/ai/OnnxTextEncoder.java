package rs.ac.ni.elfak.asap.backend.ai;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * e5-small sentence encoder: tokenizer.json → ONNX encoder → attention-masked mean pooling → L2 normalization.
 * The same path as {@code ml/asap_ml/retrieval.py#OnnxEncoder}, verified by the bundle parity fixtures.
 */
public final class OnnxTextEncoder implements AutoCloseable {

    private final HuggingFaceTokenizer tokenizer;
    private final OrtEnvironment environment;
    private final OrtSession session;
    private final String prefix;

    public OnnxTextEncoder(Path modelDir, String prefix, int maxLength) throws IOException {
        this.tokenizer = HuggingFaceTokenizer.builder()
                .optTokenizerPath(modelDir.resolve("tokenizer.json"))
                .optMaxLength(maxLength)
                .optTruncation(true)
                .optPadding(false)
                .build();
        this.environment = OrtEnvironment.getEnvironment();
        try {
            this.session = environment.createSession(modelDir.resolve("model.onnx").toString(), new OrtSession.SessionOptions());
        } catch (OrtException e) {
            tokenizer.close();
            throw new IOException("Cannot load ONNX model", e);
        }
        this.prefix = prefix;
    }

    public static OnnxTextEncoder of(RuntimeBundle bundle) throws IOException {
        BundleModel.ModelSpec model = bundle.manifest().model();
        return new OnnxTextEncoder(bundle.dir().resolve("model"), model.prefix(), model.maxLength());
    }

    public float[] encode(String text) {
        Encoding encoding = tokenizer.encode(prefix + text);
        long[] ids = encoding.getIds();
        long[] mask = encoding.getAttentionMask();
        try (OnnxTensor idTensor = OnnxTensor.createTensor(environment, new long[][] {ids});
                OnnxTensor maskTensor = OnnxTensor.createTensor(environment, new long[][] {mask});
                OrtSession.Result result = session.run(Map.of("input_ids", idTensor, "attention_mask", maskTensor))) {
            float[][] hidden = ((float[][][]) result.get(0).getValue())[0];
            return VectorMath.normalize(meanPool(hidden, mask));
        } catch (OrtException e) {
            throw new IllegalStateException("ONNX inference failed", e);
        }
    }

    private static float[] meanPool(float[][] hidden, long[] mask) {
        int dim = hidden[0].length;
        double[] sum = new double[dim];
        double count = 0;
        for (int t = 0; t < hidden.length; t++) {
            if (mask[t] == 0) {
                continue;
            }
            count++;
            for (int j = 0; j < dim; j++) {
                sum[j] += hidden[t][j];
            }
        }
        float[] out = new float[dim];
        for (int j = 0; j < dim; j++) {
            out[j] = (float) (sum[j] / count);
        }
        return out;
    }

    @Override
    public void close() throws OrtException {
        session.close();
        tokenizer.close();
    }
}
