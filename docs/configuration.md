Subsystem Configuration
-----------------------

Possible configuration options of subsystem.

Supported exception sources:

* Debugger
* Logging

Supported exception listeners:

* ABRT Listener (with local data storage)

DataSource Listener attributes:

* Data Source - JNDI name of relational data source, where exceptions are stored

Dispatcher:

* Setup used dispatcher

Example of Configuration
========================

```
<subsystem xmlns="urn:cz:muni:exception:1.0">
    <dispatcher async="true">
        <blacklist>
            <class>java.lang.*</class>
        </blacklist>
    </dispatcher>
	<sources>
		<logging-source enabled="true" />
		<debugger-source enabled="false" />
	</sources>
	<filters>
	    <class name="com.sun.jdi.*" />
	</filters>
	<listeners>
		<database-listener dataSource="jboss/ExampleDS" />
	</listeners>
</subsystem>
```
