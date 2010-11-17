package com.butent.bee.egg.client.grid;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.communication.ResponseCallback;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.communication.ContentType;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.data.BeeView;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class CellUpdater implements FieldUpdater<Integer, String> {
  private BeeView view;
  private int column;
  private CellKeyProvider keyProvider;
  private BeeCellTable table;

  public CellUpdater(BeeView view, int column, CellKeyProvider keyProvider, BeeCellTable table) {
    this.view = view;
    this.column = column;
    this.keyProvider = keyProvider;
    this.table = table;
  }

  @Override
  public void update(int index, Integer object, String value) {
    if (keyProvider != null) {
      BeeKeeper.getLog().info(keyProvider.getKeyName(), keyProvider.getKey(object));
    }
    BeeKeeper.getLog().info(object, view.getColumnNames()[column], value);

    if (view instanceof BeeRowSet) {
      final BeeRowSet rs = (BeeRowSet) view;

      rs.setValue(object, column, value);

      BeeRowSet upd = rs.getUpdate();

      if (BeeUtils.isEmpty(upd)) {
        BeeKeeper.getLog().info("Nothing to update");
      } else {
        ResponseCallback callback = new ResponseCallback() {
          @Override
          public void onResponse(JsArrayString arr) {
            Assert.notNull(arr);
            Assert.isTrue(arr.length() >= 1);
            int updCount = BeeUtils.toInt(arr.get(0));

            if (updCount > 0) {
              rs.commit();
            } else {
              rs.rollback();
            }
            table.redraw();
            BeeKeeper.getLog().info("Resposnse from update: ", updCount);
          }
        };
        BeeKeeper.getRpc()
          .makePostRequest("rpc_ui_commit", ContentType.BINARY, upd.serialize(), callback);
      }
    }
  }
}
