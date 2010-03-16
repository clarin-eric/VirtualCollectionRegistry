CLARIN Virtual Collection Registry
----------------------------------

The connection to the database is configured using JNDI using the
name "jdbc/VirtualCollectionStore".
When using Apache Tomcat add the following to the context configuration
of the web application (by either modifying "server.xml" or providing an
appropiate context configuration, e.g. like
  "$CATALINA_HOME/conf/Catalina/localhost/VirtualCollectionRegistry.xml";
if in doubt, please check the Apache Tomcat documentation):

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
  Customize $dbuser, $dbpass and $dbname to match your local settings.
  NOTE: currently only MySQL is supported.

  Furthermore, you will need to set the base URI for virtual collection
  registry (customize value as needed):
  <Parameter name="pid_provider.base_uri"
             value="http://127.0.0.1:8080/VirtualCollectionRegistry"
             override="false"/>
