package com.butent.bee.client.communication;

import com.google.gwt.user.client.ui.IsWidget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasHtml;
import com.butent.bee.shared.communication.Presence;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ChatUtils {

  private static final DateTimeFormat dateTimeFormat =
      DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.MONTH_DAY);

  public static int countOtherUsers(Collection<Long> users) {
    int size = BeeUtils.size(users);
    if (size > 0 && users.contains(BeeKeeper.getUser().getUserId())) {
      size--;
    }
    return size;
  }

  public static Map<Presence, Integer> countUserPresence(Collection<Long> users) {
    EnumMap<Presence, Integer> map = new EnumMap<>(Presence.class);

    Presence p;
    int count;

    for (Long u : users) {
      if (BeeKeeper.getUser().is(u)) {
        p = BeeKeeper.getUser().getPresence();
      } else {
        p = Global.getUsers().getUserPresence(u);
      }

      if (p == null) {
        p = Presence.OFFLINE;
      }

      if (map.containsKey(p)) {
        count = map.get(p);
      } else {
        count = 0;
      }

      map.put(p, count + 1);
    }

    return map;
  }

  public static String elapsed(long start) {
    if (start <= 0) {
      return null;
    }

    long time = System.currentTimeMillis() - start;

    if (time <= 0) {
      return BeeConst.STRING_EMPTY;
    } else if (time < TimeUtils.MILLIS_PER_SECOND) {
      return BeeUtils.toString(time) + "ms";
    } else if (time < TimeUtils.MILLIS_PER_MINUTE) {
      return BeeUtils.toString(time / TimeUtils.MILLIS_PER_SECOND) + "s";
    } else if (time < TimeUtils.MILLIS_PER_HOUR) {
      return BeeUtils.toString(time / TimeUtils.MILLIS_PER_MINUTE) + "m";
    } else if (time < TimeUtils.MILLIS_PER_DAY) {
      return BeeUtils.toString(time / TimeUtils.MILLIS_PER_HOUR) + "h";
    } else {
      return dateTimeFormat.format(new DateTime(start));
    }
  }

  public static List<Long> getOtherUsers(List<Long> users) {
    List<Long> result = new ArrayList<>();

    if (!BeeUtils.isEmpty(users)) {
      for (Long u : users) {
        if (DataUtils.isId(u) && !result.contains(u) && !BeeKeeper.getUser().is(u)) {
          result.add(u);
        }
      }
    }

    return result;
  }

  public static boolean needsRefresh(long millis) {
    return System.currentTimeMillis() - millis < TimeUtils.MILLIS_PER_DAY
        + TimeUtils.MILLIS_PER_HOUR;
  }

  public static void renderOtherUsers(HasIndexedWidgets container, Collection<Long> users,
      String itemStyleName) {

    Assert.notNull(container);
    if (!container.isEmpty()) {
      container.clear();
    }

    if (BeeUtils.isEmpty(users)) {
      return;
    }

    for (Long userId : users) {
      if (Objects.equals(userId, BeeKeeper.getUser().getUserId())) {
        continue;
      }

      UserData userData = Global.getUsers().getUserData(userId);
      if (userData != null) {
        CustomDiv widget = new CustomDiv(itemStyleName);
        widget.setText(userData.getFirstName());
        widget.setTitle(userData.getUserSign());

        container.add(widget);
      }
    }
  }

  public static <T extends HasHtml & IsWidget> void updateTime(T widget, long time) {
    if (widget != null && time > 0) {
      widget.setText(elapsed(time));
      widget.asWidget().setTitle(TimeUtils.renderDateTime(time));
    }
  }

  private ChatUtils() {
  }
}
