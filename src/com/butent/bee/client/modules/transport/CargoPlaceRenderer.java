package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.FlagRenderer;
import com.butent.bee.client.widget.DateTimeLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CargoPlaceRenderer extends AbstractCellRenderer {

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "places-";

  final Map<String, Pair<Integer, String>> data = new LinkedHashMap<>();
  private final FlagRenderer flagRenderer;
  private final int postIndex;

  public CargoPlaceRenderer(List<? extends IsColumn> columns, String options) {
    super(null);
    String prefix = BeeUtils.nvl(options, BeeConst.STRING_EMPTY);

    data.put(COL_PLACE_DATE, Pair.of(DataUtils.getColumnIndex(prefix + COL_PLACE_DATE, columns),
        Localized.dictionary().date()));
    data.put(COL_PLACE_NOTE, Pair.of(DataUtils.getColumnIndex(prefix + COL_PLACE_NOTE, columns),
        Localized.dictionary().note()));
    data.put(COL_PLACE_COMPANY,
        Pair.of(DataUtils.getColumnIndex(prefix + COL_PLACE_COMPANY, columns),
            Localized.dictionary().company()));
    data.put(COL_PLACE_CONTACT,
        Pair.of(DataUtils.getColumnIndex(prefix + COL_PLACE_CONTACT, columns),
            Localized.dictionary().contact()));
    data.put(COL_PLACE_ADDRESS,
        Pair.of(DataUtils.getColumnIndex(prefix + COL_PLACE_ADDRESS, columns),
            Localized.dictionary().address()));
    data.put(COL_PLACE_CITY, Pair.of(DataUtils.getColumnIndex(prefix + "CityName", columns),
        Localized.dictionary().city()));
    data.put(COL_PLACE_COUNTRY,
        Pair.of(DataUtils.getColumnIndex(prefix + "CountryName", columns),
            Localized.dictionary().country()));
    data.put(COL_PLACE_NUMBER,
        Pair.of(DataUtils.getColumnIndex(prefix + COL_PLACE_NUMBER, columns),
            Localized.dictionary().ref()));

    int codeIndex = DataUtils.getColumnIndex(prefix + "CountryCode", columns);

    if (!BeeConst.isUndef(codeIndex)) {
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

      if (!BeeConst.isUndef(itemInfo.getA())) {
        String txt = row.getString(itemInfo.getA());

        if (!BeeUtils.isEmpty(txt)) {
          r++;
          table.setHtml(r, 0, itemInfo.getB(), STYLE_PREFIX + "caption");

          if (BeeUtils.same(item, COL_PLACE_DATE)) {
            DateTimeLabel dt = new DateTimeLabel(false);
            dt.setValue(new DateTime(BeeUtils.toLong(txt)));
            table.setWidget(r, 1, dt);

          } else if (BeeUtils.same(item, COL_PLACE_COUNTRY)) {
            if (flagRenderer != null) {
              txt = BeeUtils.joinWords(flagRenderer.render(row), txt);
            }
            table.setHtml(r, 1, txt);

          } else if (BeeUtils.same(item, COL_PLACE_CITY)) {
            if (!BeeConst.isUndef(postIndex)) {
              txt = BeeUtils.joinItems(txt, row.getString(postIndex));
            }
            table.setHtml(r, 1, txt);

          } else {
            table.setHtml(r, 1, txt);
          }
        }
      }
    }
    return table.getRowCount() > 0 ? table.toString() : BeeConst.STRING_EMPTY;
  }
}
