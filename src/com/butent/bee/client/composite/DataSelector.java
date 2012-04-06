package com.butent.bee.client.composite;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HandlesAllKeyEvents;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.SelectionOracle;
import com.butent.bee.client.data.SelectionOracle.Callback;
import com.butent.bee.client.data.SelectionOracle.Request;
import com.butent.bee.client.data.SelectionOracle.Response;
import com.butent.bee.client.data.SelectionOracle.SelectionColumn;
import com.butent.bee.client.data.SelectionOracle.Suggestion;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.menu.MenuBar;
import com.butent.bee.client.menu.MenuCommand;
import com.butent.bee.client.menu.MenuItem;
import com.butent.bee.client.utils.JsonUtils;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.HasTextDimensions;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

/**
 * Enables using user interface component for entering text entries while the system is suggesting
 * possible matching values from the list.
 */

public class DataSelector extends Complex implements Editor, HasTextDimensions {

  /**
   * Handles suggestion display.
   */

  private class Display {

    private final MenuBar menu;
    private final Popup popup;

    private Boolean pendingSelection = null;
    private boolean ready = false;

    private Display(UIObject partner) {
      this.menu = new MenuBar(0, true);
      menu.addStyleName(getStyle(STYLE_MENU));

      this.popup = new Popup(true, false);
      popup.setStyleName(getStyle(STYLE_POPUP));
      popup.setWidget(menu);

      popup.addAutoHidePartner(partner.getElement());
      popup.addCloseHandler(new CloseHandler<PopupPanel>() {
        public void onClose(CloseEvent<PopupPanel> event) {
          if (event.isAutoClosed()) {
            getMenu().clearItems();
            exit(false);
          }
        }
      });
    }

    private void addClassToCell(MenuItem item, String className) {
      if (item == null || BeeUtils.isEmpty(className)) {
        return;
      }

      Element parent = item.getElement().getParentElement();
      if (DomUtils.isTableCellElement(parent)) {
        parent.addClassName(className);
      }
    }

    private void cancelSelection() {
      getMenu().selectItem(null);
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
          if (it instanceof SuggestionItem) {
            item = it;
            break;
          }
        }
      }
      if (item == null || item.getCommand() == null) {
        return false;
      }

