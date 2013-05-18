package com.butent.bee.shared.menu;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.utils.Codec;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Item", namespace = DataUtils.MENU_NAMESPACE)
public class MenuItem extends Menu {
  @XmlAttribute
  private String service;
  @XmlAttribute
  private String parameters;

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 2);
    service = arr[0];
    parameters = arr[1];
  }

  public String getParameters() {
    return parameters;
  }

  public String getService() {
    return service;
  }
  
  @Override
  public int getSize() {
    return 1;
  }

  @Override
  public String serialize() {
    return super.serialize(new Object[] {service, parameters});
  }
}
