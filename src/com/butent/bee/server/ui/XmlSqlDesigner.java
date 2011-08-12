package com.butent.bee.server.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.server.sql.SqlConstants.SqlDataType;
import com.butent.bee.shared.data.XmlTable;
import com.butent.bee.shared.data.XmlTable.XmlField;
import com.butent.bee.shared.data.XmlTable.XmlKey;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Enables to design system database using graphical tool.
 */

@XmlRootElement(name = "sql")
public class XmlSqlDesigner {

  public static final String EXT = "_EXT";
  public static final String STATE = "STATE";

  /**
   * Contains a list of possible key types.
   */

  private enum KeyType {
    PRIMARY, UNIQUE, INDEX
  }

  /**
   * Handles xml elements containing information about data types.
   */

  public static class DataType {
    @XmlAttribute
    public String label;
    @XmlAttribute
    public String sql;
  }

  /**
   * Handles xml elements containing information about data type groups.
   */

  public static class DataTypeGroup {
    @XmlAttribute
    public String label;
    @XmlAttribute
    public String color;
    @XmlElement(name = "type")
    public Collection<DataType> types;
  }

  /**
   * Handles xml elements containing information about data relations.
   */

  private static class DataRelation {
    @XmlAttribute
    private String table;
    @XmlAttribute(name = "row")
    private String field;
  }

  /**
   * Handles xml elements containing information about data fields.
   */

  private static class DataField {
    @XmlAttribute
    private String name;
    @XmlAttribute(name = "null")
    private int isNull;
    @XmlAttribute(name = "autoincrement")
    private int isTranslatable;
    @XmlElement(name = "datatype")
    private String type;
    @SuppressWarnings("unused")
    @XmlElement
    private String comment;
    @XmlElement
    private DataRelation relation;

    private DataField() {
    }

    private DataField(String name, String type, boolean notNull, boolean translatable,
        boolean isProtected) {
      this.name = name;
      this.type = type;
      this.isNull = notNull ? 0 : 1;
      this.isTranslatable = translatable ? 1 : 0;

      if (isProtected) {
        this.comment = "PROTECTED";
      }
    }
  }

  /**
   * Handles xml elements containing information about data keys.
   */

  private static class DataKey {
    @XmlAttribute
    private KeyType type;
    @XmlElement(name = "part")
    private List<String> parts;

    private DataKey() {
    }

    private DataKey(KeyType type, String... parts) {
      this.type = type;
      this.parts = Lists.newArrayList(parts);
    }
  }

  /**
   * Handles xml elements containing information about data tables.
   */

  private static class DataTable {
    @XmlAttribute
    private String name;

    @XmlElement(name = "row")
    private Collection<DataField> fields;

    @XmlElement(name = "key")
    private Collection<DataKey> keys;

    private String getPrimaryKeyField() {
      String primary = null;

      if (keys != null) {
        for (DataKey key : keys) {
          if (key.type == KeyType.PRIMARY) {
            if (!BeeUtils.isEmpty(primary) || key.parts == null || key.parts.size() != 1) {
              primary = null;
              break;
            }
            primary = key.parts.iterator().next();
          }
        }
      }
      return primary;
    }

    private boolean isUnique(String fieldName) {
      boolean unique = false;

      if (keys != null) {
        for (DataKey key : keys) {
          if (key.type == KeyType.UNIQUE) {
            unique = key.parts != null
                && key.parts.size() == 1
                && BeeUtils.same(key.parts.iterator().next(), fieldName);

            if (unique) {
              break;
            }
          }
        }
      }
      return unique;
    }
  }

  /**
   * Enables transforming xml information about tables into objects.
   */

