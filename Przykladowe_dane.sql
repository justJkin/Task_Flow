-------------------------
-- Dane testowe --
-------------------------

-- Tabele bazowe --
INSERT INTO team (name) VALUES 
('Development'),
('Marketing'),
('DevOps');

-- Użytkownicy z podziałem na zespoły + admin --
INSERT INTO "user" (first_name, last_name, email, password_hash, role, team_id) VALUES
-- Admin systemowy (bez zespołu)
('Admin', 'System', 'admin@example.com', 'admin_hash', 'admin', NULL),

-- Zespół Development (1 menager + 3 userów)
('Jan', 'Kowalski', 'jan.dev@example.com', 'hash1', 'manager', 1),
('Anna', 'Nowak', 'anna.dev@example.com', 'hash2', 'user', 1),
('Piotr', 'Wiśniewski', 'piotr.dev@example.com', 'hash3', 'user', 1),
('Maria', 'Dąbrowska', 'maria.dev@example.com', 'hash4', 'user', 1),

-- Zespół Marketing (1 menager + 3 userów)
('Katarzyna', 'Wójcik', 'kasia.mark@example.com', 'hash5', 'manager', 2),
('Michał', 'Lewandowski', 'michal.mark@example.com', 'hash6', 'user', 2),
('Alicja', 'Kamińska', 'alicja.mark@example.com', 'hash7', 'user', 2),
('Tomasz', 'Zieliński', 'tomek.mark@example.com', 'hash8', 'user', 2),

-- Zespół DevOps (1 menager + 3 userów)
('Robert', 'Szymański', 'robert.ops@example.com', 'hash9', 'manager', 3),
('Magdalena', 'Woźniak', 'magda.ops@example.com', 'hash10', 'user', 3),
('Grzegorz', 'Kozłowski', 'grzegorz.ops@example.com', 'hash11', 'user', 3),
('Monika', 'Jankowska', 'monika.ops@example.com', 'hash12', 'user', 3);

-- Projekty z pełną hierarchią --
INSERT INTO project (name, description, status, start_date, end_date, admin_id) VALUES
-- Aktywne (po 30.03.2025)
('Platforma E-commerce', 'System sklepu online', 'active', '2024-01-01', '2025-04-15', 1),
('Aplikacja Mobilna', 'Aplikacja dla klientów', 'active', '2024-02-01', '2025-05-30', 1),
('Analiza Big Data', 'System analityczny', 'active', '2024-03-01', '2025-06-15', 1),

-- Archiwalne (przed 30.03.2025)
('Stara Wersja API', 'Poprzednia wersja systemu', 'archived', '2023-01-01', '2024-02-28', 1),
('Kampania 2023', 'Promocja świąteczna', 'archived', '2023-11-01', '2024-01-15', 1);

-- Kamienie milowe (3-4 per projekt) --
INSERT INTO milestone (project_id, name, weight, team_id) VALUES
-- Projekt 1: E-commerce (suma 100)
(1, 'Backend', 40, 1),
(1, 'Frontend', 35, 1),
(1, 'Testy', 25, 3),

-- Projekt 2: Aplikacja Mobilna (suma 100)
(2, 'UI/UX', 30, 2),
(2, 'Integracje', 40, 3),
(2, 'Wydajność', 30, 3),

-- Projekt 3: Big Data (suma 100)
(3, 'Zbieranie Danych', 50, 3),
(3, 'Przetwarzanie', 30, 3),
(3, 'Wizualizacja', 20, 2);

-- Zadania (3-4 per kamień) --
INSERT INTO task (milestone_id, name, status, priority, weight, due_date) VALUES
-- Backend E-commerce (40)
(1, 'API Produktów', 'In Progress', 1, 15, '2024-03-01'),
(1, 'Płatności Online', 'To Do', 2, 20, '2024-04-15'),
(1, 'Autoryzacja', 'Done', 3, 5, '2024-02-20'),

-- Frontend E-commerce (35)
(2, 'Katalog Produktów', 'Done', 1, 20, '2024-03-10'),
(2, 'Koszyk Zakupowy', 'In Progress', 2, 15, '2024-04-01');

-- Cząstki (3 per zadanie) --
INSERT INTO subtask (task_id, name, weight, is_done) VALUES
-- API Produktów (15)
(1, 'Endpoint GET /products', 5, true),
(1, 'Endpoint POST /products', 5, true),
(1, 'Walidacja danych', 5, false),

-- Płatności Online (20)
(2, 'Integracja z PayPal', 10, false),
(2, 'Integracja z Przelewy24', 10, false),

-- Katalog Produktów (20)
(4, 'Grid produktów', 10, true),
(4, 'Filtry wyszukiwania', 5, true),
(4, 'Sortowanie', 5, false);

-- Przypisania zadań --
INSERT INTO task_assignment (user_id, task_id) VALUES
(2, 1),   -- Developer
(3, 2),   -- Developer
(6, 4),   -- Marketer
(10, 5);  -- DevOps

-- Przykładowe komentarze i powiadomienia --
INSERT INTO comment (task_id, user_id, content) VALUES
(1, 2, 'Potrzebne dodatkowe parametry w API'),
(4, 6, 'Klient zaakceptował projekt gridu');-

INSERT INTO notification (user_id, task_id, type, content) VALUES
(2, 1, 'reminder', 'Deadline API za 3 dni'),
(6, 4, 'update', 'Nowe wymagania UX');
