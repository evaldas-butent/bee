package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

class CargoEvent {

  enum Type {
    LOADING, UNLOADING
  }
  
  static Multimap<Long, CargoEvent> splitByCountry(Collection<CargoEvent> events) {
    Multimap<Long, CargoEvent> result = LinkedListMultimap.create();
    if (BeeUtils.isEmpty(events)) {
      return result;
    }

    for (CargoEvent event : events) {
      if (event.isLoading() && event.isCargoEvent()) {
        result.put(event.getCountryId(), event);
      }
    }

    for (CargoEvent event : events) {
      if (event.isLoading() && event.isHandlingEvent()) {
        result.put(event.getCountryId(), event);
      }
    }
    for (CargoEvent event : events) {
      if (event.isUnloading() && event.isHandlingEvent()) {
        result.put(event.getCountryId(), event);
      }
    }

    for (CargoEvent event : events) {
      if (event.isUnloading() && event.isCargoEvent()) {
        result.put(event.getCountryId(), event);
      }
    }

    return result;
  }
  
  private final OrderCargo cargo;
  private final CargoHandling handling;

  private final boolean loading;

  CargoEvent(OrderCargo cargo, boolean loading) {
    this(cargo, null, loading);
  }

  CargoEvent(OrderCargo cargo, CargoHandling handling, boolean loading) {
    this.cargo = cargo;
    this.handling = handling;

    this.loading = loading;
  }

  OrderCargo getCargo() {
    return cargo;
  }

  Long getCountryId() {
    if (isCargoEvent()) {
      return loading ? cargo.getLoadingCountry() : cargo.getUnloadingCountry();
    } else if (loading) {
      return BeeUtils.nvl(handling.getLoadingCountry(), cargo.getLoadingCountry());
    } else {
      return BeeUtils.nvl(handling.getUnloadingCountry(), cargo.getUnloadingCountry());
    }
  }

  CargoHandling getHandling() {
    return handling;
  }

  String getPlace() {
    if (isCargoEvent()) {
      return loading ? cargo.getLoadingPlace() : cargo.getUnloadingPlace();
    } else {
      return loading ? handling.getLoadingPlace() : handling.getUnloadingPlace();
    }
  }

  String getTerminal() {
    if (isCargoEvent()) {
      return loading ? cargo.getLoadingTerminal() : cargo.getUnloadingTerminal();
    } else {
      return loading ? handling.getLoadingTerminal() : handling.getUnloadingTerminal();
    }
  }

  boolean isCargoEvent() {
    return handling == null;
  }

  boolean isHandlingEvent() {
    return handling != null;
  }

  boolean isLoading() {
    return loading;
  }
  
  boolean isUnloading() {
    return !loading;
  }
}
