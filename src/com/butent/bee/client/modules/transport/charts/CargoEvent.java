package com.butent.bee.client.modules.transport.charts;

import com.butent.bee.shared.utils.BeeUtils;

class CargoEvent {

  enum Type {
    LOADING, UNLOADING
  }
  
  private final Freight freight;
  private final CargoHandling cargoHandling;

  private final boolean loading;

  CargoEvent(Freight freight, CargoHandling cargoHandling, boolean loading) {
    this.freight = freight;
    this.cargoHandling = cargoHandling;

    this.loading = loading;
  }

  CargoHandling getCargoHandling() {
    return cargoHandling;
  }

  Long getCountryId() {
    if (isFreightEvent()) {
      return loading ? freight.getLoadingCountry() : freight.getUnloadingCountry();
    } else if (loading) {
      return BeeUtils.nvl(cargoHandling.getLoadingCountry(), freight.getLoadingCountry());
    } else {
      return BeeUtils.nvl(cargoHandling.getUnloadingCountry(), freight.getUnloadingCountry());
    }
  }

  Freight getFreight() {
    return freight;
  }

  String getPlace() {
    if (isFreightEvent()) {
      return loading ? freight.getLoadingPlace() : freight.getUnloadingPlace();
    } else {
      return loading ? cargoHandling.getLoadingPlace() : cargoHandling.getUnloadingPlace();
    }
  }

  String getTerminal() {
    if (isFreightEvent()) {
      return loading ? freight.getLoadingTerminal() : freight.getUnloadingTerminal();
    } else {
      return loading ? cargoHandling.getLoadingTerminal() : cargoHandling.getUnloadingTerminal();
    }
  }

  boolean isFreightEvent() {
    return cargoHandling == null;
  }

  boolean isHandlingEvent() {
    return cargoHandling != null;
  }

  boolean isLoading() {
    return loading;
  }
  
  boolean isUnloading() {
    return !loading;
  }
}
