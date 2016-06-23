package com.butent.bee.client.render;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.UserInfo;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.Colors;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.export.XCell;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.export.XStyle;

/**
 * Renderer for styling of data creation and modification in row based UI data structures.
 * 
 * <p>
 * This renderer creates simple div of html element with css style rule where indicates is row are
 * new or updated.
 * </p>
 * 
 */
public abstract class AbstractRowModeRenderer extends AbstractCellRenderer {

  /**
   * Default name of css-class in new row cell mode.
   */
  public static final String STYLE_MODE_NEW = BeeConst.CSS_CLASS_PREFIX + "row-Mode-new";

  /**
   * Default name of css-class in updated row cell mode.
   */
  public static final String STYLE_MODE_UPD = BeeConst.CSS_CLASS_PREFIX + "row-Mode-upd";

  /**
   * List of data store modes for rows.
   */
  public enum RowMode {
    /**
     * Mode of new created data row.
     */
    NEW,

    /**
     * Mode of modified data row.
     */
    UPD
  }

  @Override
  public String render(IsRow row) {
    RowMode mode = getMode(row);

    if (mode == null) {
      return BeeConst.STRING_EMPTY;
    }

    switch (mode) {
      case NEW:
        return renderMode(getNewRowModeStyleName());
      case UPD:
        return renderMode(getUpdatedRowModeStyleName());
    }

    Assert.untouchable();
    return null;
  }

  @Override
  public XCell export(IsRow row, int cellIndex, Integer styleRef, XSheet sheet) {
    RowMode mode = getMode(row);
    if (mode == null) {
      return null;
    }

    XStyle style = new XStyle();

    switch (mode) {
      case NEW:
        style.setColor(getNewRowModeColor());
        break;
      case UPD:
        style.setColor(getUpdatedRowModeColor());
        break;
    }

    return XCell.forStyle(cellIndex, sheet.registerStyle(style));
  }

  /**
   * Creates instance of row based UI data cell renderer.
   */
  public AbstractRowModeRenderer() {
    super(null);
  }

  /**
   * Returns html name or code of text color for exported cells of data having NEW mode.
   * 
   * @return name or code of text color in html
   */
  public String getNewRowModeColor() {
    return Colors.LIGHTGREEN;
  }

  /**
   * Returns css-class name of data cell with NEW mode.
   * 
   * @return css-class name of data cell
   */
  public String getNewRowModeStyleName() {
    return STYLE_MODE_NEW;
  }

  /**
   * Returns html name or code of text color for exported cells of data having UPD mode.
   * 
   * @return name or code of text color in html
   */
  public String getUpdatedRowModeColor() {
    return Colors.YELLOW;
  }

  /**
   * Returns css-class name of data cell with UPD mode.
   * 
   * @return css-class name of data cell
   */
  public String getUpdatedRowModeStyleName() {
    return STYLE_MODE_UPD;
  }

  /**
   * Returns true if row has property or field where current system user can identify modifications
   * of data. This method identify source of data changes in row and check can current system user
   * view changes of data.
   * 
   * @param row reference of data row where store statuses of data modifications by user.
   * @param userId reference from {@link getUserId()}
   * @return true if user has modifications of data.
   */
  public abstract boolean hasUserProperty(IsRow row, Long userId);

  /**
   * Returns time in milliseconds when current system user has viewed data row.
   * 
   * @param row reference of data row where store statuses of data modifications by user.
   * @param userId reference from {@link getUserId()}
   * @return time in milliseconds of last viewed data.
   */
  public abstract Long getLastAccess(IsRow row, Long userId);

  /**
   * Returns time in milliseconds when current system user has modifications of data row.
   * 
   * @param row reference of data row where store statuses of data modifications by user.
   * @param userId reference from {@link getUserId()}
   * @return time in milliseconds of last viewed data.
   */
  public abstract Long getLastUpdate(IsRow row, Long userId);

  /**
   * Returns Id of user (usually current system user) where using for filter modifications of data.
   * 
   * @return user Id
   * @see BeeKeeper#getUser()
   * @see UserInfo#getUserId()
   */
  protected Long getUserId() {
    return BeeKeeper.getUser().getUserId();
  }

  private static String renderMode(String styleName) {
    return "<div class=\"" + styleName + "\"></div>";
  }

  private RowMode getMode(IsRow row) {
    if (row == null) {
      return null;
    }

    Long userId = getUserId();

    if (!hasUserProperty(row, userId)) {
      return null;
    }

    Long access = getLastAccess(row, userId);
    if (access == null) {
      return RowMode.NEW;
    }

    Long update = getLastUpdate(row, userId);
    if (update != null && access < update) {
      return RowMode.UPD;
    } else {
      return null;
    }
  }

}
