package com.butent.bee.shared.data;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.SystemBean.SysObject;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.utils.BeeUtils;

import org.junit.Test;

public class TestXmlState {
  @Test
  public void testRead() {
    String resource = Config.getPath(SysObject.STATE.getPath() + "/"
        + SysObject.STATE.getFileName("Visible"), true);
    String schemaSource = SysObject.STATE.getSchemaPath();

    if (!BeeUtils.isEmpty(resource)) {
      XmlState state = XmlUtils.unmarshal(XmlState.class, resource, schemaSource);
      System.out.println(XmlUtils.marshal(state, schemaSource));
    }
  }
}
