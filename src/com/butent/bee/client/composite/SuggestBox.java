package com.butent.bee.client.composite;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.HandlesAllKeyEvents;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import com.butent.bee.client.data.SelectionOracle;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.menu.MenuBar;
import com.butent.bee.client.menu.MenuCommand;
import com.butent.bee.client.menu.MenuItem;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.data.view.HasRelationInfo;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

/**
 * Enables using user interface component for entering text entries while the system is suggesting
 * possible matching values from the list.
 */

public class SuggestBox extends Composite implements HasText, HasAllKeyHandlers,
    HasSelectionHandlers<Suggestion>, Editor, HasRelationInfo, HasOptions {

  /**
   * Requires for implementing methods to have an event to handle situations when a suggestion is
   * selected.
   */

  public static interface SuggestionCallback {
    void onSuggestionSelected(Suggestion suggestion);
  }

  /**
   * Handles animation events of of suggestion box popup menu.
   */

  private static class Display implements HasAnimation {

    private final SuggestionMenu suggestionMenu;
    private final Popup suggestionPopup;

    private SuggestBox lastSuggestBox = null;

    private boolean hideWhenEmpty = true;

    public Display() {
      suggestionMenu = new SuggestionMenu(true);
      suggestionPopup = createPopup();
      suggestionPopup.setWidget(suggestionMenu);
    }

    public void hideSuggestions() {
      suggestionPopup.hide();
    }

    public boolean isAnimationEnabled() {
      return suggestionPopup.isAnimationEnabled();
    }

    public boolean isSuggestionListShowing() {
      return suggestionPopup.isShowing();
    }

    public void setAnimationEnabled(boolean enable) {
      suggestionPopup.setAnimationEnabled(enable);
    }

    protected Popup createPopup() {
      Popup p = new Popup(true, false);
      p.setStyleName("bee-SuggestBoxPopup");
      p.setPreviewingAllNativeEvents(true);
      return p;
    }

    protected Suggestion getCurrentSelection() {
      if (!isSuggestionListShowing()) {
        return null;
      }
      MenuItem item = suggestionMenu.getSelectedItem();
      return item == null ? null : ((SuggestionMenuItem) item).getSuggestion();
    }

    protected void moveSelectionDown() {
      if (isSuggestionListShowing()) {
        suggestionMenu.selectItem(suggestionMenu.getSelectedItemIndex() + 1);
      }
    }

    protected void moveSelectionUp() {
      if (isSuggestionListShowing()) {
        if (suggestionMenu.getSelectedItemIndex() == -1) {
          suggestionMenu.selectItem(suggestionMenu.getNumItems() - 1);
        } else {
          suggestionMenu.selectItem(suggestionMenu.getSelectedItemIndex() - 1);
        }
      }
    }

    protected void showSuggestions(final SuggestBox suggestBox,
        Collection<? extends Suggestion> suggestions, boolean isAutoSelectEnabled,
        final SuggestionCallback callback) {
      boolean anySuggestions = (suggestions != null && suggestions.size() > 0);
      if (!anySuggestions && hideWhenEmpty) {
        hideSuggestions();
        return;
      }

      if (suggestionPopup.isAttached()) {
        suggestionPopup.hide();
      }
      suggestionMenu.clearItems();

      for (final Suggestion curSuggestion : suggestions) {
        SuggestionMenuItem menuItem = new SuggestionMenuItem(curSuggestion, suggestionMenu,
            new MenuCommand() {
              @Override
              public void execute() {
                callback.onSuggestionSelected(curSuggestion);
              }
            });
        suggestionMenu.addItem(menuItem);
      }

      if (isAutoSelectEnabled && anySuggestions) {
        suggestionMenu.selectItem(0);
      }

      if (lastSuggestBox != suggestBox) {
        if (lastSuggestBox != null) {
          suggestionPopup.removeAutoHidePartner(lastSuggestBox.getElement());
        }
        lastSuggestBox = suggestBox;
        suggestionPopup.addAutoHidePartner(suggestBox.getElement());
      }

      suggestionPopup.showRelativeTo(suggestBox);
    }
  }

  /**
   * Handles suggestion menu items list.
   */

  private static class SuggestionMenu extends MenuBar {

    public SuggestionMenu(boolean vertical) {
      super(0, vertical);
    }

    public int getNumItems() {
      return getItems().size();
    }

    public int getSelectedItemIndex() {
      MenuItem selectedItem = getSelectedItem();
      if (selectedItem != null) {
        return getItems().indexOf(selectedItem);
      }
      return -1;
    }

    public void selectItem(int index) {
      List<MenuItem> items = getItems();
      if (index >= 0 && index < items.size()) {
        selectItem(items.get(index));
      }
    }
  }

  /**
   * Manages a single suggestion box list entry.
   */

  private static class SuggestionMenuItem extends MenuItem {
    private Suggestion suggestion;

    public SuggestionMenuItem(Suggestion suggestion, SuggestionMenu menuBar, MenuCommand command) {
      super(menuBar, suggestion.getDisplayString(), command);
      StyleUtils.setWordWrap(getElement(), false);
      setSuggestion(suggestion);
    }

    public Suggestion getSuggestion() {
      return suggestion;
    }

    public void setSuggestion(Suggestion suggestion) {
      this.suggestion = suggestion;
    }
  }

  private static final String STYLENAME_DEFAULT = "bee-SuggestBox";

  private int limit = 12;
  private boolean selectsFirstItem = true;
  private String currentText;

  private SuggestOracle oracle;

  private final Display display;
  private final InputText box;

  private final Callback callback = new Callback() {
    public void onSuggestionsReady(Request request, Response response) {
      display.showSuggestions(SuggestBox.this, response.getSuggestions(),
          isAutoSelectEnabled(), suggestionCallback);
    }
  };

  private final SuggestionCallback suggestionCallback = new SuggestionCallback() {
    public void onSuggestionSelected(Suggestion suggestion) {
      setNewSelection(suggestion);
    }
  };
  
  private RelationInfo relationInfo = null;
  private String options = null;

  public SuggestBox() {
    this(new InputText());
  }

  public SuggestBox(InputText box) {
    this(box, new Display());
  }

  public SuggestBox(InputText box, Display display) {
    this.box = box;
    this.display = display;
    initWidget(box);

    addEventsToTextBox();
    setStyleName(STYLENAME_DEFAULT);
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return addDomHandler(handler, BlurEvent.getType());
  }

  public HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addDomHandler(handler, KeyDownEvent.getType());
  }

  public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
    return addDomHandler(handler, KeyPressEvent.getType());
  }

  public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
    return addDomHandler(handler, KeyUpEvent.getType());
  }

  public HandlerRegistration addSelectionHandler(SelectionHandler<Suggestion> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public void createId() {
    DomUtils.createId(this, "suggest");
  }

  public Display getDisplay() {
    return display;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public int getLimit() {
    return limit;
  }

  public String getNormalizedValue() {
    return getBox().getNormalizedValue();
  }

  public String getOptions() {
    return options;
  }

  public SuggestOracle getOracle() {
    return oracle;
  }

  public RelationInfo getRelationInfo() {
    return relationInfo;
  }

  public int getTabIndex() {
    return box.getTabIndex();
  }

  public String getText() {
    return box.getText();
  }

  public String getValue() {
    return box.getValue();
  }

  public boolean handlesKey(int keyCode) {
    return !BeeUtils.inList(keyCode, KeyCodes.KEY_UP, KeyCodes.KEY_DOWN);
  }

  public boolean isAutoSelectEnabled() {
    return selectsFirstItem;
  }

  public boolean isEditing() {
    return box.isEditing();
  }

  public boolean isNullable() {
    return getBox().isNullable();
  }

  public void setAccessKey(char key) {
    box.setAccessKey(key);
  }

  public void setAutoSelectEnabled(boolean selectsFirstItem) {
    this.selectsFirstItem = selectsFirstItem;
  }

  public void setEditing(boolean editing) {
    box.setEditing(editing);
  }

  public void setFocus(boolean focused) {
    box.setFocus(focused);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public void setNullable(boolean nullable) {
    getBox().setNullable(nullable);
  }

  public void setOptions(String options) {
    this.options = options;
  }

  public void setOracle(SuggestOracle oracle) {
    this.oracle = oracle;
  }

  public void setRelationInfo(RelationInfo relationInfo) {
    this.relationInfo = relationInfo;
    if (relationInfo != null && getOracle() == null) {
      setOracle(new SelectionOracle(relationInfo.getRelTable(), relationInfo.getRelField()));
    }
  }

  public void setTabIndex(int index) {
    box.setTabIndex(index);
  }

  public void setText(String text) {
    box.setText(text);
  }

  public void setValue(String newValue) {
    box.setValue(newValue);
  }

  public void setValue(String value, boolean fireEvents) {
    box.setValue(value, fireEvents);
  }

  public void showSuggestionList() {
    if (isAttached()) {
      currentText = null;
      refreshSuggestions();
    }
  }

  public void startEdit(String oldValue, char charCode, EditorAction onEntry) {
    if (Character.isLetterOrDigit(charCode)) {
      setValue(BeeUtils.toString(charCode));
    } else {
      setValue(BeeConst.STRING_EMPTY);
    }
  }

  public String validate() {
    return getBox().validate();
  }

  private void addEventsToTextBox() {
    class TextBoxEvents extends HandlesAllKeyEvents implements ValueChangeHandler<String> {

      public void onKeyDown(KeyDownEvent event) {
        switch (event.getNativeKeyCode()) {
          case KeyCodes.KEY_DOWN:
            display.moveSelectionDown();
            break;
          case KeyCodes.KEY_UP:
            display.moveSelectionUp();
            break;
          case KeyCodes.KEY_ENTER:
          case KeyCodes.KEY_TAB:
            Suggestion suggestion = display.getCurrentSelection();
            if (suggestion == null) {
              display.hideSuggestions();
            } else {
              setNewSelection(suggestion);
            }
            break;
        }
        delegateEvent(SuggestBox.this, event);
      }

      public void onKeyPress(KeyPressEvent event) {
        delegateEvent(SuggestBox.this, event);
      }

      public void onKeyUp(KeyUpEvent event) {
        refreshSuggestions();
        delegateEvent(SuggestBox.this, event);
      }

      public void onValueChange(ValueChangeEvent<String> event) {
        delegateEvent(SuggestBox.this, event);
      }
    }

    TextBoxEvents events = new TextBoxEvents();
    events.addKeyHandlersTo(box);
    box.addValueChangeHandler(events);
  }

  private void fireSuggestionEvent(Suggestion selectedSuggestion) {
    SelectionEvent.fire(this, selectedSuggestion);
  }

  private InputText getBox() {
    return box;
  }

  private void refreshSuggestions() {
    String text = getText();
    if (text.equals(currentText)) {
      return;
    } else {
      currentText = text;
    }
    showSuggestions(text);
  }

  private void setNewSelection(Suggestion curSuggestion) {
    currentText = curSuggestion.getReplacementString();
    setText(currentText);
    display.hideSuggestions();
    fireSuggestionEvent(curSuggestion);
  }

  private void showSuggestions(String query) {
    if (query.length() == 0) {
      oracle.requestDefaultSuggestions(new Request(null, limit), callback);
    } else {
      oracle.requestSuggestions(new Request(query, limit), callback);
    }
  }
}
