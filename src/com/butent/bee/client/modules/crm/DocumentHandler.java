package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.presenter.GridFormPresenter;
import com.butent.bee.client.presenter.TreePresenter;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.utils.FileUtils.FileInfo;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.widget.InputFile;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class DocumentHandler {

  private static class CreationHandler extends AbstractFormCallback {

    private static final BeeLogger logger = LogUtils.getLogger(CreationHandler.class);

    private final FileCollector collector = new FileCollector();

    private CreationHandler() {
    }

    @Override
    public void afterCreateWidget(String name, Widget widget, WidgetDescriptionCallback callback) {
      if (widget instanceof GridPanel) {
        ((GridPanel) widget).setGridCallback(getCollector());
      } else if (widget instanceof InputFile) {
        getCollector().setInputWidget((InputFile) widget);
      }
    }

    @Override
    public FormCallback getInstance() {
      return new CreationHandler();
    }

    @Override
    public boolean onPrepareForInsert(final FormView form, final DataView dataView, IsRow row) {
      Assert.noNulls(dataView, row);

      if (getCollector().getFiles().isEmpty()) {
        dataView.notifyWarning("Pasirinkite bylas");
        return false;
      }

      List<BeeColumn> columns = Lists.newArrayList();
      List<String> values = Lists.newArrayList();

      for (BeeColumn column : form.getDataColumns()) {
        if (!column.isWritable()) {
          continue;
        }

        String colName = column.getId();
        String value = row.getString(form.getDataIndex(colName));

        if (!BeeUtils.isEmpty(value)) {
          columns.add(column);
          values.add(value);
        } else if (BeeUtils.inListSame(colName, CrmConstants.COL_DOCUMENT_DATE,
            CrmConstants.COL_TYPE, CrmConstants.COL_GROUP, CrmConstants.COL_CATEGORY,
            CrmConstants.COL_NAME)) {
          dataView.notifySevere(colName + ": value required");
          return false;
        }
      }

      Queries.insert(DOCUMENT_VIEW_NAME, columns, values, new RowCallback() {
        @Override
        public void onFailure(String... reason) {
          dataView.notifySevere(reason);
        }

        @Override
        public void onSuccess(BeeRow result) {
          dataView.finishNewRow(result);
          sendFiles(result.getId());
        }
      });
      return false;
    }

    @Override
    public void onStartNewRow(final FormView form, IsRow oldRow, final IsRow newRow) {
      getCollector().clear();

      newRow.setValue(form.getDataIndex(CrmConstants.COL_DOCUMENT_DATE),
          System.currentTimeMillis());

      if (oldRow != null) {
        copyValues(form, oldRow, newRow,
            Lists.newArrayList(CrmConstants.COL_TYPE, CrmConstants.COL_TYPE_NAME,
                CrmConstants.COL_GROUP, CrmConstants.COL_GROUP_NAME,
                CrmConstants.COL_CATEGORY, CrmConstants.COL_CATEGORY_NAME));

      } else if (form.getViewPresenter() instanceof GridFormPresenter) {
        GridCallback gcb = ((GridFormPresenter) form.getViewPresenter()).getGridCallback();

        if (gcb instanceof DocumentGridHandler) {
          IsRow category = ((DocumentGridHandler) gcb).getSelectedCategory();

          if (category != null) {
            newRow.setValue(form.getDataIndex(CrmConstants.COL_CATEGORY), category.getId());
            newRow.setValue(form.getDataIndex(CrmConstants.COL_CATEGORY_NAME),
                ((DocumentGridHandler) gcb).getCategoryValue(category, CrmConstants.COL_NAME));
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

    private void sendFiles(long docId) {
      List<BeeColumn> columns =
          Lists.newArrayList(new BeeColumn(ValueType.LONG, CrmConstants.COL_DOCUMENT),
              new BeeColumn(ValueType.DATETIME, CrmConstants.COL_FILE_DATE),
              new BeeColumn(ValueType.TEXT, CrmConstants.COL_NAME),
              new BeeColumn(ValueType.TEXT, CrmConstants.COL_MIME));

      String[] values = new String[columns.size()];
      values[0] = BeeUtils.toString(docId);

      int dateIndex = 1;
      int nameIndex = 2;
      int mimeIndex = 3;

      for (FileInfo info : getCollector().getFiles().values()) {
        values[dateIndex] = TimeUtils.normalize(info.getLastModifiedDate());
        values[nameIndex] = BeeUtils.trim(info.getName());
        values[mimeIndex] = BeeUtils.trim(info.getType());

        Queries.insert(FILE_VIEW_NAME, columns, Lists.newArrayList(values), null);
      }
    }
  }

  private static class DocumentGridHandler extends AbstractGridCallback implements
      SelectionHandler<IsRow> {

    private static final String FILTER_KEY = "f1";
    private IsRow selectedCategory = null;
    private TreePresenter categoryTree = null;

    @Override
    public void afterCreateWidget(String name, Widget widget, WidgetDescriptionCallback callback) {
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
        return ComparisonFilter.isEqual(CrmConstants.COL_CATEGORY, new LongValue(category));
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
    GridFactory.registerGridCallback("Documents", new DocumentGridHandler());
    FormFactory.registerFormCallback("NewDocument", new CreationHandler());
  }

  private DocumentHandler() {
  }
}
