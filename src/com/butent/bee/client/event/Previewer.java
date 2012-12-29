package com.butent.bee.client.event;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;

import java.util.List;

public class Previewer implements NativePreviewHandler, HasInfo {
  
  private static final Previewer INSTANCE = new Previewer(); 

  public static void ensureRegistered(PreviewHandler handler) {
    Assert.notNull(handler);
    if (!INSTANCE.contains(handler)) {
      INSTANCE.add(handler);
    }
  }

  public static void ensureUnregistered(PreviewHandler handler) {
    Assert.notNull(handler);
    if (INSTANCE.contains(handler)) {
      INSTANCE.remove(handler);
    }
  }
  
  public static void register(PreviewHandler handler) {
    Assert.notNull(handler);
    
    Assert.state(!INSTANCE.contains(handler));
    INSTANCE.add(handler);
  }

  public static void registermouseDownPriorHandler(PreviewHandler handler) {
    Assert.notNull(handler);
    INSTANCE.mouseDownPriorHandlers.add(handler);
  }
  
  public static void unregister(PreviewHandler handler) {
    Assert.notNull(handler);

    Assert.state(INSTANCE.contains(handler));
    INSTANCE.remove(handler);
  }

  public static void unregistermouseDownPriorHandler(PreviewHandler handler) {
    Assert.notNull(handler);
    INSTANCE.mouseDownPriorHandlers.remove(handler);
  }
  
  private final List<PreviewHandler> handlers = Lists.newArrayList();
  private final List<PreviewHandler> mouseDownPriorHandlers = Lists.newArrayList();

  private int modalCount = 0;

  private Previewer() {
    Event.addNativePreviewHandler(this);
  }
  
  @Override
  public List<Property> getInfo() {
    List<Property> info = Lists.newArrayList();
    info.add(new Property("Modal Count", BeeUtils.toString(modalCount)));
    
    for (PreviewHandler handler : handlers) {
      info.add(new Property(handler.getId(), NameUtils.getName(handler)));
    }
    return info;
  }
  
  @Override
  public void onPreviewNativeEvent(NativePreviewEvent event) {
    if (modalCount == 0) {
      String type = event.getNativeEvent().getType();

      if (EventUtils.EVENT_TYPE_MOUSE_DOWN.equals(type)) {
        for (int i = 0; i < mouseDownPriorHandlers.size(); i++) {
          mouseDownPriorHandlers.get(i).onEventPreview(event);
          if (event.isCanceled() || event.isConsumed()) {
            return;
          }
        }
      }
    }
    
    if (handlers.isEmpty()) {
      return;

    } else if (handlers.size() == 1) {
      handlers.get(0).onEventPreview(event);

    } else {
      for (int i = handlers.size() - 1; i >= 0; i--) {
        handlers.get(i).onEventPreview(event);
        if (event.isCanceled() || event.isConsumed()) {
          break;
        }
      }
    }
  }

  private void add(PreviewHandler handler) {
    handlers.add(handler);
    if (handler.isModal()) {
      modalCount++;
    }
  }
  
  private boolean contains(PreviewHandler handler) {
    return !BeeConst.isUndef(indexOf(handler));
  }

  private int indexOf(PreviewHandler handler) {
    for (int i = 0; i < handlers.size(); i++) {
      if (BeeUtils.same(handler.getId(), handlers.get(i).getId())) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }
  
  private void remove(PreviewHandler handler) {
    handlers.remove(indexOf(handler));
    if (handler.isModal()) {
      modalCount--;
    }
  }
}
