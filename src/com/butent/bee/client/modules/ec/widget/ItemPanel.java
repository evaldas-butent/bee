package com.butent.bee.client.modules.ec.widget;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.modules.ec.EcItemList;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ItemPanel extends Flow {

  private static final String STYLE_PRIMARY = "ItemPanel";
  private static final String STYLE_GROUPS = STYLE_PRIMARY + "-groups";
  private static final String STYLE_MANUFACTURERS = STYLE_PRIMARY + "-manufacturers";
  private static final String STYLE_ITEMS = STYLE_PRIMARY + "-items";

  private static final String STYLE_GROUP = STYLE_PRIMARY + "-group";
  private static final String STYLE_MANUFACTURER = STYLE_PRIMARY + "-manufacturer";

  private static final String STYLE_WRAPPER = "wrapper";
  private static final String STYLE_CAPTION = "caption";
  private static final String STYLE_CONTAINER = "container";
  private static final String STYLE_SELECTABLE = "selectable";
  private static final String STYLE_LABEL = "label";
  private static final String STYLE_SELECTED = "selected";

  private final List<EcItem> items = Lists.newArrayList();

  private final List<String> groups = Lists.newArrayList();
  private final List<String> manufacturers = Lists.newArrayList();

  private final Set<String> selectedGroups = Sets.newHashSet();
  private final Set<String> selectedManufacturers = Sets.newHashSet();

  private final Flow manufacturerWrapper;
  private final ItemList itemWrapper;

  public ItemPanel(EcItemList ecItemList) {
    super(EcStyles.name(STYLE_PRIMARY));

    int size = ecItemList.size();
    for (int i = 0; i < size; i++) {
      EcItem ecItem = ecItemList.get(i);
      items.add(ecItem);

      for (String group : ecItem.getGroups()) {
        if (!groups.contains(group)) {
          groups.add(group);
        }
      }

      String manufacturer = ecItem.getManufacturer();
      if (!BeeUtils.isEmpty(manufacturer) && !manufacturers.contains(manufacturer)) {
        manufacturers.add(manufacturer);
      }
    }

    if (groups.size() > 1) {
      Collections.sort(groups);
    }
    if (manufacturers.size() > 1) {
      Collections.sort(manufacturers);
    }

    boolean showGroups = size > 1 && !groups.isEmpty();
    boolean showManufacturers = size > 1 && !manufacturers.isEmpty();

    if (showGroups) {
      add(renderGroups());
    }
    if (showManufacturers) {
      this.manufacturerWrapper = new Flow();
      EcStyles.add(manufacturerWrapper, STYLE_MANUFACTURERS, STYLE_WRAPPER);

      renderManufacturers(manufacturers);
      add(manufacturerWrapper);
    } else {
      this.manufacturerWrapper = null;
    }

    this.itemWrapper = new ItemList(items);
    EcStyles.add(itemWrapper, STYLE_ITEMS, STYLE_WRAPPER);
    add(itemWrapper);
  }

  private List<EcItem> filterByGroup(List<EcItem> input) {
    if (selectedGroups.isEmpty() || selectedGroups.size() >= groups.size()) {
      return input;

    } else {
      List<EcItem> result = Lists.newArrayList();
      for (EcItem item : input) {
        if (BeeUtils.containsAny(selectedGroups, item.getGroups())) {
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

  private List<String> getManufacturers(List<EcItem> input) {
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
    itemWrapper.render(filterByManufacturer(filterByGroup(items)));
  }

  private void refreshManufacturersAndItems() {
    List<EcItem> filteredByGroup = filterByGroup(items);

    if (manufacturerWrapper == null) {
      itemWrapper.render(filteredByGroup);
    } else {
      List<String> filteredManufacturers = getManufacturers(filteredByGroup);
      if (!selectedManufacturers.isEmpty()) {
        selectedManufacturers.retainAll(filteredManufacturers);
      }

      renderManufacturers(filteredManufacturers);
      itemWrapper.render(filterByManufacturer(filteredByGroup));
    }
  }

  private Widget renderGroup(final String group, boolean selectable) {
    if (selectable) {
      final CheckBox checkBox = new CheckBox(group);
      EcStyles.add(checkBox, STYLE_GROUP, STYLE_SELECTABLE);

      checkBox.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          boolean selected = checkBox.getValue();
          checkBox.setStyleName(EcStyles.name(STYLE_GROUP, STYLE_SELECTED), selected);

          if (selected) {
            selectedGroups.add(group);
          } else {
            selectedGroups.remove(group);
          }
          refreshManufacturersAndItems();
        }
      });

      return checkBox;

    } else {
      Label label = new Label(group);
      EcStyles.add(label, STYLE_GROUP, STYLE_LABEL);
      return label;
    }
  }

  private Widget renderGroups() {
    boolean selectable = items.size() > 1 && groups.size() > 1;

    Flow panel = new Flow();
    EcStyles.add(panel, STYLE_GROUPS, STYLE_WRAPPER);
    if (selectable) {
      EcStyles.add(panel, STYLE_GROUPS, STYLE_SELECTABLE);
    }

    String caption = selectable
        ? Localized.constants.ecSelectCategory() : Localized.constants.ecItemCategory();
    Label label = new Label(caption);
    EcStyles.add(label, STYLE_GROUPS, STYLE_CAPTION);
    panel.add(label);

    Flow container = new Flow();
    EcStyles.add(container, STYLE_GROUPS, STYLE_CONTAINER);

    for (String group : groups) {
      container.add(renderGroup(group, selectable));
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
      String caption = selectable
          ? Localized.constants.ecSelectManufacturer() : Localized.constants.ecItemManufacturer();
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
