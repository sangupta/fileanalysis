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

Once file load is complete, you will receive a prompt for `fa-query $` where you can fire some queries.
Following are some samples of the same. To exit the prompt, use any of the command like `quit` or `exit`.

The database `TABLE` created from the file is dependent on the format:

For CSV, Pipe-delimited, Tab-delimited, custom-delimited file: DATA
For Apache, Log4j, Logback: LOGS

To see what columns were created:

```
fa-query $ show columns from data;
 | FIELD     | TYPE         | NULL | KEY | DEFAULT                                                                     
 | --------- | ------------ | ---- | --- | ----------------------------------------------------------------------------
 | LINENUM   | BIGINT(19)   | NO   |     | (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_6C6A0A9C_6162_4500_9526_64DA8B5E8B53)
 | STREET    | VARCHAR(255) | YES  |     | NULL                                                                        
 | CITY      | VARCHAR(255) | YES  |     | NULL                                                                        
 | ZIP       | BIGINT(19)   | YES  |     | NULL                                                                        
 | STATE     | VARCHAR(255) | YES  |     | NULL                                                                        
 | BEDS      | BIGINT(19)   | YES  |     | NULL                                                                        
 | BATHS     | BIGINT(19)   | YES  |     | NULL                                                                        
 | SQFT      | BIGINT(19)   | YES  |     | NULL                                                                        
 | TYPE      | VARCHAR(255) | YES  |     | NULL                                                                        
 | SALE_DATE | VARCHAR(255) | YES  |     | NULL                                                                        
 | PRICE     | BIGINT(19)   | YES  |     | NULL                                                                        
 | LATITUDE  | DOUBLE(17)   | YES  |     | NULL                                                                        
 | LONGITUDE | DOUBLE(17)   | YES  |     | NULL                                                                        

Total number of records found: 13
```

To view all data:

```
fa-query $ select * from data;
 | LINENUM | STREET                          | CITY           | ZIP   | STATE | BEDS | BATHS | SQFT | TYPE        | SALE_DATE                    | PRICE  | LATITUDE  | LONGITUDE  
 | ------- | ------------------------------- | -------------- | ----- | ----- | ---- | ----- | ---- | ----------- | ---------------------------- | ------ | --------- | -----------
 | 1       | 3526 HIGH ST                    | SACRAMENTO     | 95838 | CA    | 2    | 1     | 836  | Residential | Wed May 21 00:00:00 EDT 2008 | 59222  | 38.631913 | -121.434879
 | 2       | 51 OMAHA CT                     | SACRAMENTO     | 95823 | CA    | 3    | 1     | 1167 | Residential | Wed May 21 00:00:00 EDT 2008 | 68212  | 38.478902 | -121.431028
 | 3       | 2796 BRANCH ST                  | SACRAMENTO     | 95815 | CA    | 2    | 1     | 796  | Residential | Wed May 21 00:00:00 EDT 2008 | 68880  | 38.618305 | -121.443839
 | 4       | 2805 JANETTE WAY                | SACRAMENTO     | 95815 | CA    | 2    | 1     | 852  | Residential | Wed May 21 00:00:00 EDT 2008 | 69307  | 38.616835 | -121.439146
 | 5       | 6001 MCMAHON DR                 | SACRAMENTO     | 95824 | CA    | 2    | 1     | 797  | Residential | Wed May 21 00:00:00 EDT 2008 | 81900  | 38.51947  | -121.435768
 | 6       | 5828 PEPPERMILL CT              | SACRAMENTO     | 95841 | CA    | 3    | 1     | 1122 | Condo       | Wed May 21 00:00:00 EDT 2008 | 89921  | 38.662595 | -121.327813
 | 7       | 6048 OGDEN NASH WAY             | SACRAMENTO     | 95842 | CA    | 3    | 2     | 1104 | Residential | Wed May 21 00:00:00 EDT 2008 | 90895  | 38.681659 | -121.351705
 | 8       | 2561 19TH AVE                   | SACRAMENTO     | 95820 | CA    | 3    | 1     | 1177 | Residential | Wed May 21 00:00:00 EDT 2008 | 91002  | 38.535092 | -121.481367
 | 9       | 11150 TRINITY RIVER DR Unit 114 | RANCHO CORDOVA | 95670 | CA    | 2    | 2     | 941  | Condo       | Wed May 21 00:00:00 EDT 2008 | 94905  | 38.621188 | -121.270555
 | 10      | 7325 10TH ST                    | RIO LINDA      | 95673 | CA    | 3    | 2     | 1146 | Residential | Wed May 21 00:00:00 EDT 2008 | 98937  | 38.700909 | -121.442979
 | 11      | 645 MORRISON AVE                | SACRAMENTO     | 95838 | CA    | 3    | 2     | 909  | Residential | Wed May 21 00:00:00 EDT 2008 | 100309 | 38.637663 | -121.45152 
 | 12      | 4085 FAWN CIR                   | SACRAMENTO     | 95823 | CA    | 3    | 2     | 1289 | Residential | Wed May 21 00:00:00 EDT 2008 | 106250 | 38.470746 | -121.458918
 | 13      | 2930 LA ROSA RD                 | SACRAMENTO     | 95815 | CA    | 1    | 1     | 871  | Residential | Wed May 21 00:00:00 EDT 2008 | 106852 | 38.618698 | -121.435833
 | 14      | 2113 KIRK WAY                   | SACRAMENTO     | 95822 | CA    | 3    | 1     | 1020 | Residential | Wed May 21 00:00:00 EDT 2008 | 107502 | 38.482215 | -121.492603
 | 15      | 4533 LOCH HAVEN WAY             | SACRAMENTO     | 95842 | CA    | 2    | 2     | 1022 | Residential | Wed May 21 00:00:00 EDT 2008 | 108750 | 38.672914 | -121.35934 
 | 16      | 7340 HAMDEN PL                  | SACRAMENTO     | 95842 | CA    | 2    | 2     | 1134 | Condo       | Wed May 21 00:00:00 EDT 2008 | 110700 | 38.700051 | -121.351278
 | 17      | 6715 6TH ST                     | RIO LINDA      | 95673 | CA    | 2    | 1     | 844  | Residential | Wed May 21 00:00:00 EDT 2008 | 113263 | 38.689591 | -121.452239
 | 18      | 6236 LONGFORD DR Unit 1         | CITRUS HEIGHTS | 95621 | CA    | 2    | 1     | 795  | Condo       | Wed May 21 00:00:00 EDT 2008 | 116250 | 38.679776 | -121.314089
 | 19      | 250 PERALTA AVE                 | SACRAMENTO     | 95833 | CA    | 2    | 1     | 588  | Residential | Wed May 21 00:00:00 EDT 2008 | 120000 | 38.612099 | -121.469095
 | 20      | 113 LEEWILL AVE                 | RIO LINDA      | 95673 | CA    | 3    | 2     | 1356 | Residential | Wed May 21 00:00:00 EDT 2008 | 121630 | 38.689999 | -121.46322 

Type "it" for more:
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
