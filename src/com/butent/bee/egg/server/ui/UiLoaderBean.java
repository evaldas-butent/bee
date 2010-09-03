package com.butent.bee.egg.server.ui;

import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;

import com.butent.bee.egg.server.Assert;
import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.http.ResponseBuffer;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.BeeColumn;
import com.butent.bee.egg.shared.utils.BeeUtils;

@Stateless
public class UiLoaderBean {
  private static Logger logger = Logger.getLogger(UiLoaderBean.class.getName());

  public void doService(String svc, RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notEmpty(svc);
    Assert.notNull(buff);

    if (svc.equals("rpc_ui_form")) {
      formInfo(reqInfo, buff);
    } else if (svc.equals("rpc_ui_form_list")) {
      formList(reqInfo, buff);
    } else if (svc.equals("rpc_ui_grid")) {
      gridInfo(reqInfo, buff);
    } else {
      String msg = BeeUtils.concat(1, svc, "loader service not recognized");
      logger.warning(msg);
      buff.add(msg);
    }
  }

  private void formList(RequestInfo reqInfo, ResponseBuffer buff) {
    buff.addColumn(new BeeColumn("Form"));
    buff.add("testForm");
    buff.add("unavailableForm");
  }

  private void formInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String fName = getXmlField(reqInfo, buff, "form_name");

    if ("testForm".equals(fName)) {
      buff.addColumn(new BeeColumn("Object"));
      buff.addColumn(new BeeColumn("Class"));
      buff.addColumn(new BeeColumn("Parent"));
      buff.addColumn(new BeeColumn("Properties"));
      buff.add(new Object[] { fName, "UiPanel", null, null });

      buff.add(new Object[] { "vLayout", "UiVerticalLayout", null, null });
      buff.add(new Object[] { "Button1", "UiButton", "", "caption=Mygtukas" });
      buff.add(new Object[] { "hLayout", "UiHorizontalLayout", "", null });
      buff.add(new Object[] { "Label1", "UiLabel", "hLayout",
          "caption=Tekstas 1" });
      buff.add(new Object[] { "Label2", "UiLabel", "Label1", null });
      buff.add(new Object[] { "Field3", "UiField", "hLayout",
          "caption=Laukas 3" });
      buff.add(new Object[] { "Field4", "UiField", "vLayout",
          "caption=Laukas 4" });
      buff.add(new Object[] { "Label3", "UiLabel", "vLayout",
          "caption=Tekstas 3" });
      buff.add(new Object[] { "LookingForParents", "UiLabel", "unknownParent",
          null });
      buff.add(new Object[] { "Label1", "UiLabel", "vLayout", null });
      buff.add(new Object[] { "Unknown", "UiUnknown", "hLayout", null });
    } else {
      String msg = "Form name not recognized: " + fName;
      logger.warning(msg);
      buff.add(msg);
    }
  }

  private void gridInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    // TODO Auto-generated method stub
  }

  private String getXmlField(RequestInfo reqInfo, ResponseBuffer buff,
      String fieldName) {
    String xml = reqInfo.getContent();
    if (BeeUtils.isEmpty(xml)) {
      buff.add("Request data not found");
      return null;
    }

    Map<String, String> fields = XmlUtils.getText(xml);
    if (BeeUtils.isEmpty(fields)) {
      buff.addLine("No elements with text found in", xml);
      return null;
    }
    return fields.get(fieldName);
  }
}
