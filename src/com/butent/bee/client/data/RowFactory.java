package com.butent.bee.client.data;

import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputWidgetCallback;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
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
  
  public static void createRow(String viewName, String formName, String caption,
      Queries.RowCallback callback) {

    if (BeeUtils.isEmpty(viewName)) {
      if (callback != null) {
        callback.onFailure("viewName not specified");
      }
      return;
    }
    if (BeeUtils.isEmpty(formName)) {
      if (callback != null) {
        callback.onFailure("formName not specified");
      }
      return;
    }
    
    DataInfo dataInfo = Global.getDataInfo(viewName, true);
    if (dataInfo == null) {
      return;
    }
    
    getForm(formName, dataInfo, caption, callback);
  }
  
  public static int setDefaults(IsRow row, DataInfo dataInfo) {
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
  
  private static void getForm(String formName, final DataInfo dataInfo,
      final String caption, final Queries.RowCallback callback) {
    FormFactory.createFormView(formName, dataInfo.getViewName(), dataInfo.getColumns(),
        new FormFactory.FormViewCallback() {
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              result.setEditing(true);
              result.start(null);
              openForm(result, dataInfo, caption, callback);
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
    IsRow row = DataUtils.createEmptyRow(dataInfo.getColumnCount());
    setDefaults(row, dataInfo);
    
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
    });
  }
  
  private RowFactory() {
  }
}
