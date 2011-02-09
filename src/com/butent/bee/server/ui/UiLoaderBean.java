package com.butent.bee.server.ui;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.BeeService;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.ui.UiLoader;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.Property;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    if ("ANALIZE".equals(formName)) {
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
          "click_proc="
              + BeeService.SERVICE_DB_INFO
              + "\n left=50px\n right=50%\r top=10\r\n height=3\\\n           3%"});

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
      UiRow row = new UiRow();
      row.setId(formName);
      row.setClassName("UiWindow");
      res.add(row);

      SqlSelect ss = new SqlSelect();
      ss.addFields("f", "properties", "top", "left", "width", "height",
          "dock_top", "dock_left", "dock_width", "dock_hght", "dock_right",
          "dock_bott").addFrom("forms", "f").setWhere(
          SqlUtils.equal("f", "form", formName));

      Map<String, String> data = qs.getRowValues(ss);

      String top = data.get("top");
      String left = data.get("left");
      String width = data.get("width");
      String height = data.get("height");
      String dockTop = BeeUtils.ifString(data.get("dock_top"), top);
      String dockLeft = BeeUtils.ifString(data.get("dock_left"), left);
      String dockWidth = data.get("dock_width");
      String dockHeight = data.get("dock_hght");
      String dockRight = BeeUtils.ifString(data.get("dock_right"), dockWidth);
      String dockBottom = BeeUtils.ifString(data.get("dock_bott"), dockHeight);

      StringBuilder props = new StringBuilder();

      if (BeeUtils.isEmpty(dockRight)) {
        props.append("width=" + width + "\n");
      } else {
        props.append("right=" + dockRight + "\n");
      }
      if (BeeUtils.isEmpty(dockBottom)) {
        props.append("height=" + height + "\n");
      } else {
        props.append("bottom=" + dockBottom + "\n");
      }
      props.append("top=" + dockTop + "\n");
      props.append("left=" + dockLeft + "\n");

      String prp = data.get("properties");
      if (!BeeUtils.isEmpty(prp)) {
        props.append(prp + "\n");
      }
      row = new UiRow();
      row.setId("Container");
      row.setClassName("UiWindow");
      row.setParent(formName);
      row.setProperties(props.toString());
      res.add(row);

      ss = new SqlSelect();
      ss.addFields("c", "control", "class", "parent", "caption", "order",
          "parameters", "properties", "top", "left", "width", "height",
          "dock_prnt", "dock_left", "dock_top", "dock_right", "dock_bott",
          "dock_width", "dock_hght").addFrom("controls", "c").setWhere(
          SqlUtils.equal("c", "form", formName));
      
      BeeRowSet brs = qs.getData(ss);
      for (BeeRow cols : brs.getRows()) {
        row = new UiRow();
        row.setId(brs.getString(cols, "control"));
        row.setClassName(getUiClass(brs.getString(cols, "class")));
        row.setParent(brs.getString(cols, "parent"));
        row.setCaption(BeeUtils.ifString(brs.getString(cols, "caption"), "")
            .replaceAll("[\"']", "").replaceFirst("\\\\<", ""));
        row.setOrder(brs.getInt(cols, "order"));

        props = new StringBuilder();
        prp = brs.getString(cols, "parameters");
        if (!BeeUtils.isEmpty(prp)) {
          props.append("parameters=" + prp + "\n");
        }
        prp = brs.getString(cols, "properties");
        if (!BeeUtils.isEmpty(prp)) {
          props.append(prp + "\n");
        }

        top = brs.getString(cols, "top");
        left = brs.getString(cols, "left");
        width = brs.getString(cols, "width");
        height = brs.getString(cols, "height");

        String dock = BeeUtils.ifString(brs.getString(cols, "dock_prnt"), "");

        dockTop = brs.getString(cols, "dock_top") + (dock.contains("t") ? "%" : "");
        dockLeft = brs.getString(cols, "dock_left") + (dock.contains("l") ? "%" : "");
        dockWidth = brs.getString(cols, "dock_width") + (dock.contains("w") ? "%" : "");
        dockHeight = brs.getString(cols, "dock_hght") + (dock.contains("h") ? "%" : "");
        dockRight = BeeUtils.ifString(brs.getString(cols, "dock_right"), dockWidth)
            + (dock.contains("r") ? "%" : "");
        dockBottom = BeeUtils.ifString(brs.getString(cols, "dock_bott"), dockHeight)
            + (dock.contains("b") ? "%" : "");

        if (BeeUtils.isEmpty(dock)) {
          props.append("top=" + top + "\n");
          props.append("left=" + left + "\n");
          props.append("width=" + width + "\n");
          props.append("height=" + height + "\n");
        } else {
          if (BeeUtils.isEmpty(dockRight)) {
            props.append("left=" + BeeUtils.ifString(dockLeft, left) + "\n");
            props.append("width=" + width + "\n");
          } else {
            if (BeeUtils.isEmpty(dockLeft)) {
              if (BeeUtils.isEmpty(dockWidth)) {
                props.append("width=" + width + "\n");
              } else {
                props.append("left=" + left + "\n");
              }
            } else {
              props.append("left=" + dockLeft + "\n");
            }
            props.append("right=" + dockRight + "\n");
          }
          if (BeeUtils.isEmpty(dockBottom)) {
            props.append("top=" + BeeUtils.ifString(dockTop, top) + "\n");
            props.append("height=" + height + "\n");
          } else {
            if (BeeUtils.isEmpty(dockTop)) {
              if (BeeUtils.isEmpty(dockHeight)) {
                props.append("height=" + height + "\n");
              } else {
                props.append("top=" + top + "\n");
              }
            } else {
              props.append("top=" + dockTop + "\n");
            }
            props.append("bottom=" + dockBottom + "\n");
          }
        }
        row.setProperties(props.toString());
        if (BeeUtils.isEmpty(row.getParent())) {
          row.setParent("Container");
        }
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

    Property[][] arr = XmlUtils.getAttributesFromFile(url.getFile(), "menu");
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

      for (Property attr : arr[i]) {
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
    if (BeeUtils.inListSame(cls, "i_button", "c_choice", "c_close", "c_edit",
        "o_butt_gr", "i_butt_gr", "c_butt_gr")) {
      return "UiButton";
    } else if ("i_combo".equals(cls)) {
      return "UiListBox";
    } else if (BeeUtils.inListSame(cls, "i_label", "c_say")) {
      return "UiLabel";
    } else if (BeeUtils.inListSame(cls, "i_field", "c_field", "o_r_sub")) {
      return "UiField";
    } else if ("i_frame".equals(cls)) {
      return "UiTab";
    } else if (BeeUtils.inListSame(cls, "i_page", "c_defer", "o_list")) {
      return "UiWindow";
    } else if (BeeUtils.inListSame(cls, "i_check", "c_check", "c_pazym",
        "c_user")) {
      return "UiCheckBox";
    } else if (BeeUtils.inListSame(cls, "r_grid", "c_grid")) {
      return "UiGrid";
    } else if (BeeUtils.inListSame(cls, "i_memo", "c_memo", "o_r_list",
        "o_f_list")) {
      return "UiTextArea";
    } else if ("i_opt_gr".equals(cls)) {
      return "UiRadioButton";
    }
    return cls;
  }
}
