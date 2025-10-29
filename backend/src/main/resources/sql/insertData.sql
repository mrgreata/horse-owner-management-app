-- FK-Prüfungen kurz aus, negative Testdaten bereinigen
SET REFERENTIAL_INTEGRITY FALSE;
DELETE FROM horse WHERE id < 0;
DELETE FROM owner WHERE id < 0;
SET REFERENTIAL_INTEGRITY TRUE;

-- ==== Owners (negative IDs kollidieren nicht mit User-Daten)
INSERT INTO owner (id, first_name, last_name, email) VALUES
                                                         (-1, 'Ann',   'Smith',   'ann@example.com'),
                                                         (-2, 'Bob',   'Fox',     'bob@example.com'),
                                                         (-3, 'Cara',  'Miller',  'cara@example.com'),
                                                         (-4, 'David', 'Andrews', 'david@example.com'),
                                                         (-5, 'Sarah', 'Brown',   'sarah@example.com'),
                                                         (-6, 'Lara', 'Croft', 'lara@adventure.tld'),
                                                         (-7, 'John', 'Doe', 'john@doe.tld'),
                                                         (-8, 'Jane', 'Doe', 'jane@doe.tld'),
                                                         (-9, 'Nathan', 'Drake', 'nathan@explorer.tld'),
                                                         (-10,'Mira', 'Blue', 'mira@blue.tld');

INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id) VALUES
                        (-1,  'Wendy',  'The famous one!', '2012-12-12', 'FEMALE', NULL),
                        (-7,  'Max',    'Stallion',        '2010-04-10', 'MALE',   NULL),
                        (-8,  'Bella',  'Mare',            '2011-06-20', 'FEMALE', NULL),
                        (-9,  'Rocky',  'Stallion',        '2013-09-12', 'MALE',   NULL),
                        (-10, 'Candy',  'Foal',            '2016-05-03', 'FEMALE', NULL),
                        (-11, 'Spirit', 'Wild horse',      '2015-05-05', 'MALE',   -1),
                        (-12, 'Blaze',  'Speedster',       '2016-06-10', 'MALE',   -2),
                        (-13, 'Daisy',  'Gentle mare',     '2018-04-04', 'FEMALE', -3),
                        (-14, 'Storm',  'Powerful',        '2014-03-03', 'MALE',   -4),
                        (-15, 'Luna',   'Graceful mare',   '2017-07-07', 'FEMALE', -5);

-- Eltern-Beziehung NACH dem Insert setzen
UPDATE horse SET mother_id = -1, father_id = -7 WHERE id = -10;
