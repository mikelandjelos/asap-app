# ASAP — plan rada

Radni cilj je operativan AI MVP. Plan je izveden iz beleški `meditations/sept_3.pdf` i početnog projektnog izveštaja, uz izmene D-024/D-025 od 2026-10-06: klasterizacija, personalizacija, PCA, MMR i doteran UI neophodni su za prihvatanje MVP-a. Sve AI/statističke komponente zahtevaju notebook verifikaciju i stvarne rezultate za izveštaj (`docs/CURRENT_REQUIREMENTS.md`, `docs/NOTEBOOK_VALIDATION.md`). Stara granica 80/95% više nije kriterijum prihvatanja.

## Najnoviji zahtev i predaja sesije — D-024

- [x] Evidentirati novi obim i pripremiti predaju drugom agentu; bez promene aplikacije.
- [x] Predložiti sažet revidirani plan sa obaveznom klasterizacijom, personalizacijom, PCA, MMR, doteranim UI-em i notebook dokazima; tražiti odobrenje pre implementacije. (T-011 plan odobren 2026-10-06, D-026.)
- [ ] Posle završenog PoC/MVP-a zameniti samo skenerski deo: CNN treniran na dokumentovanom skupu EAN/UPC slika, TFLite na telefonu i ZXing/drugi dekoder ili posebno odobren end-to-end pristup. Google Code Scanner ostaje privremeno.
- [x] U revidiranom dizajnu dopuniti kanonske dijagrame klasterizacijom i kasnijom zamenom skenera. Prezentaciju ostaviti za kraj. (T-011/S1: arhitektura + novi cevovod preporuka; prezentacijski renderi odloženi.)

T-010/S1 je prihvaćen kao nacrt; T-009/S1 čeka prihvatanje. Detalji su u `docs/SESSION_HANDOFF.md`.

## Prioritet — T-011: AI MVP (D-026)

Plan je odobren 2026-10-06; svaki podzadatak zahteva posebno odobrenje. Detalji u `docs/PLANS.md`. Izveštaj se temeljno prerađuje posle MVP-a, zatim prezentacija.

- [x] S1: projektne odluke (skup podataka, embedding model, skladište, uloge klasterizacije/PCA/MMR, profil istorije, notebook matrica, UI pravac) — samo dokumentacija. (`docs/AI_MVP_DESIGN.md`, D-027; prihvaćeno 2026-10-06 uz D-028.)
- [x] S1a: tekst aplikacije i fixture podaci prevedeni na engleski (D-028); 32 Android testa, lint 0, 23 backend testa.
- [x] S2: ograničene probe (26 poziva), ruter spajanjem polja daje 100 % naziv/brend/kategorija naspram 58–67 %; izabran redosled izvora i offline katalog od 10.000 proizvoda (D-029, notebook 00).
- [x] S3: notebook 01 — skup podataka, embedding i top-N pretraga. (e5-small > MiniLM; hibrid 0,9·e5 + 0,1·TF-IDF značajno bolji od oba; ONNX paritet 0,9999999; D-030.)
- [x] S4: notebook 02 — klasterizacija i PCA. (Prostor tipa proizvoda, k = 60, čistoća 0,78; PCA redukcija nije usvojena; mapa PCA(2); D-031.)
- [x] S5: notebook 03 — personalizacija i MMR. (Višeinteresni profil, β = 0,4, +0,007 nDCG@10 značajno; MMR λ = 0,6: −4,9 % nDCG, +21 % raznovrsnost; D-032.)
- [ ] S6: backend pipeline i v2 API ugovor sa proverom saglasnosti sa notebook-ovima.
  - [x] S6a: verzionisani paket artefakata + parity fiksture; nezavisna provera prolazi (D-034).
  - [x] S6b: Java učitavanje paketa, ONNX enkoder, char TF-IDF, hibridna pretraga, parity testovi. (32/32 testova; D-035.)
  - [ ] S6c: Java profil, MMR, klasteri/PCA; v2 ugovor i endpoint; v1 ostaje.
  - [ ] S6d: ruter izvora (OFF, UPCitemdb) sa keširanjem; testovi sa simuliranim odgovorima.
- [ ] S7: Android istorija i doteran UI.
- [ ] S8: end-to-end provera na telefonu i izveštaj sa stvarnim rezultatima.

## T-010: mock MVP i nacrt izveštaja (S2–S3 odloženi, D-026)

