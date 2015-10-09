package com.butent.bee.client.modules.documents;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HasHandlers;

import static com.butent.bee.shared.modules.documents.DocumentConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.VAR_TOTAL;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.trade.TradeUtils;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.interceptor.FileGridInterceptor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public final class DocumentsHandler {

  private static final class DocumentBuilder extends AbstractFormInterceptor {

    private FileCollector collector;
    private UnboundSelector templSelector;

    @Override
    public void afterCreateWidget(String name, IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {

      if (widget instanceof FileCollector) {
        this.collector = (FileCollector) widget;
        this.collector.bindDnd(getFormView());
      }

      if (widget instanceof UnboundSelector) {
        templSelector = (UnboundSelector) widget;
      }
    }

    @Override
    public void afterInsertRow(final IsRow result, boolean forced) {
      if (collector != null && !collector.isEmpty()) {
        sendFiles(result.getId(), collector.getFiles(), null);
        collector.clear();
      }

      if (result.getString(Data.getColumnIndex(VIEW_DOCUMENTS, COL_DOCUMENT_COMPANY)) != null) {
        insertCompanyInfo(result, null);
      }

      if (templSelector != null) {
        Long selId = BeeUtils.toLong(templSelector.getValue());
        if (DataUtils.isId(selId)) {

          Queries.getRow(VIEW_DOCUMENT_TEMPLATES, selId, new RowCallback() {
            @Override
            public void onSuccess(BeeRow templateRow) {
              insertTemplateContent(result, Data.getString(VIEW_DOCUMENT_TEMPLATES, templateRow,
                  COL_DOCUMENT_CONTENT));
            }
          });

        }

      }
    }

    @Override
    public void beforeRefresh(FormView form, IsRow row) {
      if (templSelector != null
          && DataUtils.isId(row.getProperty(TaskConstants.PRM_DEFAULT_DBA_TEMPLATE))) {
        templSelector.setValue(BeeUtils.toLong(row.getProperty(
            TaskConstants.PRM_DEFAULT_DBA_TEMPLATE)), true);
      }

      DateTime t1 = row.getDateTime(form.getDataIndex(COL_DOCUMENT_DATE));
       /* resetting document date without current time */
      if (t1 != null && DataUtils.isNewRow(row)) {
        t1.setLocalTime(new JustDate(t1).getTime());
        row.setValue(form.getDataIndex(COL_DOCUMENT_DATE), t1);
        form.refreshBySource(COL_DOCUMENT_DATE);
      }
    }

    @Override
    public FormInterceptor getInstance() {
      return new DocumentBuilder();
    }

    @Override
    public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {

    }
  }

  public static void register() {
    GridFactory.registerGridInterceptor(VIEW_DOCUMENT_TEMPLATES, new DocumentTemplatesGrid());

    GridFactory.registerGridInterceptor(VIEW_DOCUMENTS, new DocumentTemplatesGrid());
    GridFactory.registerGridInterceptor(VIEW_DOCUMENT_FILES,
        new FileGridInterceptor(COL_DOCUMENT, AdministrationConstants.COL_FILE,
            AdministrationConstants.COL_FILE_CAPTION, AdministrationConstants.ALS_FILE_NAME));

    GridFactory.registerGridInterceptor("RelatedDocuments", new RelatedDocumentsHandler());

    FormFactory.registerFormInterceptor(TBL_DOCUMENT_TREE, new DocumentTreeForm());

    FormFactory.registerFormInterceptor("DocumentTemplate", new DocumentTemplateForm());
    FormFactory.registerFormInterceptor(FORM_DOCUMENT, new DocumentForm());
    FormFactory.registerFormInterceptor("DocumentItem", new DocumentDataForm());

    FormFactory.registerFormInterceptor("NewDocument", new DocumentBuilder());

    TradeUtils.registerTotalRenderer(VIEW_DOCUMENT_ITEMS, VAR_TOTAL);
  }

  static void copyDocumentData(Long dataId, final IdCallback callback) {
    Assert.notNull(callback);

    if (!DataUtils.isId(dataId)) {
      callback.onSuccess(dataId);
    } else {
      ParameterList args = createArgs(SVC_COPY_DOCUMENT_DATA);
      args.addDataItem(COL_DOCUMENT_DATA, dataId);

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          response.notify(BeeKeeper.getScreen());

          if (!response.hasErrors()) {
            callback.onSuccess(response.getResponseAsLong());
          }
        }
      });
    }
  }

  static ParameterList createArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.DOCUMENTS, method);
  }

  private static void sendFiles(final Long docId, Collection<FileInfo> files,
      final ScheduledCommand onComplete) {

    final String viewName = VIEW_DOCUMENT_FILES;
    final List<BeeColumn> columns = Data.getColumns(viewName);

    final Holder<Integer> latch = Holder.of(files.size());

    for (final FileInfo fileInfo : files) {
      FileUtils.uploadFile(fileInfo, new Callback<Long>() {
        @Override
        public void onSuccess(Long result) {
          BeeRow row = DataUtils.createEmptyRow(columns.size());

          Data.setValue(viewName, row, COL_DOCUMENT, docId);
          Data.setValue(viewName, row, AdministrationConstants.COL_FILE, result);

          Data.setValue(viewName, row, COL_FILE_DATE,
              fileInfo.getFileDate() == null ? new DateTime() : fileInfo.getFileDate());
          Data.setValue(viewName, row, COL_FILE_VERSION, fileInfo.getFileVersion());

          Data.setValue(viewName, row, COL_FILE_CAPTION,
              BeeUtils.notEmpty(fileInfo.getCaption(), fileInfo.getName()));
          Data.setValue(viewName, row, COL_DESCRIPTION, fileInfo.getDescription());

          Queries.insert(viewName, columns, row, new RowCallback() {
            @Override
            public void onSuccess(BeeRow br) {
              latch.set(latch.get() - 1);
              if (!BeeUtils.isPositive(latch.get()) && onComplete != null) {
                onComplete.execute();
              }
            }
          });
        }
      });
    }
  }

  private DocumentsHandler() {
  }

  public static void insertCompanyInfo(IsRow row, String oldValue) {

    if (row == null) {
      return;
    }

    final Long rowId = row.getId();
    final String company = row.getString(Data.getColumnIndex(VIEW_DOCUMENTS,
        COL_DOCUMENT_COMPANY));

    if (!BeeUtils.isEmpty(company)) {

      Filter filter =
          Filter.and(Filter.equals(COL_DOCUMENT, rowId), Filter
              .equals(COL_DOCUMENT_COMPANY, oldValue));

      Queries.update(AdministrationConstants.VIEW_RELATIONS, filter, COL_DOCUMENT_COMPANY, company,
          new IntCallback() {

            @Override
            public void onSuccess(Integer result) {
              if (result == 0) {
                Queries.insert(AdministrationConstants.VIEW_RELATIONS, Data.getColumns(
                    AdministrationConstants.VIEW_RELATIONS,
                    Lists.newArrayList(COL_DOCUMENT_COMPANY,
                        COL_DOCUMENT)), Lists.newArrayList(company, BeeUtils
                    .toString(rowId)));
              }
            }
          });
    }
  }

  public static void insertTemplateContent(final IsRow row, String value) {

    if (row == null) {
      return;
    }

    List<BeeColumn> cols = Data.getColumns(VIEW_DOCUMENT_DATA,
        Lists.newArrayList(COL_DOCUMENT_CONTENT));
    List<String> values = Lists.newArrayList(value);

    Queries.insert(VIEW_DOCUMENT_DATA, cols, values, null, new RowCallback() {

      @Override
      public void onSuccess(BeeRow result) {
        Queries.update(VIEW_DOCUMENTS, row.getId(), COL_DOCUMENT_DATA,
            Value.getValue(result.getId()), new IntCallback() {
              @Override
              public void onSuccess(Integer updResult) {
                DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_DOCUMENTS);
              }
            });
      }
    });

  }
}
