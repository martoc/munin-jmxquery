package org.munin;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JMXQuery {

  public JMXQuery(String url) {
    this(url, null, null);
  }

  public JMXQuery(String url, String username, String password) {
    this.url = url;
    this.username = username;
    this.password = password;
  }

  private void connect() throws IOException {
    Map environment = null;
    if (username != null && password != null) {
      environment = new HashMap();
      environment.put("jmx.remote.credentials", new String[] {username, password});
      environment.put("username", username);
      environment.put("password", password);
    }
    JMXServiceURL jmxUrl = new JMXServiceURL(url);
    connector = JMXConnectorFactory.connect(jmxUrl, environment);
    connection = connector.getMBeanServerConnection();
  }

  private void list() throws IOException, InstanceNotFoundException, IntrospectionException, ReflectionException {
    if (config == null)
      listAll();
    else
      listConfig();
  }

  private void listConfig() {
    for (Iterator i$ = config.getFields().iterator(); i$.hasNext();) {
      Configuration.FieldProperties field = (Configuration.FieldProperties) i$.next();
      try {
        Object value = null;
        String mBeanName = field.getJmxObjectName().toString();
        if (mBeanName.endsWith("*")) {
          Set mbeans = connection.queryNames(field.getJmxObjectName(), null);
          ObjectName obj = (ObjectName) mbeans.iterator().next();
          value = connection.getAttribute(obj, field.getJmxAttributeName());
        } else {
          value = connection.getAttribute(field.getJmxObjectName(), field.getJmxAttributeName());
        }
        output(field.getFieldname(), value, field.getJmxAttributeKey());
      } catch (Exception e) {
        System.err.println((new StringBuilder()).append("Fail to output ").append(field).toString());
        e.printStackTrace();
      }
    }

  }

  private void output(String name, Object attr, String key) {
    if (attr instanceof CompositeDataSupport) {
      CompositeDataSupport cds = (CompositeDataSupport) attr;
      if (key == null)
        throw new IllegalArgumentException((new StringBuilder()).append("Key is null for composed data ").append(name)
            .toString());
      System.out.println((new StringBuilder()).append(name).append(".value ").append(format(cds.get(key))).toString());
    } else {
      System.out.println((new StringBuilder()).append(name).append(".value ").append(format(attr)).toString());
    }
  }

  private void output(String name, Object attr) {
    if (attr instanceof CompositeDataSupport) {
      CompositeDataSupport cds = (CompositeDataSupport) attr;
      String key;
      for (Iterator it = cds.getCompositeType().keySet().iterator(); it.hasNext(); System.out
          .println((new StringBuilder()).append(name).append(".").append(key).append(".value ")
              .append(format(cds.get(key))).toString()))
        key = it.next().toString();

    } else {
      System.out.println((new StringBuilder()).append(name).append(".value ").append(format(attr)).toString());
    }
  }

  private void listAll() throws IOException, InstanceNotFoundException, IntrospectionException, ReflectionException {
    Set mbeans = connection.queryNames(null, null);
    for (Iterator i$ = mbeans.iterator(); i$.hasNext();) {
      ObjectName name = (ObjectName) i$.next();
      MBeanInfo info = connection.getMBeanInfo(name);
      MBeanAttributeInfo attrs[] = info.getAttributes();
      String attrNames[] = new String[attrs.length];
      for (int i = 0; i < attrs.length; i++)
        attrNames[i] = attrs[i].getName();

      try {
        AttributeList attributes = connection.getAttributes(name, attrNames);
        Iterator i2$ = attributes.asList().iterator();
        while (i2$.hasNext()) {
          Attribute attribute = (Attribute) i2$.next();
          output((new StringBuilder()).append(name.getCanonicalName()).append("%").append(attribute.getName())
              .toString(), attribute.getValue());
        }
      } catch (Exception e) {
        System.err.println((new StringBuilder()).append("error getting ").append(name).append(":")
            .append(e.getMessage()).toString());
      }
    }

  }

  private String format(Object value) {
    if (value == null) return null;
    if (value instanceof String) return (String) value;
    if (value instanceof Number) {
      NumberFormat f = NumberFormat.getInstance();
      f.setMaximumFractionDigits(2);
      f.setGroupingUsed(false);
      return f.format(value);
    }
    if (value instanceof Object[])
      return Integer.toString(Arrays.asList((Object[]) (Object[]) value).size());
    else
      return value.toString();
  }

  private void disconnect() throws IOException {
    connector.close();
  }

  public static void main(String args[]) throws IOException, MalformedObjectNameException, NullPointerException,
      InstanceNotFoundException, IntrospectionException, ReflectionException {
    String url;
    String config_file;
    JMXQuery query;
    int arglen = args.length;
    if (arglen < 1) {
      System.err
          .println("Usage of program is:\njava -cp jmxquery.jar org.munin.JMXQuery --url=<URL> [--user=<username> --pass=<password>] [--conf=<config file> [config]]\n, where <URL> is a JMX URL, for example: service:jmx:rmi:///jndi/rmi://HOST:PORT/jmxrmi\nWhen invoked with the config file (see examples folder) - operates as Munin plugin with the provided configuration\nWithout options just fetches all JMX attributes using provided URL");
      System.exit(1);
    }
    url = null;
    String user = null;
    String pass = null;
    config_file = null;
    boolean toconfig = false;
    for (int i = 0; i < arglen; i++) {
      if (args[i].startsWith("--url=")) {
        url = args[i].substring(6);
        continue;
      }
      if (args[i].startsWith("--user=")) {
        user = args[i].substring(7);
        continue;
      }
      if (args[i].startsWith("--pass=")) {
        pass = args[i].substring(7);
        continue;
      }
      if (args[i].startsWith("--conf=")) {
        config_file = args[i].substring(7);
        continue;
      }
      if (args[i].equals("config")) toconfig = true;
    }

    if (url == null || user != null && pass == null || user == null && pass != null || config_file == null && toconfig) {
      System.err
          .println("Usage of program is:\njava -cp jmxquery.jar org.munin.JMXQuery --url=<URL> [--user=<username> --pass=<password>] [--conf=<config file> [config]]\n, where <URL> is a JMX URL, for example: service:jmx:rmi:///jndi/rmi://HOST:PORT/jmxrmi\nWhen invoked with the config file (see examples folder) - operates as Munin plugin with the provided configuration\nWithout options just fetches all JMX attributes using provided URL");
      System.exit(1);
    }
    if (toconfig) {
      try {
        Configuration.parse(config_file).report(System.out);
      } catch (Exception e) {
        System.err.println((new StringBuilder()).append(e.getMessage()).append(" reading ").append(config_file)
            .toString());
        System.exit(1);
      }
    }
    query = new JMXQuery(url, user, pass);
    query.connect();
    if (config_file != null) query.setConfig(Configuration.parse(config_file));
    query.list();
    try {
      query.disconnect();
    } catch (IOException e) {
      System.err.println((new StringBuilder()).append(e.getMessage()).append(" closing ").append(url).toString());
      System.exit(1);
    }
    try {
      query.disconnect();
    } catch (IOException e) {
      System.err.println((new StringBuilder()).append(e.getMessage()).append(" closing ").append(url).toString());
    }
    try {
      query.disconnect();
    } catch (IOException e) {
      System.err.println((new StringBuilder()).append(e.getMessage()).append(" closing ").append(url).toString());
      System.exit(1);
    }
  }

  private void setConfig(Configuration configuration) {
    config = configuration;
  }

  public Configuration getConfig() {
    return config;
  }

  private static final String USERNAME_KEY = "username";
  private static final String PASSWORD_KEY = "password";
  public static final String USAGE =
      "Usage of program is:\njava -cp jmxquery.jar org.munin.JMXQuery --url=<URL> [--user=<username> --pass=<password>] [--conf=<config file> [config]]\n, where <URL> is a JMX URL, for example: service:jmx:rmi:///jndi/rmi://HOST:PORT/jmxrmi\nWhen invoked with the config file (see examples folder) - operates as Munin plugin with the provided configuration\nWithout options just fetches all JMX attributes using provided URL";
  private String url;
  private String username;
  private String password;
  private JMXConnector connector;
  private MBeanServerConnection connection;
  private Configuration config;
}
