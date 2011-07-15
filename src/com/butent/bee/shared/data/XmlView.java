package com.butent.bee.shared.data;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "BeeView", namespace = DataUtils.DEFAULT_NAMESPACE)
public class XmlView {

  @XmlRootElement(name = "BeeColumn", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlColumn {
    @XmlAttribute
    public String name;
    @XmlAttribute
    public String expression;
    @XmlAttribute
    public String locale;
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
  public boolean readOnly;

  @XmlElementRef
  @XmlElementWrapper(name = "BeeColumns", namespace = DataUtils.DEFAULT_NAMESPACE)
  public Collection<XmlColumn> columns;

  @XmlElementRef
  @XmlElementWrapper(name = "BeeOrder", namespace = DataUtils.DEFAULT_NAMESPACE)
  public Collection<XmlOrder> orders;
}
