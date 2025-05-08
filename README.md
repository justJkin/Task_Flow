
Programowanie zespołowe laboratorium _**1**_ grupa _**2**_

# Dokumentacja projetu: **System do zarządzania zadaniami w przedsiębiorstwie - TaskFlow**

## Zespoł projetowy:
- Paweł Grechuta (Project menager)
- Sebastian Domin (Full Stack)
- Paulina Hawro (Full Stack)
- Mikołaj Abram (Full Stack)
- Mateusz Chorab (Full Stack)

## Opis programu / systemu
TaskFlow to system zarządzający zadaniami w małej/średniej firmie, sprawdzi się zarówno w IT, ale także w marketingu czy sprzedaży! System idzie w myśl zasady *dziel i zwyciężaj* i stawia na pracownika. 

System daje każdemu użytkownikowi dostęp do osobistej *Tablicy* Kanban, na której użytkownik ma możliwość ogranizować własną pracę przeciagając między *Sektorami* swoje *Zadania* i *Cząstki*.
## Cel projektu 
TaskFlow ma za zadanie to usprawnić i usystematyzować pracę nad *Projektami*, wykazać się pracownikom i pracować trochę wg wsłasnych reguł. Wspólna pula zadać zachęca do interesowania się co jest do zrobienia i wybierać bardziej odpowiadające zadanie, jednocześnie może mieć zarezerwowane 3 zadania na raz. Jednocześnie *Menager* ma możliwość przypisania zadań do konkretnego *Usera*.

## Zakres projektu 
TaskFlow zakłada 3 poziomy uprawnień:
 - Admin
 - Menager
 - User

*Zespoły* zawierają *Userów* są zarządzane przez *Menagerów* i tworzone przez *Adminów*.

Problemy rozwiązuje się za pomocą procesów.
*Admin* otrzymuje rozpisany *Projekt* i dzieli go na sekcje (np. baza danych, backend, frontend/graficy, montażyści, copywritterzy, social media). *Sekcje* przypisuje do zespołów i przekazuje *Menagerowi*.

*Menager*  otrzymaną sekcję rozbija na zadania do wykonania członkom zespołu (np. stworzenie schematu, implementacja bazy danych, testy bazy/stworzenie sloganu, napisanie scenariusza reklamy, stworzenie opisów). *Zadanie* te trafiają do wspólnej puli zadań i to *Userzy* wybierają sobie zadania do wykonania.

*User* przeciąga sobie zadanie i dzieli je na *Cząstki* czyli części elementarne zadania np. (Opracowanie tabel, stworzenie tabel wymiany, stworzenie pól tabel, dodanie relacji/określenie celu, stworzenie listy pomysłów, selekcja pomysłów, dopracowanie sloganu). 

*Cząstki* po opracowaniu mają przypisywane wagi, które sumują się do 10, waga reprezentuje nakład pracy i czasu na jego wykonanie. Wykonanie *Cząstki* wypełnia pasek postępu *Zadania*, które oprócz własnych wag mają również priorytet, który determinuje kolejność ich wykonywania w *Sekcji* *Projektu*
*Zespoły* zawierają *Userów* są zarządzane przez *Menagerów* i tworzone przez *Adminów*.

## Wymagania stawiane aplikacji / systemowi 
### Wymagania funkcjonalne
## 1. Zarządzanie projektami, zadaniami i cząstkami CRUD
- Tworzenie nowego *Projektu/Zadania/Cząstki* za pomocą prostego kreatora
- Wyświetlanie listy z elementami 
- Edycja istniejących elementów
- Usuwanie istniejących elementów
- Zależne od uprawnień
- Pula Zadań
- Obsługa wag i priorytetów
## 2. Tablica Kanban 
- Osobna dla każdego użytkownika
- Podzielona na *Sektory* (np. DO WYKONANIA, W TRAKCIE, DO TESTÓW, GOTOWE)
- Przeciąganie zadań między *Sektorami* w celu zmiany ich statusu
- Pula Zadań 
## 3. Kalendarz
- Wewnętrzny *Kalendarz* z *Terminarzem*
- *Lista* ze zbliżającymi się *Teminami*
- (Opcjonalnie) obsługa kalendarza systemowego lub/i Google/Office
## 4. Panele uzytkownika
Zawierają statystyki, ranking, raportowania. Dziedziczy zawartość.
- #### Panel Usera
  Zawiera panel do zarządzania swoimi danymi 
- #### Panel Menagera
  Zawiera panel do zarządzania *Zespołem*
