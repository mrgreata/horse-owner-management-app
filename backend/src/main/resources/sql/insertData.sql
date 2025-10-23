-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data


-- Remove seeded owners (keep user-entered)
DELETE FROM owner WHERE id < 0;

-- Seed owners with negative IDs (won't collide with user data)
INSERT INTO owner (id, first_name, last_name, email) VALUES
(-1, 'Ann',   'Smith',   'ann@example.com'),
(-2, 'Bob',   'Fox',     'bob@example.com'),
(-3, 'Cara',  'Miller',  'cara@example.com'),
(-4, 'David', 'Andrews', 'david@example.com'),
(-5, 'Sarah', 'Brown',   'sarah@example.com');


DELETE FROM horse where id < 0;

INSERT INTO horse (id, name, description, date_of_birth, sex)
VALUES (-1, 'Wendy', 'The famous one!', '2012-12-12', 'FEMALE');

-- Einige Elternbeziehungen (nur Beispiele; IDs an deine Seeds anpassen)
-- -1 Wendy (F) ist Mutter von -10; -7 Max (M) ist Vater von -10
INSERT INTO horse (id, name, description, date_of_birth, sex) VALUES
    (-7, 'Max',    'Stallion', '2010-04-10', 'MALE'),
    (-8, 'Bella',  'Mare',     '2011-06-20', 'FEMALE'),
    (-9, 'Rocky',  'Stallion', '2013-09-12', 'MALE'),
    (-10,'Candy',  'Foal',     '2016-05-03', 'FEMALE');

UPDATE horse SET mother_id = -1, father_id = -7 WHERE id = -10;  -- Wendy & Max → Candy
