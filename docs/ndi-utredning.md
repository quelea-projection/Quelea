# Utredning: NDI-utgång från projektorskärmen till OBS Studio

Status: fungerande testversion med bild, verifierad mot OBS på macOS. Utgångspunkt: `master` (`bdfda2b0`), november 2026.

## Slutsats

**Det fungerar.** Testversionen i den här branchen skickar Queleas projektorbild till OBS Studio över NDI, inklusive text, logotyp, svart skärm och video. Det krävs inga ändringar i hur bilden ritas, och ingen projektor behöver vara ansluten.

Tre saker återstår innan det är klart för en PR:

1. **Test på Windows och Linux.** Allt är hittills verifierat på en M1 Pro.
2. **Licensen.** NDI-runtimen är proprietär och Quelea är GPLv3. Quelea skeppar inga NDI-filer, men SDK:ns fullständiga licensavtal behöver läsas.
3. **Ljud.** Video skickas utan ljud, eftersom ljudet går direkt till ljudkortet.

## Resultat av testversionen

Testat med NDI 6.3.2, OBS 33.0.1 och DistroAV på Apple M1 Pro, Java 25 arm64.

| Test | Resultat |
|---|---|
| Unika bilder med video | 172 av 172 och 142 av 168 i två körningar, 28 bilder/s |
| Bildflöde med sång | 27–29 bilder/s, 1920×1080 BGRX |
| Utan projektor (dolt fönster) | Fungerar, virtuell yta på 1920×1080 |
| Synligt fönster 960×540, Retina | Skickas som 1920×1080 |
| Versbyte, svart skärm, logotyp | Följer med direkt |
| OBS tar emot källan | Ja, bekräftat med skärmdump från OBS |
| Start och stopp av sändaren | 12 omgångar, ingen krasch |
| Devolay-forken på x86_64, Java 17 | 180 av 180 bilder, samma som arm64 |

Queleas processorlast, i procent av en kärna:

| | Ingen mottagare | Med mottagare |
|---|---|---|
| Sång | 8 % | 61 % |
| Video | 26–29 % | 57–62 % |

Utan mottagare kostar NDI ingenting, eftersom ingen bild fångas då. Med mottagare går det åt lika mycket för stillastående text som för video. Det går troligen att sänka rejält genom att inte skicka om oförändrade bilder.

**Inte testat:**

- Windows och Linux, och Queleas paketerade Mac-version.
- Inställningspanelen: den laddas utan fel, men att slå på NDI där och spara är inte provat. Testerna använde `quelea.properties`.
- Felmeddelandet när NDI-runtimen saknas.
- Mottagning på en annan dator, och hack i projektorbilden vid videouppspelning.

I OBS pausas källan när den inte syns, enligt DistroAV:s standardinställning. Den måste alltså ligga i en aktiv scen för att ge bild.

## Behövs NDI alls?

