package com.butent.bee.client.modules.transport;

import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.FlagRenderer;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class CargoPlaceRenderer extends AbstractGridInterceptor {

  private static class PlaceRenderer extends AbstractCellRenderer {

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
    private final int codeIndex;
    private final int placeIndex;
    private final int terminalIndex;

    private final FlagRenderer flagRenderer;

    public PlaceRenderer(List<? extends IsColumn> columns, String prefix) {
      super(null);

      dateIndex = DataUtils.getColumnIndex(prefix + "Date", columns);
      countryIndex = DataUtils.getColumnIndex(prefix + "CountryName", columns);
      codeIndex = DataUtils.getColumnIndex(prefix + "CountryCode", columns);
      placeIndex = DataUtils.getColumnIndex(prefix + "Place", columns);
      terminalIndex = DataUtils.getColumnIndex(prefix + "Terminal", columns);

      if (codeIndex != BeeConst.UNDEF) {
        flagRenderer = new FlagRenderer(CellSource.forColumn(columns.get(codeIndex), codeIndex));
      } else {
        flagRenderer = null;
      }
    }

    @Override
    public String render(IsRow row) {
      StringBuilder sb = new StringBuilder();

      if (!row.isNull(dateIndex)) {
        sb.append(renderTr("Data", row.getDate(dateIndex).toString()));
      }
      if (!row.isNull(countryIndex)) {
        String s = row.getString(countryIndex);

        if (flagRenderer != null) {
          s = BeeUtils.joinWords(flagRenderer.render(row), s);
        }
        sb.append(renderTr("Šalis", s));
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

  @Override
  public AbstractCellRenderer getRenderer(String columnId, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription) {

    if (BeeUtils.inListSame(columnId, "Loading", "Unloading")) {
      return new PlaceRenderer(dataColumns, columnId);
    } else {
      return null;
    }
  }
}
