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
      buff.add(new Object[]{formName, "UiPanel", null, null, null});

      buff.add(new Object[]{"vLayout", "UiVerticalLayout", null, null, null});
      buff.add(new Object[]{
          "Button1", "UiButton", "", "Database Tables",
          "service=" + BeeService.SERVICE_DB_TABLES});
      buff.add(new Object[]{"hLayout", "UiHorizontalLayout", "", null, null});
      buff.add(new Object[]{"Label1", "UiLabel", "hLayout", "Tekstas 1", null});
      buff.add(new Object[]{"Label2", "UiLabel", "Label1", null, null});
      buff.add(new Object[]{"Field3", "UiField", "hLayout", "Laukas 3", null});
      buff.add(new Object[]{"Field4", "UiField", "vLayout", "Laukas 4", null});
      buff.add(new Object[]{"Label3", "UiLabel", "vLayout", "Tekstas 3", null});
      buff.add(new Object[]{
          "LookingForParents", "UiLabel", "unknownParent", null, null});
      buff.add(new Object[]{"Label1", "UiLabel", "vLayout", null, null});
      buff.add(new Object[]{"Unknown", "UiUnknown", "hLayout", null, null});

      for (Object[] cols : buff) {
        UiRow row = new UiRow();
        row.setId((String) cols[0]);
        row.setClassName((String) cols[1]);
        row.setParent((String) cols[2]);
        row.setCaption((String) cols[3]);
        row.setProperties((String) cols[4]);
        res.add(row);
      }
    } else if ("slowForm".equals(formName)) {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      List<Object[]> buff = new ArrayList<Object[]>();
      buff.add(new Object[]{formName, "UiPanel", null, null, null});

      buff.add(new Object[]{
          "Button1", "UiButton", null, "Database Info",
          "service=" + BeeService.SERVICE_DB_INFO});

      for (Object[] cols : buff) {
        UiRow row = new UiRow();
        row.setId((String) cols[0]);
        row.setClassName((String) cols[1]);
        row.setParent((String) cols[2]);
        row.setCaption((String) cols[3]);
        row.setProperties((String) cols[4]);
        res.add(row);
      }
    }
    return res;
  }

}
