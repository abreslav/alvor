DROP TRIGGER DeleteCollectionContentsRow;;;
DROP TRIGGER DeleteCollectionContentsStmt;;;
--DROP TABLE Log;; ;
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

CREATE MEMORY TABLE -- temp table for communicating info out from trigger DeleteCollectionRow
	AbstractStringsToDelete 
(
	id INTEGER
);;;

/*
CREATE TABLE Log (
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	message LONGVARCHAR
); ;;
*/


CREATE TABLE
	Files
( 
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	
	name VARCHAR(4096) UNIQUE
);;;

CREATE TABLE
	SourceRanges -- encodes positions
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

CREATE TABLE
	CharacterSets
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	
	data VARCHAR(10) UNIQUE NOT NULL
);;;

CREATE TABLE
	Unsupported -- contains messages for unsupported hotspots
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
		2 - sequence(a is null, id references CollectionContents(collection)),
		3 - choice(a is null, id references CollectionContents(collection)),
		4 - repetition(a references AbstractStrings(id)) 
		5 - sameAs(a references AbstractStrings(id))
			  used for example for a variable when a repesents earlier assignment expression  
		6 - unsupported(a references Unsupported(id))
		7 - parameter(a = ordinal number of parameter)
	*/
	type TINYINT NOT NULL, 
	a INTEGER DEFAULT NULL,
	sourceRange INTEGER REFERENCES SourceRanges(id) ON DELETE CASCADE NOT NULL -- position
);;;


CREATE TABLE 
	CollectionContents
(
	collection INTEGER REFERENCES AbstractStrings(id) ON DELETE CASCADE,
	item INTEGER REFERENCES AbstractStrings(id) ON DELETE CASCADE,
	index INTEGER,
	
	UNIQUE(collection, index)
);;;

CREATE INDEX CollectionContentsIndex ON CollectionContents(index);;;

CREATE TRIGGER DeleteCollectionContentsRow AFTER DELETE
	ON CollectionContents
	REFERENCING OLD ROW AS d
	FOR EACH ROW
begin atomic
	-- if one piece of a collection is invalidated then the whole collection should be invalidated
	-- (but the abstract strings behind other collection items remain)

	-- following statement just doesn't work for some reason
	-- DELETE FROM AbstractStrings where id = deleted.collection;
	
	-- Workaround: keep log of deleted stuff and delete it in DeleteCollectionContentsStmt
	insert into AbstractStringsToDelete (id) values (d.collection);
end;;;

CREATE TRIGGER DeleteCollectionContentsStmt AFTER DELETE
	ON CollectionContents
	FOR EACH Statement
begin atomic
	-- Clean up using the log left by DeleteCollectionContentsRow
	DELETE FROM AbstractStrings WHERE (id IN (SELECT id FROM AbstractStringsToDelete)); 
	DELETE FROM AbstractStringsToDelete;
end;;;

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
);;;