package com.butent.bee.shared.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "BeeTable", namespace = DataUtils.DEFAULT_NAMESPACE)
public class XmlTable {

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
    @XmlAttribute
    public boolean cascade;
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

  public static class XmlKey {
    @XmlAttribute
    public boolean unique;
    @XmlElement(name = "KeyField", namespace = DataUtils.DEFAULT_NAMESPACE)
    public Collection<String> fields;

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

  @XmlElementWrapper(name = "BeeFields", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElement(name = "BeeField", namespace = DataUtils.DEFAULT_NAMESPACE)
  public List<XmlField> fields;

  @XmlElementWrapper(name = "BeeExtended", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElement(name = "BeeField", namespace = DataUtils.DEFAULT_NAMESPACE)
  public List<XmlField> extFields;

  @XmlElementWrapper(name = "BeeStates", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElement(name = "BeeState", namespace = DataUtils.DEFAULT_NAMESPACE)
  public Set<String> states;

  @XmlElementWrapper(name = "BeeKeys", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElement(name = "BeeKey", namespace = DataUtils.DEFAULT_NAMESPACE)
  public Set<XmlKey> keys;

  private boolean safe = false;

  public boolean isProtected() {
    return safe;
  }

  public void merge(XmlTable otherTable) {
    if (otherTable != null) {
      if (!BeeUtils.isEmpty(otherTable.fields)) {
        for (XmlField field : otherTable.fields) {
          if ((fields == null || !fields.contains(field))
              && (extFields == null || !extFields.contains(field))) {

            if (fields == null) {
              fields = Lists.newArrayList(field);
            } else {
              fields.add(field);
            }
          }
        }
      }
      if (!BeeUtils.isEmpty(otherTable.extFields)) {
        for (XmlField field : otherTable.extFields) {
          if ((fields == null || !fields.contains(field))
              && (extFields == null || !extFields.contains(field))) {

            if (extFields == null) {
              extFields = Lists.newArrayList(field);
            } else {
              extFields.add(field);
            }
          }
        }
      }
      if (!BeeUtils.isEmpty(otherTable.states)) {
        if (states == null) {
          states = Sets.newHashSet(otherTable.states);
        } else {
          states.addAll(otherTable.states);
        }
        if (!BeeUtils.isEmpty(otherTable.keys)) {
          if (keys == null) {
            keys = Sets.newHashSet(otherTable.keys);
          } else {
            keys.addAll(otherTable.keys);
          }
        }
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
}
