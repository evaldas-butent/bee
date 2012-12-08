package com.butent.bee.client.modules.transport;

import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class CargoPlaceRenderer extends AbstractCellRenderer {
  private final int dateIndex;
  private final int countryIndex;
  private final int placeIndex;
  private final int terminalIndex;

  public CargoPlaceRenderer(List<? extends IsColumn> columns, String prefix) {
    super(null);

    dateIndex = DataUtils.getColumnIndex(prefix + "Date", columns);
    countryIndex = DataUtils.getColumnIndex(prefix + "CountryName", columns);
    placeIndex = DataUtils.getColumnIndex(prefix + "Place", columns);
    terminalIndex = DataUtils.getColumnIndex(prefix + "Terminal", columns);
  }

  @Override
  public String render(IsRow row) {
    String s = "";
    String tr = "<tr>";
    String tre = "</tr>";
    String tdc = "<td align=\"right\" style=\"font-weight: bold;\">";
    String td = "<td>";
    String tde = "</td>";

    if (!row.isNull(dateIndex)) {
      s = s + tr + tdc + "Data: " + tde + td + new JustDate(row.getInteger(dateIndex)) + tde + tre;
    }
    if (!row.isNull(countryIndex)) {
      s = s + tr + tdc + "Šalis: " + tde + td + row.getString(countryIndex) + tde + tre;
    }
    if (!row.isNull(placeIndex)) {
      s = s + tr + tdc + "Vieta: " + tde + td + row.getString(placeIndex) + tde + tre;
    }
    if (!row.isNull(terminalIndex)) {
      s = s + tr + tdc + "Terminalas: " + tde + td + row.getString(terminalIndex) + tde + tre;
    }
    if (!BeeUtils.isEmpty(s)) {
      s = "<table border=\"0\" style=\"border-collapse: collapse;\">" + s + "</table>";
    }
    return s;
  }
}
