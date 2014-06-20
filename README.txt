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

  You need to choose, which persistent identifier provider you want to use.
  You can either use a dummy provider or the GWDG handle provider.
  a) For using the dummy provider add the following:
  <Parameter name="spring.profiles.active"
             value="vcr.pid.dummy"
             override="false"/>   

  b) For using the GWDG handle provider add following and customize the
     base URI for the virtual collection registry and the values for
     $gwdg_user and $gwdg_password:

  <Parameter name="spring.profiles.active"
             value="vcr.pid.gwdg"
             override="false"/>   
  <Parameter name="pid_provider.base_uri"
             value="http://127.0.0.1:8080/VirtualCollectionRegistry"
             override="false"/>
  <Parameter name="pid_provider.username" value="$gwdg_user" override="false"/>
  <Parameter name="pid_provider.password" value="$gwdg_password" override="false"/>