- #### Panel Admina
  Zawiera panel do zarządzania *Zespołami*, użytkownikami, Logi systemowe

## 5. Powiadomienia
- Powiadomienia o czynnościach dotyczących użytkownika, terminach, komentarzach
- Listy:
  - Najnowsze (top10)
  - Nieprzeczytanie
  - Przeczytanie (archiwum)
## 6. Poziomy uprawnień
Użytkownicy o różnych poziomach uprawnień mają dostęp do różnych funkcjonalności w ramach tego samego modułu
## 7. Statystyki i raporty
Dostosowane do poziomu uprawnień
- Statystyki - dotyczą wykonywanych zadań: osobistych, zespołowych i Projektowych

- Ranking - na tle *Zespołu*, *Zespołowe*, systemowe

- Raporty - postęp prac, wykonane zadania

## 8. Pasek postępu
Dzieli się na *Projekt/Zadanie/Cząstki*.
Poziom postępu zależy od wag (dla Zadania suma 10, dla Projektu wybiera *Admin*), wypełnianie *Cząstek* o danej wadze wypełnia zadanie w określonym stopniu.
Wykonywanie *Cząstekczek* uzupełnia *Zadania*, których wykonywanie wypełnia *Projekt* w zależności od wag.

## 9. Prioretyzacja 
*Projekt/Zadanie/Cząstki* mają swój priorytet determinujący kolejność ich wykonywania

## 10. Filtrowanie i Sortowanie 
- Listy w systemie mają być filtrowane  wg różnych kryteriów (np. kategoria, status, osoba odpowiedzialna)
- Listy w systemie mają być sortowane wg różnych kryteriów (np. data utworzenia, priorytet)

## 11. Komentarze
Do Zadań i Cząstek można dodać komentarz będący, krótkim tekstem, notatką lub pytaniem.

### Wymagania niefunkcjonalne:

1. Wydajność - Obsługa jednoczesnych logowań do 50 użytkowników bez opóźnień, wyszukiwanie w bazie danych w czasie nie dłuższym niż 5 sekund
2. Bezpieczeństwo- Szyfrowanie danych użytkowników
3. Skalowalność- System zdolny do obsługi wzrostu liczby użytkowników bez konieczności zmian
4. Dostępność - System dostępny 24 godziny na dobę z przerwą techniczną
5. Łatwość użycia - Intuicyjny i  przyjazny interfejs dla użytkowników końcowych.

# Panele / zakładki systemu, które będą oferowały potrzebne funkcjonalności
## 1. Dashboard
- *User* 
	1. Powitanie: Data i godzina 
	2. Szybki Terminarz: Przegląd z zadaniami o najkrótszym terminie
	3. Powiadomienia: Top5 najnowszych
	4. Statystyki: Zbiór podstawowych statystyk mający na celu motywowanie użytkownika poprzez informowanie go o jego wydajności
- *Menager* 
	Zawiera dodatkowo statystyki dotyczące zespołu
- *Admin* 
Zawiera statysyki dotyczące wszystkich zepołów i systemu 

## 2. Zadania
Zakładka do zarządzania zadaniami do wykonania, zawiera pasek postępu
-*User* 
Ma dostępne zadania rozpisane przez *Menagera*, które może zarezerwować, podzielić na *Cząstki* dodać komentarze, oznaczyć jako wykonane.
- *Menager*
Może dodatkowo śledzić prace członków *Zespołu*, tworzyć *Zadania* na podstawie wymagań od *Admina* 
- *Admin* 
Może tu tworzyć *Projekt*, podzielić go na sekcje, z opisami wymagań i przypisać je do *Zespołów*

## 3. Tablica
Indywidualna *Tablica* Kanban, pozwala organizować prace użytkownikom przeciągając zadania z puli między *Sektorami*. Utworzone *Cząstki* przypisane do *Zadań* trafiają do *Sektora*  DO ZROBIENIA.

## 4. Kalendarz
Zakładka z terminarzem, zawiera kalendarz z zaznaczonymi terminami wykonania zadań. Pod kartą z kalendarzem zawiera listę wszystkich terminów w kolejności chronologicznej.

## 5. Zarządzanie
Zawiera panele użytkownika zależne od uprawnień:
- *User*
Zawiera *Zespół*, podgląd wykonanych *Zadań*, Statystyki, Ranking i funkcje Raportowania, zarządzanie profilem.
- *Menager*
Może dodatkowo w swoim panelu zarządzać swoim *Zespołem*, bardziej szczegółowe rankingi i statystyki dotyczące czlonków zespołu osobno.
-*Admin*
Admin w swoim panelu może zarządzać, *Zespołami*, użytkownikami, zawiera logi systemowe

