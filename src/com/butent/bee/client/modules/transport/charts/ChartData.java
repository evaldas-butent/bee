package com.butent.bee.client.modules.transport.charts;

import com.google.gwt.user.client.ui.HasEnabled;

import com.butent.bee.client.Global;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ChartData implements HasEnabled {

  enum Type implements HasCaption {
    DRIVER(Localized.dictionary().drivers()),
    DRIVER_GROUP(Localized.dictionary().driverGroupsShort()),
    CARGO(Localized.dictionary().cargos()),
    CARGO_TYPE(Localized.dictionary().trCargoTypes()),
    CUSTOMER(Localized.dictionary().transportationCustomers()),
    MANAGER(Localized.dictionary().managers()),
    LOADING(Localized.dictionary().cargoLoading()),
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

  private final Set<String> items = new HashSet<>();

  private final Set<Long> ids = new HashSet<>();
  private final Map<String, Long> itemToId = new HashMap<>();

  private final List<String> orderedItems = new ArrayList<>();
  private final List<String> selectedItems = new ArrayList<>();

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

  void add(String name) {
    add(name, null);
  }

  void add(String name, Long id) {
    if (name != null && !name.isEmpty()) {
      items.add(name);

      if (id != null) {
        ids.add(id);
        itemToId.put(name, id);
      }
    }
  }

  <E extends Enum<?> & HasCaption> void add(E item) {
    if (item != null) {
      add(item.getCaption(), (long) item.ordinal());
    }
  }

  void add(JustDate date) {
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
    return id != null && ids.contains(id);
  }

  boolean contains(String name) {
    return name != null && items.contains(name);
  }

  boolean containsAny(Collection<Long> itemIds) {
    if (BeeUtils.isEmpty(itemIds) || ids.isEmpty()) {
      return false;
    }

    for (Long id : itemIds) {
      if (contains(id)) {
        return true;
      }
    }

    return false;
  }

  void deselectAll() {
    if (!selectedItems.isEmpty()) {
      selectedItems.clear();
    }
  }

  Long getItemId(String item) {
    return itemToId.get(item);
  }

  List<String> getOrderedItems() {
    if (orderedItems.size() != size()) {
      orderedItems.clear();
      orderedItems.addAll(items);

      if (size() > 1) {
        orderedItems.sort(null);
      }
    }
    return orderedItems;
  }

  int getNumberOfSelectedItems() {
    return selectedItems.size();
  }

  int getNumberOfUnselectedItems() {
    return size() - getNumberOfSelectedItems();
  }

  List<String> getSelectedItems() {
    List<String> result = new ArrayList<>();

    if (!selectedItems.isEmpty()) {
      result.addAll(selectedItems);
    }
    return result;
  }

  Type getType() {
    return type;
  }

  boolean hasSelection() {
    return !selectedItems.isEmpty();
  }

  boolean isEmpty() {
    return items.isEmpty();
  }

  boolean setItemSelected(String item, boolean selected) {
    if (BeeUtils.isEmpty(item)) {
      return false;

    } else if (selected) {
      if (selectedItems.contains(item)) {
        return false;
      } else {
        selectedItems.add(item);
        return true;
      }

    } else {
      return selectedItems.remove(item);
    }
  }

  private int size() {
    return items.size();
  }
}
