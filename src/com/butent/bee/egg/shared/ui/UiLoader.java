package com.butent.bee.egg.shared.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

public abstract class UiLoader {

  public class UiRow {
    private final String id, className, parent, properties;

    public UiRow(String id, String className, String parent, String properties) {
      this.id = id;
      this.className = className;
      this.parent = parent;
      this.properties = properties;
    }

    private String getId() {
      return id;
    }

    private String getClassName() {
      return className;
    }

    private String getParent() {
      return parent;
    }

    private String getProperties() {
      return properties;
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

  private void addChilds(UiComponent parent, Map<String, List<UiRow>> rows,
      Set<String> orphans) {
    List<UiRow> childs = rows.get(parent.getId());

    if (!BeeUtils.isEmpty(childs)) {
      for (UiRow row : childs) {
        String oId = row.getId();

        orphans.remove(oId);
        UiComponent child = UiComponent
            .createComponent(row.getClassName(), oId);

        if (!BeeUtils.isEmpty(child)) {
          child.loadProperties(row.getProperties());
          addChilds(child, rows, orphans);
          parent.addChild(child);
        }
      }
    }
  }

  protected abstract List<UiRow> getFormData(String formName, Object... params);
}
