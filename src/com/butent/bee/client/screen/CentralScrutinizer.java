package com.butent.bee.client.screen;

import com.google.common.base.Objects;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.cli.Shell;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Stack;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

class CentralScrutinizer extends Stack implements CloseHandler<IdentifiableWidget> {
  
  private static class Appliance extends Flow implements HasCloseHandlers<IdentifiableWidget> {
    
    private static final String STYLE_NAME = "bee-Appliance"; 

    private final Domain domain;
    private final Long key;

    private Appliance(Domain domain, Long key, String caption) {
      super();
      this.domain = domain;
      this.key = key;
      
      addStyleName(STYLE_NAME);
      addStyleName(STYLE_NAME + BeeUtils.proper(domain.name()));
      
      if (domain.getImageResource() != null) {
        BeeImage icon = new BeeImage(domain.getImageResource());
        icon.addStyleName(STYLE_NAME + "-icon");
        add(icon);
      }
      
      BeeLabel label = new BeeLabel(BeeUtils.notEmpty(caption, domain.getCaption()));
      label.addStyleName(STYLE_NAME + "-label");
      add(label);
      
      if (domain.isClosable()) {
        BeeImage close = new BeeImage(Global.getImages().closeSmall());
        close.addStyleName(STYLE_NAME + "-close");
        
        close.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            CloseEvent.fire(Appliance.this, Appliance.this);
          }
        });
        
        add(close);
      }
    }

    @Override
    public HandlerRegistration addCloseHandler(CloseHandler<IdentifiableWidget> handler) {
      return addHandler(handler, CloseEvent.getType());
    }

    @Override
    public String getIdPrefix() {
      return "muffin";
    }

    private Domain getDomain() {
      return domain;
    }
    
    private Long getKey() {
      return key;
    }

    private boolean is(Domain otherDomain, Long otherKey) {
      return getDomain().equals(otherDomain) && Objects.equal(getKey(), otherKey);
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(CentralScrutinizer.class);
  
  private static final double DEFAULT_HEADER_SIZE = 25.0;

  CentralScrutinizer() {
    super(Unit.PX);
    addStyleName("bee-CentralScrutinizer");
  }
  
  @Override
  public String getIdPrefix() {
    return "scrutinizer";
  }

  @Override
  public void onClose(CloseEvent<IdentifiableWidget> event) {
    if (event.getTarget() instanceof Appliance) {
      Appliance appliance = (Appliance) event.getTarget();
      remove(appliance.getDomain(), appliance.getKey());
    }
  }
  
  @Override
  public boolean remove(Widget child) {
    boolean removed = super.remove(child);
    if (removed && child instanceof HandlesStateChange) {
      ((HandlesStateChange) child).onStateChange(State.REMOVED);
    }
    return removed;
  }

  @Override
  protected void onHeaderClick(Widget child) {
    super.onHeaderClick(child);
    
    if (child instanceof HandlesStateChange) {
      ((HandlesStateChange) child).onStateChange(State.ACTIVATED);
    }
  }

  boolean activate(Domain domain, Long key) {
    int index = find(domain, key);
    if (index >= 0) {
      showWidget(index);
      return true;
    } else {
      return false;
    }
  }

  void add(Domain domain, IdentifiableWidget widget) {
    add(domain, widget, null, null);
  }

  void add(Domain domain, IdentifiableWidget widget, Long key, String caption) {
    Assert.notNull(domain);
    Assert.notNull(widget);
    
    if (find(domain, key) >= 0) {
      logger.warning("attemp to add existing appliance failed:", domain, key);
      return;
    }
    
    int before = getWidgetCount();
    for (int i = 0; i < getWidgetCount(); i++) {
      Appliance appliance = getAppliance(i);
      if (appliance != null && appliance.getDomain().getOrdinal() > domain.getOrdinal()) {
        before = i;
        break;
      }
    }
    
    Appliance appliance = new Appliance(domain, key, caption);
    appliance.addCloseHandler(this);
    
    insert(widget.asWidget(), appliance, DEFAULT_HEADER_SIZE, before);
  }

  boolean contains(Domain domain, Long key) {
    return find(domain, key) >= 0;
  }
  
  boolean remove(Domain domain, Long key) {
    int index = find(domain, key);
    if (index >= 0) {
      return remove(index);
    } else {
      return false;
    }
  }
  
  void start() {
    add(Domain.REPORT, Global.getReports());

    Shell shell = new Shell();
    shell.restore();
    
    add(Domain.ADMIN, shell);
    
    add(Domain.WHITE_ZONE, new Flow());
    activate(Domain.WHITE_ZONE, null);
  }
  
  private int find(Domain domain, Long key) {
    for (int i = 0; i < getWidgetCount(); i++) {
      Appliance appliance = getAppliance(i);
      if (appliance != null && appliance.is(domain, key)) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  private Appliance getAppliance(int index) {
    Widget headerWidget = getHeaderWidget(index);
    if (headerWidget instanceof Appliance) {
      return (Appliance) headerWidget;
    } else {
      return null;
    }
  }
}
