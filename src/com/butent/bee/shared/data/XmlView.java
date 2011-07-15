package com.butent.bee.shared.data;

import com.butent.bee.server.Config;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "BeeView", namespace = Config.DEFAULT_NAMESPACE)
public class XmlView {

  @XmlRootElement(name = "BeeColumn", namespace = Config.DEFAULT_NAMESPACE)
  public static class XmlColumn {
    @XmlAttribute
    public String name;
    @XmlAttribute
    public String expression;
    @XmlAttribute
    public String locale;
  }

  @XmlRootElement(name = "OrderBy", namespace = Config.DEFAULT_NAMESPACE)
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
  @XmlElementWrapper(name = "BeeColumns", namespace = Config.DEFAULT_NAMESPACE)
  public Collection<XmlColumn> columns;

  @XmlElementRef
  @XmlElementWrapper(name = "BeeOrder", namespace = Config.DEFAULT_NAMESPACE)
  public Collection<XmlOrder> orders;
}
