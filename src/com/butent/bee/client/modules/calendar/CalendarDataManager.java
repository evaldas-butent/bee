package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.calendar.CalendarConstants.ItemType;
import com.butent.bee.shared.modules.calendar.CalendarItem;
import com.butent.bee.shared.modules.calendar.CalendarTask;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CalendarDataManager {

  private static BeeLogger logger = LogUtils.getLogger(CalendarDataManager.class);

  private final List<CalendarItem> items = Lists.newArrayList();
  private Range<DateTime> range;

  public CalendarDataManager() {
    super();
  }

  public void addItem(CalendarItem item) {
    if (item != null) {
      items.add(item);
    }
  }

  public List<CalendarItem> getItems() {
    return items;
  }

  public void loadItems(long calendarId, final Range<DateTime> visibleRange, boolean force,
      final IntCallback callback) {

    if (!force && getRange() != null && visibleRange != null
        && getRange().encloses(visibleRange)) {
      if (callback != null) {
        callback.onSuccess(getSize());
      }
      return;
    }

    ParameterList params = CalendarKeeper.createRequestParameters(SVC_GET_CALENDAR_ITEMS);
    params.addQueryItem(PARAM_CALENDAR_ID, calendarId);

    if (visibleRange != null) {
      params.addQueryItem(PARAM_START_TIME, visibleRange.lowerEndpoint().getTime());
      params.addQueryItem(PARAM_END_TIME, visibleRange.upperEndpoint().getTime());
    }

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        items.clear();

        if (!response.isEmpty()) {
          Map<String, String> data = Codec.beeDeserializeMap(response.getResponseAsString());

          for (Map.Entry<String, String> entry : data.entrySet()) {
            ItemType type = EnumUtils.getEnumByName(ItemType.class, entry.getKey());
            if (type == null) {
              logger.severe("item type not recognized", entry.getKey());
              continue;
            }

            String[] arr = Codec.beeDeserializeCollection(entry.getValue());
            if (ArrayUtils.isEmpty(arr)) {
              logger.warning("item type", entry.getKey(), "has no values");
              continue;
            }

            for (String item : arr) {
              switch (type) {
                case APPOINTMENT:
                  addItem(new Appointment(BeeRow.restore(item)));
                  break;
                case TASK:
                  addItem(CalendarTask.restore(item));
                  break;
              }
            }
          }
        }

        setRange(visibleRange);
        if (callback != null) {
          callback.onSuccess(getSize());
        }
      }
    });
  }

  public boolean removeAppointment(long id) {
    int index = getAppointmentIndex(id);
    if (BeeConst.isUndef(index)) {
      return false;
    }

    items.remove(index);
    return true;
  }

  public void sort() {
    if (items.size() > 1) {
      Collections.sort(items);
    }
  }

  private int getAppointmentIndex(long id) {
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).getId() == id && items.get(i).getItemType() == ItemType.APPOINTMENT) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  private Range<DateTime> getRange() {
    return range;
  }

  private int getSize() {
    return items.size();
  }

  private void setRange(Range<DateTime> range) {
    this.range = range;
  }
}