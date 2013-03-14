package com.butent.bee.client.modules.transport.charts;

import com.butent.bee.shared.time.JustDate;

interface HasShipmentInfo {
  
  Long getLoadingCountry();
  JustDate getLoadingDate();
  String getLoadingPlace();
  String getLoadingTerminal();

  Long getUnloadingCountry();
  JustDate getUnloadingDate();
  String getUnloadingPlace();
  String getUnloadingTerminal();
}
