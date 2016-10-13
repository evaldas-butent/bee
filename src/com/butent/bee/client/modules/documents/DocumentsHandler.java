package com.butent.bee.client.modules.documents;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import static com.butent.bee.shared.modules.documents.DocumentConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.event.logical.RowActionEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.interceptor.FileGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowTransformEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.menu.MenuHandler;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiConsumer;

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
  }

  private static class RowTransformHandler implements RowTransformEvent.Handler {

    @Override
    public void onRowTransform(RowTransformEvent event) {
      if (event.hasView(VIEW_DOCUMENTS)) {
        event.setResult(DataUtils.join(Data.getDataInfo(VIEW_DOCUMENTS), event.getRow(),
            Lists.newArrayList(COL_DOCUMENT_NAME, ALS_DOCUMENT_COMPANY_NAME, "CompanyTypeName",
                COL_DOCUMENT_DATE, "Expires", COL_DOCUMENT_NUMBER, COL_REGISTRATION_NUMBER,
                ALS_CATEGORY_NAME, ALS_TYPE_NAME, ALS_PLACE_NAME, ALS_STATUS_NAME,
                COL_DOCUMENT_RECEIVED, COL_DOCUMENT_SENT, COL_DOCUMENT_RECEIVED_NUMBER,
                COL_DOCUMENT_SENT_NUMBER, COL_DESCRIPTION, ClassifierConstants.COL_FIRST_NAME,
                ClassifierConstants.COL_LAST_NAME, ClassifierConstants.ALS_POSITION_NAME, "Notes"),
            BeeConst.STRING_SPACE));
      } else if (event.hasView(VIEW_DOCUMENT_FILES)) {
        event.setResult(BeeUtils.joinWords(
            Data.getString(event.getViewName(), event.getRow(), COL_FILE_CAPTION),
            Data.getString(event.getViewName(), event.getRow(), COL_FILE_DESCRIPTION),
            Data.getString(event.getViewName(), event.getRow(), COL_FILE_COMMENT),
            Format.getDefaultDateTimeFormat().format(Data.getDateTime(event.getViewName(),
                event.getRow(), COL_FILE_DATE)),
            Data.getString(event.getViewName(), event.getRow(),
                AdministrationConstants.ALS_FILE_NAME),
            Data.getString(event.getViewName(), event.getRow(),
                AdministrationConstants.ALS_FILE_TYPE),
            Data.getString(event.getViewName(), event.getRow(), COL_FILE_OWNER_FIRST_NAME),
            Data.getString(event.getViewName(), event.getRow(), COL_FILE_OWNER_LAST_NAME)));
      }
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

    BeeKeeper.getBus().registerRowTransformHandler(new RowTransformHandler());

    MenuService.DOCUMENTS.setHandler(new MenuHandler() {

      @Override
      public void onSelection(String parameters) {
        GridOptions options = null;
        GridInterceptor interceptor = GridFactory.getGridInterceptor(VIEW_DOCUMENTS);
        String key = GridFactory.getSupplierKey(VIEW_DOCUMENTS, interceptor);

        if (!BeeUtils.isEmpty(parameters)) {
          options = GridOptions.forCaptionAndFilter(getCaption(parameters), getFilter(parameters));
          key = BeeUtils.join(BeeConst.STRING_UNDER, key, parameters);
        }

        GridFactory.createGrid(VIEW_DOCUMENTS, key, interceptor, EnumSet.of(UiOption.GRID),
            options, ViewHelper.getPresenterCallback());
      }

      private String getCaption(String parameters) {
        switch (parameters) {
          case COL_DOCUMENT_SENT:
            return Localized.dictionary().documentFilterSent();
          case COL_DOCUMENT_RECEIVED:
            return Localized.dictionary().documentFilterReceived();

          default:
            return Data.getViewCaption(VIEW_DOCUMENTS);
        }
      }

      private Filter getFilter(String parameters) {

        if (Data.containsColumn(VIEW_DOCUMENTS, parameters)) {
          return Filter.notNull(parameters);
        }

        return Filter.isTrue();
      }
    });

    Global.getNewsAggregator().registerFilterHandler(Feed.DOCUMENTS,
        new BiConsumer<GridOptions, PresenterCallback>() {
          @Override
          public void accept(GridOptions gridOptions, PresenterCallback callback) {
            GridFactory.openGrid("NewsDocuments", null, gridOptions, callback);
          }
        });


    BeeKeeper.getBus().registerRowActionHandler(new RowActionEvent.Handler() {
      @Override
      public void onRowAction(RowActionEvent event) {
        if (event.isEditRow() && event.hasView(VIEW_DOCUMENT_FILES)) {
          event.consume();

          if (event.hasRow() && event.getOpener() != null) {
            Long documentId = Data.getLong(event.getViewName(), event.getRow(), COL_DOCUMENT);
            RowEditor.open(VIEW_DOCUMENTS, documentId, event.getOpener());
          }
        }
      }
    });
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
