What can happen when file is changed:
 	- easy (possibly intraprocedural) stuff -- can be resolved by searching and evaluating hotspots in this file 
    	- hotspots may appear or disappear
     
	- interprocedural stuff
		- new call-sites appear to some method in another (unchanged) file containing hotspot ...
		- ... or such call-sites can disappear
		- a method body can be altered so that this method becomes a new hotspot
			- callers of this method will not be aware of this, because interface is not changed
			- need to search *all files* for calls to this new hotspot
		- a method implementation called from other files can appear/disappear/change:
			- if it disappears then code breaks and we can expect user to modify other files soon,
           or if it's an overridden method (one of many implementations) then other implementations
           (possibly in superclass) remain and we can just remove this option
         - if new implementation appears then its string should be added 
         - it method body changes then only method string in cache should be updated (or invalidated)  


How to react to file change:
 - track down and remember all hotspots which were depending on this file (but don't invalidate them)
   Use cache database for this (assume cache is complete representation of all abstract strings,
   if cache is disabled, then it's whole another story anyway) 
 - delete items rooted in this file (markers, cached AST and cached )
 	(parts of abstract-strings positioned in other files can stay in cache)
     - call-site arguments corresponding to options of AS-s in other files can be removed ???
       
 - search and evaluate hotspots in this file
 - repair cache representation of abstract strings rooted in other files which were/are depending on this file:
     - add options corresponding to call-sites in this file 


Another take:
    - a file can provide callsite options and method implementation options
    - when file is changed then re-search for calls to "hotspot wrappers" (methods that take
      a string argument that they use as part of hotspot (either directly or not))
    - also search for possible method implementations of "string providers"
      (methods used for constructing some hotspot value)
     
    












After a file is changed:
- remove from cache hotspots of this file and respective abstract strings
  (components of as-s with positions in other files can stay)

- re-search hotspots and recollect abstract strings (possibly reusing the components in other files)

- HARD PART: recompute parts of abstract strings required by other files
  (ie. abstract method definitions and callsite argument expressions)
  for this, cache should have some higher level info besides positions (names)
  
   
TODO: instead invalidating files, invalidate source-ranges 