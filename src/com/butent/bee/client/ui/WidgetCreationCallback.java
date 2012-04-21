package com.butent.bee.client.ui;

import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

public class WidgetCreationCallback implements FormFactory.WidgetDescriptionCallback {

  private final Map<String, String> namedWidgets = Maps.newHashMap();
  private final Map<String, String> potentialChildren = Maps.newHashMap();

  private WidgetDescription lastWidgetDescription = null;
  
  public WidgetCreationCallback() {
    super();
  }
  
  public void addBinding(String widgetName, String widgetId, String parentName) {
    Assert.notNull(widgetId);
    if (!BeeUtils.isEmpty(widgetName)) {
      namedWidgets.put(widgetName, widgetId);
    }
    if (!BeeUtils.isEmpty(parentName)) {
      potentialChildren.put(widgetId, parentName);
    }
  }

  public void bind(Widget root, String defaultId) {
    if (potentialChildren.isEmpty()) {
      return;
    }
    Assert.notNull(root);
    Assert.notNull(defaultId);

    for (Map.Entry<String, String> entry : potentialChildren.entrySet()) {
      Widget child = DomUtils.getChildQuietly(root, entry.getKey());
      if (child == null) {
        BeeKeeper.getLog().severe("id:", entry.getKey(), "widget not found");
        continue;
      }

      if (child instanceof HasFosterParent) {
        String parentId =
            BeeUtils.isEmpty(entry.getValue()) ? defaultId : namedWidgets.get(entry.getValue());
        if (BeeUtils.isEmpty(parentId)) {
          BeeKeeper.getLog().severe("child id:", entry.getKey(), "parent name:", entry.getValue(),
              "not found");
        } else {
          ((HasFosterParent) child).setParentId(parentId);
        }
      }
    }
  }

  public WidgetDescription getLastWidgetDescription() {
    return lastWidgetDescription; 
  }

  public void onFailure(Object... messages) {
    BeeKeeper.getLog().severe(messages);
  }

  public void onSuccess(WidgetDescription result) {
    lastWidgetDescription = result;
    
    String id = result.getWidgetId();
    if (!BeeUtils.isEmpty(id)) {
      if (!BeeUtils.isEmpty(result.getWidgetName())) {
        namedWidgets.put(result.getWidgetName(), id);
      }
      if (result.getWidgetType().isChild() || !BeeUtils.isEmpty(result.getParentName())) {
        potentialChildren.put(id, result.getParentName());
      }
    }
  }
}
