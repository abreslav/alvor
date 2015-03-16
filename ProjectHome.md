# Alvor #
Alvor is an Eclipse plug-in, which statically validates SQL sentences embedded in Java code. It can be used either as one-shot full-program analyzer or as incremental as-you-type error guard. SQL strings found in the code can be checked against built-in SQL grammar or against actual test database.

NB! Alvor is currently in BETA quality!

## Installation ##
Use following Eclipse update-site:
http://updates.alvor.googlecode.com/hg/
NB! you may need to uncheck "Group items by category".

Alvor works with Eclipse 3.5 - 3.7 (Possibly also with 3.8/4.2)

## Screenshot ##
Here Alvor has caught a name misspelling and a missing space (between "persons" and "where"):
| ![http://wiki.alvor.googlecode.com/hg/pics/screen1.png](http://wiki.alvor.googlecode.com/hg/pics/screen1.png) |
|:--------------------------------------------------------------------------------------------------------------|

## Basic usage ##
Right-click your Java project and select "Alvor SQL checker" -> "Perform manual SQL
check". Found problems appear in "Problems" view. (Unsupported cases are visible in "Markers" view).

## Automatic, incremental checking ##
If you want automatic checking after each save, then:
  * right-click your Java project
  * select "Properties"
  * open "Alvor SQL checker" property page
  * check "Plug Alvor to project's build process"

NB! after changing configuration, Alvor will first perform a full build, and then updates checking results incrementally each time a Java file is saved.

## Configuration ##
In "Alvor SQL checker" property page you can also configure:
  * how to find SQL strings from the code
  * database to use for validating the strings
For more information see [Configuration](Configuration.md)

## More info ##
  * ["An Interactive Tool for Analyzing Embedded SQL Queries", APLAS 2010](http://www.springerlink.com/content/cx22j11001706143/), gives overview of the approach used.
  * See [here](Design.md) if you want to dig into code or just learn more how Alvor works.
  * If you have problems running Alvor or you want to contribute, then write to [aivar.annamaa@ut.ee](mailto:aivar.annamaa@ut.ee)

## Credits ##
Until 2011 Alvor was developed in [STACC](http://www.stacc.ee/) (Estonian Software Technology and Applications Competence Centre) with support from [Nortal](http://www.nortal.com).
