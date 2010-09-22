package com.butent.bee.egg.server.ui;

import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.sql.QueryBuilder;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.ui.UiLoader;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;
import com.butent.bee.egg.shared.utils.StringProp;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class UiLoaderBean extends UiLoader {

  @EJB
  QueryServiceBean qs;

  @Override
  protected List<UiRow> getFormData(String formName, Object... params) {
    List<UiRow> res = new ArrayList<UiRow>();

    if ("VALIUTOS".equals(formName)) {
      List<Object[]> buff = new ArrayList<Object[]>();
      buff.add(new Object[]{formName, "UiWindow", null, null, null});

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
    } else if ("ANALIZE".equals(formName)) {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      List<Object[]> buff = new ArrayList<Object[]>();
      buff.add(new Object[]{formName, "UiWindow", null, null, null});

      buff.add(new Object[]{
          "Button1",
          "UiButton",
          null,
          "Database Info",
          "service=" + BeeService.SERVICE_DB_INFO
              + "\n left=50px\n right=50%\r top=10\r\n height=33%"});

      for (Object[] cols : buff) {
        UiRow row = new UiRow();
        row.setId((String) cols[0]);
        row.setClassName((String) cols[1]);
        row.setParent((String) cols[2]);
        row.setCaption((String) cols[3]);
        row.setProperties((String) cols[4]);
        res.add(row);
      }
    } else {
      QueryBuilder qb = new QueryBuilder();
      qb.addFields("c", "control", "class", "parent", "caption", "order").addFrom(
          "x_controls", "c").setWhere(
          SqlUtils.equal("c", "form", "'" + formName + "'"));
      List<Object[]> data = qs.getQueryData(qb);

      UiRow row = new UiRow();
      row.setId(formName);
      row.setClassName("UiWindow");
      res.add(row);

      for (Object[] cols : data) {
        row = new UiRow();
        row.setId((String) cols[0]);
        row.setClassName((String) cols[1]);
        row.setParent((String) cols[2]);
        row.setCaption((String) cols[3]);
        row.setOrder((Integer) cols[4]);
        res.add(row);
      }
    }
    return res;
  }

  @Override
  protected List<UiRow> getMenuData(String menuName, Object... params) {
    String rootLayout = (String) params[0];
    String itemLayout = (String) params[1];
    String resource = (String) params[2];

    URL url = Thread.currentThread().getContextClassLoader().getResource(
        resource);
    if (url == null) {
      LogUtils.warning(logger, "Menu resource not found");
      return null;
    }

    StringProp[][] arr = XmlUtils.getAttributesFromFile(url.getFile(), "menu");
    if (arr == null) {
      return null;
    }

    int r = arr.length;
    List<UiRow> res = new ArrayList<UiRow>(r);

    UiRow row = new UiRow();
    row.setId(menuName);
    row.setClassName(rootLayout);
    res.add(row);

    for (int i = 0; i < r; i++) {
      if (arr[i] == null) {
        LogUtils.warning(logger, "menu item", i, "not initialized");
        continue;
      }

      row = new UiRow();

      StringBuilder props = new StringBuilder();

      for (StringProp attr : arr[i]) {
        String name = attr.getName();
        String value = attr.getValue();

        if (!BeeUtils.allNotEmpty(name, value)) {
          continue;
        }

        if (BeeUtils.same(name, "id")) {
          row.setId(value);
        } else if (BeeUtils.same(name, "parent")) {
          row.setParent(value);
        } else if (BeeUtils.same(name, "text")) {
          row.setCaption(value);
        } else if (BeeUtils.same(name, "order")) {
          row.setOrder(BeeUtils.toInt(value));
        } else {
          props.append(props.length() > 0 ? "\n" : "").append(name).append("=").append(
              value);
        }
      }

      if (BeeUtils.isEmpty(row.getClassName())) {
        row.setClassName(itemLayout);
      }
      row.setProperties(props.toString());
      res.add(row);
    }
    return res;
  }
}
