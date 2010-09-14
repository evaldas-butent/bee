package com.butent.bee.egg.server.ui;

import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.ui.UiLoader;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;

@Stateless
public class UiLoaderImpl extends UiLoader {

  @Override
  protected List<UiRow> getFormData(String formName, Object... params) {
    List<UiRow> res = new ArrayList<UiRow>();

    if ("testForm".equals(formName)) {
      List<Object[]> buff = new ArrayList<Object[]>();
      buff.add(new Object[]{formName, "UiPanel", null, null});

      buff.add(new Object[]{"vLayout", "UiVerticalLayout", null, null});
      buff.add(new Object[]{
          "Button1", "UiButton", "",
          "caption=Database Tables;service=" + BeeService.SERVICE_DB_TABLES});
      buff.add(new Object[]{"hLayout", "UiHorizontalLayout", "", null});
      buff.add(new Object[]{"Label1", "UiLabel", "hLayout", "caption=Tekstas 1"});
      buff.add(new Object[]{"Label2", "UiLabel", "Label1", null});
      buff.add(new Object[]{"Field3", "UiField", "hLayout", "caption=Laukas 3"});
      buff.add(new Object[]{"Field4", "UiField", "vLayout", "caption=Laukas 4"});
      buff.add(new Object[]{"Label3", "UiLabel", "vLayout", "caption=Tekstas 3"});
      buff.add(new Object[]{
          "LookingForParents", "UiLabel", "unknownParent", null});
      buff.add(new Object[]{"Label1", "UiLabel", "vLayout", null});
      buff.add(new Object[]{"Unknown", "UiUnknown", "hLayout", null});

      for (Object[] cols : buff) {
        UiRow row = new UiRow();
        row.setId((String) cols[0]);
        row.setClass((String) cols[1]);
        row.setParent((String) cols[2]);
        row.setProperties((String) cols[3]);
        res.add(row);
      }
    }
    return res;
  }

}
