# ASAP — plan rada

Radni cilj je operativan AI MVP sa približno 80% planiranih funkcionalnosti. Plan je izveden iz beleški `meditations/sept_3.pdf` i početnog projektnog izveštaja.

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
- [ ] Instalirati i proveriti JDK/JVM, Android Studio i Android SDK.
- [ ] Izdvojiti potrebne biblioteke i njihove verzije.
- [ ] Napraviti mali tehnički eksperiment sa Google Code Scanner API-jem iz ML Kit ponude.
- [ ] Definisati ponovljiv lokalni postupak za pokretanje, testiranje i izgradnju projekta.

## 2. Arhitektura i izvori podataka

- [ ] Precizirati arhitekturu mobilne aplikacije, API servisa, servisa preporuka i skladišta podataka.
- [ ] Definisati granice MVP-a i plan implementacije po iteracijama.
- [ ] Postaviti početne projekte za mobilnu aplikaciju i backend.
- [ ] Definisati modele proizvoda, korisničke interakcije i preporuke.
- [ ] Pronaći i proceniti API-je za podatke o proizvodima na osnovu barkoda.
- [ ] Izabrati rezervni izvor ili skup podataka za razvoj bez zavisnosti od eksternog API-ja.
- [ ] Definisati način čuvanja metapodataka i vektorskih reprezentacija proizvoda.
- [ ] Dokumentovati slučajeve upotrebe i tok podataka od skeniranja do preporuke.

## 3. Data-driven PoC

- [ ] Pripremiti reprezentativan skup proizvoda sa barkodom, nazivom, opisom i kategorijom.
- [ ] Izabrati model za generisanje semantičkih vektorskih reprezentacija.
- [ ] Implementirati generisanje i čuvanje embedding vektora.
- [ ] Implementirati top-N semantičku pretragu kosinusnom sličnošću.
- [ ] Oceniti opcionu MMR diversifikaciju rezultata.
- [ ] Definisati i testirati korisnički profil kao centroid istorije interakcija.
- [ ] Izmeriti kvalitet i odziv PoC-a pre integracije u sistem.
- [ ] Odlučiti da li će PoC biti zasebno postavljen ili simuliran lokalno na završnoj prezentaciji.

## 4. MVP implementacija i integracija

- [ ] Implementirati kameru i skeniranje barkoda na mobilnom uređaju.
- [ ] Povezati barkod sa metapodacima proizvoda preko backend API-ja.
- [ ] Integrisati semantičku pretragu i preporuke.
- [ ] Implementirati osnovni UI za proizvod, slične proizvode i personalizovane preporuke.
- [ ] Dodati obradu grešaka, praznih rezultata i nedostupnosti eksternih servisa.
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

## Opciono posle osnovnog MVP-a

- [ ] Klasterizovati proizvode ili korisničke interakcije.
- [ ] Dodati PCA projekciju i jednostavnu vizuelnu analitiku u aplikaciji.
