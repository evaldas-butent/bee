package com.butent.bee.client.ui;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.tree.BeeTree;
import com.butent.bee.client.tree.BeeTreeItem;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeService;
import com.butent.bee.shared.BeeStage;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

class RowSetService extends CompositeService {

  private enum Stages {
    REQUEST_TABLE_LIST, CHOOSE_TABLE, REQUEST_TABLE, LOAD_TABLE, SHOW_TABLE,
    INSERT, SAVE, CANCEL,
    FILTER_STATES, CHOOSE_STATES, EDIT_STATE, CHOOSE_STATE, REQUEST_STATETABLE
  }

  private String tbl = "table_name";
  private String limit = "table_limit";
  private String offset = "table_offset";
  private String stt = "table_states";

  private Stages stage = null;
  private BeeRowSet rs;

  protected RowSetService(String... serviceId) {
    super(serviceId);
    stage = Stages.REQUEST_TABLE_LIST;
  }

  @Override
  protected CompositeService create(String svcId) {
    return new RowSetService(self(), svcId);
  }

  @Override
  protected boolean doStage(Object... params) {
    if (params.length > 1 && params[1] instanceof String
        && !BeeUtils.same((String) params[1], "dummy_stage")) {
      stage = Stages.valueOf((String) params[1]);
    }
    Assert.notNull(stage);
    boolean ok = true;

    if (!Global.isVar(tbl)) {
      Global.createVar(tbl, "Choose table", BeeType.TYPE_STRING, null, BeeWidget.LIST);
    }
    if (!Global.isVar(limit)) {
      Global.createVar(limit, "Limit", BeeType.TYPE_INT, null, BeeWidget.TEXT);
    }
    if (!Global.isVar(offset)) {
      Global.createVar(offset, "Offset", BeeType.TYPE_INT, null, BeeWidget.TEXT);
    }
    if (!Global.isVar(stt)) {
      Global.createVar(stt, "Choose state", BeeType.TYPE_STRING, null, BeeWidget.LIST);
    }
    switch (stage) {
      case REQUEST_TABLE_LIST:
        stage = Stages.CHOOSE_TABLE;

        BeeKeeper.getRpc().makeGetRequest(adoptService("rpc_ui_tables"));
        break;

      case CHOOSE_TABLE:
        JsArrayString arr = (JsArrayString) params[0];
        int cc = (Integer) params[1];

        List<String> lst = new ArrayList<String>(arr.length() - cc);
        for (int i = cc; i < arr.length(); i++) {
          lst.add(arr.get(i));
        }
        if (BeeUtils.isEmpty(lst)) {
          Global.showError("NO TABLES");
          unregister();
          return false;
        }
        Global.getVar(tbl).setItems(lst);
        Global.getVar(tbl).setValue(lst.get(0));

        Global.inputVars(new BeeStage(self(), Stages.REQUEST_TABLE.name()), "ALL TABLES",
            tbl, limit, offset);
        break;

      case REQUEST_TABLE:
        GwtEvent<?> event = (GwtEvent<?>) params[0];

        String table = Global.getVarValue(tbl);

        if (BeeUtils.isEmpty(table)) {
          Global.showError("Table name not specified");
          return false;
        } else {
          stage = Stages.LOAD_TABLE;

          Global.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest(adoptService("rpc_ui_table"),
              XmlUtils.createString(BeeService.XML_TAG_DATA
                  , tbl, table
                  , limit, Global.getVarValue(limit)
                  , offset, Global.getVarValue(offset)
                  , stt, Global.getVarValue(stt)));
        }
        Global.getVar(tbl).setValue("");
        Global.getVar(stt).setValue("");
        break;

      case LOAD_TABLE:
        JsArrayString fArr = (JsArrayString) params[0];
        rs = BeeRowSet.restore(fArr.get(0));

        stage = Stages.SHOW_TABLE;
        doSelf();
        break;

      case SHOW_TABLE:
        BeeKeeper.getUi().updateMenu(getTree(rs));

        if (rs.isEmpty()) {
          BeeKeeper.getUi().updateActivePanel(new BeeLabel("RowSet is empty"));
        } else {
          BeeKeeper.getUi().showGrid(rs);
        }
        HasWidgets panel = BeeKeeper.getUi().getActivePanel();

        FlowPanel buttons = new FlowPanel();
        buttons.add(new BeeButton("NEW", self(), Stages.INSERT.name()));
        buttons.add(new BeeButton("SAVE", self(), Stages.SAVE.name()));
        buttons.add(new BeeButton("CANCEL", self(), Stages.CANCEL.name()));
        buttons.add(new BeeButton("FILTER STATES", self(), Stages.FILTER_STATES.name()));
        buttons.add(new BeeButton("EDIT STATE ROLES", self(), Stages.EDIT_STATE.name()));

        Split root = new Split();
        root.addNorth(buttons, 25);
        root.add(panel.iterator().next(), true);
        BeeKeeper.getUi().updateActiveQuietly(root, true);
        break;

      case SAVE:
        BeeRowSet upd = rs.getChanges();

        if (BeeUtils.isEmpty(upd)) {
          BeeKeeper.getLog().info("Nothing to update");
        } else {
          BeeKeeper.getRpc().makePostRequest("rpc_ui_commit", ContentType.BINARY, upd.serialize(),
              new ResponseCallback() {
                @Override
                public void onResponse(JsArrayString rArr) {
                  Assert.notNull(rArr);
                  Assert.parameterCount(rArr.length(), 2);
                  int updCount = BeeUtils.toInt(rArr.get(0));

                  if (updCount < 0) {
                    rs.rollback();
                    BeeKeeper.getLog().severe("Resposnse error: ", rArr.get(1));
                  } else {
                    rs.commit(BeeRowSet.restore(rArr.get(1)));
                    BeeKeeper.getLog().warning("Update count: ", updCount);
                  }
                  stage = Stages.SHOW_TABLE;
                  doSelf();
                }
              });
        }
        break;

      case INSERT:
        rs.addRow(new String[rs.getColumnCount()]).markForInsert();

        stage = Stages.SHOW_TABLE;
        doSelf();
        break;

      case CANCEL:
        rs.rollback();

        stage = Stages.SHOW_TABLE;
        doSelf();
        break;

      case FILTER_STATES:
        table = rs.getViewName();
        Global.getVar(tbl).setValue(table);

        if (BeeUtils.isEmpty(table)) {
          Global.showError("Table name not specified");
          ok = false;
        } else {
          stage = Stages.CHOOSE_STATES;

          BeeKeeper.getRpc().makePostRequest(adoptService("rpc_ui_states"),
              XmlUtils.createString(BeeService.XML_TAG_DATA, tbl, table));
        }
        break;

      case CHOOSE_STATES:
        arr = (JsArrayString) params[0];
        cc = (Integer) params[1];

        lst = new ArrayList<String>();
        int x = 1;

        for (int i = cc; i < arr.length(); i++) {
          String current = arr.get(i);
          lst.add(current);

          for (int j = x; j < (arr.length() - cc); j++) {
            for (int k = cc + j; k < arr.length(); k++) {
              lst.add(current + " " + arr.get(k));
            }
            current += " " + arr.get(cc + j);
          }
          x++;
        }
        if (BeeUtils.isEmpty(lst)) {
          Global.showError("TABLE " + Global.getVarValue(tbl) + " HAS NO STATES");
          return false;
        }
        Global.getVar(stt).setItems(lst);
        Global.getVar(stt).setValue(lst.get(0));
        Global.inputVars(new BeeStage(self(), Stages.REQUEST_TABLE.name()),
            Global.getVarValue(tbl), stt);
        break;

      case EDIT_STATE:
        table = rs.getViewName();
        Global.getVar(tbl).setValue(table);

        stage = Stages.CHOOSE_STATE;

        BeeKeeper.getRpc().makePostRequest(adoptService("rpc_ui_states"),
              XmlUtils.createString(BeeService.XML_TAG_DATA, tbl, table));
        break;

      case CHOOSE_STATE:
        arr = (JsArrayString) params[0];
        cc = (Integer) params[1];

        lst = new ArrayList<String>();

        for (int i = cc; i < arr.length(); i++) {
          lst.add(arr.get(i));
        }
        if (BeeUtils.isEmpty(lst)) {
          Global.showError("NO STATES");
          return false;
        }
        Global.getVar(stt).setItems(lst);
        Global.getVar(stt).setValue(lst.get(0));
        Global.inputVars(new BeeStage(self(), Stages.REQUEST_STATETABLE.name()),
            BeeUtils.ifString(Global.getVarValue(tbl), "ALL TABLES"), stt);
        break;

      case REQUEST_STATETABLE:
        event = (GwtEvent<?>) params[0];

        String states = Global.getVarValue(stt);

        if (BeeUtils.isEmpty(states)) {
          Global.showError("State name not specified");
          return false;
        } else {
          stage = Stages.LOAD_TABLE;

          Global.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest(adoptService("rpc_ui_statetable"),
              XmlUtils.createString(BeeService.XML_TAG_DATA
                  , tbl, Global.getVarValue(tbl)
                  , stt, states));
        }
        Global.getVar(tbl).setValue("");
        Global.getVar(stt).setValue("");
        break;

      default:
        Global.showError("Unhandled stage: " + stage);
        unregister();
        ok = false;
        break;
    }
    return ok;
  }

  private Widget getTree(BeeRowSet rs) {
    BeeTree root = new BeeTree();
    BeeTreeItem item = new BeeTreeItem("RowSet");
    root.addItem(item);
    item.addItem("ViewName: " + rs.getViewName());

    BeeTreeItem cols = new BeeTreeItem("Columns");
    for (BeeColumn col : rs.getColumns()) {
      BeeTreeItem c = new BeeTreeItem(col.getName());
      c.addItem("Type: " + col.getType() + "-" + col.getTypeName());
      c.addItem("Prec: " + col.getPrecision());
      c.addItem("Scale: " + col.getScale());
      c.addItem("Nullable: " + col.getNullable());
      cols.addItem(c);
    }
    item.addItem(cols);

    BeeTreeItem rows = new BeeTreeItem("Data");
    for (int i = 0; i < rs.getRowCount(); i++) {
      BeeTreeItem r = new BeeTreeItem("Row" + i);

      for (int j = 0; j < rs.getColumnCount(); j++) {
        r.addItem(rs.getColumnName(j) + ": " + rs.getValue(i, j));
      }
      rows.addItem(r);
    }
    item.addItem(rows);

    return root;
  }
}
