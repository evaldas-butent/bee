package com.butent.bee.client;

import com.google.common.collect.Lists;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

import elemental.html.History;

import elemental.events.PopStateEvent;

import elemental.events.Event;
import elemental.events.EventListener;

import elemental.js.JsBrowser;

public class Historian implements HasInfo {
  
  private static final Historian INSTANCE = new Historian();
  
  private static final BeeLogger logger = LogUtils.getLogger(Historian.class);
  
  public static Historian getInstance() {
    return INSTANCE;
  }
  
  private final List<Place> places = Lists.newArrayList();

  private int position = BeeConst.UNDEF;
  
  private Historian() {
    super();
    
    JsBrowser.getWindow().setOnpopstate(new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        Object state = ((PopStateEvent) evt).getState();
        logger.debug("history", getHistory().getLength(),
            "pop", (state == null) ? "null" : state.toString());
      }
    });
  }
  
  @Override
  public List<Property> getInfo() {
    List<Property> info = PropertyUtils.createProperties("Size", places.size(),
        "Position", getPosition());
    
    for (int i = 0; i < places.size(); i++) {
      info.add(new Property(String.valueOf(i), places.get(i).getId()));
    }
    return info;
  }
  
  public int getPosition() {
    return position;
  }

  public void reset() {
    places.clear();
    setPosition(BeeConst.UNDEF);
  }

  private History getHistory() {
    return JsBrowser.getWindow().getHistory();
  }

  private void setPosition(int position) {
    this.position = position;
  }
}
