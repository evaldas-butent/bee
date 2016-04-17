package com.butent.bee.shared.ui;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Lists possible actions with a user interface component (like refresh, save, close etc).
 */

public enum Action implements BeeSerializable, HasCaption {
  ADD(FontAwesome.PLUS, Localized.dictionary().actionAdd(), "add", true),
  AUDIT(FontAwesome.HISTORY, Localized.dictionary().actionAudit(), "audit", false),
  AUTO_FIT(FontAwesome.ARROWS_H, Localized.dictionary().autoFit(), "auto-fit", false),
  BOOKMARK(FontAwesome.BOOKMARK_O, Localized.dictionary().actionBookmark(), "bookmark", false),
  CANCEL(FontAwesome.CLOSE, Localized.dictionary().actionCancel(), "cancel", false),
  CLOSE(FontAwesome.CLOSE, Localized.dictionary().actionClose(), "close", false),
  CONFIGURE(FontAwesome.COG, Localized.dictionary().actionConfigure(), "configure", false),
  COPY(FontAwesome.COPY, Localized.dictionary().actionCopy(), "copy", true),
  DELETE(FontAwesome.TRASH_O, Localized.dictionary().actionDelete(), "delete", true),
  EDIT(FontAwesome.EDIT, Localized.dictionary().actionEdit(), "edit", true),
  EXPORT(FontAwesome.FILE_EXCEL_O, Localized.dictionary().actionExport(), "export", false),
  FILTER(FontAwesome.FILTER, Localized.dictionary().actionFilter(), "filter", false),
  MAXIMIZE(FontAwesome.SQUARE_O, Localized.dictionary().actionMaximize(), "maximize", false),
  MENU(FontAwesome.NAVICON, Localized.dictionary().menu(), "menu", false),
  MINIMIZE(FontAwesome.MINUS, Localized.dictionary().actionMinimize(), "minimize", false),
  MERGE(FontAwesome.OBJECT_GROUP, Localized.dictionary().actionMerge(), "merge", true),
  PRINT(FontAwesome.PRINT, Localized.dictionary().actionPrint(), "print", false),
  REFRESH(FontAwesome.REFRESH, Localized.dictionary().actionRefresh(), "refresh", false),
  REMOVE_FILTER(FontAwesome.REMOVE, Localized.dictionary().actionRemoveFilter(), "removeFilter",
      false),
  RESET_SETTINGS(FontAwesome.TIMES_CIRCLE_O, Localized.dictionary().actionResetSettings(),
      "resetSettings", false),
  RIGHTS(FontAwesome.EYE, Localized.dictionary().rights(), "rights", true),
  SAVE(FontAwesome.SAVE, Localized.dictionary().actionSave(), "save", true);

  public static final Set<Action> NO_ACTIONS = new HashSet<>();

  public static Set<Action> parse(String s) {
    Set<Action> result = new HashSet<>();
    if (!BeeUtils.isEmpty(s)) {
      for (String item : NameUtils.NAME_SPLITTER.split(s)) {
        Action action = restore(item);
        if (action != null) {
          result.add(action);
        }
      }
    }
    return result;
  }

  public static Action restore(String s) {
    if (!BeeUtils.isEmpty(s)) {
      for (Action action : Action.values()) {
        if (BeeUtils.same(action.name(), s)) {
          return action;
        }
      }
    }
    return null;
  }

  private final FontAwesome icon;
  private final String caption;

  private final String styleSuffix;
  private final boolean disablable;

  Action(FontAwesome icon, String caption, String styleSuffix, boolean disablable) {
    this.icon = icon;
    this.caption = caption;
    this.styleSuffix = styleSuffix;
    this.disablable = disablable;
  }

  @Override
  public void deserialize(String s) {
    Assert.untouchable();
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public FontAwesome getIcon() {
    return icon;
  }

  public String getStyleName() {
    return BeeConst.CSS_CLASS_PREFIX + "Action-" + styleSuffix;
  }

  public String getStyleSuffix() {
    return styleSuffix;
  }

  public boolean isDisablable() {
    return disablable;
  }

  @Override
  public String serialize() {
    return this.name();
  }
}