## 6. Statystyki i Raporty
Dostęp do tego modułu będzie z poziomu zakładki Zarządzanie, rodzaje i ilość statystyk oraz danych do raportu zależy od poziomu uprawnień

- *User*

- *Menager*

- *Admin*
  
## 7. Powiadomienia
Zawiera 3 listy:
Najnowsze (Top 10)
Nieprzeczytane
Przeczytane (archiwum powiadomień)

## Przepływ informacji w środowisku systemu 
Np. Scentralizowany oparty na bazie danych.

## Użytkownicy aplikacji i ich uprawnienia 
## Poziomy uprawnień w systemie

### ADMIN – najwyższy poziom uprawnień

**Zakres:**
- Zarządzanie systemem i użytkownikami
- Tworzenie i zarządzanie Projektami, Zespołami, Zadaniami
- Dostęp do logów systemowych
- Pełny dostęp do wszystkich danych i statystyk

**Uprawnienia:**
- Zarządzanie:
  - Użytkownikami (CRUD)
  - Grupami/Zespołami (CRUD)
  - Projektami (CRUD)
  - Zadaniami (planowanie i tworzenie)
- Raporty systemowe
- Statystyki pełne (wszystkie zespoły i system)
- Monitor aktywności (logi)
- Przypisywanie sekcji Projektu do Zespołów
- Planowanie i zarządzanie przerwami technicznymi
- Kalendarz
- Dashboard (pełne statystyki)
- Tworzenie i śledzenie postępu Projektów
- Dostęp do grup i przypisywanie zadań
- Systemowe powiadomienia


### MANAGER – średni poziom uprawnień

**Zakres:**
- Zarządzanie zespołem i zadaniami w ramach przypisanej sekcji projektu
- Raportowanie, statystyki zespołu
- Tworzenie i przydzielanie zadań

**Uprawnienia:**
- Tworzenie i przydzielanie Zadań
- Zarządzanie zespołem i jego członkami
- Dashboard zespołowy
- Statystyki i raporty zespołowe
- Kalendarz (zadania i spotkania)
- Priorytetyzacja i nadawanie wag zadaniom
- Śledzenie postępu członków zespołu
- Podgląd grupy
- Dostęp do Tablicy (Kanban)
- Dostęp do cząstek zadań
- Powiadomienia zespołowe
- Filtrowanie i sortowanie zadań
- Komentarze do zadań i cząstek
- Panel Managera
- Ranking zespołu

### USER – podstawowy poziom uprawnień

**Zakres:**
- Zarządzanie własną pracą, wybór zadań, dzielenie ich na cząstki
- Osobisty kalendarz, tablica Kanban, powiadomienia
- Raportowanie swojej aktywności

**Uprawnienia:**
- Wybór zadań z puli i przypisywanie do siebie (do 3 jednocześnie)
- Tworzenie i edycja cząstek
- Nadawanie wag cząstkom
- Organizacja pracy na Tablicy Kanban (przeciąganie między sektorami)
- Dodawanie komentarzy do Zadań i Cząstek
- Osobisty Dashboard:
  - Powitanie, Data i Godzina
  - Terminarz (Top zadania)
  - Statystyki osobiste
- Profil użytkownika (edycja danych)
- Kalendarz i przypomnienia
- Powiadomienia (nowe, przeczytane, archiwum)
- Statystyki osobiste
- Raporty osobiste
- Ranking osobisty
- Panel Usera
- Filtrowanie i sortowanie

### Wspólne funkcjonalności (dostosowane do poziomu uprawnień)

- Dashboard
- Zarządzanie zadaniami (CRUD)
- Kalendarz i terminarz
- Tablica (Kanban)
- Powiadomienia
- Raporty i statystyki
- Komentarze
- Filtrowanie i sortowanie


# Interesariusze

## Interesariusze wewnętrzni
Interesariusze wewnętrzni to osoby lub grupy bezpośrednio związane z przedsiębiorstwem, które korzystają z systemu lub są zaangażowane w jego rozwój i zarządzanie:

