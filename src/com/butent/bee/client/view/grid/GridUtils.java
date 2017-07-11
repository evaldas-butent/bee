package com.butent.bee.client.view.grid;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Settings;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.CustomProperties;
import com.butent.bee.shared.data.HasCustomProperties;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.WindowType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GridUtils {

  private static final BeeLogger logger = LogUtils.getLogger(GridUtils.class);

  public static Set<Action> getDisabledActions(GridDescription description,
      GridInterceptor interceptor) {

    Set<Action> result = new HashSet<>();

    Set<Action> actions = (interceptor == null)
        ? description.getDisabledActions()
        : interceptor.getDisabledActions(description.getDisabledActions());

    if (!BeeUtils.isEmpty(actions)) {
      result.addAll(actions);
    }
    return result;
  }

  public static Set<Action> getEnabledActions(GridDescription description,
      GridInterceptor interceptor) {

    Set<Action> result = new HashSet<>();

    Set<Action> actions = (interceptor == null)
        ? description.getEnabledActions()
        : interceptor.getEnabledActions(description.getEnabledActions());

    if (!BeeUtils.isEmpty(actions)) {
      result.addAll(actions);
    }
    return result;
  }

  public static boolean hasPaging(GridDescription gridDescription, Collection<UiOption> uiOptions,
      GridFactory.GridOptions gridOptions) {

    Boolean paging = (gridDescription == null) ? null : gridDescription.getPaging();
    if (gridOptions != null && gridOptions.getPaging() != null) {
      paging = gridOptions.getPaging();
    }

    if (UiOption.hasPaging(uiOptions)) {
      return !BeeUtils.isFalse(paging);
    } else {
      return BeeUtils.isTrue(paging);
    }
  }

  static boolean containsColumn(Collection<ColumnDescription> columnDescriptions, String id) {
    return getColumnDescription(columnDescriptions, id) != null;
  }

  static ColumnDescription getColumnDescription(Collection<ColumnDescription> columnDescriptions,
      String id) {
    if (!BeeUtils.isEmpty(columnDescriptions)) {
      for (ColumnDescription columnDescription : columnDescriptions) {
        if (columnDescription.is(id)) {
          return columnDescription;
        }
      }
    }
    return null;
  }

  static ColumnInfo getColumnInfo(Collection<ColumnInfo> columns, String id) {
    if (!BeeUtils.isEmpty(columns)) {
      for (ColumnInfo columnInfo : columns) {
        if (columnInfo.is(id)) {
          return columnInfo;
        }
      }
    }
    return null;
  }

  static int getColumnIndex(List<ColumnInfo> columns, String id) {
    if (!BeeUtils.isEmpty(columns)) {
      for (int i = 0; i < columns.size(); i++) {
        if (columns.get(i).is(id)) {
          return i;
        }
      }
    }
    return BeeConst.UNDEF;
  }

  static WindowType getEditWindowType(GridDescription gridDescription, boolean isChild) {
    WindowType windowType = gridDescription.getEditWindow();

    if (windowType == null) {
      String wtp = isChild
          ? BeeKeeper.getUser().getChildEditWindow() : BeeKeeper.getUser().getGridEditWindow();

      if (BeeUtils.isEmpty(wtp)) {
        wtp = isChild ? Settings.getChildEditWindow() : Settings.getGridEditWindow();
      }

      windowType = WindowType.parse(wtp);

      if (windowType == null) {
        windowType = isChild ? WindowType.DEFAULT_CHILD_EDIT : WindowType.DEFAULT_GRID_EDIT;
      }
    }

    return windowType;
  }

  static int getIndex(List<String> names, String name) {
    int index = names.indexOf(name);
    if (index < 0) {
      logger.severe("name not found:", name);
    }
    return index;
  }

  static WindowType getNewRowWindowType(GridDescription gridDescription, boolean isChild) {
    WindowType windowType = gridDescription.getNewRowWindow();

    if (windowType == null) {
      String wtp = isChild
          ? BeeKeeper.getUser().getChildNewRowWindow() : BeeKeeper.getUser().getGridNewRowWindow();

      if (BeeUtils.isEmpty(wtp)) {
        wtp = isChild ? Settings.getChildNewRowWindow() : Settings.getGridNewRowWindow();
      }

      windowType = WindowType.parse(wtp);

      if (windowType == null) {
        windowType = isChild ? WindowType.DEFAULT_CHILD_NEW_ROW : WindowType.DEFAULT_GRID_NEW_ROW;
      }
    }

    return windowType;
  }

  static String normalizeValue(String value) {
    return BeeUtils.isEmpty(value) ? null : value.trim();
  }

  static void updateProperties(IsRow target, IsRow source) {
    if (!BeeUtils.isEmpty(target.getProperties())) {
      Long userId = BeeKeeper.getUser().getUserId();
      CustomProperties retain = new CustomProperties();

      for (Map.Entry<String, String> entry : target.getProperties().entrySet()) {
        if (HasCustomProperties.isUserPropertyName(entry.getKey(), userId)) {
          retain.put(entry.getKey(), entry.getValue());
        }
      }

      if (retain.isEmpty() && BeeUtils.isEmpty(source.getProperties())) {
        target.setProperties(null);

      } else {
        target.getProperties().clear();
        if (!retain.isEmpty()) {
          target.getProperties().putAll(retain);
        }
      }
    }

    if (!BeeUtils.isEmpty(source.getProperties())) {
      for (Map.Entry<String, String> entry : source.getProperties().entrySet()) {
        if (isPropertyRelevant(entry.getKey())) {
          target.setProperty(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  private static boolean isPropertyRelevant(String key) {
    if (HasCustomProperties.isUserPropertyName(key)) {
      return BeeKeeper.getUser().is(HasCustomProperties.extractUserIdFromUserPropertyName(key));
    } else {
      return !BeeUtils.isEmpty(key);
    }
  }

  private GridUtils() {
  }
}
