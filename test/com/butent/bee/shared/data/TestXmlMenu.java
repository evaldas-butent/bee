package com.butent.bee.shared.data;

import com.butent.bee.server.Config;
import com.butent.bee.server.ui.UiHolderBean.UiObject;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.menu.Menu;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import org.junit.Test;

public class TestXmlMenu {
  @Test
  public void testRead() {
    String resource = Config.getPath("modules/Commons/" + UiObject.MENU.getPath() + "/"
        + UiObject.MENU.getFileName("References"), true);
    String schemaSource = UiObject.MENU.getSchemaPath();

    if (!BeeUtils.isEmpty(resource)) {
      Menu menu = XmlUtils.unmarshal(Menu.class, resource, schemaSource);
      System.out.println(XmlUtils.marshal(menu, schemaSource));
      System.out.println(XmlUtils.marshal(Menu.restore(Codec.beeSerialize(menu)), schemaSource));
    }
  }
}
