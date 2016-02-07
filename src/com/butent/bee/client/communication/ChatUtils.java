package com.butent.bee.client.communication;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.style.StyleUtils;
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
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ChatUtils {

  private static final DateTimeFormat dateTimeFormat =
      DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.MONTH_DAY);

  public static void applyPresence(Widget widget, Multimap<Presence, Long> presence) {
    if (presence.keySet().size() == 1) {
      Presence p = BeeUtils.peek(presence.keySet());
      StyleUtils.setBackgroundColor(widget, p.getBackground());

    } else if (presence.keySet().size() > 1) {
      List<String> colors = new ArrayList<>();

      List<Presence> keys = new ArrayList<>(presence.keySet());
      Collections.sort(keys);

      for (Presence p : keys) {
        String color = p.getBackground();

        for (int i = 0; i < presence.get(p).size(); i++) {
          colors.add(color);
        }
      }

      widget.getElement().getStyle().setBackgroundImage("linear-gradient(to bottom, "
          + BeeUtils.joinItems(colors) + ")");
    }
  }

  public static String buildUserTitle(Collection<Long> users, boolean addPresence) {
    List<String> lines = new ArrayList<>();

    if (!BeeUtils.isEmpty(users)) {
      for (Long u : users) {
        String signature = Global.getUsers().getSignature(u);

        if (addPresence) {
          String presence = EnumUtils.getCaption(Global.getUsers().getUserPresence(u));
          lines.add(BeeUtils.joinItems(signature, BeeUtils.toLowerCase(presence)));
        } else {
          lines.add(signature);
        }
      }
    }

    return BeeUtils.buildLines(lines);
  }

  public static int countOtherUsers(Collection<Long> users) {
    int size = BeeUtils.size(users);
    if (size > 0 && users.contains(BeeKeeper.getUser().getUserId())) {
      size--;
    }
    return size;
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

  public static Multimap<Presence, Long> getUserPresence(Collection<Long> users) {
    Multimap<Presence, Long> result = ArrayListMultimap.create();
    Presence p;

    for (Long u : users) {
      if (BeeKeeper.getUser().is(u)) {
        p = BeeKeeper.getUser().getPresence();
      } else {
        p = Global.getUsers().getUserPresence(u);
      }

      if (p == null) {
        p = Presence.OFFLINE;
      }

      result.put(p, u);
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
