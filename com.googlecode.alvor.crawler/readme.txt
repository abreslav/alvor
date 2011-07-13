== Overview ==

StringExpressionEvaluator "crawls" in AST-s and tries to return abstract string
for given string expression node. It is intraprocedural (doesn't cross method boundaries).

StringCollector collects incomplete information from StringExpressionEvaluator into
Cache until Cache contains complete information.

VariableTracker helps StringExpressionEvaluator to find out where a variable was
last assigned.

ConnectionTracker tries to assign a connection descriptor for each hotspot
(needed in project with multiple data sources).
 

== How string collection works ==

String collector's job is to update project's cache's "abstract_strings" table
according to requirements given in "patterns" table. 

String collection is designed to proceed incrementally (file-by-file), by 
collecting or updating pieces of information in the cache. Cache can contain incomplete
information about the project, but it also stores information about what still 
needs to be collected.

In simplest, string collector: 
	1) uses SearchEngine to find all occurrences of sql expressions,
	2) asks StringExpressionEvaluator to find abstract value of given expression node
	3) stores abstract string in the cache

Complications arise because of the need for incremental and interprocedural analysis.
StringExpressionEvaluator is designed to be intraprocedural, therefore when
part of abstract strings comes beyound method boundaries then it returns string
with a placeholder ("pattern reference") in that place. Incomplete string is stored
in the cache, but next phase of the string collection now must also resolve 
those "pattern references".  

"patterns" table describes what to search for in given project. It includes "primary patterns"
which describe which methods are considered hotspots. Primary patterns originate from
configuration (".alvor") and are loaded into cache during cache's initialization.

There are also "secondary patterns" which are introduced during the collection and which
describe the placeholders for interprocedural parts. batch_no in patterns table
describes the string collection phase when this pattern was introduced.

"files" table assigns numeric id-s to project's files and also keeps track on 
which patterns (both primary and secondary) this file has been searched for.
When a file is changed then it's batch_no is reset to 0, indicating that this file
must be checked again for all patterns.

At each phase, the string collector looks up files which are not checked
for newest patterns, and searches those new patterns in them and updates results
accordingly. It's safe to cancel this process and resume afterwards. One phase
of collection may introduce new secondary patterns, therefore this process is repeated
until there are no new patterns.

The description above used "hotspot patterns" as an example, but same process is used
with "function patterns" (corresponding to cases when part of string comes from a
method call) and "field patterns" (when part of the string comes from a field). 

