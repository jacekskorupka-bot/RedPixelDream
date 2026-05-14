# Log Projektu RedPixelDream

Niniejszy plik zawiera szczegółową historię rozwoju aplikacji RedPixelDream, dokumentując współpracę dewelopera z Agentem AI.

---

## 🚀 Historia Wersji i Przebieg Prac

### 📍 Wersja v1.1 (Aktualna)
**Status:** Wydana (Commit: `Release v1.1`)
**Główne nowości:** Personalizacja, Pogoda i Lokalizacja.

#### 1. Implementacja Modułu Pogody i Lokalizacji
*   **Funkcjonalność:** Pobieranie aktualnej temperatury z Open-Meteo API oraz nazwy miejscowości przez Geocoder.
*   **Technologia:** Wykorzystanie `thread` do asynchronicznych zapytań HTTP (bez klucza API).
*   **Uprawnienia:** Dodano `INTERNET` oraz `ACCESS_COARSE_LOCATION`.

#### 2. Moduł Personalizacji (Settings Panel)
*   **Interfejs:** Stworzono `activity_main.xml` jako centrum dowodzenia aplikacją.
*   **Opcje:** 
    *   Suwak jasności wygaszacza (`SeekBar`).
    *   Wybór koloru przewodniego: Czerwony, Bursztynowy, Zielony (`RadioGroup`).
*   **Trwałość:** Zastosowano `SharedPreferences` do zapisu preferencji użytkownika.

#### 3. Poprawki techniczne i optymalizacja
*   **Błąd AGP 9.0+:** Usunięto redundantny plugin `org.jetbrains.kotlin.android`, który blokował budowanie projektu w nowym środowisku.
*   **Dream Settings:** Utworzono `dream_info.xml` i powiązano go z usługą w manifestu, aktywując przycisk ustawień w opcjach systemowych wygaszacza.
*   **Kompilacja:** Wygenerowano plik APK (`app-debug.apk`) w wersji 1.1 (versionCode 2).

---

### 📍 Wersja v1.0
**Status:** Stabilna
**Opis:** Kompletna baza wygaszacza nocnego.

*   **Design:** Luksusowy interfejs nocny z czerwonymi konturami i minimalnym efektem poświaty.
*   **Kalendarz:** Podwójny widok – lista zadań na dziś/jutro (proporcja 60%) oraz graficzna siatka miesiąca (proporcja 40%).
*   **Month Grid:** Implementacja siatki typu monospace z dynamicznym wyróżnianiem bieżącego dnia (czerwone tło).
*   **Status Bar:** Integracja czasu, dnia tygodnia, poziomu baterii oraz godziny najbliższego alarmu.

---

### 📍 Wersja v0.9
**Główne nowości:** Optymalizacja zdrowia baterii.

*   **Battery Protection:** Dodano wizualne ostrzeżenie `(80% LIMIT)` i zmianę koloru na żółty po osiągnięciu bezpiecznego progu ładowania.
*   **Separator wydarzeń:** Wprowadzenie estetycznych linii (─────) w agendzie kalendarza.

---

### 📍 Wersja v0.7
**Główne nowości:** Interakcja sensoryczna.

*   **Proximity Sensor:** Utworzono `ProximityService.kt`, który nasłuchuje czujnika zbliżenia w tle i wybudza aplikację (funkcja "Wykrywanie obecności").
*   **Wake Logic:** Implementacja `setTurnScreenOn` w `MainActivity.kt`.

---

### 📍 Fundamenty (v0.1 - v0.5)
**Główne nowości:** Bezpieczeństwo ekranu i baza DreamService.

*   **Burn-in Protection:** System "Pixel Shift" – cykliczne, losowe przesunięcia całej zawartości o kilka pikseli (co minutę).
*   **Night Comfort:** Automatyczne ustawienie jasności na poziom `0.03f` (onAttachedToWindow).
*   **Typography:** Zmiana czcionki zegara na 120sp w wariancie obłym (sans-serif) dla lepszej czytelności i estetyki.

---

## 🛠️ Stos Technologiczny
*   **Język:** Kotlin
*   **IDE:** Android Studio (AGP 9.0+)
*   **AI:** Gemini / Agent Android Studio
*   **Kluczowe API:** DreamService, SensorManager, CalendarContract, Open-Meteo API.

---
*Log wygenerowany automatycznie przez Agenta AI. Ostatnia aktualizacja: 14.05.2026*
