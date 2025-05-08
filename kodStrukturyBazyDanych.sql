-------------------
-- Typy enumowe --
-------------------

/* Role systemowe użytkowników */
CREATE TYPE role_enum AS ENUM ('admin', 'manager', 'user');

/* Statusy realizacji zadań */
CREATE TYPE task_status_enum AS ENUM ('To Do', 'In Progress', 'Done');

/* Cykl życia projektu */
CREATE TYPE project_status AS ENUM ('draft', 'active', 'archived');

-------------------------
-- Struktury tabel --
-------------------------

/* Zespoły w organizacji */
CREATE TABLE team (
    id SERIAL PRIMARY KEY,                     -- Unikalny identyfikator
    name VARCHAR(50) NOT NULL UNIQUE,          -- Unikalna nazwa zespołu
    created_at TIMESTAMP DEFAULT NOW(),        -- Data utworzenia
    updated_at TIMESTAMP DEFAULT NOW()         -- Data ostatniej aktualizacji
);

/* Użytkownicy systemu */
CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(20) NOT NULL,           -- Imię użytkownika
    last_name VARCHAR(30) NOT NULL,            -- Nazwisko użytkownika
    email VARCHAR(50) UNIQUE NOT NULL,         -- Unikalny adres email
    password_hash VARCHAR(255) NOT NULL,       -- Zaszyfrowane hasło
    role role_enum NOT NULL,                   -- Przypisana rola
    team_id INT REFERENCES team(id),           -- Przynależność do zespołu
    created_at TIMESTAMP DEFAULT NOW(),        -- Data rejestracji
    last_login TIMESTAMP                       -- Ostatnia data logowania
);

/* Projekty zarządzane w systemie */
CREATE TABLE project (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,                -- Nazwa projektu
    description TEXT,                          -- Opis projektu
    total_weight INT NOT NULL DEFAULT 100 
        CHECK (total_weight = 100),            -- Stała pojemność projektu (100 jednostek)
    status project_status DEFAULT 'draft',     -- Aktualny status projektu
    start_date DATE NOT NULL,                  -- Data rozpoczęcia
    end_date DATE,                             -- Planowana data zakończenia
    admin_id INT REFERENCES "user"(id) NOT NULL, -- ID administratora projektu
    created_at TIMESTAMP DEFAULT NOW(),        -- Data utworzenia projektu
    progress SMALLINT DEFAULT 0                -- Ogólny postęp projektu
);

/* Kamienie milowe projektu */
CREATE TABLE milestone (
    id SERIAL PRIMARY KEY,
    project_id INT REFERENCES project(id) ON DELETE CASCADE NOT NULL, -- Powiązanie z projektem
    name VARCHAR(100) NOT NULL,                -- Nazwa kamienia milowego
    description TEXT,                          -- Szczegółowy opis
    weight INT NOT NULL 
        CHECK (weight > 0 AND weight <= 100),  -- Waga w kontekście projektu (1-100)
    progress SMALLINT DEFAULT 0,               -- Postęp realizacji kamienia
    team_id INT REFERENCES team(id) NOT NULL,  -- Zespół odpowiedzialny
    created_at TIMESTAMP DEFAULT NOW()         -- Data utworzenia
);

/* Zadania w ramach kamieni milowych */
CREATE TABLE task (
    id SERIAL PRIMARY KEY,
    milestone_id INT REFERENCES milestone(id) ON DELETE CASCADE NOT NULL, -- Powiązany kamień
    name VARCHAR(100) NOT NULL,                -- Tytuł zadania
    description TEXT,                          -- Szczegóły zadania
    status task_status_enum DEFAULT 'To Do',   -- Aktualny status realizacji
    priority SMALLINT 
        CHECK (priority BETWEEN 1 AND 5),      -- Priorytet (1 = najwyższy)
    weight INT NOT NULL 
        CHECK (weight > 0),                    -- Waga w ramach kamienia
    progress SMALLINT DEFAULT 0,               -- Postęp wykonania zadania
    due_date DATE,                             -- Termin realizacji
    created_at TIMESTAMP DEFAULT NOW(),        -- Data utworzenia
    updated_at TIMESTAMP DEFAULT NOW()         -- Data ostatniej aktualizacji
);

