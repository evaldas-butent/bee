package com.butent.bee.client.composite;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.HasRelatedRow;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.data.SelectionOracle;
import com.butent.bee.client.data.SelectionOracle.Callback;
import com.butent.bee.client.data.SelectionOracle.Request;
import com.butent.bee.client.data.SelectionOracle.Response;
import com.butent.bee.client.data.SelectionOracle.Suggestion;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.CloseEvent;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.menu.MenuBar;
import com.butent.bee.client.menu.MenuCommand;
import com.butent.bee.client.menu.MenuItem;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.EnumRenderer;
import com.butent.bee.client.render.RendererFactory;
import com.butent.bee.client.render.SimpleRenderer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.HasTextBox;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumable;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.ViewColumn;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.menu.MenuConstants;
import com.butent.bee.shared.menu.MenuConstants.BAR_TYPE;
import com.butent.bee.shared.menu.MenuConstants.ITEM_TYPE;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.HasCapsLock;
import com.butent.bee.shared.ui.HasVisibleLines;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.ui.SelectorColumn;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Enables using user interface component for entering text entries while the system is suggesting
 * possible matching values from the list.
 */

public class DataSelector extends Composite implements Editor, HasVisibleLines, HasTextBox,
    HasRelatedRow, HasCapsLock {

  protected final class InputWidget extends InputText {

    private InputWidget() {
      super();

      addMouseWheelHandler(inputEvents);

      sinkEvents(Event.ONBLUR | Event.ONCLICK | Event.KEYEVENTS);
    }

    @Override
    public void onBrowserEvent(Event event) {
      boolean showing = getSelector().isShowing();
      int type = event.getTypeInt();

      boolean consumed = false;

      switch (type) {
        case Event.ONBLUR:
          if (showing || isAdding()) {
            return;
          } else {
            reset();
          }
          break;

        case Event.ONCLICK:
          onMouseClick(false);
          break;

        case Event.ONKEYDOWN:
          if (isEmbedded() && event.getKeyCode() == KeyCodes.KEY_DELETE && isNullable()
              && !BeeUtils.isEmpty(getDisplayValue())) {
            setSelection(null);
            consumed = true;

          } else if (isEmbedded() && !isActive()) {
            if (event.getKeyCode() == KeyCodes.KEY_BACKSPACE
                && !BeeUtils.isEmpty(getDisplayValue())) {
              start(SHOW_SELECTOR);
              consumed = true;
            }

          } else {
            inputEvents.onKeyDown(event);
            consumed = inputEvents.isConsumed();
          }
          break;

        case Event.ONKEYPRESS:
          if (isEmbedded() && !isActive()) {
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

    protected void onMouseClick(boolean alwaysAsk) {
      if (isEmbedded() && !isActive()) {
        start(EditStartEvent.CLICK);
        if (!alwaysAsk) {
          return;
        }
      }

      if (!getSelector().isShowing()) {
        clearDisplay();
        askOracle();
      }
    }
  }

  private class InputEvents implements MouseWheelHandler, Consumable {

    private boolean consumed;

    @Override
    public void consume() {
      setConsumed(true);
    }

    @Override
    public boolean isConsumed() {
      return consumed;
    }

    @Override
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

    @Override
    public void setConsumed(boolean consumed) {
      this.consumed = consumed;
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
          if (getSelector().isShowing()) {
            event.stopPropagation();
          }
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
          } else if (!isWaiting() && isNewRowEnabled()) {
            RowFactory.createRelatedRow(DataSelector.this);
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
  }

  /**
   * Handles suggestion display.
   */

  private final class Selector {

    private final MenuBar menu;
    private final Popup popup;

    private MenuItem itemPrev;
    private MenuItem itemNext;

    private Boolean pendingSelection;

    private Selector(ITEM_TYPE itemType, Element partner, String selectorClass) {
      this.menu = new MenuBar(MenuConstants.ROOT_MENU_INDEX, true, BAR_TYPE.TABLE, itemType);

      menu.addStyleName(STYLE_MENU);

      this.popup = new Popup(OutsideClick.CLOSE, STYLE_POPUP);
      if (!BeeUtils.isEmpty(selectorClass)) {
        popup.addStyleName(selectorClass);
      }

      popup.setWidget(menu);

      popup.setKeyboardPartner(partner);

      popup.addCloseHandler(new CloseEvent.Handler() {
        @Override
        public void onClose(CloseEvent event) {
          if (event.isUserCaused()) {
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
      getPopup().close();
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
        getPopup().close();
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

      getPopup().showRelativeTo(target.getElement());
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

  private static final String STYLE_EDITABLE_CONTAINER = STYLE_SELECTOR + "-editableContainer";
  private static final String STYLE_EDITABLE_ACTIVE = STYLE_SELECTOR + "-editable-active";
  private static final String STYLE_EDITABLE_INPUT = STYLE_SELECTOR + "-editableInput";

  private static final String STYLE_DRILL = STYLE_SELECTOR + "-drill";
  private static final String STYLE_DRILL_DISABBLED = STYLE_DRILL + "-disabled";

  private static final int DEFAULT_VISIBLE_LINES = 10;

  private static final Operator DEFAULT_SEARCH_TYPE = Operator.CONTAINS;

  private static final char SHOW_SELECTOR = '*';

  private final Callback callback = new Callback() {
    @Override
    public void onSuggestionsReady(Request request, Response response) {
      DataSelector.this.getInput().removeStyleName(STYLE_WAITING);

      boolean found = !response.isEmpty();
      DataSelector.this.getInput().setStyleName(STYLE_NOT_FOUND, !found);
      if (request.isEmpty()) {
        DataSelector.this.setAlive(found);
      }

      if (isEditing()) {
        setHasMore(response.hasMoreSuggestions());
        getSelector().showSuggestions(response, DataSelector.this);
      }
      DataSelector.this.setWaiting(false);
    }
  };

  private int visibleLines = DEFAULT_VISIBLE_LINES;

  private final SelectionOracle oracle;
  private final Operator searchType;

  private final InputWidget input;
  private final Selector selector;

  private final List<String> choiceColumns = Lists.newArrayList();
  private final Map<Integer, SelectorColumn> selectorColumns = Maps.newHashMap();

  private final boolean tableMode;

  private final AbstractCellRenderer rowRenderer;
  private final Map<Integer, AbstractCellRenderer> cellRenderers = Maps.newHashMap();

  private final InputEvents inputEvents = new InputEvents();

  private final boolean embedded;

  private final String newRowForm;
  private final String newRowColumns;
  private final String newRowCaption;
  private final boolean newRowEnabled;

  private final String editViewName;

  private final String editForm;
  private final Boolean editModal;
  private final boolean editEnabled;

  private final int editTargetIndex;
  private final int editSourceIndex;

  private final String relationLabel;

  private Long editRowId;

  private boolean active;

  private BeeRow relatedRow;
  private String editorValue;

  private Request lastRequest;
  private int offset;
  private boolean hasMore;

  private boolean alive = true;
  private boolean waiting;
  private boolean adding;

  private Widget drill;

  private String options;

  private boolean handlesTabulation;

  public DataSelector(Relation relation, boolean embedded) {
    super();

    this.embedded = embedded;

    DataInfo dataInfo = Data.getDataInfo(relation.getViewName());
    this.oracle = new SelectionOracle(relation, dataInfo);
    this.searchType =
        (relation.getOperator() == null) ? DEFAULT_SEARCH_TYPE : relation.getOperator();

    ITEM_TYPE itemType = relation.getItemType();

    this.input = new InputWidget();
    this.selector = new Selector(itemType, this.input.getElement(), relation.getSelectorClass());

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

    int dataIndex = (size == 1) ? dataInfo.getColumnIndex(choiceColumns.get(0)) : BeeConst.UNDEF;
    BeeColumn dataColumn =
        BeeConst.isUndef(dataIndex) ? null : dataInfo.getColumns().get(dataIndex);

    CellSource cellSource =
        (dataColumn == null) ? null : CellSource.forColumn(dataColumn, dataIndex);

    this.rowRenderer = RendererFactory.getRenderer(relation.getRowRendererDescription(),
        relation.getRowRender(), relation.getRowRenderTokens(), relation.getItemKey(),
        choiceColumns, dataInfo.getColumns(), cellSource);

    if (rowRenderer instanceof EnumRenderer && relation.getSearchableColumns().size() == 1) {
      oracle.createTranslator(((EnumRenderer) rowRenderer).getCaptions(),
          ((EnumRenderer) rowRenderer).getValueStartIndex());
    }

    oracle.addRowCountChangeHandler(new Consumer<Integer>() {
      @Override
      public void accept(Integer parameter) {
        DataSelector.this.setAlive(parameter > 0);
      }
    });

    oracle.addDataReceivedHandler(new Consumer<BeeRowSet>() {
      @Override
      public void accept(BeeRowSet rowSet) {
        if (!DataUtils.isEmpty(rowSet)) {
          SelectorEvent.fire(DataSelector.this, State.LOADED);
        }
      }
    });

    if (tableMode) {
      initCellRenderers(dataInfo);
      getSelector().getMenu().addStyleName(STYLE_TABLE);
    }

    if (BeeUtils.isPositive(relation.getVisibleLines())) {
      setVisibleLines(relation.getVisibleLines());
    }

    this.newRowForm = BeeUtils.notEmpty(relation.getNewRowForm(), dataInfo.getNewRowForm());
    this.newRowColumns = BeeUtils.notEmpty(relation.getNewRowColumns(),
        dataInfo.getNewRowColumns());
    this.newRowCaption = BeeUtils.notEmpty(relation.getNewRowCaption(),
        dataInfo.getNewRowCaption());
    this.newRowEnabled = relation.isNewRowEnabled();

    if (relation.isEditEnabled(true)) {
      String es = relation.getEditSource();
      String ev = relation.getEditViewName();

      String targetViewName = relation.getTargetViewName();
      DataInfo editDataInfo;

      if (!BeeUtils.isEmpty(es) && !BeeUtils.isEmpty(targetViewName)) {
        editDataInfo = Data.getDataInfo(targetViewName);
      } else {
        editDataInfo = dataInfo;
      }

      if (BeeUtils.isEmpty(ev)) {
        if (BeeUtils.isEmpty(es)) {
          ev = relation.getViewName();
        } else if (editDataInfo != null) {
          ev = editDataInfo.getRelationView(es);
        }
      }

      this.editViewName = ev;

      String ef = relation.getEditForm();

      if (BeeUtils.isEmpty(ef) && !BeeUtils.isEmpty(ev)) {
        if (BeeUtils.same(ev, dataInfo.getViewName())) {
          ef = RowEditor.getFormName(null, dataInfo);
        } else {
          ef = RowEditor.getFormName(null, Data.getDataInfo(ev));
        }
      }

      this.editForm = ef;

      this.editModal = relation.isEditModal();
      this.editEnabled = !BeeUtils.isEmpty(ev) && !BeeUtils.isEmpty(ef);

      int etIndex = BeeConst.UNDEF;
      int esIndex = BeeConst.UNDEF;

      if (!BeeUtils.isEmpty(es) && editDataInfo != null) {
        if (BeeUtils.isEmpty(targetViewName)) {
          esIndex = editDataInfo.getColumnIndex(es);

        } else {
          etIndex = editDataInfo.getColumnIndex(es);

          ViewColumn vc = editDataInfo.getViewColumn(es);
          if (vc != null) {
            esIndex = dataInfo.getColumnIndexBySource(vc.getTable(), vc.getField(),
                ViewColumn.VISIBLE);
          }
        }
      }

      this.editTargetIndex = etIndex;
      this.editSourceIndex = esIndex;

    } else {
      this.editViewName = null;

      this.editForm = null;
      this.editModal = null;
      this.editEnabled = false;

      this.editTargetIndex = BeeConst.UNDEF;
      this.editSourceIndex = BeeConst.UNDEF;
    }

    this.relationLabel = Localized.maybeTranslate(relation.getLabel());

    Binder.addMouseWheelHandler(selector.getPopup(), inputEvents);

    if (dataColumn != null && ValueType.isString(dataColumn.getType())
        && dataColumn.getPrecision() > 0) {
      input.setMaxLength(dataColumn.getPrecision());
    }

    init(input, embedded);
  }

  @Override
  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return getInput().addDomHandler(handler, BlurEvent.getType());
  }

  @Override
  public HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  @Override
  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return getInput().addDomHandler(handler, FocusEvent.getType());
  }

  @Override
  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return getInput().addDomHandler(handler, KeyDownEvent.getType());
  }

  public HandlerRegistration addSelectorHandler(SelectorEvent.Handler handler) {
    return addHandler(handler, SelectorEvent.getType());
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public void clearDisplay() {
    setDisplayValue(BeeConst.STRING_EMPTY);
  }

  @Override
  public void clearValue() {
    setValue(null);
    clearDisplay();
  }

  public List<String> getChoiceColumns() {
    return choiceColumns;
  }

  @Override
  public EditorAction getDefaultFocusAction() {
    return EditorAction.SELECT;
  }

  public String getDisplayValue() {
    return getInput().getText();
  }

  public String getEditForm() {
    return editForm;
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "selector";
  }

  public String getNewRowCaption() {
    return newRowCaption;
  }

  public String getNewRowColumns() {
    return newRowColumns;
  }

  public String getNewRowForm() {
    return newRowForm;
  }

  @Override
  public String getNormalizedValue() {
    return getEditorValue();
  }

  @Override
  public String getOptions() {
    return options;
  }

  public SelectionOracle getOracle() {
    return oracle;
  }

  @Override
  public Long getRelatedId() {
    return (getRelatedRow() == null) ? null : getRelatedRow().getId();
  }

  @Override
  public BeeRow getRelatedRow() {
    return relatedRow;
  }

  @Override
  public int getTabIndex() {
    return getInput().getTabIndex();
  }

  @Override
  public TextBoxBase getTextBox() {
    return getInput();
  }

  @Override
  public String getValue() {
    return getEditorValue();
  }

  @Override
  public int getVisibleLines() {
    return visibleLines;
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.DATA_SELECTOR;
  }

  @Override
  public boolean handlesKey(int keyCode) {
    return isActive();
  }

  @Override
  public boolean handlesTabulation() {
    return handlesTabulation;
  }

  public boolean isAdding() {
    return adding;
  }

  public boolean isEditEnabled() {
    return editEnabled;
  }

  @Override
  public boolean isEditing() {
    return getInput().isEditing();
  }

  public Boolean isEditModal() {
    return BeeUtils.isTrue(editModal) || UiHelper.isModal(getWidget());
  }

  public boolean isEmbedded() {
    return embedded;
  }

  @Override
  public boolean isEnabled() {
    return getInput().isEnabled();
  }

  @Override
  public boolean isNullable() {
    return getInput().isNullable();
  }

  @Override
  public boolean isOrHasPartner(Node node) {
    return getInput().isOrHasPartner(node)
        || getSelector().getPopup().getElement().isOrHasChild(node);
  }

  @Override
  public void normalizeDisplay(String normalizedValue) {
  }

  public void onRefresh(IsRow targetRow) {
    if (!BeeConst.isUndef(getEditTargetIndex())) {
      if (targetRow == null) {
        setEditRowId(null);
      } else {
        setEditRowId(targetRow.getLong(getEditTargetIndex()));
      }
    }
  }

  @Override
  public void setAccessKey(char key) {
    getInput().setAccessKey(key);
  }

  public void setAdding(boolean adding) {
    this.adding = adding;
  }

  public void setAdditionalFilter(Filter additionalFilter) {
    if (Objects.equal(additionalFilter, getOracle().getAdditionalFilter())) {
      return;
    }
    getOracle().setAdditionalFilter(additionalFilter);
    setAlive(true);
  }

  public void setDisplayValue(String value) {
    getInput().setValue(value, false);
  }

  @Override
  public void setEditing(boolean editing) {
    getInput().setEditing(editing);
  }

  @Override
  public void setEnabled(boolean enabled) {
    getInput().setEnabled(enabled);
  }

  @Override
  public void setFocus(boolean focused) {
    getInput().setFocus(focused);
  }

  @Override
  public void setHandlesTabulation(boolean handlesTabulation) {
    this.handlesTabulation = handlesTabulation;
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  @Override
  public void setNullable(boolean nullable) {
    getInput().setNullable(nullable);
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  public void setSelection(BeeRow row) {
    setRelatedRow(row);
    setEditorValue(row == null ? null : BeeUtils.toString(row.getId()));

    if (!BeeConst.isUndef(getEditSourceIndex())) {
      if (row == null) {
        setEditRowId(null);
      } else {
        setEditRowId(row.getLong(getEditSourceIndex()));
      }
    }

    hideSelector();
    reset();

    fireEvent(new EditStopEvent(State.CHANGED, KeyCodes.KEY_TAB, false));
    SelectorEvent.fire(this, State.CHANGED);
  }

  @Override
  public void setTabIndex(int index) {
    getInput().setTabIndex(index);
  }

  @Override
  public void setUpperCase(boolean upperCase) {
    getInput().setUpperCase(upperCase);
  }

  @Override
  public void setValue(String value) {
    setEditorValue(value);
  }

  @Override
  public void setVisibleLines(int lines) {
    this.visibleLines = lines;
  }

  @Override
  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
    SelectorEvent.fire(this, State.OPEN);

    setRelatedRow(null);
    if (!isEmbedded()) {
      setEditorValue(oldValue);
    }

    setLastRequest(null);
    setOffset(0);
    setHasMore(false);

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
        setWaiting(true);
      }
    }

    inputEvents.consume();
    setActive(true);
  }

  @Override
  public List<String> validate(boolean checkForNull) {
    return Collections.emptyList();
  }

  @Override
  public List<String> validate(String normalizedValue, boolean checkForNull) {
    return Collections.emptyList();
  }

  protected void exit(boolean hideSelector, State state, Integer keyCode, boolean hasModifiers) {
    if (hideSelector) {
      hideSelector();
    }
    reset();

    SelectorEvent.fire(this, state);
    fireEvent(new EditStopEvent(state, keyCode, hasModifiers));
  }

  protected int getEditSourceIndex() {
    return editSourceIndex;
  }

  protected String getEditViewName() {
    return editViewName;
  }

  protected InputWidget getInput() {
    return input;
  }

  protected String getRelationLabel() {
    return relationLabel;
  }

  protected void hideSelector() {
    getSelector().hide();
  }

  protected void init(InputWidget inputWidget, boolean embed) {
    inputWidget.addStyleName(STYLE_SELECTOR);
    if (embed) {
      inputWidget.addStyleName(STYLE_EMBEDDED);
    }

    if (embed && isEditEnabled()) {
      final Flow container = new Flow();
      container.addStyleName(STYLE_EDITABLE_CONTAINER);

      inputWidget.addStyleName(STYLE_EDITABLE_INPUT);

      inputWidget.addFocusHandler(new FocusHandler() {
        @Override
        public void onFocus(FocusEvent event) {
          container.addStyleName(STYLE_EDITABLE_ACTIVE);
        }
      });
      inputWidget.addBlurHandler(new BlurHandler() {
        @Override
        public void onBlur(BlurEvent event) {
          container.removeStyleName(STYLE_EDITABLE_ACTIVE);
        }
      });

      container.add(inputWidget);

      InlineLabel label = new InlineLabel(String.valueOf(BeeConst.DRILL_DOWN));
      label.addStyleName(STYLE_DRILL);
      label.addStyleName(STYLE_DRILL_DISABBLED);

      label.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          editRow();
        }
      });

      setDrill(label);
      container.add(label);

      initWidget(container);

    } else {
      initWidget(inputWidget);
    }

    SelectorEvent.fire(this, State.INITIALIZED);
  }

  protected boolean isActive() {
    return active;
  }

  @Override
  protected void onUnload() {
    SelectorEvent.fire(this, State.UNLOADING);

    getOracle().onUnload();
    super.onUnload();
  }

  protected void reset() {
    setActive(false);
    setWaiting(false);

    setLastRequest(null);
    setOffset(0);
    setHasMore(false);

    getInput().removeStyleName(STYLE_WAITING);
    getInput().removeStyleName(STYLE_NOT_FOUND);

    if (getDrill() != null) {
      getWidget().removeStyleName(STYLE_EDITABLE_ACTIVE);
    }
  }

  protected void setActive(boolean active) {
    this.active = active;
  }

  protected void setRelatedRow(BeeRow relatedRow) {
    this.relatedRow = relatedRow;
  }

  protected void updateDisplay(String value) {
    if (BeeUtils.isEmpty(value)) {
      clearDisplay();
    } else {
      setDisplayValue(value.trim());
    }
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

  private static void addClassToCell(MenuItem item, String className) {
    TableCellElement cell = DomUtils.getParentCell(item, true);
    if (cell != null) {
      cell.addClassName(className);
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
    setWaiting(true);

    getOracle().requestSuggestions(request, getCallback());
  }

  private void editRow() {
    Long rowId;

    if (BeeConst.isUndef(getEditTargetIndex())) {
      rowId = BeeUtils.toLongOrNull(getEditorValue());
    } else {
      rowId = getEditRowId();
    }

    if (!DataUtils.isId(rowId)) {
      return;
    }

    boolean modal = isEditModal();
    RowCallback rowCallback;

    if (modal) {
      rowCallback = new RowCallback() {
        @Override
        public void onSuccess(BeeRow result) {
          if (BeeUtils.same(getEditViewName(), getOracle().getViewName())) {
            setRelatedRow(result);

          } else {
            BeeRow row = getRelatedRow();
            if (row == null && BeeUtils.isLong(getEditorValue())) {
              row = getOracle().getCachedRow(BeeUtils.toLong(getEditorValue()));
            }

            if (row != null && !BeeConst.isUndef(getEditSourceIndex())) {
              RelationUtils.updateRow(getOracle().getDataInfo(),
                  getOracle().getDataInfo().getColumnId(getEditSourceIndex()), row,
                  Data.getDataInfo(getEditViewName()), result, false);
              setRelatedRow(row);
            }
          }

          if (getRelatedRow() != null) {
            fireEvent(new EditStopEvent(State.EDITED));
          }
        }
      };

    } else {
      rowCallback = null;
    }

    RowEditor.openRow(getEditForm(), Data.getDataInfo(getEditViewName()), rowId, modal,
        getWidget(), rowCallback, null);
  }

  private void exit(boolean hideSelector, State state) {
    exit(hideSelector, state, null, false);
  }

  private Callback getCallback() {
    return callback;
  }

  private Map<Integer, AbstractCellRenderer> getCellRenderers() {
    return cellRenderers;
  }

  private int getColumnCount() {
    return getChoiceColumns().size();
  }

  private Widget getDrill() {
    return drill;
  }

  private String getEditorValue() {
    return editorValue;
  }

  private Long getEditRowId() {
    return editRowId;
  }

  private int getEditTargetIndex() {
    return editTargetIndex;
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

  private void initCellRenderers(DataInfo dataInfo) {
    if (!getCellRenderers().isEmpty()) {
      getCellRenderers().clear();
    }

    for (Map.Entry<Integer, SelectorColumn> entry : getSelectorColumns().entrySet()) {
      SelectorColumn sc = entry.getValue();
      int index = dataInfo.getColumnIndex(sc.getSource());

      CellSource cellSource = BeeConst.isUndef(index)
          ? null : CellSource.forColumn(dataInfo.getColumns().get(index), index);

      AbstractCellRenderer renderer = RendererFactory.getRenderer(sc.getRendererDescription(),
          sc.getRender(), sc.getRenderTokens(), sc.getItemKey(), sc.getRenderColumns(),
          dataInfo.getColumns(), cellSource);

      if (renderer != null) {
        getCellRenderers().put(entry.getKey(), renderer);
      }
    }

    if (getCellRenderers().isEmpty() && getColumnCount() == 1 && getRowRenderer() != null) {
      getCellRenderers().put(0, getRowRenderer());
    }

    for (int i = 0; i < getColumnCount(); i++) {
      if (!getCellRenderers().containsKey(i)) {
        int index = dataInfo.getColumnIndex(getChoiceColumns().get(i));

        CellSource cellSource = CellSource.forColumn(dataInfo.getColumns().get(index), index);
        AbstractCellRenderer renderer = new SimpleRenderer(cellSource);

        getCellRenderers().put(i, renderer);
      }
    }
  }

  private boolean isAlive() {
    return alive;
  }

  private boolean isInstant() {
    Operator operator = getSearchType();
    return Operator.CONTAINS.equals(operator) || Operator.STARTS.equals(operator);
  }

  private boolean isNewRowEnabled() {
    return newRowEnabled;
  }

  private boolean isTableMode() {
    return tableMode;
  }

  private boolean isWaiting() {
    return waiting;
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

  private void setAlive(boolean alive) {
    if (isAlive() != alive) {
      this.alive = alive;
      getInput().setStyleName(STYLE_EMPTY, !alive);
    }
  }

  private void setDrill(Widget drill) {
    this.drill = drill;
  }

  private void setEditorValue(String ev) {
    if (getDrill() != null && BeeUtils.isEmpty(this.editorValue) != BeeUtils.isEmpty(ev)) {
      getDrill().setStyleName(STYLE_DRILL_DISABBLED, BeeUtils.isEmpty(ev));
    }
    this.editorValue = ev;
  }

  private void setEditRowId(Long editRowId) {
    this.editRowId = editRowId;
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

  private void setWaiting(boolean waiting) {
    this.waiting = waiting;
  }

  private void start(int keyCode) {
    startEdit(null, BeeUtils.toChar(keyCode), null, null);
  }
}