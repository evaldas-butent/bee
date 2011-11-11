package com.butent.bee.shared.data;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Handles views information storage in XML structure.
 */

@XmlRootElement(name = "BeeView", namespace = DataUtils.DEFAULT_NAMESPACE)
public class XmlView {

  @XmlSeeAlso({XmlSimpleColumn.class, XmlHiddenColumn.class, XmlAggregateColumn.class,
      XmlSimpleJoin.class, XmlAggregateJoin.class, XmlExternalJoin.class})
  public static class XmlColumn {
    @XmlAttribute
    public String expression;
  }

  @XmlRootElement(name = "BeeSimpleColumn", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlSimpleColumn extends XmlColumn {
    @XmlAttribute
    public String name;
    @XmlAttribute
    public String locale;
  }

  @XmlRootElement(name = "BeeHiddenColumn", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlHiddenColumn extends XmlSimpleColumn {
  }

  @XmlRootElement(name = "BeeAggregateColumn", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlAggregateColumn extends XmlSimpleColumn {
    @XmlAttribute
    public String aggregate;
  }

  @XmlRootElement(name = "BeeSimpleJoin", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlSimpleJoin extends XmlColumn {
    @XmlAttribute
    public String joinType;
    @XmlElementRef
    public Collection<XmlColumn> columns;
  }

  @XmlRootElement(name = "BeeExternalJoin", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlExternalJoin extends XmlSimpleJoin {
    @XmlAttribute
    public String source;
  }

  @XmlRootElement(name = "BeeAggregateJoin", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlAggregateJoin extends XmlSimpleJoin {
  }

  @XmlRootElement(name = "OrderBy", namespace = DataUtils.DEFAULT_NAMESPACE)
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
  @XmlElementRef
  public Collection<XmlOrder> orders;
}
