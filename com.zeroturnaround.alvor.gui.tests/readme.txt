How can user interact with Alvor and how should Alvor respond?
--------------------------------------------------------------

Check SQL command:
State:
	- With or without builder installed
	- with or without existing results from previous analysis
	- Project is successfully compiled (no Java error markers)
	
  * After using "Check SQL" command, Alvor updates SQL markers for current project 
    so that there are:
	a) info markers for all hotspot expressions
	b) background markers for all expressions (or only literals?) which contribute to a 
	   hotspot expression
	c) error markers (with error message and counterexample) for hotspots which contain invalid SQL
	d) (optionally) error markers on offending parts of SQL string literals
	e) warning markers on hotspots where Alvor failed to construct set of possible SQL strings
	 
	- Create initial reference list of found errors / warnings for each project
	- When there is mismatch, then either the program or reference list should be fixed


If checking ends without Alvor error / warning markers, it means that there are no 
erroneous calls
in this project leading to base set of hotspots found in this project. 


TODO: how to find "current" project


Checking part of the project
----------------------------




Incremental stuff (ie. AlvorBuilder is enabled for current project): 
-------------------------------------------------------------------	
"Clean" command should remove all Alvor markers (and clean its caches)

After "Build" command (or after each save, if auto-build is enabled), Alvor should update
its markers according to new state of the project.

In particular, if correct markers are computed for the project, then after a dummy change to a file 
(eg. adding a space to the end), the resulting set of markers should be equivalent to previous
set of markers.

TODO test the effect of refactoring the target project

Edge cases
----------
If project has Java errors, then Alvor builder leaves its markers as they were and returns
without checking anything. If   