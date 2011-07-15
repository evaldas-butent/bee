package com.butent.bee.shared.data;

import com.butent.bee.server.Config;
import com.butent.bee.server.sql.SqlConstants.SqlDataType;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "BeeTable", namespace = Config.DEFAULT_NAMESPACE)
public class XmlTable {

  @XmlRootElement(name = "BeeField", namespace = Config.DEFAULT_NAMESPACE)
  public static class XmlField {
    @XmlAttribute
    public String name;
    @XmlAttribute
    public SqlDataType type;
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
  }

  @XmlRootElement(name = "BeeKey", namespace = Config.DEFAULT_NAMESPACE)
  public static class XmlKey {
    @XmlAttribute
    public boolean unique;
    @XmlElement(name = "KeyField", namespace = Config.DEFAULT_NAMESPACE)
    public Collection<String> fields;
  }

  @XmlAttribute
  public String name;
  @XmlAttribute
  public String idName;
  @XmlAttribute
  public String versionName;

  @XmlElementRef
  @XmlElementWrapper(name = "BeeFields", namespace = Config.DEFAULT_NAMESPACE)
  public Collection<XmlField> fields;

  @XmlElementRef
  @XmlElementWrapper(name = "BeeExtended", namespace = Config.DEFAULT_NAMESPACE)
  public Collection<XmlField> extFields;

  @XmlElement(name = "BeeState", namespace = Config.DEFAULT_NAMESPACE)
  @XmlElementWrapper(name = "BeeStates", namespace = Config.DEFAULT_NAMESPACE)
  public Collection<String> states;

  @XmlElementRef
  @XmlElementWrapper(name = "BeeKeys", namespace = Config.DEFAULT_NAMESPACE)
  public Collection<XmlKey> keys;
}
