package com.butent.bee.client.modules.transport.charts;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

final class PlaceMatcher {

  static PlaceMatcher maybeCreate(Collection<ChartData> data) {
    if (BeeUtils.isEmpty(data)) {
      return null;
    }

    ChartData loadCountry = FilterHelper.getDataByType(data, ChartData.Type.LOADING_COUNTRY);
    ChartData loadData = FilterHelper.getDataByType(data, ChartData.Type.LOADING);
    ChartData unloadCountry = FilterHelper.getDataByType(data, ChartData.Type.UNLOADING_COUNTRY);
    ChartData unloadData = FilterHelper.getDataByType(data, ChartData.Type.UNLOADING);
    ChartData placeData = FilterHelper.getDataByType(data, ChartData.Type.PLACE);

    if (BeeUtils.anyNotNull(loadCountry, loadData, unloadCountry, unloadData, placeData)) {
      return new PlaceMatcher(loadCountry, loadData, unloadCountry, unloadData, placeData);
    } else {
      return null;
    }
  }

  private final ChartData loadingCountry;
  private final ChartData loadData;
  private final ChartData unloadingCountry;
  private final ChartData unloadData;
  private final ChartData placeData;

  private final boolean checkLoad;
  private final boolean checkUnload;

  private PlaceMatcher(ChartData loadingCountry, ChartData loadData, ChartData unloadingCountry,
                       ChartData unloadData, ChartData placeData) {
    this.loadingCountry = loadingCountry;
    this.loadData = loadData;
    this.unloadingCountry = unloadingCountry;
    this.unloadData = unloadData;
    this.placeData = placeData;

    this.checkLoad = loadingCountry != null || loadData != null || placeData != null;
    this.checkUnload = unloadingCountry != null || unloadData != null || placeData != null;
  }

  boolean matches(HasShipmentInfo item) {
    if (item == null) {
      return false;
    }

    if (checkLoad) {
      String country = Places.getCountryLabel(item.getLoadingCountry());
      String info = Places.getLoadingPlaceInfo(item);

      if (!BeeUtils.isEmpty(info) || !BeeUtils.isEmpty(country)) {
        if (loadingCountry != null && loadingCountry.contains(country)) {
          return true;
        }
        if (loadData != null && loadData.contains(info)) {
          return true;
        }
        if (placeData != null && placeData.contains(info)) {
          return true;
        }
      }
    }

    if (checkUnload) {
      String country = Places.getCountryLabel(item.getUnloadingCountry());
      String info = Places.getUnloadingPlaceInfo(item);

      if (!BeeUtils.isEmpty(info)  || !BeeUtils.isEmpty(country)) {
        if (unloadingCountry!= null && unloadingCountry.contains(country)) {
          return true;
        }

        if (unloadData != null && unloadData.contains(info)) {
          return true;
        }
        if (placeData != null && placeData.contains(info)) {
          return true;
        }
      }
    }

    return false;
  }

  boolean matchesAnyOf(Collection<? extends HasShipmentInfo> items) {
    if (items != null) {
      for (HasShipmentInfo item : items) {
        if (matches(item)) {
          return true;
        }
      }
    }
    return false;
  }
}
