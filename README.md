# RedPixelDream

**RedPixelDream** to minimalistyczny i funkcjonalny wygaszacz ekranu (DreamService) dla systemu Android, zaprojektowany specjalnie do użytku nocnego. Aplikacja skupia się na czytelności, niskim obciążeniu wzroku oraz ochronie wyświetlaczy OLED/AMOLED przed wypaleniem.

---
### 🤖 O projekcie
Ten projekt jest wyjątkowy – został zbudowany w środowisku **Android Studio** przy ścisłej współpracy człowieka z potężnym modelem AI **Gemini**. Dzięki połączeniu kreatywnej wizji autora i wsparcia technologicznego AI, udało się stworzyć narzędzie, które łączy luksusową estetykę z wysoką funkcjonalnością.

---

## 🌟 Funkcje (Wersja 1.0 - Stable)

- **Luksusowy Design (Inter/Montserrat Style)**: Zegar w kolorze głębokiej czerwieni z unikalnym efektem konturu (outline), co minimalizuje świecenie matrycy i chroni wzrok.
- **Wykrywanie Obecności (Proximity)**: Uruchamia wygaszacz automatycznie po wykryciu zbliżenia (np. machnięcie ręką nad telefonem), wybudzając przy tym ekran.
- **Podwójny Kalendarz**: 
    - Lista wydarzeń na **dzisiaj** i **jutro**.
    - Graficzna siatka miesiąca z wyraźnym, negatywowym zaznaczeniem bieżącego dnia.
- **Pasek Statusu 50/50**: Informacje o poziomie baterii (zielony) oraz najbliższym alarmie (pomarańczowy).
- **Battery Protection**: Ostrzeżenie wizualne (żółty kolor i napis 80% LIMIT), gdy poziom naładowania przekroczy bezpieczny próg.
- **Zaawansowana ochrona Burn-in**: Inteligentny system "Pixel Shift" przesuwający zawartość ekranu co 60 sekund.
- **Ultra-niska jasność**: Automatyczne wymuszanie jasności 0.03f dla komfortu w nocy.
- **Wymuszona orientacja Landscape**: Idealna dla telefonów na ładowarkach i podstawkach.

## 🚀 Jak używać?

1. **Instalacja**: Skompiluj projekt w Android Studio i zainstaluj na telefonie.
2. **Uprawnienia**: Przy pierwszym uruchomieniu nadaj uprawnienie do kalendarza oraz (ważne!) "Wyświetlaj nad innymi aplikacjami" w ustawieniach systemowych Androida.
3. **Aktywacja**:
   - Wejdź w **Ustawienia systemu Android**.
   - Przejdź do: **Wyświetlacz** -> **Wygaszacz ekranu**.
   - Wybierz **Czerwony Pixel**.

## 🛠️ Technologie

- **Język**: Kotlin
- **IDE**: Android Studio
- **AI Support**: Gemini
- **Architektura**: DreamService API, Foreground Services (Sensors)

## 📝 Autor

**Jack** - [Profil GitHub](https://github.com/jacekskorupka-bot)

---
*Projekt RedPixelDream v1.0 - Stworzony z pasją, zbudowany z inteligencją.*