NDI ger mest när OBS körs på **en annan dator** än Quelea, eller när man vill ha text med genomskinlig bakgrund. På samma dator går det redan i dag att använda fönsterinfångning i OBS. Andra sätt som används i dag, enligt [#356](https://github.com/quelea-projection/Quelea/issues/356):

| Sätt | Fungerar för | Begränsning |
|---|---|---|
| Fönster-/skärminfångning i OBS | Samma dator | Ingen genomskinlighet |
| Webbläsarkälla mot mobile lyrics-servern | Enbart text | Ingen bild eller video |
| HDMI-utgång till capture-kort | Annan dator | Kräver hårdvara, nyckling behövs för overlay |

## Tidigare arbete

Frågan är inte ny. [#336](https://github.com/quelea-projection/Quelea/issues/336) är öppen sedan 2021 och har en fungerande prototyp: [PaulD987/Quelea, branch `ndi-output`](https://github.com/PaulD987/Quelea/tree/ndi-output). Någon PR skapades aldrig. Underhållarna var positiva i princip, men oroliga för native-kod.

Prototypen är värd att läsa, men går inte att ta över rakt av:

- Den ligger 568 commits efter `master`.
- Den beror på Devolay 1.2.0 från Bintray, som inte längre finns. Gruppen `com.walker` saknas också på Maven Central.
- Den använder en **separat tredje skärm** med egna inställningar och ändrar flera drawers (+384/−37 rader i 18 filer i första commiten). Det ger bra overlay-text, men är inte samma sak som att spegla projektorn.
- Den skickar en bild per ritning (`sendNDISnapshot()`), alltså ingen löpande video, och allokerar nya buffertar varje gång.

## Så ritas projektorbilden

Allt som syns på projektorn ligger i en enda JavaFX-nod, `DisplayCanvas`, i `DisplayStage`. Det gör fångsten enkel: `canvas.snapshot()` används redan i `LivePanel.java:636` och `WebDrawer.java:84`.

| Innehåll | Följer med i en snapshot? | Varför |
|---|---|---|
| Sångtext, bibeltext, bilder, PDF | Ja | Noder i canvasen |
| Video och videobakgrund | Ja | `FXImageSink` → `ImageView` i canvasen |
| Webbsidor | Ja | `WebView` läggs i canvasen |
| Notiser, logotyp, svart skärm | Ja | Permanenta noder i canvasen |
| Testbild | Nej | Ligger utanför canvasen, i `DisplayStage` |
| PowerPoint via extern app | Nej | Eget fönster utanför Quelea (`PowerPointHandler`) |
| **Ljud** | **Nej** | `PlayBin` i `VidDisplay` har ingen egen ljud-sink, ljudet går till standardutgången |

## Mätning av bildfångst

Risken var att fångsten skulle störa projektorbilden, eftersom den körs på JavaFX-tråden. [`SnapBench.java`](ndi-utredning/SnapBench.java) efterliknar värsta fallet: en 1920×1080-canvas med text, där hela videobilden skrivs om varje pulse precis som `FXImageSink` gör.

Två körningar med synligt fönster. Intervallen visar skillnaden mellan körningarna.

| Läge | Snapshot p50 | Snapshot p95 | Läsning till buffert p50 | JavaFX-tråden upptagen |
|---|---|---|---|---|
| 1080p, 10 bilder/s | 5,8–7,0 ms | 15–18 ms | 0,45 ms | 9 % |
| 720p, 30 bilder/s | 3,8–4,6 ms | 8–32 ms | 0,2 ms | 14–23 % |
| **1080p, 30 bilder/s** | **6,3–6,5 ms** | **19–29 ms** | **0,46 ms** | **27–32 %** |
| 1080p, 60 bilder/s | 5,1–6,1 ms | 9–12 ms | 0,46 ms | 38–44 % |

Slutsatser:

- **Medelvärdet är inget problem, svansen kan vara det.** Enstaka fångster tar 20–30 ms, och längsta glappet mellan pulser med fångst var 67–212 ms mot cirka 30 ms utan. Det kan märkas som hack i video på projektorn.
- **Pulsfrekvensen sjönk** från cirka 119 till 94–102 per sekund vid 1080p i 30 bilder/s.
- **Skalning till 720p sparar förvånansvärt lite**, så 1080p bör vara standard.
- **Fångst med dolt projektorfönster fungerar.** Alla fångade bilder hade nytt innehåll (177 av 177 vid 30 bilder/s), och p95 var lägre (9 ms) eftersom bilden inte ritas två gånger. Quelea kan alltså skicka NDI utan ansluten projektor.

Det här täcker mätningen **inte**:

- Bara Apple M1 Pro med JavaFX ES2-pipeline. En dator med integrerad Intel-grafik kan bete sig helt annorlunda.
- NDI:s kodning är inte med, eftersom NDI-runtimen inte fanns installerad. Den körs på processorn och är den största okända kostnaden.
- Det är en syntetisk scen, inte Quelea med verklig video och GStreamer-avkodning.
- Skärmen var 120 Hz och fönstret större än skärmen, med korrekt 1920×1080-layout.

## Tre sätt att prata med NDI

Sökning på GitHub och Maven Central (november 2026) visar att Devolay och dess forkar är de enda Java-biblioteken för NDI. `Pluto-sun/NDI-Java` är ett affärssystem utan koppling till NDI-protokollet.

| | Devolay-forken 2.2.0-vic.2 | Devolay 2.1.1 | Egen bindning med JNA | GStreamer `ndisink` |
|---|---|---|---|---|
| Maven-koordinat | `io.github.vicvalentim:devolay` | `me.walkerknapp:devolay` | Ingen, JNA finns redan | Ingen i Java |
| Licens | Apache-2.0 | Apache-2.0 | GPLv3, vår kod | MPL-2.0 |
| Senaste release | Augusti 2026 | Juli 2024 | – | Aktivt |
| NDI-version | Headers 6.3.2 | NDI 4 enligt ärende #38 | Valfri | 5 och 6 |
| Apple Silicon | Ja | Nej | Ja | Beror på paket |
| Proprietär NDI-fil i paketet | Nej, laddar installerad runtime | Bara i `integrated` | Nej | Nej |
| Ljud | Ja, API finns | Ja, API finns | Måste byggas | Via `ndisinkcombiner` |
| Risk | En underhållare, 1 stjärna, natives från forken | Saknar arm64 och NDI 6 | Egna struct-definitioner och eget underhåll | Finns elementet i alla paket? |

**Vald lösning: Devolay-forken.** Egen bindning med JNA är reservplanen. Forken ger färdigt API för både video, ljud och mottagning, och laddar runtimen dynamiskt som licensen kräver. Paketet behåller namnrymden `me.walkerknapp.devolay`, så vi kan byta tillbaka till upstream om den återupptas. Linux arm64 saknas, så Raspberry Pi täcks inte.

Statisk granskning av forkens jar, utan att köra den:

- Kontrollsumman från Maven Central stämmer, och en signaturfil finns.
- macOS arm64-filen importerar bara `dlopen`, `getenv` och filkontroller, inga nätverksanrop.
- Windows x64-filen importerar bara `KERNEL32` och `msvcrt`.
- Java-koden använder varken `java.net` eller startar processer.

Provkörd med godkänt resultat, se [Resultat av testversionen](#resultat-av-testversionen). Forkens **mottagardel** kastar undantag på NDI:s statusmeddelande 101 (`source_change`), som saknas i dess enum. Quelea använder bara sändardelen, men felet bör rapporteras till forken.

GStreamer är intressant som andrahandsval för ljudets skull. `ndisink` finns i GStreamer 1.24.6 på den här Macen, men det är inte kontrollerat i Windows-, Linux-, Snap- eller Flatpak-paketen. NDI skulle också sluta fungera på installationer där GStreamer inte startar.

## Licens

Det här är inte juridisk rådgivning. NDI:s webbdokumentation tar inte upp GPL alls, och det fullständiga avtalet finns bara i SDK-nedladdningen.

Enligt [Software Distribution](https://docs.ndi.video/docs/sdk/software-distribution) och [Licensing](https://docs.ndi.video/docs/sdk/licensing):

- **Headerfiler** får distribueras med öppen källkod under MIT-licens.
- **Binärer** får bara skeppas om vårt licensavtal täcker NDI:s villkor, vilket är svårt att förena med GPLv3.
- **Alternativet** är att länka till NDI:s runtime-nedladdning och läsa platsen från en miljövariabel.
- **Krav på synlighet:** länk till ndi.video nära NDI-valet i appen, på webbplatsen och i dokumentationen, samt varumärkestexten i About-rutan.
- **Namn:** vill man ha "NDI" i produktnamnet ska man kontakta NDI. Det gäller inte en funktion i Quelea, men det är värt att bekräfta.

Föreslagen hållning: **skeppa inga NDI-filer**. Quelea laddar runtimen om den finns, annars är funktionen avstängd med en länk till nedladdningen. Samma upplägg föreslogs i #336, och [DistroAV](https://github.com/DistroAV/DistroAV) (GPL-2.0) kräver också att NDI Runtime installeras separat.

Dokumentationen nämner `NDI_RUNTIME_DIR_V5` och länken `ndi.link/NDIRedistV5` men också SDK 6.3.2, medan GStreamer-pluginet läser både V6 och V5. Koden bör därför leta efter båda, och aktuell länk behöver bekräftas.

I OBS krävs [DistroAV](https://github.com/DistroAV/DistroAV) (senaste release 6.2.2, oktober 2026) och NDI Runtime 6.

## Föreslagen design

Testversionen speglar projektorns `DisplayCanvas`, utan ändringar i drawers:

```text
JavaFX-tråden                              Sändartråd
Timer, 30 bilder/s
 └ canvas.snapshot(params, återanvänd bild)
 └ getPixels → ledig direkt-buffert  ───▶  NDI send (async)
                 (två buffertar)
```

- Nytt paket `org.quelea.services.ndi`. `NdiSender` kapslar in Devolay, så att biblioteket går att byta ut. `NdiOutput` fångar bilden.
- Två buffertar som växlas, eftersom asynkron sändning behöver behålla bilden tills nästa anrop. Är båda upptagna hoppas bilden över.
- Sändning på en egen tråd, så att en långsam kodare aldrig stoppar JavaFX-tråden.
- Ingen fångst utan mottagare. Kontrollen sker utanför JavaFX-tråden.
- `snapshot(callback, ...)`, som följer med nästa pulse i stället för att tvinga fram en egen rendering.
- Upplösningen följer skärmens pixlar, med taket 1920×1080.
- Inställningar under Server Settings: på/av, källnamn, bildfrekvens. Av som standard.
- Runtimen laddas bara när funktionen är på, så ingen annan påverkas.

Kvar att lösa:

- **Stillbilder:** hoppa över oförändrade bilder för att sänka lasten.
- **Länk till ndi.video:** finns som text i rubriken, men bör vara klickbar för att uppfylla NDI:s krav.
- **Dold utgång:** knappen som döljer projektorn stoppar inte NDI-bilden. Det behöver bestämmas om den ska göra det.

## Utanför första versionen

- **Ljud.** Kräver en egen ljud-sink i `VidDisplay`. Tills dess kan OBS ta ljudet på annat sätt.
- **Separat overlay-utgång** med genomskinlig bakgrund och eget tema, som prototypen hade. Det är troligen det många vill ha för livesändning, men är en större ändring.
- **Scenbild (stage view)** som egen NDI-källa.

## Nästa steg

1. Slå på NDI via inställningspanelen och bekräfta att det sparas och startar.
2. Testa på en typisk Windows-dator, med OBS på en annan dator.
3. Hoppa över oförändrade bilder för att sänka processorlasten.
4. Läs SDK:ns fullständiga licensavtal och fråga vid behov licensing@ndi.video om GPL.
5. Stäm av med underhållarna i #336 innan en PR skapas.

## Köra mätningen igen

Kräver JDK 25 och JavaFX 25. `JFX` ska peka på en katalog med JavaFX-jarfiler för din plattform.

```bash
javac --module-path "$JFX" --add-modules javafx.graphics --add-exports=javafx.graphics/com.sun.prism=ALL-UNNAMED -d out docs/ndi-utredning/SnapBench.java
```

```bash
java --module-path "$JFX" --add-modules javafx.graphics --add-exports=javafx.graphics/com.sun.prism=ALL-UNNAMED -cp out SnapBench
```

Lägg till `-Dhidden=true` för att mäta med dolt projektorfönster.
