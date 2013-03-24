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

    boolean isSelected() {
      return selected;
    }

    private void increment() {
      count++;
    }

    private void setSelected(boolean selected) {
      this.selected = selected;
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
  }

  void deselectAll() {
    for (Item item : items) {
      item.setSelected(false);
    }
    setNumberOfSelectedItems(0);
  }

  List<Item> getItems() {
    return items;
  }

  int getNumberOfSelectedItems() {
    return numberOfSelectedItems;
  }
  
  int getNumberOfUnselectedItems() {
    return size() - getNumberOfSelectedItems();
  }

  Collection<String> getSelectedNames() {
    List<String> names = Lists.newArrayList();

    for (Item item : items) {
      if (item.isSelected()) {
        names.add(item.name);
      }
    }

    return names;
  }

  Type getType() {
    return type;
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
    for (Item item : items) {
      item.setSelected(true);
    }
    setNumberOfSelectedItems(size());
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

  int size() {
    return items.size();
  }

  private Item find(String name) {
    for (Item item : items) {
      if (Objects.equal(item.name, name)) {
        return item;
      }
    }
    return null;
  }
  
  private void setNumberOfSelectedItems(int numberOfSelectedItems) {
    this.numberOfSelectedItems = numberOfSelectedItems;
  }
}