Korisnik je 2026-10-06 odobrio plan T-010 i S1 za ubrzanu pripremu nacrta izveštaja; realizacija narednih koraka i dalje zahteva posebno odobrenje. T-009 je pauziran, bez zatvaranja ili gubitka postojećeg rada. Puni AI MVP ostaje cilj nakon ovog ograničenog odstupanja. Prezentacija se završava tek na kraju. Markeri i dokazi vode se u `docs/REPORT_COMPLETION.md`.

- [x] T-010/S1: napisati celovit srpski nacrt izveštaja sa vidljivim markerima i spiskom nedostajućih dokaza. (Prihvaćen kao radni nacrt 2026-10-06; biće prerađen posle MVP-a.)
- [ ] T-010/S2: pripremiti minimalan ponovljiv mock demo na postojećem Android/backend toku.
- [ ] T-010/S3: uskladiti izveštaj, prezentaciju i README sa proverenim demo stanjem i pripremiti predaju.

## 0. Osnova projekta i dokumentacija

- [x] Postaviti repozitorijum i osnovnu strukturu projekta.
- [x] Preneti postojeći DOCX izveštaj u LaTeX.
- [x] Postaviti Beamer prezentaciju koja će se dopunjavati tokom projekta.
- [x] Usvojiti PlantUML kao glavni alat za softverske dijagrame.
- [x] Koristiti Mermaid za jednostavnije dijagrame pogodne za Markdown dokumentaciju.
- [x] Inicijalizovati `AGENTS.md` i operativnu dokumentaciju za rad kroz nezavisne sesije.
- [x] Definisati izvore istine, pravila predaje konteksta i kriterijum završetka zadatka.
- [x] Dodati izvorne PlantUML dijagrame arhitekture i tokova podataka.
- [ ] (Kontinualno) Dopunjavati status, odluke, predaju sesije, izveštaj, slučajeve upotrebe, dijagrame i prezentaciju posle svake značajne promene.

## 1. Razvojno okruženje

- [x] Izabrati Java ili Kotlin kao jezik mobilne aplikacije i dokumentovati odluku.
- [x] Instalirati i proveriti JDK/JVM, Android Studio i Android SDK.
- [x] Izdvojiti potrebne biblioteke i njihove verzije.
- [x] Napraviti mali tehnički eksperiment sa Google Code Scanner API-jem iz ML Kit ponude.
- [x] Definisati ponovljiv lokalni postupak za pokretanje, testiranje i izgradnju projekta.

## 2. Arhitektura i izvori podataka

- [x] Precizirati arhitekturu mobilne aplikacije, API servisa, servisa preporuka i skladišta podataka.
- [x] Definisati granice MVP-a i plan implementacije po iteracijama.
- [x] Postaviti početne projekte za mobilnu aplikaciju i backend.
- [x] Definisati modele proizvoda, korisničke interakcije i preporuke. (T-008 je prihvaćen i zatvoren: model proizvoda, interakcija, istorije i preporuka, AI granica i dva kanonska dijagrama.)
- [x] Pronaći i proceniti API-je za podatke o proizvodima na osnovu barkoda. (T-009 zatvoren kroz T-011/S2, D-029.)
- [x] Izabrati rezervni izvor ili skup podataka za razvoj bez zavisnosti od eksternog API-ja. (OFF/OBF/OPFF izvoz, 10.000 proizvoda, D-029.)
- [ ] Definisati način čuvanja metapodataka i vektorskih reprezentacija proizvoda.
- [ ] Dokumentovati slučajeve upotrebe i tok podataka od skeniranja do preporuke.

## 3. Data-driven PoC

- [x] Pripremiti reprezentativan skup proizvoda sa barkodom, nazivom, opisom i kategorijom. (T-011/S2–S3)
- [x] Izabrati model za generisanje semantičkih vektorskih reprezentacija. (T-011/S2–S3)
- [x] Implementirati generisanje i čuvanje embedding vektora. (T-011/S2–S3)
- [ ] Implementirati top-N semantičku pretragu kosinusnom sličnošću.
- [ ] Definisati i implementirati obaveznu klasterizaciju. (Definisano i provereno u notebook-u 02, D-031; integracija u S6.)
- [ ] Implementirati i notebook eksperimentima proveriti obaveznu MMR diversifikaciju rezultata.
- [x] Definisati i testirati korisnički profil kao centroid istorije interakcija. (Notebook 03; usvojen višeinteresni profil, D-032.)
- [ ] Izmeriti kvalitet i odziv PoC-a pre integracije u sistem.
- [ ] Odlučiti da li će PoC biti zasebno postavljen ili simuliran lokalno na završnoj prezentaciji.

## 4. MVP implementacija i integracija