      if (item instanceof NavigationItem) {
        setPendingSelection(!((NavigationItem) item).isNext());
      }
      item.getCommand().execute();
      return true;
    }

    private void hide() {
      getPopup().hide();
    }

    private boolean isItemSelected() {
      if (isShowing()) {
        return getMenu().getSelectedItem() != null;
      } else {
        return false;
      }
    }

    private boolean isReady() {
      return ready;
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

    private boolean selectCurrentSugestion() {
      if (!isShowing()) {
        return false;
      }
      MenuItem item = getMenu().getSelectedItem();
      if (item instanceof SuggestionItem) {
        item.getCommand().execute();
        return true;
      }
      return false;
    }

    private void setPendingSelection(Boolean pendingSelection) {
      this.pendingSelection = pendingSelection;
    }

    private void setReady(boolean ready) {
      this.ready = ready;
    }

    private void showSuggestions(Response response, UIObject target) {
      Collection<Suggestion> suggestions = response.getSuggestions();
      if (BeeUtils.isEmpty(suggestions)) {
        getPopup().hide();
        return;
      }

      getMenu().clearItems();
      MenuItem prev = null;
      MenuItem next = null;
      if (getOffset() > 0) {
        prev = getMenu().addItem(new NavigationItem(getMenu(), false));
        addClassToCell(prev, getStyle(STYLE_NAVIGATION_CELL));
      }
      for (Suggestion suggestion : suggestions) {
        MenuItem item = getMenu().addItem(new SuggestionItem(getMenu(), suggestion));
        addClassToCell(item, getStyle(STYLE_ITEM_CELL));
      }
      if (response.hasMoreSuggestions()) {
        next = getMenu().addItem(new NavigationItem(getMenu(), true));
        addClassToCell(next, getStyle(STYLE_NAVIGATION_CELL));
      }

      if (getPendingSelection() != null) {
        MenuItem item = getPendingSelection() ? next : prev;
        if (item != null) {
          getMenu().selectItem(item);
        }
        setPendingSelection(null);
      }
      
      if (!isReady()) {
        setReady(true);
      }
      getPopup().showRelativeTo(target);
    }

    private void start() {
      setReady(false);
    }
  }

  private class InputEvents extends HandlesAllKeyEvents implements MouseWheelHandler {

    public void onKeyDown(KeyDownEvent event) {
      if (!isEnabled()) {
        return;
      }

      int keyCode = event.getNativeKeyCode();
      boolean hasModifiers = EventUtils.hasModifierKey(event.getNativeEvent());

      if (isEmbedded() && !getDisplay().isShowing()) {
        if (keyCode == KeyCodes.KEY_DELETE && hasModifiers && isNullable()) {
          event.preventDefault();
          setValue(BeeConst.STRING_EMPTY);
          setSelectedValue(null);
        }
        return;
      }

      switch (keyCode) {
        case KeyCodes.KEY_DOWN:
          event.preventDefault();
          if (hasModifiers && hasMore()) {
            nextOffset();
          } else {
            getDisplay().moveSelectionDown();
          }
          break;

        case KeyCodes.KEY_UP:
          event.preventDefault();
          if (hasModifiers && getOffset() > 0) {
            prevOffset();
          } else {
            getDisplay().moveSelectionUp();
          }
          break;

        case KeyCodes.KEY_PAGEDOWN:
          if (hasMore()) {
            event.preventDefault();
            nextPage();
          }
          break;

        case KeyCodes.KEY_PAGEUP:
          if (getOffset() > 0) {
            event.preventDefault();
            prevPage();
          }
          break;

        case KeyCodes.KEY_ESCAPE:
          event.preventDefault();
          if (getDisplay().isItemSelected()) {
            getDisplay().cancelSelection();
          } else {
            exit(true);
          }
          break;

        case KeyCodes.KEY_ENTER:
          if (getDisplay().handleKeyboardSelection(isInstant(), hasModifiers)) {
            event.preventDefault();
            event.stopPropagation();
          }
          break;

        case KeyCodes.KEY_TAB:
          event.preventDefault();
          if (!getDisplay().selectCurrentSugestion()) {
            exit(true);
          }
          break;
          
        case 113:
          event.preventDefault();
          getOracle().rotateCaching();
          setLastRequest(null);
          askOracle();
      }
    }

    public void onKeyPress(KeyPressEvent event) {
    }

    public void onKeyUp(KeyUpEvent event) {
      if (!isEnabled()) {
        return;
      }
      int keyCode = event.getNativeKeyCode();

      if (isEmbedded() && !getDisplay().isShowing()) {
        if (BeeUtils.inList(keyCode, KeyCodes.KEY_UP, KeyCodes.KEY_DOWN, KeyCodes.KEY_TAB,
            KeyCodes.KEY_ESCAPE)) {
          return;
        }
        if (keyCode == KeyCodes.KEY_ENTER && isInstant()) {
          return;
        }
      }

      if (isInstant() || keyCode == KeyCodes.KEY_ENTER) {
        askOracle();
      }
    }

    public void onMouseWheel(MouseWheelEvent event) {
      if (!isEnabled()) {
        return;
      }
      int y = event.getDeltaY();
      if (y > 0) {
        nextOffset();
      } else if (y < 0) {
        prevOffset();
      }
    }
  }

  private class LimitEvents implements ValueChangeHandler<String> {
    public void onValueChange(ValueChangeEvent<String> event) {
      if (isEnabled()) {
        askOracle();
      }
    }
  }

  private class NavigationItem extends MenuItem {

    final boolean next;

    private NavigationItem(MenuBar parent, boolean next) {
      super(parent, next ? ITEM_NEXT : ITEM_PREV);
      this.next = next;

      setCommand(new MenuCommand() {
        @Override
        public void execute() {
          if (isNext()) {
            nextPage();
          } else {
            prevPage();
          }
        }
      });
      addStyleName(getStyle(STYLE_NAVIGATION));
    }

    private boolean isNext() {
      return next;
    }
  }

  private class SearchTypeEvents implements ClickHandler {
    public void onClick(ClickEvent event) {
      if (!isEnabled()) {
        return;
      }
      Operator[] constants = Operator.class.getEnumConstants();
      Operator oldType = getSearchType();
      int index = -1;
      int start = 0;

      if (oldType != null) {
        start = oldType.ordinal() + 1;
      }
      for (int i = start; i < constants.length; i++) {
        if (!BeeUtils.isEmpty(constants[i].toTextString())) {
          index = i;
          break;
        }
      }
      setSearchType(index < 0 ? null : constants[index]);

      if (event.getSource() instanceof UIObject) {
        ((UIObject) event.getSource()).getElement().setInnerHTML(getSearchSymbol());
      }
      askOracle();
      getInput().setFocus(true);
    }
  }

  private class SuggestionItem extends MenuItem {

    private final long rowId;
    private final String relValue;

    private SuggestionItem(MenuBar parent, Suggestion suggestion) {
      super(parent, suggestion.getDisplayString());
      this.rowId = suggestion.getRowId();
      this.relValue = suggestion.getRelValue();

      MenuCommand menuCommand = new MenuCommand() {
        @Override
        public void execute() {
          setSelection(getRowId(), getRelValue());
        }
      };
      setCommand(menuCommand);
      addStyleName(getStyle(STYLE_ITEM));
    }

    private String getRelValue() {
      return relValue;
    }

    private long getRowId() {
      return rowId;
    }
  }

  private static final String ITEM_PREV = String.valueOf('\u25b2');

  private static final String ITEM_NEXT = String.valueOf('\u25bc');

  private static final String STYLE_SELECTOR = "bee-DataSelector";
  private static final String STYLE_EMBEDDED = "embedded";

  private static final String STYLE_INPUT = "input";
  private static final String STYLE_TYPE = "type";
  private static final String STYLE_LIMIT = "limit";

  private static final String STYLE_POPUP = "popup";
  private static final String STYLE_MENU = "menu";
  private static final String STYLE_ITEM = "item";
  private static final String STYLE_ITEM_CELL = "itemCell";
  private static final String STYLE_NAVIGATION = "navigation";
  private static final String STYLE_NAVIGATION_CELL = "navigationCell";

  private static final int DEFAULT_VISIBLE_LINES = 10;
  private static final int DEFAULT_CHARACTER_WIDTH = -1;

  private static final Operator DEFAULT_SEARCH_TYPE = Operator.CONTAINS;

  private static final CachingPolicy DEFAULT_CACHING_POLICY = CachingPolicy.FULL;
  private static final int DEFAULT_CACHING_THRESHOLD = 1000;

  private static final String OPTION_SHOW_LIMIT = "showLimit";
  private static final String OPTION_SEARCH_TYPE = "searchType";
  private static final String OPTION_CACHING = "caching";
  private static final String OPTION_COLUMNS = "columns";

  private final Callback callback = new Callback() {
    public void onSuggestionsReady(Request request, Response response) {
      if (isEditing()) {
        setHasMore(response.hasMoreSuggestions());
        getDisplay().showSuggestions(response, DataSelector.this);
      }
    }
  };

  private int characterWidth = DEFAULT_CHARACTER_WIDTH;
  private int visibleLines = DEFAULT_VISIBLE_LINES;

  private Operator searchType = DEFAULT_SEARCH_TYPE;

  private CachingPolicy cachingPolicy = DEFAULT_CACHING_POLICY;
  private int cachingThreshold = DEFAULT_CACHING_THRESHOLD;

  private final SelectionOracle oracle;

  private final Display display;
  private final InputText input;
  private final InputSpinner limitWidget;

  private final InputEvents inputEvents = new InputEvents();
  private final SearchTypeEvents typeEvents = new SearchTypeEvents();
  private final LimitEvents limitEvents = new LimitEvents();

  private final RelationInfo relationInfo;

  private final List<SelectionColumn> columns = Lists.newArrayList();
  
  private final boolean embedded;

  private String selectedValue = null;

  private Request lastRequest = null;
  private int offset = 0;
  private boolean hasMore = false;
  
  public DataSelector(RelationInfo relationInfo, boolean embedded) {
    this(relationInfo, embedded, null);
  }
  
  public DataSelector(RelationInfo relationInfo, boolean embedded, JSONObject options) {
    super(embedded ? Position.RELATIVE : Position.ABSOLUTE);

    this.relationInfo = relationInfo;
    this.embedded = embedded;

    boolean showLimit = false;
    if (options != null) {
      initOptions(options);
      showLimit = !JsonUtils.isEmpty(options.get(OPTION_SHOW_LIMIT));
    }

    this.oracle = createOracle();

    this.input = new InputText();
    this.display = new Display(this);

    input.addStyleName(getStyle(embedded, STYLE_INPUT));
    add(input);

    InlineLabel label = new InlineLabel(getSearchSymbol());
    label.addStyleName(getStyle(embedded, STYLE_TYPE));
    add(label);

    int right = 2;

    if (showLimit) {
      this.limitWidget = new InputSpinner(DEFAULT_VISIBLE_LINES, 1, 99);
      limitWidget.addStyleName(getStyle(embedded, STYLE_LIMIT));
      add(limitWidget);

      StyleUtils.makeAbsolute(limitWidget);
      StyleUtils.setRight(limitWidget, right);
      right += 40;

      limitWidget.addValueChangeHandler(limitEvents);
    } else {
      this.limitWidget = null;
    }

    StyleUtils.makeAbsolute(label);
    StyleUtils.setRight(label, right);

    StyleUtils.makeAbsolute(input);
    StyleUtils.setRight(input, right + 16);

    setStyleName(getStyle(embedded, null));

    inputEvents.addKeyHandlersTo(input);
    input.addMouseWheelHandler(inputEvents);

    label.addClickHandler(typeEvents);
    
    if (embedded) {
      getDisplay().start();
      setEditing(true);
    }
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return getInput().addBlurHandler(handler);
  }

  public HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }
  
  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return getInput().addFocusHandler(handler);
  }

  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addDomHandler(handler, KeyDownEvent.getType());
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public Callback getCallback() {
    return callback;
  }

  public int getCharacterWidth() {
    return characterWidth;
  }

  public Display getDisplay() {
    return display;
  }

  @Override
  public String getIdPrefix() {
    return "selector";
  }

  public String getNormalizedValue() {
    return getSelectedValue();
  }

  public int getTabIndex() {
    return getInput().getTabIndex();
  }

  public String getValue() {
    return getSelectedValue();
  }

  public int getVisibleLines() {
    if (getLimitWidget() == null) {
      return visibleLines;
    }
    return getLimitWidget().getIntValue();
  }

  public boolean handlesKey(int keyCode) {
    if (isEmbedded()) {
      return getDisplay().isShowing() || keyCode == KeyCodes.KEY_ENTER && !isInstant();
    } else {
      return true;
    }
  }

  public boolean isEditing() {
    return getInput().isEditing();
  }

  public boolean isEnabled() {
    return getInput().isEnabled();
  }

  public boolean isNullable() {
    return getInput().isNullable();
  }

  public void setAccessKey(char key) {
    getInput().setAccessKey(key);
  }

  public void setCharacterWidth(int width) {
    this.characterWidth = width;
  }

  public void setEditing(boolean editing) {
    getInput().setEditing(editing);
  }

  public void setEnabled(boolean enabled) {
    DomUtils.enableChildren(this, enabled);
  }

  public void setFocus(boolean focused) {
    getInput().setFocus(focused);
  }

  public void setNullable(boolean nullable) {
    getInput().setNullable(nullable);
  }

  public void setSelectedValue(String selectedValue) {
    this.selectedValue = selectedValue;
  }

  public void setTabIndex(int index) {
    getInput().setTabIndex(index);
  }

  public void setValue(String newValue) {
    setValue(newValue, false);
  }

  public void setValue(String value, boolean fireEvents) {
    getInput().setValue(value, fireEvents);
  }

  public void setVisibleLines(int lines) {
    if (getLimitWidget() == null) {
      this.visibleLines = lines;
    } else {
      getLimitWidget().setValue(lines);
    }
  }

  public void startEdit(String oldValue, char charCode, EditorAction onEntry) {
    setLastRequest(null);
    setOffset(0);

    getDisplay().start();
    getInput().setFocus(true);

    if (Character.isLetterOrDigit(charCode)) {
      setValue(BeeUtils.toString(charCode));
      if (isInstant()) {
        askOracle();
      }
    } else {
      if (isInstant() || BeeUtils.isEmpty(getInput().getValue())) {
        setValue(BeeConst.STRING_EMPTY);
      } else {
        getInput().selectAll();
      }
    }
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

  private void askOracle() {
    String query = BeeUtils.trim(getInput().getText());
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

    getOracle().requestSuggestions(request, getCallback());
  }

  private SelectionOracle createOracle() {
    return new SelectionOracle(getRelationInfo(), getColumns(), getCachingPolicy(),
        getCachingThreshold());
  }

  private void exit(boolean hideDisplay) {
    if (hideDisplay) {
      getDisplay().hide();
    }
    fireEvent(new EditStopEvent(State.CANCELED));
  }

  private CachingPolicy getCachingPolicy() {
    return cachingPolicy;
  }

  private int getCachingThreshold() {
    return cachingThreshold;
  }

  private List<SelectionColumn> getColumns() {
    return columns;
  }

  private InputText getInput() {
    return input;
  }

  private Request getLastRequest() {
    return lastRequest;
  }

  private InputSpinner getLimitWidget() {
    return limitWidget;
  }

  private int getOffset() {
    return offset;
  }

  private SelectionOracle getOracle() {
    return oracle;
  }

  private RelationInfo getRelationInfo() {
    return relationInfo;
  }

  private String getSearchSymbol() {
    if (getSearchType() == null) {
      return "?";
    }
    return getSearchType().toTextString();
  }

  private Operator getSearchType() {
    return searchType;
  }
  
  private String getSelectedValue() {
    return selectedValue;
  }

  private String getStyle(boolean emb, String suffix) {
    return BeeUtils.concat(BeeConst.CHAR_MINUS, STYLE_SELECTOR,
        emb ? STYLE_EMBEDDED : BeeConst.STRING_EMPTY, suffix);
  }

  private String getStyle(String suffix) {
    return BeeUtils.concat(BeeConst.CHAR_MINUS, STYLE_SELECTOR, suffix);
  }

  private boolean hasMore() {
    return hasMore;
  }
  
  private void initOptions(JSONObject options) {
    if (options == null) {
      return;
    }

    if (options.containsKey(OPTION_SEARCH_TYPE)) {
      String search = JsonUtils.getString(options, OPTION_SEARCH_TYPE);
      if (!BeeUtils.isEmpty(search)) {
        Operator type = BeeUtils.getConstant(Operator.class, search);
        if (type != null) {
          setSearchType(type);
        }
      }
    }

    if (options.containsKey(OPTION_CACHING)) {
      JSONValue caching = options.get(OPTION_CACHING);
      if (caching != null) {
        if (caching.isBoolean() != null) {
          setCachingThreshold(BeeConst.UNDEF);
          setCachingPolicy(caching.isBoolean().booleanValue()
              ? CachingPolicy.FULL : CachingPolicy.NONE);

        } else if (caching.isNumber() != null) {
          setCachingPolicy(CachingPolicy.FULL);
          setCachingThreshold(BeeUtils.toInt(caching.isNumber().doubleValue()));

        } else if (caching.isString() != null) {
          String s = caching.isString().stringValue();
          if (BeeUtils.isInt(s)) {
            setCachingThreshold(BeeUtils.toInt(s));
          } else {
            CachingPolicy policy = CachingPolicy.get(s);
            if (policy != null) {
              setCachingPolicy(policy);
            }
          }
        }
      }
    }

    if (options.containsKey(OPTION_COLUMNS)) {
      JSONValue cols = options.get(OPTION_COLUMNS);
      List<String> colList = Lists.newArrayList();

      if (cols != null) {
        if (cols.isString() != null) {
          colList.add(cols.isString().stringValue());
        } else if (cols.isArray() != null) {
          for (int i = 0; i < cols.isArray().size(); i++) {
            colList.add(JsonUtils.toString(cols.isArray().get(i)));
          }
        }
      }

      String name;
      boolean searchable;
      for (String column : colList) {
        if (BeeUtils.isEmpty(column)) {
          continue;
        }

        if (BeeUtils.isPrefixOrSuffix(column, BeeConst.CHAR_MINUS)) {
          name = BeeUtils.removePrefixAndSuffix(column, BeeConst.CHAR_MINUS);
          searchable = false;
        } else {
          name = column.trim();
          searchable = true;
        }

        if (!BeeUtils.isEmpty(name)) {
          getColumns().add(new SelectionColumn(name, searchable));
        }
      }
    }
  }

  private boolean isEmbedded() {
    return embedded;
  }

  private boolean isInstant() {
    Operator type = getSearchType();
    if (type == null) {
      return false;
    } else {
      return type.isStringOperator();
    }
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

  private void setCachingPolicy(CachingPolicy cachingPolicy) {
    this.cachingPolicy = cachingPolicy;
  }

  private void setCachingThreshold(int cachingThreshold) {
    this.cachingThreshold = cachingThreshold;
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

  private void setSearchType(Operator searchType) {
    this.searchType = searchType;
  }

  private void setSelection(long rowId, String relValue) {
    setSelectedValue(BeeUtils.toString(rowId));
    getDisplay().hide();
    if (isEmbedded()) {
      getInput().setValue(relValue);
    }
    fireEvent(new EditStopEvent(State.CHANGED));
  }
}