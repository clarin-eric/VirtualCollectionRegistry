CLARIN Virtual Collection Registry
----------------------------------

* DATABASE CONNECTION *

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

* CONTEXT PARAMETERS *

PUBLIC APPLICATION URL:

Add the following context parameter (typically to the global context.xml file)
and substitute the value with the correct *public* base location of the VCR:

  <Parameter name="eu.clarin.cmdi.virtualcollectionregistry.base_uri"
             value="http://my/server/VirtualCollectionRegistry"
             override="false"/>

Add no trailing slash. You can verify this by checking the service links in the
help page of the running application.

BETA MODE:

Add the following context parameter (typically to the global context.xml file):

    <Parameter name="eu.clarin.cmdi.virtualcollectionregistry.beta_mode"
             value="true"
             override="false"/>

Set the value "true" to enable beta mode or "false" to disable beta mode.
Defaults to "false" if this parameter is not provided.

ADMINISTRATOR USERS:

Add the following context parameter:

<Parameter
	   name="eu.clarin.cmdi.virtualcollectionregistry.admindb"
	   value="/path/to/vcr-admin.conf" />

Add the username of each user that should get administrator rights to the 
referenced file, one username per line.

* PID PROVIDER *

  You need to choose, which persistent identifier provider you want to use.
  You can either use a dummy provider or the GWDG handle provider.
  a) For using the dummy provider add the following:
  <Parameter name="spring.profiles.active"
             value="vcr.pid.dummy"
             override="false"/>   

  b) For using the GWDG handle provider add following and customize the
     values for $gwdg_user and $gwdg_password:

  <Parameter name="spring.profiles.active"
             value="vcr.pid.gwdg"
             override="false"/>   
  <Parameter name="pid_provider.username" value="$gwdg_user" override="false"/>
  <Parameter name="pid_provider.password" value="$gwdg_password" override="false"/>

  c) For using the EPIC API v2 handle provider add following and customize the
     values for $epic_user and $epic_password:
     
  <Parameter name="spring.profiles.active"
             value="vcr.pid.epic”
             override="false"/>   

  <Parameter name="pid_provider.epic.service_base_url"
             value="http://pid-vm04.gwdg.de:8080/handles/"
             override="false"/>               
  <Parameter name="pid_provider.epic.handle_prefix"
             value="11148"
             override="false"/>     
  <Parameter name="pid_provider.epic.user"
             value="$epic_user"
             override="false"/>     
  <Parameter name="pid_provider.epic.password"
             value="$epic_password"
             override="false"/>
             
  Add the following parameter to configure a custom PID 'infix'. The example below
  configures the default behaviour, i.e. '{prefix}/VCR-{id}'.
   
  <Parameter name="pid_provider.epic.infix"
             value="VCR-"/>
             
* AUTHENTICATION *

The application has two alternative authentication configuration represented by two
versions of the web.xml file. The default web.xml assumes Tomcat UserDatabaseRealm,
which is useful for testing purposes.

Ensure that a user entry exists in tomcat-users.xml and that it has role "vcr":
<user password="tomcat" roles="vcr" username="tomcat"/>

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

3. Configure the right SSO and SLO locations in WEB-INF/shhaa.xml: 

        <authentication>
            ...
            <sso action="lI">https://shib-host/Shibboleth.sso/Login</sso> 
            <slo action="lO">https://shib-host/Shibboleth.sso/Logout</slo>     
        </authentication>

* OAI PROVIDER *

Collection display name:

Add the following to the Tomcat context.xml file to set a custom collection display name 
(MdCollectionDisplayName header element) for the CMDI representations provided by the 
built-in OAI provider:

<Parameter name="eu.clarin.cmdi.virtualcollectionregistry.collectiondisplayname
           value="CLARIN Virtual Collection Registry" />

Change the value of the 'value' attribute if desired. If this parameter is not set,
the default value "CLARIN Virtual Collection Registry" will be used as a collection name.
