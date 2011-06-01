package com.butent.bee.server.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.ui.BeeGrid;
import com.butent.bee.shared.ui.GridColumn.ColType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

/**
 * Reads grids structure from xml files and stores them into cache.
 */

@Singleton
@Lock(LockType.READ)
public class GridHolderBean {

  public static final String GRID_SCHEMA = "grid.xsd";
  public static final String GRID_PATH = "grids/";
  
  private static final String TAG_GRID = "BeeGrid";
  private static final String TAG_COLUMNS = "columns";

  private static final String ATTR_VIEW_NAME = "viewName";
  private static final String ATTR_NAME = "name";
  private static final String ATTR_CAPTION = "caption";
  private static final String ATTR_READ_ONLY = "readOnly";
  private static final String ATTR_WIDTH = "width";
  private static final String ATTR_SOURCE = "source";
  private static final String ATTR_RELATION = "relation";

  private static Logger logger = Logger.getLogger(GridHolderBean.class.getName());

  @EJB
  SystemBean sys;

  private Map<String, BeeGrid> gridCache = Maps.newHashMap();

  public BeeGrid getGrid(String gridName) {
    Assert.state(isGrid(gridName), "Not a grid: " + gridName);
    return gridCache.get(gridKey(gridName));
  }

  public void initGrids() {
    gridCache.clear();
  }

  public boolean isGrid(String gridName) {
    if (BeeUtils.isEmpty(gridName)) {
      return false;
    }
    if (!gridCache.containsKey(gridKey(gridName))) {
      initGrid(gridName);
    }
    return gridCache.containsKey(gridKey(gridName));
  }

  private String gridKey(String gridName) {
    Assert.notEmpty(gridName);
    return gridName.trim().toLowerCase();
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    initGrids();
  }

  @Lock(LockType.WRITE)
  private void initGrid(String gridName) {
    String resource = Config.getPath(GRID_PATH + gridName + ".xml");

    if (!BeeUtils.isEmpty(resource)) {
      boolean loaded = false;
      Collection<BeeGrid> grids = loadGrids(resource, Config.getSchemaPath(GRID_SCHEMA));

      if (!BeeUtils.isEmpty(grids)) {
        for (BeeGrid grid : grids) {
          if (BeeUtils.same(grid.getName(), gridName)) {
            if (loaded) {
              LogUtils.warning(logger, resource, "Dublicate grid name:", gridName);
            } else if (grid.isEmpty()) {
              LogUtils.warning(logger, resource, "Grid has no columns defined:", gridName);
            } else {
              loaded = true;
              registerGrid(grid);
            }
          }
        }
      }
      if (loaded) {
        LogUtils.info(logger, "Loaded grid [", gridName, "] description from", resource);
      } else {
        LogUtils.warning(logger, resource, "Grid description not found:", gridName);
      }
    }
  }

  @Lock(LockType.WRITE)
  private Collection<BeeGrid> loadGrids(String resource, String schema) {
    Document xml = XmlUtils.getXmlResource(resource, schema);
    if (BeeUtils.isEmpty(xml)) {
      return null;
    }
    Collection<BeeGrid> data = Lists.newArrayList();
    Element root = xml.getDocumentElement();
    NodeList grids = root.getElementsByTagName(TAG_GRID);

    for (int i = 0; i < grids.getLength(); i++) {
      Element grid = (Element) grids.item(i);

      String vw = grid.getAttribute(ATTR_VIEW_NAME);
      if (!sys.isView(vw)) {
        LogUtils.warning(logger, "Unrecongized view name:", vw);
        continue;
      }
      BeeGrid grd = new BeeGrid(grid.getAttribute(ATTR_NAME)
          , vw
          , grid.getAttribute(ATTR_CAPTION)
          , BeeUtils.toBoolean(grid.getAttribute(ATTR_READ_ONLY)));

      List<Element> cols = XmlUtils.getChildrenElements(XmlUtils.getFirstChildElement(grid, TAG_COLUMNS));

      for (int j = 0; j < cols.size(); j++) {
        Element col = cols.get(j);

        String name = col.getAttribute(ATTR_NAME);
        String caption = col.getAttribute(ATTR_CAPTION);
        boolean readOnly = BeeUtils.toBoolean(col.getAttribute(ATTR_READ_ONLY));
        int width = BeeUtils.toInt(col.getAttribute(ATTR_WIDTH));

        ColType type = ColType.getColType(col.getTagName());

        switch (type) {
          case DATA:
            String colName = col.getAttribute(ATTR_SOURCE);
            BeeView view = sys.getView(vw);

            if (view.hasColumn(colName)) {
              grd.addDataColumn(name, caption, readOnly, width, colName);
            } else {
              LogUtils.warning(logger, "Unrecognized view column:", vw, colName);
            }
            break;

          case RELATED:
            colName = col.getAttribute(ATTR_SOURCE);
            view = sys.getView(vw);

            if (view.hasColumn(colName)) {
              String relTable = sys.getRelation(view.getTable(colName), view.getField(colName));

              if (!BeeUtils.isEmpty(relTable)) {
                String relField = col.getAttribute(ATTR_RELATION);

                if (sys.hasField(relTable, relField)) {
                  grd.addRelatedColumn(name, caption, readOnly, width,
                        colName, relTable, relField);
                } else {
                  LogUtils.warning(logger, "Unrecognized relation field:", relTable, relField);
                }
              } else {
                LogUtils.warning(logger, "Not a relation column:", colName);
              }
            } else {
              LogUtils.warning(logger, "Unrecognized view column:", vw, colName);
            }
            break;

          case CALCULATED:
//            grd.addCalculatedColumn(name, caption, readOnly, width, col
//                  .getAttribute("expression"));
            break;

          case ID:
            grd.addIdColumn(name, caption, width);
            break;

          case VERSION:
            grd.addVersionColumn(name, caption, width);
            break;

          default:
            LogUtils.warning(logger, "Unsupported column type:", col.getTagName());
            break;
        }
      }
      data.add(grd);
    }
    return data;
  }
  
  private void registerGrid(BeeGrid grid) {
    if (!BeeUtils.isEmpty(grid)) {
      gridCache.put(gridKey(grid.getName()), grid);
    }
  }
}
