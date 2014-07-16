package com.butent.bee.client.modules.service;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.documents.DocumentConstants;
import com.butent.bee.shared.modules.service.ServiceConstants.ObjectStatus;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

public final class ServiceUtils {
  
  private ServiceUtils() {
  }

  public static void ensureDocumentDefaultValues(DataInfo formDataInfo, IsRow formRow,
      FormView parentForm) {

    DataInfo dataInfo = Data.getDataInfo(parentForm.getViewName());
    IsRow parentFormRow = parentForm.getActiveRow();

    if (dataInfo == null) {
      return;
    }

    ObjectStatus status =
        EnumUtils.getEnumByIndex(ObjectStatus.class, parentFormRow.getInteger(dataInfo
            .getColumnIndex(COL_OBJECT_STATUS)));

    fillCategory(status, formDataInfo, formRow);
  }
  
  private static void fillCategory(ObjectStatus status, DataInfo dataInfo, IsRow formRow) {
    
    switch (status) {
      case POTENTIAL_OBJECT:
        Global.getParameter(PRM_SVC_POTENTIAL_OBJECT_CATEGORY,
            getParameterConsumer(dataInfo, formRow));
        break;
      case PROJECT_OBJECT:
        Global.getParameter(PRM_SVC_PROJECT_OBJECT_CATEGORY,
            getParameterConsumer(dataInfo, formRow));
        break;
      case SERVICE_OBJECT:
        Global.getParameter(PRM_SVC_SERVICE_OBJECT_CATEGORY,
            getParameterConsumer(dataInfo, formRow));
        break;
      default:
        break;
    }
  }

  private static Consumer<String> getParameterConsumer(final DataInfo dataInfo,
      final IsRow formRow) {
    return new Consumer<String>() {

      @Override
      public void accept(String input) {
        Long categoryId = BeeUtils.toLong(input);

        if (!DataUtils.isId(categoryId)) {
          return;
        }
        
        Queries.getRow(DocumentConstants.VIEW_DOCUMENT_TREE, categoryId,
            getDocumentsTreeRowCallback(dataInfo, formRow));

        formRow
            .setValue(dataInfo.getColumnIndex(DocumentConstants.COL_DOCUMENT_CATEGORY), categoryId);
        formRow.setValue(dataInfo.getColumnIndex(DocumentConstants.COL_REGISTRATION_NUMBER), input);
      }
    };
  }
  
  private static RowCallback getDocumentsTreeRowCallback(final DataInfo dataInfo,
      final IsRow formRow) {
    return new RowCallback() {

      @Override
      public void onSuccess(BeeRow result) {
        DataInfo treeData = Data.getDataInfo(DocumentConstants.VIEW_DOCUMENT_TREE);

        formRow
            .setValue(dataInfo.getColumnIndex(DocumentConstants.COL_DOCUMENT_CATEGORY), result
                .getId());
        formRow.setValue(dataInfo.getColumnIndex(DocumentConstants.ALS_CATEGORY_NAME),
            result.getString(treeData.getColumnIndex(DocumentConstants.COL_NAME)));
      }

    };
  }
}
