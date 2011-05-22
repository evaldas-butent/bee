package com.butent.bee.client.view.edit;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.shared.data.IsRow;

public class EditStartEvent extends GwtEvent<EditStartEvent.Handler> { 
  
  public interface Handler extends EventHandler {
    void onEditStart(EditStartEvent event);
  }
  
  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static Type<Handler> getType() {
    return TYPE;
  }
  
  private final IsRow rowValue;
  private final String columnId;

  private final Element sourceElement;
  private final int charCode;
  
  public EditStartEvent(IsRow rowValue, String columnId, Element sourceElement, int charCode) {
    this.rowValue = rowValue;
    this.columnId = columnId;
    this.sourceElement = sourceElement;
    this.charCode = charCode;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public int getCharCode() {
    return charCode;
  }

  public String getColumnId() {
    return columnId;
  }

  public IsRow getRowValue() {
    return rowValue;
  }
  
  public Element getSourceElement() {
    return sourceElement;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onEditStart(this);
  }
}