- **Zarząd / Kierownictwo wyższego szczebla** – odpowiedzialni za decyzje strategiczne, finansowanie oraz nadzór nad realizacją projektu.
- **Administratorzy systemu** – zarządzają dostępami, konfiguracją systemu, bezpieczeństwem danych.
- **Managerowie zespołów** – planują i przydzielają zadania, monitorują postęp pracy swoich zespołów.
- **Użytkownicy końcowi (pracownicy)** – codziennie korzystają z systemu do wykonywania swoich zadań.
- **Działy IT / DevOps** – odpowiadają za rozwój, utrzymanie i integrację systemu z innymi narzędziami.
- **Analitycy biznesowi** – zbierają wymagania, monitorują efektywność procesów i raportują wyniki.
- **Zespół projektowy / Scrum team** – odpowiada za projektowanie, implementację oraz wdrażanie systemu.

---

## Interesariusze zewnętrzni
Interesariusze zewnętrzni to osoby lub organizacje spoza przedsiębiorstwa, które mają wpływ na projekt lub są nim zainteresowane:

- **Klienci końcowi** – odbiorcy produktów lub usług firmy, których satysfakcja pośrednio zależy od efektywności działania systemu.
- **Partnerzy biznesowi** – firmy współpracujące przy realizacji projektów lub dostarczające usługi/technologie.
- **Dostawcy oprogramowania / usług chmurowych** – zapewniają infrastrukturę lub komponenty systemu.
- **Konsultanci zewnętrzni** – doradzają w zakresie wdrożeń, optymalizacji procesów lub bezpieczeństwa.
- **Organy regulacyjne i audytorskie** – dbają o zgodność projektu z przepisami prawa i normami branżowymi.
- **Inwestorzy / udziałowcy** – mogą być zainteresowani wynikami projektu i jego wpływem na wartość firmy.

---

## Diagramy UML
### Diagram przypadków użycia
- ###### [Diagram przypadków użycia]!![Diagram przypadków użycia](https://github.com/user-attachments/assets/3f5837a1-4b7e-4d39-88db-b7df9114c9f4)

