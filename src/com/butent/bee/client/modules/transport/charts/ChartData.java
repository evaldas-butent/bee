package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
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
      return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof Item) ? name.equals(((Item) obj).name) : false;
    }

    @Override
    public int hashCode() {
      return name.hashCode();
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
    
    void invert() {
      setSelected(!isSelected());
    }

    boolean isSelected() {
      return selected;
    }

    void setSelected(boolean selected) {
      this.selected = selected;
    }

    private void increment() {
      count++;
    }
  }

  enum Type implements HasCaption {
    CUSTOMER("Užsakovas"),
    ORDER("Užsakymas"),
    CARGO("Krovinys"),
    LOADING("Pakrovimas"),
    UNLOADING("Iškrovimas"),
    VEHICLE("Vilkikas"),
    TRAILER("Puspriekabė"),
    DRIVER("Vairuotojas");

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

  private final Collection<Item> items = Sets.newTreeSet();

  ChartData(Type type) {
    this.type = type;
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
  
  void add(Collection<String> names) {
    if (names != null) {
      for (String name : names) {
        add(name);
      }
    }
  }

  void clear() {
    items.clear();
  }

  List<Item> getList() {
    return Lists.newArrayList(items);
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

  void setSelected(boolean selected) {
    for (Item item : items) {
      item.setSelected(selected);
    }
  }
  
  void setSelected(String name, boolean selected) {
    Item item = find(name);
    if (item != null) {
      item.setSelected(selected);
    }
  }

  int size() {
    return items.size();
  }

  private Item find(String name) {
    if (!BeeUtils.isEmpty(name)) {
      for (Item item : items) {
        if (item.name.equals(name)) {
          return item;
        }
      }
    }
    return null;
  }
}
