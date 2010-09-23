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
      qb.addFields("c", "control", "class", "parent", "caption", "order",
          "parameters", "top", "left", "width", "height", "dock_prnt",
          "dock_left", "dock_top", "dock_right", "dock_bott", "dock_width",
          "dock_hght").addFrom("x_controls", "c").setWhere(
          SqlUtils.equal("c", "form", "'" + formName + "'"));
      List<Object[]> data = qs.getQueryData(qb);

      UiRow row = new UiRow();
      row.setId(formName);
      row.setClassName("UiWindow");
      res.add(row);

      for (Object[] cols : data) {
        row = new UiRow();
        row.setId((String) cols[0]);
        row.setClassName(getUiClass((String) cols[1]));
        row.setParent((String) cols[2]);
        row.setCaption(BeeUtils.ifString(cols[3], "").replaceFirst("^[\"']", "").replaceFirst(
            "[\"']$", "").replaceFirst("\\\\<", ""));
        row.setOrder((Integer) cols[4]);

        StringBuilder props = new StringBuilder();
        if (!BeeUtils.isEmpty(cols[5])) {
          props.append("parameters=" + cols[5] + "\n");
        }

        String top = BeeUtils.isEmpty(cols[6]) ? ""
            : BeeUtils.transform(cols[6]);
        String left = BeeUtils.isEmpty(cols[7]) ? ""
            : BeeUtils.transform(cols[7]);
        String width = BeeUtils.isEmpty(cols[8]) ? ""
            : BeeUtils.transform(cols[8]);
        String height = BeeUtils.isEmpty(cols[9]) ? ""
            : BeeUtils.transform(cols[9]);
        String dock = BeeUtils.ifString(cols[10], "");
        String dockLeft = BeeUtils.ifString(cols[11], left);
        String dockTop = BeeUtils.ifString(cols[12], top);
        String dockRight = BeeUtils.ifString(cols[13], "");
        String dockBottom = BeeUtils.ifString(cols[14], "");
        String dockWidth = BeeUtils.ifString(cols[15], "");
        String dockHeight = BeeUtils.ifString(cols[16], "");

        if (BeeUtils.isEmpty(dock)) {
          props.append("top=" + top + "\n");
          props.append("left=" + left + "\n");
          props.append("width=" + width + "\n");
          props.append("height=" + height + "\n");
        } else {
          if (BeeUtils.isEmpty(dockRight)) {
            props.append("left=" + dockLeft + (dock.contains("l") ? "%" : "")
                + "\n");

            if (BeeUtils.isEmpty(dockWidth)) {
              props.append("width=" + width + "\n");
            } else {
              props.append("right=" + dockWidth
                  + (dock.contains("w") ? "%" : "") + "\n");
            }
          } else {
            props.append("width=" + width + "\n");
            props.append("right=" + dockRight + (dock.contains("r") ? "%" : "")
                + "\n");
          }
          if (BeeUtils.isEmpty(dockBottom)) {
            props.append("top=" + dockTop + (dock.contains("t") ? "%" : "")
                + "\n");

            if (BeeUtils.isEmpty(dockHeight)) {
              props.append("height=" + height + "\n");
            } else {
              props.append("bottom=" + dockHeight
                  + (dock.contains("h") ? "%" : "") + "\n");
            }
          } else {
            props.append("height=" + height + "\n");
            props.append("bottom=" + dockBottom
                + (dock.contains("b") ? "%" : "") + "\n");
          }
        }
        row.setProperties(props.toString());
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

  private String getUiClass(String cls) {
    if (BeeUtils.inListSame(cls, "i_button", "c_close", "c_edit")) {
      return "UiButton";
    } else if ("i_combo".equals(cls)) {
      return "UiListBox";
    } else if (BeeUtils.inListSame(cls, "i_label", "c_say")) {
      return "UiLabel";
    } else if ("i_field".equals(cls)) {
      return "UiField";
    } else if ("i_frame".equals(cls)) {
      return "UiTab";
    } else if (BeeUtils.inListSame(cls, "i_page", "c_defer")) {
      return "UiWindow";
    } else if (BeeUtils.inListSame(cls, "i_check", "c_check")) {
      return "UiCheckBox";
    } else if (BeeUtils.inListSame(cls, "r_grid", "c_grid")) {
      return "UiTextArea";
    } else if (BeeUtils.inListSame(cls, "i_memo", "c_memo")) {
      return "UiTextArea";
    }
    return cls;
  }
}
