# ASAP

**Automatska Semantička Analiza Proizvoda** je planirani mobilni AI MVP za prepoznavanje proizvoda pomoću barkoda, semantičku pretragu i personalizovane preporuke.

Projekat je u ranoj PoC fazi. Java/XML Android klijent u `android/` integriše Google Code Scanner za EAN/UPC barkodove; build, sedam lokalnih testova i Android lint prolaze, a na fizičkom telefonu potvrđena su dva uspešna skeniranja i pravilno otkazivanje. Java/Spring Boot backend u `backend/` implementira prihvaćeni deterministički I1 ugovor nad kontrolisanim lokalnim podacima; njegovih 23 testa i Maven build prolaze. Android još nije povezan sa backendom, a stvarni izvori podataka, semantička pretraga i personalizovane preporuke nisu implementirani. Početni predlog projekta nalazi se u [LaTeX izveštaju](report/report.tex), plan rada u [TODO listi](TODO.md), a prezentacija u [Beamer izvoru](presentation/asap-presentation.tex).

## Android PoC

```sh
cd android
ANDROID_HOME=/home/mih/Android/Sdk ./gradlew testDebugUnitTest lintDebug assembleDebug
```

Ekran nudi akciju skeniranja, status i očitanu vrednost. Tehnički PoC skenera je fizički potvrđen; brojevi skeniranih barkodova nisu sačuvani u repozitorijumu.

## Backend I1

```sh
cd backend
./mvnw verify
java -jar target/asap-backend-0.0.1-SNAPSHOT.jar
```

Endpoint `POST /api/v1/scan-queries` vraća odvojene ishode proizvoda i preporuka iz kontrolisanog fixture skupa. Rezultati sa oznakom `DETERMINISTIC_FIXTURE` služe samo za proveru integracije i nisu AI preporuke.

Operativna dokumentacija za nastavak rada kroz nezavisne sesije počinje u [`docs/README.md`](docs/README.md). Pravila za agente nalaze se u [`AGENTS.md`](AGENTS.md), trenutno stanje projekta u [`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md), a prihvaćena granica operativnog MVP-a i plan iteracija u [`docs/MVP_SCOPE.md`](docs/MVP_SCOPE.md).

## Dokumentacija

Za lokalno generisanje PDF dokumenata potreban je TeX Live. Kompletne naredbe i pravila rada nalaze se u [`docs/WORKFLOW.md`](docs/WORKFLOW.md).

```sh
mkdir -p build
pdflatex -output-directory=build report/report.tex
lualatex -output-directory=build presentation/asap-presentation.tex
lualatex -output-directory=build presentation/asap-presentation.tex
```

Izvorni DOCX ostaje u roditeljskom direktorijumu repozitorijuma, a skenirane radne beleške su u `meditations/`.
