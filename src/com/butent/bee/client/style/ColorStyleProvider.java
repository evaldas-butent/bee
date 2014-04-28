package com.butent.bee.client.style;

import com.google.gwt.safecss.shared.SafeStyles;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.css.CssProperties;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.export.XFont;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.export.XStyle;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public final class ColorStyleProvider implements StyleProvider {

  private static final BeeLogger logger = LogUtils.getLogger(ColorStyleProvider.class);

  public static ColorStyleProvider create(List<? extends IsColumn> columns,
      String bgName, String fgName) {
    Assert.notEmpty(columns);
    Assert.notEmpty(bgName);
    Assert.notEmpty(fgName);

    CellSource bgSource = null;
    CellSource fgSource = null;

    for (int i = 0; i < columns.size(); i++) {
      IsColumn column = columns.get(i);

      if (BeeUtils.same(column.getId(), bgName)) {
        bgSource = CellSource.forColumn(column, i);
        if (fgSource != null) {
          break;
        }

      } else if (BeeUtils.same(column.getId(), fgName)) {
        fgSource = CellSource.forColumn(column, i);
        if (bgSource != null) {
          break;
        }
      }
    }

    if (bgSource == null || fgSource == null) {
      if (bgSource == null) {
        logger.severe("bg column not found:", bgName);
      }
      if (fgSource == null) {
        logger.severe("fg column not found:", fgName);
      }
      return null;

    } else {
      return new ColorStyleProvider(bgSource, fgSource);
    }
  }

  public static ColorStyleProvider create(String viewName, String bgName, String fgName) {
    return create(Data.getColumns(viewName), bgName, fgName);
  }

  public static ColorStyleProvider createDefault(String viewName) {
    return create(viewName, COL_BACKGROUND, COL_FOREGROUND);
  }

  private final CellSource bgSource;
  private final CellSource fgSource;

  private ColorStyleProvider(CellSource bgSource, CellSource fgSource) {
    this.bgSource = bgSource;
    this.fgSource = fgSource;
  }

  @Override
  public Integer getExportStyleRef(IsRow row, XSheet sheet) {
    if (row == null || sheet == null) {
      return null;
    }
    
    String bgValue = bgSource.getString(row);
    String fgValue = fgSource.getString(row);
    
    if (BeeUtils.allEmpty(bgValue, fgValue)) {
      return null;
    }
    
    XStyle style = new XStyle();
    if (!BeeUtils.isEmpty(bgValue)) {
      style.setColor(bgValue);
    }

    if (!BeeUtils.isEmpty(fgValue)) {
      XFont font = new XFont();
      font.setColor(fgValue);
      style.setFontRef(sheet.registerFont(font));
    }
    
    return sheet.registerStyle(style);
  }

  @Override
  public StyleDescriptor getStyleDescriptor(IsRow row) {
    if (row == null) {
      return null;
    }

    String bgValue = bgSource.getString(row);
    String fgValue = fgSource.getString(row);

    SafeStyles styles;
    if (BeeUtils.isEmpty(bgValue)) {
      styles = BeeUtils.isEmpty(fgValue)
          ? null : StyleUtils.buildStyle(CssProperties.COLOR, fgValue);
    } else if (BeeUtils.isEmpty(fgValue)) {
      styles = StyleUtils.buildStyle(CssProperties.BACKGROUND_COLOR, bgValue);
    } else {
      styles = StyleUtils.buildStyle(CssProperties.BACKGROUND_COLOR, bgValue,
          CssProperties.COLOR, fgValue);
    }

    return (styles == null) ? null : StyleDescriptor.of(styles);
  }
}