/* Cząstki składowe zadań */
CREATE TABLE subtask (
    id SERIAL PRIMARY KEY,
    task_id INT REFERENCES task(id) ON DELETE CASCADE NOT NULL, -- Powiązane zadanie
    name VARCHAR(100) NOT NULL,                -- Nazwa cząstki
    description TEXT,                          -- Opis szczegółowy
    weight INT NOT NULL 
        CHECK (weight > 0),                    -- Waga w ramach zadania
    is_done BOOLEAN DEFAULT false,             -- Status realizacji
    priority SMALLINT 
        CHECK (priority BETWEEN 1 AND 5),      -- Priorytet cząstki
    due_date DATE,                             -- Termin wykonania
    created_at TIMESTAMP DEFAULT NOW()         -- Data utworzenia
);

/* Przypisania zadań do użytkowników */
CREATE TABLE task_assignment (
    user_id INT REFERENCES "user"(id) NOT NULL, -- ID użytkownika
    task_id INT REFERENCES task(id) NOT NULL,  -- ID zadania
    assigned_at TIMESTAMP DEFAULT NOW(),       -- Data przypisania
    PRIMARY KEY (user_id, task_id)             -- Unikalna kombinacja
);

/* Komentarze do zadań/cząstek */
CREATE TABLE comment (
    id SERIAL PRIMARY KEY,
    task_id INT REFERENCES task(id),           -- Powiązane zadanie
    subtask_id INT REFERENCES subtask(id),     -- Powiązana cząstka
    user_id INT REFERENCES "user"(id) NOT NULL, -- Autor komentarza
    content TEXT NOT NULL,                     -- Treść komentarza
    created_at TIMESTAMP DEFAULT NOW(),        -- Data dodania
    CHECK (COALESCE(task_id, subtask_id) IS NOT NULL) -- Musi dotyczyć zadania lub cząstki
);

/* System powiadomień */
CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES "user"(id) NOT NULL,             -- Odbiorca
    task_id INT REFERENCES task(id),                        -- Powiązane zadanie
    subtask_id INT REFERENCES subtask(id),                  -- Powiązana cząstka
    type VARCHAR(20) NOT NULL 
        CHECK (type IN ('reminder', 'update', 'alert')),    -- Typ powiadomienia
    content TEXT NOT NULL,                                  -- Treść wiadomości
    is_read BOOLEAN DEFAULT false,                          -- Status przeczytania
    created_at TIMESTAMP DEFAULT NOW()                      -- Data utworzenia
);

/* Konfiguracja sekcji Kanban */
CREATE TABLE kanban_sector (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,          -- Nazwa sekcji (np. "To Do")
    order_position SMALLINT NOT NULL UNIQUE    -- Kolejność na tablicy
);

/* Rejestr zdarzeń systemowych */
CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES "user"(id),         -- Użytkownik wykonujący akcję
    action_type VARCHAR(50) NOT NULL,          -- Typ operacji (CREATE/UPDATE/DELETE)
    target_table VARCHAR(50) NOT NULL,         -- Tabela docelowa
    target_id INT NOT NULL,                    -- ID rekordu
    old_values JSONB,                          -- Stan przed zmianą
    new_values JSONB,                          -- Stan po zmianie
    executed_at TIMESTAMP DEFAULT NOW()        -- Czas wykonania
);

-------------------------
-- Indeksy --
-------------------------

-- Optymalizacja kluczy obcych
CREATE INDEX idx_user_team_id ON "user"(team_id);                                                   -- Szybkie wyszukiwanie członków zespołu
CREATE INDEX idx_task_milestone_id ON task(milestone_id);                                           -- Zadania wg kamieni
CREATE INDEX idx_subtask_task_id ON subtask(task_id);                                               -- Cząstki wg zadań
CREATE INDEX idx_milestone_project_id ON milestone(project_id);                                     -- Kamienie wg projektów
CREATE INDEX idx_milestone_team_id ON milestone(team_id);                                           -- Kamienie przypisane do zespołów

