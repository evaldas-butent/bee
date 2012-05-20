package com.butent.bee.client.data;

import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputWidgetCallback;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class RowFactory {

  public static final String OK = "Išsaugoti";
  public static final String CANCEL = "Atšaukti";
  
  public static final String DIALOG_STYLE = "bee-NewRow";
  
  public static BeeRow createEmptyRow(DataInfo dataInfo, boolean defaults) {
    BeeRow row = DataUtils.createEmptyRow(dataInfo.getColumnCount());
    if (defaults) {
      setDefaults(row, dataInfo);
    }
    return row;
  }

  public static void createRow(String viewName, String formName, Queries.RowCallback rowCallback) {
    createRow(viewName, formName, null, null, rowCallback);
  }

  public static void createRow(String viewName, String formName, FormCallback formCallback,
      Queries.RowCallback rowCallback) {
    createRow(viewName, formName, null, formCallback, rowCallback);
  }

  public static void createRow(String viewName, String formName, String caption,
      Queries.RowCallback rowCallback) {
    createRow(viewName, formName, caption, null, rowCallback);
  }

  public static void createRow(String viewName, String formName, String caption) {
    createRow(viewName, formName, caption, null, null);
  }
  
  public static void createRow(String viewName, String formName, String caption,
      FormCallback formCallback, Queries.RowCallback rowCallback) {

    if (BeeUtils.isEmpty(viewName)) {
      if (rowCallback != null) {
        rowCallback.onFailure("viewName not specified");
      }
      return;
    }
    if (BeeUtils.isEmpty(formName)) {
      if (rowCallback != null) {
        rowCallback.onFailure("formName not specified");
      }
      return;
    }

    DataInfo dataInfo = Global.getDataInfo(viewName);
    if (dataInfo == null) {
      return;
    }

    getForm(formName, caption, formCallback, dataInfo, rowCallback);
  }

  public static int setDefaults(BeeRow row, DataInfo dataInfo) {
    if (row == null || dataInfo == null) {
      return BeeConst.UNDEF;
    }

    List<String> colNames = Lists.newArrayList();
    for (BeeColumn column : dataInfo.getColumns()) {
      if (column.hasDefaults()) {
        colNames.add(column.getId());
      }
    }
    if (colNames.isEmpty()) {
      return 0;
    }

    return DataUtils.setDefaults(row, colNames, dataInfo.getColumns(), Global.getDefaults())
        + RelationUtils.setDefaults(dataInfo, row, colNames, dataInfo.getColumns());
  }

  private static int countValues(IsRow row, List<BeeColumn> columns) {
    int cnt = 0;
    for (int i = 0; i < columns.size(); i++) {
      if (!BeeUtils.isEmpty(row.getString(i)) && columns.get(i).isWritable()) {
        cnt++;
      }
    }
    return cnt;
  }

  private static void getForm(String formName, final String caption, FormCallback formCallback,
      final DataInfo dataInfo, final Queries.RowCallback rowCallback) {

    FormCallback fcb =
        (formCallback == null) ? FormFactory.getFormCallback(formName) : formCallback;

    FormFactory.createFormView(formName, dataInfo.getViewName(), dataInfo.getColumns(), fcb,
        new FormFactory.FormViewCallback() {
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              result.setEditing(true);
              result.start(null);
              openForm(result, dataInfo, caption, rowCallback);
            }
          }
        }, false);
  }

  private static void insert(IsRow row, final DataInfo dataInfo,
      final Queries.RowCallback callback) {

    Queries.insert(dataInfo.getViewName(), dataInfo.getColumns(), row, new Queries.RowCallback() {
      @Override
      public void onSuccess(BeeRow result) {
        BeeKeeper.getBus().fireEvent(new RowInsertEvent(dataInfo.getViewName(), result));
        if (callback != null) {
          callback.onSuccess(result);
        }
      }
    });
  }

  private static void openForm(final FormView formView, final DataInfo dataInfo, String caption,
      final Queries.RowCallback callback) {
    BeeRow row = createEmptyRow(dataInfo, true);
    formView.updateRow(row, false);

    String cap = BeeUtils.ifString(caption, formView.getCaption());
    Global.inputWidget(cap, formView.asWidget(), new InputWidgetCallback() {
      @Override
      public String getErrorMessage() {
        if (!formView.validate()) {
          return InputBoxes.SILENT_ERROR;
        } else if (countValues(formView.getActiveRow(), dataInfo.getColumns()) <= 0) {
          return "All columns cannot be empty";
        } else {
          return null;
        }
      }

      @Override
      public void onSuccess() {
        insert(formView.getActiveRow(), dataInfo, callback);
      }
    }, OK, CANCEL, true, DIALOG_STYLE);
  }

  private RowFactory() {
  }
}
