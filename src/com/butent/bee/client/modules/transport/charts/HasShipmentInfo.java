package com.butent.bee.client.modules.transport.charts;

import com.butent.bee.shared.time.JustDate;

interface HasShipmentInfo {

  Long getLoadingCountry();

  JustDate getLoadingDate();

  String getLoadingPlace();

  String getLoadingPostIndex();

  Long getLoadingCity();

  String getLoadingNumber();

  Long getUnloadingCountry();

  JustDate getUnloadingDate();

  String getUnloadingPlace();

  String getUnloadingPostIndex();

  Long getUnloadingCity();

  String getUnloadingNumber();
}
