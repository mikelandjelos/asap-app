# Accepted backend baseline

Status: Accepted and frozen under T-007/S1. The baseline is selected but remains unimplemented until T-007/S3.

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

## Deferred downloads and verification

S1 performs no download and creates no backend project. If this baseline is accepted, T-007/S3—not S2—will be authorized to add the wrapper and resolve:

- the checksum-pinned Apache Maven 3.9.16 binary distribution used by Maven Wrapper 3.3.3;
- the Spring Boot 4.1.1 parent/plugin metadata and the accepted direct dependencies;
- managed transitive runtime libraries, including Spring MVC, Jackson, and embedded Tomcat;
- managed test libraries, including Spring Boot test support, JUnit Jupiter, AssertJ, and Hamcrest.

The scaffold must then pass `backend/mvnw verify` and package an executable JAR. Exact wrapper-distribution checksums must be taken from Apache's official release metadata and committed with the wrapper configuration during S3.

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
