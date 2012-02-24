package com.butent.bee.client.presenter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsDate;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.Queries.RowCallback;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.utils.EvalHelper;
import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

public class TreePresenter implements Presenter {

  private static class Evaluator {
    private final JavaScriptObject interpreter;
    private JavaScriptObject rowValues;
    private List<? extends IsColumn> columns;

    public Evaluator(Calculation calc, String itemName) {
      JavaScriptObject jso = null;

      if (calc != null) {
        if (!BeeUtils.isEmpty(calc.getExpression())) {
          jso = createInterpreter(calc.getExpression(), true);
        } else if (!BeeUtils.isEmpty(calc.getFunction())) {
          jso = createInterpreter(calc.getFunction(), false);
        }
      }
      if (jso == null) {
        jso = createInterpreter(
            BeeUtils.isEmpty(itemName) ? "'ID=' + rowId" : "row." + itemName, true);
      }
      this.interpreter = jso;
    }

    public String evaluate(IsRow item) {
      EvalHelper.toJso(columns, item, rowValues);
      return doEval(interpreter, rowValues, item.getId(), JsDate.create(item.getVersion()));
    }

    public List<? extends IsColumn> getColumns() {
      return columns;
    }

    public void setColumns(List<? extends IsColumn> columns) {
      this.columns = columns;
      this.rowValues = EvalHelper.createJso(columns);
    }

    private JavaScriptObject createInterpreter(String xpr, boolean isExpression) {
      String body;

      if (isExpression) {
        body = "return " + xpr + ";";
      } else {
        body = xpr;
      }
      return JsUtils.createFunction("row, rowId, rowVersion", body);
    }

    private native String doEval(JavaScriptObject fnc, JavaScriptObject row, double rowId,
        JsDate rowVersion) /*-{
      try {
        var result = fnc(row, rowId, rowVersion);
        if (result == null) {
          return result;
        }
        if (result.getTime) {
          return String(result.getTime());
        }
        return String(result);
      } catch (err) {
        return err;
      }
    }-*/;
  }

  private final TreeView treeView;
  private final String source;
  private final String parentName;
  private final String itemName;
  private final Evaluator evaluator;
  private final String editor;
  private final String creator;

  public TreePresenter(TreeView view, String source, String parentName,
      String itemName, Calculation calc, String editorForm, String creatorForm) {
    this.treeView = view;
    this.source = source;
    this.evaluator = new Evaluator(calc, itemName);
    this.parentName = parentName;
    this.itemName = itemName;
    this.editor = editorForm;
    this.creator = BeeUtils.ifString(creatorForm, editorForm);

    requery();
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

  private void addBranch(Long parentId, Map<Long, List<Long>> hierarchy, Map<Long, IsRow> items) {
    List<Long> branch = hierarchy.get(parentId);

    if (branch != null) {
      for (Long leaf : branch) {
        IsRow item = items.get(leaf);
        getView().addItem(parentId, evaluator.evaluate(item), item);
        addBranch(item.getId(), hierarchy, items);
      }
    }
  }

  private void addItem() {
    if (!BeeUtils.isEmpty(creator)) {
      BeeKeeper.getScreen().notifySevere("Creator form not implemented");
      return;

    } else if (!BeeUtils.isEmpty(itemName)) {
      final Long parentId;
      String prompt = null;
      IsRow parent = getView().getSelectedItem();

      if (parent != null) {
        parentId = parent.getId();
        prompt = evaluator.evaluate(parent);
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
          columns.add(new BeeColumn(itemName));
          values.add(value);

          Queries.insert(source, columns, values, new RowCallback() {
            @Override
            public void onFailure(String[] reason) {
              BeeKeeper.getScreen().notifySevere(reason);
            }

            @Override
            public void onSuccess(BeeRow result) {
              getView().addItem(parentId, evaluator.evaluate(result), result);
            }
          });
        }
      });
    } else {
      BeeKeeper.getScreen().notifySevere("Creator form or item name not specified");
      return;
    }
  }

  private void editItem() {
    if (!BeeUtils.isEmpty(editor)) {
      BeeKeeper.getScreen().notifySevere("Editor form not implemented");
      return;

    } else if (!BeeUtils.isEmpty(itemName)) {
      final IsRow item = getView().getSelectedItem();
      if (item == null) {
        return;
      }
      int itemIndex = DataUtils.getColumnIndex(itemName, evaluator.getColumns());

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
                  getView().updateItem(evaluator.evaluate(result), result);
                }
              });
        }
      }, oldValue);
    } else {
      BeeKeeper.getScreen().notifySevere("Editor form or item name not specified");
      return;
    }
  }

  private TreeView getView() {
    return treeView;
  }

  private void requery() {
    Queries.getRowSet(source, null, new RowSetCallback() {
      @Override
      public void onFailure(String[] reason) {
        BeeKeeper.getScreen().notifySevere(reason);
      }

      @Override
      public void onSuccess(BeeRowSet result) {
        evaluator.setColumns(result.getColumns());
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
