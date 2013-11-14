package com.butent.bee.shared.data;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.SystemBean.SysObject;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.utils.BeeUtils;

import org.junit.Test;

public class TestXmlTable {
  @Test
  public void testRead() {
    String resource = Config.getPath("modules/Commons/" + SysObject.TABLE.getPath() + "/"
        + SysObject.TABLE.getFileName("Users"), true);
    String schemaSource = SysObject.TABLE.getSchemaPath();

    if (!BeeUtils.isEmpty(resource)) {
      XmlTable table = XmlUtils.unmarshal(XmlTable.class, resource, schemaSource);
      System.out.println(XmlUtils.marshal(table, schemaSource));
    }
  }
}
