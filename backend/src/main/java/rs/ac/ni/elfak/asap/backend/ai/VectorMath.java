package rs.ac.ni.elfak.asap.backend.ai;

/** Small dense-vector helpers shared by the AI pipeline. */
public final class VectorMath {

    private VectorMath() {
    }

    public static float[] normalize(float[] v) {
        double norm = 0;
        for (float x : v) {
            norm += x * (double) x;
        }
        norm = Math.max(Math.sqrt(norm), 1e-12);
        float[] out = new float[v.length];
        for (int i = 0; i < v.length; i++) {
            out[i] = (float) (v[i] / norm);
        }
        return out;
    }

    public static double dot(float[] a, float[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * (double) b[i];
        }
        return sum;
    }
}
