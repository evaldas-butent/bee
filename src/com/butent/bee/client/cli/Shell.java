package com.butent.bee.client.cli;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.utils.BeeUtils;

public class Shell extends InputArea {

  private static final char EOL = '\n';
  
  public Shell() {
    super();
    addStyleName("bee-Shell");
    sinkEvents(Event.ONKEYDOWN | Event.ONDBLCLICK);
  }

  @Override
  public String getIdPrefix() {
    return "shell";
  }

  @Override
  public void onBrowserEvent(Event event) {
    boolean ok = !BeeUtils.isEmpty(getValue());
    if (ok) {
      String type = event.getType();
      if (EventUtils.isKeyDown(type)) {
        ok = event.getKeyCode() == KeyCodes.KEY_ENTER;
        if (ok && EventUtils.hasModifierKey(event)) {
          int p = getCursorPos();
          setValue(new StringBuilder(getValue()).insert(p, EOL).toString());
          setCursorPos(p + 1);
          event.preventDefault();
          return;
        }
      } else {
        ok = EventUtils.isDblClick(type);
      }
    }
    
    if (ok) {
      int pos = getCursorPos();
      int start = (pos > 0) ? getValue().lastIndexOf(EOL, pos - 1) + 1 : 0;
      int end = getValue().indexOf(EOL, pos);

      String line;
      if (end == start) {
        line = null;
      } else if (end > start) {
        line = getValue().substring(start, end);
      } else {
        line = getValue().substring(start);
      }
      
      event.preventDefault();
      if (BeeUtils.isEmpty(line)) {
        return;
      }
      
      if (pos == getValue().length()) {
        setValue(getValue() + EOL);
        setCursorPos(pos + 1);
      }
      CliWorker.execute(line.trim());
    } else  {
      super.onBrowserEvent(event);
    }
  }
}
