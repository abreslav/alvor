== Overview ==

Cache enables incremental analysis by allowing piecemeal collection 
and updating of abstract strings.
 
Alvor creates one H2 database per Java project to store that project's 
abstract strings and related stuff (hotspot patterns, file identifiers).
Database structure is defined in db/cache_setup.sql

At runtime, database files are stored in 
".metadata/.plugins/com.googlecode.alvor.cache" folder under workspace.
Database is automatically created if it doesn't exist.

