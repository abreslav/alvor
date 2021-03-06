DROP TABLE patterns if exists;;;
DROP TABLE hotspots if exists;;;
DROP TABLE abstract_strings if exists;;;
DROP TABLE files if exists;;;

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
	4 - choice (can represent branches, hotspot options or function implementation options)
	5 - function pattern reference (children are arguments, int_value refers to function pattern)
	6 - hotspot pattern reference (corresponds to parameter in a pattern, no children, 
	    int_value refers to hotspot pattern.
	    If there were no overriding, then instead this one could use pattern-choice directly,
	    but a pattern can be "instantiated" by different overridden methods.
	    Basically there's n-to-n correspondance between pattern options and pattern instances,
	    thats why this indirection is required).
	7 - field pattern reference, int_value refers to field_pattern
	8 - repetition (child is repetition body) 
	9 - function parameter (used inside string function definitions, int_value is the number of parameter)
	10 - unsupported(str_value = message)
	*/
	kind TINYINT NOT NULL, 
	parent_id integer default null references abstract_strings(id) on delete cascade,
	item_index TINYINT default null, /* used in children of sequence, choice and application */
	int_value INTEGER DEFAULT NULL,
	str_value LONGVARCHAR DEFAULT NULL,
	str_value2 LONGVARCHAR DEFAULT NULL,
	file_id INTEGER default null REFERENCES files(id) ON DELETE CASCADE,
	start INTEGER default null,
	length INTEGER default null
);;;

CREATE TABLE patterns
(
	/* id always references a choice */
	id INTEGER PRIMARY KEY references abstract_strings(id) on delete cascade,  
	kind tinyint not null,
	class_name varchar(300) not null,
	method_name varchar(300) not null,
	argument_types varchar(300) not null,
	argument_index tinyint not null,
	pattern_role tinyint not null, /* 1 = primary, 2 = secondary */
	batch_no integer not null      /* shows when this pattern was published */
);;;



create table hotspots
(
	string_id integer primary key references abstract_strings(id) on delete cascade,
	file_id integer not null references files(id) on delete cascade,
	start integer not null,
	length integer not null,
	conn_descriptor varchar(300) default null,
	checked boolean default false not null
);;;