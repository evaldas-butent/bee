package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.tree.BeeTree;
import com.butent.bee.client.tree.BeeTreeItem;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.FileUtils.FileInfo;
import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.widget.BeeListBox;
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

import java.util.List;
import java.util.Map;

public class DocumentHandler {

  private static class CreationHandler extends AbstractFormCallback implements ChangeHandler,
      DragOverHandler, DropHandler {

    private final List<FileInfo> files = Lists.newArrayList();
    private BeeListBox infoWidget = null;

    private CreationHandler() {
    }

    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (BeeUtils.same(name, "form") && widget instanceof Flow) {
        ((Flow) widget).addDragOverHandler(this);
        ((Flow) widget).addDropHandler(this);
      } else if (widget instanceof InputFile) {
        ((InputFile) widget).addChangeHandler(this);
      } else if (BeeUtils.same(name, "info") && widget instanceof BeeListBox) {
        setInfoWidget((BeeListBox) widget);
      }
    }

    public List<FileInfo> getFiles() {
      return files;
    }

    @Override
    public void onChange(ChangeEvent event) {
      if (event.getSource() instanceof InputFile) {
        addFiles(FileUtils.getFileInfo(((InputFile) event.getSource())));
      }
    }

    public void onDragOver(DragOverEvent event) {
      EventUtils.eatEvent(event);
      JsUtils.setProperty(event.getDataTransfer(), "dropEffect", "copy");
    }

    public void onDrop(DropEvent event) {
      EventUtils.eatEvent(event);
      addFiles(FileUtils.getFileInfo(event.getDataTransfer()));
    }

