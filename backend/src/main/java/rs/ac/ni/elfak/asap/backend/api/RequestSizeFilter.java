package rs.ac.ni.elfak.asap.backend.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestSizeFilter extends OncePerRequestFilter {

    static final int MAX_BODY_BYTES = 2 * 1024;
    static final int MAX_V2_BODY_BYTES = 16 * 1024;
    private static final Map<String, Integer> LIMITS = Map.of(
            "/api/v1/scan-queries", MAX_BODY_BYTES,
            "/api/v2/scan-queries", MAX_V2_BODY_BYTES);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Integer limit = LIMITS.get(request.getRequestURI());
        if (!"POST".equals(request.getMethod()) || limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getContentLengthLong() > limit) {
            writeTooLarge(response, limit);
            return;
        }
        byte[] body = request.getInputStream().readNBytes(limit + 1);
        if (body.length > limit) {
            writeTooLarge(response, limit);
            return;
        }
        filterChain.doFilter(new CachedBodyRequest(request, body), response);
    }

    private static void writeTooLarge(HttpServletResponse response, int limit) throws IOException {
        response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {"type":"urn:asap:problem:invalid-request","title":"Content too large",\
                "status":413,"detail":"The request body exceeds %d bytes.",\
                "errors":[{"field":"$","code":"BODY_TOO_LARGE"}]}""".formatted(limit));
    }

    private static final class CachedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        private CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body.clone();
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream input = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return input.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    throw new UnsupportedOperationException("Async reads are not supported");
                }

                @Override
                public int read() {
                    return input.read();
                }
            };
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }
    }
}
