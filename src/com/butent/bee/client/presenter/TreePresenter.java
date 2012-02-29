package com.butent.bee.client.presenter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.Queries.RowCallback;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.form.FormImpl;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

public class TreePresenter implements Presenter {

  private final TreeView treeView;
  private final String source;
  private final String parentName;
  private final String itemName;
  private final String relName;
  private final Calculation calculation;
  private List<BeeColumn> dataColumns = null;
  private Evaluator evaluator = null;
  private final Element editor;
  private Long relId = null;
  private FormView formView = null;

  public TreePresenter(TreeView view, String source, String parentName,
      String itemName, String relName, Calculation calc, Element form) {

    this.treeView = view;
    this.source = source;
    this.parentName = parentName;
    this.itemName = itemName;
    this.relName = relName;
    this.editor = form;

    String expr = BeeUtils.isEmpty(itemName) ? "'ID=' + rowId" : "row." + itemName;

    if (calc == null) {
      this.calculation = new Calculation(expr, null, null);
    } else {
      this.calculation = new Calculation(calc.hasExpressionOrFunction() ? calc.getExpression()
          : expr, calc.getFunction(), calc.getLambda());
    }
    if (BeeUtils.isEmpty(relName)) {
      requery();
    }
  }

  public List<BeeColumn> getDataColumns() {
    return dataColumns;
  }

  @Override
  public Widget getWidget() {
    return getView().asWidget();
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

      case REQUERY:
        requery();
        break;

      default:
        BeeKeeper.getLog().info(action, "not implemented");
    }
  }

  @Override
  public void onViewUnload() {
    getView().setViewPresenter(null);
  }

  public void updateRelation(Long parentId) {
    this.relId = parentId;
    requery();
  }

  private void addBranch(Long parentId, Map<Long, List<Long>> hierarchy, Map<Long, IsRow> items) {
    List<Long> branch = hierarchy.get(parentId);

    if (branch != null) {
      for (Long leaf : branch) {
        IsRow item = items.get(leaf);
        getView().addItem(parentId, evaluate(item), item);
        addBranch(item.getId(), hierarchy, items);
      }
    }
  }

  private void addItem() {
    if (!BeeUtils.isEmpty(relName) && relId == null) {
      return;
    }
    if (!BeeUtils.isEmpty(itemName)) {
      final Long parentId;
      String prompt = null;
      IsRow parent = getView().getSelectedItem();

      if (parent != null) {
        parentId = parent.getId();
        prompt = evaluate(parent);
      } else {
        parentId = null;
      }
      Global.inputString(source, prompt, new StringCallback(true) {
        @Override
        public void onSuccess(String value) {
          List<BeeColumn> columns = Lists.newArrayList();
          List<String> values = Lists.newArrayList();

          if (parentId != null) {
            columns.add(new BeeColumn(parentName));
            values.add(BeeUtils.toString(parentId));
          }
          if (!BeeUtils.isEmpty(relName)) {
            columns.add(new BeeColumn(relName));
            values.add(BeeUtils.toString(relId));
          }
          columns.add(new BeeColumn(itemName));
          values.add(value);

          Queries.insert(source, columns, values, new RowCallback() {
            @Override
            public void onFailure(String[] reason) {
              BeeKeeper.getScreen().notifySevere(reason);
            }

            @Override
            public void onSuccess(BeeRow result) {
              getView().addItem(parentId, evaluate(result), result);
            }
          });
        }
      });

    } else if (!BeeUtils.isEmpty(editor)) {
      BeeKeeper.getScreen().notifySevere("Editor form not implemented");
      return;

    } else {
      BeeKeeper.getScreen().notifySevere("Creator form or item name not specified");
      return;
    }
  }

  private void editItem() {
    if (!BeeUtils.isEmpty(relName) && relId == null) {
      return;
    }
    if (!BeeUtils.isEmpty(editor)) {
      if (getView().getSelectedItem() == null) {
        return;
      }
      if (formView == null) {
        formView = new FormImpl();
        formView.create(new FormDescription(editor), getDataColumns(), null);
      }
      formView.updateRow(getView().getSelectedItem(), false);

      DialogBox dialog = new DialogBox(formView.getCaption());
      dialog.setAnimationEnabled(true);

      dialog.setWidget(formView);

      dialog.center();
      return;

    } else if (!BeeUtils.isEmpty(itemName)) {
      final IsRow item = getView().getSelectedItem();
      if (item == null) {
        return;
      }
      int itemIndex = DataUtils.getColumnIndex(itemName, getDataColumns());

      if (BeeUtils.equals(itemIndex, BeeConst.UNDEF)) {
        BeeKeeper.getScreen().notifySevere("Item column not found", itemName);
        return;
      }
      final String oldValue = item.getString(itemIndex);

      Global.inputString(source, null, new StringCallback(true) {
        @Override
        public void onSuccess(String value) {
          Queries.update(source, item.getId(), item.getVersion(),
              Lists.newArrayList(new BeeColumn(itemName)), Lists.newArrayList(oldValue),
              Lists.newArrayList(value), new RowCallback() {
                @Override
                public void onFailure(String[] reason) {
                  BeeKeeper.getScreen().notifySevere(reason);
                }

                @Override
                public void onSuccess(BeeRow result) {
                  getView().updateItem(evaluate(result), result);
                }
              });
        }
      }, oldValue);
    } else {
      BeeKeeper.getScreen().notifySevere("Editor form or item name not specified");
      return;
    }
  }

  private String evaluate(IsRow row) {
    if (BeeUtils.allNotEmpty(evaluator, row)) {
      evaluator.update(row);
      return evaluator.evaluate();
    }
    return null;
  }

  private TreeView getView() {
    return treeView;
  }

  private void requery() {
    Filter flt = null;

    if (!BeeUtils.isEmpty(relName)) {
      flt = ComparisonFilter.compareWithValue(relName, Operator.EQ,
          new LongValue(relId == null ? BeeConst.UNDEF : relId));
    }
    Queries.getRowSet(source, null, flt, null, new RowSetCallback() {
      @Override
      public void onFailure(String[] reason) {
        BeeKeeper.getScreen().notifySevere(reason);
      }

      @Override
      public void onSuccess(BeeRowSet result) {
        if (evaluator == null) {
          dataColumns = result.getColumns();
          evaluator = Evaluator.create(calculation, itemName, getDataColumns());
        }
        getView().removeItems();

        if (result.isEmpty()) {
          return;
        }
        int parentIndex = result.getColumnIndex(parentName);

        if (BeeUtils.equals(parentIndex, BeeConst.UNDEF)) {
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

  private void removeItem() {
    final IsRow data = getView().getSelectedItem();

    if (data != null) {
      Global.confirm("Remove item?", new BeeCommand() {
        @Override
        public void execute() {
          Queries.deleteRow(source, data.getId(), data.getVersion(),
              new IntCallback() {
                @Override
                public void onFailure(String[] reason) {
                  BeeKeeper.getScreen().notifySevere(reason);
                }

                @Override
                public void onSuccess(Integer result) {
                  getView().removeItem(data);
                }
              });
        }
      });
    }
  }
}
