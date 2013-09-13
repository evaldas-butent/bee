package com.butent.bee.client.view.grid;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.HasActiveRow;
import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.dom.Rulers;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.dom.Stacking;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.Modifiers;
import com.butent.bee.client.event.logical.ActiveRowChangeEvent;
import com.butent.bee.client.event.logical.DataRequestEvent;
import com.butent.bee.client.event.logical.ScopeChangeEvent;
import com.butent.bee.client.event.logical.SelectionCountChangeEvent;
import com.butent.bee.client.event.logical.SortEvent;
import com.butent.bee.client.event.logical.VisibilityChangeEvent;
import com.butent.bee.client.grid.CellContext;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.cell.HeaderCell;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.grid.column.SelectionColumn;
import com.butent.bee.client.layout.FlexLayout;
import com.butent.bee.client.render.RenderableColumn;
import com.butent.bee.client.style.ConditionalStyle;
import com.butent.bee.client.style.Font;
import com.butent.bee.client.style.StyleDescriptor;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.HasEditStartHandlers;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.value.HasValueType;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.CssUnit;
import com.butent.bee.shared.ui.Flexibility;
import com.butent.bee.shared.ui.Flexible;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.ui.GridComponentDescription;
import com.butent.bee.shared.ui.NavigationOrigin;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages the structure and behavior of a cell grid user interface component.
 */

