package com.butent.bee.client.modules.transport;

import com.google.common.collect.Maps;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.FlagRenderer;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.widget.DateLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

public class CargoPlaceRenderer extends AbstractGridInterceptor {

  public static class PlaceRenderer extends AbstractCellRenderer {

    private static final String STYLE_PREFIX = "bee-places-";

    final Map<String, Pair<Integer, String>> data = Maps.newLinkedHashMap();
    private final FlagRenderer flagRenderer;
    private final int postIndex;

    public PlaceRenderer(List<? extends IsColumn> columns, String prefix) {
      super(null);

      data.put(COL_PLACE_DATE, Pair.of(DataUtils.getColumnIndex(prefix + COL_PLACE_DATE, columns),
          Localized.constants.date()));
      data.put(COL_PLACE_CONTACT,
          Pair.of(DataUtils.getColumnIndex(prefix + COL_PLACE_CONTACT, columns),
              Localized.constants.contact()));
      data.put(COL_PLACE_ADDRESS,
          Pair.of(DataUtils.getColumnIndex(prefix + COL_PLACE_ADDRESS, columns),
              Localized.constants.address()));
      data.put(COL_PLACE_CITY, Pair.of(DataUtils.getColumnIndex(prefix + "CityName", columns),
          Localized.constants.city()));
      data.put(COL_PLACE_COUNTRY,
          Pair.of(DataUtils.getColumnIndex(prefix + "CountryName", columns),
              Localized.constants.country()));
      data.put(COL_PLACE_TERMINAL,
          Pair.of(DataUtils.getColumnIndex(prefix + COL_PLACE_TERMINAL, columns),
              Localized.constants.terminal()));

      int codeIndex = DataUtils.getColumnIndex(prefix + "CountryCode", columns);

      if (codeIndex != BeeConst.UNDEF) {
        flagRenderer = new FlagRenderer(CellSource.forColumn(columns.get(codeIndex), codeIndex));
      } else {
        flagRenderer = null;
      }
      postIndex = DataUtils.getColumnIndex(prefix + COL_PLACE_POST_INDEX, columns);
    }

    @Override
    public String render(IsRow row) {
      HtmlTable table = new HtmlTable();
      table.addStyleName(STYLE_PREFIX + "table");
      int r = -1;

      for (String item : data.keySet()) {
        Pair<Integer, String> itemInfo = data.get(item);

        if (itemInfo.getA() != BeeConst.UNDEF) {
          String txt = row.getString(itemInfo.getA());

          if (!BeeUtils.isEmpty(txt)) {
            r++;
            table.setText(r, 0, itemInfo.getB(), STYLE_PREFIX + "caption");

            if (BeeUtils.same(item, COL_PLACE_DATE)) {
              DateLabel dt = new DateLabel(false);
              dt.setValue(new JustDate(BeeUtils.toLong(txt)));
              table.setWidget(r, 1, dt);

            } else if (BeeUtils.same(item, COL_PLACE_COUNTRY)) {
              if (flagRenderer != null) {
                txt = BeeUtils.joinWords(flagRenderer.render(row), txt);
              }
              table.setHTML(r, 1, txt);

            } else if (BeeUtils.same(item, COL_PLACE_CITY)) {
              if (postIndex != BeeConst.UNDEF) {
                txt = BeeUtils.joinItems(txt, row.getString(postIndex));
              }
              table.setText(r, 1, txt);

            } else {
              table.setText(r, 1, txt);
            }
          }
        }
      }
      return table.getRowCount() > 0 ? table.toString() : BeeConst.STRING_EMPTY;
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
