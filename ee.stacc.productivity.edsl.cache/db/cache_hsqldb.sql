DROP TRIGGER RememberDeletedStrings;;;
--DROP INDEX CollectionContentsIndex;; ;
--DROP TRIGGER logdelconst;; ;
--DROP TRIGGER DeleteSameAndRepetitionSta;; ;
--DROP TRIGGER DeleteSameAndRepetition;; ;
DROP TRIGGER DeleteCollectionRow;;;
--DROP TRIGGER DeleteCollectionSta;; ;
DROP TABLE AbstractStringsToDelete;;;
--DROP TABLE Log;; ;
DROP TABLE DeletedAbstractStrings;;;
DROP TABLE MethodImplementations;;;
DROP TABLE MethodImplementationScope;;;
DROP TABLE Signatures;;;
DROP TABLE Unsupported;;;
DROP TABLE Hotspots;;;
DROP TABLE MethodUsageScope;;;
DROP TABLE Methods;;;
DROP TABLE CollectionContents;;;
DROP TABLE AbstractStrings;;;
DROP TABLE CharacterSets;;;
DROP TABLE StringConstants;;;
DROP TABLE SourceRanges;;;
DROP TABLE Files;;;

SET FILES LOG FALSE;;;

SET DATABASE DEFAULT TABLE TYPE CACHED;;;

/*
CREATE TABLE Log (
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	message LONGVARCHAR
)*/

CREATE MEMORY TABLE 
	AbstractStringsToDelete 
(
	id INTEGER
);;;

CREATE TABLE
	Files
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	
	name VARCHAR(4096) UNIQUE
);;;

CREATE TABLE
	SourceRanges
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	
	file INTEGER REFERENCES Files(id) ON DELETE CASCADE,
	start INTEGER,
	length INTEGER,
	
	UNIQUE(file, start, length)
);;;

CREATE TABLE
	StringConstants
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	
	literalValue LONGVARCHAR,
	escapedValue LONGVARCHAR UNIQUE
);;;

/*create trigger logdelconst after delete on StringConstants
	referencing old row as d
	for each row
begin atomic
	insert into log(message) values ('delconst ' + d.id + ' ' + d.literalValue);
end*/

CREATE TABLE
	CharacterSets
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	
	data VARCHAR(10) UNIQUE NOT NULL
);;;

CREATE TABLE
	Unsupported
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

	message LONGVARCHAR
);;;

CREATE TABLE
	AbstractStrings
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	
	/*
		0 - constant(a),
		1 - char set(a),
		2 - sequence(contents in CollectionContents),
		3 - choice(contents in CollectionContents),
		4 - repetition(a) 
		5 - sameAs(a)
		6 - unsupported (a)
	*/
	type TINYINT NOT NULL, 
	a INTEGER DEFAULT NULL,
	b INTEGER DEFAULT NULL,
	sourceRange INTEGER REFERENCES SourceRanges(id) ON DELETE CASCADE UNIQUE NOT NULL
);;;

CREATE MEMORY TABLE
	DeletedAbstractStrings
(
	id INTEGER UNIQUE,
	type TINYINT,
	a INTEGER DEFAULT NULL
);;;

CREATE TRIGGER RememberDeletedStrings AFTER DELETE
	ON AbstractStrings
	REFERENCING OLD ROW AS deleted
	FOR EACH ROW
	INSERT INTO DeletedAbstractStrings(id, type, a) VALUES (deleted.id, deleted.type, deleted.a);
;;;

