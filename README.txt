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

  c) For using the EPIC API v2 handle provider add following and customize the
     base URI for the virtual collection registry and… TODO

  <Parameter name="spring.profiles.active"
             value="vcr.pid.epic”
             override="false"/>   
  <Parameter name="pid_provider.base_uri"
             value="http://127.0.0.1:8080/VirtualCollectionRegistry"
             override="false"/>

To shibbolize this application, the following steps are required:

1. Use the shibboleth version of web.xml called 'web-shib.xml' instead of
the default one by renaming it to and overwriting web.xml (you can make a
backup of the original web.xml). If the package was built for a production
environment, it should already have the right web.xml in place.

2. Add the following to the relevant Apache configuration:

        <Location /vcr>
            ProxyPass ajp://localhost:8009/vcr
            AuthType            shibboleth
            ShibRequireSession  Off
            ShibUseHeaders      On
            Satisfy             All
            Require             shibboleth
        </Location>

        <Location /vcr/service/submit>
            ShibRequireSession  On
        </Location>

Adjust locations to the desired and relevant local alternatives. The second
block is required to make the virtual collection form submit service work
with POSTs (current versions of SHHAA do not support this).