- [x] Implementirati kameru i skeniranje barkoda na mobilnom uređaju.
- [x] Povezati barkod sa kontrolisanim metapodacima proizvoda preko backend API-ja i fizički potvrditi I1 tok.
- [ ] Integrisati semantičku pretragu i preporuke.
- [ ] Implementirati osnovni UI za proizvod, slične proizvode i personalizovane preporuke.
- [ ] Dodati obradu grešaka, praznih rezultata i nedostupnosti eksternih servisa. (I1 stanja skenera, veze i domenskih ishoda su implementirana i relevantni fizički slučajevi potvrđeni; budući stvarni provider mora proširiti ovu proveru.)
- [ ] Dodati automatske testove za ključne tokove.
- [ ] Predstaviti radnu verziju nastavniku i drugim timovima; zabeležiti datum i komentare u izveštaju.
- [ ] Sprovesti dogovorene korekcije i pripremiti konačnu verziju MVP-a.

## 5. Evaluacija i završna predaja

- [ ] Definisati merljive kriterijume uspeha za skeniranje, pretragu i preporuke.
- [ ] Testirati MVP sa potencijalnim korisnicima u dogovoru sa nastavnikom.
- [ ] Uneti datum, podatke o korisnicima i komentare u Izveštaj 5.
- [ ] Dopuniti svih pet delova izveštaja stvarnim odlukama, rezultatima i povratnim informacijama.
- [ ] Dopuniti prezentaciju arhitekturom, demonstracijom, rezultatima i naučenim lekcijama.
- [ ] Proveriti završni kriterijum: operativna MVP aplikacija, demonstrabilan PoC, kompletan izveštaj i kompletna prezentacija.

## Posle MVP-a — D-028

- [ ] PCA prikaz „ti naspram drugih korisnika (prijatelja)“ — zahteva naloge i društvene podatke (D-033).
- [ ] Praćenje drifta i ponovno fitovanje: kako katalog raste, meriti drift (npr. objašnjena varijansa novih proizvoda u postojećoj PCA bazi, udaljenost do centroida, pokrivenost TF-IDF rečnika) i po pragu ponovo fitovati PCA, klastere i TF-IDF, uz verzionisanje artefakata i notebook proveru.
- [ ] (Stretch) Konsolidator podataka: pozadinski proces koji izvorne zapise (API-ji, izvozi, scraper) spaja u jedinstveni „zlatni zapis“ po proizvodu (razrešavanje entiteta, GTIN aliasi, rešavanje konflikata po pouzdanosti i svežini izvora, poreklo po polju), sa eventualnom konzistentnošću; zahteva bazu podataka.
- [ ] (Stretch) AI web scraper u backend-u koji postepeno prikuplja podatke o proizvodima i obogaćuje katalog; zahteva poštovanje robots.txt/uslova korišćenja, licenci i ograničenja brzine, uz evidentirano poreklo podataka. Uz vektorsku bazu i pretragu ovo je napredni stretch cilj.
- [ ] Uvesti pravu bazu podataka sa vektorskom pretragom (PostgreSQL + pgvector ili Qdrant) iza interfejsa pretrage; uporediti sa in-memory osnovom.

## Završna faza pre izveštaja — D-033

- [ ] Postaviti backend na jeftin server posle MVP-a (npr. DigitalOcean/Hetzner, ~2–4 GB RAM), HTTPS, release build aplikacije sa produkcionim URL-om; razmotriti int8 kvantizaciju modela radi manje memorije (zahteva parity proveru).
- [ ] Dashboard lične analitike pored istorije i PCA prikaza (sadržaj i metrike dogovoriti; radi se na kraju MVP-a).
- [ ] Doraditi PCA prikaz „ti naspram tema“ (centroid korisnika među centroidima 60 tema); detalje prodiskutovati pre S7.

## Obavezne dodatne MVP provere — D-025

- [ ] Implementirati PCA i dogovorenu vizuelnu analitiku; proveriti metod i performanse u notebook-u.
- [ ] Dogovoriti i implementirati doteran UI; proveriti čitljivost, pristupačnost, sve ishode i upotrebljivost na telefonu.
- [ ] Za CNN/TFLite, embedding, pretragu, klasterizaciju, personalizaciju, PCA, MMR i svaku dodatnu AI/statističku komponentu pripremiti izvršiv notebook, stvarne metrike, grafikone/tabele i vezu sa izveštajem.
- [ ] Proveriti ponovljivost eksperimenata i saglasnost notebook metoda sa aplikacionom implementacijom.
