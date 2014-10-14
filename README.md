Datacite Resolution Stats
=========================

Simple application that takes DOI resolution logs provided by CNRI, parses them into mongo documents, makes them available via a RESTful interface.  Also provides a nice, clean user interface enabling users to query and view the stats.

Developed with RESTlet, Guice, Contour.js and MongoDB.

Simple mvn package up the lot into a .war file and deploy in your favourite container.  Mongo can be configured via web.xml init-params.  See GuiceConfigModule.java for parameter names.  Mongo can be swapped out by implementing the LogLoader and LogQueryResolver interfaces and making them use your favourite repository.

Screenshot
==========
![Screenshot](datacite-screenshot.png)

API
===
The RESTful API enables daily, monthly and Top DOI queries.  Daily and monthly queries can be restricted to individual DOI prefixes and further broken down into per-DOI stats.  Results are returned in JSON (csv and XML to follow)

API Examples
--------
Return the top 100 DOIs of all time and their hit counts
`api/stats/hits?limit=100`

Return the top 1000 DOIs with the prefix 10.1234
`api/stats/hits/10.1234?limit=1000`

Return daily stats for all datacite
`api/stats/daily`

Return monthly stats for all datacite
`api/stats/monthly`

Return dailystats for 10.1234
`api/stats/daily/10.5061`

Return monthly stats for 10.1234 as a MAP
`api/stats/daily/10.5061?map`

Return monthly stats for 10.1234 broken down by doi
`api/stats/daily/10.5061?breakdown=true`

In addition, a map of DOI prefixes -> Datacentre names is provided at
`api/dois/prefixes`

Issues
======

Timezone problems
-----------------

Logs are provided in UTC and EST timezones.  Mongo stores them all as UTC.  The application aggregates them as UTC.  This would not normally be a problem.

The issue is that logs provided in "monthly" blocks equate to different stretches of time!  This means over/underlaps of data at the beginning and end of the first and most recent month.  There is little we can do about this when querying but does mean the last day of the most recent month is WRONG.  Answers on a postcard.  It also makes unit testing a pain.  If it was simply a different timezone per user we could work this out, but it's not.

Slow first request
------------------

The first request made to the server for daily or monthly overall stats is slow (roughly 30s, although it does complete).  This request is subsequently cached. This needs to be computed on log load or cached in the db so that it can be returned instantly.

TODO
----

* FIX TIMEZONE issues detailed above...
* Implement CSV & XML content types
* Implement a date slider to restrict view
* Provide an admin servlet that fires off bulk load jobs.  Currently run via junit script.
* Live fetch (and cache in the db) datacite customer prefixes & display names.  Currently parsed from a static JSON file.