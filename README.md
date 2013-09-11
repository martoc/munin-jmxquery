munin-jmxquery
==============

This project has been forked from munin-monitoring/contrib, this munin plugin allows you to monitor MBean attributes in Munin.

Folder "etc" contains configuration files for Java, C3P0 and Tomcat monitoring examples.
The format of configuration file is a superset of Munin plugin "config" command output
(http://munin.projects.linpro.no/wiki/protocol-config)

these are some examples

Fixed MBean name

catalina_error_count.label errors
catalina_error_count.jmxObjectName Catalina:name=http-8080,type=GlobalRequestProcessor
catalina_error_count.jmxAttributeName errorCount
catalina_error_count.type DERIVE
catalina_error_count.min 0

Dynamic MBean Name (Using the wildcard)

c3p0_connections_busy.label busy
c3p0_connections_busy.jmxObjectName com.mchange.v2.c3p0:type=PooledDataSource*
c3p0_connections_busy.jmxAttributeName numBusyConnections
