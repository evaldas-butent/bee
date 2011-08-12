package com.butent.bee.shared.data;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Handles views information storage in XML structure.
 */

@XmlRootElement(name = "BeeView", namespace = DataUtils.DEFAULT_NAMESPACE)
public class XmlView {

  /**
   * Handles data column information storage in XML structure.
   */

  public static class XmlColumn {
    @XmlAttribute
    public String name;
    @XmlAttribute
    public String expression;
    @XmlAttribute
    public String locale;
  }

  /**
   * Handles data ordering information storage in XML structure.
   */

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

  @XmlElementWrapper(name = "BeeColumns", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElement(name = "BeeColumn", namespace = DataUtils.DEFAULT_NAMESPACE)
  public Collection<XmlColumn> columns;

  @XmlElementWrapper(name = "BeeOrder", namespace = DataUtils.DEFAULT_NAMESPACE)
  @XmlElement(name = "OrderBy", namespace = DataUtils.DEFAULT_NAMESPACE)
  public Collection<XmlOrder> orders;
}
