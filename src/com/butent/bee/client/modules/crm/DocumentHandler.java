package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.presenter.GridFormPresenter;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.TreePresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.FileLinkRenderer;
import com.butent.bee.client.render.FileSizeRenderer;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class DocumentHandler {

  private static class DocumentBuilder extends AbstractFormInterceptor {

    private static final BeeLogger logger = LogUtils.getLogger(DocumentBuilder.class);

    private FileCollector collector = null;

    private DocumentBuilder() {
    }

    @Override
    public void afterCreateWidget(String name, IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {
      if (widget instanceof FileCollector) {
        this.collector = (FileCollector) widget;
        this.collector.bindDnd(getFormView(), getFormView().asWidget().getElement());
      }
    }

    @Override
    public FormInterceptor getInstance() {
      return new DocumentBuilder();
    }

    @Override
    public void onReadyForInsert(final ReadyForInsertEvent event) {
      Assert.notNull(event);
      event.consume();

      if (getCollector() == null) {
        event.getCallback().onFailure("File collector not found");
        return;
      }

      if (getCollector().getFiles().isEmpty()) {
        event.getCallback().onFailure(Localized.constants.chooseFiles());
        return;
      }

      Queries.insert(DOCUMENT_VIEW_NAME, event.getColumns(), event.getValues(),
          event.getChildren(), new RowCallback() {
            @Override
            public void onFailure(String... reason) {
              event.getCallback().onFailure(reason);
            }

            @Override
            public void onSuccess(BeeRow result) {
              event.getCallback().onSuccess(result);
              sendFiles(result.getId(), getCollector().getFiles(), null);
            }
          });
    }

    @Override
    public void onStartNewRow(final FormView form, IsRow oldRow, final IsRow newRow) {
      if (getCollector() != null) {
        getCollector().clear();
      }

      if (oldRow != null) {
        copyValues(form, oldRow, newRow,
            Lists.newArrayList(COL_DOCUMENT_CATEGORY, COL_DOCUMENT_CATEGORY_NAME,
                COL_DOCUMENT_TYPE, COL_DOCUMENT_TYPE_NAME,
                COL_DOCUMENT_PLACE, COL_DOCUMENT_PLACE_NAME));

      } else if (form.getViewPresenter() instanceof GridFormPresenter) {
        GridInterceptor gcb = ((GridFormPresenter) form.getViewPresenter()).getGridInterceptor();

        if (gcb instanceof DocumentGridHandler) {
          IsRow category = ((DocumentGridHandler) gcb).getSelectedCategory();

          if (category != null) {
            newRow.setValue(form.getDataIndex(COL_DOCUMENT_CATEGORY), category.getId());
            newRow.setValue(form.getDataIndex(COL_DOCUMENT_CATEGORY_NAME),
                ((DocumentGridHandler) gcb).getCategoryValue(category, COL_NAME));
          }
        }
      }
    }

    private void copyValues(FormView form, IsRow oldRow, IsRow newRow, List<String> colNames) {
      for (String colName : colNames) {
        int index = form.getDataIndex(colName);
        if (index >= 0) {
          newRow.setValue(index, oldRow.getString(index));
        } else {
          logger.warning("copyValues: column", colName, "not found");
        }
      }
    }

    private FileCollector getCollector() {
      return collector;
    }
  }

  private static class DocumentGridHandler extends AbstractGridInterceptor implements
      SelectionHandler<IsRow> {

    private static final String FILTER_KEY = "f1";
    private IsRow selectedCategory = null;
    private TreePresenter categoryTree = null;

    private DocumentGridHandler() {
    }

    @Override
    public void afterCreateWidget(String name, IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {
      if (widget instanceof TreeView && BeeUtils.same(name, "Tree")) {
        ((TreeView) widget).addSelectionHandler(this);
        categoryTree = ((TreeView) widget).getTreePresenter();
      }
    }

    @Override
    public DocumentGridHandler getInstance() {
      return new DocumentGridHandler();
    }

    @Override
    public void onSelection(SelectionEvent<IsRow> event) {
      if (event != null && getGridPresenter() != null) {
        setSelectedCategory(event.getSelectedItem());
        Long category = (getSelectedCategory() == null) ? null : getSelectedCategory().getId();

        getGridPresenter().getDataProvider().setParentFilter(FILTER_KEY, getFilter(category));
        getGridPresenter().refresh(true);
      }
    }

    private String getCategoryValue(IsRow category, String colName) {
      if (BeeUtils.allNotNull(category, categoryTree)) {
        return category.getString(DataUtils.getColumnIndex(colName, categoryTree.getDataColumns()));
      }
      return null;
    }

    private Filter getFilter(Long category) {
      if (category == null) {
        return null;
      } else {
        return ComparisonFilter.isEqual(COL_DOCUMENT_CATEGORY, new LongValue(category));
      }
    }

    private IsRow getSelectedCategory() {
      return selectedCategory;
    }

    private void setSelectedCategory(IsRow selectedCategory) {
      this.selectedCategory = selectedCategory;
    }
  }

  private static class FileGridHandler extends AbstractGridInterceptor {

    private static final String STYLE_PREFIX = "bee-crm-DocumentAddFiles-";

    private FileGridHandler() {
    }

    @Override
    public boolean beforeAction(Action action, final GridPresenter presenter) {
      if (Action.ADD.equals(action)) {
        final Long docId = presenter.getGridView().getRelId();
        if (!DataUtils.isId(docId)) {
          return false;
        }

        Simple panel = new Simple();
        panel.addStyleName(STYLE_PREFIX + "panel");

        final FileCollector collector = new FileCollector(FileCollector.getDefaultFace(),
            FileCollector.ALL_COLUMNS, FileCollector.ALL_COLUMNS);
        panel.setWidget(collector);

        collector.bindDnd(panel, panel.getElement());

        Global.inputWidget("Naujos bylos", panel, new InputCallback() {
          @Override
          public String getErrorMessage() {
            return collector.getFiles().isEmpty() ? InputBoxes.SILENT_ERROR : null;
          }

          @Override
          public void onSuccess() {
            sendFiles(docId, collector.getFiles(), new ScheduledCommand() {
              @Override
              public void execute() {
                presenter.refresh(false);
              }
            });
          }
        }, STYLE_PREFIX + "dialog");

        return false;

      } else {
        return super.beforeAction(action, presenter);
      }
    }

    @Override
    public GridInterceptor getInstance() {
      return new FileGridHandler();
    }

    @Override
    public AbstractCellRenderer getRenderer(String columnName,
        List<? extends IsColumn> dataColumns, ColumnDescription columnDescription) {

      if (BeeUtils.same(columnName, COL_FILE)) {
        return new FileLinkRenderer(DataUtils.getColumnIndex(columnName, dataColumns),
            DataUtils.getColumnIndex(COL_CAPTION, dataColumns));

      } else if (BeeUtils.same(columnName, COL_FILE_SIZE)) {
        int index = DataUtils.getColumnIndex(columnName, dataColumns);
        return new FileSizeRenderer(CellSource.forColumn(dataColumns.get(index), index));
      
      } else {
        return super.getRenderer(columnName, dataColumns, columnDescription);
      }
    }
  }

  private static final String DOCUMENT_VIEW_NAME = "Documents";

  public static void register() {
    GridFactory.registerGridInterceptor("Documents", new DocumentGridHandler());
    GridFactory.registerGridInterceptor("DocumentFiles", new FileGridHandler());

    FormFactory.registerFormInterceptor("NewDocument", new DocumentBuilder());
  }

  private static void sendFiles(final Long docId, List<NewFileInfo> files,
      final ScheduledCommand onComplete) {

    final String viewName = VIEW_DOCUMENT_FILES;
    final List<BeeColumn> columns = Data.getColumns(viewName);

    final Holder<Integer> latch = Holder.of(files.size());

    for (final NewFileInfo fileInfo : files) {
      FileUtils.upload(fileInfo, new Callback<Long>() {
        @Override
        public void onSuccess(Long result) {
          BeeRow row = DataUtils.createEmptyRow(columns.size());

          Data.setValue(viewName, row, COL_DOCUMENT, docId);
          Data.setValue(viewName, row, COL_FILE, result);

          Data.setValue(viewName, row, COL_FILE_DATE,
              BeeUtils.nvl(fileInfo.getFileDate(), fileInfo.getLastModified()));
          Data.setValue(viewName, row, COL_FILE_VERSION, fileInfo.getFileVersion());

          Data.setValue(viewName, row, COL_CAPTION,
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

  private DocumentHandler() {
  }
}
