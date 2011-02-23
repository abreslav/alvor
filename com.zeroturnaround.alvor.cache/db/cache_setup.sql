DROP TABLE project_patterns if exists;;;
DROP TABLE patterns if exists;;;
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
	1 - constant(str_value = value, str_value2 = escaped value)
	2 - char set(str_value = contents)
	3 - sequence
	4 - choice (can be hotspot pattern or function)
	5 - application (child is argument, int_value is function_id) 
	6 - repetition (child is repetition body) 
	7 - unsupported(str_value = message)
	8 - parameter (used inside functions)
	*/
	kind TINYINT NOT NULL, 
	parent_id integer default null references abstract_strings(id) on delete cascade,
	item_index TINYINT default null, /* used in children of sequence (or choice?) */
	int_value INTEGER DEFAULT NULL,
	str_value LONGVARCHAR DEFAULT NULL,
	str_value2 LONGVARCHAR DEFAULT NULL,
	file_id INTEGER default null REFERENCES files(id) ON DELETE CASCADE,
	start INTEGER default null,
	length INTEGER default null
);;;

CREATE TABLE patterns
(
	id INTEGER PRIMARY KEY references abstract_strings(id) on delete cascade,  
	kind tinyint not null,
	class_name varchar(300) not null,
	method_name varchar(300) not null,
	argument_index tinyint not null
);;;



CREATE TABLE project_patterns
(
	project_name varchar(100) not null,
	pattern_id INTEGER not null REFERENCES patterns(id) ON DELETE CASCADE,
	batch_no integer not null,
	source_id integer references files(id) on delete cascade,
	primary key (project_name, pattern_id)
);;;


