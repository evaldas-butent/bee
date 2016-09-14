package com.butent.bee.client;

import com.google.gwt.user.client.ui.IsWidget;

import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.Set;

public final class UserPanelHelper {
  public static final String VAR_DEFAULT_PANEL = "default";
  public static final IsUserPanel DEFAULT_PANEL = new SimpleUserPanel();
  private static final Set<IsUserPanel> PANEL_SET = new HashSet<>();

  public static void register() {
    PANEL_SET.clear();
    PANEL_SET.add(DEFAULT_PANEL);
    PANEL_SET.add(new NotificationUserPanel());
    LogUtils.getRootLogger().info("Register user panels", PANEL_SET.size());
  }

  public static IsWidget getUserPanel(String panelName) {
    for (IsUserPanel panel : PANEL_SET) {
      if (BeeUtils.same(panel.getName(), panelName)) {
        LogUtils.getRootLogger().info("Switching user panel", panel.getName());
        return panel.create();
      }
    }

    LogUtils.getRootLogger().info("Switching default user panel", DEFAULT_PANEL.getName());
    return DEFAULT_PANEL.create();
  }

  private UserPanelHelper() {

  }
}
