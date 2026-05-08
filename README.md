# RedPixelDream

**RedPixelDream** to minimalistyczny i funkcjonalny wygaszacz ekranu (DreamService) dla systemu Android, zaprojektowany specjalnie do użytku nocnego. Aplikacja skupia się na czytelności, niskim obciążeniu wzroku oraz ochronie wyświetlaczy OLED/AMOLED przed wypaleniem.

## 🌟 Funkcje (Wersja 0.6)

- **Zegar nocny z obramowaniem**: Duży zegar w kolorze głębokiej czerwieni. Zastosowano unikalny efekt "pustej" czcionki (tylko kontur), co minimalizuje świecenie matrycy i chroni wzrok.
- **Dynamiczny Dzień Tygodnia**: Pod godziną wyświetlana jest nazwa dnia w pasującym, smukłym stylu konturowym.
- **Rozbudowany Kalendarz**: Automatyczne pobieranie wydarzeń z systemowego kalendarza na **dzisiaj** oraz **jutro** z wyraźnymi nagłówkami sekcji.
- **Pasek statusu 50/50**: Dolna część ekranu podzielona na informacje o poziomie baterii (zielony) oraz najbliższym zaplanowanym alarmie (pomarańczowy).
- **Zaawansowana ochrona Burn-in**: Inteligentny system przesuwający całą zawartość ekranu co 60 sekund o losową liczbę pikseli, zapobiegający utrwalaniu obrazu na panelach AMOLED.
- **Ultra-niska jasność**: Automatyczne wymuszanie jasności na poziomie 0.03f, idealne do całkowicie ciemnych pomieszczeń.
- **Wymuszona orientacja Landscape**: Wygaszacz automatycznie przełącza się w tryb poziomy (z obsługą czujnika), co jest idealne dla telefonów na ładowarkach biurkowych.
- **Natychmiastowy Podgląd**: Możliwość przetestowania wyglądu wygaszacza bezpośrednio po uruchomieniu aplikacji z poziomu Android Studio lub ikony launchera.
- **Optymalizacja kodu**: Usunięte zagnieżdżone wagi layoutów, poprawiona dostępność (Accessibility) oraz pełna obsługa zasobów `strings.xml`.

## 🚀 Jak używać?

1. **Instalacja**: Skompiluj projekt w Android Studio i zainstaluj na telefonie.
2. **Uprawnienia**: Przy pierwszym uruchomieniu otwórz aplikację, aby nadać uprawnienia do odczytu kalendarza.
3. **Aktywacja**:
   - Wejdź w **Ustawienia systemu Android**.
   - Przejdź do: **Wyświetlacz** -> **Wygaszacz ekranu** (lub *Daydream*).
   - Wybierz z listy **Czerwony Pixel**.
   - W ustawieniach wygaszacza możesz wybrać, kiedy ma się aktywować (np. "Podczas ładowania").

## 🛠️ Technologie

- **Język**: Kotlin
- **Platforma**: Android SDK 36 (Android 16+)
- **Architektura**: DreamService API
- **UI**: XML Layouts (RelativeLayout/LinearLayout) + Programowe rysowanie efektów Paint (Stroke).

## 📸 Podgląd

*(Miejsce na zrzut ekranu)*
Stylistyka opiera się na czerwonych konturach na idealnie czarnym tle, co zapewnia maksymalny kontrast przy minimalnym zużyciu energii.

## 📝 Autor

**Jack** - [Profil GitHub](https://github.com/jacekskorupka-bot)

---
*Projekt stworzony z myślą o miłośnikach minimalizmu i spokojnego snu.*
