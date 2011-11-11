package com.butent.bee.shared.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Handles data table information storage in XML structure.
 */
@XmlRootElement(name = "BeeTable", namespace = DataUtils.DEFAULT_NAMESPACE)
public class XmlTable {

  /**
   * Handles data field information storage in XML structure.
   */
  @XmlRootElement(name = "BeeField", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlField {
    @XmlAttribute
    public String name;
    @XmlAttribute
    public String type;
    @XmlAttribute
    public int precision;
    @XmlAttribute
    public int scale;
    @XmlAttribute
    public boolean notNull;
    @XmlAttribute
    public boolean unique;
    @XmlAttribute
    public String relation;
    @XmlTransient
    public String relationField;
    @XmlAttribute
    public String cascade;
    @XmlAttribute
    public boolean translatable;

    private boolean safe = false;

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      XmlField other = (XmlField) obj;

      if (name == null) {
        if (other.name != null) {
          return false;
        }
      } else if (!BeeUtils.same(name, other.name)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
    }

    public boolean isProtected() {
      return safe;
    }
  }

  /**
   * Handles table key information storage in XML structure.
   */
  @XmlRootElement(name = "BeeKey", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlKey {
    @XmlAttribute
    public boolean unique;
    @XmlElement(name = "KeyField", namespace = DataUtils.DEFAULT_NAMESPACE)
    public List<String> fields;

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      XmlKey other = (XmlKey) obj;

      if (fields == null) {
        if (other.fields != null) {
          return false;
        }
      } else if (!fields.equals(other.fields)) {
        return false;
      }
      if (unique != other.unique) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((fields == null) ? 0 : fields.hashCode());
      result = prime * result + (unique ? 1231 : 1237);
      return result;
    }
  }

  @XmlAttribute
  public String name;
  @XmlAttribute
  public String idName;
  @XmlAttribute
  public String versionName;
  @XmlAttribute
  public int x;
  @XmlAttribute
  public int y;

  @XmlElementWrapper(name = "BeeFields", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElementRef
  public List<XmlField> fields;

  @XmlElementWrapper(name = "BeeExtended", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElementRef
  public List<XmlField> extFields;

  @XmlElementWrapper(name = "BeeStates", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElement(name = "BeeState", namespace = DataUtils.DEFAULT_NAMESPACE)
  public Set<String> states;

  @XmlElementWrapper(name = "BeeKeys", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElementRef
  public Set<XmlKey> keys;

  private boolean safe = false;

  public XmlTable getChanges(XmlTable otherTable) {
    XmlTable diff = null;

    if (otherTable != null) {
      boolean upd = false;
      diff = new XmlTable();
      diff.name = name;

      upd = upd || !BeeUtils.equals(x, otherTable.x);
      diff.x = otherTable.x;

      upd = upd || !BeeUtils.equals(y, otherTable.y);
      diff.y = otherTable.y;

      if (!BeeUtils.isEmpty(otherTable.fields)) {
        for (XmlField field : otherTable.fields) {
          if (!isProtected(field)) {
            if (diff.fields == null) {
              diff.fields = Lists.newArrayList(field);

            } else if (diff.findField(field) == null) {
              diff.fields.add(field);
            }
            upd = true;
          }
        }
      }
      if (!BeeUtils.isEmpty(otherTable.extFields)) {
        for (XmlField field : otherTable.extFields) {
          if (!isProtected(field)) {
            if (diff.extFields == null) {
              diff.extFields = Lists.newArrayList(field);

            } else if (diff.findField(field) == null) {
              diff.extFields.add(field);
            }
            upd = true;
          }
        }
      }
      if (!BeeUtils.isEmpty(otherTable.states)) {
        for (String state : otherTable.states) {
          if (states == null || !states.contains(state)) {
            if (diff.states == null) {
              diff.states = Sets.newHashSet(state);
            } else {
              diff.states.add(state);
            }
            upd = true;
          }
        }
      }
      if (!BeeUtils.isEmpty(otherTable.keys)) {
        for (XmlKey key : otherTable.keys) {
          if (keys == null || !keys.contains(key)) {
            if (diff.keys == null) {
              diff.keys = Sets.newHashSet(key);
            } else {
              diff.keys.add(key);
            }
            upd = true;
          }
        }
      }
      if (!upd) {
        diff = null;
      }
    }
    return diff;
  }

  public boolean isProtected() {
    return safe;
  }

  public boolean isProtected(XmlField field) {
    XmlField fld = findField(field);

    if (fld != null) {
      return fld.isProtected();
    }
    return false;
  }

  public void merge(XmlTable otherTable) {
    XmlTable diff = getChanges(otherTable);

    if (diff != null) {
      x = diff.x;
      y = diff.y;

      if (!BeeUtils.isEmpty(diff.fields)) {
        if (fields == null) {
          fields = Lists.newArrayList();
        }
        for (XmlField field : diff.fields) {
          removeField(field);
          fields.add(field);
        }
      }
      if (!BeeUtils.isEmpty(diff.extFields)) {
        if (extFields == null) {
          extFields = Lists.newArrayList();
        }
        for (XmlField field : diff.extFields) {
          removeField(field);
          extFields.add(field);
        }
      }
      if (!BeeUtils.isEmpty(diff.states)) {
        if (states == null) {
          states = Sets.newHashSet();
        }
        states.addAll(diff.states);
      }
      if (!BeeUtils.isEmpty(diff.keys)) {
        if (keys == null) {
          keys = Sets.newHashSet();
        }
        keys.addAll(diff.keys);
      }
    }
  }

  public XmlTable protect() {
    if (!BeeUtils.isEmpty(fields)) {
      for (XmlField field : fields) {
        field.safe = true;
      }
    }
    if (!BeeUtils.isEmpty(extFields)) {
      for (XmlField field : extFields) {
        field.safe = true;
      }
    }
    this.safe = true;
    return this;
  }

  private XmlField findField(XmlField field) {
    if (!BeeUtils.isEmpty(fields)) {
      for (XmlField fld : fields) {
        if (BeeUtils.equals(field, fld)) {
          return fld;
        }
      }
    }
    if (!BeeUtils.isEmpty(extFields)) {
      for (XmlField fld : extFields) {
        if (BeeUtils.equals(field, fld)) {
          return fld;
        }
      }
    }
    return null;
  }

  private void removeField(XmlField field) {
    if (!BeeUtils.isEmpty(fields)) {
      fields.remove(field);
    }
    if (!BeeUtils.isEmpty(extFields)) {
      extFields.remove(field);
    }
  }
}
