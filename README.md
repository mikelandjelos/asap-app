# ASAP

**Automatska Semantička Analiza Proizvoda** je planirani mobilni AI MVP za prepoznavanje proizvoda pomoću barkoda, semantičku pretragu i personalizovane preporuke.

Projekat je u ranoj PoC fazi. Java/XML Android klijent u `android/` integriše Google Code Scanner, poziva Retrofit/Moshi I1 granicu i prikazuje odvojene ishode proizvoda i determinističkih demo rezultata; sva 32 lokalna testa i Android lint prolaze. Java/Spring Boot backend u `backend/` implementira deterministički I1 ugovor nad kontrolisanim lokalnim podacima i ima 23 prolazna testa. Ceo kontrolisani Android--backend tok fizički je potvrđen na telefonu, uključujući poznat i nepoznat proizvod, prazan i nedostupan rezultat i nedostupan backend; stvarni izvori podataka, semantička pretraga i personalizovane preporuke nisu implementirani. Početni predlog projekta nalazi se u [LaTeX izveštaju](report/report.tex), plan rada u [TODO listi](TODO.md), a prezentacija u [Beamer izvoru](presentation/asap-presentation.tex).

## Izveštaj za rok

Najnoviji obim (D-024/D-025): klasterizacija, personalizacija, PCA, MMR i doteran UI obavezni su za MVP. Svaka AI/statistička komponenta mora imati ponovljive notebook provere i stvarne rezultate za izveštaj; merodavni su [aktuelni zahtevi](docs/CURRENT_REQUIREMENTS.md) i [pravila evaluacije](docs/NOTEBOOK_VALIDATION.md). Google Code Scanner ostaje privremeno; tek nakon PoC/MVP-a skenerski deo se zamenjuje treniranim CNN/TFLite rešenjem za EAN/UPC, uz naknadni izbor dekodera. Nastavak počinje sažetim revidiranim planom iz [predaje sesije](docs/SESSION_HANDOFF.md), ne automatskim izvršavanjem T-010/S2.

Celovit radni nacrt je u [PDF izveštaju](report/report.pdf). Vidljivi markeri R01–R06 odvajaju uslovni tekst buduće završne verzije od ostvarenih rezultata; [spisak preostalog rada i dokaza](docs/REPORT_COMPLETION.md) vodi do svakog markera. Prioritet T-010 koristi postojeći mock tok, bez tvrdnje da je puni AI MVP završen. Prezentacija se završava na kraju; originalni implementacioni plan ostaje sačuvan.

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