  private static class XmlTableAdapter extends XmlAdapter<DataTable, XmlTable> {
    @Override
    public DataTable marshal(XmlTable xmlTable) throws Exception {
      DataTable table = null;

      if (xmlTable != null) {
        table = new DataTable();
        table.name = xmlTable.name;
        table.fields = Lists.newArrayList(
            new DataField(xmlTable.idName, SqlDataType.LONG.name(), true, false,
                xmlTable.isProtected()));
        table.keys = Lists.newArrayList(new DataKey(KeyType.PRIMARY, xmlTable.idName));

        for (int i = 0; i < 2; i++) {
          boolean extMode = (i > 0);
          Collection<XmlField> fields = (extMode ? xmlTable.extFields : xmlTable.fields);

          if (!BeeUtils.isEmpty(fields)) {
            for (XmlField xmlField : fields) {
              String type = xmlField.type + (extMode ? EXT : "");

              if (BeeUtils.isPositive(xmlField.precision)) {
                type = type + "(" + xmlField.precision;

                if (BeeUtils.isPositive(xmlField.scale)) {
                  type = type + "," + xmlField.scale;
                }
                type = type + ")";
              }
              DataField field =
                    new DataField(xmlField.name, type, xmlField.notNull, xmlField.translatable,
                        xmlField.isProtected());

              if (!BeeUtils.isEmpty(xmlField.relation)) {
                field.relation = new DataRelation();
                field.relation.table = xmlField.relation;
                field.relation.field = xmlField.relationField;
              }
              table.fields.add(field);

              if (xmlField.unique) {
                table.keys.add(new DataKey(KeyType.UNIQUE, xmlField.name));
              }
            }
          }
        }
        if (!BeeUtils.isEmpty(xmlTable.states)) {
          for (String state : xmlTable.states) {
            table.fields.add(new DataField(state, STATE, false, false, false));
          }
        }
        if (!BeeUtils.isEmpty(xmlTable.keys)) {
          for (XmlKey xmlKey : xmlTable.keys) {
            table.keys.add(new DataKey(xmlKey.unique ? KeyType.UNIQUE : KeyType.INDEX,
                xmlKey.fields.toArray(new String[0])));
          }
        }
      }
      return table;
    }

    @Override
    public XmlTable unmarshal(DataTable table) throws Exception {
      XmlTable xmlTable = null;

      if (table != null) {
        xmlTable = new XmlTable();
        xmlTable.name = table.name;
        xmlTable.idName = table.getPrimaryKeyField();

        if (!BeeUtils.isEmpty(table.fields)) {
          for (DataField field : table.fields) {
            if (BeeUtils.same(field.type, STATE)) {
              if (xmlTable.states == null) {
                xmlTable.states = Sets.newHashSet();
              }
              xmlTable.states.add(field.name);

            } else if (!BeeUtils.same(field.name, xmlTable.idName)) {
              XmlField xmlField = new XmlField();
              xmlField.name = field.name;
              xmlField.notNull = BeeUtils.isEmpty(field.isNull);
              xmlField.unique = table.isUnique(field.name);
              xmlField.translatable = !BeeUtils.isEmpty(field.isTranslatable);

              if (field.relation != null) {
                xmlField.relation = field.relation.table;
                xmlField.relationField = field.relation.field;
              }
              boolean extMode = false;

              if (!BeeUtils.isEmpty(field.type)) {
                String pattern = "^([a-zA-Z]+)(" + EXT + "){0,1}(\\((\\d+)(,(\\d+)){0,1}\\)){0,1}$";
                xmlField.type = field.type.replaceFirst(pattern, "$1");
                extMode = !BeeUtils.isEmpty(field.type.replaceFirst(pattern, "$2"));
                xmlField.precision = BeeUtils.toInt(field.type.replaceFirst(pattern, "$4"));
                xmlField.scale = BeeUtils.toInt(field.type.replaceFirst(pattern, "$6"));
              }
              if (extMode) {
                if (xmlTable.extFields == null) {
                  xmlTable.extFields = Lists.newArrayList();
                }
                xmlTable.extFields.add(xmlField);
              } else {
                if (xmlTable.fields == null) {
                  xmlTable.fields = Lists.newArrayList();
                }
                xmlTable.fields.add(xmlField);
              }
            }
          }
        }
        if (!BeeUtils.isEmpty(table.keys)) {
          for (DataKey key : table.keys) {
            if (key.type != KeyType.PRIMARY && key.parts != null) {
              if (key.type != KeyType.UNIQUE || key.parts.size() > 1) {
                XmlKey xmlKey = new XmlKey();
                xmlKey.unique = (key.type == KeyType.UNIQUE);
                xmlKey.fields = key.parts;

                if (xmlTable.keys == null) {
                  xmlTable.keys = Sets.newHashSet();
                }
                xmlTable.keys.add(xmlKey);
              }
            }
          }
        }
      }
      return xmlTable;
    }
  }

  @XmlElementWrapper(name = "datatypes")
  @XmlElement(name = "group")
  public Collection<DataTypeGroup> types;

  @XmlElement(name = "table")
  @XmlJavaTypeAdapter(XmlTableAdapter.class)
  public Collection<XmlTable> tables;
}
