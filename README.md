# ASAP

**Automatska Semantička Analiza Proizvoda** je planirani mobilni AI MVP za prepoznavanje proizvoda pomoću barkoda, semantičku pretragu i personalizovane preporuke.

Projekat je u ranoj PoC fazi. Java/XML Android klijent nalazi se u `android/`, koristi prihvaćeni Android 16 build baseline i integriše Google Code Scanner za EAN/UPC barkodove bez dozvole aplikacije za kameru. Build, lokalni testovi i Android lint prolaze, ali skeniranje još nije potvrđeno na fizičkom uređaju. Backend, podaci i preporuke nisu implementirani. Početni predlog projekta nalazi se u [LaTeX izveštaju](report/report.tex), plan rada u [TODO listi](TODO.md), a prezentacija koja će se dopunjavati tokom razvoja u [Beamer izvoru](presentation/asap-presentation.tex).

## Android PoC

```sh
cd android
ANDROID_HOME=/home/mih/Android/Sdk ./gradlew testDebugUnitTest lintDebug assembleDebug
```

Ekran nudi akciju skeniranja, status i očitanu vrednost. Ova faza samo gradi APK; instalacija i potvrda ponašanja na telefonu pripadaju zasebno odobrenom podzadatku T-005/S3.

Operativna dokumentacija za nastavak rada kroz nezavisne sesije počinje u [`docs/README.md`](docs/README.md). Pravila za agente nalaze se u [`AGENTS.md`](AGENTS.md), a trenutno stanje projekta u [`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md).

## Dokumentacija

Za lokalno generisanje PDF dokumenata potreban je TeX Live. Kompletne naredbe i pravila rada nalaze se u [`docs/WORKFLOW.md`](docs/WORKFLOW.md).

```sh
mkdir -p build
pdflatex -output-directory=build report/report.tex
lualatex -output-directory=build presentation/asap-presentation.tex
lualatex -output-directory=build presentation/asap-presentation.tex
```

Izvorni DOCX ostaje u roditeljskom direktorijumu repozitorijuma, a skenirane radne beleške su u `meditations/`.
