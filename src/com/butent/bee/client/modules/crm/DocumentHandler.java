package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.Callback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.presenter.GridFormPresenter;
import com.butent.bee.client.presenter.TreePresenter;
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
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
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
        event.getCallback().onFailure("Pasirinkite bylas");
        return;
      }
      
      List<String> required = Lists.newArrayList(COL_DOCUMENT_DATE, COL_TYPE, COL_GROUP,
          COL_CATEGORY, COL_NAME);
      List<String> empty = Lists.newArrayList();
      
      for (String colName : required) {
        if (!DataUtils.contains(event.getColumns(), colName)) {
          empty.add(colName);
        }
      }
      
      if (!empty.isEmpty()) {
        event.getCallback().onFailure(empty.toString(), "value required");
        return;
      }

      Queries.insert(DOCUMENT_VIEW_NAME, event.getColumns(), event.getValues(), new RowCallback() {
        @Override
        public void onFailure(String... reason) {
          event.getCallback().onFailure(reason);
        }

        @Override
        public void onSuccess(BeeRow result) {
          event.getCallback().onSuccess(result);
          sendFiles(result.getId());
        }
      });
    }

    @Override
    public void onStartNewRow(final FormView form, IsRow oldRow, final IsRow newRow) {
      if (getCollector() != null) {
        getCollector().clear();
      }

      newRow.setValue(form.getDataIndex(COL_DOCUMENT_DATE), TimeUtils.nextHour(-1));

      if (oldRow != null) {
        copyValues(form, oldRow, newRow,
            Lists.newArrayList(COL_TYPE, COL_TYPE_NAME, COL_GROUP, COL_GROUP_NAME,
                COL_CATEGORY, COL_CATEGORY_NAME));

      } else if (form.getViewPresenter() instanceof GridFormPresenter) {
        GridInterceptor gcb = ((GridFormPresenter) form.getViewPresenter()).getGridInterceptor();

        if (gcb instanceof DocumentGridHandler) {
          IsRow category = ((DocumentGridHandler) gcb).getSelectedCategory();

          if (category != null) {
            newRow.setValue(form.getDataIndex(COL_CATEGORY), category.getId());
            newRow.setValue(form.getDataIndex(COL_CATEGORY_NAME),
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

    private void sendFiles(final long docId) {
      final String viewName = FILE_VIEW_NAME;
      final List<BeeColumn> columns = Data.getColumns(viewName);

      for (final NewFileInfo fileInfo : getCollector().getFiles()) {
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
            
            Queries.insert(viewName, columns, row, null);
          }
        });
      }
    }
  }

  private static class DocumentGridHandler extends AbstractGridInterceptor implements
      SelectionHandler<IsRow> {

    private static final String FILTER_KEY = "f1";
    private IsRow selectedCategory = null;
    private TreePresenter categoryTree = null;

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
        Long category = null;
        setSelectedCategory(event.getSelectedItem());

        if (getSelectedCategory() != null) {
          category = getSelectedCategory().getId();
        }
        getGridPresenter().getDataProvider().setParentFilter(FILTER_KEY, getFilter(category));
        getGridPresenter().refresh(true);
      }
    }

    private String getCategoryValue(IsRow category, String colName) {
      if (BeeUtils.allNotNull(category, categoryTree, categoryTree.getDataColumns())) {
        return category.getString(DataUtils.getColumnIndex(colName, categoryTree.getDataColumns()));
      }
      return null;
    }

    private Filter getFilter(Long category) {
      if (category == null) {
        return null;
      } else {
        return ComparisonFilter.isEqual(COL_CATEGORY, new LongValue(category));
      }
    }

    private IsRow getSelectedCategory() {
      return selectedCategory;
    }

    private void setSelectedCategory(IsRow selectedCategory) {
      this.selectedCategory = selectedCategory;
    }
  }

  private static final String DOCUMENT_VIEW_NAME = "Documents";
  private static final String FILE_VIEW_NAME = "DocumentFiles";

  public static void register() {
    GridFactory.registerGridInterceptor("Documents", new DocumentGridHandler());
    FormFactory.registerFormInterceptor("NewDocument", new DocumentBuilder());
  }

  private DocumentHandler() {
  }
}
