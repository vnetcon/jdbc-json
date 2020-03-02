# jdbc-json
jdbc driver for parsing jsno/xml files direclty to database. 

## Building
mvn clean install  
mvn package

## Quick Start in DBeaver

1. Create a connecton using jdbc-json driver.
2. Add the actual driver (e.g. postgresql driver) into driver managers libraries in same connection
3. Open the connection using path to file and schema where you want to extranct the json/xml file
4. Start browsing the automatically genereated files.

Below is a screenshot of the fincoice_utf8.xml file that have been extracted to tables into database.
In this picture the generated tables are on the left side (tablelist) and on main view you can see ER diagram generetad by DBeaver.
All the relatios between tables are genereted automcatcally during the table generation...

![jdbc-rest-dbeaver](http://vnetcon.s3-website-eu-west-1.amazonaws.com/img/jdbc-json-dbeaver.png)


//TODO: Finalize this documentation, add the code and create wiki pages. In short we will update these pages soon


