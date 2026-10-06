package rs.ac.ni.elfak.asap.backend.ai;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class CharTfidfTest {

    @Test
    void ngramsMatchSklearnCharWbSemantics() {
        // sklearn: " ab " -> 3-grams " ab", "ab "; 4-gram " ab " ; 5-gram skipped after a single short gram
        assertEquals(List.of(" ab", "ab ", " ab "), CharTfidf.ngrams("AB", 3, 5));
        // a 1-letter word yields its padded form once (" a ") and then stops
        assertEquals(List.of(" a "), CharTfidf.ngrams("a", 3, 5));
    }

    @Test
    void whitespaceRunsAndCodePointsAreHandled() {
        assertEquals(CharTfidf.ngrams("x  y", 3, 3), CharTfidf.ngrams("x\t\ny", 3, 3));
        // supplementary characters count as one code point
        assertEquals(List.of(" 😀 "), CharTfidf.ngrams("😀", 3, 3));
        assertEquals(List.of(), CharTfidf.ngrams("   ", 3, 5));
    }

    @Test
    void sublinearTfIdfIsL2Normalized() {
        CharTfidf tfidf = new CharTfidf(List.of(" ab", "ab ", " ab "), new double[] {1.0, 2.0, 3.0}, 3, 5);
        CharTfidf.SparseVector v = tfidf.transform("ab ab");
        assertArrayEquals(new int[] {0, 1, 2}, v.indices());
        double w = 1 + Math.log(2);
        double norm = Math.sqrt(w * w * (1 + 4 + 9));
        assertArrayEquals(new double[] {w / norm, 2 * w / norm, 3 * w / norm}, v.values(), 1e-12);
    }

    @Test
    void productTextRulesMatchPython() {
        assertEquals("Oats | Brand | Porridges | breakfasts, porridges",
                ProductText.full("Oats", "Brand", "Porridges", List.of("en:breakfasts", "en:porridges"), "", ""));
        assertEquals("Router", ProductText.type("Router", "", List.of()));
        assertEquals("plant based foods", ProductText.tagLabel("en:plant-based-foods"));
    }
}
