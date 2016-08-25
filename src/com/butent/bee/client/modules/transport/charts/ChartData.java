package com.butent.bee.client.modules.transport.charts;

import com.google.gwt.user.client.ui.HasEnabled;

import com.butent.bee.client.Global;
import com.butent.bee.client.i18n.Collator;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class ChartData implements HasEnabled {

  static final class Item implements Comparable<Item> {
    private final String name;
    private final Long id;

    private int count;

    private boolean selected;
    private boolean enabled = true;

    private boolean wasSelected;
    private boolean wasEnabled;

    private Item(String name, Long id) {
      this.name = name;
      this.id = id;

      this.count = 1;
    }

    @Override
    public int compareTo(Item o) {
      return Collator.DEFAULT.compare(name, o.name);
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof Item) ? Objects.equals(name, ((Item) obj).name) : false;
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

    private void increment() {
      count++;
    }

    private void restoreState() {
      selected = wasSelected;
      enabled = wasEnabled;
    }

    private void saveState() {
      wasSelected = selected;
      wasEnabled = enabled;
    }

    private void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    private void setSelected(boolean selected) {
      this.selected = selected;
    }
  }

  enum Type implements HasCaption {
    DRIVER(Localized.dictionary().drivers()),
    DRIVER_GROUP(Localized.dictionary().driverGroupsShort()),
    CARGO(Localized.dictionary().cargos()),
    CARGO_TYPE(Localized.dictionary().trCargoTypes()),
    CUSTOMER(Localized.dictionary().transportationCustomers()),
    MANAGER(Localized.dictionary().managers()),
    LOADING(Localized.dictionary().cargoLoading()),
    LOADING_COUNTRY(Localized.dictionary().trLoadingCountry()),
    ORDER(Localized.dictionary().trOrders()),
    ORDER_STATUS(Localized.dictionary().trOrderStatus()),
    PLACE(Localized.dictionary().cargoHandlingPlaces()),
    TRAILER(Localized.dictionary().trailers()),
    TRIP(Localized.dictionary().trips()),
    TRIP_STATUS(Localized.dictionary().trTripStatus()),
    TRIP_ARRIVAL(Localized.dictionary().transportArrival()),
    TRIP_DEPARTURE(Localized.dictionary().transportDeparture()),
    TRUCK(Localized.dictionary().trucks()),
    UNLOADING(Localized.dictionary().cargoUnloading()),
    UNLOADING_COUNTRY(Localized.dictionary().trUnloadingCountry()),
    VEHICLE_GROUP(Localized.dictionary().vehicleGroupsShort()),
    VEHICLE_MODEL(Localized.dictionary().vehicleModelsShort()),
    VEHICLE_TYPE(Localized.dictionary().trVehicleTypesShort());

    private final String caption;

    Type(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  private final Type type;

  private final List<Item> items = new ArrayList<>();

  private int numberOfSelectedItems;
  private int numberOfDisabledItems;

  private boolean enabled = true;

  ChartData(Type type) {
    this.type = type;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;

    if (!enabled) {
      deselectAll();
    }
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

  <E extends Enum<?> & HasCaption> void addNotNull(E item) {
    if (item != null) {
      add(item.getCaption(), (long) item.ordinal());
    }
  }

  void addNotNull(JustDate date) {
    if (date != null) {
      add(date.toString(), (long) date.getDays());
    }
  }

  void addUser(Long userId) {
    if (userId != null) {
      String signature = Global.getUsers().getSignature(userId);
      if (signature != null) {
        add(signature, userId);
      }
    }
  }

  void clear() {
    items.clear();

    setNumberOfSelectedItems(0);
    setNumberOfDisabledItems(0);
  }

  boolean contains(Enum<?> e) {
    if (e == null) {
      return false;
    }

    long id = e.ordinal();
    return contains(id);
  }

  boolean contains(JustDate date) {
    if (date == null) {
      return false;
    }

    long id = date.getDays();
    return contains(id);
  }

  boolean contains(Long id) {
    if (id == null) {
      return false;
    }

    for (Item item : items) {
      if (Objects.equals(item.id, id)) {
        return true;
      }
    }
    return false;
  }

  boolean contains(String name) {
    return find(name) != null;
  }

  boolean containsAny(Collection<Long> ids) {
    if (BeeUtils.isEmpty(ids)) {
      return false;
    }

    for (Long id : ids) {
      if (contains(id)) {
        return true;
      }
    }

    return false;
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
      }

      setNumberOfDisabledItems(size());
      setNumberOfSelectedItems(0);
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

  Collection<String> getDisabledNames() {
    List<String> names = new ArrayList<>();

    if (getNumberOfDisabledItems() > 0) {
      for (Item item : items) {
        if (!item.isEnabled()) {
          names.add(item.name);
        }
      }
    }

    return names;
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

  List<Item> getSelectedItems() {
    List<Item> result = new ArrayList<>();

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
    List<String> names = new ArrayList<>();

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

  void restoreState() {
    if (!isEmpty()) {
      int cntSelected = 0;
      int cntDisabled = 0;

      for (Item item : items) {
        item.restoreState();
        if (!isEnabled()) {
          item.setSelected(false);
        }

        if (item.isSelected()) {
          cntSelected++;
        }
        if (!item.isEnabled()) {
          cntDisabled++;
        }
      }

      setNumberOfSelectedItems(cntSelected);
      setNumberOfDisabledItems(cntDisabled);
    }
  }

  void saveState() {
    for (Item item : items) {
      item.saveState();
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

  boolean setItemEnabled(String name, boolean enbl) {
    return setItemEnabled(find(name), enbl);
  }

  boolean setItemEnabled(Item item, boolean enbl) {
    if (item != null && item.isEnabled() != enbl) {
      item.setEnabled(enbl);
      setNumberOfDisabledItems(getNumberOfDisabledItems() + (enbl ? -1 : 1));

      if (!enbl) {
        setItemSelected(item, false);
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

  private Item find(String name) {
    for (Item item : items) {
      if (Objects.equals(item.name, name)) {
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

  private int size() {
    return items.size();
  }
}
