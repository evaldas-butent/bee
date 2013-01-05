package com.butent.bee.client.modules.transport;

import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

import java.util.List;

class CargoPlaceRenderer extends AbstractCellRenderer {
  
  private static final String STYLE_PREFIX = "bee-tr-PlaceRenderer-";

  private static final String TR = "<tr>";
  private static final String TRE = "</tr>";

  private static final String TD_0 = "<td class=\"" + STYLE_PREFIX + "labelCell\">";
  private static final String TD_1 = "<td class=\"" + STYLE_PREFIX + "valueCell\">";
  private static final String TDE = "</td>";

  private static final String TBL = "<table class=\"" + STYLE_PREFIX + "table\">";
  private static final String TBLE = "</table>";
  
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
    StringBuilder sb = new StringBuilder();

    if (!row.isNull(dateIndex)) {
      sb.append(renderTr("Data", row.getDate(dateIndex).toString()));
    }
    if (!row.isNull(countryIndex)) {
      sb.append(renderTr("Šalis", row.getString(countryIndex)));
    }
    if (!row.isNull(placeIndex)) {
      sb.append(renderTr("Vieta", row.getString(placeIndex)));
    }
    if (!row.isNull(terminalIndex)) {
      sb.append(renderTr("Term", row.getString(terminalIndex)));
    }

    return (sb.length() > 0) ? TBL + sb.toString() + TBLE : BeeConst.STRING_EMPTY;
  }
  
  private String renderTr(String label, String value) {
    return TR + TD_0 + label + ":" + TDE + TD_1 + value + TDE + TRE;
  }
}
