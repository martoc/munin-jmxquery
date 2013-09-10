package org.munin;

import java.io.*;
import java.util.*;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class Configuration {
  public class FieldProperties extends Properties {

    public String getJmxAttributeKey() {
      return jmxAttributeKey;
    }

    public String getJmxAttributeName() {
      return jmxAttributeName;
    }

    public ObjectName getJmxObjectName() {
      return jmxObjectName;
    }

    public String toString() {
      return fieldname;
    }

    public void set(String key, String value) throws MalformedObjectNameException,
        NullPointerException {
      if ("jmxObjectName".equals(key)) {
        if (jmxObjectName != null)
          throw new IllegalStateException((new StringBuilder())
              .append("jmxObjectName already set for ").append(this).toString());
        jmxObjectName = new ObjectName(value);
      } else if ("jmxAttributeName".equals(key)) {
        if (jmxAttributeName != null)
          throw new IllegalStateException((new StringBuilder())
              .append("jmxAttributeName already set for ").append(this).toString());
        jmxAttributeName = value;
      } else if ("jmxAttributeKey".equals(key)) {
        if (jmxAttributeKey != null)
          throw new IllegalStateException((new StringBuilder())
              .append("jmxAttributeKey already set for ").append(this).toString());
        jmxAttributeKey = value;
      } else {
        put(key, value);
      }
    }

    public void report(PrintStream out) {
      java.util.Map.Entry entry;
      for (Iterator it = entrySet().iterator(); it.hasNext(); out.println((new StringBuilder())
          .append(fieldname).append('.').append(entry.getKey()).append(" ")
          .append(entry.getValue()).toString()))
        entry = (java.util.Map.Entry) it.next();

    }

    public String getFieldname() {
      return fieldname;
    }

    private static final long serialVersionUID = 1L;
    private ObjectName jmxObjectName;
    private String jmxAttributeName;
    private String jmxAttributeKey;
    private String fieldname;
    private static final String JMXOBJECT = "jmxObjectName";
    private static final String JMXATTRIBUTE = "jmxAttributeName";
    private static final String JMXATTRIBUTEKEY = "jmxAttributeKey";
    final Configuration this$0;

    public FieldProperties(String fieldname) {
      this$0 = Configuration.this;
      this.fieldname = fieldname;
    }
  }


  private Configuration() {
    graph_properties = new Properties();
    fieldMap = new HashMap();
    fields = new ArrayList();
  }

  public static Configuration parse(String config_file) throws IOException,
      MalformedObjectNameException, NullPointerException {
    BufferedReader reader;
    Configuration configuration;
    reader = new BufferedReader(new FileReader(config_file));
    configuration = new Configuration();
    do {
      String s = reader.readLine();
      if (s == null) break;
      if (!s.startsWith("%") && s.length() > 5 && !s.startsWith(" ")) configuration.parseString(s);
    } while (true);
    reader.close();
    return configuration;
  }

  private void parseString(String s) throws MalformedObjectNameException, NullPointerException {
    String nameval[] = s.split(" ", 2);
    if (nameval[0].indexOf('.') > 0) {
      String name = nameval[0];
      String fieldname = name.substring(0, name.lastIndexOf('.'));
      FieldProperties field;
      if (!fieldMap.containsKey(fieldname)) {
        field = new FieldProperties(fieldname);
        fieldMap.put(fieldname, field);
        fields.add(field);
      }
      field = (FieldProperties) fieldMap.get(fieldname);
      String key = name.substring(name.lastIndexOf('.') + 1);
      field.set(key, nameval[1]);
    } else {
      graph_properties.put(nameval[0], nameval[1]);
    }
  }

  public Properties getGraphProperties() {
    return graph_properties;
  }

  public void report(PrintStream out) {
    java.util.Map.Entry entry;
    for (Iterator it = graph_properties.entrySet().iterator(); it.hasNext(); out
        .println((new StringBuilder()).append(entry.getKey()).append(" ").append(entry.getValue())
            .toString()))
      entry = (java.util.Map.Entry) it.next();

    FieldProperties field;
    for (Iterator i$ = fields.iterator(); i$.hasNext(); field.report(out))
      field = (FieldProperties) i$.next();

  }

  public List getFields() {
    return fields;
  }

  private Properties graph_properties;
  private Map fieldMap;
  private List fields;
}
