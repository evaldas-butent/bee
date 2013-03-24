package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.Range;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

class Vehicle extends Filterable implements HasDateRange, HasItemName {

  private static final int numberIndex = Data.getColumnIndex(VIEW_VEHICLES, COL_NUMBER);

  private static final int parentModelNameIndex =
      Data.getColumnIndex(VIEW_VEHICLES, COL_PARENT_MODEL_NAME);
  private static final int modelNameIndex = Data.getColumnIndex(VIEW_VEHICLES, COL_MODEL_NAME);
  private static final String modelLabel = Data.getColumnLabel(VIEW_VEHICLES, COL_MODEL);

  private static final int typeNameIndex = Data.getColumnIndex(VIEW_VEHICLES, COL_TYPE_NAME);

  private static final int notesIndex = Data.getColumnIndex(VIEW_VEHICLES, COL_VEHICLE_NOTES);
  private static final String notesLabel = Data.getColumnLabel(VIEW_VEHICLES, COL_VEHICLE_NOTES);

  private static final int startIndex =
      Data.getColumnIndex(VIEW_VEHICLES, COL_VEHICLE_START_DATE);
  private static final int endIndex = Data.getColumnIndex(VIEW_VEHICLES, COL_VEHICLE_END_DATE);
  private static final String startLabel =
      Data.getColumnLabel(VIEW_VEHICLES, COL_VEHICLE_START_DATE);
  private static final String endLabel = Data.getColumnLabel(VIEW_VEHICLES, COL_VEHICLE_END_DATE);

  private final BeeRow row;

  private final Long id;
  private final String number;
  private final String model;
  private final String type;

  private final Range<JustDate> range;
  
  private final String itemName;

  Vehicle(BeeRow row) {
    this.row = row;

    this.id = row.getId();
    this.number = row.getString(numberIndex);
    this.model = BeeUtils.joinWords(row.getString(parentModelNameIndex), 
        row.getString(modelNameIndex));
    this.type = BeeUtils.trim(row.getString(typeNameIndex));

    this.range = ChartHelper.getActivity(row.getDate(startIndex), row.getDate(endIndex));
    
    this.itemName = BeeUtils.joinWords(number, model);
  }

  @Override
  public boolean filter(FilterType filterType, Collection<ChartData> data) {
    boolean match = true;
    
    for (ChartData cd : data) {
      if (cd.getType() == ChartData.Type.VEHICLE_MODEL) {
        match = cd.contains(getModel());
      } else if (cd.getType() == ChartData.Type.VEHICLE_TYPE) {
        match = cd.contains(getType());
      }
      
      if (!match) {
        break;
      }
    }

    return match;
  }

  @Override
  public String getItemName() {
    return itemName;
  }

  @Override
  public Range<JustDate> getRange() {
    return range;
  }

  Long getId() {
    return id;
  }
  
  String getInactivityTitle(Range<JustDate> inactivity) {
    if (inactivity == null || getRange() == null) {
      return BeeConst.STRING_EMPTY;

    } else if (inactivity.hasUpperBound() && getRange().hasLowerBound()
        && BeeUtils.isLess(inactivity.upperEndpoint(), getRange().lowerEndpoint())) {
      return ChartHelper.buildTitle(startLabel, getRange().lowerEndpoint());

    } else if (inactivity.hasLowerBound() && getRange().hasUpperBound()
        && BeeUtils.isMore(inactivity.lowerEndpoint(), getRange().upperEndpoint())) {
      return ChartHelper.buildTitle(endLabel, getRange().upperEndpoint());

    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  String getInfo() {
    return BeeUtils.joinWords(getModel(), getNotes());
  }
  
  String getMessage(String caption) {
    return ChartHelper.buildTitle(caption, getNumber(), modelLabel, getModel(),
        notesLabel, getNotes());
  }
  
  String getModel() {
    return model;
  }

  String getNotes() {
    return BeeUtils.trim(row.getString(notesIndex));
  }
  
  String getNumber() {
    return number;
  }

  String getTitle() {
    return getNotes();
  }

  String getType() {
    return type;
  }
}
