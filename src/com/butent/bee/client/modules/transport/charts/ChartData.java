package com.butent.bee.client.modules.transport.charts;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import com.butent.bee.client.Global;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

class ChartData {

  static class Item implements Comparable<Item> {
    private final String name;
    private final Long id;

    private int count;

    private boolean selected = false;
    private boolean used = false;
    private boolean enabled = true;

    private Item(String name, Long id) {
      this.name = name;
      this.id = id;

      this.count = 1;
    }

    @Override
    public int compareTo(Item o) {
      return BeeUtils.compare(name, o.name);
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof Item) ? Objects.equal(name, ((Item) obj).name) : false;
    }

    @Override
    public int hashCode() {
      return (name == null) ? 0 : name.hashCode();
    }

    int getCount() {
      return count;
    }

    Long getId() {
      return id;
    }

    String getName() {
      return name;
    }

    boolean isEnabled() {
      return enabled;
    }

    boolean isSelected() {
      return selected;
    }

    boolean isUsed() {
      return used;
    }

    private void increment() {
      count++;
    }

    private void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    private void setSelected(boolean selected) {
      this.selected = selected;
    }

    private void setUsed(boolean used) {
      this.used = used;
    }
  }

  enum Type implements HasCaption {
    DRIVER(Global.CONSTANTS.drivers()),
    DRIVER_GROUP(Global.CONSTANTS.driverGroupsShort()),
    CARGO(Global.CONSTANTS.cargos()),
    CUSTOMER(Global.CONSTANTS.transportationCustomers()),
    LOADING(Global.CONSTANTS.cargoLoading()),
    ORDER(Global.CONSTANTS.transportationOrders()),
    ORDER_STATUS(Global.CONSTANTS.transportationOrderStatuses()),
    PLACE(Global.CONSTANTS.cargoHandlingPlaces()),
    TRAILER(Global.CONSTANTS.trailers()),
    TRIP(Global.CONSTANTS.trips()),
    TRUCK(Global.CONSTANTS.trucks()),
    UNLOADING(Global.CONSTANTS.cargoUnloading()),
    VEHICLE_GROUP(Global.CONSTANTS.vehicleGroupsShort()),
    VEHICLE_MODEL(Global.CONSTANTS.vehicleModelsShort()),
    VEHICLE_TYPE(Global.CONSTANTS.vehicleTypesShort());

    private final String caption;

    private Type(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  private final Type type;

  private final List<Item> items = Lists.newArrayList();

  private int numberOfSelectedItems = 0;
  private int numberOfUsedItems = 0;
  private int numberOfDisabledItems = 0;

  ChartData(Type type) {
    this.type = type;
  }

  void add(Collection<String> names) {
    if (names != null) {
      for (String name : names) {
        add(name);
      }
    }
  }

  void add(String name) {
    add(name, null);
  }

  void add(String name, Long id) {
    Item item = find(name);
    if (item == null) {
      items.add(new Item(name, id));
    } else {
      item.increment();
    }
  }

  void addNotEmpty(String name) {
    if (!BeeUtils.isEmpty(name)) {
      add(name);
    }
  }
  
  void clear() {
    items.clear();

    setNumberOfSelectedItems(0);
    setNumberOfUsedItems(0);
    setNumberOfDisabledItems(0);
  }

  boolean contains(Long id) {
    if (id == null) {
      return false;
    }

    for (Item item : items) {
      if (Objects.equal(item.id, id)) {
        return true;
      }
    }
    return false;
  }

  boolean contains(String name) {
    return find(name) != null;
  }
  
  void deselectAll() {
    if (getNumberOfSelectedItems() > 0) {
      for (Item item : items) {
        item.setSelected(false);
      }
      setNumberOfSelectedItems(0);
    }
  }

  void disableAll() {
    if (getNumberOfEnabledItems() > 0) {
      for (Item item : items) {
        item.setEnabled(false);

        item.setSelected(false);
        item.setUsed(false);
      }

      setNumberOfDisabledItems(size());

      setNumberOfSelectedItems(0);
      setNumberOfUsedItems(0);
    }
  }

  void enableAll() {
    if (getNumberOfDisabledItems() > 0) {
      for (Item item : items) {
        item.setEnabled(true);
      }
      setNumberOfDisabledItems(0);
    }
  }
  
  List<Item> getItems() {
    return items;
  }

  int getNumberOfEnabledItems() {
    return size() - getNumberOfDisabledItems();
  }
  
  int getNumberOfEnabledUnselectedItems() {
    return size() - getNumberOfSelectedItems() - getNumberOfDisabledItems();
  }

  int getNumberOfSelectedItems() {
    return numberOfSelectedItems;
  }

  int getNumberOfUsedItems() {
    return numberOfUsedItems;
  }

  List<Item> getSelectedItems() {
    List<Item> result = Lists.newArrayList();

    if (getNumberOfSelectedItems() > 0) {
      for (Item item : items) {
        if (item.isSelected()) {
          result.add(item);
        }
      }
    }

    return result;
  }

  Collection<String> getSelectedNames() {
    List<String> names = Lists.newArrayList();

    if (getNumberOfSelectedItems() > 0) {
      for (Item item : items) {
        if (item.isSelected()) {
          names.add(item.name);
        }
      }
    }

    return names;
  }

  Type getType() {
    return type;
  }
  
  Collection<String> getUsedNames() {
    List<String> names = Lists.newArrayList();

    if (getNumberOfUsedItems() > 0) {
      for (Item item : items) {
        if (item.isUsed()) {
          names.add(item.name);
        }
      }
    }

    return names;
  }

  boolean hasSelection() {
    return getNumberOfSelectedItems() > 0;
  }

  boolean isEmpty() {
    return items.isEmpty();
  }

  void prepare() {
    if (size() > 1) {
      Collections.sort(items);
    }
  }

  void selectAll() {
    if (getNumberOfSelectedItems() < size()) {
      for (Item item : items) {
        item.setSelected(true);
      }
      setNumberOfSelectedItems(size());
    }
  }

  boolean setItemEnabled(Item item, boolean enabled) {
    if (item != null && item.isEnabled() != enabled) {
      item.setEnabled(enabled);
      setNumberOfDisabledItems(getNumberOfDisabledItems() + (enabled ? -1 : 1));
      
      if (!enabled) {
        setItemSelected(item, false);
        setItemUsed(item, false);
      }
      
      return true;
    } else {
      return false;
    }
  }

  boolean setItemSelected(Item item, boolean selected) {
    if (item != null && item.isSelected() != selected) {
      item.setSelected(selected);
      setNumberOfSelectedItems(getNumberOfSelectedItems() + (selected ? 1 : -1));
      return true;
    } else {
      return false;
    }
  }

  boolean setItemUsed(Item item, boolean used) {
    if (item != null && item.isUsed() != used) {
      item.setUsed(used);
      setNumberOfUsedItems(getNumberOfUsedItems() + (used ? 1 : -1));
      return true;
    } else {
      return false;
    }
  }

  boolean setSelected(int index, boolean selected) {
    if (BeeUtils.isIndex(items, index)) {
      return setItemSelected(items.get(index), selected);
    } else {
      return false;
    }
  }

  boolean setSelected(String name, boolean selected) {
    return setItemSelected(find(name), selected);
  }
  
  void unuseAll() {
    if (getNumberOfUsedItems() > 0) {
      for (Item item : items) {
        item.setUsed(false);
      }
      setNumberOfUsedItems(0);
    }
  }
  
  private Item find(String name) {
    for (Item item : items) {
      if (Objects.equal(item.name, name)) {
        return item;
      }
    }
    return null;
  }

  private int getNumberOfDisabledItems() {
    return numberOfDisabledItems;
  }

  private void setNumberOfDisabledItems(int numberOfDisabledItems) {
    this.numberOfDisabledItems = numberOfDisabledItems;
  }

  private void setNumberOfSelectedItems(int numberOfSelectedItems) {
    this.numberOfSelectedItems = numberOfSelectedItems;
  }

  private void setNumberOfUsedItems(int numberOfUsedItems) {
    this.numberOfUsedItems = numberOfUsedItems;
  }

  private int size() {
    return items.size();
  }
}
