package rs.ac.ni.elfak.asap.backend.ai;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

/** Dense row-major float32 matrix read from a raw little-endian bundle file. */
public record FloatMatrix(int rows, int cols, float[] data) {

    public FloatMatrix {
        if (data.length != (long) rows * cols) {
            throw new IllegalArgumentException("Matrix size does not match its shape");
        }
    }

    public static FloatMatrix read(Path file, int rows, int cols) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        if (bytes.length != (long) rows * cols * Float.BYTES) {
            throw new IOException("Unexpected size for " + file.getFileName());
        }
        float[] data = new float[rows * cols];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(data);
        return new FloatMatrix(rows, cols, data);
    }

    public float[] row(int i) {
        float[] out = new float[cols];
        System.arraycopy(data, i * cols, out, 0, cols);
        return out;
    }

    /** Dot product of row {@code i} with {@code v}, accumulated in double. */
    public double dot(int i, float[] v) {
        int offset = i * cols;
        double sum = 0;
        for (int j = 0; j < cols; j++) {
            sum += data[offset + j] * (double) v[j];
        }
        return sum;
    }

    /** All row dot products: {@code M · v}. */
    public double[] times(float[] v) {
        double[] out = new double[rows];
        for (int i = 0; i < rows; i++) {
            out[i] = dot(i, v);
        }
        return out;
    }
}
