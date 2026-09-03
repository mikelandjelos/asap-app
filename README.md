# ASAP

**Automatska Semantička Analiza Proizvoda** je planirani mobilni AI MVP za prepoznavanje proizvoda pomoću barkoda, semantičku pretragu i personalizovane preporuke.

Projekat je trenutno u fazi predloga i pripreme: aplikativni kod još nije implementiran. Početni predlog projekta nalazi se u [LaTeX izveštaju](report/report.tex) ([PDF](build/report.pdf)), plan rada u [TODO listi](TODO.md), a prezentacija koja će se dopunjavati tokom razvoja u [Beamer izvoru](presentation/asap-presentation.tex) ([PDF](build/asap-presentation.pdf)).

## Dokumentacija

Za lokalno generisanje PDF dokumenata potreban je TeX Live:

```sh
mkdir -p build
pdflatex -output-directory=build report/report.tex
lualatex -output-directory=build presentation/asap-presentation.tex
lualatex -output-directory=build presentation/asap-presentation.tex
```

Izvorni DOCX ostaje u roditeljskom direktorijumu repozitorijuma, a skenirane radne beleške su u `meditations/`.
