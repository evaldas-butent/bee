package com.butent.bee.egg.shared.ui;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public abstract class UiLoader {

  protected class UiRow {
    private String id, className, parent, properties;

    public UiRow() {
    }

    public String getClassName() {
      return className;
    }

    public String getId() {
      return id;
    }

    public String getParent() {
      return parent;
    }

    public String getProperties() {
      return properties;
    }

    public void setClassName(String className) {
      this.className = className;
    }

    public void setId(String id) {
      this.id = id;
    }

    public void setParent(String parent) {
      this.parent = parent;
    }

    public void setProperties(String properties) {
      this.properties = properties;
    }
  }

  protected Logger logger = Logger.getLogger(this.getClass().getName());

  public UiComponent getFormContent(String formName, Object... params) {
    Assert.notEmpty(formName);

    UiComponent root = null;
    List<UiRow> data = getFormData(formName, params);

    if (!BeeUtils.isEmpty(data)) {
      Map<String, List<UiRow>> rows = new LinkedHashMap<String, List<UiRow>>();
      Set<String> orphans = new HashSet<String>();

      for (UiRow row : data) {
        String oId = row.getId();
        String oParent = row.getParent();

        if (orphans.contains(oId)) {
          logger.warning("Dublicate object name: " + oId);
          continue;
        }
        orphans.add(oId);

        if (oId.equals(formName) && BeeUtils.isEmpty(root)) {
          root = UiComponent.createComponent(row.getClassName(), oId);

          if (!BeeUtils.isEmpty(root)) {
            root.loadProperties(row.getProperties());
            continue;
          }
        }

        if (BeeUtils.isEmpty(oParent)) {
          oParent = formName;
        }
        if (!rows.containsKey(oParent)) {
          rows.put(oParent, new ArrayList<UiRow>());
        }
        rows.get(oParent).add(row);
      }

      if (BeeUtils.isEmpty(root)) {
        logger.severe("Missing root component: " + formName);
      } else {
        orphans.remove(formName);
        addChilds(root, rows, orphans);

        for (String orphan : orphans) {
          logger.warning("Object is orphan: " + orphan);
        }
      }
    }
    return root;
  }

  protected abstract List<UiRow> getFormData(String formName, Object... params);

  private void addChilds(UiComponent parent, Map<String, List<UiRow>> rows,
      Set<String> orphans) {
    List<UiRow> childs = rows.get(parent.getId());

    if (!BeeUtils.isEmpty(childs)) {
      for (UiRow row : childs) {
        String oId = row.getId();

        orphans.remove(oId);
        UiComponent child = UiComponent.createComponent(row.getClassName(), oId);

        if (!BeeUtils.isEmpty(child)) {
          child.loadProperties(row.getProperties());
          addChilds(child, rows, orphans);
          parent.addChild(child);
        }
      }
    }
  }
}
