DELETE FROM AbstractStrings;
DELETE FROM SourceRanges;
DELETE FROM Files;
INSERT INTO Files(name) VALUES
	('a'),
	('b'),
	('c')
;
INSERT INTO SourceRanges(file, start, length) VALUES
	((SELECT id FROM Files WHERE name = 'a'), 10, 20);
INSERT INTO SourceRanges(file, start, length) VALUES
	((SELECT id FROM Files WHERE name = 'b'), 110, 20);
INSERT INTO SourceRanges(file, start, length) VALUES
	((SELECT id FROM Files WHERE name = 'b'), 210, 20);
INSERT INTO SourceRanges(file, start, length) VALUES
	((SELECT id FROM Files WHERE name = 'c'), 310, 20);
INSERT INTO SourceRanges(file, start, length) VALUES
	((SELECT id FROM Files WHERE name = 'b'), 410, 20)
;
INSERT INTO AbstractStrings(type, a, b, sourceRange) VALUES
	(1, 0, 1, (SELECT SOurceRanges.id FROM SOurceRanges LEFT JOIN Files ON SourceRanges.file = Files.id WHERE name = 'a'));
INSERT INTO AbstractStrings(type, a, b, sourceRange) VALUES
	(2, 2, 1, (SELECT SOurceRanges.id FROM SOurceRanges LEFT JOIN Files ON SourceRanges.file = Files.id WHERE name = 'b' AND start = 410));
SELECT * FROM SOURCERanges;
SELECT * FROM SourceRanges LEFT JOIN FIles ON (file = Files.id);
SELECT * FROM Files;
SELECT type, a, b, name, start, length FROM AbstractSTrings 
	LEFT JOIN SourceRanges ON AbstractStrings.sourceRange = SourceRanges.id
	LEFT JOIN Files ON SourceRanges.file = Files.id
	WHERE name IS NOT NULL
;
SELECT type, a, b FROM AbstractStrings WHERE sourceRange = 
	(SELECT id FROM SourceRanges WHERE 
		(file = (SELECT id FROM Files WHERE name = 'b')) 
		AND (start = 410) AND (length = 20))
