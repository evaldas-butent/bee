package com.butent.bee.egg.client.ui;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.utils.BeeXml;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.BeeStage;
import com.butent.bee.egg.shared.BeeType;
import com.butent.bee.egg.shared.BeeWidget;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.data.BeeView;
import com.butent.bee.egg.shared.data.DataUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;

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

        String[] lst = new String[arr.length() - cc];
        for (int i = cc; i < arr.length(); i++) {
          lst[i - 1] = arr.get(i);
        }

        if (!BeeGlobal.isField(fld)) {
          BeeGlobal.createField(fld, "Table name", BeeType.TYPE_STRING,
              lst[0], BeeWidget.LIST, lst);
        }

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
        BeeKeeper.getLog().info("text", fArr.get(0));

        BeeRowSet rs = BeeRowSet.restore(fArr.get(0));

        BeeView view = DataUtils.createView(rs.getData(),
            (Object[]) rs.getColumns());
        BeeKeeper.getUi().showGrid(view);

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
