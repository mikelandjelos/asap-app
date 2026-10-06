# Accepted backend baseline

Current scope note (D-024/D-025): this is the implemented I1 technology baseline, not a restriction on required later embedding/retrieval, clustering, personalization, PCA or MMR work. Each AI/statistical component needs notebook evidence under `NOTEBOOK_VALIDATION.md`; no new framework is selected here. Android I1 integration was subsequently completed under T-007/S4.

Status: Accepted and frozen under T-007/S1; realized by the accepted T-007/S3 implementation.

Last researched: 2026-09-05 from official Spring Boot, Apache Maven, Javalin, and Quarkus documentation.

## Frozen minimal baseline

| Concern | Recommendation | Reason |
| --- | --- | --- |
| Runtime and source level | OpenJDK 21 | The complete JDK is already installed and verified; Java 21 is an LTS release and is within Spring Boot 4.1.1's supported Java 17–26 range. |
| Framework | Spring Boot 4.1.1 | Current stable release, conventional standalone REST/JSON support, embedded server, dependency management, and mature test support minimize assembly work for the I1 slice. |
| Web stack | Servlet Spring MVC through `spring-boot-starter-webmvc` | The I1 API is small and synchronous. Spring Boot 4's dedicated MVC starter includes the HTTP/JSON path needed without adopting a reactive stack. |
| Build | Maven 3.9.16 through Maven Wrapper 3.3.3 (`only-script`) | A small conventional POM is sufficient. A repository-owned wrapper makes the build reproducible without installing Maven globally; wrapper 3.3.3 is the latest release available on the research date. |
| Project location | `backend/`, initially one Maven module | Matches the accepted single-deployment modular-monolith boundary without premature multi-module build complexity. |
| Coordinates | group `rs.ac.ni.elfak.asap`, artifact `asap-backend`, base package `rs.ac.ni.elfak.asap.backend` | Keeps institutional/project identity consistent with the Android namespace while making the deployment role explicit. |

## Initial dependency policy

S3 should inherit dependency and plugin versions from the Spring Boot 4.1.1 parent rather than pinning managed transitive libraries independently. Its initial direct dependency set should remain:

- production: `org.springframework.boot:spring-boot-starter-webmvc`;
- tests: `org.springframework.boot:spring-boot-starter-test`;
- validation: add `org.springframework.boot:spring-boot-starter-validation` only if the accepted S2 contract uses Jakarta Bean Validation annotations.

Use Maven Central only, exact release versions, and no snapshots, milestones, or dynamic ranges. Do not add Spring Data/JPA, a database driver, Actuator, Lombok, DevTools, Docker Compose support, Spring Modulith, Spring AI, an external-provider client, or a vector-store client for deterministic I1. Internal Java packages and interfaces are sufficient to preserve the already accepted API, product-resolution, and recommendation boundaries.

The S2 contract should decide endpoint paths, payloads, status mapping, validation limits, fixtures, and whether the conditional validation starter is justified. S1 does not pre-empt those decisions.

## Candidate comparison

| Candidate | Advantages for ASAP | Cost/risk for the first slice | Result |
| --- | --- | --- | --- |
| Spring Boot 4.1.1 | Familiar Java model; managed dependency graph; embedded server; automatic JSON conversion; focused MVC and broad test support; natural path toward later validation/data adapters | More framework machinery and slower startup than Javalin; conventions must not be allowed to blur module ownership | Recommended |
| Javalin 7.2.3 | Small API, explicit routing, Java 17+, Jetty 12, quick startup | ASAP would need to establish more conventions for JSON, validation, error mapping, dependency assembly, and module wiring itself | Viable lightweight alternative, not recommended |
| Quarkus 3.33 LTS | Strong dev mode and extension ecosystem; efficient runtime/native path | Build-time extension conventions and native optimization add concepts that deterministic I1 does not need | Deferred unless deployment constraints later justify it |

Spring Boot is the shortest maintainable path here because the user already knows Java, the required JDK is present, and the project needs a tested REST boundary that can grow into the accepted modules. The recommendation does not commit the project to Spring persistence, security, AI, or cloud products.

## Realized scaffold and verification

The approved S3 scaffold now exists under `backend/`. It uses the exact baseline above, adds the conditionally approved validation starter, and packages the canonical `docs/fixtures/i1-products.json` resource directly so there is no second editable fixture copy. The Maven Wrapper records distribution SHA-256 `5af3b743dd8b876b5c45da33b676251e5f1687712644abb4ee519ca56e1d89ce`.

- production dependencies are `spring-boot-starter-webmvc` and `spring-boot-starter-validation`;
- the sole direct test dependency is `spring-boot-starter-test`;
- no excluded persistence, AI, provider, security, or cloud dependency was added;
- `./mvnw verify` passes 23 tests and packages an executable JAR;
- the packaged JAR contains the fixture and starts successfully on Java 21.

This historical S3 evidence did not itself authorize Android integration; T-007/S4 subsequently implemented and validated it, as recorded in `PROJECT_STATUS.md`.

## AI runtime dependencies (T-011/S6b, D-035)

- `com.microsoft.onnxruntime:onnxruntime` 1.30.0 (CPU), the same version as the Python runtime used for the notebook parity runs.
- `ai.djl.huggingface:tokenizers` 0.38.0, which bundles native libraries for linux/osx x86_64/aarch64. No runtime download is needed.
- Effect: the executable JAR grows to ~101 MB. The 526 MB bundle stays outside the JAR (`data/processed/bundle/`).

## Official sources

- [Spring Boot project and current release](https://spring.io/projects/spring-boot/)
- [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [Spring Boot build systems and starters](https://docs.spring.io/spring-boot/reference/using/build-systems.html)
- [Spring Boot servlet web applications](https://docs.spring.io/spring-boot/reference/web/servlet.html)
- [Spring Boot testing](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/maven-plugin/)
- [Apache Maven release history](https://maven.apache.org/docs/history.html)
- [Apache Maven Wrapper releases](https://github.com/apache/maven-wrapper/releases)
- [Javalin documentation](https://javalin.io/documentation)
- [Quarkus release stream](https://quarkus.io/releases/)
