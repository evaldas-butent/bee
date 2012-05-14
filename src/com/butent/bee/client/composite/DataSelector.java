package com.butent.bee.client.composite;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.HasRelatedRow;
import com.butent.bee.client.data.SelectionOracle;
import com.butent.bee.client.data.SelectionOracle.Callback;
import com.butent.bee.client.data.SelectionOracle.Request;
import com.butent.bee.client.data.SelectionOracle.Response;
import com.butent.bee.client.data.SelectionOracle.Suggestion;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.menu.MenuBar;
import com.butent.bee.client.menu.MenuCommand;
import com.butent.bee.client.menu.MenuItem;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.EnumRenderer;
import com.butent.bee.client.render.RendererFactory;
import com.butent.bee.client.render.SimpleRenderer;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.EditorFactory;
import com.butent.bee.client.view.edit.HasTextBox;
import com.butent.bee.client.view.edit.SelectorEvent;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.menu.MenuConstants;
import com.butent.bee.shared.menu.MenuConstants.BAR_TYPE;
import com.butent.bee.shared.menu.MenuConstants.ITEM_TYPE;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.HasVisibleLines;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.ui.SelectorColumn;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Enables using user interface component for entering text entries while the system is suggesting
 * possible matching values from the list.
 */

public class DataSelector extends Composite implements Editor, HasVisibleLines, HasTextBox,
    HasRelatedRow {

  public class SimpleHandler implements FocusHandler, BlurHandler, EditStopEvent.Handler {

    private final AbstractCellRenderer renderer;

    private String text = null;

    public SimpleHandler(AbstractCellRenderer renderer) {
      this.renderer = renderer;
    }

    public SimpleHandler(int dataIndex) {
      this(dataIndex, ValueType.TEXT);
    }

    public SimpleHandler(int dataIndex, ValueType dataType) {
      this(new SimpleRenderer(dataIndex, dataType));
    }

    public void onBlur(BlurEvent event) {
      updateDisplay(getText());
      setEditing(false);
    }

    public void onEditStop(EditStopEvent event) {
      if (renderer != null) {
        if (event.isChanged()) {
          setText(getRelatedRow() == null ? null : renderer.render(getRelatedRow()));
        }
        updateDisplay(getText());
        UiHelper.moveFocus(getParent(), getElement(), true);
      }
    }

    public void onFocus(FocusEvent event) {
      setText(getDisplayValue());
      setEditing(true);
    }

    private String getText() {
      return text;
    }

    private void setText(String text) {
      this.text = text;
    }

    private void updateDisplay(String value) {
      if (BeeUtils.isEmpty(value)) {
        clearDisplay();
      } else {
        setDisplayValue(value.trim());
      }
    }
  }

  private class InputEvents implements MouseWheelHandler {

    private boolean consumed = false;

    public void onMouseWheel(MouseWheelEvent event) {
      if (!isEnabled() || !isActive() || !getSelector().isShowing()) {
        return;
      }

      int y = event.getDeltaY();
      if (y > 0) {
        nextOffset();
      } else if (y < 0) {
        prevOffset();
      }
    }

    private void consume() {
      setConsumed(true);
    }

    private boolean isConsumed() {
      return consumed;
    }

    private void onKeyDown(Event event) {
      if (!isEnabled() || !isActive()) {
        setConsumed(false);
        return;
      }

      int keyCode = event.getKeyCode();
      boolean hasModifiers = EventUtils.hasModifierKey(event);

      switch (keyCode) {
        case KeyCodes.KEY_DOWN:
          consume();
          if (hasModifiers && hasMore()) {
            nextOffset();
          } else {
            getSelector().moveSelectionDown();
          }
          break;

        case KeyCodes.KEY_UP:
          consume();
          if (hasModifiers && getOffset() > 0) {
            prevOffset();
          } else {
            getSelector().moveSelectionUp();
          }
          break;

        case KeyCodes.KEY_PAGEDOWN:
          consume();
          if (hasMore()) {
            nextPage();
          } else {
            getSelector().selectLast();
          }
          break;

        case KeyCodes.KEY_PAGEUP:
          consume();
          if (getOffset() > 0) {
            prevPage();
          } else {
            getSelector().selectFirst();
          }
          break;

        case KeyCodes.KEY_ESCAPE:
          consume();
          if (getSelector().isItemSelected()) {
            getSelector().cancelSelection();
          } else {
            exit(true, State.CANCELED);
          }
          break;

        case KeyCodes.KEY_ENTER:
          consume();
          if (getSelector().isShowing()) {
            getSelector().handleKeyboardSelection(isInstant(), hasModifiers);
          } else if (BeeUtils.isEmpty(getDisplayValue())) {
            askOracle();
          }
          break;

        case KeyCodes.KEY_TAB:
          consume();
          exit(true, State.CLOSED, keyCode, hasModifiers);
          break;

        default:
          setConsumed(false);
      }
    }

    private void onKeyUp(Event event) {
      if (!isEnabled() || isConsumed() || !isActive()) {
        return;
      }
      int keyCode = event.getKeyCode();

      if (isInstant() || keyCode == KeyCodes.KEY_ENTER) {
        consume();
        askOracle();
      }
    }

    private void setConsumed(boolean consumed) {
      this.consumed = consumed;
    }
  }

  /**
   * Handles suggestion display.
   */

  private class Selector {

    private final MenuBar menu;
    private final Popup popup;

    private MenuItem itemPrev = null;
    private MenuItem itemNext = null;

    private Boolean pendingSelection = null;

    private Selector(ITEM_TYPE itemType, UIObject partner) {
      this.menu = new MenuBar(MenuConstants.ROOT_MENU_INDEX, true, BAR_TYPE.TABLE, itemType);

      menu.addStyleName(STYLE_MENU);

      this.popup = new Popup(true, false);
      popup.setStyleName(STYLE_POPUP);
      popup.setWidget(menu);

      popup.addAutoHidePartner(partner.getElement());
      popup.addCloseHandler(new CloseHandler<Popup>() {
        public void onClose(CloseEvent<Popup> event) {
          if (event.isAutoClosed()) {
            getMenu().clearItems();
            exit(false, State.CANCELED);
          }
        }
      });
    }

    private void cancelSelection() {
      getMenu().selectItem(null);
    }

    private MenuItem createNavigationItem(boolean next) {
      MenuCommand command;
      if (next) {
        command = new MenuCommand() {
          @Override
          public void execute() {
            nextPage();
          }
        };
      } else {
        command = new MenuCommand() {
          @Override
          public void execute() {
            prevPage();
          }
        };
      }

      MenuItem item = new MenuItem(menu, next ? ITEM_NEXT : ITEM_PREV, ITEM_TYPE.LABEL, command);
      item.addStyleName(STYLE_NAVIGATION);

      return item;
    }

    private MenuItem getItemNext() {
      return itemNext;
    }

    private MenuItem getItemPrev() {
      return itemPrev;
    }

    private MenuBar getMenu() {
      return menu;
    }

    private Boolean getPendingSelection() {
      return pendingSelection;
    }

    private Popup getPopup() {
      return popup;
    }

    private boolean handleKeyboardSelection(boolean instant, boolean hasModifiers) {
      if (!isShowing()) {
        return false;
      }
      if (!instant && !hasModifiers) {
        return false;
      }

      MenuItem item = getMenu().getSelectedItem();
      if (item == null && (hasModifiers || instant && getMenu().getItemCount() == 1)) {
        for (MenuItem it : getMenu().getItems()) {
          if (!isNavigationItem(it)) {
            item = it;
            break;
          }
        }
      }
      if (item == null || item.getCommand() == null) {
        return false;
      }

      if (isNavigationItem(item)) {
        setPendingSelection(item == getItemPrev());
      }
      item.getCommand().execute();
      return true;
    }

    private void hide() {
      getPopup().hide();
    }

    private void initNavigationItem(MenuItem item) {
      addClassToCell(item, STYLE_NAVIGATION_CELL);
      if (isTableMode() && getColumnCount() > 1) {
        DomUtils.setColSpan(DomUtils.getParentCell(item, true), getColumnCount());
      }
    }

    private boolean isItemSelected() {
      if (isShowing()) {
        return getMenu().getSelectedItem() != null;
      } else {
        return false;
      }
    }

    private boolean isNavigationItem(MenuItem item) {
      return item == getItemPrev() || item == getItemNext();
    }

    private boolean isShowing() {
      return getPopup().isShowing();
    }

    private void moveSelectionDown() {
      if (isShowing()) {
        getMenu().moveDown();
      }
    }

    private void moveSelectionUp() {
      if (isShowing()) {
        getMenu().moveUp();
      }
    }

    private void selectFirst() {
      if (isShowing()) {
        getMenu().selectFirstItem();
      }
    }

    private void selectLast() {
      if (isShowing()) {
        getMenu().selectLastItem();
      }
    }

    private void setItemNext(MenuItem itemNext) {
      this.itemNext = itemNext;
    }

    private void setItemPrev(MenuItem itemPrev) {
      this.itemPrev = itemPrev;
    }

    private void setPendingSelection(Boolean pendingSelection) {
      this.pendingSelection = pendingSelection;
    }

    private void showSuggestions(Response response, UIObject target) {
      Collection<Suggestion> suggestions = response.getSuggestions();
      if (BeeUtils.isEmpty(suggestions)) {
        getPopup().hide();
        return;
      }

      getMenu().clearItems();

      if (getOffset() > 0) {
        if (getItemPrev() == null) {
          setItemPrev(createNavigationItem(false));
        }
        getMenu().addItem(getItemPrev());
        initNavigationItem(getItemPrev());
      }

      for (Suggestion suggestion : suggestions) {
        addItem(getMenu(), suggestion.getRow());
      }

      if (response.hasMoreSuggestions()) {
        if (getItemNext() == null) {
          setItemNext(createNavigationItem(true));
        }
        getMenu().addItem(getItemNext());
        initNavigationItem(getItemNext());
      }

      if (getPendingSelection() != null) {
        MenuItem item = getPendingSelection() ? getItemNext() : getItemPrev();
        if (item != null) {
          getMenu().selectItem(item);
        }
        setPendingSelection(null);
      }

      getPopup().showRelativeTo(target);
    }
  }

  private static final String ITEM_PREV = String.valueOf('\u25b2');
  private static final String ITEM_NEXT = String.valueOf('\u25bc');

  private static final String STYLE_SELECTOR = "bee-DataSelector";

  private static final String STYLE_EMBEDDED = STYLE_SELECTOR + "-embedded";

  private static final String STYLE_WAITING = STYLE_SELECTOR + "-waiting";
  private static final String STYLE_NOT_FOUND = STYLE_SELECTOR + "-notFound";
  private static final String STYLE_EMPTY = STYLE_SELECTOR + "-empty";

  private static final String STYLE_POPUP = STYLE_SELECTOR + "-popup";
  private static final String STYLE_MENU = STYLE_SELECTOR + "-menu";

  private static final String STYLE_TABLE = STYLE_SELECTOR + "-table";
  private static final String STYLE_ROW = STYLE_SELECTOR + "-row";
  private static final String STYLE_CELL = STYLE_SELECTOR + "-cell";

  private static final String STYLE_CONTENT = STYLE_SELECTOR + "-content";
  private static final String STYLE_ITEM = STYLE_SELECTOR + "-item";

  private static final String STYLE_NAVIGATION = STYLE_SELECTOR + "-navigation";
  private static final String STYLE_NAVIGATION_CELL = STYLE_SELECTOR + "-navigationCell";

  private static final int DEFAULT_VISIBLE_LINES = 10;

  private static final Operator DEFAULT_SEARCH_TYPE = Operator.CONTAINS;

  private static final char SHOW_SELECTOR = '*';

  private final Callback callback = new Callback() {
    public void onSuggestionsReady(Request request, Response response) {
      DataSelector.this.getInput().removeStyleName(STYLE_WAITING);
      
      boolean found = response != null && !BeeUtils.isEmpty(response.getSuggestions());
      DataSelector.this.getInput().setStyleName(STYLE_NOT_FOUND, !found);
      if (!found && BeeUtils.isEmpty(request.getQuery())) {
        DataSelector.this.getInput().addStyleName(STYLE_EMPTY);
      }

      if (isEditing()) {
        setHasMore(response.hasMoreSuggestions());
        getSelector().showSuggestions(response, DataSelector.this);
      }
    }
  };

  private int visibleLines = DEFAULT_VISIBLE_LINES;

  private final SelectionOracle oracle;
  private final Operator searchType;

  private final InputText input;
  private final Selector selector;

  private final List<String> choiceColumns = Lists.newArrayList();
  private final Map<Integer, SelectorColumn> selectorColumns = Maps.newHashMap();

  private final boolean tableMode;

  private final AbstractCellRenderer rowRenderer;
  private final Map<Integer, AbstractCellRenderer> cellRenderers = Maps.newHashMap();

  private final InputEvents inputEvents = new InputEvents();

  private final boolean embedded;

  private boolean active = false;

  private BeeRow selectedRow = null;
  private String editorValue = null;

  private Request lastRequest = null;
  private int offset = 0;
  private boolean hasMore = false;

  public DataSelector(Relation relation, boolean embedded) {
    super();

    this.embedded = embedded;

    DataInfo viewInfo = Global.getDataInfo(relation.getViewName(), true);
    this.oracle = new SelectionOracle(relation, viewInfo);
    this.searchType =
        (relation.getOperator() == null) ? DEFAULT_SEARCH_TYPE : relation.getOperator();

    ITEM_TYPE itemType = relation.getItemType();

    this.input = new InputText();
    this.selector = new Selector(itemType, this.input);

    this.choiceColumns.addAll(relation.getChoiceColumns());
    for (SelectorColumn selectorColumn : relation.getSelectorColumns()) {
      int index = this.choiceColumns.indexOf(selectorColumn.getSource());
      if (index >= 0) {
        this.selectorColumns.put(index, selectorColumn);
      }
    }
    int size = choiceColumns.size();

    this.tableMode = ITEM_TYPE.ROW.equals(itemType) || !selectorColumns.isEmpty()
        || itemType == null && size > 1 && !relation.hasRowRenderer();

    int dataIndex = (size == 1) ? viewInfo.getColumnIndex(choiceColumns.get(0)) : BeeConst.UNDEF;
    this.rowRenderer = RendererFactory.getRenderer(relation.getRowRendererDescription(),
        relation.getRowRender(), relation.getItemKey(), choiceColumns, viewInfo.getColumns(),
        dataIndex);

    if (rowRenderer instanceof EnumRenderer && relation.getSearchableColumns().size() == 1) {
      oracle.createTranslator(((EnumRenderer) rowRenderer).getCaptions(),
          ((EnumRenderer) rowRenderer).getValueStartIndex());
    }

    if (tableMode) {
      initCellRenderers(viewInfo);
      getSelector().getMenu().addStyleName(STYLE_TABLE);
    }

    if (BeeUtils.isPositive(relation.getVisibleLines())) {
      setVisibleLines(relation.getVisibleLines());
    }

    input.addStyleName(STYLE_SELECTOR);
    if (embedded) {
      input.addStyleName(STYLE_EMBEDDED);
    }

    initWidget(input);

    input.addMouseWheelHandler(inputEvents);
    Binder.addMouseWheelHandler(selector.getPopup(), inputEvents);

    sinkEvents(Event.ONBLUR | Event.ONCLICK | Event.KEYEVENTS);
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return addDomHandler(handler, BlurEvent.getType());
  }

  public HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return addDomHandler(handler, FocusEvent.getType());
  }

  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addDomHandler(handler, KeyDownEvent.getType());
  }

  public Collection<HandlerRegistration> addSimpleHandler(AbstractCellRenderer renderer) {
    return addSimpleHandler(new SimpleHandler(renderer));
  }

  public Collection<HandlerRegistration> addSimpleHandler(int dataIndex) {
    return addSimpleHandler(new SimpleHandler(dataIndex));
  }

  public Collection<HandlerRegistration> addSimpleHandler(int dataIndex, ValueType dataType) {
    return addSimpleHandler(new SimpleHandler(dataIndex, dataType));
  }

  public Collection<HandlerRegistration> addSimpleHandler(SimpleHandler handler) {
    Assert.notNull(handler);
    return Lists.newArrayList(addBlurHandler(handler), addFocusHandler(handler),
        addEditStopHandler(handler));
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public EditorAction getDefaultFocusAction() {
    return EditorAction.SELECT;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "selector";
  }

  public String getNormalizedValue() {
    return getEditorValue();
  }

  public SelectionOracle getOracle() {
    return oracle;
  }

  public IsRow getRelatedRow() {
    return selectedRow;
  }

  public int getTabIndex() {
    return getInput().getTabIndex();
  }

  public TextBoxBase getTextBox() {
    return getInput();
  }

  public String getValue() {
    return getEditorValue();
  }

  public int getVisibleLines() {
    return visibleLines;
  }

  public boolean handlesKey(int keyCode) {
    return isActive();
  }

  public boolean isEditing() {
    return getInput().isEditing();
  }

  public boolean isEmbedded() {
    return embedded;
  }

  public boolean isEnabled() {
    return getInput().isEnabled();
  }

  public boolean isNullable() {
    return getInput().isNullable();
  }

  @Override
  public void onBrowserEvent(Event event) {
    boolean showing = getSelector().isShowing();
    int type = event.getTypeInt();

    boolean consumed = false;

    switch (type) {
      case Event.ONBLUR:
        if (showing) {
          return;
        } else {
          deactivate();
        }
        break;

      case Event.ONCLICK:
        if (isEmbedded() && !isActive()) {
          start(EditorFactory.START_MOUSE_CLICK);
        } else if (!showing) {
          clearDisplay();
          askOracle();
        }
        break;

      case Event.ONKEYDOWN:
        if (isEmbedded() && !isActive()) {
          int keyCode = event.getKeyCode();

          switch (keyCode) {
            case KeyCodes.KEY_BACKSPACE:
              if (!BeeUtils.isEmpty(getDisplayValue())) {
                start(SHOW_SELECTOR);
              }
              consumed = true;
              break;
            case KeyCodes.KEY_DELETE:
              consumed = true;
              if (isNullable() && !BeeUtils.isEmpty(getDisplayValue())) {
                setSelection(null);
              }
              break;
          }

        } else {
          inputEvents.onKeyDown(event);
          consumed = inputEvents.isConsumed();
        }
        break;

      case Event.ONKEYPRESS:
        if (isEmbedded() && !isActive()) {
          consumed = true;
          int charCode = event.getCharCode();

          if (charCode > BeeConst.CHAR_SPACE
              && Codec.isValidUnicodeChar(BeeUtils.toChar(charCode))) {
            start(charCode);
            consumed = true;
          }
        } else {
          consumed = inputEvents.isConsumed();
        }
        break;

      case Event.ONKEYUP:
        inputEvents.onKeyUp(event);
        consumed = inputEvents.isConsumed();
        break;
    }

    if (consumed) {
      event.preventDefault();
      inputEvents.consume();
    } else {
      super.onBrowserEvent(event);
    }
  }

  public void setAccessKey(char key) {
    getInput().setAccessKey(key);
  }

  public void setAdditionalFilter(Filter additionalFilter) {
    if (Objects.equal(additionalFilter, getOracle().getAdditionalFilter())) {
      return;
    }
    getInput().removeStyleName(STYLE_EMPTY);
    
    getOracle().setAdditionalFilter(additionalFilter);
  }
  
  public void setDisplayValue(String value) {
    getInput().setValue(value, false);
  }

  public void setEditing(boolean editing) {
    getInput().setEditing(editing);
  }

  public void setEnabled(boolean enabled) {
    getInput().setEnabled(enabled);
  }

  public void setFocus(boolean focused) {
    getInput().setFocus(focused);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setNullable(boolean nullable) {
    getInput().setNullable(nullable);
  }

  public void setTabIndex(int index) {
    getInput().setTabIndex(index);
  }

  public void setValue(String newValue) {
    setEditorValue(newValue);
  }

  public void setValue(String value, boolean fireEvents) {
    setEditorValue(value);
  }

  public void setVisibleLines(int lines) {
    this.visibleLines = lines;
  }

  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
    SelectorEvent.fire(this, State.OPEN);

    setSelectedRow(null);
    if (!isEmbedded()) {
      setEditorValue(oldValue);
    }

    setLastRequest(null);
    setOffset(0);

    if (charCode != BeeConst.CHAR_SPACE && Codec.isValidUnicodeChar(charCode)) {
      if (charCode == SHOW_SELECTOR) {
        clearDisplay();
      } else {
        setDisplayValue(BeeUtils.toString(charCode));
      }
      if (isInstant()) {
        askOracle();
      }

    } else if (!isEmbedded()) {
      String text = (sourceElement == null) ? null : sourceElement.getInnerText();
      if (BeeUtils.isEmpty(text)) {
        clearDisplay();
      } else {
        setDisplayValue(text.trim());
        getInput().selectAll();
      }
    }

    setActive(true);
  }

  public String validate() {
    return null;
  }

  @Override
  protected void onUnload() {
    if (!BeeKeeper.getScreen().isTemporaryDetach()) {
      getOracle().onUnload();
    }
    super.onUnload();
  }

  private void addCells(Element rowElement, BeeRow row) {
    for (int i = 0; i < getColumnCount(); i++) {
      String cellContent = getCellRenderers().get(i).render(row);
      SelectorColumn selectorColumn = getSelectorColumns().get(i);

      TableCellElement cellElement = DomUtils.createTableCell();
      cellElement.addClassName(STYLE_CELL);

      Element contentElement = DomUtils.createSpan(cellContent);
      contentElement.addClassName(STYLE_CONTENT);

      if (selectorColumn != null) {
        StyleUtils.updateAppearance(contentElement, selectorColumn.getClasses(),
            selectorColumn.getStyle());

        UiHelper.setHorizontalAlignment(cellElement, selectorColumn.getHorAlign());
        UiHelper.setVerticalAlignment(cellElement, selectorColumn.getVertAlign());
      }

      cellElement.appendChild(contentElement);
      rowElement.appendChild(cellElement);
    }
  }

  private void addClassToCell(MenuItem item, String className) {
    Element element = DomUtils.getParentCell(item, true);
    if (DomUtils.isTableCellElement(element)) {
      element.addClassName(className);
    }
  }

  private void addItem(MenuBar menu, final BeeRow row) {
    MenuCommand menuCommand = new MenuCommand() {
      @Override
      public void execute() {
        setSelection(row);
      }
    };

    MenuItem item;
    if (isTableMode()) {
      item = new MenuItem(menu, null, ITEM_TYPE.ROW, menuCommand);
      addCells(item.getElement(), row);
    } else {
      item = new MenuItem(menu, renderItem(row), menuCommand);
    }

    menu.addItem(item);

    item.addStyleName(STYLE_ITEM);
    if (isTableMode()) {
      item.addStyleName(STYLE_ROW);
    } else {
      addClassToCell(item, STYLE_CELL);
    }
  }

  private void askOracle() {
    String query = BeeUtils.trim(getDisplayValue());
    Operator type = getSearchType();
    int start = getOffset();
    int size = getVisibleLines();

    if (getLastRequest() != null) {
      if (!BeeUtils.equalsTrim(query, getLastRequest().getQuery())
          || type != getLastRequest().getSearchType()) {
        start = 0;
        setOffset(start);
      }
    }

    Request request = new Request(query, type, start, size);
    if (request.equals(getLastRequest())) {
      return;
    }
    setLastRequest(request);

    getInput().addStyleName(STYLE_WAITING);

    getOracle().requestSuggestions(request, getCallback());
  }

  private void clearDisplay() {
    setDisplayValue(BeeConst.STRING_EMPTY);
  }

  private void deactivate() {
    setActive(false);

    getInput().removeStyleName(STYLE_WAITING);
    getInput().removeStyleName(STYLE_NOT_FOUND);
  }

  private void exit(boolean hideSelector, State state) {
    exit(hideSelector, state, null, false);
  }

  private void exit(boolean hideSelector, State state, Integer keyCode, boolean hasModifiers) {
    if (hideSelector) {
      getSelector().hide();
    }
    deactivate();

    SelectorEvent.fire(this, state);
    fireEvent(new EditStopEvent(state, keyCode, hasModifiers));
  }

  private Callback getCallback() {
    return callback;
  }

  private Map<Integer, AbstractCellRenderer> getCellRenderers() {
    return cellRenderers;
  }

  private List<String> getChoiceColumns() {
    return choiceColumns;
  }

  private int getColumnCount() {
    return getChoiceColumns().size();
  }

  private String getDisplayValue() {
    return getInput().getText();
  }

  private String getEditorValue() {
    return editorValue;
  }

  private InputText getInput() {
    return input;
  }

  private Request getLastRequest() {
    return lastRequest;
  }

  private int getOffset() {
    return offset;
  }

  private AbstractCellRenderer getRowRenderer() {
    return rowRenderer;
  }

  private Operator getSearchType() {
    return searchType;
  }

  private Selector getSelector() {
    return selector;
  }

  private Map<Integer, SelectorColumn> getSelectorColumns() {
    return selectorColumns;
  }

  private boolean hasMore() {
    return hasMore;
  }

  private void initCellRenderers(DataInfo viewInfo) {
    if (!getCellRenderers().isEmpty()) {
      getCellRenderers().clear();
    }

    for (Map.Entry<Integer, SelectorColumn> entry : getSelectorColumns().entrySet()) {
      SelectorColumn sc = entry.getValue();
      AbstractCellRenderer renderer = RendererFactory.getRenderer(sc.getRendererDescription(),
          sc.getRender(), sc.getItemKey(), sc.getRenderColumns(), viewInfo.getColumns(),
          viewInfo.getColumnIndex(sc.getSource()));

      if (renderer != null) {
        getCellRenderers().put(entry.getKey(), renderer);
      }
    }

    if (getCellRenderers().isEmpty() && getColumnCount() == 1 && getRowRenderer() != null) {
      getCellRenderers().put(0, getRowRenderer());
    }

    for (int i = 0; i < getColumnCount(); i++) {
      if (!getCellRenderers().containsKey(i)) {
        int index = viewInfo.getColumnIndex(getChoiceColumns().get(i));
        AbstractCellRenderer renderer = new SimpleRenderer(index, viewInfo.getColumns().get(index));
        getCellRenderers().put(i, renderer);
      }
    }
  }

  private boolean isActive() {
    return active;
  }

  private boolean isInstant() {
    Operator operator = getSearchType();
    return Operator.CONTAINS.equals(operator) || Operator.STARTS.equals(operator);
  }

  private boolean isTableMode() {
    return tableMode;
  }

  private void nextOffset() {
    if (hasMore()) {
      setOffset(getOffset() + 1);
      askOracle();
    }
  }

  private void nextPage() {
    if (hasMore()) {
      setOffset(getOffset() + getVisibleLines());
      askOracle();
    }
  }

  private void prevOffset() {
    if (getOffset() > 0) {
      setOffset(getOffset() - 1);
      askOracle();
    }
  }

  private void prevPage() {
    if (getOffset() > 0) {
      setOffset(Math.max(getOffset() - getVisibleLines(), 0));
      askOracle();
    }
  }

  private String renderItem(BeeRow row) {
    return getRowRenderer().render(row);
  }

  private void setActive(boolean active) {
    this.active = active;
  }

  private void setEditorValue(String editorValue) {
    this.editorValue = editorValue;
  }

  private void setHasMore(boolean hasMore) {
    this.hasMore = hasMore;
  }

  private void setLastRequest(Request lastRequest) {
    this.lastRequest = lastRequest;
  }

  private void setOffset(int offset) {
    this.offset = offset;
  }

  private void setSelectedRow(BeeRow row) {
    this.selectedRow = row;
  }

  private void setSelection(BeeRow row) {
    setSelectedRow(row);
    setEditorValue(row == null ? null : BeeUtils.toString(row.getId()));

    getSelector().hide();
    deactivate();

    SelectorEvent.fire(this, State.CHANGED);
    fireEvent(new EditStopEvent(State.CHANGED, KeyCodes.KEY_TAB, false));
  }

  private void start(int keyCode) {
    startEdit(null, BeeUtils.toChar(keyCode), null, null);
  }
}