Subsystem Configuration
-----------------------

Possible configuration options of subsystem.

Supported exception sources:

* Debugger
* Logging

Supported exception listeners:

* Mail Listener
* ABRT Listener (with local data storage)

Mail Listener attributes:

* Mail Provider - JNDI name of mail provider

ABRT Listener attributes:

* Data Source - JNDI name of relational data source, where exceptions are stored

Example of Configuration
========================

```
<subsystem xmlns="urn:cz:muni:exception:1.0">
	<sources>
		<logging-source enabled="true" />
		<debugger-source enabled="false" />
	</sources>
	<listeners>
		<mail-listener mail-provider="mail/mymail" />
		<abrt-listener data-source="jdbc/sample" />
	</listeners>
</subsystem>
```
