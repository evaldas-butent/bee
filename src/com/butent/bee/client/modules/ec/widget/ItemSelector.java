package com.butent.bee.client.modules.ec.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import elemental.events.KeyboardEvent.KeyCode;

public class ItemSelector extends Flow implements HasSelectionHandlers<String> {

  private static final String STYLE_PRIMARY = "ItemSelector";

  private final Editor editor;

  public ItemSelector(String caption) {
    super(EcStyles.name(STYLE_PRIMARY));

    if (!BeeUtils.isEmpty(caption)) {
      Label label = new Label(caption);
      EcStyles.add(label, STYLE_PRIMARY, "label");
      add(label);
    }

    this.editor = createEditor();
    add(editor);

    Button button = new Button(Localized.constants.ecDoSearch());
    EcStyles.add(button, STYLE_PRIMARY, "submit");

    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        maybeFire();
      }
    });

    add(button);
  }

  @Override
  public HandlerRegistration addSelectionHandler(SelectionHandler<String> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  private Editor createEditor() {
    InputText input = new InputText();
    EcStyles.add(input, STYLE_PRIMARY, "input");
    
    input.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCode.ENTER) {
          maybeFire();
        }
      }
    });
    return input;
  }
  
  private void maybeFire() {
    String value = BeeUtils.trim(editor.getValue());
    if (!value.isEmpty()) {
      SelectionEvent.fire(this, value);
    }
  }
}
