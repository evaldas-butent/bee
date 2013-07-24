package com.butent.bee.client.modules.ec.widget;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ItemPanel extends Flow implements SelectionHandler<TreeItem> {

  private static final BeeLogger logger = LogUtils.getLogger(ItemPanel.class);

  private static final String STYLE_PRIMARY = "ItemPanel";
  private static final String STYLE_CATEGORIES = STYLE_PRIMARY + "-categories";
  private static final String STYLE_MANUFACTURERS = STYLE_PRIMARY + "-manufacturers";
  private static final String STYLE_ITEMS = STYLE_PRIMARY + "-items";

  private static final String STYLE_CATEGORY = STYLE_PRIMARY + "-category";
  private static final String STYLE_MANUFACTURER = STYLE_PRIMARY + "-manufacturer";

  private static final String STYLE_WRAPPER = "wrapper";
  private static final String STYLE_CAPTION = "caption";
  private static final String STYLE_CONTAINER = "container";
  private static final String STYLE_SELECTABLE = "selectable";
  private static final String STYLE_LABEL = "label";
  private static final String STYLE_SELECTED = "selected";
  private static final String STYLE_TREE = "tree";

  private final List<EcItem> items = Lists.newArrayList();

  private final Set<Long> categories = Sets.newHashSet();
  private final List<String> manufacturers = Lists.newArrayList();

  private final Set<Long> selectedCategories = Sets.newHashSet();
  private final Set<String> selectedManufacturers = Sets.newHashSet();

  private Flow manufacturerWrapper;
  private ItemList itemWrapper;

  public ItemPanel() {
    super(EcStyles.name(STYLE_PRIMARY));
  }

  @Override
  public void clear() {
    super.clear();

    if (!items.isEmpty()) {
      items.clear();
      categories.clear();
      manufacturers.clear();

      selectedCategories.clear();
      selectedManufacturers.clear();
    }
  }

  @Override
  public void onSelection(SelectionEvent<TreeItem> event) {
    Object userObject = event.getSelectedItem().getUserObject();
    Long category = (userObject instanceof Long) ? (Long) userObject : null;

    selectedCategories.clear();
    if (category != null) {
      selectedCategories.add(category);
    }
    refreshManufacturersAndItems();
  }

  public void render(List<EcItem> ecItems) {
    long millis = System.currentTimeMillis();

    if (!isEmpty()) {
      clear();
    }

    int size = ecItems.size();
    for (int i = 0; i < size; i++) {
      EcItem ecItem = ecItems.get(i);
      items.add(ecItem);

      categories.addAll(ecItem.getCategoryList());

      String manufacturer = ecItem.getManufacturer();
      if (!BeeUtils.isEmpty(manufacturer) && !manufacturers.contains(manufacturer)) {
        manufacturers.add(manufacturer);
      }
    }
    
    boolean debug = EcKeeper.isDebug();
    if (debug) {
      logger.debug("cat", categories.size(), "man", manufacturers.size(),
          TimeUtils.elapsedMillis(millis));
      millis = System.currentTimeMillis();
    }

    if (manufacturers.size() > 1) {
      Collections.sort(manufacturers);
    }

    boolean showCategories = size > 1 && !categories.isEmpty();
    boolean showManufacturers = size > 1 && !manufacturers.isEmpty();

    if (showCategories) {
      add(renderCategories());
      if (debug) {
        logger.debug("cat rendered", TimeUtils.elapsedMillis(millis));
        millis = System.currentTimeMillis();
      }
    }

    if (showManufacturers) {
      this.manufacturerWrapper = new Flow();
      EcStyles.add(manufacturerWrapper, STYLE_MANUFACTURERS, STYLE_WRAPPER);

      renderManufacturers(manufacturers);
      add(manufacturerWrapper);
      if (debug) {
        logger.debug("man rendered", TimeUtils.elapsedMillis(millis));
        millis = System.currentTimeMillis();
      }
    } else {
      this.manufacturerWrapper = null;
    }

    this.itemWrapper = new ItemList(items);
    EcStyles.add(itemWrapper, STYLE_ITEMS, STYLE_WRAPPER);
    add(itemWrapper);
    
    if (debug) {
      logger.debug("items rendered", TimeUtils.elapsedMillis(millis));
    }
  }

  private List<EcItem> filterByCategory(List<EcItem> input) {
    if (selectedCategories.isEmpty() || selectedCategories.size() >= categories.size()) {
      return input;

    } else {
      List<EcItem> result = Lists.newArrayList();
      for (EcItem item : input) {
        if (BeeUtils.containsAny(selectedCategories, item.getCategoryList())) {
          result.add(item);
        }
      }
      return result;
    }
  }

  private List<EcItem> filterByManufacturer(List<EcItem> input) {
    if (selectedManufacturers.isEmpty() || selectedManufacturers.size() >= manufacturers.size()) {
      return input;

    } else {
      List<EcItem> result = Lists.newArrayList();
      for (EcItem item : input) {
        if (selectedManufacturers.contains(item.getManufacturer())) {
          result.add(item);
        }
      }
      return result;
    }
  }

  private static List<String> getManufacturers(List<EcItem> input) {
    List<String> result = Lists.newArrayList();

    int size = input.size();
    for (int i = 0; i < size; i++) {
      String manufacturer = input.get(i).getManufacturer();
      if (!BeeUtils.isEmpty(manufacturer) && !result.contains(manufacturer)) {
        result.add(manufacturer);
      }
    }

    if (result.size() > 1) {
      Collections.sort(result);
    }
    return result;
  }

  private void refreshItems() {
    itemWrapper.render(filterByManufacturer(filterByCategory(items)));
  }

  private void refreshManufacturersAndItems() {
    List<EcItem> filteredByCategory = filterByCategory(items);

    if (manufacturerWrapper == null) {
      itemWrapper.render(filteredByCategory);
    } else {
      List<String> filteredManufacturers = getManufacturers(filteredByCategory);
      if (!selectedManufacturers.isEmpty()) {
        selectedManufacturers.retainAll(filteredManufacturers);
      }

      renderManufacturers(filteredManufacturers);
      itemWrapper.render(filterByManufacturer(filteredByCategory));
    }
  }

  private Widget renderCategories() {
    boolean selectable = items.size() > 1 && categories.size() > 1;

    Flow panel = new Flow();
    EcStyles.add(panel, STYLE_CATEGORIES, STYLE_WRAPPER);
    if (selectable) {
      EcStyles.add(panel, STYLE_CATEGORIES, STYLE_SELECTABLE);
    }

    if (!selectable) {
      Label caption = new Label(Localized.getConstants().ecItemCategory());
      EcStyles.add(caption, STYLE_CATEGORIES, STYLE_CAPTION);
      panel.add(caption);
    }

    Flow container = new Flow();
    EcStyles.add(container, STYLE_CATEGORIES, STYLE_CONTAINER);

    if (selectable) {
      Tree tree = EcKeeper.buildCategoryTree(categories);
      EcStyles.add(tree, STYLE_CATEGORIES, STYLE_TREE);

      tree.addSelectionHandler(this);

      container.add(tree);

    } else {
      for (Long category : categories) {
        Label label = new Label(EcKeeper.getCategoryName(category));
        EcStyles.add(label, STYLE_CATEGORY, STYLE_LABEL);

        container.add(label);
      }
    }

    panel.add(container);

    return panel;
  }

  private Widget renderManufacturer(final String manufacturer, boolean selectable) {
    if (selectable) {
      final CheckBox checkBox = new CheckBox(manufacturer);
      EcStyles.add(checkBox, STYLE_MANUFACTURER, STYLE_SELECTABLE);

      if (selectedManufacturers.contains(manufacturer)) {
        checkBox.setValue(true);
        checkBox.addStyleName(EcStyles.name(STYLE_MANUFACTURER, STYLE_SELECTED));
      }

      checkBox.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          boolean selected = checkBox.getValue();
          checkBox.setStyleName(EcStyles.name(STYLE_MANUFACTURER, STYLE_SELECTED), selected);

          if (selected) {
            selectedManufacturers.add(manufacturer);
          } else {
            selectedManufacturers.remove(manufacturer);
          }
          refreshItems();
        }
      });

      return checkBox;

    } else {
      Label label = new Label(manufacturer);
      EcStyles.add(label, STYLE_MANUFACTURER, STYLE_LABEL);
      return label;
    }
  }

  private void renderManufacturers(List<String> input) {
    boolean selectable = items.size() > 1 && input.size() > 1;

    if (!manufacturerWrapper.isEmpty()) {
      manufacturerWrapper.clear();
    }
    if (selectable) {
      manufacturerWrapper.setStyleName(EcStyles.name(STYLE_MANUFACTURERS, STYLE_SELECTABLE),
          selectable);
    }

    if (!input.isEmpty()) {
      String caption = selectable ? Localized.getConstants().ecSelectManufacturer() 
          : Localized.getConstants().ecItemManufacturer();
      Label label = new Label(caption);
      EcStyles.add(label, STYLE_MANUFACTURERS, STYLE_CAPTION);
      manufacturerWrapper.add(label);
    }

    Flow container = new Flow();
    EcStyles.add(container, STYLE_MANUFACTURERS, STYLE_CONTAINER);

    for (String manufacturer : input) {
      container.add(renderManufacturer(manufacturer, selectable));
    }
    manufacturerWrapper.add(container);
  }
}
