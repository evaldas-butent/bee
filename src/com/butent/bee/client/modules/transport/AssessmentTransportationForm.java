package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.COL_CARGO;
import static com.butent.bee.shared.modules.transport.TransportConstants.COL_TRIP;
import static com.butent.bee.shared.modules.transport.TransportConstants.TBL_CARGO_TRIPS;
import static com.butent.bee.shared.modules.transport.TransportConstants.TBL_ORDER_CARGO;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.output.PrintFormInterceptor;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

public class AssessmentTransportationForm extends PrintFormInterceptor {

  private Widget totals;
  private Widget cargo;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, "Totals")) {
      totals = widget.asWidget();

    } else if (BeeUtils.same(name, COL_CARGO)) {
      cargo = widget.asWidget();
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    if (!BeeUtils.anyNotNull(totals, cargo)) {
      return;
    }
    Queries.getRowSet(TBL_ORDER_CARGO, null,
        Filter.in(Data.getIdColumn(TBL_ORDER_CARGO), TBL_CARGO_TRIPS, COL_CARGO,
            Filter.equals(COL_TRIP, row.getId())),
        new RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            int colWeight = result.getColumnIndex("Weight");
            int colWeightUnit = result.getColumnIndex("WeightUnitName");
            Map<String, Double> weight = Maps.newHashMap();
            int colQuantity = result.getColumnIndex("Quantity");
            int colQuantityUnit = result.getColumnIndex("QuantityUnitName");
            Map<String, Double> quantity = Maps.newHashMap();
            int colVolume = result.getColumnIndex("Volume");
            int colVolumeUnit = result.getColumnIndex("VolumeUnitName");
            Map<String, Double> volume = Maps.newHashMap();

            int colLoadingCountry = result.getColumnIndex("LoadingCountryName");
            int colLoadingAddress = result.getColumnIndex("LoadingAddress");
            int colLoadingDate = result.getColumnIndex("LoadingDate");
            int colUnloadingCountry = result.getColumnIndex("UnloadingCountryName");
            int colUnloadingAddress = result.getColumnIndex("UnloadingAddress");
            int colUnloadingDate = result.getColumnIndex("UnloadingDate");

            LocalizableConstants loc = Localized.getConstants();
            Map<Integer, Pair<Integer, String>> map = Maps.newHashMap();

            map.put(colLoadingAddress, Pair.of(0, loc.trLoadingAddress()));
            map.put(colWeight, Pair.of(1, loc.weight()));
            map.put(colQuantity, Pair.of(2, loc.trdQuantity()));
            map.put(colVolume, Pair.of(3, loc.volume()));
            map.put(colLoadingDate, Pair.of(4, loc.trLoadingDate()));
            map.put(colUnloadingAddress, Pair.of(5, loc.trUnloadingAddress()));
            map.put(colUnloadingDate, Pair.of(6, loc.trUnloadingDate()));

            HtmlTable cargoInfo = new HtmlTable();
            int c = 0;

            for (Pair<Integer, String> pair : map.values()) {
              cargoInfo.setHtml(c, pair.getA(), pair.getB());
            }
            for (BeeRow r : result.getRows()) {
              c++;
              Double value = r.getDouble(colWeight);
              if (BeeUtils.isPositive(value)) {
                String unit = r.getString(colWeightUnit);
                weight.put(unit, BeeUtils.unbox(weight.get(unit)) + value);
                cargoInfo.setHtml(c, map.get(colWeight).getA(), BeeUtils.joinWords(value, unit));
              }
              value = r.getDouble(colQuantity);
              if (BeeUtils.isPositive(value)) {
                String unit = r.getString(colQuantityUnit);
                quantity.put(unit, BeeUtils.unbox(quantity.get(unit)) + value);
                cargoInfo.setHtml(c, map.get(colQuantity).getA(), BeeUtils.joinWords(value, unit));
              }
              value = r.getDouble(colVolume);
              if (BeeUtils.isPositive(value)) {
                String unit = r.getString(colVolumeUnit);
                volume.put(unit, BeeUtils.unbox(volume.get(unit)) + value);
                cargoInfo.setHtml(c, map.get(colVolume).getA(), BeeUtils.joinWords(value, unit));
              }
              for (Integer idx : map.keySet()) {
                if (BeeUtils.inList(idx, colLoadingDate, colUnloadingDate)) {
                  DateTime dt = r.getDateTime(idx);
                  cargoInfo.setHtml(c, map.get(idx).getA(),
                      dt == null ? null : dt.toCompactString());

                } else if (Objects.equal(idx, colLoadingAddress)) {
                  cargoInfo.setHtml(c, map.get(idx).getA(),
                      BeeUtils.joinItems(r.getString(colLoadingCountry), r.getString(idx)));

                } else if (Objects.equal(idx, colUnloadingAddress)) {
                  cargoInfo.setHtml(c, map.get(idx).getA(),
                      BeeUtils.joinItems(r.getString(colUnloadingCountry), r.getString(idx)));
                }
              }
            }
            if (totals != null) {
              HtmlTable table = new HtmlTable();

              for (int i = 0; i < 3; i++) {
                int r = 0;
                table.setHtml(r++, i, (i == 0) ? loc.weight()
                    : ((i == 1) ? loc.trdQuantity() : loc.volume()));

                Map<String, Double> m = (i == 0) ? weight : ((i == 1) ? quantity : volume);

                for (String unit : m.keySet()) {
                  table.setHtml(r++, i,
                      BeeUtils.joinWords(BeeUtils.toString(m.get(unit), 3), unit));
                }
              }
              totals.getElement().setInnerHTML(table.getElement().getString());
            }
            if (cargo != null) {
              cargo.getElement().setInnerHTML(cargoInfo.getElement().getString());
            }
          }
        });
    super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new AbstractFormInterceptor() {
      @Override
      public void afterCreateWidget(String name, IdentifiableWidget widget,
          WidgetDescriptionCallback callback) {

        if (BeeUtils.same(name, COL_CARGO)) {
          widget.getElement().setInnerHTML(cargo.getElement().getInnerHTML());
        }
        super.afterCreateWidget(name, widget, callback);
      }

      @Override
      public FormInterceptor getInstance() {
        return null;
      }
    };
  }

  @Override
  public FormInterceptor getInstance() {
    return new AssessmentTransportationForm();
  }
}
