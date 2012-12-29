package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.VisibilityChangeEvent;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.HasWidgetSupplier;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.HeaderImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;

import java.util.EnumSet;
import java.util.List;

class ShippingSchedule extends Complex implements Presenter, View, Printable,
    VisibilityChangeEvent.Handler, HasWidgetSupplier {

  static final String supplierKey = "shipping_schedule";
  
  private static final BeeLogger logger = LogUtils.getLogger(ShippingSchedule.class);

  private static final String STYLE_PREFIX = "bee-tr-ss-";

  private static final String STYLE_CONTAINER = STYLE_PREFIX + "Container";

  static void open(final Callback<IdentifiableWidget> callback) {
    Assert.notNull(callback);
    callback.onSuccess(new ShippingSchedule());
  }

  private final HeaderView header;

  private final List<HandlerRegistration> registry = Lists.newArrayList();

  private boolean enabled = true;

  private ShippingSchedule() {
    super();
    addStyleName(STYLE_CONTAINER);

    this.header = GWT.create(HeaderImpl.class);
    header.create("Reisų kalendorius", false, true, EnumSet.of(UiOption.ROOT),
        EnumSet.of(Action.REFRESH, Action.CONFIGURE), null);
    header.setViewPresenter(this);
    add(header);
  }

  @Override
  public String getCaption() {
    return header.getCaption();
  }

  @Override
  public String getEventSource() {
    return null;
  }

  @Override
  public HeaderView getHeader() {
    return header;
  }

  @Override
  public View getMainView() {
    return this;
  }

  @Override
  public Element getPrintElement() {
    return getElement();
  }

  @Override
  public String getSupplierKey() {
    return supplierKey;
  }

  @Override
  public Presenter getViewPresenter() {
    return this;
  }

  @Override
  public IdentifiableWidget getWidget() {
    return this;
  }

  @Override
  public String getWidgetId() {
    return getId();
  }

  @Override
  public void handleAction(Action action) {
    switch (action) {
      case REFRESH:
        refresh();
        break;

      case CONFIGURE:
        break;

      case CLOSE:
        BeeKeeper.getScreen().closeWidget(this);
        break;

      case PRINT:
        Printer.print(this);
        break;

      default:
        logger.info(action, "not implemented");
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean onPrint(Element source, Element target) {
    boolean ok;
    String id = source.getId();

    if (getId().equals(id)) {
      int height = source.getClientHeight() + getPrintHeightAdjustment();
      StyleUtils.setSize(target, source.getClientWidth(), height);
      ok = true;

    } else if (header.asWidget().getElement().isOrHasChild(source)) {
      ok = header.onPrint(source, target);

    } else {
      ok = true;
    }

    return ok;
  }

  @Override
  public void onViewUnload() {
  }

  @Override
  public void onVisibilityChange(VisibilityChangeEvent event) {
    if (event.isVisible() && DomUtils.isOrHasAncestor(getElement(), event.getId())) {
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void setEventSource(String eventSource) {
  }

  @Override
  public void setViewPresenter(Presenter viewPresenter) {
  }

  @Override
  protected void onLoad() {
    super.onLoad();

    EventUtils.clearRegistry(registry);
    registry.add(VisibilityChangeEvent.register(this));
  }
  
  @Override
  protected void onUnload() {
    EventUtils.clearRegistry(registry);

    super.onUnload();
  }

  private int getPrintHeightAdjustment() {
    return 0;
  }

  private void refresh() {
  }
}