    @Override
    public boolean onPrepareForInsert(final FormView form, final DataView dataView, IsRow row) {
      Assert.noNulls(dataView, row);

      if (getFiles().isEmpty()) {
        dataView.notifySevere("Pasirinkite bylas");
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
        } else if (BeeUtils.inListSame(colName, "DocumentDate", "Type", CrmConstants.COL_CATEGORY,
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
          sendFiles(result.getId(), result.getLong(form.getDataIndex(CrmConstants.COL_CATEGORY)));
        }
      });
      return false;
    }

    @Override
    public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
      getFiles().clear();
      refreshInfoWidget();

      newRow.setValue(form.getDataIndex("DocumentDate"), System.currentTimeMillis());
      if (oldRow != null) {
        copyValues(form, oldRow, newRow,
            Lists.newArrayList("Type", "TypeName", "Category", "CategoryName"));
      }
    }

    private void addFiles(List<FileInfo> fileInfos) {
      if (fileInfos == null || fileInfos.isEmpty()) {
        return;
      }

      int cnt = 0;
      for (FileInfo info : fileInfos) {
        if (!containsFile(info)) {
          getFiles().add(info);
          cnt++;
        }
      }

      if (cnt > 0) {
        refreshInfoWidget();
      }
    }

    private boolean containsFile(FileInfo info) {
      for (FileInfo fi : getFiles()) {
        if (BeeUtils.same(fi.getName(), info.getName())) {
          return true;
        }
      }
      return false;
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

    private BeeListBox getInfoWidget() {
      return infoWidget;
    }

    private void refreshInfoWidget() {
      if (getInfoWidget() == null) {
        return;
      }

      getInfoWidget().clear();
      if (getFiles().isEmpty()) {
        return;
      }

      for (FileInfo info : getFiles()) {
        getInfoWidget().addItem(BeeUtils.concat(BeeConst.DEFAULT_LIST_SEPARATOR, info.getName(),
            info.getSize(), info.getLastModifiedDate(), info.getType()));
      }
    }

    private void sendFiles(long docId, Long category) {
      List<BeeColumn> columns = Lists.newArrayList(new BeeColumn(ValueType.LONG, "Document"),
          new BeeColumn(ValueType.DATETIME, "FileDate"),
          new BeeColumn(ValueType.TEXT, CrmConstants.COL_NAME),
          new BeeColumn(ValueType.TEXT, "Mime"));

      String[] values = new String[columns.size()];
      values[0] = BeeUtils.toString(docId);

      int dateIndex = 1;
      int nameIndex = 2;
      int mimeIndex = 3;

      for (FileInfo info : getFiles()) {
        values[dateIndex] = (info.getLastModifiedDate() == null)
            ? null : info.getLastModifiedDate().serialize();
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

    private void setInfoWidget(BeeListBox infoWidget) {
      this.infoWidget = infoWidget;
    }
  }

  private static class DocumentGridHandler extends AbstractGridCallback implements
      SelectionHandler<TreeItem> {

    private static final String FILTER_KEY = "f1";

    private GridPresenter gridPresenter = null;

    private DocumentGridHandler() {
    }

    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (widget instanceof BeeTree) {
        populateTree((BeeTree) widget, new BeeTreeItem(ROOT_LABEL), null);
        ((BeeTree) widget).addSelectionHandler(this);
      }
    }

    public void onSelection(SelectionEvent<TreeItem> event) {
      if (event == null) {
        return;
      }
      if (getGridPresenter() != null) {
        getGridPresenter().getDataProvider().setParentFilter(FILTER_KEY,
            getFilter(getItemId(event.getSelectedItem())));
        getGridPresenter().requery(true);
      }
    }

    @Override
    public void onShow(GridPresenter presenter) {
      setGridPresenter(presenter);
    }

    private Filter getFilter(Long category) {
      if (category == null) {
        return null;
      } else {
        return ComparisonFilter.isEqual(CrmConstants.COL_CATEGORY, new LongValue(category));
      }
    }

    private GridPresenter getGridPresenter() {
      return gridPresenter;
    }

    private void setGridPresenter(GridPresenter gridPresenter) {
      this.gridPresenter = gridPresenter;
    }
  }

  private static class TreeHandler extends AbstractFormCallback {

    private static int counter = 0;

    private BeeTree treeWidget = null;
    private final BeeTreeItem rootItem = new BeeTreeItem(ROOT_LABEL);

    private final List<BeeColumn> columns = Lists.newArrayList();

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
      if (getTreeWidget() == null || getColumns().isEmpty()) {
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
        ord = Math.max(ord,
            BeeUtils.toInt(getString(parentItem.getChild(i), CrmConstants.COL_ORDER)));
      }

      List<BeeColumn> cols = Lists.newArrayList();
      List<String> values = Lists.newArrayList();

      for (BeeColumn column : getColumns()) {
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
          parentItem.addItem(new BeeTreeItem(name.trim(), result));
          if (!parentItem.getState()) {
            parentItem.setState(true);
          }
        }
      });
    }

    private List<BeeColumn> getColumns() {
      return columns;
    }

    private BeeTreeItem getRootItem() {
      return rootItem;
    }

    private String getString(TreeItem item, String columnId) {
      if (item.getUserObject() instanceof IsRow) {
        return ((IsRow) item.getUserObject()).getString(DataUtils.getColumnIndex(columnId,
            getColumns()));
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

      populateTree(getTreeWidget(), getRootItem(), new Queries.RowSetCallback() {
        public void onFailure(String[] reason) {
        }

        public void onSuccess(BeeRowSet result) {
          setColumns(result.getColumns());
        }
      });
    }

    private void setColumns(List<BeeColumn> cols) {
      if (!this.columns.isEmpty()) {
        this.columns.clear();
      }
      this.columns.addAll(cols);
    }

    private void setTreeWidget(BeeTree treeWidget) {
      this.treeWidget = treeWidget;
    }
  }

  private static final String TREE_VIEW_NAME = "DocumentTree";
  private static final String DOCUMENT_VIEW_NAME = "Documents";
  private static final String FILE_VIEW_NAME = "Files";

  private static final String ROOT_LABEL = "Kategorijos";

  public static void register() {
    FormFactory.registerFormCallback("DocumentTree", new TreeHandler());
    GridFactory.registerGridCallback("Documents", new DocumentGridHandler());

    FormFactory.registerFormCallback("NewDocument", new CreationHandler());
  }

  private static Long getItemId(TreeItem item) {
    if (item != null && item.getUserObject() instanceof IsRow) {
      return ((IsRow) item.getUserObject()).getId();
    } else {
      return null;
    }
  }

  private static void populateTree(final BeeTree tree, final BeeTreeItem root,
      final Queries.RowSetCallback rowSetCallback) {
    Queries.getRowSet(TREE_VIEW_NAME, null, new Queries.RowSetCallback() {
      public void onFailure(String[] reason) {
        BeeKeeper.getScreen().notifySevere(reason);
        if (rowSetCallback != null) {
          rowSetCallback.onFailure(reason);
        }
      }

      public void onSuccess(BeeRowSet result) {
        if (rowSetCallback != null) {
          rowSetCallback.onSuccess(result);
        }

        tree.clear();
        root.removeItems();
        tree.addItem(root);

        if (!result.isEmpty()) {
          int parentIndex = result.getColumnIndex(CrmConstants.COL_PARENT);
          int nameIndex = result.getColumnIndex(CrmConstants.COL_NAME);

          Map<Long, TreeItem> treeItems = Maps.newHashMap();
          List<IsRow> pendingRows = Lists.newArrayList();

          for (IsRow row : result.getRows()) {
            Long pId = row.getLong(parentIndex);
            if (pId == null) {
              BeeTreeItem item = new BeeTreeItem(row.getString(nameIndex), row);
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
              Long pId = row.getLong(parentIndex);
              TreeItem parentItem = treeItems.get(pId);

              if (parentItem == null) {
                pendingRows.add(row);
              } else {
                BeeTreeItem item = new BeeTreeItem(row.getString(nameIndex), row);
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

  private DocumentHandler() {
  }
}
