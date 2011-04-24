package com.butent.bee.client.view.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.IconCellDecorator;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.TableLayout;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableColElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rulers;
import com.butent.bee.client.presenter.DataPresenter;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CellGrid extends AbstractHasData<IsRow> implements HasId {

  public interface Resources extends ClientBundle {
    @Source("CellGrid.css")
    Style cellGridStyle();
  }

  @ImportedWithPrefix("bee-CellGrid")
  public interface Style extends CssResource {

    int bodyBorderWidth();

    String cellGridCell();

    String cellGridEvenRow();

    String cellGridEvenRowCell();

    String cellGridFirstColumn();

    String cellGridFirstColumnFooter();

    String cellGridFirstColumnHeader();

    String cellGridFooter();

    String cellGridHeader();

    String cellGridHoveredRow();

    String cellGridHoveredRowCell();

    String cellGridKeyboardSelectedCell();

    String cellGridKeyboardSelectedRow();

    String cellGridKeyboardSelectedRowCell();

    String cellGridLastColumn();

    String cellGridLastColumnFooter();

    String cellGridLastColumnHeader();

    String cellGridLoading();

    String cellGridOddRow();

    String cellGridOddRowCell();

    String cellGridSelectedRow();

    String cellGridSelectedRowCell();

    String cellGridSortableHeader();

    String cellGridSortedHeaderAscending();

    String cellGridSortedHeaderDescending();

    String cellGridWidget();

    int footerBorderWidth();

    int headerBorderWidth();
  }

  interface Template extends SafeHtmlTemplates {
    @Template("<div style=\"outline:none;\">{0}</div>")
    SafeHtml div(SafeHtml contents);

    @Template("<div style=\"outline:none; height:{0}px;\">{1}</div>")
    SafeHtml divFixedHeight(int height, SafeHtml contents);

    @Template("<div style=\"outline:none; width:{0}px; height:{1}px;\">{2}</div>")
    SafeHtml divFixedSize(int width, int height, SafeHtml contents);

    @Template("<div style=\"outline:none; width:{0}px;\">{1}</div>")
    SafeHtml divFixedWidth(int width, SafeHtml contents);
    
    @Template("<div style=\"outline:none;\" tabindex=\"{0}\">{1}</div>")
    SafeHtml divFocusable(int tabIndex, SafeHtml contents);

    @Template("<div style=\"outline:none; height:{0}px;\" tabindex=\"{1}\">{2}</div>")
    SafeHtml divFocusableFixedHeight(int height, int tabIndex, SafeHtml contents);

    @Template("<div style=\"outline:none; width:{0}px; height:{1}px;\" tabindex=\"{2}\">{3}</div>")
    SafeHtml divFocusableFixedSize(int width, int height, int tabIndex, SafeHtml contents);

    @Template("<div style=\"outline:none; width:{0}px;\" tabindex=\"{1}\">{2}</div>")
    SafeHtml divFocusableFixedWidth(int width, int tabIndex, SafeHtml contents);
    
    @Template("<table><tbody>{0}</tbody></table>")
    SafeHtml tbody(SafeHtml rowHtml);

    @Template("<td class=\"{0}\" style=\"padding:{1};\">{2}</td>")
    SafeHtml td(String classes, String padding, SafeHtml contents);

    @Template("<td class=\"{0}\" align=\"{1}\" valign=\"{2}\" style=\"padding:{3};\">{4}</td>")
    SafeHtml tdBothAlign(String classes, String hAlign, String vAlign, String padding,
        SafeHtml contents);

    @Template("<td class=\"{0}\" align=\"{1}\" style=\"padding:{2};\">{3}</td>")
    SafeHtml tdHorizontalAlign(String classes, String hAlign, String padding, SafeHtml contents);

    @Template("<td class=\"{0}\" valign=\"{1}\" style=\"padding:{2};\">{3}</td>")
    SafeHtml tdVerticalAlign(String classes, String vAlign, String padding, SafeHtml contents);

    @Template("<table><tfoot>{0}</tfoot></table>")
    SafeHtml tfoot(SafeHtml rowHtml);

    @Template("<th colspan=\"{0}\" class=\"{1}\" style=\"padding:{2};\">{3}</th>")
    SafeHtml th(int colspan, String classes, String padding, SafeHtml contents);

    @Template("<table><thead>{0}</thead></table>")
    SafeHtml thead(SafeHtml rowHtml);

    @Template("<tr onclick=\"\" class=\"{0}\">{1}</tr>")
    SafeHtml tr(String classes, SafeHtml contents);
  }

  private static class Impl {

    private final com.google.gwt.user.client.Element tmpElem =
        Document.get().createDivElement().cast();

    protected TableSectionElement convertToSectionElement(CellGrid table,
        String sectionTag, SafeHtml rowHtml) {
      DOM.setEventListener(tmpElem, table);

      sectionTag = sectionTag.toLowerCase();
      if ("tbody".equals(sectionTag)) {
        tmpElem.setInnerHTML(template.tbody(rowHtml).asString());
      } else if ("thead".equals(sectionTag)) {
        tmpElem.setInnerHTML(template.thead(rowHtml).asString());
      } else if ("tfoot".equals(sectionTag)) {
        tmpElem.setInnerHTML(template.tfoot(rowHtml).asString());
      } else {
        throw new IllegalArgumentException("Invalid table section tag: " + sectionTag);
      }
      TableElement tableElem = tmpElem.getFirstChildElement().cast();

      DOM.setEventListener(tmpElem, null);

      if ("tbody".equals(sectionTag)) {
        return tableElem.getTBodies().getItem(0);
      } else if ("thead".equals(sectionTag)) {
        return tableElem.getTHead();
      } else if ("tfoot".equals(sectionTag)) {
        return tableElem.getTFoot();
      } else {
        throw new IllegalArgumentException("Invalid table section tag: " + sectionTag);
      }
    }

    protected void replaceAllRows(CellGrid table, TableSectionElement section, SafeHtml html) {
      if (!table.isAttached()) {
        DOM.setEventListener(table.getElement(), table);
      }

      section.setInnerHTML(html.asString());

      if (!table.isAttached()) {
        DOM.setEventListener(table.getElement(), null);
      }
    }
  }

  @SuppressWarnings("unused")
  private static class ImplTrident extends Impl {

    @Override
    protected void replaceAllRows(CellGrid table, TableSectionElement section, SafeHtml html) {
      Element child = section.getFirstChildElement();
      while (child != null) {
        Element next = child.getNextSiblingElement();
        section.removeChild(child);
        child = next;
      }

      TableSectionElement newSection = convertToSectionElement(table, section.getTagName(), html);
      child = newSection.getFirstChildElement();
      while (child != null) {
        Element next = child.getNextSiblingElement();
        section.appendChild(child);
        child = next;
      }
    }
  }

  private static final int UNDEF = -1;

  public static int defaultHeaderCellHeight = UNDEF;
  public static int defaultBodyCellHeight = UNDEF;
  public static int defaultFooterCellHeight = UNDEF;

  public static int defaultMinCellWidth = 1;
  public static int defaultMaxCellWidth = 1024;
  public static int defaultMinCellHeight = 1;

  public static int defaultMaxCellHeight = 256;
  public static Edges defaultHeaderCellPadding = new Edges(3.0, 5.0);
  public static Edges defaultBodyCellPadding = new Edges(2.0, 5.0);
  public static Edges defaultFooterCellPadding = new Edges(2.0, 2.0);
  
  private static final int DEFAULT_PAGESIZE = 15;

  private static Resources DEFAULT_RESOURCES = null;
  private static Style DEFAULT_STYLE = null;

  private static Impl TABLE_IMPL = null;

  private static Template template = null;

  private static Widget createDefaultLoadingIndicator() {
    return new BeeImage(Global.getImages().loading());
  }
  private static Resources getDefaultResources() {
    if (DEFAULT_RESOURCES == null) {
      DEFAULT_RESOURCES = GWT.create(Resources.class);
    }
    return DEFAULT_RESOURCES;
  }
  private static Style getDefaultStyle() {
    if (DEFAULT_STYLE == null) {
      DEFAULT_STYLE = getDefaultResources().cellGridStyle();
      DEFAULT_STYLE.ensureInjected();
    }
    return DEFAULT_STYLE;
  }

  private int headerCellHeight = defaultHeaderCellHeight;
  private int bodyCellHeight = defaultBodyCellHeight;
  private int footerCellHeight = defaultFooterCellHeight;

  private int minCellWidth = defaultMinCellWidth;
  private int maxCellWidth = defaultMaxCellWidth;
  private int minCellHeight = defaultMinCellHeight;
  private int maxCellHeight = defaultMaxCellHeight;

  private Edges headerCellPadding = defaultHeaderCellPadding;
  private Edges bodyCellPadding = defaultBodyCellPadding;
  private Edges footerCellPadding = defaultFooterCellPadding;

  private boolean cellIsEditing;

  private final List<Column<IsRow, ?>> columns = new ArrayList<Column<IsRow, ?>>();
  private final Map<Column<IsRow, ?>, Integer> columnWidths =
      new HashMap<Column<IsRow, ?>, Integer>();

  private boolean dependsOnSelection;

  private final SimplePanel emptyTableWidgetContainer = new SimplePanel();
  private final List<Header<?>> footers = new ArrayList<Header<?>>();

  private boolean handlesSelection;

  private final List<Header<?>> headers = new ArrayList<Header<?>>();

  private TableRowElement hoveringRow;

  private boolean isInteractive;

  private int keyboardSelectedColumn = 0;
  private final SimplePanel loadingIndicatorContainer = new SimplePanel();

  private final DeckPanel messagesPanel = new DeckPanel();

  private RowStyles<IsRow> rowStyles;
  private IconCellDecorator<SafeHtml> sortAscDecorator;
  private IconCellDecorator<SafeHtml> sortDescDecorator;
  private final Style style;

  private final TableElement table;
  private final TableColElement colgroup;
  private final TableSectionElement tbody;
  private final TableSectionElement tbodyLoading;
  private final TableCellElement tbodyLoadingCell;
  private final TableSectionElement tfoot;
  private final TableSectionElement thead;

  private final ColumnSortList sortList;
  private boolean updatingSortList;

  public CellGrid() {
    this(DEFAULT_PAGESIZE);
  }

  public CellGrid(final int pageSize) {
    this(pageSize, null, null);
  }

  public CellGrid(final int pageSize, ProvidesKey<IsRow> keyProvider) {
    this(pageSize, null, keyProvider);
  }

  public CellGrid(final int pageSize, Style style) {
    this(pageSize, style, null);
  }

  public CellGrid(final int pageSize, Style style, ProvidesKey<IsRow> keyProvider) {
    super(Document.get().createTableElement(), pageSize, keyProvider);

    if (TABLE_IMPL == null) {
      TABLE_IMPL = GWT.create(Impl.class);
    }
    if (template == null) {
      template = GWT.create(Template.class);
    }

    if (style == null) {
      this.style = getDefaultStyle();
    } else {
      this.style = style;
      this.style.ensureInjected();
    }

    sortList = new ColumnSortList(new ColumnSortList.Delegate() {
      public void onModification() {
        if (!updatingSortList) {
          createHeaders(false);
        }
      }
    });

    table = getElement().cast();
    table.setCellSpacing(0);
    colgroup = Document.get().createColGroupElement();
    table.appendChild(colgroup);
    thead = table.createTHead();

    if (table.getTBodies().getLength() > 0) {
      tbody = table.getTBodies().getItem(0);
    } else {
      tbody = Document.get().createTBodyElement();
      table.appendChild(tbody);
    }
    table.appendChild(tbodyLoading = Document.get().createTBodyElement());
    tfoot = table.createTFoot();
    setStyleName(this.style.cellGridWidget());

    tbodyLoadingCell = Document.get().createTDElement();
    TableRowElement tr = Document.get().createTRElement();
    tbodyLoading.appendChild(tr);
    tr.appendChild(tbodyLoadingCell);
    tbodyLoadingCell.setAlign("center");

    tbodyLoadingCell.appendChild(messagesPanel.getElement());
    adopt(messagesPanel);
    messagesPanel.add(emptyTableWidgetContainer);

    loadingIndicatorContainer.setStyleName(this.style.cellGridLoading());
    setLoadingIndicator(createDefaultLoadingIndicator());
    messagesPanel.add(loadingIndicatorContainer);

    Set<String> eventTypes = new HashSet<String>();
    eventTypes.add("mouseover");
    eventTypes.add("mouseout");
    CellBasedWidgetImpl.get().sinkEvents(this, eventTypes);

    createId();
  }

  public CellGrid(ProvidesKey<IsRow> keyProvider) {
    this(DEFAULT_PAGESIZE, null, keyProvider);
  }

  public void addColumn(Column<IsRow, ?> col) {
    insertColumn(getColumnCount(), col);
  }

  public void addColumn(Column<IsRow, ?> col, Header<?> header) {
    insertColumn(getColumnCount(), col, header);
  }

  public void addColumn(Column<IsRow, ?> col, Header<?> header, Header<?> footer) {
    insertColumn(getColumnCount(), col, header, footer);
  }

  public void addColumn(Column<IsRow, ?> col, SafeHtml headerHtml) {
    insertColumn(getColumnCount(), col, headerHtml);
  }

  public void addColumn(Column<IsRow, ?> col, SafeHtml headerHtml, SafeHtml footerHtml) {
    insertColumn(getColumnCount(), col, headerHtml, footerHtml);
  }

  public void addColumn(Column<IsRow, ?> col, String headerString) {
    insertColumn(getColumnCount(), col, headerString);
  }

  public void addColumn(Column<IsRow, ?> col, String headerString, String footerString) {
    insertColumn(getColumnCount(), col, headerString, footerString);
  }

  public HandlerRegistration addColumnSortHandler(ColumnSortEvent.Handler handler) {
    return addHandler(handler, ColumnSortEvent.getType());
  }

  public void addColumnStyleName(int index, String styleName) {
    ensureTableColElement(index).addClassName(styleName);
  }

  public void clearColumnWidth(Column<IsRow, ?> column) {
    columnWidths.remove(column);
  }

  public void createId() {
    DomUtils.createId(this, "cell-grid");
  }

  public int getBodyBorderWidth() {
    return style.bodyBorderWidth();
  }

  public int getBodyCellHeight() {
    return bodyCellHeight;
  }

  public Edges getBodyCellPadding() {
    return bodyCellPadding;
  }

  public int getBodyHeight() {
    int height = getClientHeight(tbody);
    return height;
  }

  public Column<IsRow, ?> getColumn(int col) {
    checkColumnBounds(col);
    return columns.get(col);
  }
  
  public int getColumnCount() {
    return columns.size();
  }

  public int getColumnIndex(Column<IsRow, ?> column) {
    return columns.indexOf(column);
  }

  public ColumnSortList getColumnSortList() {
    return sortList;
  }

  public int getColumnWidth(Column<IsRow, ?> column) {
    if (column == null) {
      return UNDEF;
    }
    Integer width = columnWidths.get(column);
    if (width == null) {
      return UNDEF;
    }
    return width;
  }

  public Widget getEmptyTableWidget() {
    return emptyTableWidgetContainer.getWidget();
  }

  public int getFooterBorderWidth() {
    return style.footerBorderWidth();
  }

  public int getFooterCellHeight() {
    return footerCellHeight;
  }

  public Edges getFooterCellPadding() {
    return footerCellPadding;
  }

  public int getHeaderBorderWidth() {
    return style.headerBorderWidth();
  }

  public int getHeaderCellHeight() {
    return headerCellHeight;
  }

  public Edges getHeaderCellPadding() {
    return headerCellPadding;
  }

  public int getHeaderHeight() {
    int height = getClientHeight(thead);
    return height;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public Widget getLoadingIndicator() {
    return loadingIndicatorContainer.getWidget();
  }

  public int getMaxCellHeight() {
    return maxCellHeight;
  }

  public int getMaxCellWidth() {
    return maxCellWidth;
  }

  public int getMinCellHeight() {
    return minCellHeight;
  }

  public int getMinCellWidth() {
    return minCellWidth;
  }
  
  public TableRowElement getRowElement(int row) {
    getPresenter().flush();
    checkRowBounds(row);
    NodeList<TableRowElement> rows = tbody.getRows();
    return rows.getLength() > row ? rows.getItem(row) : null;
  }

  public void insertColumn(int beforeIndex, Column<IsRow, ?> col) {
    insertColumn(beforeIndex, col, (Header<?>) null, (Header<?>) null);
  }

  public void insertColumn(int beforeIndex, Column<IsRow, ?> col, Header<?> header) {
    insertColumn(beforeIndex, col, header, null);
  }

  public void insertColumn(int beforeIndex, Column<IsRow, ?> col,
      Header<?> header, Header<?> footer) {
    if (beforeIndex != getColumnCount()) {
      checkColumnBounds(beforeIndex);
    }

    headers.add(beforeIndex, header);
    footers.add(beforeIndex, footer);
    columns.add(beforeIndex, col);
    boolean wasinteractive = isInteractive;
    updateDependsOnSelection();

    if (!wasinteractive && isInteractive) {
      keyboardSelectedColumn = beforeIndex;
    }

    Set<String> consumedEvents = new HashSet<String>();
    {
      Set<String> cellEvents = col.getCell().getConsumedEvents();
      if (cellEvents != null) {
        consumedEvents.addAll(cellEvents);
      }
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
    CellBasedWidgetImpl.get().sinkEvents(this, consumedEvents);

    redraw();
  }

  public void insertColumn(int beforeIndex, Column<IsRow, ?> col, SafeHtml headerHtml) {
    insertColumn(beforeIndex, col, new SafeHtmlHeader(headerHtml), null);
  }

  public void insertColumn(int beforeIndex, Column<IsRow, ?> col,
      SafeHtml headerHtml, SafeHtml footerHtml) {
    insertColumn(beforeIndex, col, new SafeHtmlHeader(headerHtml), new SafeHtmlHeader(footerHtml));
  }

  public void insertColumn(int beforeIndex, Column<IsRow, ?> col, String headerString) {
    insertColumn(beforeIndex, col, new TextHeader(headerString), null);
  }

  public void insertColumn(int beforeIndex, Column<IsRow, ?> col,
      String headerString, String footerString) {
    insertColumn(beforeIndex, col, new TextHeader(headerString), new TextHeader(footerString));
  }

  public void redrawFooters() {
    createHeaders(true);
  }

  public void redrawHeaders() {
    createHeaders(false);
  }

  public void removeColumn(Column<IsRow, ?> col) {
    int index = columns.indexOf(col);
    Assert.nonNegative(index);
    removeColumn(index);
  }

  public void removeColumn(int index) {
    Assert.isIndex(columns, index);

    columns.remove(index);
    headers.remove(index);
    footers.remove(index);
    updateDependsOnSelection();

    if (index <= keyboardSelectedColumn) {
      keyboardSelectedColumn = 0;
      if (isInteractive) {
        for (int i = 0; i < columns.size(); i++) {
          if (isColumnInteractive(columns.get(i))) {
            keyboardSelectedColumn = i;
            break;
          }
        }
      }
    }
    redraw();
  }

  public void removeColumnStyleName(int index, String styleName) {
    if (index >= colgroup.getChildCount()) {
      return;
    }
    ensureTableColElement(index).removeClassName(styleName);
  }

  public void setBodyCellHeight(int bodyCellHeight) {
    this.bodyCellHeight = bodyCellHeight;
  }

  public void setBodyCellPadding(Edges bodyCellPadding) {
    this.bodyCellPadding = bodyCellPadding;
  }

  public void setColumnWidth(Column<IsRow, ?> column, double width, Unit unit) {
    int containerSize = table.getOffsetWidth();
    Assert.isPositive(containerSize);
    setColumnWidth(column, width, unit, containerSize);
  }

  public void setColumnWidth(Column<IsRow, ?> column, double width, Unit unit, int containerSize) {
    setColumnWidth(column, Rulers.getIntPixels(width, unit, containerSize));
  }

  public void setColumnWidth(Column<IsRow, ?> column, int width) {
    columnWidths.put(column, width);
  }

  public void setEmptyTableWidget(Widget widget) {
    emptyTableWidgetContainer.setWidget(widget);
  }

  public void setFooterCellHeight(int footerCellHeight) {
    this.footerCellHeight = footerCellHeight;
  }

  public void setFooterCellPadding(Edges footerCellPadding) {
    this.footerCellPadding = footerCellPadding;
  }

  public void setHeaderCellHeight(int headerCellHeight) {
    this.headerCellHeight = headerCellHeight;
  }

  public void setHeaderCellPadding(Edges headerCellPadding) {
    this.headerCellPadding = headerCellPadding;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setLoadingIndicator(Widget widget) {
    loadingIndicatorContainer.setWidget(widget);
  }

  public void setMaxCellHeight(int maxCellHeight) {
    this.maxCellHeight = maxCellHeight;
  }

  public void setMaxCellWidth(int maxCellWidth) {
    this.maxCellWidth = maxCellWidth;
  }

  public void setMinCellHeight(int minCellHeight) {
    this.minCellHeight = minCellHeight;
  }

  public void setMinCellWidth(int minCellWidth) {
    this.minCellWidth = minCellWidth;
  }

  public void setRowStyles(RowStyles<IsRow> rowStyles) {
    this.rowStyles = rowStyles;
  }

  public void setTableLayoutFixed(boolean isFixed) {
    if (isFixed) {
      table.getStyle().setTableLayout(TableLayout.FIXED);
    } else {
      table.getStyle().clearTableLayout();
    }
  }

  public final void setWidth(String width, boolean isFixedLayout) {
    super.setWidth(width);
    setTableLayoutFixed(isFixedLayout);
  }

  @Override
  protected Element convertToElements(SafeHtml html) {
    return TABLE_IMPL.convertToSectionElement(CellGrid.this, "tbody", html);
  }

  @Override
  protected boolean dependsOnSelection() {
    return dependsOnSelection;
  }

  @Override
  protected Element getChildContainer() {
    return tbody;
  }

  @Override
  protected Element getKeyboardSelectedElement() {
    int rowIndex = getKeyboardSelectedRow();
    NodeList<TableRowElement> rows = tbody.getRows();
    if (rowIndex >= 0 && rowIndex < rows.getLength() && columns.size() > 0) {
      TableRowElement tr = rows.getItem(rowIndex);
      TableCellElement td = tr.getCells().getItem(keyboardSelectedColumn);
      return getCellParent(td);
    }
    return null;
  }

  @Override
  protected boolean isKeyboardNavigationSuppressed() {
    return cellIsEditing;
  }

  @Override
  protected void onBlur() {
    Element elem = getKeyboardSelectedElement();
    if (elem != null) {
      TableCellElement td = elem.getParentElement().cast();
      TableRowElement tr = td.getParentElement().cast();
      td.removeClassName(style.cellGridKeyboardSelectedCell());
      setRowStyleName(tr, style.cellGridKeyboardSelectedRow(),
          style.cellGridKeyboardSelectedRowCell(), false);
    }
  }

  @Override
  protected void onBrowserEvent2(Event event) {
    EventTarget eventTarget = event.getEventTarget();
    if (!Element.is(eventTarget)) {
      return;
    }
    final Element target = event.getEventTarget().cast();

    String eventType = event.getType();
    if ("keydown".equals(eventType) && !isKeyboardNavigationSuppressed()
        && KeyboardSelectionPolicy.DISABLED != getKeyboardSelectionPolicy()) {
      if (handleKey(event)) {
        return;
      }
    }

    TableCellElement tableCell = findNearestParentCell(target);
    if (tableCell == null) {
      return;
    }

    Element trElem = tableCell.getParentElement();
    if (trElem == null) {
      return;
    }
    TableRowElement tr = TableRowElement.as(trElem);
    Element sectionElem = tr.getParentElement();
    if (sectionElem == null) {
      return;
    }
    TableSectionElement section = TableSectionElement.as(sectionElem);

    boolean isClick = "click".equals(eventType);
    int col = tableCell.getCellIndex();
    if (section == thead) {
      Header<?> header = headers.get(col);
      if (header != null) {
        if (cellConsumesEventType(header.getCell(), eventType)) {
          Context context = new Context(0, col, header.getKey());
          header.onBrowserEvent(context, tableCell, event);
        }

        Column<IsRow, ?> column = columns.get(col);
        if (isClick && column.isSortable()) {
          updatingSortList = true;
          sortList.push(column);
          updatingSortList = false;
          ColumnSortEvent.fire(this, sortList);
        }
      }
    } else if (section == tfoot) {
      Header<?> footer = footers.get(col);
      if (footer != null && cellConsumesEventType(footer.getCell(), eventType)) {
        Context context = new Context(0, col, footer.getKey());
        footer.onBrowserEvent(context, tableCell, event);
      }
    } else if (section == tbody) {
      int row = tr.getSectionRowIndex();
      if ("mouseover".equals(eventType)) {
        if (hoveringRow != null && tbody.isOrHasChild(hoveringRow)) {
          setRowStyleName(hoveringRow, style.cellGridHoveredRow(),
              style.cellGridHoveredRowCell(), false);
        }
        hoveringRow = tr;
        setRowStyleName(hoveringRow, style.cellGridHoveredRow(),
            style.cellGridHoveredRowCell(), true);
      } else if ("mouseout".equals(eventType) && hoveringRow != null) {
        setRowStyleName(hoveringRow, style.cellGridHoveredRow(),
            style.cellGridHoveredRowCell(), false);
        hoveringRow = null;
      } else if (isClick && ((getPresenter().getKeyboardSelectedRowInView() != row)
          || (keyboardSelectedColumn != col))) {
        boolean isFocusable = CellBasedWidgetImpl.get().isFocusable(target);
        isFocused = isFocused || isFocusable;
        keyboardSelectedColumn = col;
        getPresenter().setKeyboardSelectedRow(row, !isFocusable, true);
      }

      if (!isRowWithinBounds(row)) {
        return;
      }
      boolean isSelectionHandled = handlesSelection
          || KeyboardSelectionPolicy.BOUND_TO_SELECTION == getKeyboardSelectionPolicy();
      IsRow value = getVisibleItem(row);
      Context context = new Context(row + getPageStart(), col, getValueKey(value));
      CellPreviewEvent<IsRow> previewEvent = CellPreviewEvent.fire(this, event,
          this, context, value, cellIsEditing, isSelectionHandled);

      if (!previewEvent.isCanceled()) {
        fireEventToCell(event, eventType, tableCell, value, context, columns.get(col));
      }
    }
  }

  @Override
  protected void onFocus() {
    Element elem = getKeyboardSelectedElement();
    if (elem != null) {
      TableCellElement td = elem.getParentElement().cast();
      TableRowElement tr = td.getParentElement().cast();
      td.addClassName(style.cellGridKeyboardSelectedCell());
      setRowStyleName(tr, style.cellGridKeyboardSelectedRow(),
          style.cellGridKeyboardSelectedRowCell(), true);
    }
  }

  @Override
  protected void onLoadingStateChanged(LoadingState state) {
    Widget message = null;
    if (state == LoadingState.LOADING) {
      message = loadingIndicatorContainer;
    } else if (state == LoadingState.LOADED && getPresenter().isEmpty()) {
      message = emptyTableWidgetContainer;
    }

    if (message != null) {
      messagesPanel.showWidget(messagesPanel.getWidgetIndex(message));
    }

    tbodyLoadingCell.setColSpan(Math.max(1, columns.size()));

    showOrHide(getChildContainer(), message == null);
    showOrHide(tbodyLoading, message != null);

    super.onLoadingStateChanged(state);
  }

  @Override
  protected void renderRowValues(SafeHtmlBuilder sb, List<IsRow> values, int start,
      SelectionModel<? super IsRow> selectionModel) {
    createHeadersAndFooters();

    int keyboardSelectedRow = getKeyboardSelectedRow() + getPageStart();

    String evenRowStyle = style.cellGridEvenRow();
    String oddRowStyle = style.cellGridOddRow();
    String cellStyle = style.cellGridCell();
    String evenCellStyle = " " + style.cellGridEvenRowCell();
    String oddCellStyle = " " + style.cellGridOddRowCell();
    String firstColumnStyle = " " + style.cellGridFirstColumn();
    String lastColumnStyle = " " + style.cellGridLastColumn();
    String selectedRowStyle = " " + style.cellGridSelectedRow();
    String selectedCellStyle = " " + style.cellGridSelectedRowCell();
    String keyboardRowStyle = " " + style.cellGridKeyboardSelectedRow();
    String keyboardRowCellStyle = " " + style.cellGridKeyboardSelectedRowCell();
    String keyboardCellStyle = " " + style.cellGridKeyboardSelectedCell();
    
    int cellHeight = getBodyCellHeight();
    int borderWidth = getBodyBorderWidth();
    
    Edges padding = getBodyCellPadding();
    String cssPadding = getCssValue(padding);

    int columnCount = columns.size();
    int length = values.size();
    int end = start + length;

    for (int i = start; i < end; i++) {
      IsRow value = values.get(i - start);
      boolean isSelected = (selectionModel == null || value == null) ? false
          : selectionModel.isSelected(value);
      boolean isEven = i % 2 == 0;
      boolean isKeyboardSelected = i == keyboardSelectedRow && isFocused;
      String trClasses = isEven ? evenRowStyle : oddRowStyle;
      if (isSelected) {
        trClasses += selectedRowStyle;
      }
      if (isKeyboardSelected) {
        trClasses += keyboardRowStyle;
      }

      if (rowStyles != null) {
        String extraRowStyles = rowStyles.getStyleNames(value, i);
        if (extraRowStyles != null) {
          trClasses += " ";
          trClasses += extraRowStyles;
        }
      }

      SafeHtmlBuilder trBuilder = new SafeHtmlBuilder();
      int curColumn = 0;
      for (Column<IsRow, ?> column : columns) {
        String tdClasses = cellStyle;
        tdClasses += isEven ? evenCellStyle : oddCellStyle;
        if (curColumn == 0) {
          tdClasses += firstColumnStyle;
        }
        if (isSelected) {
          tdClasses += selectedCellStyle;
        }
        if (isKeyboardSelected) {
          tdClasses += keyboardRowCellStyle;
        }
        if (curColumn == columnCount - 1) {
          tdClasses += lastColumnStyle;
        }

        SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
        if (value != null) {
          Context context = new Context(i, curColumn, getValueKey(value));
          column.render(context, value, cellBuilder);
        }

        boolean focusable = (i == keyboardSelectedRow && curColumn == keyboardSelectedColumn);
        if (focusable) {
          if (isFocused) {
            tdClasses += keyboardCellStyle;
          }
        }

        SafeHtml contents = renderCell(cellBuilder.toSafeHtml(), focusable,
            getColumnWidth(column), cellHeight, padding, borderWidth);

        HorizontalAlignmentConstant hAlign = column.getHorizontalAlignment();
        VerticalAlignmentConstant vAlign = column.getVerticalAlignment();
        if (hAlign != null && vAlign != null) {
          trBuilder.append(template.tdBothAlign(tdClasses,
              hAlign.getTextAlignString(), vAlign.getVerticalAlignString(), cssPadding, contents));
        } else if (hAlign != null) {
          trBuilder.append(template.tdHorizontalAlign(tdClasses,
              hAlign.getTextAlignString(), cssPadding, contents));
        } else if (vAlign != null) {
          trBuilder.append(template.tdVerticalAlign(tdClasses,
              vAlign.getVerticalAlignString(), cssPadding, contents));
        } else {
          trBuilder.append(template.td(tdClasses, cssPadding, contents));
        }

        curColumn++;
      }

      sb.append(template.tr(trClasses, trBuilder.toSafeHtml()));
    }
  }

  @Override
  protected void replaceAllChildren(SafeHtml html) {
    TABLE_IMPL.replaceAllRows(CellGrid.this, tbody, CellBasedWidgetImpl.get().processHtml(html));
  }

  @Override
  protected boolean resetFocusOnCell() {
    int row = getKeyboardSelectedRow();
    if (isRowWithinBounds(row) && columns.size() > 0) {
      Column<IsRow, ?> column = columns.get(keyboardSelectedColumn);
      return resetFocusOnCellImpl(row, keyboardSelectedColumn, column);
    }
    return false;
  }

  @Override
  protected void setKeyboardSelected(int index, boolean selected, boolean stealFocus) {
    if (KeyboardSelectionPolicy.DISABLED == getKeyboardSelectionPolicy()
        || !isRowWithinBounds(index) || columns.size() == 0) {
      return;
    }

    TableRowElement tr = getRowElement(index);
    String cellStyle = style.cellGridKeyboardSelectedCell();
    boolean updatedSelection = !selected || isFocused || stealFocus;
    setRowStyleName(tr, style.cellGridKeyboardSelectedRow(),
        style.cellGridKeyboardSelectedRowCell(), selected);
    NodeList<TableCellElement> cells = tr.getCells();
    for (int i = 0; i < cells.getLength(); i++) {
      TableCellElement td = cells.getItem(i);

      setStyleName(td, cellStyle, updatedSelection && selected && i == keyboardSelectedColumn);

      final com.google.gwt.user.client.Element cellParent = getCellParent(td).cast();
      setFocusable(cellParent, selected && i == keyboardSelectedColumn);
    }

    if (selected && stealFocus && !cellIsEditing) {
      TableCellElement td = tr.getCells().getItem(keyboardSelectedColumn);
      final com.google.gwt.user.client.Element cellParent = getCellParent(td).cast();
      CellBasedWidgetImpl.get().resetFocus(new Scheduler.ScheduledCommand() {
        public void execute() {
          cellParent.focus();
        }
      });
    }
  }

  private void checkColumnBounds(int col) {
    Assert.betweenExclusive(col, 0, getColumnCount());
  }

  private void createHeaders(boolean isFooter) {
    List<Header<?>> theHeaders = isFooter ? footers : headers;
    TableSectionElement section = isFooter ? tfoot : thead;

    String className = isFooter ? style.cellGridFooter() : style.cellGridHeader();
    String firstColumnStyle = " "
        + (isFooter ? style.cellGridFirstColumnFooter() : style.cellGridFirstColumnHeader());
    String lastColumnStyle = " "
        + (isFooter ? style.cellGridLastColumnFooter() : style.cellGridLastColumnHeader());
    String sortableStyle = " " + style.cellGridSortableHeader();
    String sortedAscStyle = " " + style.cellGridSortedHeaderAscending();
    String sortedDescStyle = " " + style.cellGridSortedHeaderDescending();
    
    String padding = getCssValue(isFooter ? getFooterCellPadding() : getHeaderCellPadding());

    boolean hasHeader = false;
    SafeHtmlBuilder sb = new SafeHtmlBuilder();
    sb.appendHtmlConstant("<tr>");
    int columnCount = columns.size();
    if (columnCount > 0) {
      ColumnSortInfo sortedInfo = (sortList.size() == 0) ? null : sortList.get(0);
      Column<?, ?> sortedColumn = (sortedInfo == null) ? null : sortedInfo.getColumn();
      boolean isSortAscending = (sortedInfo == null) ? false : sortedInfo.isAscending();

      Header<?> prevHeader = theHeaders.get(0);
      Column<IsRow, ?> column = columns.get(0);
      int prevColspan = 1;
      boolean isSortable = false;
      boolean isSorted = false;
      StringBuilder classesBuilder = new StringBuilder(className);
      classesBuilder.append(firstColumnStyle);
      if (!isFooter && column.isSortable()) {
        isSortable = true;
        isSorted = (column == sortedColumn);
      }

      int curColumn;
      for (curColumn = 1; curColumn < columnCount; curColumn++) {
        Header<?> header = theHeaders.get(curColumn);

        if (header != prevHeader) {
          SafeHtml headerHtml = SafeHtmlUtils.EMPTY_SAFE_HTML;
          if (prevHeader != null) {
            hasHeader = true;

            SafeHtmlBuilder headerBuilder = new SafeHtmlBuilder();
            Context context = new Context(0, curColumn - prevColspan, prevHeader.getKey());
            prevHeader.render(context, headerBuilder);

            if (isSorted) {
              SafeHtml unwrappedHeader = headerBuilder.toSafeHtml();
              headerBuilder = new SafeHtmlBuilder();
              getSortDecorator(isSortAscending).render(null, unwrappedHeader, headerBuilder);
            }
            headerHtml = headerBuilder.toSafeHtml();
          }
          if (isSortable) {
            classesBuilder.append(sortableStyle);
          }
          if (isSorted) {
            classesBuilder.append(isSortAscending ? sortedAscStyle : sortedDescStyle);
          }
          sb.append(template.th(prevColspan, classesBuilder.toString(), padding, headerHtml));

          prevHeader = header;
          prevColspan = 1;
          classesBuilder = new StringBuilder(className);
          isSortable = false;
          isSorted = false;
        } else {
          prevColspan++;
        }

        column = columns.get(curColumn);
        if (!isFooter && column.isSortable()) {
          isSortable = true;
          isSorted = (column == sortedColumn);
        }
      }

      SafeHtml headerHtml = SafeHtmlUtils.EMPTY_SAFE_HTML;
      if (prevHeader != null) {
        hasHeader = true;

        SafeHtmlBuilder headerBuilder = new SafeHtmlBuilder();
        Context context = new Context(0, curColumn - prevColspan, prevHeader.getKey());
        prevHeader.render(context, headerBuilder);

        if (isSorted) {
          SafeHtml unwrappedHeader = headerBuilder.toSafeHtml();
          headerBuilder = new SafeHtmlBuilder();
          getSortDecorator(isSortAscending).render(null, unwrappedHeader, headerBuilder);
        }
        headerHtml = headerBuilder.toSafeHtml();
      }
      if (isSortable) {
        classesBuilder.append(sortableStyle);
      }
      if (isSorted) {
        classesBuilder.append(isSortAscending ? sortedAscStyle : sortedDescStyle);
      }

      classesBuilder.append(" ");
      classesBuilder.append(lastColumnStyle);
      sb.append(template.th(prevColspan, classesBuilder.toString(), padding, headerHtml));
    }
    sb.appendHtmlConstant("</tr>");

    TABLE_IMPL.replaceAllRows(this, section, sb.toSafeHtml());

    setVisible(section, hasHeader);
  }

  private void createHeadersAndFooters() {
    createHeaders(false);
    createHeaders(true);
  }

  private TableColElement ensureTableColElement(int index) {
    for (int i = colgroup.getChildCount(); i <= index; i++) {
      colgroup.appendChild(Document.get().createColElement());
    }
    return colgroup.getChild(index).cast();
  }

  private int findInteractiveColumn(int start, boolean reverse) {
    if (!isInteractive) {
      return 0;
    } else if (reverse) {
      for (int i = start - 1; i >= 0; i--) {
        if (isColumnInteractive(columns.get(i))) {
          return i;
        }
      }
      for (int i = columns.size() - 1; i >= start; i--) {
        if (isColumnInteractive(columns.get(i))) {
          return i;
        }
      }
    } else {
      for (int i = start + 1; i < columns.size(); i++) {
        if (isColumnInteractive(columns.get(i))) {
          return i;
        }
      }
      for (int i = 0; i <= start; i++) {
        if (isColumnInteractive(columns.get(i))) {
          return i;
        }
      }
    }
    return 0;
  }

  private TableCellElement findNearestParentCell(Element elem) {
    while ((elem != null) && (elem != table)) {
      String tagName = elem.getTagName();
      if ("td".equalsIgnoreCase(tagName) || "th".equalsIgnoreCase(tagName)) {
        return elem.cast();
      }
      elem = elem.getParentElement();
    }
    return null;
  }

  private <C> void fireEventToCell(Event event, String eventType,
      TableCellElement tableCell, IsRow value, Context context, Column<IsRow, C> column) {
    Cell<C> cell = column.getCell();
    if (cellConsumesEventType(cell, eventType)) {
      C cellValue = column.getValue(value);
      Element parentElem = getCellParent(tableCell);
      boolean cellWasEditing = cell.isEditing(context, parentElem, cellValue);
      column.onBrowserEvent(context, parentElem, value, event);
      cellIsEditing = cell.isEditing(context, parentElem, cellValue);
      if (cellWasEditing && !cellIsEditing) {
        CellBasedWidgetImpl.get().resetFocus(new Scheduler.ScheduledCommand() {
          public void execute() {
            setFocus(true);
          }
        });
      }
    }
  }

  private Element getCellParent(TableCellElement td) {
    return td.getFirstChildElement();
  }

  private native int getClientHeight(Element element) /*-{
    return element.clientHeight;
  }-*/;

  private String getCssValue(Edges edges) {
    if (edges == null) {
      return Edges.EMPTY_CSS_VALUE;
    } else {
      return edges.getCssValue();
    }
  }

  private IconCellDecorator<SafeHtml> getSortDecorator(boolean ascending) {
    if (ascending) {
      if (sortAscDecorator == null) {
        sortAscDecorator = new IconCellDecorator<SafeHtml>(
            Global.getImages().ascending(), new SafeHtmlCell());
      }
      return sortAscDecorator;
    } else {
      if (sortDescDecorator == null) {
        sortDescDecorator = new IconCellDecorator<SafeHtml>(
            Global.getImages().descending(), new SafeHtmlCell());
      }
      return sortDescDecorator;
    }
  }

  private boolean handleKey(Event event) {
    DataPresenter<IsRow> presenter = getPresenter();
    int oldRow = getKeyboardSelectedRow();
    boolean isRtl = LocaleInfo.getCurrentLocale().isRTL();
    int keyCodeLineEnd = isRtl ? KeyCodes.KEY_LEFT : KeyCodes.KEY_RIGHT;
    int keyCodeLineStart = isRtl ? KeyCodes.KEY_RIGHT : KeyCodes.KEY_LEFT;
    int keyCode = event.getKeyCode();

    if (keyCode == keyCodeLineEnd) {
      int nextColumn = findInteractiveColumn(keyboardSelectedColumn, false);
      if (nextColumn <= keyboardSelectedColumn) {
        if (presenter.hasKeyboardNext()) {
          keyboardSelectedColumn = nextColumn;
          presenter.keyboardNext();
          event.preventDefault();
          return true;
        }
      } else {
        keyboardSelectedColumn = nextColumn;
        getPresenter().setKeyboardSelectedRow(oldRow, true, true);
        event.preventDefault();
        return true;
      }
    } else if (keyCode == keyCodeLineStart) {
      int prevColumn = findInteractiveColumn(keyboardSelectedColumn, true);
      if (prevColumn >= keyboardSelectedColumn) {
        if (presenter.hasKeyboardPrev()) {
          keyboardSelectedColumn = prevColumn;
          presenter.keyboardPrev();
          event.preventDefault();
          return true;
        }
      } else {
        keyboardSelectedColumn = prevColumn;
        getPresenter().setKeyboardSelectedRow(oldRow, true, true);
        event.preventDefault();
        return true;
      }
    }
    return false;
  }

  private boolean isColumnInteractive(Column<IsRow, ?> column) {
    Set<String> consumedEvents = column.getCell().getConsumedEvents();
    return consumedEvents != null && consumedEvents.size() > 0;
  }

  private SafeHtml renderCell(SafeHtml cellContent, boolean focusable,
      int width, int height, Edges padding, int borderWidth) {
    SafeHtml result = SafeHtmlUtils.EMPTY_SAFE_HTML;

    int w = UNDEF;
    int h = UNDEF;
    
    boolean fixedWidth = false;
    boolean fixedHeight = false;
    
    if (width > 0) {
      w = width - borderWidth;
      if (padding != null) {
        w -= Math.max(BeeUtils.toInt(padding.getRightValue()), 0);
        w -= Math.max(BeeUtils.toInt(padding.getRightValue()), 0);
      }
      if (w > 0) {
        w = BeeUtils.limit(w, minCellWidth, maxCellWidth);
        fixedWidth = (w > 0);
      }
    }
    
    if (height > 0) {
      h = height - borderWidth;
      if (padding != null) {
        h -= Math.max(BeeUtils.toInt(padding.getTopValue()), 0);
        h -= Math.max(BeeUtils.toInt(padding.getBottomValue()), 0);
      }
      if (h > 0) {
        h = BeeUtils.limit(h, minCellHeight, maxCellHeight);
        fixedHeight = (h > 0);
      }
    }
    
    if (focusable) {
      int tabIndex = getTabIndex();
      if (fixedWidth && fixedHeight) {
        result = template.divFocusableFixedSize(w, h, tabIndex, cellContent);
      } else if (fixedWidth) {
        result = template.divFocusableFixedWidth(w, tabIndex, cellContent);
      } else if (fixedHeight) {
        result = template.divFocusableFixedHeight(h, tabIndex, cellContent);
      } else {
        result = template.divFocusable(tabIndex, cellContent);
      }

    } else {
      if (fixedWidth && fixedHeight) {
        result = template.divFixedSize(w, h, cellContent);
      } else if (fixedWidth) {
        result = template.divFixedWidth(w, cellContent);
      } else if (fixedHeight) {
        result = template.divFixedHeight(h, cellContent);
      } else {
        result = template.div(cellContent);
      }
    }
    
    return result;
  }

  private <C> boolean resetFocusOnCellImpl(int row, int col, Column<IsRow, C> column) {
    Element parent = getKeyboardSelectedElement();
    IsRow value = getVisibleItem(row);
    Object key = getValueKey(value);
    C cellValue = column.getValue(value);
    Cell<C> cell = column.getCell();
    Context context = new Context(row + getPageStart(), col, key);
    return cell.resetFocus(context, parent, cellValue);
  }
  
  private void setRowStyleName(TableRowElement tr, String rowStyle, String cellStyle, boolean add) {
    setStyleName(tr, rowStyle, add);
    NodeList<TableCellElement> cells = tr.getCells();
    for (int i = 0; i < cells.getLength(); i++) {
      setStyleName(cells.getItem(i), cellStyle, add);
    }
  }
  
  private void updateDependsOnSelection() {
    dependsOnSelection = false;
    handlesSelection = false;
    isInteractive = false;
    for (Column<IsRow, ?> column : columns) {
      Cell<?> cell = column.getCell();
      if (cell.dependsOnSelection()) {
        dependsOnSelection = true;
      }
      if (cell.handlesSelection()) {
        handlesSelection = true;
      }
      if (isColumnInteractive(column)) {
        isInteractive = true;
      }
    }
  }
}