/*CREATE TRIGGER DeleteSameAndRepetition AFTER DELETE
	ON AbstractStrings
	REFERENCING OLD ROW AS deleted
	FOR EACH ROW
BEGIN ATOMIC
	--INSERT INTO Log(message) VALUES ('del ' + deleted.id);
	INSERT INTO AbstractStringsToDelete VALUES(SELECT id FROM AbstractStrings WHERE (type IN (4, 5)) AND (a = deleted.id));
	DELETE FROM StringConstants WHERE id IN (
		SELECT id FROM StringConstants 
			LEFT JOIN (SELECT a FROM AbstractStrings WHERE type = 0) 
				ON (a = StringConstants.id)
		WHERE a IS NULL
	);
	DELETE FROM CharacterSets WHERE id IN (
		SELECT id FROM CharacterSets 
			LEFT JOIN (SELECT a FROM AbstractStrings WHERE type = 1) 
				ON (a = CharacterSets.id)
		WHERE a IS NULL
	);
	DELETE FROM Unsupported WHERE id IN (
		SELECT id FROM Unsupported 
			LEFT JOIN (SELECT a FROM AbstractStrings WHERE type = 6) 
				ON (a = Unsupported.id)
		WHERE a IS NULL
	);
END*/

/*CREATE TRIGGER DeleteSameAndRepetitionSta AFTER DELETE
	ON AbstractStrings
	FOR EACH STATEMENT
BEGIN ATOMIC
	DELETE FROM AbstractStrings WHERE id IN (SELECT id FROM AbstractStringsToDelete);
	DELETE FROM AbstractStringsToDelete;
END*/

CREATE TABLE 
	CollectionContents
(
	collection INTEGER REFERENCES AbstractStrings(id) ON DELETE CASCADE,
	item INTEGER REFERENCES AbstractStrings(id) ON DELETE CASCADE,
	index INTEGER,
	
	UNIQUE(collection, index)
);;;

CREATE INDEX CollectionContentsIndex ON CollectionContents(index);;;

CREATE TRIGGER DeleteCollectionRow AFTER DELETE
	ON CollectionContents
	REFERENCING OLD ROW AS deleted
	FOR EACH ROW
BEGIN ATOMIC
	-- This trigger is meant to be run when an item of a collection is removed from the cache
	--INSERT INTO Log(message) VALUES ('delcol ' + deleted.item + ' ' + deleted.collection);
	INSERT INTO AbstractStringsToDelete values (deleted.collection);
END;;;

/*CREATE TRIGGER DeleteCollectionSta AFTER DELETE
	ON CollectionContents
	FOR EACH STATEMENT
BEGIN ATOMIC
	DELETE FROM AbstractStrings WHERE id IN (SELECT id FROM AbstractStringsToDelete);
	DELETE FROM AbstractStringsToDelete;
END*/

CREATE TABLE
	Methods
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	
	class VARCHAR(128),
	name VARCHAR(128),
	
	UNIQUE (class, name)
);;;

CREATE TABLE
	MethodUsageScope
(
	method INTEGER REFERENCES Methods(id) ON DELETE CASCADE,
	file INTEGER REFERENCES Files(id) ON DELETE CASCADE,
	
	UNIQUE (method, file)
);;;

CREATE TABLE
	Hotspots
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

	method INTEGER REFERENCES Methods(id) ON DELETE CASCADE,
	argumentIndex INTEGER,
	sourceRange INTEGER REFERENCES SourceRanges(id) ON DELETE CASCADE UNIQUE
);;;

CREATE TABLE
	Signatures
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	
	signature LONGVARCHAR
);;;

CREATE TABLE
	MethodImplementations
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

	signature INTEGER REFERENCES Signatures(id) ON DELETE CASCADE,
	abstractString INTEGER REFERENCES AbstractStrings(id) ON DELETE CASCADE,		
	sourceRange INTEGER REFERENCES SourceRanges(id) ON DELETE CASCADE UNIQUE
);;;

CREATE TABLE
	MethodImplementationScope
(
	signature INTEGER REFERENCES Signatures(id) ON DELETE CASCADE,
	file INTEGER REFERENCES Files(id) ON DELETE CASCADE,
	
	UNIQUE (signature, file)
)

/*

select * from log



select * from abstractstrings left join sourceranges on (sourcerange = sourceranges.id) left join files on (file = files.id)

select * from collectioncontents

select * from stringconstants

delete from files where id = 0

delete from abstractstrings where id = 1

select * from log

select * from stringconstants
*/;;;