package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.FlagRenderer;
import com.butent.bee.client.widget.DateTimeLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.html.builder.elements.Br;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class CargoPlaceRenderer extends AbstractCellRenderer {

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "places-";

  final Map<String, String> data = new LinkedHashMap<>();
  private final FlagRenderer flagRenderer;

  public CargoPlaceRenderer(CellSource source) {
    super(source);

    Dictionary dic = Localized.dictionary();

    data.put(COL_PLACE_DATE, dic.date());
    data.put(COL_PLACE_NOTE, dic.note());
    data.put(COL_PLACE_COMPANY, dic.company());
    data.put(COL_PLACE_CONTACT, dic.contact());
    data.put(COL_PLACE_ADDRESS, dic.address());
    data.put(COL_PLACE_CITY, dic.city());
    data.put(COL_PLACE_COUNTRY, dic.country());
    data.put(COL_PLACE_NUMBER, dic.ref());

    flagRenderer = new FlagRenderer(CellSource.forProperty(COL_PLACE_COUNTRY, null,
        ValueType.TEXT));
  }

  @Override
  public String render(IsRow row) {
    HtmlTable table = new HtmlTable();
    table.addStyleName(STYLE_PREFIX + "table");
    int r = -1;

    String handlingData = getString(row);

    if (BeeUtils.isEmpty(handlingData)) {
      return BeeConst.STRING_EMPTY;
    }

    for (SimpleRowSet.SimpleRow handle : SimpleRowSet.restore(handlingData)) {
      for (String column : data.keySet()) {

        if (!handle.hasColumn(column)) {
          continue;
        }

        String txt = handle.getValue(column);

        if (BeeUtils.isEmpty(txt)) {
          continue;
        }

        r++;
        table.setHtml(r, 0, data.get(column), STYLE_PREFIX + "caption");

        switch (column) {
          case COL_PLACE_DATE:
            DateTimeLabel dt = new DateTimeLabel(false);
            dt.setValue(new DateTime(BeeUtils.toLong(txt)));
            table.setWidget(r, 1, dt);
            break;
          case COL_PLACE_COUNTRY:
            String country = handle.hasColumn(ALS_COUNTRY_NAME)
                ? BeeUtils.nvl(handle.getValue(ALS_COUNTRY_NAME), "") : "";
            String code = handle.hasColumn(COL_COUNTRY + COL_COUNTRY_CODE)
                ? BeeUtils.nvl(handle.getValue(COL_COUNTRY + COL_COUNTRY_CODE), "") : "";

            String flag = "";

            if (!BeeUtils.isEmpty(code) && flagRenderer != null) {
              BeeRow fr = new BeeRow(0, 0);
              fr.setProperty(COL_PLACE_COUNTRY, code);

              flag = flagRenderer.render(fr);
            }

            table.setHtml(r, 1, BeeUtils.joinWords(flag, country, code));
            break;
          case COL_PLACE_CITY:
            txt = handle.hasColumn(ALS_CITY_NAME)
                ? BeeUtils.nvl(handle.getValue(ALS_CITY_NAME), "") : "";
            String postIndex = handle.hasColumn(COL_PLACE_POST_INDEX)
                ? BeeUtils.nvl(handle.getValue(COL_PLACE_POST_INDEX), "") : "";

            table.setHtml(r, 1, BeeUtils.joinWords(txt, postIndex));
            break;
          default:
            table.setHtml(r, 1, txt);
            break;
        }
      }
      r++;
      table.setHtml(r, 0, new Br().build());
    }
    return table.getRowCount() > 0 ? table.toString() : BeeConst.STRING_EMPTY;
  }
}