-- Filtrowanie aktywnych rekordów
CREATE INDEX idx_task_status ON task(status) WHERE status != 'Done';                                -- Lista niezakończonych zadań
CREATE INDEX idx_task_due_date ON task(due_date);                                                   -- Sortowanie po terminach
CREATE INDEX idx_subtask_due_date ON subtask(due_date);                                             -- Terminy cząstek
CREATE INDEX idx_notification_user_read ON notification(user_id, is_read) WHERE is_read = false;    -- Nieprzeczytane powiadomienia

-- Raportowanie i analiza
CREATE INDEX idx_project_progress ON project(progress);                                             -- Śledzenie postępu projektów
CREATE INDEX idx_audit_log_executed_at ON audit_log(executed_at);                                   -- Audyt czasowy
CREATE INDEX idx_comment_created_at ON comment(created_at);                                         -- Statystyki aktywności

-- Złożone optymalizacje
CREATE INDEX idx_task_team_composite ON task(milestone_id, priority) WHERE status = 'To Do';        -- Zadania do realizacji z priorytetem
CREATE INDEX idx_project_milestone_composite ON milestone(project_id, weight);                      -- Analiza rozkładu wag

---------------------------------
-- Funkcje i triggery --
---------------------------------

CREATE OR REPLACE FUNCTION update_progress_chain()
RETURNS TRIGGER AS $$
BEGIN
    -- Aktualizacja zadania na podstawie cząstek
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

    -- Aktualizacja kamienia na podstawie zadań
    WITH milestone_stats AS (
        SELECT
            SUM(weight * progress) / 100 AS done,
            SUM(weight) AS total
        FROM task
        WHERE milestone_id = (
            SELECT milestone_id 
            FROM task 
            WHERE id = COALESCE(NEW.task_id, OLD.task_id)
        )
    )
    UPDATE milestone
    SET progress = COALESCE((done * 100) / NULLIF(total, 0), 0)
    FROM milestone_stats
    WHERE id = (SELECT milestone_id FROM task WHERE id = COALESCE(NEW.task_id, OLD.task_id));

    -- Aktualizacja projektu na podstawie kamieni
    WITH project_stats AS (
        SELECT
            SUM(weight * progress) / 100 AS done,
            SUM(weight) AS total
        FROM milestone
        WHERE project_id = (
            SELECT project_id 
            FROM milestone 
            WHERE id = (
                SELECT milestone_id 
                FROM task 
                WHERE id = COALESCE(NEW.task_id, OLD.task_id)
            )
        )
    )
    UPDATE project
    SET progress = COALESCE((done * 100) / NULLIF(total, 0), 0)
    FROM project_stats;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

/* Walidacja struktury kamieni milowych */
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

/* Kontrola wag zadań w kamieniach */
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
        RAISE EXCEPTION 'Suma wag zadań (%s) przekracza wagę kamienia (%s)', tasks_sum, milestone_weight;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

/* Kontrola wag cząstek w zadaniach */
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
        RAISE EXCEPTION 'Suma wag cząstek (%s) przekracza wagę zadania (%s)', subtasks_sum, task_weight;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

/* Weryfikacja przed aktywacją projektu */
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

-- Śledzenie zmian w cząstkach
CREATE TRIGGER subtask_changes
AFTER INSERT OR UPDATE OF is_done, weight OR DELETE ON subtask
FOR EACH ROW EXECUTE FUNCTION update_progress_chain();

-- Automatyczna walidacja kamieni
CREATE CONSTRAINT TRIGGER check_milestone_weights
AFTER INSERT OR UPDATE ON milestone
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW EXECUTE FUNCTION validate_milestone_weights();

-- Kontrola spójności zadań
CREATE TRIGGER check_task_weights
AFTER INSERT OR UPDATE ON task
FOR EACH ROW EXECUTE FUNCTION validate_task_weights();

-- Kontrola spójności cząstek
CREATE TRIGGER check_subtask_weights
AFTER INSERT OR UPDATE ON subtask
FOR EACH ROW EXECUTE FUNCTION validate_subtask_weights();

-- Blokada aktywacji projektu
CREATE TRIGGER trg_project_activation
BEFORE UPDATE OF status ON project
FOR EACH ROW EXECUTE FUNCTION check_project_activation();
