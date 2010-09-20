* "site.xml" defines whole update-site -- this is the only file required for building the site

* before updating the site, delete all files in this folder except "site.xml" and this readme.txt
	- seems that otherwise Eclipse keeps old version of feature*.jar despite of rebuilding
	
* for building new version of site, open "site.xml" editor and press "Synchronize" and "Build all"
	- not sure if "Synchronize" is required, but it can't do harm 
	- each build creates plugin jar-s with new filenames (when plug-ins are changed?), but
  	  new feature.jar references only newest versions. This is the other reason for deleting
  	  old stuff before building.

* for deploying update-site, transfer all files from this folder (except this readme.txt)
  to web. You can delete old stuff from web, to keep things cleaner. 

* Steps are probably a bit different if you want to keep different versions of the plug-ins 
  available 

-------

* Keep in mind that Eclipse can cache information about update-sites and available versions. 
  When in trouble, restarting Eclipse could help.

* Seems that "Check for updates" detects updated update-site only when feature version is
  increased (but then you also need to replace the feature in update-site project)  