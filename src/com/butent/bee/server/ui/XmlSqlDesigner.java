package com.butent.bee.server.ui;

import com.google.common.collect.Lists;

import com.butent.bee.server.sql.SqlConstants.SqlDataType;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "sql")
public class XmlSqlDesigner {

  public enum KeyType {
    PRIMARY, UNIQUE, INDEX
  }

  public static class DataType {
    @XmlAttribute
    public String label;
    @XmlAttribute
    public SqlDataType sql;
  }

  public static class DataTypeGroup {
    @XmlAttribute
    public String label;
    @XmlElement(name = "type")
    public Collection<DataType> types;
  }

  public static class DataRelation {
    @XmlAttribute
    public String table;
    @XmlAttribute(name = "row")
    public String field;

    public DataRelation() {
    }

    public DataRelation(String table, String field) {
      this.table = table;
      this.field = field;
    }
  }

  public static class DataField {
    @XmlAttribute
    public String name;
    @XmlAttribute(name = "null")
    public int isNull;
    @XmlElement(name = "datatype")
    public String type;
    @XmlElement
    public String comment;
    @XmlElement
    public DataRelation relation;

    public DataField() {
    }

    public DataField(String name, String type, boolean notNull, boolean isProtected) {
      this.name = name;
      this.type = type;
      this.isNull = notNull ? 0 : 1;

      if (isProtected) {
        this.comment = "PROTECTED";
      }
    }
  }

  public static class DataKey {
    @XmlAttribute
    public KeyType type;
    @XmlElement(name = "part")
    public Collection<String> parts;

    public DataKey() {
    }

    public DataKey(KeyType type, String... parts) {
      this.type = type;
      this.parts = Lists.newArrayList(parts);
    }
  }

  public static class DataTable {
    @XmlAttribute
    public String name;

    @XmlElement(name = "row")
    public Collection<DataField> fields;

    @XmlElement(name = "key")
    public Collection<DataKey> keys;
  }

  @XmlElementWrapper(name = "datatypes")
  @XmlElement(name = "group")
  public Collection<DataTypeGroup> types;

  @XmlElement(name = "table")
  public Collection<DataTable> tables;
}
