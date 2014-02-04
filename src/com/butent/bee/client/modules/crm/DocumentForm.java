package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.ui.Relation.Caching;
import com.butent.bee.shared.utils.BeeUtils;

public class DocumentForm extends DocumentDataForm {

  private final Button newTemplateButton = new Button(Localized.getConstants()
      .newDocumentTemplate(), new ClickHandler() {
    @Override
    public void onClick(ClickEvent event) {
      createTemplate();
    }
  });

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, TBL_DOCUMENT_ITEMS) && widget instanceof ChildGrid) {
      final ChildGrid grid = (ChildGrid) widget;

      grid.setGridInterceptor(new AbstractGridInterceptor() {
        @Override
        public void onEditStart(final EditStartEvent event) {
          if (!BeeUtils.same(event.getColumnId(), COL_DOCUMENT_DATA)) {
            if (event.isReadOnly()) {
              event.consume();
            }
            return;
          }
          Long dataId = DataUtils.getLong(getDataColumns(), event.getRowValue(), COL_DOCUMENT_DATA);

          if (!DataUtils.isId(dataId)) {
            event.consume();

            Relation relation = Relation.create(TBL_DOCUMENT_TEMPLATES,
                Lists.newArrayList(COL_DOCUMENT_CATEGORY_NAME, COL_DOCUMENT_TEMPLATE_NAME));
            relation.disableNewRow();
            relation.setCaching(Caching.QUERY);

            final UnboundSelector selector = UnboundSelector.create(relation);

            HtmlTable table = new HtmlTable();
            table.setText(0, 0, Localized.getConstants().templateName());
            table.setWidget(0, 1, selector);

            Global.inputWidget(Localized.getConstants().selectDocumentTemplate(), table,
                new InputCallback() {
                  @Override
                  public void onSuccess() {
                    final Consumer<Long> executor = new Consumer<Long>() {
                      @Override
                      public void accept(Long newDataId) {
                        String viewName = grid.getPresenter().getViewName();

                        Queries.update(viewName, event.getRowValue().getId(),
                            event.getRowValue().getVersion(),
                            Data.getColumns(viewName, Lists.newArrayList(COL_DOCUMENT_DATA)),
                            Lists.newArrayList((String) null),
                            Lists.newArrayList(BeeUtils.toString(newDataId)),
                            null, new RowUpdateCallback(viewName) {
                              @Override
                              public void onSuccess(BeeRow result) {
                                super.onSuccess(result);

                                grid.getPresenter().getGridView()
                                    .onEditStart(new EditStartEvent(result, event.getColumnId(),
                                        event.getSourceElement(), event.getCharCode(),
                                        event.isReadOnly()));
                              }
                            });
                      }
                    };
                    Long data = null;

                    if (selector.getRelatedRow() != null) {
                      data = Data.getLong(TBL_DOCUMENT_TEMPLATES, selector.getRelatedRow(),
                          COL_DOCUMENT_DATA);
                    }
                    if (DataUtils.isId(data)) {
                      DocumentHandler.copyDocumentData(data, new IdCallback() {
                        @Override
                        public void onSuccess(Long result) {
                          executor.accept(result);
                        }
                      });
                    } else {
                      Queries.insert(TBL_DOCUMENT_DATA, Data.getColumns(TBL_DOCUMENT_DATA,
                          Lists.newArrayList(COL_DOCUMENT_CONTENT)),
                          Lists.newArrayList((String) null), null, new RowCallback() {
                            @Override
                            public void onSuccess(BeeRow result) {
                              executor.accept(result.getId());
                            }
                          });
                    }
                  }
                });
          }
        }
      });
    }

  }

  @Override
  public FormInterceptor getInstance() {
    return new DocumentForm();
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    if (getHeaderView() == null) {
      return;
    }
    getHeaderView().clearCommandPanel();

    if (!DataUtils.isNewRow(row)) {
      getHeaderView().addCommandItem(newTemplateButton);
    }
  }

  private void createTemplate() {
    LocalizableConstants loc = Localized.getConstants();

    Global.inputString(loc.newDocumentTemplate(), loc.templateName(),
        new StringCallback() {
          @Override
          public void onSuccess(final String value) {
            DocumentHandler.copyDocumentData(getLongValue(COL_DOCUMENT_DATA),
                new IdCallback() {
                  @Override
                  public void onSuccess(Long dataId) {
                    Queries.insert(TBL_DOCUMENT_TEMPLATES,
                        Data.getColumns(TBL_DOCUMENT_TEMPLATES,
                            Lists.newArrayList(COL_DOCUMENT_CATEGORY, COL_DOCUMENT_TEMPLATE_NAME,
                                COL_DOCUMENT_DATA)),
                        Lists.newArrayList(getStringValue(COL_DOCUMENT_CATEGORY), value,
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
}
