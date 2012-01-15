package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.presenter.GridFormPresenter;
import com.butent.bee.client.tree.BeeTree;
import com.butent.bee.client.tree.BeeTreeItem;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.utils.FileUtils.FileInfo;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.widget.InputFile;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.List;
import java.util.Map;

public class DocumentHandler {

  private static class CreationHandler extends AbstractFormCallback {

    private FileCollector collector = null;

    private CreationHandler() {
    }

    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (widget instanceof GridPanel) {
        ((GridPanel) widget).setGridCallback(getCollector());
      } else if (widget instanceof InputFile) {
        getCollector().setInputWidget((InputFile) widget);
      }
    }
    
    @Override
    public CreationHandler getInstance() {
      CreationHandler instance = new CreationHandler();
      instance.setCollector(new FileCollector());
      return instance;
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
        String colName = column.getId();
        if (form.isForeign(colName)) {
          continue;
        }
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

      Queries.insert(DOCUMENT_VIEW_NAME, columns, values, new Queries.RowCallback() {
        public void onFailure(String[] reason) {
          dataView.notifySevere(reason);
        }

        public void onSuccess(BeeRow result) {
          dataView.finishNewRow(result);
          sendFiles(result.getId());
        }
      });
      return false;
    }

    @Override
    public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
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
          IsRow categoryRow = ((DocumentGridHandler) gcb).getSelectedCategory();
          if (categoryRow != null) {
            newRow.setValue(form.getDataIndex(CrmConstants.COL_CATEGORY), categoryRow.getId());
            newRow.setValue(form.getDataIndex(CrmConstants.COL_CATEGORY_NAME),
                categoryRow.getString(treeViewNameIndex));
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
          BeeKeeper.getLog().warning("copyValues: column", colName, "not found");
        }
      }
    }
    
    private FileCollector getCollector() {
      return collector;
    }

    private void setCollector(FileCollector collector) {
      this.collector = collector;
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

        Queries.insert(FILE_VIEW_NAME, columns, Lists.newArrayList(values),
            new Queries.RowCallback() {
              public void onFailure(String[] reason) {
                BeeKeeper.getScreen().notifySevere(reason);
              }

              public void onSuccess(BeeRow result) {
              }
            });
      }
    }
  }

  private static class DocumentGridHandler extends AbstractGridCallback implements
      SelectionHandler<TreeItem> {

    private static final String FILTER_KEY = "f1";
    private IsRow selectedCategory = null;

    private DocumentGridHandler() {
    }

    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (widget instanceof BeeTree) {
        populateTree((BeeTree) widget, new BeeTreeItem(ROOT_LABEL));
        ((BeeTree) widget).addSelectionHandler(this);
      }
    }

    @Override
    public DocumentGridHandler getInstance() {
      return new DocumentGridHandler();
    }

    public void onSelection(SelectionEvent<TreeItem> event) {
      if (event != null && getGridPresenter() != null) {
        Long category;
        if (event.getSelectedItem().getUserObject() instanceof IsRow) {
          setSelectedCategory((IsRow) event.getSelectedItem().getUserObject());
          category = getSelectedCategory().getId();
        } else {
          setSelectedCategory(null);
          category = null;
        }

        getGridPresenter().getDataProvider().setParentFilter(FILTER_KEY, getFilter(category));
        getGridPresenter().requery(true);
      }
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

  private static class TreeHandler extends AbstractFormCallback {

    private static int counter = 0;

    private BeeTree treeWidget = null;
    private final BeeTreeItem rootItem = new BeeTreeItem(ROOT_LABEL);

    private TreeHandler() {
    }

    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (widget instanceof BeeTree) {
        setTreeWidget((BeeTree) widget);
        refresh();
      }
    }

    @Override
    public boolean beforeAction(Action action, FormPresenter presenter) {
      boolean result;

      switch (action) {
        case REFRESH:
          refresh();
          result = false;
          break;

        case ADD:
          add();
          result = false;
          break;

        case DELETE:
          result = false;
          break;

        case EDIT:
          result = false;
          break;

        default:
          result = true;
      }
      return result;
    }

    @Override
    public FormCallback getInstance() {
      if (counter++ == 0) {
        return this;
      } else {
        return new TreeHandler();
      }
    }

    private void add() {
      if (getTreeWidget() == null || TREE_VIEW_COLUMNS.isEmpty()) {
        return;
      }

      final String name = Window.prompt("Nauja kategorija:", BeeConst.STRING_EMPTY);
      if (BeeUtils.isEmpty(name)) {
        return;
      }

      final TreeItem parentItem = (getTreeWidget().getSelectedItem() == null)
          ? getRootItem() : getTreeWidget().getSelectedItem();

      Long pId = getItemId(parentItem);

      int ord = 0;
      for (int i = 0; i < parentItem.getChildCount(); i++) {
        ord = Math.max(ord, BeeUtils.toInt(getString(parentItem.getChild(i), treeViewOrderIndex)));
      }

      List<BeeColumn> cols = Lists.newArrayList();
      List<String> values = Lists.newArrayList();

      for (BeeColumn column : TREE_VIEW_COLUMNS) {
        String columnId = column.getId();

        if (BeeUtils.same(columnId, CrmConstants.COL_NAME)) {
          cols.add(column);
          values.add(name.trim());
        } else if (BeeUtils.same(columnId, CrmConstants.COL_ORDER)) {
          cols.add(column);
          values.add(BeeUtils.toString(++ord));
        } else if (BeeUtils.same(columnId, CrmConstants.COL_PARENT) && pId != null) {
          cols.add(column);
          values.add(BeeUtils.toString(pId));
        }
      }

      Queries.insert(TREE_VIEW_NAME, cols, values, new Queries.RowCallback() {
        public void onFailure(String[] reason) {
          BeeKeeper.getScreen().notifySevere(reason);
        }

        public void onSuccess(BeeRow result) {
          parentItem.addItem(createTreeItem(result));
          if (!parentItem.getState()) {
            parentItem.setState(true);
          }
        }
      });
    }

    private BeeTreeItem getRootItem() {
      return rootItem;
    }

    private String getString(TreeItem item, int index) {
      if (item.getUserObject() instanceof IsRow && index >= 0) {
        return ((IsRow) item.getUserObject()).getString(index);
      } else {
        return null;
      }
    }

    private BeeTree getTreeWidget() {
      return treeWidget;
    }

    private void refresh() {
      if (getTreeWidget() == null) {
        return;
      }
      populateTree(getTreeWidget(), getRootItem());
    }

    private void setTreeWidget(BeeTree treeWidget) {
      this.treeWidget = treeWidget;
    }
  }

  private static final String TREE_VIEW_NAME = "DocumentTree";
  private static final String DOCUMENT_VIEW_NAME = "Documents";
  private static final String FILE_VIEW_NAME = "Files";

  private static final String ROOT_LABEL = "Kategorijos";
  private static final List<BeeColumn> TREE_VIEW_COLUMNS = Lists.newArrayList();

  private static int treeViewParentIndex = BeeConst.UNDEF;
  private static int treeViewOrderIndex = BeeConst.UNDEF;
  private static int treeViewNameIndex = BeeConst.UNDEF;
  private static int treeViewCountIndex = BeeConst.UNDEF;

  public static void register() {
    FormFactory.registerFormCallback("DocumentTree", new TreeHandler());
    GridFactory.registerGridCallback("Documents", new DocumentGridHandler());

    FormFactory.registerFormCallback("NewDocument", new CreationHandler());
  }

  private static BeeTreeItem createTreeItem(IsRow row) {
    String html = BeeUtils.concat(1, row.getString(treeViewOrderIndex),
        row.getString(treeViewNameIndex));

    int count = BeeUtils.toInt(row.getString(treeViewCountIndex));
    if (count > 0) {
      html = html + BeeConst.STRING_SPACE + BeeUtils.bracket(count);
    }

    return new BeeTreeItem(html, row);
  }

  private static Long getItemId(TreeItem item) {
    if (item != null && item.getUserObject() instanceof IsRow) {
      return ((IsRow) item.getUserObject()).getId();
    } else {
      return null;
    }
  }

  private static void populateTree(final BeeTree tree, final BeeTreeItem root) {
    Queries.getRowSet(TREE_VIEW_NAME, null, new Queries.RowSetCallback() {
      public void onFailure(String[] reason) {
        BeeKeeper.getScreen().notifySevere(reason);
      }

      public void onSuccess(BeeRowSet result) {
        setTreeViewColumns(result.getColumns());

        tree.clear();
        root.removeItems();
        tree.addItem(root);

        if (!result.isEmpty()) {
          Map<Long, TreeItem> treeItems = Maps.newHashMap();
          List<IsRow> pendingRows = Lists.newArrayList();

          for (IsRow row : result.getRows()) {
            Long pId = row.getLong(treeViewParentIndex);
            if (pId == null) {
              BeeTreeItem item = createTreeItem(row);
              root.addItem(item);
              treeItems.put(row.getId(), item);
            } else {
              pendingRows.add(row);
            }
          }

          while (!pendingRows.isEmpty()) {
            int cnt = pendingRows.size();
            List<IsRow> rows = Lists.newArrayList(pendingRows);
            pendingRows.clear();

            for (IsRow row : rows) {
              Long pId = row.getLong(treeViewParentIndex);
              TreeItem parentItem = treeItems.get(pId);

              if (parentItem == null) {
                pendingRows.add(row);
              } else {
                BeeTreeItem item = createTreeItem(row);
                parentItem.addItem(item);
                treeItems.put(row.getId(), item);
              }
            }
            if (pendingRows.size() >= cnt) {
              break;
            }
          }

          if (!root.getState()) {
            root.setState(true);
          }
        }
      }
    });
  }

  private static void setTreeViewColumns(List<BeeColumn> columns) {
    if (TREE_VIEW_COLUMNS.isEmpty()) {
      TREE_VIEW_COLUMNS.addAll(columns);

      treeViewParentIndex = DataUtils.getColumnIndex(CrmConstants.COL_PARENT, columns);
      treeViewOrderIndex = DataUtils.getColumnIndex(CrmConstants.COL_ORDER, columns);
      treeViewNameIndex = DataUtils.getColumnIndex(CrmConstants.COL_NAME, columns);
      treeViewCountIndex = DataUtils.getColumnIndex(CrmConstants.COL_DOCUMENT_COUNT, columns);
    }
  }

  private DocumentHandler() {
  }
}
