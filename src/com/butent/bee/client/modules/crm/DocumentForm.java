package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

public class DocumentForm extends DocumentDataForm {

  @Override
  public FormInterceptor getInstance() {
    return new DocumentForm();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.PRINT && !DataUtils.isNewRow(getActiveRow())) {
      final String content = getDataValue(COL_DOCUMENT_CONTENT);
      LocalizableConstants loc = Localized.getConstants();

      if (BeeUtils.isEmpty(content)) {
        getFormView().notifyWarning(loc.documentContentIsEmpty());
      } else {
        Global.inputString(loc.file(), loc.fileName(), new StringCallback() {
          @Override
          public void onSuccess(String value) {
            ParameterList args = CrmKeeper.createArgs(SVC_CREATE_PDF_DOCUMENT);
            args.addDataItem(COL_DOCUMENT, getActiveRowId());
            args.addDataItem(CommonsConstants.ALS_FILE_NAME, value);
            args.addDataItem(COL_DOCUMENT_CONTENT, parseContent(content));

            BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
              @Override
              public void onResponse(ResponseObject response) {
                response.notify(getFormView());

                if (response.hasErrors()) {
                  return;
                }
                ((ChildGrid) getFormView().getWidgetByName(TBL_DOCUMENT_FILES))
                    .getPresenter().refresh(false);
              }
            });
          }
        }, getDataValue(COL_DOCUMENT_NAME));
      }
      return false;
    }
    return true;
  }

  @Override
  public void onStart(FormView form) {
    if (getHeaderView() == null) {
      return;
    }
    final LocalizableConstants loc = Localized.getConstants();

    getHeaderView().addCommandItem(new Button(loc.newDocumentTemplate(),
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            Global.inputString(loc.newDocumentTemplate(), loc.templateName(),
                new StringCallback() {
                  @Override
                  public void onSuccess(final String value) {
                    DocumentHandler.copyDocumentData(getDataLong(COL_DOCUMENT_DATA),
                        new IdCallback() {
                          @Override
                          public void onSuccess(Long dataId) {
                            Queries.insert(TBL_DOCUMENT_TEMPLATES,
                                Data.getColumns(TBL_DOCUMENT_TEMPLATES,
                                    Lists.newArrayList(COL_DOCUMENT_CATEGORY,
                                        COL_DOCUMENT_TEMPLATE_NAME, COL_DOCUMENT_DATA)),
                                Lists.newArrayList(getDataValue(COL_DOCUMENT_CATEGORY), value,
                                    DataUtils.isId(dataId) ? BeeUtils.toString(dataId) : null),
                                null, new RowInsertCallback(TBL_DOCUMENT_TEMPLATES, null) {
                                  @Override
                                  public void onSuccess(BeeRow result) {
                                    super.onSuccess(result);
                                    RowEditor.openRow(TBL_DOCUMENT_TEMPLATES, result, true);
                                  }
                                });
                          }
                        });
                  }
                });
          }
        }));
  }
}
