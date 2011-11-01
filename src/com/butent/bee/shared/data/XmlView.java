package com.butent.bee.shared.data;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Handles views information storage in XML structure.
 */

@XmlRootElement(name = "BeeView", namespace = DataUtils.DEFAULT_NAMESPACE)
public class XmlView {

  @XmlSeeAlso({XmlSimpleColumn.class, XmlJoinColumn.class})
  public static class XmlColumn {
    @XmlAttribute
    public String name;
    @XmlAttribute
    public String expression;
  }

  @XmlRootElement(name = "BeeSimpleColumn", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlSimpleColumn extends XmlColumn {
    @XmlAttribute
    public String locale;
  }

  @XmlRootElement(name = "BeeJoinColumn", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlJoinColumn extends XmlColumn {
    @XmlAttribute
    public String source;
    @XmlAttribute
    public String joinType;
    @XmlElementRef
    public Collection<XmlColumn> columns;
  }

  public static class XmlOrder {
    @XmlAttribute
    public String column;
    @XmlAttribute
    public boolean descending;
  }

  @XmlAttribute
  public String name;
  @XmlAttribute
  public String source;
  @XmlAttribute
  public String filter;

  @XmlAttribute
  public boolean readOnly;

  @XmlElementWrapper(name = "BeeColumns", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElementRef
  public Collection<XmlColumn> columns;

  @XmlElementWrapper(name = "BeeOrder", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElement(name = "OrderBy", namespace = DataUtils.DEFAULT_NAMESPACE)
  public Collection<XmlOrder> orders;
}