public class CellGrid extends Widget implements IdentifiableWidget, HasDataTable,
    HasEditStartHandlers, HasEnabled, HasActiveRow, RequiresResize, VisibilityChangeEvent.Handler,
    SettingsChangeEvent.HasSettingsChangeHandlers {

  public final class ColumnInfo implements HasValueType, Flexible {
    private final String columnId;
    private final String label;

    private final CellSource source;

    private final AbstractColumn<?> column;
    private final ColumnHeader header;
    private final ColumnFooter footer;

    private final AbstractFilterSupplier filterSupplier;

    private int initialWidth = BeeConst.UNDEF;
    private int minWidth = BeeConst.UNDEF;
    private int maxWidth = BeeConst.UNDEF;

    private int headerWidth = BeeConst.UNDEF;
    private int bodyWidth = BeeConst.UNDEF;
    private int footerWidth = BeeConst.UNDEF;

    private int resizedWidth = BeeConst.UNDEF;
    private int flexWidth = BeeConst.UNDEF;

    private int autoFitRows = BeeConst.UNDEF;
    private Flexibility flexibility;

    private StyleDescriptor headerStyle;
    private StyleDescriptor bodyStyle;
    private StyleDescriptor footerStyle;

    private ConditionalStyle dynStyles;

    private boolean colReadOnly;

    private boolean cellResizable = true;

    private boolean hidable = true;

    private ColumnInfo(String columnId, String label, CellSource source, AbstractColumn<?> column,
        ColumnHeader header, ColumnFooter footer, AbstractFilterSupplier filterSupplier) {

      this.columnId = columnId;
      this.label = label;
      this.source = source;

      this.column = column;
      this.header = header;
      this.footer = footer;

      this.filterSupplier = filterSupplier;
    }

    @Override
    public int clampSize(Orientation orientation, int size) {
      if (Orientation.HORIZONTAL.equals(orientation)) {
        return clampWidth(size);
      } else if (Orientation.VERTICAL.equals(orientation)) {
        return BeeUtils.clamp(size, getBodyComponent().getMinHeight(),
            getBodyComponent().getMaxHeight());
      } else {
        return size;
      }
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof ColumnInfo) && columnId.equals(((ColumnInfo) obj).columnId);
    }

    @Override
    public Flexibility getFlexibility() {
      return flexibility;
    }

    @Override
    public int getHypotheticalSize(Orientation orientation, boolean flexible) {
      if (Orientation.HORIZONTAL.equals(orientation)) {
        return flexible ? getWidth(getResizedWidth(), BeeConst.UNDEF) : getWidth();

      } else if (Orientation.VERTICAL.equals(orientation)) {
        return getBodyCellHeight();

      } else {
        return BeeConst.UNDEF;
      }
    }

    @Override
    public ValueType getValueType() {
      return (getSource() == null) ? getColumn().getValueType() : getSource().getValueType();
    }

    @Override
    public int hashCode() {
      return columnId.hashCode();
    }

    @Override
    public boolean isFlexible() {
      return getFlexibility() != null || BeeConst.isUndef(getInitialWidth());
    }

    @Override
    public void setFlexibility(Flexibility flexibility) {
      this.flexibility = flexibility;
    }

    @Override
    public boolean updateSize(Orientation orientation, int size) {
      if (Orientation.HORIZONTAL.equals(orientation)) {
        return updateFlexWidth(size);
      } else {
        return false;
      }
    }

    String getColumnId() {
      return columnId;
    }

    AbstractFilterSupplier getFilterSupplier() {
      return filterSupplier;
    }

    ColumnFooter getFooter() {
      return footer;
    }

    ColumnHeader getHeader() {
      return header;
    }

    String getLabel() {
      return label;
    }

    List<String> getSortBy() {
      return getColumn().getSortBy();
    }

    void initProperties(ColumnDescription columnDescription, GridDescription gridDescription,
        List<? extends IsColumn> dataColumns) {

      Assert.notNull(columnDescription);

      if (columnDescription.getColType().isReadOnly()
          || BeeUtils.isTrue(columnDescription.getReadOnly())) {
        setColReadOnly(true);
      }

      if (columnDescription.getWidth() != null) {
        setInitialWidth(columnDescription.getWidth());
      }
      if (columnDescription.getMinWidth() != null) {
        setMinWidth(columnDescription.getMinWidth());
      }
      if (columnDescription.getMaxWidth() != null) {
        setMaxWidth(columnDescription.getMaxWidth());
      }

      String af = BeeUtils.notEmpty(columnDescription.getAutoFit(), gridDescription.getAutoFit());
      if (BeeUtils.isInt(af)) {
        setAutoFitRows(BeeUtils.toInt(af));
      } else if (BeeConst.isTrue(af)) {
        setAutoFitRows(Integer.MAX_VALUE);
      }

      if (columnDescription.getFlexibility() != null) {
        setFlexibility(columnDescription.getFlexibility());
      }

      if (columnDescription.getHeaderStyle() != null) {
        setHeaderStyle(StyleDescriptor.copyOf(columnDescription.getHeaderStyle()));
      }
      if (columnDescription.getBodyStyle() != null) {
        setBodyStyle(StyleDescriptor.copyOf(columnDescription.getBodyStyle()));
      }
      if (columnDescription.getFooterStyle() != null) {
        setFooterStyle(StyleDescriptor.copyOf(columnDescription.getFooterStyle()));
      }

      if (columnDescription.getDynStyles() != null) {
        ConditionalStyle styles = ConditionalStyle.create(columnDescription.getDynStyles(),
            getColumnId(), dataColumns);
        if (styles != null) {
          setDynStyles(styles);
        }
      }

      Boolean cr = columnDescription.getCellResizable();
      if (cr == null) {
        setCellResizable(ValueType.TEXT.equals(getValueType()));
      } else {
        setCellResizable(cr);
      }
    }

    boolean is(String id) {
      return BeeUtils.same(getColumnId(), id);
    }

    boolean isColReadOnly() {
      return colReadOnly;
    }

    boolean isHidable() {
      return hidable;
    }

    private void buildSafeStyles(SafeStylesBuilder stylesBuilder, ComponentType componentType) {
      StyleDescriptor sd = getStyleDescriptor(componentType);
      if (sd == null) {
        return;
      }
      sd.buildSafeStyles(stylesBuilder);
    }

    private int clampWidth(int w) {
      return BeeUtils.clamp(w, getLowerWidthBound(), getUpperWidthBound());
    }

    private void ensureBodyWidth(int w) {
      if (w > 0) {
        setBodyWidth(Math.max(getBodyWidth(), w));
      }
    }

    private void ensureHeaderWidth(int w) {
      if (w > 0) {
        setHeaderWidth(Math.max(getHeaderWidth(), w));
      }
    }

    private int getAutoFitRows() {
      return autoFitRows;
    }

    private Font getBodyFont() {
      if (getBodyStyle() == null) {
        return null;
      }
      return getBodyStyle().getFont();
    }

    private StyleDescriptor getBodyStyle() {
      return bodyStyle;
    }

    private int getBodyWidth() {
      return bodyWidth;
    }

    private String getClassName(ComponentType componentType) {
      StyleDescriptor sd = getStyleDescriptor(componentType);
      if (sd == null) {
        return null;
      }
      return sd.getClassName();
    }

    private AbstractColumn<?> getColumn() {
      return column;
    }

    private ConditionalStyle getDynStyles() {
      return dynStyles;
    }

    private int getFlexWidth() {
      return flexWidth;
    }

    private StyleDescriptor getFooterStyle() {
      return footerStyle;
    }

    private int getFooterWidth() {
      return footerWidth;
    }

    private Font getHeaderFont() {
      if (getHeaderStyle() == null) {
        return null;
      }
      return getHeaderStyle().getFont();
    }

    private StyleDescriptor getHeaderStyle() {
      return headerStyle;
    }

    private int getHeaderWidth() {
      return headerWidth;
    }

    private int getInitialWidth() {
      return initialWidth;
    }

    private int getLowerWidthBound() {
      if (getMinWidth() > 0) {
        return getMinWidth();
      } else if (getInitialWidth() <= 0) {
        return BeeUtils.positive(getMinCellWidth(), DEFAULT_MIN_CELL_WIDTH);
      } else if (getMinCellWidth() > getInitialWidth()) {
        return Math.min(DEFAULT_MIN_CELL_WIDTH, getInitialWidth());
      } else if (getMinCellWidth() > 0) {
        return getMinCellWidth();
      } else {
        return Math.min(DEFAULT_MIN_CELL_WIDTH, getInitialWidth());
      }
    }

    private int getMaxWidth() {
      return maxWidth;
    }

    private int getMinWidth() {
      return minWidth;
    }

    private int getResizedWidth() {
      return resizedWidth;
    }

    private CellSource getSource() {
      return source;
    }

    private StyleDescriptor getStyleDescriptor(ComponentType componentType) {
      switch (componentType) {
        case HEADER:
          return getHeaderStyle();
        case BODY:
          return getBodyStyle();
        case FOOTER:
          return getFooterStyle();
      }
      return null;
    }

    private int getUpperWidthBound() {
      if (getMaxWidth() > 0) {
        return getMaxWidth();
      } else if (getInitialWidth() <= 0) {
        return BeeUtils.positive(getMaxCellWidth(), DEFAULT_MAX_CELL_WIDTH);
      } else if (getMaxCellWidth() <= 0) {
        return Math.max(DEFAULT_MAX_CELL_WIDTH, getInitialWidth());
      } else if (getMaxCellWidth() >= getInitialWidth()) {
        return getMaxCellWidth();
      } else {
        return Math.max(DEFAULT_MAX_CELL_WIDTH, getInitialWidth());
      }
    }

    private int getWidth() {
      return getWidth(getResizedWidth(), getFlexWidth());
    }

    private int getWidth(int resized, int flex) {
      int w = BeeUtils.positive(resized, flex, getInitialWidth());
      if (w <= 0) {
        w = BeeUtils.positive(Math.max(getBodyWidth(), getHeaderWidth()), getFooterWidth());
      }
      return clampWidth(w);
    }

    private boolean isActionColumn() {
      return ColType.ACTION.equals(getColumn().getColType());
    }

    private boolean isCalculated() {
      return ColType.CALCULATED.equals(getColumn().getColType());
    }

    private boolean isCellResizable() {
      return cellResizable;
    }

    private boolean isRenderable() {
      return getColumn() instanceof RenderableColumn;
    }

    private boolean isSelection() {
      return ColType.SELECTION.equals(getColumn().getColType());
    }

    private void setAutoFitRows(int autoFitRows) {
      this.autoFitRows = autoFitRows;
    }

    private void setBodyFont(String fontDeclaration) {
      if (getBodyStyle() == null) {
        if (!BeeUtils.isEmpty(fontDeclaration)) {
          setBodyStyle(new StyleDescriptor(null, null, fontDeclaration));
        }
      } else {
        getBodyStyle().setFontDeclaration(fontDeclaration);
      }
    }

    private void setBodyStyle(StyleDescriptor bodyStyle) {
      this.bodyStyle = bodyStyle;
    }

    private void setBodyWidth(int bodyWidth) {
      this.bodyWidth = bodyWidth;
    }

    private void setCellResizable(boolean cellResizable) {
      this.cellResizable = cellResizable;
    }

    private void setColReadOnly(boolean colReadOnly) {
      this.colReadOnly = colReadOnly;
    }

    private void setDynStyles(ConditionalStyle dynStyles) {
      this.dynStyles = dynStyles;
    }

    private void setFlexWidth(int flexWidth) {
      this.flexWidth = flexWidth;
    }

    private void setFooterFont(String fontDeclaration) {
      if (getFooterStyle() == null) {
        if (!BeeUtils.isEmpty(fontDeclaration)) {
          setFooterStyle(new StyleDescriptor(null, null, fontDeclaration));
        }
      } else {
        getFooterStyle().setFontDeclaration(fontDeclaration);
      }
    }

    private void setFooterStyle(StyleDescriptor footerStyle) {
      this.footerStyle = footerStyle;
    }

    private void setFooterWidth(int footerWidth) {
      this.footerWidth = footerWidth;
    }

    private void setHeaderFont(String fontDeclaration) {
      if (getHeaderStyle() == null) {
        if (!BeeUtils.isEmpty(fontDeclaration)) {
          setHeaderStyle(new StyleDescriptor(null, null, fontDeclaration));
        }
      } else {
        getHeaderStyle().setFontDeclaration(fontDeclaration);
      }
    }

    private void setHeaderStyle(StyleDescriptor headerStyle) {
      this.headerStyle = headerStyle;
    }

    private void setHeaderWidth(int headerWidth) {
      this.headerWidth = headerWidth;
    }

    private void setHidable(boolean hidable) {
      this.hidable = hidable;
    }

    private void setInitialWidth(int initialWidth) {
      this.initialWidth = initialWidth;
    }

    private void setMaxWidth(int maxWidth) {
      this.maxWidth = maxWidth;
    }

    private void setMinWidth(int minWidth) {
      this.minWidth = minWidth;
    }

    private void setResizedWidth(int resizedWidth) {
      this.resizedWidth = resizedWidth;
    }

    private boolean updateFlexWidth(int newWidth) {
      if (newWidth != getFlexWidth() || getResizedWidth() >= 0) {
        setFlexWidth(newWidth);
        setResizedWidth(BeeConst.UNDEF);
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Contains templates which facilitates compile-time binding of HTML templates to generate
   * SafeHtml strings.
   */

  public interface Template extends SafeHtmlTemplates {
    @Template("<div data-row=\"{0}\" data-col=\"{1}\" class=\"{2}\" style=\"{3}\">{4}</div>")
    SafeHtml cell(String rowIdx, int colIdx, String classes, SafeStyles styles, SafeHtml contents);

    // CHECKSTYLE:OFF
    @Template("<div data-row=\"{0}\" data-col=\"{1}\" class=\"{2}\" style=\"{3}\" tabindex=\"{4}\">{5}</div>")
    SafeHtml cellFocusable(String rowIdx, int colIdx, String classes, SafeStyles styles,
        int tabIndex, SafeHtml contents);

    // CHECKSTYLE:ON

    @Template("<div class=\"{0}\">{1}</div>")
    SafeHtml emptiness(String classes, String text);

    @Template("<div id=\"{0}\" style=\"position:absolute; top:-64px; left:-64px;\">{1}{2}</div>")
    SafeHtml resizer(String id, SafeHtml handle, SafeHtml bar);

    @Template("<div id=\"{0}\" style=\"position:absolute;\"></div>")
    SafeHtml resizerBar(String id);

    @Template("<div id=\"{0}\" style=\"position:absolute;\"></div>")
    SafeHtml resizerHandle(String id);
  }

  private final class CellInfo {
    private int width;
    private int height;

    private Edges nextRowPadding;
    private Edges nextRowBorders;

    private Edges nextColumnPadding;
    private Edges nextColumnBorders;

    private CellInfo(int width, int height) {
      this.width = width;
      this.height = height;
    }

    private int getHeight() {
      return height;
    }

    private Edges getNextColumnBorders() {
      return nextColumnBorders;
    }

    private Edges getNextColumnPadding() {
      return nextColumnPadding;
    }

    private Edges getNextRowBorders() {
      return nextRowBorders;
    }

    private Edges getNextRowPadding() {
      return nextRowPadding;
    }

    private int getWidth() {
      return width;
    }

    private void setHeight(int height) {
      this.height = height;
    }

    private void setNextColumnBorders(Edges nextColumnBorders) {
      this.nextColumnBorders = nextColumnBorders;
    }

    private void setNextColumnPadding(Edges nextColumnPadding) {
      this.nextColumnPadding = nextColumnPadding;
    }

    private void setNextRowBorders(Edges nextRowBorders) {
      this.nextRowBorders = nextRowBorders;
    }

    private void setNextRowPadding(Edges nextRowPadding) {
      this.nextRowPadding = nextRowPadding;
    }

    private void setSize(int w, int h) {
      setWidth(w);
      setHeight(h);
    }

    private void setWidth(int width) {
      this.width = width;
    }
  }

  private final class Component {
    private final ComponentType type;

    private StyleDescriptor style;

    private int cellHeight = BeeConst.UNDEF;
    private int minHeight = BeeConst.UNDEF;
    private int maxHeight = BeeConst.UNDEF;

    private Edges padding;
    private Edges borderWidth;
    private Edges margin;

    private Component(ComponentType type, int cellHeight, int minHeight, int maxHeight,
        Edges padding, Edges borderWidth, Edges margin) {
      this.type = type;
      this.cellHeight = cellHeight;
      this.minHeight = minHeight;
      this.maxHeight = maxHeight;
      this.padding = padding;
      this.borderWidth = borderWidth;
      this.margin = margin;
    }

    private void buildSafeStyles(SafeStylesBuilder stylesBuilder) {
      if (getSafeStyles() != null) {
        stylesBuilder.append(getSafeStyles());
      }
      if (getFont() != null) {
        stylesBuilder.append(getFont().buildCss());
      }
    }

    private Edges getBorderWidth() {
      return borderWidth;
    }

    private int getCellHeight() {
      return cellHeight;
    }

    private String getClassName() {
      if (getStyle() == null) {
        return null;
      }
      return getStyle().getClassName();
    }

    private Font getFont() {
      if (getStyle() == null) {
        return null;
      }
      return getStyle().getFont();
    }

    private Edges getMargin() {
      return margin;
    }

    private int getMaxHeight() {
      return maxHeight;
    }

    private int getMinHeight() {
      return minHeight;
    }

    private Edges getPadding() {
      return padding;
    }

    private SafeStyles getSafeStyles() {
      if (getStyle() == null) {
        return null;
      }
      return getStyle().getSafeStyles();
    }

    private StyleDescriptor getStyle() {
      return style;
    }

    private void setBorderWidth(Edges borderWidth) {
      this.borderWidth = borderWidth;
    }

    private void setCellHeight(int cellHeight) {
      this.cellHeight = cellHeight;
    }

    private void setFont(String fontDeclaration) {
      if (getStyle() == null) {
        if (!BeeUtils.isEmpty(fontDeclaration)) {
          setStyle(new StyleDescriptor(null, null, fontDeclaration));
        }
      } else {
        getStyle().setFontDeclaration(fontDeclaration);
      }
    }

    private void setMargin(Edges margin) {
      this.margin = margin;
    }

    private void setMaxHeight(int maxHeight) {
      this.maxHeight = maxHeight;
    }

    private void setMinHeight(int minHeight) {
      this.minHeight = minHeight;
    }

    private void setPadding(Edges padding) {
      this.padding = padding;
    }

    private void setStyle(StyleDescriptor style) {
      this.style = style;
    }
  }

  private final class RenderInfo {
    private final String rowIdx;
    private final int colIdx;
    private final String classes;
    private final SafeStyles styles;
    private final boolean focusable;
    private final SafeHtml content;

    private RenderInfo(String rowIdx, int colIdx, String classes, SafeStyles styles,
        boolean focusable, SafeHtml content) {
      this.rowIdx = rowIdx;
      this.colIdx = colIdx;
      this.classes = classes;
      this.styles = styles;
      this.focusable = focusable;
      this.content = content;
    }

    private String getClasses() {
      return classes;
    }

    private int getColIdx() {
      return colIdx;
    }

    private SafeHtml getContent() {
      return content;
    }

    private String getRowIdx() {
      return rowIdx;
    }

    private SafeStyles getStyles() {
      return styles;
    }

    private boolean isFocusable() {
      return focusable;
    }

    private SafeHtml render() {
      SafeHtml result;
      if (isFocusable()) {
        result = template.cellFocusable(rowIdx, colIdx, classes, styles, getTabIndex(), content);
      } else {
        result = template.cell(rowIdx, colIdx, classes, styles, content);
      }
      return result;
    }
  }

  private enum RenderMode {
    FULL, CONTENT
  }

  private enum ResizerMode {
    HORIZONTAL(10, 4), VERTICAL(10, 4);

    private final int handlePx;
    private final int barPx;

    private ResizerMode(int handlePx, int barPx) {
      this.handlePx = handlePx;
      this.barPx = barPx;
    }

    private int getBarPx() {
      return barPx;
    }

    private int getHandlePx() {
      return handlePx;
    }
  }

  private class ResizerMoveTimer extends Timer {
    private boolean pending;
    private int pendingMove;

    @Override
    public void cancel() {
      if (pending) {
        pending = false;
        super.cancel();
      }
    }

    @Override
    public void run() {
      pending = false;
      doResize();
    }

    @Override
    public void schedule(int delayMillis) {
      pending = true;
      super.schedule(delayMillis);
    }

    private void doResize() {
      if (pendingMove == 0) {
        return;
      }
      switch (getResizerStatus()) {
        case HORIZONTAL:
          resizeHorizontal(pendingMove);
          break;
        case VERTICAL:
          resizeVertical(pendingMove);
          break;
      }
      pendingMove = 0;
    }

    private void handleMove(int by, int millis) {
      if (by == 0) {
        return;
      }
      pendingMove += by;
      if (!pending && pendingMove != 0) {
        schedule(millis);
      }
    }

    private void reset() {
      cancel();
      pendingMove = 0;
    }

    private void stop() {
      cancel();
      doResize();
    }
  }

  private final class ResizerShowTimer extends Timer {
    private boolean pending;

    private Element element;
    private String rowIdx;
    private int colIdx = BeeConst.UNDEF;

    private ResizerMode resizerMode;
    private Rectangle rectangle;

    private ResizerShowTimer() {
      super();
    }

    @Override
    public void cancel() {
      if (pending) {
        pending = false;
        super.cancel();
      }
    }

    @Override
    public void run() {
      pending = false;

      switch (resizerMode) {
        case HORIZONTAL:
          if (!showColumnResizer(element, colIdx)) {
            pending = true;
          }
          break;
        case VERTICAL:
          if (!showRowResizer(element, rowIdx)) {
            pending = true;
          }
          break;
      }
    }

    @Override
    public void schedule(int delayMillis) {
      pending = true;
      super.schedule(delayMillis);
    }

    private void handleEvent(Event event) {
      if (!pending) {
        return;
      }
      if (!EventUtils.isMouseMove(event.getType())) {
        cancel();
        return;
      }

      if (!rectangle.contains(event.getClientX(), event.getClientY())) {
        cancel();
      }
    }

    private void start(Element el, String row, int col, ResizerMode resizer, Rectangle rect,
        int millis) {
      this.element = el;
      this.rowIdx = row;
      this.colIdx = col;
      this.resizerMode = resizer;
      this.rectangle = rect;

      schedule(millis);
    }
  }

  private final class RowChangeScheduler extends Timer {

    private int sensitivityMillis;

    private long lastTime;

    private RowChangeScheduler(int sensitivityMillis) {
      super();
      this.sensitivityMillis = sensitivityMillis;
    }

    @Override
    public void run() {
      fireChangeEvent();
    }

    private void fireChangeEvent() {
      CellGrid.this.fireEvent(new ActiveRowChangeEvent(CellGrid.this.getActiveRow()));
    }

    private long getLastTime() {
      return lastTime;
    }

    private int getSensitivityMillis() {
      return sensitivityMillis;
    }

    private void scheduleEvent() {
      if (getSensitivityMillis() <= 0) {
        fireChangeEvent();
      } else {
        long now = System.currentTimeMillis();
        long last = getLastTime();
        setLastTime(now);

        long elapsed = now - last;
        int delayMillis = getSensitivityMillis();

        if (elapsed < delayMillis) {
          if (elapsed > 0 && elapsed < delayMillis / 2) {
            delayMillis -= elapsed;
          }
          schedule(delayMillis);
        } else {
          fireChangeEvent();
        }
      }
    }

    private void setLastTime(long lastTime) {
      this.lastTime = lastTime;
    }

    private void setSensitivityMillis(int sensitivityMillis) {
      this.sensitivityMillis = sensitivityMillis;
    }
  }

  private enum TargetType {
    CONTAINER, RESIZER, HEADER, BODY, FOOTER;
  }

  private static final BeeLogger logger = LogUtils.getLogger(CellGrid.class);

  private static Edges defaultBodyCellPadding = new Edges(2, 3);
  private static Edges defaultBodyBorderWidth = new Edges(1);
  private static Edges defaultBodyCellMargin;

  private static Edges defaultFooterCellPadding = new Edges(2, 3, 0);
  private static Edges defaultFooterBorderWidth = new Edges(1);
  private static Edges defaultFooterCellMargin;

  private static Edges defaultHeaderCellPadding;
  private static Edges defaultHeaderBorderWidth = new Edges(1);
  private static Edges defaultHeaderCellMargin;

  private static final int DEFAULT_MIN_CELL_WIDTH = 16;
  private static final int DEFAULT_MAX_CELL_WIDTH = 999;

  private static int defaultMinCellHeight = 8;
  private static int defaultMaxCellHeight = 256;

  private static int defaultResizerShowSensitivityMillis = 100;
  private static int defaultResizerMoveSensitivityMillis;
  private static int defaultRowChangeSensitivityMillis;

  private static int pageSizeCalculationReserve = 3;

  public static final String STYLE_EVEN_ROW = "bee-CellGridEvenRow";
  public static final String STYLE_ODD_ROW = "bee-CellGridOddRow";

  private static final String STYLE_GRID = "bee-CellGrid";

  private static final String STYLE_EMPTY = "bee-CellGridEmpty";

  private static final String STYLE_CELL = "bee-CellGridCell";

  private static final String STYLE_HEADER = "bee-CellGridHeader";
  private static final String STYLE_BODY = "bee-CellGridBody";
  private static final String STYLE_FOOTER = "bee-CellGridFooter";

  private static final String STYLE_SELECTED_ROW = "bee-CellGridSelectedRow";

  private static final String STYLE_COLUMN_PREFIX = "bee-CellGridColumn-";

  private static final String STYLE_ACTIVE_ROW = "bee-CellGridActiveRow";
  private static final String STYLE_ACTIVE_CELL = "bee-CellGridActiveCell";
  private static final String STYLE_RESIZED_CELL = "bee-CellGridResizedCell";
  private static final String STYLE_RESIZER = "bee-CellGridResizer";
  private static final String STYLE_RESIZER_HANDLE = "bee-CellGridResizerHandle";
  private static final String STYLE_RESIZER_BAR = "bee-CellGridResizerBar";

  private static final String STYLE_RESIZER_HORIZONTAL = "bee-CellGridResizerHorizontal";
  private static final String STYLE_RESIZER_HANDLE_HORIZONTAL =
      "bee-CellGridResizerHandleHorizontal";
  private static final String STYLE_RESIZER_BAR_HORIZONTAL = "bee-CellGridResizerBarHorizontal";

  private static final String STYLE_RESIZER_VERTICAL = "bee-CellGridResizerVertical";
  private static final String STYLE_RESIZER_HANDLE_VERTICAL = "bee-CellGridResizerHandleVertical";
  private static final String STYLE_RESIZER_BAR_VERTICAL = "bee-CellGridResizerBarVertical";

  private static final String HEADER_ROW = "header";
  private static final String FOOTER_ROW = "footer";
  private static final Template template = GWT.create(Template.class);

  public static int getDefaultBodyCellHeight() {
    return BeeUtils.resize(BeeKeeper.getScreen().getHeight(), 300, 1500, 14, 25);
  }

  public static int getDefaultFooterCellHeight() {
    return BeeUtils.resize(BeeKeeper.getScreen().getHeight(), 300, 1500, 16, 21);
  }

  public static int getDefaultHeaderCellHeight() {
    return BeeUtils.resize(BeeKeeper.getScreen().getHeight(), 300, 800, 16, 28);
  }

  private static boolean cellConsumesEventType(Cell<?> cell, String eventType) {
    Set<String> consumedEvents = cell.getConsumedEvents();
    return consumedEvents != null && consumedEvents.contains(eventType);
  }

  private static String getBodyCellSelector(int row, int col) {
    return Selectors.conjunction(getBodyRowSelector(row), getColumnSelector(col));
  }

  private static String getBodyRowSelector(int rowIndex) {
    return Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, rowIndex);
  }

  private static String getCellSelector(String rowIdx, int col) {
    return Selectors.conjunction(getRowSelector(rowIdx), getColumnSelector(col));
  }

  private static String getColumnSelector(int col) {
    return Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_COLUMN, col);
  }

  private static String getCssValue(Edges edges) {
    if (edges == null) {
      return Edges.EMPTY_CSS_VALUE;
    } else {
      return edges.getCssValue();
    }
  }

  private static String getFooterCellSelector(int col) {
    return Selectors.conjunction(getFooterRowSelector(), getColumnSelector(col));
  }

  private static String getFooterRowSelector() {
    return Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, FOOTER_ROW);
  }

  private static String getHeaderCellSelector(int col) {
    return Selectors.conjunction(getHeaderRowSelector(), getColumnSelector(col));
  }

  private static String getHeaderRowSelector() {
    return Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, HEADER_ROW);
  }

  private static int getHeightIncrement(Edges edges) {
    int incr = 0;
    if (edges != null) {
      incr += BeeUtils.toNonNegativeInt(edges.getTopValue());
      incr += BeeUtils.toNonNegativeInt(edges.getBottomValue());
    }
    return incr;
  }

  private static int getHeightIncrement(Edges padding, Edges border, Edges margin) {
    return getHeightIncrement(padding) + getHeightIncrement(border) + getHeightIncrement(margin);
  }

  private static String getRowSelector(String rowIdx) {
    return Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, rowIdx);
  }

  private static int getWidthIncrement(Edges edges) {
    int incr = 0;
    if (edges != null) {
      incr += BeeUtils.toNonNegativeInt(edges.getLeftValue());
      incr += BeeUtils.toNonNegativeInt(edges.getRightValue());
    }
    return incr;
  }

  private static int getWidthIncrement(Edges padding, Edges border, Edges margin) {
    return getWidthIncrement(padding) + getWidthIncrement(border) + getWidthIncrement(margin);
  }

  private static Edges incrementEdges(Edges defaultEdges, int dw, int dh) {
    Edges edges = (defaultEdges == null) ? new Edges(0) : Edges.copyOf(defaultEdges);

    if (dw != 0) {
      int dr = dw / 2;
      int dl = dw - dr;

      if (dl != 0) {
        edges.setLeft(edges.getIntLeft() + dl);
      }
      if (dr != 0) {
        edges.setLeft(edges.getIntRight() + dr);
      }
    }

    if (dh != 0) {
      int db = dh / 2;
      int dt = dh - db;

      if (dt != 0) {
        edges.setTop(edges.getIntTop() + dt);
      }
      if (db != 0) {
        edges.setBottom(edges.getIntBottom() + db);
      }
    }

    return edges;
  }

  private static void incrementHeight(NodeList<Element> nodes, int dh) {
    if (nodes != null && nodes.getLength() > 0 && dh != 0) {
      int height = StyleUtils.getHeight(nodes.getItem(0));
      if (height + dh >= 0) {
        StyleUtils.setStylePropertyPx(nodes, StyleUtils.STYLE_HEIGHT, height + dh);
      }
    }
  }

  private static void incrementTop(NodeList<Element> nodes, int dt) {
    if (nodes != null && nodes.getLength() > 0 && dt != 0) {
      int top = StyleUtils.getTop(nodes.getItem(0));
      if (top + dt >= 0) {
        StyleUtils.setStylePropertyPx(nodes, StyleUtils.STYLE_TOP, top + dt);
      }
    }
  }

  private static boolean isBodyRow(String rowIdx) {
    return BeeUtils.isDigit(rowIdx);
  }

  private static boolean isFooterRow(String rowIdx) {
    return BeeUtils.same(rowIdx, FOOTER_ROW);
  }

  private static boolean isHeaderRow(String rowIdx) {
    return BeeUtils.same(rowIdx, HEADER_ROW);
  }

  private static int limitCellHeight(int height, Component component) {
    if (component == null) {
      return height;
    }
    int h = height;
    if (component.getMinHeight() > 0) {
      h = Math.max(h, component.getMinHeight());
    }
    if (component.getMaxHeight() > 0 && component.getMaxHeight() > component.getMinHeight()) {
      h = Math.min(h, component.getMaxHeight());
    }
    return h;
  }

  private static void updateComponent(GridComponentDescription src, Component dst) {
    if (src == null || dst == null) {
      return;
    }

    if (src.getStyle() != null) {
      dst.setStyle(StyleDescriptor.copyOf(src.getStyle()));
    }

    if (src.getHeight() != null) {
      dst.setCellHeight(src.getHeight());
    }
    if (src.getMinHeight() != null) {
      dst.setMinHeight(src.getMinHeight());
    }
    if (src.getMaxHeight() != null) {
      dst.setMaxHeight(src.getMaxHeight());
    }

    if (!BeeUtils.isEmpty(src.getPadding())) {
      dst.setPadding(Edges.parse(src.getPadding()));
    }
    if (!BeeUtils.isEmpty(src.getBorderWidth())) {
      dst.setBorderWidth(Edges.parse(src.getBorderWidth()));
    }
    if (!BeeUtils.isEmpty(src.getMargin())) {
      dst.setMargin(Edges.parse(src.getMargin()));
    }
  }

  private final List<ColumnInfo> predefinedColumns = Lists.newArrayList();
  private final List<Integer> visibleColumns = Lists.newArrayList();
  private final Component headerComponent = new Component(ComponentType.HEADER,
      getDefaultHeaderCellHeight(), defaultMinCellHeight, defaultMaxCellHeight,
      defaultHeaderCellPadding, defaultHeaderBorderWidth, defaultHeaderCellMargin);
  private final Component bodyComponent = new Component(ComponentType.BODY,
      getDefaultBodyCellHeight(), defaultMinCellHeight, defaultMaxCellHeight,
      defaultBodyCellPadding, defaultBodyBorderWidth, defaultBodyCellMargin);
  private final Component footerComponent = new Component(ComponentType.FOOTER,
      getDefaultFooterCellHeight(), defaultMinCellHeight, defaultMaxCellHeight,
      defaultFooterCellPadding, defaultFooterBorderWidth, defaultFooterCellMargin);

  private int minCellWidth = DEFAULT_MIN_CELL_WIDTH;
  private int maxCellWidth = DEFAULT_MAX_CELL_WIDTH;
  private Flexibility defaultFlexibility;

  private int activeRowIndex = BeeConst.UNDEF;
  private int activeColumnIndex = BeeConst.UNDEF;

  private int pageSize = BeeConst.UNDEF;
  private int pageStart;

  private int rowCount = BeeConst.UNDEF;
  private final List<IsRow> rowData = Lists.newArrayList();

  private ConditionalStyle rowStyles;
  private final LinkedHashMap<Long, RowInfo> selectedRows = Maps.newLinkedHashMap();
  private final Order sortOrder = new Order();

  private int tabIndex;
  private int zIndex;

  private final String resizerId = DomUtils.createUniqueId("resizer");
  private final String resizerHandleId = DomUtils.createUniqueId("resizer-handle");

  private final String resizerBarId = DomUtils.createUniqueId("resizer-bar");

  private ResizerMode resizerStatus;

  private boolean isResizing;

  private String resizerRow;

  private int resizerCol = BeeConst.UNDEF;

  private Modifiers resizerModifiers;

  private int resizerStartValue = BeeConst.UNDEF;

  private int resizerPosition = BeeConst.UNDEF;

  private int resizerPositionMin = BeeConst.UNDEF;

  private int resizerPositionMax = BeeConst.UNDEF;

  private int resizerShowSensitivityMillis = defaultResizerShowSensitivityMillis;

  private int resizerMoveSensitivityMillis = defaultResizerMoveSensitivityMillis;

  private final ResizerShowTimer resizerShowTimer = new ResizerShowTimer();

  private final ResizerMoveTimer resizerMoveTimer = new ResizerMoveTimer();

  private final Map<Long, Integer> resizedRows = Maps.newHashMap();

  private final Table<Long, String, CellInfo> resizedCells = HashBasedTable.create();

  private boolean readOnly;

  private boolean editing;

  private boolean enabled = true;

  private boolean areColumnWidthsEstimated;

  private boolean wasLayoutDone;

  private final List<Long> renderedRows = Lists.newArrayList();

  private RenderMode renderMode;

  private final RowChangeScheduler rowChangeScheduler =
      new RowChangeScheduler(defaultRowChangeSensitivityMillis);

  private Evaluator rowEditable;

  public CellGrid() {
    setElement(Document.get().createDivElement());

    sinkEvents(Event.ONKEYDOWN | Event.ONKEYPRESS | Event.ONCLICK | Event.ONMOUSEDOWN
        | Event.ONMOUSEMOVE | Event.ONMOUSEUP | Event.ONMOUSEOUT | Event.ONMOUSEWHEEL);

    setStyleName(STYLE_GRID);
    String id = DomUtils.createId(this, getIdPrefix());

    VisibilityChangeEvent.register(id, this);
  }

  @Override
  public HandlerRegistration addActiveRowChangeHandler(ActiveRowChangeEvent.Handler handler) {
    return addHandler(handler, ActiveRowChangeEvent.getType());
  }

  public ColumnInfo addColumn(String columnId, CellSource source, AbstractColumn<?> column,
      ColumnHeader header) {
    return addColumn(columnId, columnId, source, column, header, null, null, true);
  }

  public ColumnInfo addColumn(String columnId, String label, CellSource source,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      AbstractFilterSupplier filterSupplier, Boolean visible) {

    Assert.notEmpty(columnId);
    Assert.notNull(column);

    ColumnInfo columnInfo = new ColumnInfo(columnId, label, source, column, header, footer,
        filterSupplier);
    if (BeeUtils.isTrue(visible)) {
      columnInfo.setHidable(false);
    }
    predefinedColumns.add(columnInfo);

    if (!BeeUtils.isFalse(visible)) {
      visibleColumns.add(predefinedColumns.size() - 1);
    }

    Set<String> consumedEvents = Sets.newHashSet();
    Set<String> cellEvents = column.getCell().getConsumedEvents();
    if (cellEvents != null) {
      consumedEvents.addAll(cellEvents);
    }

    if (header != null) {
      Set<String> headerEvents = header.getCell().getConsumedEvents();
      if (headerEvents != null) {
        consumedEvents.addAll(headerEvents);
      }
    }

    if (footer != null) {
      Set<String> footerEvents = footer.getCell().getConsumedEvents();
      if (footerEvents != null) {
        consumedEvents.addAll(footerEvents);
      }
    }

    if (!consumedEvents.isEmpty()) {
      EventUtils.sinkEvents(this, consumedEvents);
    }

    return columnInfo;
  }

  @Override
  public HandlerRegistration addDataRequestHandler(DataRequestEvent.Handler handler) {
    return addHandler(handler, DataRequestEvent.getType());
  }

  @Override
  public HandlerRegistration addEditStartHandler(EditStartEvent.Handler handler) {
    return addHandler(handler, EditStartEvent.getType());
  }

  @Override
  public HandlerRegistration addScopeChangeHandler(ScopeChangeEvent.Handler handler) {
    return addHandler(handler, ScopeChangeEvent.getType());
  }

  @Override
  public HandlerRegistration addSelectionCountChangeHandler(
      SelectionCountChangeEvent.Handler handler) {
    return addHandler(handler, SelectionCountChangeEvent.getType());
  }

  @Override
  public HandlerRegistration addSettingsChangeHandler(SettingsChangeEvent.Handler handler) {
    return addHandler(handler, SettingsChangeEvent.getType());
  }

  @Override
  public HandlerRegistration addSortHandler(SortEvent.Handler handler) {
    return addHandler(handler, SortEvent.getType());
  }

  public void autoFit(boolean redraw) {
    for (int i = 0; i < getColumnCount(); i++) {
      int width = estimateColumnWidth(i, true);
      if (width <= 0) {
        continue;
      }

      ColumnInfo columnInfo = getColumnInfo(i);
      columnInfo.setResizedWidth(columnInfo.clampWidth(width));
    }

    boolean pageSizeChanged = updatePageSize();
    if (redraw && !pageSizeChanged) {
      render(true);
    }
  }

  public void autoFitColumn(int col) {
    int oldWidth = getColumnWidth(col);
    int newWidth = estimateColumnWidth(col, true);
    if (newWidth <= 0) {
      return;
    }
    resizeColumnWidth(col, oldWidth, newWidth - oldWidth);
  }

  public void autoFitColumn(String columnId) {
    int col = getColumnIndex(columnId);
    if (isColumnWithinBounds(col)) {
      autoFitColumn(col);
    }
  }

  public void deactivate() {
    activateCell(BeeConst.UNDEF, BeeConst.UNDEF);
  }

  public boolean doFlexLayout() {
    int width = getElement().getClientWidth();
    if (width > 0) {
      return doFlexLayout(width);
    } else {
      wasLayoutDone = false;
      return false;
    }
  }

  public boolean doFlexLayout(int containerWidth) {
    wasLayoutDone = true;

    List<ColumnInfo> columns = getColumns();
    boolean hasFlexibility = getDefaultFlexibility() != null;

    if (!hasFlexibility) {
      for (ColumnInfo columnInfo : columns) {
        if (columnInfo.getFlexibility() != null) {
          hasFlexibility = true;
          break;
        }
      }

      if (!hasFlexibility) {
        return false;
      }
    }

    int distrWidth = containerWidth;

    int cellWidthIncrement = getBodyCellWidthIncrement();
    if (cellWidthIncrement > 0) {
      distrWidth -= getColumnCount() * cellWidthIncrement;
    }

    boolean changed = FlexLayout.doLayout(distrWidth, getBodyComponent().getFont(),
        Orientation.HORIZONTAL, columns, getDefaultFlexibility());

    if (changed) {
      getRenderedRows().clear();
    }
    return changed;
  }

  public void estimateColumnWidths() {
    estimateColumnWidths(getRowData(), 0, getDataSize());
  }

  public <T extends IsRow> void estimateColumnWidths(List<T> rows, int start, int end) {
    if (!rows.isEmpty()) {
      for (int i = 0; i < getColumnCount(); i++) {
        estimateColumnWidth(i, rows, start, end, true);
      }
      areColumnWidthsEstimated = true;
    }
  }

  public int estimateHeaderWidth(int col, boolean addMargins) {
    ColumnInfo columnInfo = getColumnInfo(col);
    ColumnHeader header = columnInfo.getHeader();
    if (header == null) {
      return 0;
    }

    SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
    CellContext context = new CellContext(0, col, null, this);
    header.render(context, cellBuilder);
    SafeHtml cellHtml = cellBuilder.toSafeHtml();

    Font font = Font.merge(getHeaderComponent().getFont(), columnInfo.getHeaderFont());
    int width = Rulers.getLineWidth(font, cellHtml.asString(), true);

    if (width > 0) {
      if (addMargins) {
        if (columnInfo.getColumn().isSortable()) {
          width += HeaderCell.SORT_INFO_WIDTH;
        }
      }
      columnInfo.ensureHeaderWidth(width);
    }
    return width;
  }

  public void estimateHeaderWidths(boolean addMargins) {
    for (int i = 0; i < getColumnCount(); i++) {
      estimateHeaderWidth(i, addMargins);
    }
  }

  public int estimatePageSize() {
    return estimatePageSize(getElement().getClientWidth(), getElement().getClientHeight(), false);
  }

  public int estimatePageSize(int containerWidth, int containerHeight, boolean checkWidth) {
    int availableBodyHeight = containerHeight - getHeaderHeight() - getFooterHeight();

    if (checkWidth) {
      int width = getBodyWidth();
      if (width <= 0 || width > containerWidth) {
        availableBodyHeight -= DomUtils.getScrollBarHeight();
      }
    }

    int bodyRowHeight = getBodyCellHeight() + getBodyCellHeightIncrement();
    if (bodyRowHeight > 0 && availableBodyHeight >= bodyRowHeight) {
      int ps = availableBodyHeight / bodyRowHeight;
      if (ps > 1 && availableBodyHeight % bodyRowHeight < pageSizeCalculationReserve) {
        ps--;
      }
      return ps;
    }
    return BeeConst.UNDEF;
  }

  @Override
  public IsRow getActiveRow() {
    int index = getActiveRowIndex();
    if (isRowWithinBounds(index)) {
      return getRowData().get(index);
    } else {
      return null;
    }
  }

  public int getBodyWidth() {
    int width = 0;
    int incr = getBodyCellWidthIncrement();

    List<ColumnInfo> columns = getColumns();

    for (ColumnInfo columnInfo : columns) {
      int w = columnInfo.getWidth();
      if (w <= 0) {
        width = BeeConst.UNDEF;
        break;
      }
      width += w + incr;
    }
    return width;
  }

  public int getChildrenHeight() {
    return getHeaderHeight() + getBodyHeight() + getFooterHeight();
  }

  public int getColumnCount() {
    return visibleColumns.size();
  }

  public String getColumnId(int col) {
    return getColumnInfo(col).getColumnId();
  }

  public int getColumnWidth(int col) {
    return getColumnInfo(col).getWidth();
  }

  public int getColumnWidth(String columnId) {
    ColumnInfo info = getColumnInfo(columnId);
    if (info == null) {
      return BeeConst.UNDEF;
    }
    return info.getWidth();
  }

  public int getDataSize() {
    return getRowData().size();
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "cell-grid";
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }

  @Override
  public int getPageStart() {
    return pageStart;
  }

  public List<ColumnInfo> getPredefinedColumns() {
    return predefinedColumns;
  }

  public IsRow getRowById(long rowId) {
    for (IsRow row : getRowData()) {
      if (row.getId() == rowId) {
        return row;
      }
    }
    return null;
  }

  @Override
  public int getRowCount() {
    return rowCount;
  }

  @Override
  public List<IsRow> getRowData() {
    return rowData;
  }

  public ConditionalStyle getRowStyles() {
    return rowStyles;
  }

  public LinkedHashMap<Long, RowInfo> getSelectedRows() {
    return selectedRows;
  }

  @Override
  public Order getSortOrder() {
    return sortOrder;
  }

  public List<Integer> getVisibleColumns() {
    return visibleColumns;
  }

  public int getZIndex() {
    return zIndex;
  }

  public boolean handleKeyboardNavigation(int keyCode, boolean hasModifiers) {
    if (getActiveRowIndex() < 0 || getActiveColumnIndex() < 0) {
      return false;
    }

    switch (keyCode) {
      case KeyCodes.KEY_DOWN:
        keyboardNext();
        return true;

      case KeyCodes.KEY_UP:
        keyboardPrev();
        return true;

      case KeyCodes.KEY_PAGEDOWN:
        if (hasModifiers) {
          keyboardEnd();
        } else {
          keyboardNextPage();
        }
        return true;

      case KeyCodes.KEY_PAGEUP:
        if (hasModifiers) {
          keyboardHome();
        } else {
          keyboardPrevPage();
        }
        return true;

      case KeyCodes.KEY_HOME:
        if (hasModifiers) {
          keyboardHome();
        }
        return true;

      case KeyCodes.KEY_END:
        if (hasModifiers) {
          keyboardEnd();
        }
        return true;

      case KeyCodes.KEY_LEFT:
        if (getActiveColumnIndex() > 0) {
          setActiveColumnIndex(getActiveColumnIndex() - 1);
        }
        return true;

      case KeyCodes.KEY_BACKSPACE:
        keyboardLeft();
        return true;

      case KeyCodes.KEY_RIGHT:
        if (getActiveColumnIndex() < getColumnCount() - 1) {
          setActiveColumnIndex(getActiveColumnIndex() + 1);
        }
        return true;

      case KeyCodes.KEY_TAB:
        if (hasModifiers) {
          keyboardLeft();
        } else {
          keyboardRight();
        }
        return true;

      default:
        return false;
    }
  }

  public void initRenderMode(String mode) {
    RenderMode rm = null;

    if (!BeeUtils.isEmpty(mode)) {
      rm = NameUtils.getEnumByName(RenderMode.class, mode);
      if (rm != null) {
        setRenderMode(rm);
        return;
      }
    }

    if (getRowStyles() != null) {
      rm = RenderMode.FULL;
    } else {
      List<ColumnInfo> columns = getColumns();
      for (ColumnInfo columnInfo : columns) {
        if (columnInfo.getDynStyles() != null || columnInfo.getAutoFitRows() > 0) {
          rm = RenderMode.FULL;
          break;
        }
      }
    }

    setRenderMode(rm);
  }

  public void insertRow(IsRow rowValue, boolean focus) {
    Assert.notNull(rowValue);

    int rc = getRowCount();
    int ps = getPageSize();
    int ar = getActiveRowIndex();

    int nr = BeeConst.UNDEF;

    if (rc <= ps || ps <= 0) {
      if (ar >= 0 && ar < rc - 1) {
        getRowData().add(ar + 1, rowValue);
        nr = ar + 1;
      } else {
        getRowData().add(rowValue);
        nr = getDataSize() - 1;
      }
    } else if (ar >= 0 && ar < ps - 1) {
      getRowData().add(ar + 1, rowValue);
      nr = ar + 1;
      if (getDataSize() > ps) {
        getRowData().remove(getDataSize() - 1);
      }
    } else {
      if (getDataSize() >= ps) {
        getRowData().remove(0);
        setPageStart(getPageStart() + 1, false, false, NavigationOrigin.SYSTEM);
      }
      getRowData().add(rowValue);
      nr = getDataSize() - 1;
    }

    if (ps >= 0 && ps == rc) {
      setPageSize(ps + 1, false);
    }
    setRowCount(rc + 1, true);

    if (rc <= ps || ps <= 0) {
      estimateColumnWidths(getRowData(), nr, nr + 1);
    }
    doFlexLayout();

    this.activeRowIndex = nr;
    if (getActiveColumnIndex() < 0) {
      this.activeColumnIndex = 0;
    }
    render(focus);
  }

  public boolean isColumnReadOnly(String columnId) {
    ColumnInfo info = getColumnInfo(columnId);
    if (info == null) {
      return false;
    }
    return info.isColReadOnly();
  }

  @Override
  public boolean isEditing() {
    return editing;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public boolean isRowEditable(IsRow rowValue) {
    boolean ok = (rowValue != null) && rowValue.isEditable();
    if (ok && getRowEditable() != null) {
      getRowEditable().update(rowValue);
      ok = BeeUtils.toBoolean(getRowEditable().evaluate());
    }
    return ok;
  }

  public boolean isRowSelected(long rowId) {
    return getSelectedRows().containsKey(rowId);
  }

  public boolean isSortable(String columnId) {
    ColumnInfo info = getColumnInfo(columnId);
    if (info == null) {
      return false;
    }
    return info.getColumn().isSortable();
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    String eventType = event.getType();

    if (isEditing()) {
      if (EventUtils.isMouseWheel(eventType)) {
        event.preventDefault();
      }
      return;
    }

    EventTarget eventTarget = event.getEventTarget();
    if (!Element.is(eventTarget)) {
      return;
    }
    Element target = Element.as(eventTarget);

    TargetType targetType = null;
    String rowIdx = null;
    int row = BeeConst.UNDEF;
    int col = BeeConst.UNDEF;

    if (target == getElement()) {
      targetType = TargetType.CONTAINER;

    } else if (isResizerOrResizerChild(target)) {
      targetType = TargetType.RESIZER;

    } else if (getElement().isOrHasChild(target)) {
      while (target != null && target != getElement()) {
        rowIdx = DomUtils.getDataRow(target);
        if (!BeeUtils.isEmpty(rowIdx)) {
          break;
        }
        target = target.getParentElement();
      }

      if (!BeeUtils.isEmpty(rowIdx)) {
        col = BeeUtils.toInt(DomUtils.getDataColumn(target));
        checkColumnBounds(col);

        if (isHeaderRow(rowIdx)) {
          targetType = TargetType.HEADER;
        } else if (isFooterRow(rowIdx)) {
          targetType = TargetType.FOOTER;
        } else if (isBodyRow(rowIdx)) {
          targetType = TargetType.BODY;
          row = BeeUtils.toInt(rowIdx);
          checkRowBounds(row);
        }
      }
    }

    if (targetType == null) {
      return;
    }

    getResizerShowTimer().handleEvent(event);

    if (EventUtils.isMouseMove(eventType)) {
      if (handleMouseMove(event, target, targetType, rowIdx, col)) {
        return;
      }

    } else if (EventUtils.isMouseDown(eventType)) {
      if (targetType == TargetType.RESIZER) {
        startResizing(event);
        event.preventDefault();
        return;
      }

    } else if (EventUtils.isMouseUp(eventType)) {
      if (isResizing()) {
        stopResizing();
        event.preventDefault();
        updatePageSize();
        return;
      }
      if (isCellActive(row, col)) {
        checkCellSize(target, row, col);
      }

    } else if (EventUtils.isMouseOut(eventType)) {
      if (targetType == TargetType.RESIZER && !isResizing()) {
        hideResizer();
        return;
      }
      if (event.getRelatedEventTarget() != null
          && !getElement().isOrHasChild(Node.as(event.getRelatedEventTarget()))) {
        if (isResizing()) {
          stopResizing();
        } else if (isResizerVisible()) {
          hideResizer();
        }
        return;
      }
    }

    if (targetType == TargetType.HEADER) {
      ColumnHeader header = getColumnInfo(col).getHeader();
      if (header != null && cellConsumesEventType(header.getCell(), eventType)) {
        CellContext context = new CellContext(0, col, null, this);
        header.onBrowserEvent(context, target, event);
      }

    } else if (targetType == TargetType.FOOTER) {
      ColumnFooter footer = getColumnInfo(col).getFooter();
      if (footer != null && cellConsumesEventType(footer.getCell(), eventType)) {
        CellContext context = new CellContext(0, col, null, this);
        footer.onBrowserEvent(context, target, event);
      }

    } else if (targetType == TargetType.BODY) {
      IsRow rowValue = getDataItem(row);
      ColumnInfo columnInfo = getColumnInfo(col);

      if (EventUtils.isClick(eventType)) {
        if (EventUtils.hasModifierKey(event) || columnInfo.isSelection()) {
          if (event.getShiftKey()) {
            selectRange(row, rowValue);
          } else {
            selectRow(row, rowValue);
          }
          activateCell(row, col);

        } else if (isCellActive(row, col)) {
          if (columnInfo.isActionColumn()) {
            fireEventToCell(row, col, event, eventType, target, rowValue);
          } else {
            startEditing(rowValue, col, target, EditStartEvent.CLICK);
          }

        } else {
          activateCell(row, col);
          if (columnInfo.isActionColumn()) {
            fireEventToCell(row, col, event, eventType, target, rowValue);
          } else if (columnInfo.getColumn().instantKarma(rowValue)) {
            startEditing(rowValue, col, target, EditStartEvent.CLICK);
          }
        }
        return;

      } else if (EventUtils.isKeyDown(eventType)) {
        if (!isCellActive(row, col)) {
          event.preventDefault();
          refocus();
          return;
        }

        int keyCode = event.getKeyCode();
        boolean hasModifiers = EventUtils.hasModifierKey(event);

        if (handleKey(keyCode, hasModifiers, row, col, target)) {
          event.preventDefault();
        } else if (columnInfo.isSelection() && !hasModifiers) {
          event.preventDefault();
          selectRow(row, rowValue);
        } else if (keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_DELETE) {
          event.preventDefault();
          if (!hasModifiers) {
            startEditing(rowValue, col, target, EditStartEvent.getStartKey(keyCode));
          }
        }
        return;

      } else if (EventUtils.isKeyPress(eventType)) {
        int charCode = event.getCharCode();
        event.preventDefault();

        if (charCode == BeeConst.CHAR_SPACE) {
          selectRow(row, rowValue);
        } else if (charCode > BeeConst.CHAR_SPACE) {
          startEditing(rowValue, col, target, charCode);
        }
        return;
      }

      fireEventToCell(row, col, event, eventType, target, rowValue);
    }
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
    Assert.notNull(event);

    long rowId = event.getRowId();
    long version = event.getVersion();

    RowInfo selectedRowInfo = getSelectedRows().get(rowId);
    if (selectedRowInfo != null) {
      selectedRowInfo.setVersion(version);
    }

    int row = getRowIndex(rowId);
    if (!isRowWithinBounds(row)) {
      return;
    }

    IsRow rowValue = getDataItem(row);
    event.applyTo(rowValue);

    List<Integer> indexBySource = getColumnIndexBySourceName(event.getSourceName());
    Integer col;

    if (indexBySource.size() == 1) {
      col = indexBySource.get(0);
    } else {
      col = null;
    }

    boolean checkZindex = false;
    if (getRowStyles() != null) {
      refreshRow(row);
      checkZindex = true;
    } else {
      if (hasCalculatedOrActionColumns()) {
        refreshCalculatedAndActionColumns(row);
        checkZindex = true;
      }

      for (int i : indexBySource) {
        if (getColumnInfo(i).isCalculated() || getColumnInfo(i).isActionColumn()) {
          continue;
        }
        if (getColumnInfo(i).isRenderable()) {
          checkZindex = true;
        }

        if (getColumnInfo(i).getDynStyles() != null) {
          refreshCell(row, i);
        } else {
          updateCellContent(row, i);
        }
      }
    }
    
    refreshFooters(event.getSourceName());

    if (checkZindex && col != null) {
      bringToFront(row, col);
    }
  }

  @Override
  public void onMultiDelete(MultiDeleteEvent event) {
    Assert.notNull(event);
    for (RowInfo rowInfo : event.getRows()) {
      deleteRow(rowInfo.getId());
    }
  }

  @Override
  public void onResize() {
    if (getRowData().isEmpty()) {
      wasLayoutDone = false;

    } else {
      boolean changed = doFlexLayout();
      if (changed) {
        render(false);
      }

      updatePageSize();
    }
  }

  @Override
  public void onRowDelete(RowDeleteEvent event) {
    Assert.notNull(event);
    deleteRow(event.getRowId());
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    Assert.notNull(event);
    IsRow newRow = event.getRow();
    Assert.notNull(newRow);

    long rowId = newRow.getId();
    long version = newRow.getVersion();

    RowInfo selectedRowInfo = getSelectedRows().get(rowId);
    if (selectedRowInfo != null) {
      selectedRowInfo.setVersion(version);
    }

    int row = getRowIndex(rowId);
    if (!isRowWithinBounds(row)) {
      return;
    }

    IsRow rowValue = getDataItem(row);
    rowValue.setVersion(version);
    for (int i = 0; i < rowValue.getNumberOfCells(); i++) {
      rowValue.setValue(i, newRow.getString(i));
    }

    if (newRow.getProperties() != null) {
      rowValue.setProperties(newRow.getProperties().copy());
    }

    refreshRow(row);
    refreshFooters(null);

    if (getActiveRowIndex() == row && getActiveColumnIndex() >= 0) {
      bringToFront(row, getActiveColumnIndex());
    }

    logger.info("grid updated row:", rowId, TimeUtils.toTimeString(version));
  }

  @Override
  public void onVisibilityChange(VisibilityChangeEvent event) {
    if (event.isVisible() && DomUtils.isOrHasAncestor(getElement(), event.getId())) {
      if (!getRowData().isEmpty() && !wasLayoutDone) {
        boolean changed = doFlexLayout();
        if (changed) {
          render(false);
        }
      }
    }
  }

  public void preliminaryUpdate(long rowId, String source, String value) {
    int row = getRowIndex(rowId);
    if (!isRowWithinBounds(row)) {
      return;
    }

    List<Integer> colIndexes = getColumnIndexBySourceName(source);
    for (int col : colIndexes) {
      Element cellElement = getCellElement(row, col);
      if (cellElement == null) {
        continue;
      }

      if (BeeUtils.isEmpty(value)) {
        cellElement.setInnerHTML(BeeConst.STRING_EMPTY);
        continue;
      }

      IsRow rowValue = getDataItem(row).copy();
      getColumnInfo(col).getSource().set(rowValue, value);

      AbstractColumn<?> column = getColumn(col);

      SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
      CellContext context = new CellContext(row, col, rowValue, this);
      column.render(context, rowValue, cellBuilder);
      SafeHtml cellHtml = cellBuilder.toSafeHtml();

      cellElement.setInnerHTML(cellHtml.asString());
    }
  }

  public void refocus() {
    Element cellElement = getActiveCellElement();
    if (cellElement != null) {
      cellElement.focus();
    }
  }

  @Override
  public void refresh() {
    if (getRowData().isEmpty()) {
      wasLayoutDone = false;

    } else {
      maybeUpdateColumnWidths();
      if (!wasLayoutDone) {
        doFlexLayout();
      }
    }

    render(true);
  }

  public int refreshCellContent(long rowId, String sourceName) {
    int row = getRowIndex(rowId);
    if (!isRowWithinBounds(row)) {
      logger.warning("refreshCell: row id", rowId, "is not visible");
      return 0;
    }

    List<Integer> colIndexes = getColumnIndexBySourceName(sourceName);
    for (int col : colIndexes) {
      updateCellContent(row, col);
    }
    return colIndexes.size();
  }

  public void refreshFooters(String sourceName) {
    for (int i = 0; i < getColumnCount(); i++) {
      ColumnFooter footer = getColumnInfo(i).getFooter();

      if (footer != null && (BeeUtils.isEmpty(sourceName) || footer.dependsOnSource(sourceName))) {
        SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
        CellContext context = new CellContext(0, i, null, this);
        footer.render(context, cellBuilder);
        SafeHtml cellHtml = cellBuilder.toSafeHtml();

        Element cellElement = getFooterCellElement(i);
        if (cellElement == null) {
          logger.warning("footer cell not found: col", i);
        } else {
          cellElement.setInnerHTML(cellHtml.asString());
        }
      }
    }
  }

  @Override
  public boolean removeRowById(long rowId) {
    deleteRow(rowId);

    int rowIndex = getRowIndex(rowId);
    if (isRowWithinBounds(rowIndex)) {
      getRowData().remove(rowIndex);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void reset() {
    getRenderedRows().clear();

    getResizedRows().clear();
    getResizedCells().clear();

    if (!getSelectedRows().isEmpty()) {
      getSelectedRows().clear();
      fireSelectionCountChange();
    }
  }

  public int resizeColumn(int col, int newWidth) {
    int oldWidth = getColumnWidth(col);
    return resizeColumnWidth(col, oldWidth, newWidth - oldWidth);
  }

  public void setBodyBorderWidth(Edges borderWidth) {
    getBodyComponent().setBorderWidth(borderWidth);
  }

  public void setBodyCellHeight(int cellHeight) {
    getBodyComponent().setCellHeight(cellHeight);
  }

  public void setBodyCellMargin(Edges margin) {
    getBodyComponent().setMargin(margin);
  }

  public void setBodyCellPadding(Edges padding) {
    getBodyComponent().setPadding(padding);
  }

  public void setBodyComponent(GridComponentDescription src) {
    updateComponent(src, getBodyComponent());
  }

  public void setBodyFont(String fontDeclaration) {
    getBodyComponent().setFont(fontDeclaration);
  }

  public void setColumnBodyFont(String columnId, String fontDeclaration) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setBodyFont(fontDeclaration);
  }

  public void setColumnBodyWidth(String columnId, int width) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setBodyWidth(width);
  }

  public void setColumnFooterFont(String columnId, String fontDeclaration) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setFooterFont(fontDeclaration);
  }

  public void setColumnFooterWidth(String columnId, int width) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setFooterWidth(width);
  }

  public void setColumnHeaderFont(String columnId, String fontDeclaration) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setHeaderFont(fontDeclaration);
  }

  public void setColumnHeaderWidth(String columnId, int width) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setHeaderWidth(width);
  }

  public void setColumnWidth(String columnId, double width, CssUnit unit) {
    int containerSize = getOffsetWidth();
    Assert.isPositive(containerSize);
    setColumnWidth(columnId, width, unit, containerSize);
  }

  public void setColumnWidth(String columnId, int width) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setResizedWidth(width);
  }

  public void setDefaultFlexibility(Flexibility defaultFlexibility) {
    this.defaultFlexibility = defaultFlexibility;
  }

  @Override
  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void setFooterBorderWidth(Edges borderWidth) {
    getFooterComponent().setBorderWidth(borderWidth);
  }

  public void setFooterCellHeight(int cellHeight) {
    getFooterComponent().setCellHeight(cellHeight);
  }

  public void setFooterCellMargin(Edges margin) {
    getFooterComponent().setMargin(margin);
  }

  public void setFooterCellPadding(Edges padding) {
    getFooterComponent().setPadding(padding);
  }

  public void setFooterComponent(GridComponentDescription src) {
    updateComponent(src, getFooterComponent());
  }

  public void setFooterFont(String fontDeclaration) {
    getFooterComponent().setFont(fontDeclaration);
  }

  public void setHeaderBorderWidth(Edges borderWidth) {
    getHeaderComponent().setBorderWidth(borderWidth);
  }

  public void setHeaderCellHeight(int cellHeight) {
    getHeaderComponent().setCellHeight(cellHeight);
  }

  public void setHeaderCellMargin(Edges margin) {
    getHeaderComponent().setMargin(margin);
  }

  public void setHeaderCellPadding(Edges padding) {
    getHeaderComponent().setPadding(padding);
  }

  public void setHeaderComponent(GridComponentDescription src) {
    updateComponent(src, getHeaderComponent());
  }

  public void setHeaderFont(String fontDeclaration) {
    getHeaderComponent().setFont(fontDeclaration);
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setMaxBodyCellHeight(int maxHeight) {
    getBodyComponent().setMaxHeight(maxHeight);
  }

  public void setMaxCellWidth(int maxCellWidth) {
    this.maxCellWidth = maxCellWidth;
  }

  public void setMinBodyCellHeight(int minHeight) {
    getBodyComponent().setMinHeight(minHeight);
  }

  public void setMinCellWidth(int minCellWidth) {
    this.minCellWidth = minCellWidth;
  }

  @Override
  public void setPageSize(int size, boolean fireScopeChange) {
    if (size == getPageSize()) {
      return;
    }

    this.pageSize = size;

    if (fireScopeChange) {
      fireScopeChange();
    }
  }

  @Override
  public void setPageStart(int start, boolean fireScopeChange, boolean fireDataRequest,
      NavigationOrigin origin) {
    Assert.nonNegative(start);
    if (start == getPageStart()) {
      return;
    }

    this.pageStart = start;

    if (fireScopeChange) {
      fireScopeChange();
    }
    if (fireDataRequest) {
      fireDataRequest(origin);
    }
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setRowChangeSensitivityMillis(Integer millis) {
    if (millis != null) {
      rowChangeScheduler.setSensitivityMillis(millis);
    }
  }

  @Override
  public void setRowCount(int count, boolean fireScopeChange) {
    Assert.nonNegative(count);
    if (count == getRowCount()) {
      return;
    }

    this.rowCount = count;

    if (getActiveRowIndex() >= count) {
      if (count <= 0) {
        this.activeRowIndex = BeeConst.UNDEF;
        this.activeColumnIndex = BeeConst.UNDEF;
      } else {
        this.activeRowIndex = count - 1;
      }
    }

    int start = getPageStart();
    int length = getPageSize();
    if (start > 0 && length > 0 && start + length > count) {
      start = Math.max(count - length, 0);
    }

    if (start != getPageStart()) {
      setPageStart(start, true, false, NavigationOrigin.SYSTEM);
    } else if (fireScopeChange) {
      fireScopeChange();
    }
  }

  @Override
  public void setRowData(List<? extends IsRow> rows, boolean refresh) {
    getRowData().clear();
    if (!BeeUtils.isEmpty(rows)) {
      getRowData().addAll(rows);

      if (!areColumnWidthsEstimated) {
        estimateColumnWidths();
      }
    }

    if (refresh) {
      refresh();
    }
  }

  public void setRowEditable(Evaluator rowEditable) {
    this.rowEditable = rowEditable;
  }

  public void setRowStyles(ConditionalStyle rowStyles) {
    this.rowStyles = rowStyles;
  }

  @Override
  public void updateActiveRow(List<? extends IsRow> rows) {
    Assert.notNull(rows);
    int oldRow = getActiveRowIndex();

    if (oldRow >= 0 && oldRow < getDataSize()) {
      int newRow = 0;
      long id = getRowData().get(oldRow).getId();
      for (int i = 0; i < rows.size(); i++) {
        if (rows.get(i).getId() == id) {
          newRow = i;
          break;
        }
      }
      this.activeRowIndex = newRow;
    }
  }

  public void updateOrder(int col, NativeEvent event) {
    checkColumnBounds(col);
    if (getColumn(col).isSortable() && getRowCount() > 1) {
      updateOrder(getColumnId(col), EventUtils.hasModifierKey(event));
      getRenderedRows().clear();
      SortEvent.fire(this, getSortOrder());
    }
  }

  public boolean updateVisibleColumns(List<Integer> columns) {
    if (!columns.isEmpty() && !columns.equals(getVisibleColumns())) {
      List<Integer> oldColumns = Lists.newArrayList(visibleColumns);

      visibleColumns.clear();
      visibleColumns.addAll(columns);

      getRenderedRows().clear();

      getResizedRows().clear();
      getResizedCells().clear();

      for (int col = 0; col < columns.size(); col++) {
        if (!oldColumns.contains(columns.get(col))) {
          estimateHeaderWidth(col, true);
          estimateColumnWidth(col, true);
        }
      }

      doFlexLayout();
      render(false);

      updatePageSize();
      return true;

    } else {
      return false;
    }
  }

  @Override
  protected void onUnload() {
    getResizerShowTimer().cancel();
    getResizerMoveTimer().cancel();

    VisibilityChangeEvent.unregister(getId());

    super.onUnload();
  }

  protected void setResizerMoveSensitivityMillis(int resizerMoveSensitivityMillis) {
    this.resizerMoveSensitivityMillis = resizerMoveSensitivityMillis;
  }

  protected void setResizerShowSensitivityMillis(int resizerShowSensitivityMillis) {
    this.resizerShowSensitivityMillis = resizerShowSensitivityMillis;
  }

  List<ColumnInfo> getColumns() {
    List<ColumnInfo> columns = Lists.newArrayList();
    for (int index : visibleColumns) {
      columns.add(predefinedColumns.get(index));
    }
    return columns;
  }

  private void activateCell(int row, int col) {
    if (getActiveRowIndex() == row) {
      setActiveColumnIndex(col);
      return;
    }
    onActivateCell(false);
    onActivateRow(false);

    this.activeRowIndex = row;
    this.activeColumnIndex = col;

    onActivateRow(true);
    onActivateCell(true);
  }

  private void activateRow(int index, int start, NavigationOrigin origin) {
    int rc = getRowCount();
    if (rc <= 0) {
      return;
    }
    if (rc <= 1) {
      setActiveRowIndex(0);
      return;
    }

    int absIndex = BeeUtils.clamp(index, 0, rc - 1);
    int oldPageStart = getPageStart();
    if (oldPageStart + getActiveRowIndex() == absIndex) {
      return;
    }

    int size = getPageSize();
    if (size <= 0 || size >= rc) {
      setActiveRowIndex(absIndex);
      return;
    }
    if (size == 1) {
      setActiveRowIndex(0);
      setPageStart(absIndex, true, true, origin);
      return;
    }

    int newPageStart;
    if (start >= 0 && absIndex >= start && absIndex < start + size) {
      newPageStart = start;
    } else if (absIndex >= oldPageStart && absIndex < oldPageStart + size) {
      newPageStart = oldPageStart;
    } else if (absIndex == oldPageStart - 1) {
      newPageStart = absIndex;
    } else if (absIndex == oldPageStart + size && getActiveRowIndex() == size - 1) {
      newPageStart = oldPageStart + 1;
    } else {
      newPageStart = (absIndex / size) * size;
    }
    newPageStart = BeeUtils.clamp(newPageStart, 0, rc - size);

    setActiveRowIndex(absIndex - newPageStart);

    if (newPageStart != oldPageStart) {
      setPageStart(newPageStart, true, true, origin);
    }
  }

  private void activateRow(int index, NavigationOrigin origin) {
    activateRow(index, BeeConst.UNDEF, origin);
  }

  private void bringToFront(int row, int col) {
    Element cellElement = getCellElement(row, col);
    Assert.notNull(cellElement);

    if (StyleUtils.getZIndex(cellElement) >= getZIndex()) {
      return;
    }
    cellElement.getStyle().setZIndex(incrementZIndex());

    if (!isCellActive(row, col)) {
      cellElement = getActiveCellElement();
      if (cellElement != null) {
        cellElement.getStyle().setZIndex(incrementZIndex());
      }
    }
  }

  private void checkCellSize(Element element, int row, int col) {
    int width = StyleUtils.getWidth(element);
    int height = StyleUtils.getHeight(element);
    if (width <= 0 || height <= 0) {
      return;
    }

    long rowId = getRowIdByIndex(row);
    String columnId = getColumnId(col);

    CellInfo cellInfo;
    Element nextElement;
    Edges padding;
    Edges borders;

    if (width == getColumnWidth(col) && height == getRowHeightById(rowId)) {
      cellInfo = getResizedCells().remove(rowId, columnId);
      if (cellInfo != null) {
        element.removeClassName(STYLE_RESIZED_CELL);

        if (row < getDataSize() - 1) {
          nextElement = getCellElement(row + 1, col);
          if (nextElement != null) {
            padding = cellInfo.getNextRowPadding();
            if (padding != null) {
              Edges cellPadding = new Edges(nextElement, StyleUtils.STYLE_PADDING);
              cellPadding.setTop(padding);
              cellPadding.setBottom(padding);
              cellPadding.applyTo(nextElement, StyleUtils.STYLE_PADDING);
            }
            borders = cellInfo.getNextRowBorders();
            if (borders != null) {
              Edges cellBorders = new Edges(nextElement, StyleUtils.STYLE_BORDER_WIDTH);
              cellBorders.setTop(borders);
              cellBorders.applyTo(nextElement, StyleUtils.STYLE_BORDER_WIDTH);
            }
          }
        }

        if (col < getColumnCount() - 1) {
          nextElement = getCellElement(row, col + 1);
          if (nextElement != null) {
            padding = cellInfo.getNextColumnPadding();
            if (padding != null) {
              Edges cellPadding = new Edges(nextElement, StyleUtils.STYLE_PADDING);
              cellPadding.setLeft(padding);
              cellPadding.setRight(padding);
              cellPadding.applyTo(nextElement, StyleUtils.STYLE_PADDING);
            }
            borders = cellInfo.getNextColumnBorders();
            if (borders != null) {
              Edges cellBorders = new Edges(nextElement, StyleUtils.STYLE_BORDER_WIDTH);
              cellBorders.setLeft(borders);
              cellBorders.applyTo(nextElement, StyleUtils.STYLE_BORDER_WIDTH);
            }
          }
        }
      }

    } else {
      cellInfo = getResizedCells().get(rowId, columnId);
      if (cellInfo == null) {
        element.addClassName(STYLE_RESIZED_CELL);
        cellInfo = new CellInfo(width, height);

        if (row < getDataSize() - 1 && Edges.hasPositiveTop(getBodyBorderWidth())) {
          nextElement = getCellElement(row + 1, col);
          if (nextElement != null) {
            padding = new Edges(nextElement, StyleUtils.STYLE_PADDING);
            borders = new Edges(nextElement, StyleUtils.STYLE_BORDER_WIDTH);

            if (borders.getIntTop() <= 0) {
              cellInfo.setNextRowPadding(Edges.copyOf(padding));
              cellInfo.setNextRowBorders(Edges.copyOf(borders));

              int px = getBodyBorderWidth().getIntTop();
              borders.setTop(borders.getIntTop() + px);
              padding = incrementEdges(padding, 0, -px);

              padding.applyTo(nextElement, StyleUtils.STYLE_PADDING);
              borders.applyTo(nextElement, StyleUtils.STYLE_BORDER_WIDTH);
            }
          }
        }

        if (col < getColumnCount() - 1 && Edges.hasPositiveLeft(getBodyBorderWidth())) {
          nextElement = getCellElement(row, col + 1);
          if (nextElement != null) {
            padding = new Edges(nextElement, StyleUtils.STYLE_PADDING);
            borders = new Edges(nextElement, StyleUtils.STYLE_BORDER_WIDTH);

            if (borders.getIntLeft() <= 0) {
              cellInfo.setNextColumnPadding(Edges.copyOf(padding));
              cellInfo.setNextColumnBorders(Edges.copyOf(borders));

              int px = getBodyBorderWidth().getIntLeft();
              borders.setLeft(borders.getIntLeft() + px);
              padding = incrementEdges(padding, -px, 0);

              padding.applyTo(nextElement, StyleUtils.STYLE_PADDING);
              borders.applyTo(nextElement, StyleUtils.STYLE_BORDER_WIDTH);
            }
          }
        }

        getResizedCells().put(rowId, columnId, cellInfo);
      } else {
        cellInfo.setSize(width, height);
      }
    }
  }

  private void checkColumnBounds(int col) {
    Assert.isTrue(isColumnWithinBounds(col));
  }

  private boolean checkResizerBounds(int position) {
    return position >= getResizerPositionMin() && position <= getResizerPositionMax();
  }

  private void checkRowBounds(int row) {
    Assert.isTrue(isRowWithinBounds(row), "row index " + row + " out of bounds: page size "
        + getPageSize() + ", row count " + getRowCount() + ", data size " + getDataSize());
  }

  private void deleteRow(long rowId) {
    if (getRenderedRows().contains(rowId)) {
      getRenderedRows().clear();
    }

    if (isRowSelected(rowId)) {
      getSelectedRows().remove(rowId);
      fireSelectionCountChange();
    }

    if (getResizedRows().containsKey(rowId)) {
      getResizedRows().remove(rowId);
    }
    if (getResizedCells().containsRow(rowId)) {
      getResizedCells().rowKeySet().remove(rowId);
    }
  }

  private int estimateBodyCellWidth(int rowIndex, int col, IsRow rowValue,
      AbstractColumn<?> column, Font font) {
    SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
    CellContext context = new CellContext(rowIndex, col, rowValue, this);
    column.render(context, rowValue, cellBuilder);
    SafeHtml cellHtml = cellBuilder.toSafeHtml();

    return Rulers.getLineWidth(font, cellHtml.asString(), true);
  }

  private int estimateColumnWidth(int col, boolean ensure) {
    return estimateColumnWidth(col, getRowData(), ensure);
  }

  private <T extends IsRow> int estimateColumnWidth(int col, List<T> rows, boolean ensure) {
    Assert.notNull(rows);
    return estimateColumnWidth(col, rows, 0, rows.size(), ensure);
  }

  private <T extends IsRow> int estimateColumnWidth(int col, List<T> rows,
      int start, int end, boolean ensure) {
    Assert.notNull(rows);

    ColumnInfo columnInfo = getColumnInfo(col);
    AbstractColumn<?> column = columnInfo.getColumn();
    Font font = Font.merge(getBodyComponent().getFont(), columnInfo.getBodyFont());

    int width = 0;
    for (int i = start; i < end; i++) {
      IsRow rowValue = rows.get(i);
      if (rowValue == null) {
        continue;
      }
      width = Math.max(width, estimateBodyCellWidth(i, col, rowValue, column, font));
    }

    if (width > 0) {
      if (ensure) {
        columnInfo.ensureBodyWidth(width);
      } else {
        columnInfo.setBodyWidth(width);
      }
    }
    return width;
  }

  private void fireDataRequest(NavigationOrigin origin) {
    fireEvent(new DataRequestEvent(origin));
  }

  private void fireEventToCell(int row, int col, Event event, String eventType,
      Element parentElem, IsRow rowValue) {
    AbstractColumn<?> column = getColumn(col);
    if (cellConsumesEventType(column.getCell(), eventType)) {
      CellContext context = new CellContext(row, col, rowValue, this);
      column.onBrowserEvent(context, parentElem, rowValue, event);
    }
  }

  private void fireScopeChange() {
    fireEvent(new ScopeChangeEvent(getPageStart(), getPageSize(), getRowCount()));
  }

  private void fireSelectionCountChange() {
    fireEvent(new SelectionCountChangeEvent(getSelectedRows().size()));
  }

  private Element getActiveCellElement() {
    if (getActiveRowIndex() >= 0 && getActiveColumnIndex() >= 0) {
      return getBodyCellElement(getActiveRowIndex(), getActiveColumnIndex());
    } else {
      return null;
    }
  }

  private int getActiveColumnIndex() {
    return activeColumnIndex;
  }

  private NodeList<Element> getActiveRowElements() {
    return getRowElements(getActiveRowIndex());
  }

  private int getActiveRowIndex() {
    return activeRowIndex;
  }

  private Edges getBodyBorderWidth() {
    return getBodyComponent().getBorderWidth();
  }

  private Element getBodyCellElement(int row, int col) {
    return Selectors.getElement(getElement(), getBodyCellSelector(row, col));
  }

  private int getBodyCellHeight() {
    return getBodyComponent().getCellHeight();
  }

  private int getBodyCellHeightIncrement() {
    return getHeightIncrement(getBodyCellPadding(), getBodyBorderWidth(), getBodyCellMargin());
  }

  private Edges getBodyCellMargin() {
    return getBodyComponent().getMargin();
  }

  private Edges getBodyCellPadding() {
    return getBodyComponent().getPadding();
  }

  private int getBodyCellWidthIncrement() {
    return getWidthIncrement(getBodyCellPadding(), getBodyBorderWidth(), getBodyCellMargin());
  }

  private Component getBodyComponent() {
    return bodyComponent;
  }

  private int getBodyHeight() {
    int height = 0;
    int increment = getBodyCellHeightIncrement();
    for (int i = 0; i < getDataSize(); i++) {
      height += getRowHeight(i) + increment;
    }
    return height;
  }

  private Edges[][] getBorders(List<IsRow> rows, Edges defaultBorders, Edges margin) {
    if (rows == null || defaultBorders == null || defaultBorders.isEmpty()) {
      return null;
    }
    int rc = rows.size();
    int cc = getColumnCount();
    if (rc <= 0 || cc <= 0) {
      return null;
    }

    int left = BeeUtils.toNonNegativeInt(defaultBorders.getLeftValue());
    int right = BeeUtils.toNonNegativeInt(defaultBorders.getRightValue());
    int top = BeeUtils.toNonNegativeInt(defaultBorders.getTopValue());
    int bottom = BeeUtils.toNonNegativeInt(defaultBorders.getBottomValue());

    boolean collapseHorizontally =
        cc > 1 && left > 0 && right > 0 && !Edges.hasPositiveHorizontalValue(margin);
    boolean collapseVertically =
        top > 0 && bottom > 0 && !Edges.hasPositiveVerticalValue(margin);
    if (!collapseHorizontally && !collapseVertically) {
      return null;
    }

    Edges firstColumn = Edges.copyOf(defaultBorders);
    Edges lastColumn = Edges.copyOf(defaultBorders);
    Edges firstRow = Edges.copyOf(defaultBorders);
    Edges lastRow = Edges.copyOf(defaultBorders);
    Edges defaultCell = Edges.copyOf(defaultBorders);

    if (collapseHorizontally) {
      firstColumn.setRight(Math.max(left, right));
      defaultCell.setLeft(0);
      defaultCell.setRight(Math.max(left, right));
      lastColumn.setLeft(0);
    }

    if (collapseVertically) {
      if (hasHeaders() && Edges.hasPositiveBottom(getHeaderBorderWidth())
          && !Edges.hasPositiveBottom(getHeaderCellMargin())) {
        firstRow.setTop(0);
      }
      if (rc > 1) {
        firstRow.setBottom(Math.max(top, bottom));
      }
      defaultCell.setTop(0);
      defaultCell.setBottom(Math.max(top, bottom));
      lastRow.setTop(0);
      if (hasFooters() && Edges.hasPositiveTop(getFooterBorderWidth())
          && !Edges.hasPositiveTop(getFooterCellMargin())) {
        lastRow.setBottom(0);
        if (rc <= 1) {
          firstRow.setBottom(0);
        }
      }
    }

    if (collapseHorizontally && collapseVertically) {
      firstColumn.setTop(defaultCell);
      firstColumn.setBottom(defaultCell);
      lastColumn.setTop(defaultCell);
      lastColumn.setBottom(defaultCell);

      firstRow.setLeft(defaultCell);
      firstRow.setRight(defaultCell);
      lastRow.setLeft(defaultCell);
      lastRow.setRight(defaultCell);
    }

    Edges[][] borders = new Edges[rc][cc];
    Edges cellBorders = null;

    for (int i = 0; i < rc; i++) {
      for (int j = 0; j < cc; j++) {
        if (i == 0) {
          if (j == 0) {
            cellBorders = Edges.copyOf(firstRow);
            cellBorders.setLeft(firstColumn);
            cellBorders.setRight(firstColumn);
          } else if (j == cc - 1) {
            cellBorders = Edges.copyOf(firstRow);
            cellBorders.setLeft(lastColumn);
            cellBorders.setRight(lastColumn);
          } else {
            cellBorders = firstRow;
          }

        } else if (i == rc - 1) {
          if (j == 0) {
            cellBorders = Edges.copyOf(lastRow);
            cellBorders.setLeft(firstColumn);
            cellBorders.setRight(firstColumn);
          } else if (j == cc - 1) {
            cellBorders = Edges.copyOf(lastRow);
            cellBorders.setLeft(lastColumn);
            cellBorders.setRight(lastColumn);
          } else {
            cellBorders = lastRow;
          }

        } else {
          if (cc == 1) {
            cellBorders = defaultCell;
          } else if (j == 0) {
            cellBorders = firstColumn;
          } else if (j == cc - 1) {
            cellBorders = lastColumn;
          } else {
            cellBorders = defaultCell;
          }
        }
        borders[i][j] = cellBorders;
      }
    }

    if (!getResizedCells().isEmpty()) {
      for (int i = 0; i < rc; i++) {
        long rowId = rows.get(i).getId();
        if (!getResizedCells().containsRow(rowId)) {
          continue;
        }

        for (int j = 0; j < cc; j++) {
          if (!getResizedCells().contains(rowId, getColumnId(j))) {
            continue;
          }
          if (collapseVertically && i < rc - 1) {
            borders[i + 1][j] = Edges.copyOf(borders[i + 1][j]);
            borders[i + 1][j].setTop(defaultBorders);
          }
          if (collapseHorizontally && j < cc - 1) {
            borders[i][j + 1] = Edges.copyOf(borders[i][j + 1]);
            borders[i][j + 1].setLeft(defaultBorders);
          }
        }
      }
    }

    return borders;
  }

  private Element getCellElement(int rowIndex, int col) {
    return getCellElement(BeeUtils.toString(rowIndex), col);
  }

  private Element getCellElement(String rowIdx, int col) {
    return Selectors.getElement(getElement(), getCellSelector(rowIdx, col));
  }

  private AbstractColumn<?> getColumn(int col) {
    return getColumnInfo(col).getColumn();
  }

  private NodeList<Element> getColumnElements(int col) {
    return Selectors.getNodes(getElement(),
        Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_COLUMN, col));
  }

  private int getColumnIndex(String columnId) {
    Assert.notEmpty(columnId);
    for (int i = 0; i < getColumnCount(); i++) {
      if (getColumnInfo(i).is(columnId)) {
        return i;
      }
    }
    logger.warning("columnId", columnId, "not found");
    return BeeConst.UNDEF;
  }

  private List<Integer> getColumnIndexBySourceName(String name) {
    Assert.notEmpty(name);
    List<Integer> result = Lists.newArrayList();

    for (int i = 0; i < getColumnCount(); i++) {
      CellSource source = getColumnInfo(i).getSource();
      if (source != null && BeeUtils.same(source.getName(), name)) {
        result.add(i);
      }
    }
    return result;
  }

  private ColumnInfo getColumnInfo(int col) {
    checkColumnBounds(col);
    return predefinedColumns.get(visibleColumns.get(col));
  }

  private ColumnInfo getColumnInfo(String columnId) {
    Assert.notEmpty(columnId);
    List<ColumnInfo> columns = getColumns();
    for (ColumnInfo info : columns) {
      if (info.is(columnId)) {
        return info;
      }
    }
    return null;
  }

  private Component getComponent(String rowIdx) {
    if (BeeUtils.isEmpty(rowIdx)) {
      return null;
    }
    if (isHeaderRow(rowIdx)) {
      return getHeaderComponent();
    }
    if (isBodyRow(rowIdx)) {
      return getBodyComponent();
    }
    if (isFooterRow(rowIdx)) {
      return getFooterComponent();
    }
    return null;
  }

  private IsRow getDataItem(int rowIndex) {
    checkRowBounds(rowIndex);
    return getRowData().get(rowIndex);
  }

  private Flexibility getDefaultFlexibility() {
    return defaultFlexibility;
  }

  private RenderMode getEffectiveRenderMode() {
    if (getRowData().isEmpty() || getRowData().size() != getRenderedRows().size()) {
      return RenderMode.FULL;

    } else if (getRenderMode() != null) {
      return getRenderMode();

    } else {
      if (!getResizedRows().isEmpty()) {
        for (Long rowId : getRenderedRows()) {
          if (getResizedRows().containsKey(rowId)) {
            return RenderMode.FULL;
          }
        }
        for (IsRow row : getRowData()) {
          if (getResizedRows().containsKey(row.getId())) {
            return RenderMode.FULL;
          }
        }
      }

      if (!getResizedCells().isEmpty()) {
        for (Long rowId : getRenderedRows()) {
          if (getResizedCells().containsRow(rowId)) {
            return RenderMode.FULL;
          }
        }
        for (IsRow row : getRowData()) {
          if (getResizedCells().containsRow(row.getId())) {
            return RenderMode.FULL;
          }
        }
      }

      if (hasFooters()) {
        return RenderMode.FULL;
      }
      return RenderMode.CONTENT;
    }
  }

  private Edges getFooterBorderWidth() {
    return getFooterComponent().getBorderWidth();
  }

  private Element getFooterCellElement(int col) {
    return Selectors.getElement(getElement(), getFooterCellSelector(col));
  }

  private int getFooterCellHeight() {
    return getFooterComponent().getCellHeight();
  }

  private int getFooterCellHeightIncrement() {
    return getHeightIncrement(getFooterCellPadding(), getFooterBorderWidth(),
        getFooterCellMargin());
  }

  private Edges getFooterCellMargin() {
    return getFooterComponent().getMargin();
  }

  private Edges getFooterCellPadding() {
    return getFooterComponent().getPadding();
  }

  private Component getFooterComponent() {
    return footerComponent;
  }

  private NodeList<Element> getFooterElements() {
    if (hasFooters()) {
      return getRowElements(FOOTER_ROW);
    } else {
      return null;
    }
  }

  private int getFooterHeight() {
    if (hasFooters()) {
      return getFooterCellHeight() + getFooterCellHeightIncrement();
    } else {
      return 0;
    }
  }

  private Edges getHeaderBorderWidth() {
    return getHeaderComponent().getBorderWidth();
  }

  private Element getHeaderCellElement(int col) {
    return Selectors.getElement(getElement(), getHeaderCellSelector(col));
  }

  private int getHeaderCellHeight() {
    return getHeaderComponent().getCellHeight();
  }

  private int getHeaderCellHeightIncrement() {
    return getHeightIncrement(getHeaderCellPadding(), getHeaderBorderWidth(),
        getHeaderCellMargin());
  }

  private Edges getHeaderCellMargin() {
    return getHeaderComponent().getMargin();
  }

  private Edges getHeaderCellPadding() {
    return getHeaderComponent().getPadding();
  }

  private Component getHeaderComponent() {
    return headerComponent;
  }

  private NodeList<Element> getHeaderElements() {
    if (hasHeaders()) {
      return getRowElements(HEADER_ROW);
    } else {
      return null;
    }
  }

  private int getHeaderHeight() {
    if (hasHeaders()) {
      return getHeaderCellHeight() + getHeaderCellHeightIncrement();
    } else {
      return 0;
    }
  }

  private int getMaxCellWidth() {
    return maxCellWidth;
  }

  private int getMinCellWidth() {
    return minCellWidth;
  }

  private List<Long> getRenderedRows() {
    return renderedRows;
  }

  private RenderMode getRenderMode() {
    return renderMode;
  }

  private Table<Long, String, CellInfo> getResizedCells() {
    return resizedCells;
  }

  private Map<Long, Integer> getResizedRows() {
    return resizedRows;
  }

  private Element getResizerBar() {
    return Document.get().getElementById(resizerBarId);
  }

  private int getResizerCol() {
    return resizerCol;
  }

  private Element getResizerContainer() {
    return Document.get().getElementById(resizerId);
  }

  private Element getResizerHandle() {
    return Document.get().getElementById(resizerHandleId);
  }

  private Modifiers getResizerModifiers() {
    return resizerModifiers;
  }

  private int getResizerMoveSensitivityMillis() {
    return resizerMoveSensitivityMillis;
  }

  private ResizerMoveTimer getResizerMoveTimer() {
    return resizerMoveTimer;
  }

  private int getResizerPosition() {
    return resizerPosition;
  }

  private int getResizerPositionMax() {
    return resizerPositionMax;
  }

  private int getResizerPositionMin() {
    return resizerPositionMin;
  }

  private String getResizerRow() {
    return resizerRow;
  }

  private int getResizerShowSensitivityMillis() {
    return resizerShowSensitivityMillis;
  }

  private ResizerShowTimer getResizerShowTimer() {
    return resizerShowTimer;
  }

  private int getResizerStartValue() {
    return resizerStartValue;
  }

  private ResizerMode getResizerStatus() {
    return resizerStatus;
  }

  private Evaluator getRowEditable() {
    return rowEditable;
  }

  private NodeList<Element> getRowElements(int row) {
    return Selectors.getNodes(getElement(),
        Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, row));
  }

  private NodeList<Element> getRowElements(String rowIdx) {
    return Selectors.getNodes(getElement(),
        Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, rowIdx));
  }

  private int getRowHeight(int row) {
    return getRowHeightById(getRowIdByIndex(row));
  }

  private int getRowHeight(String rowIdx) {
    if (isHeaderRow(rowIdx)) {
      return getHeaderCellHeight();
    }
    if (isFooterRow(rowIdx)) {
      return getFooterCellHeight();
    }
    return getRowHeight(BeeUtils.toInt(rowIdx));
  }

  private int getRowHeightById(long id) {
    Integer height = getResizedRows().get(id);
    if (height == null) {
      return getBodyCellHeight();
    } else {
      return height;
    }
  }

  private long getRowIdByIndex(int rowIndex) {
    return getDataItem(rowIndex).getId();
  }

  private int getRowIndex(long rowId) {
    int row = BeeConst.UNDEF;
    for (int i = 0; i < getDataSize(); i++) {
      if (getRowData().get(i).getId() == rowId) {
        row = i;
        break;
      }
    }
    return row;
  }

  private int getRowWidth(String rowIdx) {
    int col = getColumnCount() - 1;
    if (col < 0) {
      return 0;
    }
    Element element = null;
    if (!BeeUtils.isEmpty(rowIdx)) {
      element = getCellElement(rowIdx, col);
    }

    if (element == null && hasHeaders()) {
      element = getHeaderCellElement(col);
    }
    if (element == null && hasFooters()) {
      element = getFooterCellElement(col);
    }
    if (element == null) {
      for (int i = 0; i < getDataSize(); i++) {
        element = getBodyCellElement(i, col);
        if (element != null) {
          break;
        }
      }
    }

    if (element == null) {
      return 0;
    } else {
      return element.getOffsetLeft() + element.getOffsetWidth();
    }
  }

  private int getTabIndex() {
    return tabIndex;
  }

  private boolean handleKey(int keyCode, boolean hasModifiers, int row, int col, Element cell) {
    if (resizeCell(keyCode, hasModifiers, row, col, cell)) {
      return true;
    }
    return handleKeyboardNavigation(keyCode, hasModifiers);
  }

  private boolean handleMouseMove(Event event, Element element, TargetType targetType,
      String eventRow, int eventCol) {
    if (getRowData().isEmpty()) {
      return false;
    }

    int x = event.getClientX();
    int y = event.getClientY();

    if (!isResizerVisible()) {
      int millis = getResizerShowSensitivityMillis();

      if (isResizeAllowed(targetType, eventRow, eventCol, ResizerMode.HORIZONTAL)) {
        int size = ResizerMode.HORIZONTAL.getHandlePx();
        int right = element.getAbsoluteRight();

        if (BeeUtils.betweenInclusive(right - x, 0, size / 2)) {
          if (millis > 0) {
            getResizerShowTimer().start(element, eventRow, eventCol, ResizerMode.HORIZONTAL,
                new Rectangle(right - size / 2, y, size, size), millis);
          } else {
            showColumnResizer(element, eventCol);
          }
          setResizerPosition(x);
          return true;
        }
      }

      if (isResizeAllowed(targetType, eventRow, eventCol, ResizerMode.VERTICAL)) {
        int size = ResizerMode.VERTICAL.getHandlePx();
        int bottom = element.getAbsoluteBottom();

        if (BeeUtils.betweenInclusive(bottom - y, 0, size / 2)) {
          if (millis > 0) {
            getResizerShowTimer().start(element, eventRow, eventCol, ResizerMode.VERTICAL,
                new Rectangle(x, bottom - size / 2, size, size), millis);
          } else {
            showRowResizer(element, eventRow);
          }
          setResizerPosition(y);
          return true;
        }
      }

    } else if (isResizing()) {
      int position = getResizerPosition();
      int millis = getResizerMoveSensitivityMillis();

      switch (getResizerStatus()) {
        case HORIZONTAL:
          if (checkResizerBounds(x)) {
            if (millis > 0) {
              getResizerMoveTimer().handleMove(x - position, millis);
            } else {
              resizeHorizontal(x - position);
            }
            setResizerPosition(x);
            return true;
          }
          break;
        case VERTICAL:
          if (checkResizerBounds(y)) {
            if (millis > 0) {
              getResizerMoveTimer().handleMove(y - position, millis);
            } else {
              resizeVertical(y - position);
            }
            setResizerPosition(y);
            return true;
          }
          break;
        default:
          Assert.untouchable();
      }

    } else {
      Element resizerContainer = getResizerContainer();
      if (resizerContainer != null
          && !Rectangle.createFromAbsoluteCoordinates(resizerContainer).contains(x, y)) {
        hideResizer();
      }
    }

    return false;
  }

  private boolean hasCalculatedOrActionColumns() {
    List<ColumnInfo> columns = getColumns();
    for (ColumnInfo columnInfo : columns) {
      if (columnInfo.isCalculated() || columnInfo.isActionColumn()) {
        return true;
      }
    }
    return false;
  }

  private boolean hasFooters() {
    List<ColumnInfo> columns = getColumns();
    for (ColumnInfo info : columns) {
      if (info.getFooter() != null) {
        return true;
      }
    }
    return false;
  }

  private boolean hasHeaders() {
    List<ColumnInfo> columns = getColumns();
    for (ColumnInfo info : columns) {
      if (info.getHeader() != null) {
        return true;
      }
    }
    return false;
  }

  private boolean hasKeyboardNext() {
    return getActiveRowIndex() + getPageStart() < getRowCount() - 1;
  }

  private boolean hasKeyboardPrev() {
    return getActiveRowIndex() > 0 || getPageStart() > 0;
  }

  private void hideResizer() {
    Element resizerContainer = getResizerContainer();
    if (resizerContainer != null) {
      StyleUtils.hideDisplay(resizerContainer);
      StyleUtils.hideDisplay(resizerHandleId);
      StyleUtils.hideDisplay(resizerBarId);
    }

    setResizerStatus(null);
    setResizerRow(null);
    setResizerCol(BeeConst.UNDEF);
    setResizerStartValue(BeeConst.UNDEF);
  }

  private int incrementZIndex() {
    int z = getZIndex() + 1;
    setZIndex(z);
    return z;
  }

  private boolean isCellActive(int row, int col) {
    return row >= 0 && col >= 0 && getActiveRowIndex() == row && getActiveColumnIndex() == col;
  }

  private boolean isCellResized(int row, int col) {
    return getResizedCells().contains(getRowIdByIndex(row), getColumnId(col));
  }

  private boolean isColumnWithinBounds(int col) {
    return col >= 0 && col < getColumnCount();
  }

  private boolean isResizeAllowed(TargetType target, String rowIdx, int col, ResizerMode resizer) {
    if (resizer.getHandlePx() <= 0) {
      return false;
    }
    if (target == TargetType.HEADER || target == TargetType.FOOTER) {
      return true;
    }
    if (target != TargetType.BODY) {
      return false;
    }

    int row = BeeUtils.toInt(rowIdx);
    if (isCellActive(row, col)) {
      return false;
    }

    switch (resizer) {
      case HORIZONTAL:
        return row == 0 && !hasHeaders() || row == getDataSize() - 1 && !hasFooters();
      case VERTICAL:
        return col == 0 || col == getColumnCount() - 1;
      default:
        Assert.untouchable();
        return false;
    }
  }

  private boolean isResizerOrResizerChild(Element element) {
    if (element == null) {
      return false;
    }
    String id = element.getId();
    if (BeeUtils.isEmpty(id)) {
      return false;
    }
    return BeeUtils.inListSame(id, resizerId, resizerHandleId, resizerBarId);
  }

  private boolean isResizerVisible() {
    return getResizerStatus() != null;
  }

  private boolean isResizing() {
    return isResizing;
  }

  private boolean isRowSelected(IsRow rowValue) {
    if (rowValue == null) {
      return false;
    } else {
      return isRowSelected(rowValue.getId());
    }
  }

  private boolean isRowWithinBounds(int row) {
    return row >= 0 && row < getDataSize();
  }

  private void keyboardEnd() {
    activateRow(getRowCount() - 1, NavigationOrigin.KEYBOARD);
  }

  private void keyboardHome() {
    activateRow(0, NavigationOrigin.KEYBOARD);
  }

  private void keyboardLeft() {
    int prevColumn = getActiveColumnIndex() - 1;
    if (prevColumn < 0) {
      if (hasKeyboardPrev()) {
        setActiveColumnIndex(getColumnCount() - 1);
        keyboardPrev();
      }
    } else {
      setActiveColumnIndex(prevColumn);
    }
  }

  private void keyboardNext() {
    activateRow(getPageStart() + getActiveRowIndex() + 1, NavigationOrigin.KEYBOARD);
  }

  private void keyboardNextPage() {
    if (getRowCount() <= 1 || getPageSize() <= 0 || getPageSize() >= getRowCount()
        || getPageStart() >= getRowCount() - getPageSize()) {
      activateRow(getRowCount() - 1, NavigationOrigin.KEYBOARD);
      return;
    }

    int absIndex = getPageStart() + getPageSize();
    int start = Math.min(absIndex, getRowCount() - getPageSize());
    if (absIndex > start) {
      if (absIndex >= getRowCount() - 1) {
        absIndex = getRowCount() - 1;
      } else {
        absIndex = start;
      }
    }
    activateRow(absIndex, start, NavigationOrigin.KEYBOARD);
  }

  private void keyboardPrev() {
    activateRow(getPageStart() + getActiveRowIndex() - 1, NavigationOrigin.KEYBOARD);
  }

  private void keyboardPrevPage() {
    if (getRowCount() <= 1 || getPageSize() <= 0 || getPageSize() >= getRowCount()
        || getPageStart() <= 0) {
      activateRow(0, NavigationOrigin.KEYBOARD);
      return;
    }

    int absIndex = getPageStart() - 1;
    int start = Math.max(0, getPageStart() - getPageSize());
    absIndex = Math.max(absIndex, start + getPageSize() - 1);

    activateRow(absIndex, start, NavigationOrigin.KEYBOARD);
  }

  private void keyboardRight() {
    int nextColumn = getActiveColumnIndex() + 1;
    if (nextColumn >= getColumnCount()) {
      if (hasKeyboardNext()) {
        setActiveColumnIndex(0);
        keyboardNext();
      }
    } else {
      setActiveColumnIndex(nextColumn);
    }
  }

  private void maybeUpdateColumnWidths() {
    for (int i = 0; i < getColumnCount(); i++) {
      ColumnInfo columnInfo = getColumnInfo(i);
      int rc = Math.min(getRowData().size(), columnInfo.getAutoFitRows());
      if (rc > 0) {
        estimateColumnWidth(i, getRowData(), 0, rc, false);
      }
    }
  }

  private void onActivateCell(boolean activate) {
    Element activeCell = getActiveCellElement();

    if (activeCell != null) {
      boolean resizable = getColumnInfo(getActiveColumnIndex()).isCellResizable();

      if (activate) {
        activeCell.getStyle().setZIndex(incrementZIndex());
        activeCell.addClassName(STYLE_ACTIVE_CELL);
        if (resizable) {
          activeCell.addClassName(StyleUtils.NAME_RESIZABLE);
        }

        activeCell.focus();

      } else {
        activeCell.removeClassName(STYLE_ACTIVE_CELL);
        if (resizable) {
          activeCell.removeClassName(StyleUtils.NAME_RESIZABLE);
        }
      }
    }
  }

  private void onActivateRow(boolean activate) {
    if (getActiveRowIndex() >= 0) {
      NodeList<Element> rowElements = getActiveRowElements();
      if (rowElements != null && rowElements.getLength() > 0) {
        if (activate) {
          StyleUtils.addClassName(rowElements, STYLE_ACTIVE_ROW);
        } else {
          StyleUtils.removeClassName(rowElements, STYLE_ACTIVE_ROW);
        }
      }
    }

    if (activate) {
      rowChangeScheduler.scheduleEvent();
    }
  }

  private void onSelectRow(int row, boolean select) {
    NodeList<Element> rowElements = getRowElements(row);
    if (rowElements != null && rowElements.getLength() > 0) {
      if (select) {
        StyleUtils.addClassName(rowElements, STYLE_SELECTED_ROW);
      } else {
        StyleUtils.removeClassName(rowElements, STYLE_SELECTED_ROW);
      }
    }
  }

  private void refreshCalculatedAndActionColumns(int rowIndex) {
    List<Integer> colIndexes = Lists.newArrayList();
    for (int col = 0; col < getColumnCount(); col++) {
      if (getColumnInfo(col).isCalculated() || getColumnInfo(col).isActionColumn()) {
        colIndexes.add(col);
      }
    }

    if (!colIndexes.isEmpty()) {
      refreshRow(rowIndex, colIndexes);
    }
  }

  private void refreshCell(int rowIndex, int colIndex) {
    refreshRow(rowIndex, Sets.newHashSet(colIndex));
  }

  private void refreshHeader(int col) {
    ColumnHeader header = getColumnInfo(col).getHeader();
    if (header == null) {
      return;
    }
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    CellContext context = new CellContext(0, col, null, this);
    header.render(context, builder);

    getHeaderCellElement(col).setInnerHTML(builder.toSafeHtml().asString());
  }

  private void refreshRow(int rowIndex) {
    refreshRow(rowIndex, null);
  }

  private void refreshRow(int rowIndex, Collection<Integer> colIndexes) {
    checkRowBounds(rowIndex);

    List<RenderInfo> renderList = renderBody(getRowData(), Sets.newHashSet(rowIndex), colIndexes);
    Assert.notEmpty(renderList);

    Element cellElement;
    for (RenderInfo renderInfo : renderList) {
      cellElement = getCellElement(renderInfo.getRowIdx(), renderInfo.getColIdx());
      Assert.notNull(cellElement);

      if (!BeeUtils.same(cellElement.getClassName(), renderInfo.getClasses())) {
        cellElement.setClassName(renderInfo.getClasses());
      }
      if (renderInfo.getStyles() != null) {
        String styles = renderInfo.getStyles().asString();
        if (!BeeUtils.same(StyleUtils.getCssText(cellElement), styles)) {
          StyleUtils.setCssText(cellElement, styles);
        }
      }
      cellElement.setInnerHTML(renderInfo.getContent().asString());
    }
  }

  private void render(boolean focus) {
    RenderMode mode = getEffectiveRenderMode();

    if (RenderMode.CONTENT.equals(mode)) {
      replaceContent(getRowData());
    } else {
      SafeHtmlBuilder sb = new SafeHtmlBuilder();
      if (getRowData().isEmpty()) {
        if (isEnabled()) {
          sb.append(template.emptiness(STYLE_EMPTY, BeeConst.STRING_EMPTY));
        }
      } else {
        renderData(sb, getRowData());
        renderResizer(sb);
      }
      replaceAllChildren(sb.toSafeHtml());
    }

    getRenderedRows().clear();
    for (IsRow row : getRowData()) {
      getRenderedRows().add(row.getId());
    }

    setZIndex(0);

    rowChangeScheduler.scheduleEvent();

    if (focus && isRowWithinBounds(getActiveRowIndex()) && getActiveColumnIndex() >= 0) {
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          Element cellElement = getActiveCellElement();
          if (cellElement != null) {
            cellElement.getStyle().setZIndex(incrementZIndex());
            cellElement.focus();
          }
        }
      });
    }
  }

  private List<RenderInfo> renderBody(List<IsRow> rows, Collection<Integer> rowIndexes,
      Collection<Integer> colIndexes) {
    int size = getDataSize();
    int start = getPageStart();
    int actRow = getActiveRowIndex();
    int actCol = getActiveColumnIndex();

    List<String> classes = Lists.newArrayList(STYLE_CELL, STYLE_BODY,
        getBodyComponent().getClassName());

    Edges padding = getBodyCellPadding();
    Edges borderWidth = getBodyBorderWidth();
    Edges margin = getBodyCellMargin();

    SafeStyles defaultPaddingStyle = StyleUtils.buildPadding(getCssValue(padding));
    SafeStyles defaultBorderWidthStyle = StyleUtils.buildBorderWidth(getCssValue(borderWidth));

    Edges[][] cellBorders = getBorders(rows, borderWidth, margin);
    boolean collapseBorders = cellBorders != null;

    SafeStylesBuilder stylesBuilder = new SafeStylesBuilder();
    getBodyComponent().buildSafeStyles(stylesBuilder);

    stylesBuilder.append(StyleUtils.buildMargin(getCssValue(margin)));
    if (!collapseBorders) {
      stylesBuilder.append(defaultPaddingStyle);
      stylesBuilder.append(defaultBorderWidthStyle);
    }
    SafeStyles defaultStyles = stylesBuilder.toSafeStyles();

    int defaultWidthIncr = getWidthIncrement(padding, borderWidth, margin);
    int defaultBorderWidthIncr = getWidthIncrement(borderWidth);

    int defaultHeightIncr = getHeightIncrement(padding, borderWidth, margin);
    int defaultBorderHeightIncr = getHeightIncrement(borderWidth);

    int top = getHeaderHeight();

    List<RenderInfo> result = Lists.newArrayList();

    for (int i = 0; i < size; i++) {
      IsRow rowValue = rows.get(i);
      Assert.notNull(rowValue);

      long valueId = rowValue.getId();
      int rowHeight = getRowHeightById(valueId);

      if (rowIndexes != null && !rowIndexes.contains(i)) {
        top += rowHeight + defaultHeightIncr;
        continue;
      }

      boolean isSelected = isRowSelected(rowValue);
      boolean isActive = i == actRow;

      List<String> rowClasses = Lists.newArrayList(classes);
      rowClasses.add(((i + start) % 2 == 1) ? STYLE_EVEN_ROW : STYLE_ODD_ROW);
      if (isActive) {
        rowClasses.add(STYLE_ACTIVE_ROW);
      }
      if (isSelected) {
        rowClasses.add(STYLE_SELECTED_ROW);
      }

      SafeStyles extraRowStyles = null;
      if (getRowStyles() != null) {
        StyleDescriptor dynRowStyle =
            getRowStyles().getStyleDescriptor(rowValue, i, BeeConst.UNDEF);
        if (dynRowStyle != null) {
          String dynRowClass = dynRowStyle.getClassName();
          if (!BeeUtils.isEmpty(dynRowClass)) {
            rowClasses.add(dynRowClass);
          }

          if (dynRowStyle.hasSafeStylesOrFont()) {
            SafeStylesBuilder extraRowstylesBuilder = new SafeStylesBuilder();
            dynRowStyle.buildSafeStyles(extraRowstylesBuilder);
            extraRowStyles = extraRowstylesBuilder.toSafeStyles();
          }
        }
      }

      int col = 0;
      int left = 0;

      String rowIdx = BeeUtils.toString(i);

      int cellWidth;
      int cellHeight;

      List<ColumnInfo> columns = getColumns();
      for (ColumnInfo columnInfo : columns) {
        AbstractColumn<?> column = columnInfo.getColumn();
        int columnWidth = columnInfo.getWidth();

        if (colIndexes == null || colIndexes.contains(col)) {
          List<String> cellClasses = Lists.newArrayList(rowClasses);
          BeeUtils.addNotEmpty(cellClasses, columnInfo.getClassName(ComponentType.BODY));

          cellClasses.add(STYLE_COLUMN_PREFIX + column.getStyleSuffix());
          cellClasses.addAll(column.getClasses());

          if (isActive && col == actCol) {
            cellClasses.add(STYLE_ACTIVE_CELL);
            if (columnInfo.isCellResizable()) {
              cellClasses.add(StyleUtils.NAME_RESIZABLE);
            }
          }

          SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
          CellContext context = new CellContext(i, col, rowValue, this);
          column.render(context, rowValue, cellBuilder);
          SafeHtml cellHtml = cellBuilder.toSafeHtml();

          SafeStylesBuilder extraStylesBuilder = new SafeStylesBuilder();
          if (extraRowStyles != null) {
            extraStylesBuilder.append(extraRowStyles);
          }
          columnInfo.buildSafeStyles(extraStylesBuilder, ComponentType.BODY);

          if (columnInfo.getDynStyles() != null) {
            StyleDescriptor dynColStyle = columnInfo.getDynStyles().getStyleDescriptor(rowValue,
                i, col, column.getValueType(), column.getString(context, rowValue));
            if (dynColStyle != null) {
              if (!BeeUtils.isEmpty(dynColStyle.getClassName())) {
                cellClasses.add(dynColStyle.getClassName());
              }
              dynColStyle.buildSafeStyles(extraStylesBuilder);
            }
          }

          if (collapseBorders) {
            Edges borders = cellBorders[i][col];
            if (borders == null) {
              extraStylesBuilder.append(defaultPaddingStyle);
              extraStylesBuilder.append(defaultBorderWidthStyle);
            } else {
              int widthIncr = defaultBorderWidthIncr - getWidthIncrement(borders);
              int heightIncr = defaultBorderHeightIncr - getHeightIncrement(borders);
              if (widthIncr != 0 || heightIncr != 0) {
                extraStylesBuilder.append(StyleUtils.buildPadding(getCssValue(incrementEdges(
                    padding, widthIncr, heightIncr))));
              } else {
                extraStylesBuilder.append(defaultPaddingStyle);
              }
              extraStylesBuilder.append(StyleUtils.buildBorderWidth(getCssValue(borders)));
            }
          }

          CellInfo cellInfo = getResizedCells().get(valueId, columnInfo.getColumnId());

          if (cellInfo == null) {
            cellWidth = columnWidth;
            cellHeight = rowHeight;

          } else {
            cellWidth = cellInfo.getWidth();
            cellHeight = cellInfo.getHeight();

            cellClasses.add(STYLE_RESIZED_CELL);
            if (cellWidth > columnWidth || cellHeight > rowHeight) {
              extraStylesBuilder.append(StyleUtils.buildZIndex(incrementZIndex()));
            }
          }

          result.add(renderCell(rowIdx, col, StyleUtils.buildClasses(cellClasses), left, top,
              cellWidth, cellHeight, defaultStyles, extraStylesBuilder.toSafeStyles(),
              column.getHorizontalAlignment(), cellHtml, true));
        }
        left += columnWidth + defaultWidthIncr;
        col++;
      }
      top += rowHeight + defaultHeightIncr;
    }
    return result;
  }

  private RenderInfo renderCell(String rowIdx, int col, String classes, int left, int top,
      int width, int height, SafeStyles styles, SafeStyles extraStyles,
      HorizontalAlignmentConstant hAlign, SafeHtml content, boolean focusable) {

    SafeStylesBuilder stylesBuilder = new SafeStylesBuilder();
    stylesBuilder.append(StyleUtils.PREFAB_POSITION_ABSOLUTE);

    if (styles != null) {
      stylesBuilder.append(styles);
    }
    if (extraStyles != null) {
      stylesBuilder.append(extraStyles);
    }
    if (hAlign != null) {
      stylesBuilder.append(StyleUtils.buildStyle(StyleUtils.CSS_TEXT_ALIGN,
          hAlign.getTextAlignString()));
    }

    stylesBuilder.append(StyleUtils.buildLeft(left));
    stylesBuilder.append(StyleUtils.buildTop(top));

    if (width > 0) {
      stylesBuilder.append(StyleUtils.buildWidth(width));
    }
    if (height > 0) {
      stylesBuilder.append(StyleUtils.buildHeight(height));
    }
    return new RenderInfo(rowIdx, col, classes, stylesBuilder.toSafeStyles(), focusable, content);
  }

  private void renderData(SafeHtmlBuilder sb, List<IsRow> rows) {
    renderHeaders(sb, true);

    List<RenderInfo> body = renderBody(rows, null, null);
    for (RenderInfo cell : body) {
      sb.append(cell.render());
    }

    renderHeaders(sb, false);
  }

  private void renderHeaders(SafeHtmlBuilder sb, boolean isHeader) {
    if (isHeader ? !hasHeaders() : !hasFooters()) {
      return;
    }
    int columnCount = getColumnCount();

    Component component = isHeader ? getHeaderComponent() : getFooterComponent();
    ComponentType componentType = isHeader ? ComponentType.HEADER : ComponentType.FOOTER;

    String classes = StyleUtils.buildClasses(STYLE_CELL,
        isHeader ? STYLE_HEADER : STYLE_FOOTER, component.getClassName());

    Edges padding = component.getPadding();
    Edges borderWidth = Edges.copyOf(component.getBorderWidth());
    Edges margin = component.getMargin();

    SafeStylesBuilder stylesBuilder = new SafeStylesBuilder();
    component.buildSafeStyles(stylesBuilder);

    stylesBuilder.append(StyleUtils.buildPadding(getCssValue(padding)));
    stylesBuilder.append(StyleUtils.buildMargin(getCssValue(margin)));

    SafeStyles firstColumnStyles = null;
    SafeStyles defaultColumnStyles = null;
    SafeStyles lastColumnStyles = null;

    int firstColumnWidthIncr = 0;
    int defaultColumnWidthIncr = 0;
    int lastColumnWidthIncr = 0;

    if (columnCount > 1 && borderWidth != null && !Edges.hasPositiveHorizontalValue(margin)) {
      int borderLeft = borderWidth.getIntLeft();
      int borderRight = borderWidth.getIntRight();

      if (borderLeft > 0 && borderRight > 0) {
        borderWidth.setRight(Math.max(borderLeft, borderRight));
        firstColumnStyles = StyleUtils.buildBorderWidth(getCssValue(borderWidth));
        firstColumnWidthIncr = getWidthIncrement(padding, borderWidth, margin);

        borderWidth.setLeft(0);
        lastColumnStyles = StyleUtils.buildBorderWidth(getCssValue(borderWidth));
        lastColumnWidthIncr = getWidthIncrement(padding, borderWidth, margin);

        borderWidth.setRight(borderRight);
        defaultColumnStyles = StyleUtils.buildBorderWidth(getCssValue(borderWidth));
        defaultColumnWidthIncr = getWidthIncrement(padding, borderWidth, margin);
      }
    }

    if (defaultColumnStyles == null) {
      stylesBuilder.append(StyleUtils.buildBorderWidth(getCssValue(borderWidth)));

      defaultColumnWidthIncr = getWidthIncrement(padding, borderWidth, margin);
      firstColumnWidthIncr = defaultColumnWidthIncr;
      lastColumnWidthIncr = defaultColumnWidthIncr;
    }
    SafeStyles styles = stylesBuilder.toSafeStyles();

    int top = isHeader ? 0 : getHeaderHeight() + getBodyHeight();
    int cellHeight = component.getCellHeight();
    int left = 0;

    int xIncr = getBodyCellWidthIncrement();
    int widthIncr;

    String rowIdx = isHeader ? HEADER_ROW : FOOTER_ROW;

    for (int i = 0; i < columnCount; i++) {
      ColumnInfo columnInfo = getColumnInfo(i);

      SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
      HorizontalAlignmentConstant hAlign = null;

      if (isHeader) {
        if (columnInfo.getHeader() != null) {
          CellContext context = new CellContext(0, i, null, this);
          columnInfo.getHeader().render(context, cellBuilder);
        }

      } else if (columnInfo.getFooter() != null) {
        CellContext context = new CellContext(0, i, null, this);
        columnInfo.getFooter().render(context, cellBuilder);
        hAlign = columnInfo.getFooter().getHorizontalAlignment();
      }

      int width = columnInfo.getWidth();
      widthIncr = (i == 0) ? firstColumnWidthIncr
          : (i == columnCount - 1) ? lastColumnWidthIncr : defaultColumnWidthIncr;

      String cellClasses = StyleUtils.buildClasses(classes, columnInfo.getClassName(componentType));

      SafeStylesBuilder extraStylesBuilder = new SafeStylesBuilder();
      columnInfo.buildSafeStyles(extraStylesBuilder, componentType);

      SafeStyles extraStyles = (i == 0) ? firstColumnStyles
          : (i == columnCount - 1) ? lastColumnStyles : defaultColumnStyles;
      if (extraStyles != null) {
        extraStylesBuilder.append(extraStyles);
      }

      SafeHtml contents = renderCell(rowIdx, i, cellClasses, left, top,
          width + xIncr - widthIncr, cellHeight, styles, extraStylesBuilder.toSafeStyles(), hAlign,
          cellBuilder.toSafeHtml(), false).render();
      sb.append(contents);

      left += width + xIncr;
    }
  }

  private void renderResizer(SafeHtmlBuilder sb) {
    sb.append(template.resizer(resizerId,
        template.resizerHandle(resizerHandleId), template.resizerBar(resizerBarId)));
  }

  private void replaceAllChildren(SafeHtml html) {
    getElement().setInnerHTML(html.asString());
  }

  private void replaceContent(List<IsRow> rows) {
    boolean checkSelection = false;
    if (!getSelectedRows().isEmpty()) {
      for (Long rowId : getRenderedRows()) {
        if (isRowSelected(rowId)) {
          checkSelection = true;
          break;
        }
      }

      if (!checkSelection) {
        for (IsRow row : rows) {
          if (isRowSelected(row)) {
            checkSelection = true;
            break;
          }
        }
      }
    }

    NodeList<Element> children = DomUtils.getChildren(getElement());
    for (int i = 0; i < children.getLength(); i++) {
      Element cell = children.getItem(i);

      String rx = DomUtils.getDataRow(cell);
      if (!isBodyRow(rx)) {
        continue;
      }
      int r = BeeUtils.toInt(rx);
      int c = BeeUtils.toInt(DomUtils.getDataColumn(cell));

      IsRow rowValue = rows.get(r);

      SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
      CellContext context = new CellContext(r, c, rowValue, this);
      getColumn(c).render(context, rowValue, cellBuilder);

      cell.setInnerHTML(cellBuilder.toSafeHtml().asString());

      if (checkSelection) {
        boolean was = isRowSelected(getRenderedRows().get(r));
        boolean is = isRowSelected(rowValue);

        if (was != is) {
          if (was) {
            cell.removeClassName(STYLE_SELECTED_ROW);
          } else {
            cell.addClassName(STYLE_SELECTED_ROW);
          }
        }
      }
    }
  }

  private boolean resizeCell(int keyCode, boolean hasModifiers, int row, int col, Element cell) {
    if (cell == null) {
      return false;
    }

    if (keyCode == KeyCodes.KEY_ESCAPE || hasModifiers && BeeUtils.inList(keyCode,
        EventUtils.KEY_INSERT, KeyCodes.KEY_DELETE, KeyCodes.KEY_DOWN, KeyCodes.KEY_LEFT,
        KeyCodes.KEY_RIGHT, KeyCodes.KEY_UP)) {
      int oldWidth = StyleUtils.getWidth(cell);
      int oldHeight = StyleUtils.getHeight(cell);
      if (oldWidth <= 0 || oldHeight <= 0) {
        return false;
      }

      int scrollWidth = cell.getScrollWidth() - (cell.getClientWidth() - oldWidth);
      int scrollHeight = cell.getScrollHeight() - (cell.getClientHeight() - oldHeight);

      int defWidth = getColumnWidth(col);
      int defHeight = getRowHeight(row);

      int newWidth = oldWidth;
      int newHeight = oldHeight;

      switch (keyCode) {
        case EventUtils.KEY_INSERT:
          if (scrollWidth > defWidth && scrollWidth > oldWidth
              || scrollHeight > defHeight && scrollHeight > oldHeight) {
            newWidth = Math.max(oldWidth, scrollWidth);
            newHeight = Math.max(oldHeight, scrollHeight);
          } else {
            newWidth += 2;
            newHeight += 2;
          }
          break;

        case KeyCodes.KEY_DELETE:
          newWidth--;
          newHeight--;
          break;

        case KeyCodes.KEY_ESCAPE:
          newWidth = getColumnWidth(col);
          newHeight = getRowHeight(row);
          break;

        case KeyCodes.KEY_DOWN:
          if (scrollHeight > defHeight && scrollHeight > oldHeight) {
            newHeight = scrollHeight;
          } else {
            newHeight += 2;
          }
          break;

        case KeyCodes.KEY_LEFT:
          newWidth--;
          break;

        case KeyCodes.KEY_RIGHT:
          if (scrollWidth > defWidth && scrollWidth > oldWidth) {
            newWidth = scrollWidth;
          } else {
            newWidth += 2;
          }
          break;

        case KeyCodes.KEY_UP:
          newHeight--;
          break;
      }

      newWidth = getColumnInfo(col).clampWidth(newWidth);
      newHeight = limitCellHeight(newHeight, getBodyComponent());
      if (newWidth <= 0) {
        newWidth = oldWidth;
      }
      if (newHeight <= 0) {
        newHeight = oldHeight;
      }

      if (newWidth == oldWidth && newHeight == oldHeight) {
        return false;
      }
      StyleUtils.setWidth(cell, newWidth);
      StyleUtils.setHeight(cell, newHeight);

      checkCellSize(cell, row, col);
      return true;
    }
    return false;
  }

  private int resizeColumnWidth(int col, int oldWidth, int incr) {
    if (incr == 0 || oldWidth <= 0) {
      return BeeConst.UNDEF;
    }

    ColumnInfo columnInfo = getColumnInfo(col);

    int newWidth = columnInfo.clampWidth(oldWidth + incr);
    if (newWidth <= 0 || !BeeUtils.sameSign(newWidth - oldWidth, incr)) {
      return BeeConst.UNDEF;
    }

    columnInfo.setResizedWidth(newWidth);

    NodeList<Element> nodes = getColumnElements(col);

    String cssWidth = null;
    for (int i = 0; i < nodes.getLength(); i++) {
      Element cellElement = nodes.getItem(i);
      String rowIdx = DomUtils.getDataRow(cellElement);

      if (isBodyRow(rowIdx)) {
        if (isCellResized(BeeUtils.toInt(rowIdx), col)) {
          continue;
        }
        if (cssWidth == null) {
          cssWidth = StyleUtils.toCssLength(StyleUtils.getWidth(cellElement)
              + newWidth - oldWidth, CssUnit.PX);
        }
        cellElement.getStyle().setProperty(StyleUtils.STYLE_WIDTH, cssWidth);
      } else {
        DomUtils.resizeHorizontalBy(cellElement, newWidth - oldWidth);
      }
    }

    refreshHeader(col);

    if (col < getColumnCount() - 1) {
      for (int i = col + 1; i < getColumnCount(); i++) {
        nodes = getColumnElements(i);
        if (nodes == null || nodes.getLength() <= 0) {
          continue;
        }
        int left = StyleUtils.getLeft(nodes.getItem(0));
        StyleUtils.setStylePropertyPx(nodes, StyleUtils.STYLE_LEFT, left + newWidth - oldWidth);
      }
    }
    return newWidth;
  }

  private void resizeHorizontal(int by) {
    if (by == 0) {
      return;
    }

    int col = getResizerCol();
    int oldWidth = getColumnWidth(col);
    int newWidth = resizeColumnWidth(col, oldWidth, by);
    if (BeeConst.isUndef(newWidth)) {
      return;
    }

    int incr = newWidth - oldWidth;
    if (incr != 0) {
      DomUtils.moveHorizontalBy(resizerId, incr);
    }
  }

  private void resizeRowElements(int row, NodeList<Element> nodes, int dh) {
    if (getResizedCells().containsRow(getRowIdByIndex(row))) {
      for (int i = 0; i < nodes.getLength(); i++) {
        Element cellElement = nodes.getItem(i);
        int col = BeeUtils.toInt(DomUtils.getDataColumn(cellElement));
        if (isCellResized(row, col)) {
          continue;
        }
        DomUtils.resizeVerticalBy(cellElement, dh);
      }
    } else {
      incrementHeight(nodes, dh);
    }
  }

  private int resizeRowHeight(String rowIdx, int oldHeight, int incr, Modifiers modifiers) {
    if (oldHeight <= 0 || incr == 0) {
      return BeeConst.UNDEF;
    }
    int newHeight = limitCellHeight(oldHeight + incr, getComponent(rowIdx));
    if (newHeight <= 0 || !BeeUtils.sameSign(newHeight - oldHeight, incr)) {
      return BeeConst.UNDEF;
    }

    int dh = newHeight - oldHeight;
    int rc = getDataSize();
    NodeList<Element> nodes;

    if (isBodyRow(rowIdx) && Modifiers.isNotEmpty(modifiers)) {
      int dt = 0;
      int row = BeeUtils.toInt(rowIdx);

      int start = 0;
      int end = rc;
      boolean updateDefault = false;

      if (modifiers.isCtrlKey()) {
        updateDefault = true;
      } else if (modifiers.isShiftKey() && row < rc - 1 || row == 0) {
        start = row;
      } else if (modifiers.isAltKey() && row > 0 || row == rc - 1) {
        end = row + 1;
      } else {
        start = row;
        end = row + 1;
      }

      for (int i = start; i < rc; i++) {
        nodes = getRowElements(i);
        if (dt != 0) {
          incrementTop(nodes, dt);
        }
        if (i >= end) {
          continue;
        }

        int rh = getRowHeight(i);
        resizeRowElements(i, nodes, newHeight - rh);
        dt += newHeight - rh;
        if (!updateDefault) {
          setRowHeight(i, newHeight);
        }
      }

      if (updateDefault) {
        setBodyCellHeight(newHeight);
        getResizedRows().clear();
      }

      nodes = getFooterElements();
      if (nodes != null) {
        incrementTop(nodes, dt);
      }

    } else if (isHeaderRow(rowIdx)) {
      incrementHeight(getHeaderElements(), dh);
      for (int i = 0; i < rc; i++) {
        incrementTop(getRowElements(i), dh);
      }
      nodes = getFooterElements();
      if (nodes != null) {
        incrementTop(nodes, dh);
      }
      setHeaderCellHeight(newHeight);

    } else if (isBodyRow(rowIdx)) {
      int row = BeeUtils.toInt(rowIdx);
      resizeRowElements(row, getRowElements(row), dh);
      if (row < rc - 1) {
        for (int i = row + 1; i < rc; i++) {
          incrementTop(getRowElements(i), dh);
        }
      }
      nodes = getFooterElements();
      if (nodes != null) {
        incrementTop(nodes, dh);
      }
      setRowHeight(row, newHeight);

    } else if (isFooterRow(rowIdx)) {
      incrementHeight(getFooterElements(), dh);
      setFooterCellHeight(newHeight);
    }

    return newHeight;
  }

  private void resizeVertical(int by) {
    if (by == 0) {
      return;
    }

    String rowIdx = getResizerRow();
    Element cellElement = getCellElement(rowIdx, 0);
    int oldHeight = getRowHeight(rowIdx);
    int oldTop = cellElement.getOffsetTop();

    int newHeight = resizeRowHeight(rowIdx, oldHeight, by, getResizerModifiers());
    if (BeeConst.isUndef(newHeight)) {
      return;
    }

    int newTop = cellElement.getOffsetTop();
    int incr = newTop - oldTop + newHeight - oldHeight;
    if (incr != 0) {
      DomUtils.moveVerticalBy(resizerId, incr);
    }
  }

  private void selectRange(int rowIndex, IsRow rowValue) {
    if (rowValue == null) {
      return;
    }
    long rowId = rowValue.getId();

    if (isRowSelected(rowId)) {
      for (int i = 0; i < getDataSize(); i++) {
        IsRow row = getDataItem(i);
        if (isRowSelected(row)) {
          selectRow(i, row);
        }
      }
      getSelectedRows().clear();
      fireSelectionCountChange();

    } else {
      int lastSelectedRow = BeeConst.UNDEF;
      if (!getSelectedRows().isEmpty()) {
        List<Long> selectedIds = Lists.newArrayList(getSelectedRows().keySet());
        int maxIndex = -1;
        for (int i = 0; i < getDataSize(); i++) {
          if (i == rowIndex) {
            continue;
          }
          int index = selectedIds.indexOf(getRowIdByIndex(i));
          if (index > maxIndex) {
            maxIndex = index;
            lastSelectedRow = i;
          }
        }
      }

      if (lastSelectedRow == BeeConst.UNDEF) {
        selectRow(rowIndex, rowValue);
      } else if (lastSelectedRow < rowIndex) {
        for (int i = lastSelectedRow + 1; i <= rowIndex; i++) {
          IsRow row = getDataItem(i);
          if (!isRowSelected(row)) {
            selectRow(i, row);
          }
        }
      } else {
        for (int i = rowIndex; i < lastSelectedRow; i++) {
          IsRow row = getDataItem(i);
          if (!isRowSelected(row)) {
            selectRow(i, row);
          }
        }
      }
    }
  }

  private void selectRow(int rowIndex, IsRow rowValue) {
    if (rowValue == null) {
      return;
    }
    long rowId = rowValue.getId();
    boolean wasSelected = isRowSelected(rowId);

    if (wasSelected) {
      getSelectedRows().remove(rowId);
    } else {
      getSelectedRows().put(rowId, new RowInfo(rowValue, isRowEditable(rowValue)));
    }

    for (int col = 0; col < getColumnCount(); col++) {
      if (getColumnInfo(col).isSelection()) {
        AbstractColumn<?> column = getColumnInfo(col).getColumn();
        if (column instanceof SelectionColumn) {
          ((SelectionColumn) column).update(getCellElement(rowIndex, col), !wasSelected);
        } else {
          refreshCell(rowIndex, col);
        }
      }
    }

    onSelectRow(rowIndex, !wasSelected);
    fireSelectionCountChange();
  }

  private void setActiveColumnIndex(int activeColumnIndex) {
    if (this.activeColumnIndex == activeColumnIndex) {
      return;
    }
    onActivateCell(false);

    this.activeColumnIndex = activeColumnIndex;

    onActivateCell(true);
  }

  private void setActiveRowIndex(int activeRowIndex) {
    if (this.activeRowIndex == activeRowIndex) {
      return;
    }
    onActivateCell(false);
    onActivateRow(false);

    this.activeRowIndex = activeRowIndex;

    onActivateRow(true);
    onActivateCell(true);
  }

  private void setColumnWidth(String columnId, double width, CssUnit unit, int containerSize) {
    setColumnWidth(columnId, Rulers.getIntPixels(width, unit, containerSize));
  }

  private void setRenderMode(RenderMode renderMode) {
    this.renderMode = renderMode;
  }

  private void setResizerBounds(int min, int max) {
    setResizerPositionMin(min);
    setResizerPositionMax(max);
  }

  private void setResizerCol(int resizerCol) {
    this.resizerCol = resizerCol;
  }

  private void setResizerModifiers(Modifiers resizerModifiers) {
    this.resizerModifiers = resizerModifiers;
  }

  private void setResizerPosition(int resizerPosition) {
    this.resizerPosition = resizerPosition;
  }

  private void setResizerPositionMax(int resizerPositionMax) {
    this.resizerPositionMax = resizerPositionMax;
  }

  private void setResizerPositionMin(int resizerPositionMin) {
    this.resizerPositionMin = resizerPositionMin;
  }

  private void setResizerRow(String resizerRow) {
    this.resizerRow = resizerRow;
  }

  private void setResizerStartValue(int resizerStartValue) {
    this.resizerStartValue = resizerStartValue;
  }

  private void setResizerStatus(ResizerMode resizerStatus) {
    this.resizerStatus = resizerStatus;
  }

  private void setResizing(boolean resizing) {
    this.isResizing = resizing;
  }

  private void setRowHeight(int row, int height) {
    Assert.isPositive(height);
    long id = getRowIdByIndex(row);
    if (height == getBodyCellHeight()) {
      getResizedRows().remove(id);
    } else {
      getResizedRows().put(id, height);
    }
  }

  private void setZIndex(int zInd) {
    this.zIndex = zInd;
    Stacking.ensureLevel(zInd);
  }

  private boolean showColumnResizer(Element cellElement, int col) {
    int x = cellElement.getOffsetLeft() + cellElement.getOffsetWidth();
    int y = cellElement.getOffsetTop();
    int h = cellElement.getOffsetHeight();

    int handleWidth = ResizerMode.HORIZONTAL.getHandlePx();
    int barWidth = ResizerMode.HORIZONTAL.getBarPx();
    int width = Math.max(handleWidth, barWidth);
    int left = Math.max(x - width / 2, 0);

    int top = (barWidth > 0) ? 0 : y;
    int height = (barWidth > 0) ? getChildrenHeight() : h;

    Element resizerElement = getResizerContainer();
    if (resizerElement == null) {
      return false;
    }

    StyleUtils.setRectangle(resizerElement, left, top, width, height);
    StyleUtils.setZIndex(resizerElement, incrementZIndex());
    resizerElement.setClassName(StyleUtils.buildClasses(STYLE_RESIZER, STYLE_RESIZER_HORIZONTAL));

    if (barWidth > 0) {
      Element barElement = getResizerBar();
      StyleUtils.setRectangle(barElement, (width - barWidth) / 2, 0, barWidth, height);
      barElement.setClassName(StyleUtils.buildClasses(STYLE_RESIZER_BAR,
          STYLE_RESIZER_BAR_HORIZONTAL));
      if (handleWidth > 0) {
        StyleUtils.hideDisplay(barElement);
      } else {
        StyleUtils.unhideDisplay(barElement);
      }
    }

    if (handleWidth > 0) {
      Element handleElement = getResizerHandle();
      StyleUtils.setRectangle(handleElement, (width - handleWidth) / 2, y, handleWidth, h);
      handleElement.setClassName(StyleUtils.buildClasses(STYLE_RESIZER_HANDLE,
          STYLE_RESIZER_HANDLE_HORIZONTAL));
      StyleUtils.unhideDisplay(handleElement);
    }

    int absLeft = cellElement.getAbsoluteLeft();
    int cellWidth = cellElement.getOffsetWidth();
    int min = absLeft + Math.min(getColumnInfo(col).getLowerWidthBound(), cellWidth);
    int max = absLeft + Math.max(getColumnInfo(col).getUpperWidthBound(), cellWidth);
    setResizerBounds(min, max);

    StyleUtils.unhideDisplay(resizerElement);
    setResizerStatus(ResizerMode.HORIZONTAL);
    setResizerCol(col);
    setResizerStartValue(getColumnWidth(col));

    return true;
  }

  private boolean showRowResizer(Element cellElement, String rowIdx) {
    int x = cellElement.getOffsetLeft();
    int y = cellElement.getOffsetTop() + cellElement.getOffsetHeight();
    int w = cellElement.getOffsetWidth();

    int handleHeight = ResizerMode.VERTICAL.getHandlePx();
    int barHeight = ResizerMode.VERTICAL.getBarPx();
    int height = Math.max(handleHeight, barHeight);
    int top = Math.max(y - height / 2, 0);

    int left = (barHeight > 0) ? 0 : x;
    int width = (barHeight > 0) ? getRowWidth(rowIdx) : w;

    Element resizerElement = getResizerContainer();
    if (resizerElement == null) {
      return false;
    }

    StyleUtils.setRectangle(resizerElement, left, top, width, height);
    StyleUtils.setZIndex(resizerElement, incrementZIndex());
    resizerElement.setClassName(StyleUtils.buildClasses(STYLE_RESIZER, STYLE_RESIZER_VERTICAL));

    if (barHeight > 0) {
      Element barElement = getResizerBar();
      StyleUtils.setRectangle(barElement, 0, (height - barHeight) / 2, width, barHeight);
      barElement.setClassName(StyleUtils.buildClasses(STYLE_RESIZER_BAR,
          STYLE_RESIZER_BAR_VERTICAL));
      if (handleHeight > 0) {
        StyleUtils.hideDisplay(barElement);
      } else {
        StyleUtils.unhideDisplay(barElement);
      }
    }

    if (handleHeight > 0) {
      Element handleElement = getResizerHandle();
      StyleUtils.setRectangle(handleElement, x, (height - handleHeight) / 2, w, handleHeight);
      handleElement.setClassName(StyleUtils.buildClasses(STYLE_RESIZER_HANDLE,
          STYLE_RESIZER_HANDLE_VERTICAL));
      StyleUtils.unhideDisplay(handleElement);
    }

    int absTop = cellElement.getAbsoluteTop();
    int cellHeight = cellElement.getOffsetHeight();

    Component component = getComponent(rowIdx);
    if (component != null) {
      int min = absTop + Math.min(Math.max(component.getMinHeight(), 0), cellHeight);
      int max = absTop + Math.max(component.getMaxHeight(), cellHeight);
      setResizerBounds(min, max);
    }

    StyleUtils.unhideDisplay(resizerElement);
    setResizerStatus(ResizerMode.VERTICAL);
    setResizerRow(rowIdx);
    setResizerStartValue((component == null) ? BeeConst.UNDEF : component.getCellHeight());

    return true;
  }

  private void startEditing(IsRow rowValue, int col, Element cellElement, int charCode) {
    fireEvent(new EditStartEvent(rowValue, getColumnId(col), cellElement, charCode,
        isReadOnly() || getColumnInfo(col).isColReadOnly()));
  }

  private void startResizing(Event event) {
    setResizerModifiers(new Modifiers(event));

    if (getResizerStatus().getBarPx() > 0 && getResizerStatus().getHandlePx() > 0) {
      StyleUtils.unhideDisplay(resizerBarId);
    }

    getResizerMoveTimer().reset();
    setResizing(true);
  }

  private void stopResizing() {
    getResizerMoveTimer().stop();
    setResizing(false);

    if (getResizerStatus() == ResizerMode.HORIZONTAL) {
      int oldWidth = getResizerStartValue();
      int newWidth = getColumnWidth(getResizerCol());

      if (oldWidth >= 0 && newWidth > 0 && oldWidth != newWidth) {
        SettingsChangeEvent.fireWidth(this, getColumnId(getResizerCol()), newWidth);
      }

    } else if (getResizerStatus() == ResizerMode.VERTICAL) {
      Component component = getComponent(getResizerRow());

      int oldHeight = getResizerStartValue();
      int newHeight = (component == null) ? BeeConst.UNDEF : component.getCellHeight();

      if (oldHeight >= 0 && newHeight > 0 && oldHeight != newHeight) {
        SettingsChangeEvent.fireHeight(this, component.type, newHeight);
      }
    }

    hideResizer();
    setResizerModifiers(null);
  }

  private void updateCellContent(int rowIndex, int col) {
    IsRow rowValue = getDataItem(rowIndex);
    Assert.notNull(rowValue);
    AbstractColumn<?> column = getColumn(col);

    SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
    CellContext context = new CellContext(rowIndex, col, rowValue, this);
    column.render(context, rowValue, cellBuilder);
    SafeHtml cellHtml = cellBuilder.toSafeHtml();

    Element cellElement = getCellElement(rowIndex, col);
    Assert.notNull(cellElement, "cell not found: row " + rowIndex + " col " + col);
    cellElement.setInnerHTML(cellHtml.asString());
  }

  private void updateOrder(String columnId, boolean hasModifiers) {
    Assert.notEmpty(columnId);
    ColumnInfo columnInfo = getColumnInfo(columnId);
    Assert.notNull(columnInfo);

    List<String> sources = columnInfo.getSortBy();
    if (BeeUtils.isEmpty(sources)) {
      return;
    }

    Order ord = getSortOrder();
    int size = ord.getSize();

    if (size <= 0) {
      ord.add(columnId, sources, true);
      return;
    }

    int index = ord.getIndex(columnId);
    if (BeeConst.isUndef(index)) {
      if (!hasModifiers) {
        ord.clear();
      }
      ord.add(columnId, sources, true);
      return;
    }

    boolean asc = ord.isAscending(columnId);

    if (hasModifiers) {
      if (size == 1) {
        ord.setAscending(columnId, !asc);
      } else if (index == size - 1) {
        if (asc) {
          ord.setAscending(columnId, !asc);
        } else {
          ord.remove(columnId);
        }
      } else {
        ord.remove(columnId);
        ord.add(columnId, sources, true);
      }

    } else if (size > 1) {
      ord.clear();
      ord.add(columnId, sources, true);
    } else if (asc) {
      ord.setAscending(columnId, !asc);
    } else {
      ord.clear();
    }
  }

  private boolean updatePageSize() {
    int oldPageSize = getPageSize();
    if (oldPageSize > 0) {
      int newPageSize = estimatePageSize();

      if (newPageSize > 0 && newPageSize != oldPageSize) {
        int rc = getRowCount();
        boolean fire = (rc > 0) && (oldPageSize < rc || newPageSize < rc);

        if (getPageStart() + newPageSize > rc) {
          int start = Math.max(rc - newPageSize, 0);
          if (start != getPageStart()) {
            setPageStart(start, false, false, NavigationOrigin.SYSTEM);
            fire = rc > 0;
          }
        }

        setPageSize(newPageSize, true);
        if (fire) {
          fireDataRequest(NavigationOrigin.SYSTEM);
        }
        return true;
      }
    }
    return false;
  }
}
