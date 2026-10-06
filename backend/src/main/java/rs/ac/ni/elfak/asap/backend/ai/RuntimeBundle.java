package rs.ac.ni.elfak.asap.backend.ai;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import rs.ac.ni.elfak.asap.backend.ai.BundleModel.CatalogEntry;
import rs.ac.ni.elfak.asap.backend.ai.BundleModel.Manifest;
import rs.ac.ni.elfak.asap.backend.ai.BundleModel.TfidfSpec;
import rs.ac.ni.elfak.asap.backend.ai.BundleModel.Theme;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/** Immutable, checksum-verified runtime bundle (D-034). Loading fails if any file differs from its manifest. */
public final class RuntimeBundle {

    private static final ObjectMapper JSON = JsonMapper.builder().build();

    private final Path dir;
    private final Manifest manifest;
    private final List<CatalogEntry> catalog;
    private final FloatMatrix embeddings;
    private final FloatMatrix typeEmbeddings;
    private final FloatMatrix clusterCentroids;
    private final FloatMatrix pcaMean;
    private final FloatMatrix pcaComponents;
    private final TfidfSpec tfidf;
    private final List<Theme> themes;
    private final List<BundleModel.MapPoint> mapSample;
    private final Map<String, String> images;

    private RuntimeBundle(Path dir) throws IOException {
        this.dir = dir;
        this.manifest = JSON.readValue(dir.resolve("manifest.json").toFile(), Manifest.class);
        verifyChecksums();
        this.catalog = readCatalog();
        this.embeddings = matrix("embeddings.f32");
        this.typeEmbeddings = matrix("type_embeddings.f32");
        this.clusterCentroids = matrix("cluster_centroids.f32");
        this.pcaMean = matrix("pca_mean.f32");
        this.pcaComponents = matrix("pca_components.f32");
        this.tfidf = JSON.readValue(dir.resolve("tfidf.json").toFile(), TfidfSpec.class);
        this.themes = JSON.readValue(dir.resolve("themes.json").toFile(), new TypeReference<List<Theme>>() { });
        this.mapSample = JSON.readValue(dir.resolve("map_sample.json").toFile(), new TypeReference<List<BundleModel.MapPoint>>() { });
        this.images = Map.copyOf(JSON.readValue(dir.resolve("images.json").toFile(), new TypeReference<Map<String, String>>() { }));
        validate();
    }

    public static RuntimeBundle load(Path dir) throws IOException {
        return new RuntimeBundle(dir);
    }

    /** Resolves {@code root/CURRENT} to the active bundle directory. */
    public static Path current(Path root) throws IOException {
        return root.resolve(Files.readString(root.resolve("CURRENT"), StandardCharsets.UTF_8).strip());
    }

    private void verifyChecksums() throws IOException {
        for (Map.Entry<String, BundleModel.FileEntry> file : manifest.files().entrySet()) {
            String actual = sha256(dir.resolve(file.getKey()));
            if (!actual.equals(file.getValue().sha256())) {
                throw new IOException("Bundle checksum mismatch: " + file.getKey());
            }
        }
    }

    private static String sha256(Path file) throws IOException {
        try (InputStream in = new DigestInputStream(Files.newInputStream(file), MessageDigest.getInstance("SHA-256"))) {
            in.transferTo(java.io.OutputStream.nullOutputStream());
            return HexFormat.of().formatHex(((DigestInputStream) in).getMessageDigest().digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<CatalogEntry> readCatalog() throws IOException {
        List<CatalogEntry> entries = new ArrayList<>();
        for (String line : Files.readAllLines(dir.resolve("catalog.jsonl"), StandardCharsets.UTF_8)) {
            entries.add(JSON.readValue(line, CatalogEntry.class));
        }
        return List.copyOf(entries);
    }

    private FloatMatrix matrix(String name) throws IOException {
        List<Integer> shape = manifest.matrices().get(name);
        int rows = shape.size() == 1 ? 1 : shape.get(0);
        int cols = shape.get(shape.size() - 1);
        return FloatMatrix.read(dir.resolve(name), rows, cols);
    }

    private void validate() {
        int n = manifest.products();
        for (int i = 0; i < catalog.size(); i++) {
            if (catalog.get(i).index() != i) {
                throw new IllegalStateException("Catalog index out of order at " + i);
            }
        }
        if (catalog.size() != n || embeddings.rows() != n || typeEmbeddings.rows() != n
                || embeddings.cols() != manifest.dim() || clusterCentroids.rows() != manifest.params().k()
                || tfidf.vocabulary().size() != tfidf.idf().length || themes.size() != manifest.params().k()) {
            throw new IllegalStateException("Bundle shapes are inconsistent");
        }
    }

    public Path dir() {
        return dir;
    }

    public Manifest manifest() {
        return manifest;
    }

    public List<CatalogEntry> catalog() {
        return catalog;
    }

    public FloatMatrix embeddings() {
        return embeddings;
    }

    public FloatMatrix typeEmbeddings() {
        return typeEmbeddings;
    }

    public FloatMatrix clusterCentroids() {
        return clusterCentroids;
    }

    public FloatMatrix pcaMean() {
        return pcaMean;
    }

    public FloatMatrix pcaComponents() {
        return pcaComponents;
    }

    public TfidfSpec tfidf() {
        return tfidf;
    }

    public List<Theme> themes() {
        return themes;
    }

    public List<BundleModel.MapPoint> mapSample() {
        return mapSample;
    }

    /** Front-image URL of a catalog product (Open Food Facts family, CC BY-SA), or {@code null}. */
    public String imageUrl(String productId) {
        return images.get(productId);
    }
}
