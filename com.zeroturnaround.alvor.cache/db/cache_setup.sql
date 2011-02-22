DROP TABLE project_patterns if exists;;;
DROP TABLE patterns if exists;;;
DROP TABLE collections if exists;;;
DROP TABLE abstract_strings if exists;;;
DROP TABLE files if exists;;;

SET FILES LOG FALSE;;;

SET DATABASE DEFAULT TABLE TYPE CACHED;;;

CREATE TABLE files
( 
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	name VARCHAR(300) not null UNIQUE,
	batch_no integer not null
);;;

CREATE TABLE abstract_strings
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	/* Kind:
		0 - constant(str_value = value, str_value2 = escaped value),
		1 - char set(str_value = contents),
		2 - sequence(id references CollectionContents(collection)),
		3 - choice(id references CollectionContents(collection)),
		4 - repetition(int_value references AbstractStrings(id)) 
		6 - unsupported(str_value = message)
		7 - parameter(int_value = ordinal number of parameter)
	*/
	kind TINYINT NOT NULL, 
	int_value INTEGER DEFAULT NULL,
	str_value LONGVARCHAR DEFAULT NULL,
	str_value2 LONGVARCHAR DEFAULT NULL,
	file_id INTEGER not null REFERENCES Files(id) ON DELETE CASCADE,
	start INTEGER not null,
	length INTEGER not null
);;;

CREATE TABLE collection_contents
(
	collection_id INTEGER not null REFERENCES abstract_strings(id) ON DELETE CASCADE,
	item_index INTEGER not null,
	item_id INTEGER not null REFERENCES abstract_strings(id) ON DELETE CASCADE,
	
	primary key (collection_id, item_index)
);;;

CREATE TABLE patterns
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	class_name varchar(300) not null,
	method_name varchar(300) not null,
	argument_index tinyint not null,
	kind tinyint not null
);;;



CREATE TABLE project_patterns
(
	project_name varchar(100) not null,
	pattern_id INTEGER not null REFERENCES patterns(id) ON DELETE CASCADE,
	batch_no integer not null,
	source_id integer references files(id) on delete cascade,
	primary key (project_name, pattern_id)
);;;


