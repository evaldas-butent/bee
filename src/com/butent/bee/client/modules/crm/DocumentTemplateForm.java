package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

public class DocumentTemplateForm extends DocumentDataForm {

  private final Button newDocumentButton = new Button(Localized.getConstants().documentNew(),
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          createDocument();
        }
      });

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    if (getHeaderView() == null) {
      return;
    }
    getHeaderView().clearCommandPanel();

    if (!DataUtils.isNewRow(row)) {
      getHeaderView().addCommandItem(newDocumentButton);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new DocumentTemplateForm();
  }

  private void createDocument() {
    LocalizableConstants loc = Localized.getConstants();

    Global.inputString(loc.documentNew(), loc.documentName(),
        new StringCallback() {
          @Override
          public void onSuccess(final String value) {
            DocumentHandler.copyDocumentData(getLongValue(COL_DOCUMENT_DATA),
                new IdCallback() {
                  @Override
                  public void onSuccess(Long dataId) {
                    Queries.insert(TBL_DOCUMENTS,
                        Data.getColumns(TBL_DOCUMENTS, Lists.newArrayList(COL_DOCUMENT_CATEGORY,
                            COL_DOCUMENT_NAME, COL_DOCUMENT_DATA)),
                        Lists.newArrayList(getStringValue(COL_DOCUMENT_CATEGORY), value,
                            DataUtils.isId(dataId) ? BeeUtils.toString(dataId) : null),
                        null, new RowInsertCallback(TBL_DOCUMENTS, null) {
                          @Override
                          public void onSuccess(BeeRow result) {
                            super.onSuccess(result);
                            RowEditor.openRow(TBL_DOCUMENTS, result, true);
                          }
                        });
                  }
                });
          }
        });
  }
}
