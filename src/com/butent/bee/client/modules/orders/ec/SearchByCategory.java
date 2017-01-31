package com.butent.bee.client.modules.orders.ec;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.i18n.Collator;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.orders.OrdersConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchByCategory extends OrdEcView implements SelectionHandler<TreeItem> {
  private static final String STYLE_PRIMARY = "ItemPanel";
  private static final String STYLE_CATEGORIES = STYLE_PRIMARY + "-categories";
  private static final String STYLE_CONTAINER = "container";
  private static final String STYLE_TREE = "tree";
  private static final String STYLE_WRAPPER = "wrapper";

  private final Map<Long, String> categoryNames = new HashMap<>();
  private final Map<String, Long> categoryById = new HashMap<>();
  private final List<Long> categoryRoots = new ArrayList<>();
  private final Multimap<Long, Long> categoryByParent = HashMultimap.create();
  private final CategoryComparator categoryComparator = new CategoryComparator();

  private final String service;
  private final OrdEcItemPanel itemPanel;

  private final class CategoryComparator implements Comparator<Long> {
    private CategoryComparator() {
    }

    @Override
    public int compare(Long o1, Long o2) {
      return Collator.DEFAULT.compare(categoryNames.get(o1), categoryNames.get(o2));
    }
  }

  public SearchByCategory(String service) {
    super();
    this.service = service;
    this.itemPanel = new OrdEcItemPanel(true, service);
  }

  @Override
  protected void createUi() {
    ensureCategories();
  }

  @Override
  protected String getPrimaryStyle() {
    return "searchByCategory";
  }

  private void ensureCategories() {
    if (categoryNames.isEmpty()) {
      ParameterList params = OrdEcKeeper.createArgs(OrdersConstants.SVC_GET_CATEGORIES);
      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          OrdEcKeeper.dispatchMessages(response);
          String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());

          if (arr != null) {
            categoryNames.clear();
            categoryRoots.clear();
            categoryByParent.clear();
            categoryById.clear();

            for (int i = 0; i < arr.length; i += 3) {
              long id = BeeUtils.toLong(arr[i]);
              long parent = BeeUtils.toLong(arr[i + 1]);
              String name = arr[i + 2];

              categoryNames.put(id, name);

              if (parent > 0) {
                categoryByParent.put(parent, id);
              } else {
                categoryRoots.add(id);
              }
            }
            buildCategoryTree();
          }
        }
      });
    }
  }

  private void buildCategoryTree() {

    Tree tree = new Tree();
    tree.addSelectionHandler(this);

    TreeItem rootItem = new TreeItem(Localized.dictionary().ecSelectCategory());
    tree.addItem(rootItem);

    if (categoryRoots.size() > 1) {
      Collections.sort(categoryRoots, categoryComparator);
    }

    for (long id : categoryRoots) {
      TreeItem treeItem = createCategoryTreeItem(id);
      rootItem.addItem(treeItem);

      fillTree(categoryByParent, id, treeItem);
    }

    Flow panel = new Flow();
    EcStyles.add(panel, STYLE_CATEGORIES, STYLE_WRAPPER);

    Flow container = new Flow();
    EcStyles.add(container, STYLE_CATEGORIES, STYLE_CONTAINER);

    EcStyles.add(tree, STYLE_CATEGORIES, STYLE_TREE);
    container.add(tree);

    panel.add(container);
    add(panel);
    add(itemPanel);
  }

  private void fillTree(Multimap<Long, Long> data, long parent, TreeItem parentItem) {
    if (data.containsKey(parent)) {
      List<Long> children = new ArrayList<>(data.get(parent));
      if (children.size() > 1) {
        Collections.sort(children, categoryComparator);
      }

      for (long id : children) {
        TreeItem childItem = createCategoryTreeItem(id);
        parentItem.addItem(childItem);

        fillTree(data, id, childItem);
      }
    }
  }

  private TreeItem createCategoryTreeItem(long id) {
    TreeItem treeItem = new TreeItem(categoryNames.get(id));
    treeItem.setUserObject(id);

    categoryById.put(treeItem.getId(), id);

    return treeItem;
  }

  @Override
  public void onSelection(SelectionEvent<TreeItem> event) {
    itemPanel.clear();
    final TreeItem treeItem = event.getSelectedItem();

    Long id = categoryById.get(treeItem.getId());

    if (DataUtils.isId(id)) {
      OrdEcKeeper.searchItems(true, service, id.toString(), 0, input -> {
        itemPanel.setQuery(id.toString());
        OrdEcKeeper.renderItems(itemPanel, input);
      });
    }
  }
}