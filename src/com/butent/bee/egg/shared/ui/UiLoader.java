package com.butent.bee.egg.shared.ui;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public abstract class UiLoader {

  protected class UiRow implements Comparable<UiRow> {
    private String id, className, caption, parent, properties;

    private int order;

    public UiRow() {
    }

    @Override
    public int compareTo(UiRow row) {
      int z = BeeUtils.compare(getParent(), row.getParent());

      if (z == BeeConst.COMPARE_EQUAL) {
        z = BeeUtils.compare(getOrder(), row.getOrder());

        if (z == BeeConst.COMPARE_EQUAL) {
          z = BeeUtils.compare(getId(), row.getId());
        }
      }
      return z;
    }

    public String getCaption() {
      return caption;
    }

    public String getClassName() {
      return className;
    }

    public String getId() {
      return id;
    }

    public int getOrder() {
      return order;
    }

    public String getParent() {
      return parent;
    }

    public String getProperties() {
      return properties;
    }

    public void setCaption(String caption) {
      this.caption = caption;
    }

    public void setClassName(String className) {
      this.className = className;
    }

    public void setId(String id) {
      this.id = id;
    }

    public void setOrder(int order) {
      this.order = order;
    }

    public void setParent(String parent) {
      this.parent = parent;
    }

    public void setProperties(String properties) {
      this.properties = properties;
    }
  }

  protected Logger logger = Logger.getLogger(this.getClass().getName());

  public UiComponent getForm(String formName, Object... params) {
    Assert.notEmpty(formName);

    List<UiRow> data = getFormData(formName, params);
    return buildObject(formName, data);
  }

  public UiComponent getMenu(String menuName, Object... params) {
    Assert.notEmpty(menuName);

    List<UiRow> data = getMenuData(menuName, params);
    return buildObject(menuName, data);
  }

  protected abstract List<UiRow> getFormData(String formName, Object... params);

  protected abstract List<UiRow> getMenuData(String menuName, Object... params);

  private void addChilds(UiComponent parent, Map<String, List<UiRow>> rows,
      Set<String> orphans) {
    List<UiRow> childs = rows.get(parent.getId());

    if (!BeeUtils.isEmpty(childs)) {
      for (UiRow row : childs) {
        String oId = row.getId();

        orphans.remove(oId);
        UiComponent child = UiComponent.createComponent(row.getClassName(), oId);

        if (!BeeUtils.isEmpty(child)) {
          child.setCaption(row.getCaption());
          child.loadProperties(row.getProperties());
          addChilds(child, rows, orphans);
          parent.addChild(child);
        }
      }
    }
  }

  private UiComponent buildObject(String rootName, List<UiRow> data) {
    UiComponent root = null;

    if (!BeeUtils.isEmpty(data)) {
      Collections.sort(data);

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

        if (oId.equals(rootName) && BeeUtils.isEmpty(root)) {
          root = UiComponent.createComponent(row.getClassName(), oId);

          if (!BeeUtils.isEmpty(root)) {
            root.setCaption(row.getCaption());
            root.loadProperties(row.getProperties());
            continue;
          }
        }

        if (BeeUtils.isEmpty(oParent)) {
          oParent = rootName;
        }
        if (!rows.containsKey(oParent)) {
          rows.put(oParent, new ArrayList<UiRow>());
        }
        rows.get(oParent).add(row);
      }

      if (BeeUtils.isEmpty(root)) {
        logger.severe("Missing root component: " + rootName);
      } else {
        orphans.remove(rootName);
        addChilds(root, rows, orphans);

        for (String orphan : orphans) {
          logger.warning("Object is orphan: " + orphan);
        }
      }
    }
    return root;
  }
}
