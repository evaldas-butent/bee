package com.butent.bee.client.modules.tasks;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.gwt.event.logical.shared.SelectionEvent;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Comparator;
import java.util.Objects;

public class TaskTemplatesGrid extends AbstractGridInterceptor {

  private Tree tree;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (widget instanceof Tree) {
      tree = (Tree) widget;
      tree.addSelectionHandler(this::handleSelection);
      initTree();
    }

    super.afterCreateWidget(name, widget, callback);
  }

  private void handleSelection(SelectionEvent<TreeItem> selectionEvent) {

    TreeItem item = selectionEvent.getSelectedItem();
    Filter filter;

    if (Objects.isNull(item)) {
      filter = null;
    } else if (item.getUserObject() instanceof IsRow) {
      filter = Filter.and(Filter.equals(COL_TASK_TYPE, ((IsRow) item.getUserObject()).getLong(
          Data.getColumnIndex(VIEW_TASK_TEMPLATES, COL_TASK_TYPE))),
          Filter.equals(ALS_TASK_PRODUCT_NAME, BeeUtils.notEmpty((String)
              item.getParentItem().getUserObject(), null)));
    } else {
      filter = Filter.equals(ALS_TASK_PRODUCT_NAME, BeeUtils.notEmpty((String) item.getUserObject(),
          null));
    }
    if (getGridPresenter().getDataProvider().setDefaultParentFilter(filter)) {
      getGridPresenter().refresh(false, true);
    }
  }

  private void initTree() {
    Queries.getRowSet(VIEW_TASK_TEMPLATES, null, (BeeRowSet result) ->  {
        int productName = result.getColumnIndex(ALS_TASK_PRODUCT_NAME);
        int typeName = result.getColumnIndex(ALS_TASK_TYPE_NAME);

        Multimap<String, IsRow> map = TreeMultimap.create(String::compareTo,
            Comparator.comparing(o -> BeeUtils.nvl(o.getString(typeName), BeeConst.STRING_EMPTY)));

        result.forEach(row -> map.put(BeeUtils.nvl(row.getString(productName),
            BeeConst.STRING_EMPTY), row));

        tree.clear();

        map.keySet().forEach(product -> {
          TreeItem branch = tree.addItem(BeeUtils.notEmpty(product,
              Localized.dictionary().filterNullLabel()));
          branch.setUserObject(product);

          map.get(product).forEach(t ->
              branch.addItem(new TreeItem("<i>" + BeeUtils.notEmpty(t.getString(typeName),
                  Localized.dictionary().filterNullLabel()) + "</i>", t)));
        });
    });
  }
}

