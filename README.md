# jdbc-json
jdbc driver for parsing jsno/xml files direclty to database.  
**This driver is a part of our bigger picture of self hosted vendor independed BigData processing based on Apache Drill. Apache drill have both JDBC and ODBC drivers and can be used with common reporting tools like Tableua, Qlik etc.**  
  
For this setup we have create a development environment that don't require any installations and help people to evaluate the 
technologies immediately. The setup contains following elements 

* Local Apache Drill for testing immediately
* Local Postgresql for used by other software
* Local Tomcat8 + REST API Server for testing the sql based rest api immediately
* Pre installed jdcb-json driver for testing the automatic JSON/XML parsing to postgresql database
* Preconfigured DBeaver to execute the tests immediately


## Quick Start (for windows 64bit) - updated 06/03/2020 (dd/mm/yyyy)
For setting up the development environment you don't need install anything. 
You just download the zip file and start the downloaded software with following steps:  

* create folder c:\vnetcon
* Download Development environment [here](http://vnetcon.s3-website-eu-west-1.amazonaws.com/dev-env.zip) to c:\vnetcon
* unzip the file. After this you should have c:\vnetcon\dev-env folder
* Create folder c:\etc\vnetcon and copy the database.properties file there
* Start apache drill by double clicking 1_StartDrill.bat
* Start database by double clicking 1_StartPostgreSQL.bat
* Start [DBeaver](https://dbeaver.io/) database tool by double clicking 2_StartDBeaver.bat
* Start Tomcat by double clicking 3_StartTomcat8.bat

After this you can point your browser to http://localhost:8080/jdbc-rest/rest/default/getUser/v1?userid=3 
to see the demo json.  
The DBeaver has ready configured setting for creating and executing sql statements against local postgresql database.


## Building
1. Clone the repo and move to the folder where pom.xml exists
2. execute: mvn clean isntall
3. execute: mvn package  
4. Use the *-with-dependencies.jar" as your jdbc drivere

## Suported databases
In theory all databases that have JDBC driver. Postgresql, Oracle, SQL Server etc.

## jdbc url
he key for understanding the jdbc url format is to keep in mind, that the /etc/vnetcon/database.properties file is the 
start point for creating connection. Example from following url
  
```
jdbc:vnetcon:json://default?url=C:\vnetcon\dev-env\example-data&dbschema=finvoice
```

  
the "default" is the "prefix" in configuration parameters in database.properties files.
  
The jdbc-json url can have follosing parameters

* url: thf folder that contains \*.xml and/or \*.json files to parse or http(s) url for rest service that return xml or json
* dbschema: into what schema the tables should be created
* encoding: what encoding the files are
* httpuser: rest service user (basic authentication)
* httppass: rest service password (basic authentication)
* httpfile: the temporary file the http request should be stored (files are loaded actually by curl)
* genfkc: Should the jdbc-rest driver create foreign key konstraints (default false)


## Screenshots
Below is a screenshot where UBL (Universal Business Language) invoice xml have been extracted to tables into database.
In this picture the generated tables are on the left side (tablelist) and on main view you can see ER diagram generetad by DBeaver.
All the relatios between tables are genereted automcatcally during the table generation...

![jdbc-json-dbeaver](http://vnetcon.s3-website-eu-west-1.amazonaws.com/img/jdbc-json-dbeaver.png)



