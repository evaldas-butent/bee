package com.butent.bee.shared.data;

import com.butent.bee.server.Config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "BeeState", namespace = Config.DEFAULT_NAMESPACE)
public class XmlState {

  @XmlAttribute
  public String name;
  @XmlAttribute
  public String mode;
  @XmlAttribute
  public boolean checked;
}