### Diagramy aktywności
- ###### [Diagram aktywności]
  Administrator wyszukuje użytkownika i wybiera opcję usunięcia. Jeśli użytkownik ma aktywne zadania, system wymaga ich przeniesienia. W przeciwnym razie administrator potwierdza usunięcie, a system usuwa użytkownika i informuje o tym.
  
  ![AKTYWNOSCI1](https://github.com/user-attachments/assets/d66336c4-d3a5-4ff2-88fd-6ecf14970098)
- ###### [Diagram aktywności]
  Użytkownik otwiera tablicę Kanban. System pobiera przypisane zadania i wyświetla je w odpowiednich kolumnach. Jeśli brak zadań, system informuje użytkownika.
  
  ![AKTYWNOSCI2](https://github.com/user-attachments/assets/db9291d5-b8a1-48bc-9d5e-7c43577b63d8)
- ###### [Diagram aktywności]
  Administrator wprowadza dane nowego użytkownika. System waliduje dane i sprawdza, czy e-mail już istnieje. Jeśli tak, system zwraca błąd. W przeciwnym razie użytkownik zostaje zapisany, a system wysyła powiadomienie o założeniu konta.
  
  ![AKTYWNOSCI3](https://github.com/user-attachments/assets/9ad34911-9bc8-4b7c-82b1-7cb406244abe)
- ###### [Diagram aktywności]
  Diagram przedstawia proces tworzenia grupy przez administratora – od otwarcia panelu, przez nadanie nazwy i wybór użytkowników, aż po utworzenie grupy lub uzupełnienie brakujących danych.
  
  ![AKTYWNOSCI4](https://github.com/user-attachments/assets/afb5ce3e-44c7-4bb1-8a48-7ff9e70c1557)
  
Proces logowania menedżera do systemu, wyboru projektu i zadania, a następnie przypisania użytkownika do zadania. System zapisuje zmiany i powiadamia użytkownika o przypisaniu.

- ###### [Diagram aktywności - przypisanie menagera do zadania]
  ![diag_aktywnosci_przypisanieMenagera](https://github.com/user-attachments/assets/c0e26e9a-c4af-46a6-a709-52721823b3a9)
  
Proces logowania administratora do systemu, wyboru użytkownika i modyfikacji jego poziomu dostępu. System zapisuje zmiany i stosuje nowe uprawnienia.

- ###### [Diagram aktywności - modyfikacja poziomu dostępu użytkownika]
  ![diag_aktywnosci_przypisanieRol](https://github.com/user-attachments/assets/7da6c483-b005-45cb-94ae-4e713a905318)
  
Proces logowania użytkownika do systemu, wyświetlania powiadomień oraz oznaczania ich jako przeczytane. System aktualizuje status powiadomień po ich przeczytaniu.

- ###### [Diagram aktywności - przeglądanie i zarządzanie powiadomieniami]
  ![diag_aktywnosci_powiadomienia](https://github.com/user-attachments/assets/1c077260-1ba9-470d-96b6-3cad30740b60)

### Diagramy sekwencji

Proces dodawania nowego użytkownika przez administratora, obejmujący walidację danych, sprawdzenie istnienia użytkownika i zapis do bazy danych.

- ###### [Diagram sekwencji - dodawanie nowego użytkownika]
  ![diag_sekwencji1](https://github.com/user-attachments/assets/30ab6746-b8ed-4290-b39b-44f6c2fa8e5a)
  
Proces tworzenia nowej grupy przez administratora, w tym walidacja danych, przypisanie użytkowników i wysyłanie powiadomień.

- ###### [Diagram sekwencji - tworzenie nowej grupy]
  ![diag_sekwencji2](https://github.com/user-attachments/assets/bd88eb4c-3322-4dec-b619-6ebf4128b855)
  
Proces usuwania użytkownika z systemu, obejmujący sprawdzenie aktywnych zadań, wyświetlenie ostrzeżeń i potwierdzenie usunięcia.

- ###### [Diagram sekwencji - usuwanie użytkownika]
  ![diag_sekwencji3](https://github.com/user-attachments/assets/7fd6f0a0-0a2f-4e8b-99f0-bdcb156af10b)
  
Proces wyświetlania przypisanych zadań użytkownika na tablicy Kanban, z podziałem na kolumny i informacją o braku zadań, jeśli takie istnieją.

- ###### [Diagram sekwencji - przeglądanie tablicy Kanban]
  ![diag_sekwencji4](https://github.com/user-attachments/assets/98354737-c39f-4036-aad4-b0a60c18163f)
  
- Proces przydzielania użytkownika do zadania przez managera.
  
- ###### [Diagram sekwencji]-przydzielenie użytkownika do zadania
   ![1 sekwencji](https://github.com/user-attachments/assets/0b82e049-54a3-4bec-a42e-62b6fad57839)

- Proces przypisania uprawnień przez administratora

- ###### [Diagram sekwencji]-przypisanie uprawnień przez administratora
  ![2 sekwencji](https://github.com/user-attachments/assets/6d484194-85f3-42dd-ae88-380ef6982d5e)

- Proces przeglądania powiadomień przez użytkownika

- ###### [Diagram sekwencji]- przeglądanie powiadomień przez użytkownika]
  ![9](https://github.com/user-attachments/assets/417176fd-cd4e-4ac1-9606-17c14954530f)




Wstawić rys. diagramu UML
- ###### [Diagram klas]
  Wstawić rys. diagramu UML

## Baza danych
###### Diagram ERD
![poprawka](https://github.com/user-attachments/assets/046f7885-7da5-4d9f-8cf5-b43e11b032f5)




###### Skrypt do utworzenia struktury bazy danych

<details>
  <summary>Kliknij, aby rozwinąć kod</summary>

  ```sql
-------------------
-- Typy enumowe --
-------------------
CREATE TYPE role_enum AS ENUM ('admin', 'manager', 'user');
CREATE TYPE task_status_enum AS ENUM ('To Do', 'In Progress', 'Done');
CREATE TYPE project_status AS ENUM ('draft', 'active', 'archived');

-------------------------
-- Struktury tabel --
-------------------------
CREATE TABLE team (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(20) NOT NULL,
    last_name VARCHAR(30) NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role role_enum NOT NULL,
    team_id INT REFERENCES team(id),
    created_at TIMESTAMP DEFAULT NOW(),
    last_login TIMESTAMP
);

CREATE TABLE project (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    total_weight INT NOT NULL DEFAULT 100 CHECK (total_weight = 100),
    status project_status DEFAULT 'draft',
    start_date DATE NOT NULL,
    end_date DATE,
    admin_id INT REFERENCES "user"(id) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    progress SMALLINT DEFAULT 0
);

CREATE TABLE milestone (
    id SERIAL PRIMARY KEY,
    project_id INT REFERENCES project(id) ON DELETE CASCADE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    weight INT NOT NULL CHECK (weight > 0 AND weight <= 100),
    progress SMALLINT DEFAULT 0,
    team_id INT REFERENCES team(id) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE task (
    id SERIAL PRIMARY KEY,
    milestone_id INT REFERENCES milestone(id) ON DELETE CASCADE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status task_status_enum DEFAULT 'To Do',
    priority SMALLINT CHECK (priority BETWEEN 1 AND 5),
    weight INT NOT NULL CHECK (weight > 0),
    progress SMALLINT DEFAULT 0,
    due_date DATE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE subtask (
    id SERIAL PRIMARY KEY,
    task_id INT REFERENCES task(id) ON DELETE CASCADE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    weight INT NOT NULL CHECK (weight > 0),
    is_done BOOLEAN DEFAULT false,
    priority SMALLINT CHECK (priority BETWEEN 1 AND 5),
    due_date DATE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE task_assignment (
    user_id INT REFERENCES "user"(id) NOT NULL,
    task_id INT REFERENCES task(id) NOT NULL,
    assigned_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (user_id, task_id)
);

CREATE TABLE comment (
    id SERIAL PRIMARY KEY,
    task_id INT REFERENCES task(id),
    subtask_id INT REFERENCES subtask(id),
    user_id INT REFERENCES "user"(id) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    CHECK (COALESCE(task_id, subtask_id) IS NOT NULL)
);

CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES "user"(id) NOT NULL,
    task_id INT REFERENCES task(id),
    subtask_id INT REFERENCES subtask(id),
    type VARCHAR(20) NOT NULL CHECK (type IN ('reminder', 'update', 'alert')),
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE kanban_sector (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    order_position SMALLINT NOT NULL UNIQUE
);

CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES "user"(id),
    action_type VARCHAR(50) NOT NULL,
    target_table VARCHAR(50) NOT NULL,
    target_id INT NOT NULL,
    old_values JSONB,
    new_values JSONB,
    executed_at TIMESTAMP DEFAULT NOW()
);

-------------------------
-- Indeksy --
-------------------------
-- Klucze obce
CREATE INDEX idx_user_team_id ON "user"(team_id);
CREATE INDEX idx_task_milestone_id ON task(milestone_id);
CREATE INDEX idx_subtask_task_id ON subtask(task_id);
CREATE INDEX idx_milestone_project_id ON milestone(project_id);
CREATE INDEX idx_milestone_team_id ON milestone(team_id);

-- Filtry
CREATE INDEX idx_task_status ON task(status) WHERE status != 'Done';
CREATE INDEX idx_task_due_date ON task(due_date);
CREATE INDEX idx_subtask_due_date ON subtask(due_date);
CREATE INDEX idx_notification_user_read ON notification(user_id, is_read) WHERE is_read = false;

-- Raporty
CREATE INDEX idx_project_progress ON project(progress);
CREATE INDEX idx_audit_log_executed_at ON audit_log(executed_at);
CREATE INDEX idx_comment_created_at ON comment(created_at);

-- Optymalizacje
CREATE INDEX idx_task_team_composite ON task(milestone_id, priority) WHERE status = 'To Do';
CREATE INDEX idx_project_milestone_composite ON milestone(project_id, weight);

---------------------------------
-- Funkcje             --
---------------------------------
CREATE OR REPLACE FUNCTION update_progress_chain()
RETURNS TRIGGER AS $$
BEGIN
    -- Aktualizacja zadania
    WITH task_stats AS (
        SELECT 
            SUM(weight) FILTER (WHERE is_done) AS done,
            SUM(weight) AS total
        FROM subtask
        WHERE task_id = COALESCE(NEW.task_id, OLD.task_id)
    )
    UPDATE task
    SET 
        progress = COALESCE((done * 100) / NULLIF(total, 0), 0),
        updated_at = NOW()
    FROM task_stats
    WHERE id = COALESCE(NEW.task_id, OLD.task_id);

    -- Aktualizacja kamienia milowego
    WITH milestone_stats AS (
        SELECT
            SUM(weight * progress) / 100 AS done,
            SUM(weight) AS total
        FROM task
        WHERE milestone_id = (
            SELECT milestone_id FROM task WHERE id = COALESCE(NEW.task_id, OLD.task_id)
        )
    )
    UPDATE milestone
    SET progress = COALESCE((done * 100) / NULLIF(total, 0), 0)
    FROM milestone_stats
    WHERE id = (SELECT milestone_id FROM task WHERE id = COALESCE(NEW.task_id, OLD.task_id));

    -- Aktualizacja projektu
    WITH project_stats AS (
        SELECT
            SUM(weight * progress) / 100 AS done,
            SUM(weight) AS total
        FROM milestone
        WHERE project_id = (
            SELECT project_id FROM milestone WHERE id = (
                SELECT milestone_id FROM task WHERE id = COALESCE(NEW.task_id, OLD.task_id)
            )
        )
    )
    UPDATE project
    SET progress = COALESCE((done * 100) / NULLIF(total, 0), 0)
    FROM project_stats;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION validate_milestone_weights()
RETURNS TRIGGER AS $$
DECLARE 
    prj_status project_status;
    total INT;
BEGIN
    SELECT p.status, SUM(m.weight) + NEW.weight
    INTO prj_status, total
    FROM project p
    LEFT JOIN milestone m ON m.project_id = p.id AND m.id != NEW.id
    WHERE p.id = NEW.project_id
    GROUP BY p.status;

    IF prj_status = 'active' AND total != 100 THEN
        RAISE EXCEPTION '[AKTYWNY PROJEKT] Suma wag musi wynosić 100 (aktualnie: %)', total;
    ELSIF total > 100 THEN
        RAISE EXCEPTION 'Suma wag kamieni nie może przekroczyć 100 (aktualnie: %)', total;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION validate_task_weights()
RETURNS TRIGGER AS $$ 
DECLARE 
    milestone_weight INT;
    tasks_sum INT;
BEGIN
    SELECT weight INTO milestone_weight 
    FROM milestone 
    WHERE id = NEW.milestone_id;

    SELECT COALESCE(SUM(weight), 0) INTO tasks_sum 
    FROM task 
    WHERE milestone_id = NEW.milestone_id;

    IF tasks_sum > milestone_weight THEN
        RAISE EXCEPTION 'Suma wag zadań (%%) przekracza wagę kamienia (%%)', 
            tasks_sum, milestone_weight;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION validate_subtask_weights()
RETURNS TRIGGER AS $$ 
DECLARE 
    task_weight INT;
    subtasks_sum INT;
BEGIN
    SELECT weight INTO task_weight 
    FROM task 
    WHERE id = NEW.task_id;

    SELECT COALESCE(SUM(weight), 0) INTO subtasks_sum 
    FROM subtask 
    WHERE task_id = NEW.task_id;

    IF subtasks_sum > task_weight THEN
        RAISE EXCEPTION 'Suma wag cząstek (%%) przekracza wagę zadania (%%)', 
            subtasks_sum, task_weight;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION check_project_activation()
RETURNS TRIGGER AS $$
DECLARE
    total_weight INT;
BEGIN
    IF NEW.status = 'active' THEN
        SELECT COALESCE(SUM(weight), 0) INTO total_weight
        FROM milestone
        WHERE project_id = NEW.id;
        
        IF total_weight != 100 THEN
            RAISE EXCEPTION 'Nie można aktywować projektu. Suma wag: %/100', total_weight;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-------------------------
-- Triggery --
-------------------------


CREATE TRIGGER subtask_changes
AFTER INSERT OR UPDATE OF is_done, weight OR DELETE ON subtask
FOR EACH ROW EXECUTE FUNCTION update_progress_chain();

CREATE TRIGGER check_task_weights
AFTER INSERT OR UPDATE ON task
FOR EACH ROW EXECUTE FUNCTION validate_task_weights();

CREATE TRIGGER check_subtask_weights
AFTER INSERT OR UPDATE ON subtask
FOR EACH ROW EXECUTE FUNCTION validate_subtask_weights();

CREATE CONSTRAINT TRIGGER check_milestone_weights
AFTER INSERT OR UPDATE ON milestone
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW EXECUTE FUNCTION validate_milestone_weights();

CREATE TRIGGER trg_project_activation
BEFORE UPDATE OF status ON project
FOR EACH ROW EXECUTE FUNCTION check_project_activation();

```
</details>

###### Opis bazy danych
## 1. Tabele i ich przeznaczenie

| Tabela             | Przeznaczenie                                                                 |
|--------------------|-------------------------------------------------------------------------------|
| `team`             | Przechowuje zespoły użytkowników                                             |
| `user`             | Dane użytkowników systemu z rolą i przypisaniem do zespołu                   |
| `project`          | Główna jednostka zarządzania z kontrolą wag i statusu                        |
| `milestone`        | Etapy projektu przypisane do zespołów z określoną wagą                       |
| `task`             | Zadania w ramach kamienia milowego z priorytetami i terminami               |
| `subtask`          | Elementarne części zadań wpływające na postęp                                |
| `task_assignment`  | Historia przypisań zadań do użytkowników                                     |
| `comment`          | Komentarze do zadań i cząstek                                                |
| `notification`     | Powiadomienia systemowe dla użytkowników                                     |
| `kanban_sector`    | Konfiguracja sekcji tablicy Kanban                                           |
| `audit_log`        | Rejestr zdarzeń i zmian w systemie                                           |

## 2. Kluczowe relacje

- **Hierarchia projektu**:  
  `project` ← (1:N) `milestone` ← (1:N) `task` ← (1:N) `subtask`
  
- **Przypisania**:  
  `user` ↔ (N:M) `task` przez `task_assignment`  
  `user` → `team` → `milestone`

- **Zależności funkcjonalne**:  
  `comment` i `notification` powiązane z `task`/`subtask`  
  `audit_log` śledzi zmiany we wszystkich tabelach

## 3. Opis logiki

- **System wagowy**:  
  - Projekt: stała wartość 100 jednostek  
  - Kamienie milowe: suma ≤100 (draft) lub =100 (active)  
  - Zadania: suma wag ≤ waga kamienia  
  - Cząstki: suma wag ≤ waga zadania  

- **Progres**:  
  - Automatyczne przeliczanie od cząstek do projektu  
  - Formuła: `(suma_wykonanych_wag / suma_wszystkich_wag) * 100`

- **Cykl życia projektu**:  
  - **Draft**: edycja kamieni milowych  
  - **Active**: blokada zmian struktury wag  
  - **Archived**: tylko do odczytu  

## 4. Opis ENUM

| Typ                | Wartości                          | Przeznaczenie                              |
|--------------------|-----------------------------------|--------------------------------------------|
| `role_enum`        | admin, manager, user              | Uprawnienia użytkowników                  |
| `task_status_enum` | To Do, In Progress, Done          | Status realizacji zadań                   |
| `project_status`   | draft, active, archived           | Etap cyklu życia projektu                 |

## 5. Opis indeksów

| Kategoria           | Przykłady                                | Cel                                      |
|---------------------|------------------------------------------|------------------------------------------|
| Klucze obce         | `idx_task_milestone_id`                  | Optymalizacja JOIN-ów                   |
| Filtry              | `idx_task_status WHERE != 'Done'`        | Szybkie wyszukiwanie aktywnych zadań    |
| Raporty             | `idx_audit_log_executed_at`              | Analiza czasowa zdarzeń                 |
| Optymalizacje       | `idx_task_team_composite`                | Efektywne pobieranie puli zadań         |

## 6. Opis funkcji

### `update_progress_chain()`
- **Cel**: Aktualizacja łańcucha postępu (subtask → task → milestone → project)  
- **Wyzwalacz**: Zmiany w tabeli `subtask`  
- **Algorytm**: Proporcjonalne przeliczenie wag wykonanych elementów

### `validate_milestone_weights()`
- **Cel**: Kontrola spójności wag kamieni milowych  
- **Warunki**:  
  - Projekty aktywne: suma = 100  
  - Projekty wersje robocze: suma ≤100

### `validate_task_weights()`
- **Cel**: Weryfikacja sumy wag zadań w kamieniu  
- **Warunek**: Suma wag zadań ≤ waga kamienia

### `validate_subtask_weights()`
- **Cel**: Kontrola sumy wag cząstek w zadaniu  
- **Warunek**: Suma wag cząstek ≤ waga zadania

### `check_project_activation()`
- **Cel**: Weryfikacja przed aktywacją projektu  
- **Warunek**: Suma wag kamieni = 100

## 7. Opis triggerów

| Trigger                   | Tabela      | Akcja                         | Funkcja                        |
|---------------------------|-------------|-------------------------------|--------------------------------|
| `subtask_changes`         | `subtask`   | INSERT/UPDATE/DELETE          | `update_progress_chain`        |
| `check_milestone_weights` | `milestone` | INSERT/UPDATE                 | `validate_milestone_weights`   |
| `check_task_weights`      | `task`      | INSERT/UPDATE                 | `validate_task_weights`        |
| `check_subtask_weights`   | `subtask`   | INSERT/UPDATE                 | `validate_subtask_weights`     |
| `trg_project_activation`  | `project`   | UPDATE status                 | `check_project_activation`     |

**Mechanizm bezpieczeństwa**:  
- Wszystkie triggery działają w kontekście transakcji  
- 4-poziomowa kontrola spójności wag (projekt → kamień → zadanie → cząstka)  
- Automatyczne blokady przy naruszeniu reguł biznesowych  
- Kaskadowe aktualizacje postępu w całej hierarchii


## Wykorzystane technologie 
- Język Java 24
  - JavaFX  (IntelliJ)
- Baza danych PostgreSQL
- GitHub
- Jira

## Pliki instalacyjne wraz z opisem instalacji i konfiguracji wraz pierwszego uruchomienia
