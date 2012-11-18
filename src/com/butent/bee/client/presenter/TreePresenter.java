package com.butent.bee.client.presenter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.logical.CatchEvent;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.view.form.FormImpl;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

public class TreePresenter extends AbstractPresenter implements CatchEvent.CatchHandler<IsRow> {

  private class CommitCallback extends RowCallback {
    private final boolean createMode;

    public CommitCallback(boolean createMode) {
      this.createMode = createMode;
    }

    @Override
    public void onSuccess(BeeRow result) {
      String text = evaluate(result);

      if (createMode) {
        Long parentId = result.getLong(DataUtils.getColumnIndex(parentName, getDataColumns()));
        getView().addItem(parentId, text, result, true);
        BeeKeeper.getBus().fireEvent(new RowInsertEvent(source, result));
      } else {
        getView().updateItem(text, result);
        BeeKeeper.getBus().fireEvent(new RowUpdateEvent(source, result));
      }
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(TreePresenter.class);
  
  private final TreeView treeView;
  private final String source;
  private final String parentName;
  @SuppressWarnings("unused")
  private final String orderName; // TODO implement order column
  private final String relationName;
  private Long relationId = null;
  private final Calculation calculation;
  private List<BeeColumn> dataColumns = null;
  private Evaluator evaluator = null;
  private final Element editor;
  private FormView formView = null;

  public TreePresenter(TreeView view, String source, String parentName,
      String orderName, String relationName, Calculation calc, Element editorForm) {
    Assert.notNull(view);
    Assert.notEmpty(source);

    this.treeView = view;
    this.source = source;
    this.parentName = parentName;
    this.orderName = orderName;
    this.relationName = relationName;
    this.editor = editorForm;

    String expr = "'ID=' + rowId";

    if (calc == null) {
      this.calculation = new Calculation(expr, null);
    } else {
      this.calculation = new Calculation(calc.hasExpressionOrFunction() ? calc.getExpression()
          : expr, calc.getFunction());
    }
    getView().addCatchHandler(this);

    if (BeeUtils.isEmpty(relationName)) {
      requery();
    }
  }

  @Override
  public String getCaption() {
    return null;
  }

  public List<BeeColumn> getDataColumns() {
    return dataColumns;
  }

  @Override
  public HeaderView getHeader() {
    return null;
  }
  
  @Override
  public View getMainView() {
    return getView();
  }

  @Override
  public IdentifiableWidget getWidget() {
    return getView();
  }

  @Override
  public void handleAction(Action action) {
    Assert.notNull(action);

    switch (action) {
      case ADD:
        if (getView().isEnabled()) {
          addItem();
        }
        break;

      case DELETE:
        if (getView().isEnabled()) {
          removeItem();
        }
        break;

      case EDIT:
        if (getView().isEnabled()) {
          editItem();
        }
        break;

      case REFRESH:
        requery();
        break;

      default:
        logger.info(action, "not implemented");
    }
  }

  @Override
  public void onCatch(CatchEvent<IsRow> event) {
    IsRow item = event.getPacket();
    int parentIndex = DataUtils.getColumnIndex(parentName, getDataColumns());
    IsRow destination = event.getDestination();

    Queries.update(source, item.getId(), item.getVersion(),
        Lists.newArrayList(getDataColumns().get(parentIndex)),
        Lists.newArrayList(item.getString(parentIndex)),
        Lists.newArrayList(destination == null ? null : BeeUtils.toString(destination.getId())),
        new CommitCallback(false));
  }

  @Override
  public void onViewUnload() {
    getView().setViewPresenter(null);
    super.onViewUnload();
  }

  public void updateRelation(Long parentId) {
    this.relationId = parentId;
    requery();
  }

  private void addBranch(Long parentId, Map<Long, List<Long>> hierarchy, Map<Long, IsRow> items) {
    List<Long> branch = hierarchy.get(parentId);

    if (branch != null) {
      for (Long leaf : branch) {
        IsRow item = items.get(leaf);
        getView().addItem(parentId, evaluate(item), item, false);
        addBranch(item.getId(), hierarchy, items);
      }
    }
  }

  private void addItem() {
    if (canModify()) {
      edit(null);
    }
  }

  private boolean canModify() {
    boolean ok = true;

    if (!BeeUtils.isEmpty(relationName) && relationId == null) {
      ok = false;
    }
    if (ok && editor == null) {
      BeeKeeper.getScreen().notifySevere("Editor form not found");
      ok = false;
    }
    return ok;
  }

  private void edit(final IsRow item) {
    final boolean addMode = (item == null);
    final IsRow row;

    if (addMode) {
      row = DataUtils.createEmptyRow(getDataColumns().size());
    } else {
      String[] arr = new String[getDataColumns().size()];

      for (int i = 0; i < arr.length; i++) {
        arr[i] = item.getString(i);
      }
      row = new BeeRow(item.getId(), item.getVersion(), arr);
    }
    if (formView == null) {
      formView = new FormImpl(FormDescription.getName(editor));
      formView.create(new FormDescription(editor), null, getDataColumns(), false, null);
      formView.setEditing(true);
      formView.start(null);
    }
    formView.updateRow(row, false);
    String caption;

    if (addMode) {
      caption = evaluate(getView().getSelectedItem());
    } else {
      caption = formView.getCaption();
    }
    Global.inputWidget(caption, formView, new InputCallback() {
      final List<BeeColumn> columns = Lists.newArrayList();
      final List<String> oldValues = Lists.newArrayList();
      final List<String> values = Lists.newArrayList();

      @Override
      public String getErrorMessage() {
        columns.clear();
        oldValues.clear();
        values.clear();

        for (int i = 0; i < getDataColumns().size(); i++) {
          if (addMode) {
            if (!BeeUtils.isEmpty(row.getString(i))) {
              columns.add(getDataColumns().get(i));
              values.add(row.getString(i));
            }
          } else {
            if (!Objects.equal(item.getString(i), row.getString(i))) {
              columns.add(getDataColumns().get(i));
              oldValues.add(item.getString(i));
              values.add(row.getString(i));
            }
          }
        }
        if (BeeUtils.isEmpty(columns)) {
          return "No changes";
        }
        return null;
      }

      @Override
      public void onClose(CloseCallback closeCallback) {
        formView.onClose(closeCallback);
      }

      @Override
      public void onSuccess() {
        if (addMode) {
          if (getView().getSelectedItem() != null) {
            columns.add(new BeeColumn(parentName));
            values.add(BeeUtils.toString(getView().getSelectedItem().getId()));
          }
          if (!BeeUtils.isEmpty(relationName)) {
            columns.add(new BeeColumn(relationName));
            values.add(BeeUtils.toString(relationId));
          }
          Queries.insert(source, columns, values, new CommitCallback(true));

        } else {
          Queries.update(source, row.getId(), row.getVersion(),
              columns, oldValues, values, new CommitCallback(false));
        }
      }
    });
  }

  private void editItem() {
    if (canModify() && getView().getSelectedItem() != null) {
      edit(getView().getSelectedItem());
    }
  }

  private String evaluate(IsRow row) {
    if (BeeUtils.allNotNull(evaluator, row)) {
      evaluator.update(row);
      return evaluator.evaluate();
    }
    return null;
  }

  private TreeView getView() {
    return treeView;
  }

  private void removeItem() {
    final IsRow data = getView().getSelectedItem();

    if (data != null) {
      String message = BeeUtils.joinWords("Išmesti", evaluate(data), "?");
      Global.getMsgBoxen().confirm(null, message, new ConfirmationCallback() {
        @Override
        public void onConfirm() {
          Queries.deleteRow(source, data.getId(), data.getVersion(),
              new IntCallback() {
                @Override
                public void onSuccess(Integer result) {
                  getView().removeItem(data);
                  BeeKeeper.getBus().fireEvent(new RowDeleteEvent(source, data.getId()));
                }
              });
        }
      }, StyleUtils.NAME_SCARY, null);
    }
  }

  private void requery() {
    Filter flt = null;

    if (!BeeUtils.isEmpty(relationName)) {
      flt = ComparisonFilter.compareWithValue(relationName, Operator.EQ,
          new LongValue(relationId == null ? BeeConst.UNDEF : relationId));
    }
    Queries.getRowSet(source, null, flt, null, new RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        if (evaluator == null) {
          dataColumns = result.getColumns();
          evaluator = Evaluator.create(calculation, null, getDataColumns());
        }
        getView().removeItems();

        if (result.isEmpty()) {
          return;
        }
        int parentIndex = result.getColumnIndex(parentName);

        if (Objects.equal(parentIndex, BeeConst.UNDEF)) {
          BeeKeeper.getScreen().notifySevere("Parent column not found", parentName);
          return;
        }
        Map<Long, List<Long>> hierarchy = Maps.newLinkedHashMap();
        Map<Long, IsRow> items = Maps.newHashMap();

        for (IsRow row : result.getRows()) {
          Long parent = row.getLong(parentIndex);
          List<Long> childs = hierarchy.get(parent);

          if (childs == null) {
            childs = Lists.newArrayList();
            hierarchy.put(parent, childs);
          }
          childs.add(row.getId());
          items.put(row.getId(), row);
        }
        for (Long parent : hierarchy.keySet()) {
          if (!items.containsKey(parent)) {
            addBranch(parent, hierarchy, items);
          }
        }
      }
    });
  }
}
