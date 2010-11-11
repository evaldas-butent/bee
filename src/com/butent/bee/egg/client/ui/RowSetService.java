package com.butent.bee.egg.client.ui;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.tree.BeeTree;
import com.butent.bee.egg.client.tree.BeeTreeItem;
import com.butent.bee.egg.client.utils.BeeXml;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.BeeStage;
import com.butent.bee.egg.shared.BeeType;
import com.butent.bee.egg.shared.BeeWidget;
import com.butent.bee.egg.shared.data.BeeColumn;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.data.BeeView;
import com.butent.bee.egg.shared.data.DataUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

class RowSetService extends CompositeService {

  private enum Stages {
    REQUEST_TABLE_LIST, CHOOSE_TABLE, REQUEST_TABLE, SHOW_TABLE
  }

  private Stages stage = null;

  protected RowSetService() {
  }

  protected RowSetService(String serviceId) {
    super(serviceId);
    nextStage();
  }

  @Override
  protected CompositeService create(String svcId) {
    return new RowSetService(svcId);
  }

  @Override
  protected boolean doStage(Object... params) {
    Assert.notNull(stage);
    boolean ok = true;
    String fld = "table_name";

    switch (stage) {
      case REQUEST_TABLE_LIST:
        BeeKeeper.getRpc().makeGetRequest(adoptService("rpc_ui_tables"));
        break;

      case CHOOSE_TABLE:
        JsArrayString arr = (JsArrayString) params[0];
        int cc = (Integer) params[1];

        List<String> lst = new ArrayList<String>(arr.length() - cc);
        for (int i = cc; i < arr.length(); i++) {
          lst.add(arr.get(i));
        }
        if (!BeeGlobal.isField(fld)) {
          BeeGlobal.createField(fld, "Table name", BeeType.TYPE_STRING, null);
          BeeGlobal.getField(fld).setWidget(BeeWidget.LIST);
        }
        BeeGlobal.getField(fld).setItems(lst);
        BeeGlobal.getField(fld).setValue(lst.get(0));

        BeeGlobal.inputFields(new BeeStage(adoptService("comp_ui_rowset"),
            BeeStage.STAGE_CONFIRM), "Get table", fld);
        break;

      case REQUEST_TABLE:
        GwtEvent<?> event = (GwtEvent<?>) params[0];

        String fName = BeeGlobal.getFieldValue(fld);

        if (BeeUtils.isEmpty(fName)) {
          BeeGlobal.showError("Table name not specified");
          ok = false;
        } else {
          BeeGlobal.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest(adoptService("rpc_ui_table"),
              BeeXml.createString(BeeService.XML_TAG_DATA, fld, fName));
        }
        break;

      case SHOW_TABLE:
        JsArrayString fArr = (JsArrayString) params[0];

        BeeRowSet rs = BeeRowSet.restore(fArr.get(0));

        BeeKeeper.getUi().updateMenu(getTree(rs));

        if (rs.isEmpty()) {
          BeeKeeper.getLog().warning("RowSet is empty");
        } else {
          BeeView view = DataUtils.createView(rs.getData(),
              (Object[]) rs.getColumns());
          BeeKeeper.getUi().showGrid(view);
        }
        break;

      default:
        BeeGlobal.showError("Unhandled stage: " + stage);
        unregister();
        ok = false;
        break;
    }

    if (ok) {
      nextStage();
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
      c.addItem("Table: " + col.getTable());
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
        r.addItem(rs.getColumn(j).getName() + ": " + rs.getRow(i).getValue(j));
      }
      rows.addItem(r);
    }
    item.addItem(rows);

    return root;
  }

  private void nextStage() {
    int x = 0;

    if (!BeeUtils.isEmpty(stage)) {
      x = stage.ordinal() + 1;
    }

    if (x < Stages.values().length) {
      stage = Stages.values()[x];
    } else {
      unregister();
    }
  }
}
