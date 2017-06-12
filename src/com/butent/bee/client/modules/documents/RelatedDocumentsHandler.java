package com.butent.bee.client.modules.documents;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.documents.DocumentConstants.*;

import com.butent.bee.client.composite.Relations;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
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
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

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
    FormView parentForm = null;
    IsRow parentRow = null;
    String parentViewName = null;
    boolean relationEnsured = false;

    DataInfo info = Data.getDataInfo(VIEW_DOCUMENTS);
    DataInfo relInfo = Data.getDataInfo(VIEW_RELATED_DOCUMENTS);
    BeeRow docRow = RowFactory.createEmptyRow(info, true);
    GridView gridView = presenter.getGridView();

    if (gridView != null) {
      parentForm = ViewHelper.getForm(gridView.asWidget());
    }
    if (parentForm != null) {
      parentRow = parentForm.getActiveRow();
    }
    if (parentForm != null) {
      parentViewName = parentForm.getViewName();
    }
    if (parentRow != null && !BeeUtils.isEmpty(parentViewName)) {

      switch (parentViewName) {
        case ProjectConstants.VIEW_PROJECTS:
          RelationUtils.updateRow(info, COL_DOCUMENT_COMPANY, docRow,
            Data.getDataInfo(parentViewName), parentRow, false);
          Data.setValue(VIEW_DOCUMENTS, docRow, COL_DOCUMENT_COMPANY,
            Data.getLong(parentViewName, parentRow, ProjectConstants.COL_COMAPNY));
          break;
        case ClassifierConstants.VIEW_COMPANIES:
          RelationUtils.updateRow(info, COL_DOCUMENT_COMPANY, docRow,
            Data.getDataInfo(parentViewName), parentRow, false);
          Data.setValue(VIEW_DOCUMENTS, docRow, COL_DOCUMENT_COMPANY, parentRow.getId());
          relationEnsured = true;
          break;
        default:
          if (relInfo == null) {
            break;
          }

          for (String col : info.getColumnNames(false)) {
            if (BeeUtils.same(parentViewName, relInfo.getRelation(col))) {
              relationEnsured = true;
              break;
            }
          }

          if (relationEnsured) {
            docRow.setProperty(Relations.PFX_RELATED + parentViewName,
              DataUtils.buildIdList(parentRow.getId()));
          }
      }
    }

    if (!relationEnsured) {
      RowFactory.createRow(info, docRow, Modality.ENABLED, new RowCallback() {
        @Override
        public void onSuccess(final BeeRow result) {
          final long docId = result.getId();

          presenter.getGridView().ensureRelId(relId
            -> Queries.insert(AdministrationConstants.VIEW_RELATIONS,
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
    } else {
      RowFactory.createRow(info, docRow, Modality.ENABLED, new RowCallback() {
        @Override
        public void onSuccess(final BeeRow result) {
          presenter.handleAction(Action.REFRESH);
          ViewHelper.getForm(presenter.getGridView().asWidget()).refresh();
        }
      });
    }

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
}
