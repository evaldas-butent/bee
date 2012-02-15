package com.butent.bee.client.modules.commons;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.composite.MultiSelector.SelectionCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.presenter.GridFormPresenter;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommonEventHandler {

  private static class CategoryCollector {
    private Long itemId;
    private final List<Long> categories = Lists.newArrayList();
    private BeeListBox widget;

    private void addCategories() {
      CompoundFilter flt = null;

      if (!isEmpty()) {
        flt = Filter.and();

        for (Long categoryId : categories) {
          flt.add(ComparisonFilter.compareId("CategoryID", Operator.NE, categoryId));
        }
      }
      Queries.getRowSet(CommonsConstants.TBL_CATEGORIES, null, flt, null, new RowSetCallback() {
        @Override
        public void onFailure(String[] reason) {
          Global.showError((Object[]) reason);
        }

        @Override
        public void onSuccess(final BeeRowSet result) {
          if (result.isEmpty()) {
            Global.showError("No more heroes any more");
            return;
          }
          MultiSelector selector = new MultiSelector("Kategorijos", result,
              Lists.newArrayList(CommonsConstants.COL_NAME),
              new SelectionCallback() {
                @Override
                public void onSelection(List<IsRow> rows) {
                  if (!BeeUtils.isEmpty(itemId)) {
                    List<Long> categoryList = Lists.newArrayList();

                    for (IsRow row : rows) {
                      categoryList.add(row.getId());
                    }
                    doRequest(CommonsConstants.SVC_ADD_CATEGORIES, getCategories(categoryList));

                  } else {
                    updateCategories(rows, result.getColumnIndex(CommonsConstants.COL_NAME), null);
                  }
                }
              });
          selector.center();
        }
      });
    }

    private void doRequest(String service, String ids) {
      ParameterList args = createArgs(service);
      args.addDataItem(CommonsConstants.VAR_ITEM_ID, itemId);
      args.addDataItem(CommonsConstants.VAR_ITEM_CATEGORIES, ids);

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          Assert.notNull(response);

          if (response.hasErrors()) {
            Global.showError((Object[]) response.getErrors());
          } else if (response.hasResponse(Integer.class)) {
            requery(itemId);
          } else {
            Global.showError("Unknown response");
          }
        }
      });
    }

    private String getCategories() {
      return getCategories(categories);
    }

    private String getCategories(List<Long> categoryList) {
      StringBuilder sb = new StringBuilder();

      for (Long categoryId : categoryList) {
        if (sb.length() > 0) {
          sb.append(BeeConst.CHAR_COMMA);
        }
        sb.append(categoryId);
      }
      return sb.toString();
    }

    private boolean isEmpty() {
      return categories.isEmpty();
    }

    private void removeCategories() {
      final List<Integer> indexes = Lists.newArrayList();

      for (int i = 0; i < widget.getItemCount(); i++) {
        if (widget.isItemSelected(i)) {
          indexes.add(i);
        }
      }
      if (!indexes.isEmpty()) {
        if (!BeeUtils.isEmpty(itemId)) {
          Global.confirm(BeeUtils.concat(1, "Pašalinti", indexes.size(), "kategorijas?"),
              new BeeCommand() {
                @Override
                public void execute() {
                  List<Long> categoryList = Lists.newArrayList();

                  for (int idx : indexes) {
                    categoryList.add(categories.get(idx));
                  }
                  doRequest(CommonsConstants.SVC_REMOVE_CATEGORIES, getCategories(categoryList));
                }
              });
        } else {
          Collections.reverse(indexes);

          for (int idx : indexes) {
            categories.remove(idx);
            widget.removeItem(idx);
          }
        }
      }
    }

    private void requery(Long item) {
      this.itemId = item;
      categories.clear();
      widget.clear();

      if (!BeeUtils.isEmpty(itemId)) {
        Filter flt = ComparisonFilter.isEqual(CommonsConstants.COL_ITEM, new LongValue(itemId));

        Queries.getRowSet(CommonsConstants.TBL_ITEM_CATEGORIES, null, flt, null,
            new RowSetCallback() {
              @Override
              public void onFailure(String[] reason) {
                Global.showError((Object[]) reason);
              }

              @Override
              public void onSuccess(BeeRowSet result) {
                if (result.isEmpty()) {
                  return;
                }
                updateCategories(result.getRows(),
                    result.getColumnIndex(CommonsConstants.COL_NAME),
                    result.getColumnIndex(CommonsConstants.COL_CATEGORY));
              }
            });
      }
    }

    private void setWidget(BeeListBox widget) {
      this.widget = widget;
    }

    private void updateCategories(Iterable<? extends IsRow> rows, int nameIndex, Integer idIndex) {
      if (!BeeUtils.isEmpty(rows)) {
        for (IsRow row : rows) {
          String category = row.getString(nameIndex);

          if (idIndex == null) {
            categories.add(row.getId());
          } else {
            categories.add(row.getLong(idIndex));
          }
          widget.addItem(category);
        }
      }
    }
  }

  private static class ItemFormHandler extends AbstractFormCallback {
    private final CategoryCollector categories = new CategoryCollector();

    @Override
    public void afterCreateWidget(final String name, final Widget widget) {
      if (BeeUtils.same(name, "Categories") && widget instanceof BeeListBox) {
        categories.setWidget((BeeListBox) widget);

      } else if (BeeUtils.same(name, "AddCategories")
          && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            categories.addCategories();
          }
        });
      } else if (BeeUtils.same(name, "RemoveCategories")
          && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            categories.removeCategories();
          }
        });
      }
    }

    @Override
    public FormCallback getInstance() {
      return new ItemFormHandler();
    }

    @Override
    public boolean onPrepareForInsert(FormView form, final DataView dataView, IsRow row) {
      Assert.noNulls(dataView, row);

      List<BeeColumn> columns = Lists.newArrayList();
      List<String> values = Lists.newArrayList();

      for (BeeColumn column : form.getDataColumns()) {
        String colName = column.getId();
        String value = row.getString(form.getDataIndex(colName));

        if (!BeeUtils.isEmpty(value)) {
          columns.add(column);
          values.add(value);

        } else if (BeeUtils.inListSame(colName, CommonsConstants.COL_NAME, "Unit")
            || (BeeUtils.same(colName, "Currency")
            && !BeeUtils.isEmpty(row.getString(form.getDataIndex("Price"))))) {

          dataView.notifySevere(colName + ": value required");
          return false;
        }
      }
      BeeRowSet rs = new BeeRowSet("Items", columns);
      rs.addRow(0, values.toArray(new String[0]));

      ParameterList args = createArgs(CommonsConstants.SVC_ITEM_CREATE);
      args.addDataItem(CommonsConstants.VAR_ITEM_DATA, Codec.beeSerialize(rs));

      if (!categories.isEmpty()) {
        args.addDataItem(CommonsConstants.VAR_ITEM_CATEGORIES, categories.getCategories());
      }
      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          Assert.notNull(response);

          if (response.hasErrors()) {
            dataView.notifySevere(response.getErrors());

          } else if (response.hasResponse(BeeRow.class)) {
            dataView.finishNewRow(BeeRow.restore((String) response.getResponse()));

          } else {
            dataView.notifySevere("Unknown response");
          }
        }
      });
      return false;
    }

    @Override
    public void onStartEdit(FormView form, IsRow row) {
      categories.requery(row.getId());
    }

    @Override
    public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
      categories.requery(null);

      if (form.getViewPresenter() instanceof GridFormPresenter) {
        GridCallback gcb = ((GridFormPresenter) form.getViewPresenter()).getGridCallback();

        if (gcb != null && gcb instanceof ItemGridHandler) {
          ItemGridHandler grd = (ItemGridHandler) gcb;

          if (grd.showServices()) {
            newRow.setValue(form.getDataIndex(CommonsConstants.COL_SERVICE), 1);
          }
          IsRow category = grd.getSelectedCategory();

          if (category != null) {
            categories.updateCategories(Lists.newArrayList(category), grd.nameIndex, null);
          }
        }
      }
    }
  }

  private static class ItemGridHandler extends AbstractGridCallback
      implements SelectionHandler<TreeItem> {

    private static final String FILTER_KEY = "f1";
    private final boolean services;
    private int nameIndex = BeeConst.UNDEF;
    private IsRow selectedCategory = null;

    private ItemGridHandler(boolean showServices) {
      this.services = showServices;
    }

    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (widget instanceof Tree && BeeUtils.same(name, "Categories")) {
        populateCategories((Tree) widget);
        ((Tree) widget).addSelectionHandler(this);
      }
    }

    @Override
    public String getCaption() {
      if (showServices()) {
        return "Paslaugos";
      } else {
        return "Prekės";
      }
    }

    @Override
    public boolean onLoad(GridDescription gridDescription) {
      gridDescription.setCaption(null);

      Filter filter = ComparisonFilter.isEmpty(CommonsConstants.COL_SERVICE);

      if (showServices()) {
        filter = ComparisonFilter.isNot(filter);
      }
      gridDescription.setFilter(filter);
      return true;
    }

    public void onSelection(SelectionEvent<TreeItem> event) {
      if (event == null) {
        return;
      }
      if (getGridPresenter() != null) {
        Long category = null;

        if (event.getSelectedItem().getUserObject() instanceof IsRow) {
          setSelectedCategory((IsRow) event.getSelectedItem().getUserObject());
          category = getSelectedCategory().getId();
        } else {
          setSelectedCategory(null);
        }
        getGridPresenter().getDataProvider().setParentFilter(FILTER_KEY, getFilter(category));
        getGridPresenter().requery(true);
      }
    }

    @Override
    public void onShow(GridPresenter presenter) {
      setGridPresenter(presenter);
    }

    public boolean showServices() {
      return services;
    }

    private Filter getFilter(Long category) {
      if (category == null) {
        return null;
      } else {
        return ComparisonFilter.isEqual(CommonsConstants.COL_CATEGORY, new LongValue(category));
      }
    }

    private IsRow getSelectedCategory() {
      return selectedCategory;
    }

    private void populateCategories(final Tree tree) {
      Queries.getRowSet(CommonsConstants.TBL_CATEGORIES, null, new Queries.RowSetCallback() {
        public void onFailure(String[] reason) {
          BeeKeeper.getScreen().notifySevere(reason);
        }

        public void onSuccess(BeeRowSet result) {
          nameIndex = result.getColumnIndex(CommonsConstants.COL_NAME);
          tree.clear();
          tree.addItem(new TreeItem("*"));

          if (!result.isEmpty()) {
            int parentIndex = result.getColumnIndex("Parent");

            Map<Long, TreeItem> treeItems = Maps.newHashMap();
            List<IsRow> pendingRows = Lists.newArrayList();

            for (IsRow row : result.getRows()) {
              Long pId = row.getLong(parentIndex);
              if (pId == null) {
                TreeItem item = new TreeItem(row.getString(nameIndex), row);
                tree.addItem(item);
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
                  TreeItem item = new TreeItem(row.getString(nameIndex), row);
                  parentItem.addItem(item);
                  treeItems.put(row.getId(), item);
                }
              }
              if (pendingRows.size() >= cnt) {
                break;
              }
            }
          }
        }
      });
    }

    private void setSelectedCategory(IsRow selectedCategory) {
      this.selectedCategory = selectedCategory;
    }
  }

  public static void openItems(String args) {
    GridFactory.openGrid("Items", new ItemGridHandler(BeeUtils.startsSame(args, "s")));
  }

  public static void register() {
    FormFactory.registerFormCallback("Item", new ItemFormHandler());
  }

  private static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(CommonsConstants.COMMONS_MODULE);
    args.addQueryItem(CommonsConstants.COMMONS_METHOD, name);
    return args;
  }

  private CommonEventHandler() {
  }
}
