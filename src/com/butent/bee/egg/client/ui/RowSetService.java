package com.butent.bee.egg.client.ui;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.communication.ResponseCallback;
import com.butent.bee.egg.client.layout.Vertical;
import com.butent.bee.egg.client.tree.BeeTree;
import com.butent.bee.egg.client.tree.BeeTreeItem;
import com.butent.bee.egg.client.utils.BeeXml;
import com.butent.bee.egg.client.widget.BeeButton;
import com.butent.bee.egg.client.widget.BeeLabel;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.BeeStage;
import com.butent.bee.egg.shared.BeeType;
import com.butent.bee.egg.shared.BeeWidget;
import com.butent.bee.egg.shared.communication.ContentType;
import com.butent.bee.egg.shared.data.BeeColumn;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

class RowSetService extends CompositeService {

  private enum Stages {
    REQUEST_TABLE_LIST, CHOOSE_TABLE, REQUEST_TABLE, LOAD_TABLE, SHOW_TABLE,
    MODIFY_TABLE, INSERT, SAVE, CANCEL, SAVE_RESPONSE
  }

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
    Assert.notNull(stage);
    boolean ok = true;
    String fld = "table_name";

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
          BeeGlobal.showError("NO TABLES");
          unregister();
          return false;
        }
        if (!BeeGlobal.isField(fld)) {
          BeeGlobal.createField(fld, "Table name", BeeType.TYPE_STRING, null);
          BeeGlobal.getField(fld).setWidget(BeeWidget.LIST);
        }
        BeeGlobal.getField(fld).setItems(lst);
        BeeGlobal.getField(fld).setValue(lst.get(0));

        stage = Stages.REQUEST_TABLE;

        BeeGlobal.inputFields(new BeeStage(self(), BeeStage.STAGE_CONFIRM), "Get table", fld);
        break;

      case REQUEST_TABLE:
        GwtEvent<?> event = (GwtEvent<?>) params[0];

        String fName = BeeGlobal.getFieldValue(fld);

        if (BeeUtils.isEmpty(fName)) {
          BeeGlobal.showError("Table name not specified");
          ok = false;
        } else {
          stage = Stages.LOAD_TABLE;

          BeeGlobal.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest(adoptService("rpc_ui_table"),
              BeeXml.createString(BeeService.XML_TAG_DATA, fld, fName));
        }
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

        stage = Stages.MODIFY_TABLE;

        FlowPanel buttons = new FlowPanel();
        buttons.add(new BeeButton("NEW", self(), Stages.INSERT.name()));
        buttons.add(new BeeButton("SAVE", self(), Stages.SAVE.name()));
        buttons.add(new BeeButton("CANCEL", self(), Stages.CANCEL.name()));

        Vertical root = new Vertical();
        root.add(panel.iterator().next());
        root.add(buttons);
        BeeKeeper.getUi().updateActiveQuietly(root, true);
        break;

      case MODIFY_TABLE:
        stage = Stages.valueOf((String) params[1]);
        doSelf();
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
        stage = Stages.MODIFY_TABLE;
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

      default:
        BeeGlobal.showError("Unhandled stage: " + stage);
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
    item.addItem("Source: " + rs.getSource());

    BeeTreeItem cols = new BeeTreeItem("Columns");
    for (BeeColumn col : rs.getColumns()) {
      BeeTreeItem c = new BeeTreeItem(col.getName());
      c.addItem("Source: " + col.getFieldSource());
      c.addItem("Field: " + col.getFieldName());
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
