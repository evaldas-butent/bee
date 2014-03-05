package com.butent.bee.client.modules.transport.charts;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.images.Flags;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.modules.classifiers.ClassifiersConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

final class Places {

  private static BeeRowSet countries;
  
  private static int countryCodeIndex = BeeConst.UNDEF;
  private static int countryNameIndex = BeeConst.UNDEF;
  
  private static ValueType placeDateType;

  static String getCountryFlag(Long countryId) {
    if (!DataUtils.isId(countryId) || DataUtils.isEmpty(countries)) {
      return null;
    }
    
    BeeRow row = countries.getRowById(countryId);
    if (row == null) {
      return null;
    }

    String code = row.getString(countryCodeIndex);
    if (BeeUtils.isEmpty(code)) {
      return null;
    } else {
      return Flags.get(code);
    }
  }
  
  static String getCountryLabel(Long countryId) {
    if (!DataUtils.isId(countryId) || DataUtils.isEmpty(countries)) {
      return null;
    }

    BeeRow row = countries.getRowById(countryId);
    if (row == null) {
      return null;
    }

    String label = row.getString(countryCodeIndex);
    if (BeeUtils.isEmpty(label)) {
      return BeeUtils.trim(row.getString(countryNameIndex));
    } else {
      return BeeUtils.trim(label).toUpperCase();
    }
  }
  
  static JustDate getLoadingDate(SimpleRow row, String colName) {
    if (getPlaceDateType() == ValueType.DATE_TIME) {
      return JustDate.get(row.getDateTime(colName));
    } else {
      return row.getDate(colName);
    }
  }
  
  static String getLoadingInfo(HasShipmentInfo item) {
    if (item == null) {
      return null;
    } else {
      return BeeUtils.joinWords(item.getLoadingDate(), getLoadingPlaceInfo(item));
    }
  }
  
  static String getLoadingPlaceInfo(HasShipmentInfo item) {
    if (item == null) {
      return null;
    } else {
      return getPlaceInfo(item.getLoadingCountry(), item.getLoadingPlace(),
          item.getLoadingTerminal());
    }
  }

  static ValueType getPlaceDateType() {
    if (Places.placeDateType == null) {
      Places.placeDateType = Data.getColumnType(TransportConstants.TBL_CARGO_PLACES,
          TransportConstants.COL_PLACE_DATE);
    }
    return Places.placeDateType;
  }
  
  static String getPlaceInfo(Long countryId, String placeName, String terminal) {
    String countryLabel = getCountryLabel(countryId);

    if (BeeUtils.isEmpty(countryLabel) || BeeUtils.containsSame(placeName, countryLabel)
        || BeeUtils.containsSame(terminal, countryLabel)) {
      return BeeUtils.joinNoDuplicates(BeeConst.STRING_SPACE, placeName, terminal);
    } else {
      return BeeUtils.joinNoDuplicates(BeeConst.STRING_SPACE, countryLabel, placeName, terminal);
    }
  }
  
  static JustDate getUnloadingDate(SimpleRow row, String colName) {
    if (getPlaceDateType() == ValueType.DATE_TIME) {
      return JustDate.get(row.getDateTime(colName));
    } else {
      return row.getDate(colName);
    }
  }

  static String getUnloadingInfo(HasShipmentInfo item) {
    if (item == null) {
      return null;
    } else {
      return BeeUtils.joinWords(item.getUnloadingDate(), getUnloadingPlaceInfo(item));
    }
  }

  static String getUnloadingPlaceInfo(HasShipmentInfo item) {
    if (item == null) {
      return null;
    } else {
      return getPlaceInfo(item.getUnloadingCountry(), item.getUnloadingPlace(),
          item.getUnloadingTerminal());
    }
  }

  static void setCountries(BeeRowSet rowSet) {
    if (!DataUtils.isEmpty(rowSet)) {
      Places.countries = rowSet;
      
      if (BeeConst.isUndef(countryCodeIndex)) {
        Places.countryCodeIndex = rowSet.getColumnIndex(ClassifiersConstants.COL_COUNTRY_CODE);
        Places.countryNameIndex = rowSet.getColumnIndex(ClassifiersConstants.COL_COUNTRY_NAME);
      }
    }
  }

  private Places() {
  }
}
