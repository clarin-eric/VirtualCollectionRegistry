CLARIN Virtual Collection Registry
----------------------------------

The connection to the database is configured using JNDI using
the name "jdbc/VirtualCollectionStore".
When using Apache Tomcat add the following to the Context of the Web-App
(either modifying "server.xml" or by providing the appropriate context.xml,
 e.g. "$CATALINA_HOME/conf/Catalina/localhost/VirtualCollectionRegistry.xml";
 if in doubt, check the Apache Tomcat documentation):

  <Resource name="jdbc/VirtualCollectionStore" auth="Container"
            type="javax.sql.DataSource"
            maxActive="8" maxIdle="4" maxWait="10000"
            removeAbandoned="true"
            defaultAutoCommit="false"
            driverClassName="com.mysql.jdbc.Driver"
            validationQuery="SELECT 1"
            username="$dbuser"
            password="$dbpass"
            url="jdbc:mysql://127.0.0.1:3306/$dbname" />

  This will configure a DBCP data source for the virtual collection registry.
  Customize $dbuser, $dbpass and $dbname to match your local setiings.
  NOTE: currently only MySQL is supported.
