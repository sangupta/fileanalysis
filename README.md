FileAnalysis
============

**SQLize files to analyze dumps and logs.**

`FileAnalysis` is a simple tool that helps push data from various file formats
such as `CSV`, `tab-delimited`, `Apache HTTP Logs`, `Log4j`, `Logback` and others
into an embedded disk persisted `H2 database`. Once the data is in, you can run
proper SQL queries around the data and analyze them in a variety of ways.

Features
--------

* Run SQL queries against structured data
* Supports pagination during queries
* Interactive tool
* Plugin mechanism to add more file formats
* Automatic detection of column data-types using heuristic
* Column names are picked up using the header row

Usage
-----

To build take a checkout of the code, and build using:

```
$ mvn clean package
```

Run the tool via:

```
$ java -jar target/fileanalysis.jar
```

The workflow to run some queries against the `demo/real-estate.csv` file are as under:

```
$ java -jar target/fileanalysis.jar

Enter file path to be analyzed: demo/real-estate.csv
Checking parameters...
Reading configuration...
Creating required tables...
Populating data...
Loaded data in 142 millis.
```

Once file laod is complete, you will receive a prompt for `fa-query $` where you can fire some queries.
Following are some samples of the same.

The database TABLE created from the file is `DATA`.

```
fa-query $ select * from data;
|       LINENUM        |               STREET                |      CITY       |  ZIP  | STATE | BEDS | BATHS | SQFT |     TYPE     |          SALE_DATE           | PRICE  | LATITUDE  |  LONGITUDE  |
+----------------------+-------------------------------------+-----------------+-------+----+---+---+------+--------------+------------------------------+--------+-----------+-------------+
|                  1.0 | 3526 HIGH ST                        | SACRAMENTO      | 95838.0 | CA | 2.0 | 1.0 | 836.0 | Residential  | Wed May 21 00:00:00 EDT 2008 | 59222.0 | 38.631913 | -121.434879 |
|                  2.0 | 51 OMAHA CT                         | SACRAMENTO      | 95823.0 | CA | 3.0 | 1.0 | 1167.0 | Residential  | Wed May 21 00:00:00 EDT 2008 | 68212.0 | 38.478902 | -121.431028 |
|                  3.0 | 2796 BRANCH ST                      | SACRAMENTO      | 95815.0 | CA | 2.0 | 1.0 | 796.0 | Residential  | Wed May 21 00:00:00 EDT 2008 | 68880.0 | 38.618305 | -121.443839 |
...

Type "it" for more: no

Total number of records found: 60
``` 

By default 20 rows of data are shown. To view more and run the cursor forward, type `it` on the prompt.


```
fa-query $ select distinct(city) from data;
|      CITY       |
+-----------------+
| FORESTHILL      |
| GALT            |
| WEST SACRAMENTO |
| RANCHO CORDOVA  |
| ELVERTA         |
| GARDEN VALLEY   |
| NORTH HIGHLANDS |
| ROCKLIN         |
| GOLD RIVER      |
| CITRUS HEIGHTS  |
| CAMERON PARK    |
| EL DORADO HILLS |
| FOLSOM          |
| MEADOW VISTA    |
| MATHER          |
| FAIR OAKS       |
| WILTON          |
| DIAMOND SPRINGS |
| GRANITE BAY     |
| RANCHO MURIETA  |
Type "it" for more: it
| WALNUT GROVE    |
| ROSEVILLE       |
| AUBURN          |
| RIO LINDA       |
| PLACERVILLE     |
| COOL            |
| LOOMIS          |
| SHINGLE SPRINGS |
| SACRAMENTO      |
| PENRYN          |
| GREENWOOD       |
| ORANGEVALE      |
| CARMICHAEL      |
| ANTELOPE        |
| ELK GROVE       |
| POLLOCK PINES   |
| EL DORADO       |
| LINCOLN         |
| SLOUGHHOUSE     |

Total number of records found: 39
```

Inspiration
-----------

The idea is inspired from the fact that I need to analyze a lot of log files
and dumps and decipher information from them. Tools such as `Excel`, `splunk` etc 
have never helped me do things faster, and thus, I always ended up writing code
to do my tasks.

I then saw a small demo video of the `textql` tool at https://github.com/dinedal/textqlb
The concept of the tool was fantastic, push data into an in-memory SQL store and then
run a query against the data.

I improved upon the idea to first persist the data on disk as well, so that multiple
queries could be run. Ended up adding stuff to nicely display the data for `SELECT` queries
as well and made sure that when the result set had hundreds of rows, we paginated the
result with user's consent.

Once achieved, I though of extending to many more formats that I often use. And thus, it
led to the birth of `FileAnalysis`.

Changelog
---------

**Current SNAPSHOT**

* Added `CSV` format - comma-delimited files
* Added Apache `log4j` format
* Added `logback` format
* Added Apache `httpd` log format
* Added `TSV` format - tab-delimited files
* Added pipe-delimited format
* Added custom-delimited format

Dependencies
------------

The project depends on the following open-source projects:

* slf4j: logging framework
* OpenCSV: the CSV reader
* H2 database: the SQL database
* jerry-core: utility functions in general
* commons-io: utility functions around I/O
* commons-lang: utility functions around core primitives

Versioning
----------

For transparency and insight into our release cycle, and for striving to maintain backward compatibility, 
`FileAnalysis` will be maintained under the Semantic Versioning guidelines as much as possible.

Releases will be numbered with the follow format:

`<major>.<minor>.<patch>`

And constructed with the following guidelines:

* Breaking backward compatibility bumps the major
* New additions without breaking backward compatibility bumps the minor
* Bug fixes and misc changes bump the patch

For more information on SemVer, please visit http://semver.org/.

License
-------
	
Copyright (c) 2014, Sandeep Gupta

The project uses various other libraries that are subject to their
own license terms. See the distribution libraries or the project
documentation for more details.

The entire source is licensed under the Apache License, Version 2.0 
(the "License"); you may not use this work except in compliance with
the LICENSE. You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
