package com.butent.bee.shared.data;

import com.butent.bee.server.Config;
import com.butent.bee.server.ui.XmlSqlDesigner;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.utils.BeeUtils;

import org.junit.Test;

public class TestXmlSqlDesigner {
  @Test
  public void testRead() {
    String resource = Config.getPath("SqlDesigner.xml", true);

    if (!BeeUtils.isEmpty(resource)) {
      XmlSqlDesigner view = XmlUtils.unmarshal(XmlSqlDesigner.class, resource, null);
      System.out.println(XmlUtils.marshal(view, null));
    }
  }
}
