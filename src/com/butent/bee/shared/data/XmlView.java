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

@XmlRootElement(name = "View", namespace = DataUtils.DEFAULT_NAMESPACE)
public class XmlView {

  @XmlSeeAlso({XmlSimpleColumn.class, XmlHiddenColumn.class, XmlAggregateColumn.class,
      XmlSimpleJoin.class, XmlAggregateJoin.class, XmlExternalJoin.class})
  public abstract static class XmlColumn {
    @XmlAttribute
    public String name;
  }

  @XmlRootElement(name = "SimpleColumn", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlSimpleColumn extends XmlColumn {
    @XmlAttribute
    public String alias;
    @XmlAttribute
    public String locale;
    @XmlElementRef
    public XmlExpression expr;
  }

  @XmlRootElement(name = "HiddenColumn", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlHiddenColumn extends XmlSimpleColumn {
  }

  @XmlRootElement(name = "AggregateColumn", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlAggregateColumn extends XmlSimpleColumn {
    @XmlAttribute
    public String aggregate;
  }

  @XmlRootElement(name = "SimpleJoin", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlSimpleJoin extends XmlColumn {
    @XmlAttribute
    public String joinType;
    @XmlElementRef
    public Collection<XmlColumn> columns;
  }

  @XmlRootElement(name = "ExternalJoin", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlExternalJoin extends XmlSimpleJoin {
    @XmlAttribute
    public String source;
  }

  @XmlRootElement(name = "AggregateJoin", namespace = DataUtils.DEFAULT_NAMESPACE)
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

  @XmlElementWrapper(name = "Columns", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElementRef
  public Collection<XmlColumn> columns;

  @XmlElementWrapper(name = "Order", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElementRef
  public Collection<XmlOrder> orders;
}
