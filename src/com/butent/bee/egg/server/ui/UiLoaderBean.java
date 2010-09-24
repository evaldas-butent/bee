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

      QueryBuilder qb = new QueryBuilder();
      qb.addFields("f", "properties", "top", "left", "width", "height",
          "dock_top", "dock_left", "dock_width", "dock_hght", "dock_right",
          "dock_bott").addFrom("x_forms", "f").setWhere(
          SqlUtils.equal("f", "form", "'" + formName + "'"));

      List<Object[]> data = qs.getQueryData(qb);
      Object[] col = data.get(0);

      String top = BeeUtils.isEmpty(col[1]) ? "" : BeeUtils.transform(col[1]);
      String left = BeeUtils.isEmpty(col[2]) ? "" : BeeUtils.transform(col[2]);
      String width = BeeUtils.isEmpty(col[3]) ? "" : BeeUtils.transform(col[3]);
      String height = BeeUtils.isEmpty(col[4]) ? ""
          : BeeUtils.transform(col[4]);
      String dockTop = BeeUtils.ifString(col[5], top);
      String dockLeft = BeeUtils.ifString(col[6], left);
      String dockWidth = BeeUtils.transform(col[7]);
      String dockHeight = BeeUtils.transform(col[8]);
      String dockRight = BeeUtils.ifString(col[9], dockWidth);
      String dockBottom = BeeUtils.ifString(col[10], dockHeight);

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

      if (!BeeUtils.isEmpty(col[0])) {
        props.append(col[0] + "\n");
      }
      row = new UiRow();
      row.setId("Container");
      row.setClassName("UiWindow");
      row.setParent(formName);
      row.setProperties(props.toString());
      res.add(row);

      qb = new QueryBuilder();
      qb.addFields("c", "control", "class", "parent", "caption", "order",
          "parameters", "properties", "top", "left", "width", "height",
          "dock_prnt", "dock_left", "dock_top", "dock_right", "dock_bott",
          "dock_width", "dock_hght").addFrom("x_controls", "c").setWhere(
          SqlUtils.equal("c", "form", "'" + formName + "'"));

      data = qs.getQueryData(qb);

      for (Object[] cols : data) {
        row = new UiRow();
        row.setId((String) cols[0]);
        row.setClassName(getUiClass((String) cols[1]));
        row.setParent((String) cols[2]);
        row.setCaption(BeeUtils.ifString(cols[3], "").replaceFirst("^[\"']", "").replaceFirst(
            "[\"']$", "").replaceFirst("\\\\<", ""));
        row.setOrder((Integer) cols[4]);

        props = new StringBuilder();
        if (!BeeUtils.isEmpty(cols[5])) {
          props.append("parameters=" + cols[5] + "\n");
        }
        if (!BeeUtils.isEmpty(cols[6])) {
          props.append(cols[6] + "\n");
        }

        top = BeeUtils.isEmpty(cols[7]) ? "" : BeeUtils.transform(cols[7]);
        left = BeeUtils.isEmpty(cols[8]) ? "" : BeeUtils.transform(cols[8]);
        width = BeeUtils.isEmpty(cols[9]) ? "" : BeeUtils.transform(cols[9]);
        height = BeeUtils.isEmpty(cols[10]) ? "" : BeeUtils.transform(cols[10]);

        String dock = BeeUtils.ifString(cols[11], "");

        dockTop = BeeUtils.isEmpty(cols[13]) ? ""
            : BeeUtils.transform(cols[13]) + (dock.contains("t") ? "%" : "");
        dockLeft = BeeUtils.isEmpty(cols[12]) ? ""
            : BeeUtils.transform(cols[12]) + (dock.contains("l") ? "%" : "");
        dockWidth = BeeUtils.isEmpty(cols[16]) ? ""
            : BeeUtils.transform(cols[16]) + (dock.contains("w") ? "%" : "");
        dockHeight = BeeUtils.isEmpty(cols[17]) ? ""
            : BeeUtils.transform(cols[17]) + (dock.contains("h") ? "%" : "");
        dockRight = BeeUtils.isEmpty(cols[14]) ? dockWidth
            : BeeUtils.transform(cols[14]) + (dock.contains("r") ? "%" : "");
        dockBottom = BeeUtils.isEmpty(cols[15]) ? dockHeight
            : BeeUtils.transform(cols[15]) + (dock.contains("b") ? "%" : "");

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
        "o_butt_gr", "i_butt_gr")) {
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
