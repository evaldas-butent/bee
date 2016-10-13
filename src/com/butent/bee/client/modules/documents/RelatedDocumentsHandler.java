package com.butent.bee.client.modules.documents;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.documents.DocumentConstants.*;
import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dialog.Modality;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;

public class RelatedDocumentsHandler extends AbstractGridInterceptor {

  private int documentIndex = BeeConst.UNDEF;

  public RelatedDocumentsHandler() {
  }

  @Override
  public void afterCreate(GridView gridView) {
    documentIndex = gridView.getDataIndex(COL_DOCUMENT);
    super.afterCreate(gridView);
  }

  @Override
  public boolean beforeAddRow(final GridPresenter presenter, boolean copy) {
    DataInfo info = Data.getDataInfo(VIEW_DOCUMENTS);
    BeeRow docRow = RowFactory.createEmptyRow(info, true);

    GridView gridView = presenter.getGridView();

    FormView parentForm = null;
    if (gridView != null) {
      parentForm = ViewHelper.getForm(gridView.asWidget());
    }

    IsRow parentRow = null;
    if (parentForm != null) {
      parentRow = parentForm.getActiveRow();
    }

    if (parentRow != null
        && BeeUtils.same(parentForm.getFormName(), ProjectConstants.FORM_PROJECT)) {

      int idxCmp = info.getColumnIndex(COL_DOCUMENT_COMPANY);
      int idxCmpName = info.getColumnIndex(ALS_DOCUMENT_COMPANY_NAME);
      int idxParCmp = parentForm.getDataIndex(ProjectConstants.COL_COMAPNY);
      int idxParCmpName = parentForm.getDataIndex(ProjectConstants.ALS_PROJECT_COMPANY_NAME);

      if (!BeeConst.isUndef(idxCmp) && !BeeConst.isUndef(idxCmpName)
          && !BeeConst.isUndef(idxParCmp) && !BeeConst.isUndef(idxParCmpName)) {

        docRow.setValue(idxCmp, parentRow.getLong(idxParCmp));
        docRow.setValue(idxCmpName, parentRow.getString(idxParCmpName));
      }
    }

    if (parentRow != null
        && BeeUtils.same(parentForm.getFormName(), ClassifierConstants.COL_ITEM)) {
      Dictionary dic = Localized.dictionary();

      Global.choice(null, dic.chooseDocumentSource(),
          Arrays.asList(dic.documents(), dic.documentNew()), value -> {
            switch (value) {
              case 0:
                final UnboundSelector us = UnboundSelector.create(Relation.create(
                    VIEW_DOCUMENTS, Arrays.asList(COL_DOCUMENT_NAME, ALS_TYPE_NAME)));

                Global.inputWidget(dic.documents(), us, new InputCallback() {
                  @Override
                  public void onSuccess() {
                    presenter.getGridView().ensureRelId(relId -> Queries.insert(VIEW_RELATIONS,
                        Data.getColumns(AdministrationConstants.VIEW_RELATIONS,
                            Lists.newArrayList(COL_DOCUMENT, presenter.getGridView()
                                .getRelColumn())),
                        Queries.asList(us.getRelatedId(), relId), null, new RowCallback() {
                          @Override
                          public void onSuccess(BeeRow row) {
                            presenter.handleAction(Action.REFRESH);
                            ViewHelper.getForm(presenter.getGridView().asWidget()).refresh();
                          }
                        }));
                  }

                  @Override
                  public String getErrorMessage() {
                    if (!DataUtils.isId(us.getRelatedId())) {
                      return Localized.dictionary().valueRequired();
                    }
                    return InputCallback.super.getErrorMessage();
                  }
                });
                break;
              case 1:
                createDocument(presenter, info, docRow);
                break;
            }
          });
      return false;
    }

    createDocument(presenter, info, docRow);
    return false;
  }

  @Override
  public GridInterceptor getInstance() {
    return new RelatedDocumentsHandler();
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    event.consume();

    if (!BeeConst.isUndef(documentIndex) && event.getRowValue() != null) {
      Long docId = event.getRowValue().getLong(documentIndex);

      if (DataUtils.isId(docId)) {
        RowEditor.open(VIEW_DOCUMENTS, docId, Opener.MODAL, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            getGridPresenter().handleAction(Action.REFRESH);
          }
        });
      }
    }
  }

  private static void createDocument(final GridPresenter presenter, DataInfo info, BeeRow docRow) {
    RowFactory.createRow(info, docRow, Modality.ENABLED, new RowCallback() {
      @Override
      public void onSuccess(final BeeRow result) {
        final long docId = result.getId();

        presenter.getGridView().ensureRelId(relId -> Queries.insert(VIEW_RELATIONS,
            Data.getColumns(AdministrationConstants.VIEW_RELATIONS,
                Lists.newArrayList(COL_DOCUMENT, presenter.getGridView().getRelColumn())),
            Queries.asList(docId, relId), null, new RowCallback() {
              @Override
              public void onSuccess(BeeRow row) {
                presenter.handleAction(Action.REFRESH);
                ViewHelper.getForm(presenter.getGridView().asWidget()).refresh();
              }
            }));
      }
    });
  }
}
