package com.butent.bee.egg.server.ui;

import com.butent.bee.egg.server.data.QueryServiceBean;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.data.BeeRowSet.BeeRow;
import com.butent.bee.egg.shared.sql.SqlSelect;
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

      BeeRow data = qs.getSingleRow(ss);

      String top = data.getString("top");
      String left = data.getString("left");
      String width = data.getString("width");
      String height = data.getString("height");
      String dockTop = BeeUtils.ifString(data.getString("dock_top"), top);
      String dockLeft = BeeUtils.ifString(data.getString("dock_left"), left);
      String dockWidth = data.getString("dock_width");
      String dockHeight = data.getString("dock_hght");
      String dockRight = BeeUtils.ifString(data.getString("dock_right"),
          dockWidth);
      String dockBottom = BeeUtils.ifString(data.getString("dock_bott"),
          dockHeight);

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

      String prp = data.getString("properties");
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

      for (BeeRow cols : qs.getData(ss).getRows()) {
        row = new UiRow();
        row.setId(cols.getString("control"));
        row.setClassName(getUiClass(cols.getString("class")));
        row.setParent(cols.getString("parent"));
        row.setCaption(BeeUtils.ifString(cols.getString("caption"), "")
            .replaceAll("[\"']", "").replaceFirst("\\\\<", ""));
        row.setOrder(cols.getInt("order"));

        props = new StringBuilder();
        prp = cols.getString("parameters");
        if (!BeeUtils.isEmpty(prp)) {
          props.append("parameters=" + prp + "\n");
        }
        prp = cols.getString("properties");
        if (!BeeUtils.isEmpty(prp)) {
          props.append(prp + "\n");
        }

        top = cols.getString("top");
        left = cols.getString("left");
        width = cols.getString("width");
        height = cols.getString("height");

        String dock = BeeUtils.ifString(cols.getString("dock_prnt"), "");

        dockTop = cols.getString("dock_top") + (dock.contains("t") ? "%" : "");
        dockLeft = cols.getString("dock_left")
            + (dock.contains("l") ? "%" : "");
        dockWidth = cols.getString("dock_width")
            + (dock.contains("w") ? "%" : "");
        dockHeight = cols.getString("dock_hght")
            + (dock.contains("h") ? "%" : "");
        dockRight = BeeUtils.ifString(cols.getString("dock_right"), dockWidth)
            + (dock.contains("r") ? "%" : "");
        dockBottom = BeeUtils.ifString(cols.getString("dock_bott"), dockHeight)
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
