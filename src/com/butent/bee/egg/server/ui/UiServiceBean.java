package com.butent.bee.egg.server.ui;

import com.butent.bee.egg.server.Assert;
import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.data.BeeColumn;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlSelect;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.ui.UiComponent;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class UiServiceBean {
  private static Logger logger = Logger.getLogger(UiServiceBean.class.getName());

  @EJB
  UiHolderBean holder;
  @EJB
  QueryServiceBean qs;

  public void doService(String svc, RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notEmpty(svc);
    Assert.notNull(buff);

    qs.switchEngine(reqInfo.getDsn());

    if (svc.equals("rpc_ui_form")) {
      formInfo(reqInfo, buff);
    } else if (svc.equals("rpc_ui_form_list")) {
      formList(buff);
    } else if (svc.equals("rpc_ui_menu")) {
      menuInfo(reqInfo, buff);
    } else if (svc.equals("rpc_ui_grid")) {
      gridInfo(reqInfo, buff);
    } else if (svc.equals("rpc_ui_rebuild")) {
      rebuildData(buff);
    } else {
      String msg = BeeUtils.concat(1, svc, "loader service not recognized");
      logger.warning(msg);
      buff.add(msg);
    }
  }

  private void formInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String fName = getXmlField(reqInfo, buff, "form_name");

    UiComponent form = holder.getForm(fName);

    if (BeeUtils.isEmpty(form)) {
      String msg = "Form name not recognized: " + fName;
      logger.warning(msg);
      buff.add(msg);
    } else {
      buff.add(form.serialize());
    }
  }

  private void formList(ResponseBuffer buff) {
    SqlSelect ss = new SqlSelect();
    ss.addFields("f", "form").addFrom("forms", "f").addOrder("f", "form");

    List<Object[]> res = qs.getData(ss);
    if (res == null) {
      return;
    }

    for (String alias : ss.getFieldAliases()) {
      buff.addColumn(new BeeColumn(alias));
    }
    for (Object[] row : res) {
      for (Object cell : row) {
        buff.add(cell);
      }
    }
  }

  private String getXmlField(RequestInfo reqInfo, ResponseBuffer buff,
      String fieldName) {
    String xml = reqInfo.getContent();
    if (BeeUtils.isEmpty(xml)) {
      buff.add("Request data not found");
      return null;
    }

    Map<String, String> fields = XmlUtils.getElements(xml);
    if (BeeUtils.isEmpty(fields)) {
      buff.addLine("No elements with text found in", xml);
      return null;
    }
    return fields.get(fieldName);
  }

  private void gridInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String gName = getXmlField(reqInfo, buff, "grid_name");
    String grd = gName;

    SqlSelect ss = new SqlSelect();
    ss.addFields("g", "properties").addFrom("grids", "g").setWhere(
        SqlUtils.equal("g", "table", gName));

    List<Object[]> data = qs.getData(ss);

    if (!BeeUtils.isEmpty(data)) {
      String x = (String) data.get(0)[0];

      if (!BeeUtils.isEmpty(x) && x.contains("parent_table")) {
        grd = x.replaceFirst(
            "^((?s).)*parent_table\\s*=\\s*[\\[\"'](.+)[\\]\"']((?s).)*$", "$2");
      }

      ss = new SqlSelect();
      ss.addFields("c", "field", "caption").addFrom("columns", "c").setWhere(
          SqlUtils.equal("c", "table", grd)).addOrder("c", "order");

      List<Object[]> cols = qs.getData(ss);

      if (!BeeUtils.isEmpty(cols)) {
        for (Object[] col : cols) {
          buff.addColumn(new BeeColumn(
              ((String) col[1]).replaceAll("['\"]", "")));
        }
        for (int i = 0; i < 20; i++) {
          for (int j = 0; j < cols.size(); j++) {
            buff.add(j == 0 ? i + 1 : BeeConst.STRING_EMPTY);
          }
        }
        return;
      }
    }
    String msg = "Grid name not recognized: " + grd;
    logger.warning(msg);
    buff.add(msg);
  }

  private void menuInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String mName = getXmlField(reqInfo, buff, "menu_name");
    String lRoot = getXmlField(reqInfo, buff, "root_layout");
    String lItem = getXmlField(reqInfo, buff, "item_layout");

    UiComponent menu = holder.getMenu(mName, lRoot, lItem,
        "/com/butent/bee/egg/server/menu.xml");

    if (BeeUtils.isEmpty(menu)) {
      String msg = "Error initializing menu: " + mName;
      logger.warning(msg);
      buff.add(msg);
    } else {
      buff.add(menu.serialize());
    }
  }

  private void rebuildData(ResponseBuffer buff) {
    String tbl = "fw_tables";
    buff.add("result: " + qs.processSql("drop table " + tbl));

    buff.add("result: "
        + qs.processSql("create table " + tbl
            + " (table_name varchar(30) not null unique"
            + ", last_id bigint not null"
            + ", version int not null, id bigint primary key)"));

    SqlInsert si = new SqlInsert(tbl);
    si.addField("table_name", "fw_tables").addField("last_id", 1).addField(
        "version", 1).addField("id", 1);
    buff.add("result: " + qs.processUpdate(si));

    si = new SqlInsert(tbl);
    si.addField("table_name", "cou`ntr]ie[s").addField("last_id", 0);
    buff.add("result: " + qs.insertData(si));

    si = new SqlInsert(tbl);
    si.addField("table_name", "cities").addField("last_id", 0);
    buff.add("result: " + qs.insertData(si));
  }
}
