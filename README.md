# jdbc-json
jdbc driver for parsing jsno/xml files direclty to database.  
  
At the moment this is not yet added to dev-env.zip. This will be done soon.

## Quick Start (for windows 64bit) - currently broken, will be fixed soon
For setting up the development environment you don't need install anything. 
You just download the zip file and start the downloaded software with following steps:  

* create folder c:\vnetcon
* Download Development environment [here](http://vnetcon.s3-website-eu-west-1.amazonaws.com/dev-env.zip) to c:\vnetcon
* unzip the file. After this you should have c:\vnetcon\dev-env folder
* Create folder c:\etc\vnetcon and copy the database.properties file there
* Start databaes by double clicking 1_StartPostgreSQL.bat
* Start [DBeaver](https://dbeaver.io/) database tool by double clicking 2_StartDBeaver.bat
* Start Tomcat by double clicking 3_StartTomcat8.bat

After this you can point your browser to http://localhost:8080/jdbc-rest/rest/default/getUser/v1?userid=3 
to see the demo json.  
The DBeaver has ready configured setting for creating and executing sql statements against local postgresql database.


## Building
1. Clone the repo and move to the folder where pom.xml exists
2. execute: mvn clean isntall
3. execute: mvn package  
4. Use the *-with-dependencies.jar" as your jdbc driver

## Suported databases
In theory all databases that have JDBC driver. Postgresql, Oracle, SQL Server etc.

## Commercial use
If you want to use this in closed code project or product you can buy a 99 USD license [here](https://vnetcon.com)  
If you think the price is too low or high you can also change the price there :)

## Screenshots
Below is a screenshot of the fincoice_utf8.xml file that have been extracted to tables into database.
In this picture the generated tables are on the left side (tablelist) and on main view you can see ER diagram generetad by DBeaver.
All the relatios between tables are genereted automcatcally during the table generation...

![jdbc-rest-dbeaver](http://vnetcon.s3-website-eu-west-1.amazonaws.com/img/jdbc-json-dbeaver.png)



