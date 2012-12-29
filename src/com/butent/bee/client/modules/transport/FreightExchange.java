package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries;
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
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

class FreightExchange extends Complex implements Presenter, View, Printable,
    VisibilityChangeEvent.Handler, HasWidgetSupplier {

  private static class Freight {
    private final Long orderId;

    private final OrderStatus orderStatus;
    private final DateTime orderDate;
    private final String orderNo;

    private final Long customerId;
    private final String customerName;

    private final Long cargoId;
    private final String cargoDescription;

    private final Long loadingPlaceId;
    private final Long unloadingPlaceId;

    private final JustDate loadingDate;
    private final Long loadingCountry;
    private final String loadingPlace;
    private final String loadingTerminal;

    private final JustDate unloadingDate;
    private final Long unloadingCountry;
    private final String unloadingPlace;
    private final String unloadingTerminal;

    private Freight(SimpleRow row) {
      super();

      this.orderId = row.getLong(COL_ORDER);

      this.orderStatus = NameUtils.getEnumByIndex(OrderStatus.class, row.getInt(COL_STATUS));
      this.orderDate = row.getDateTime(COL_ORDER_DATE);
      this.orderNo = row.getValue(COL_ORDER_NO);

      this.customerId = row.getLong(COL_CUSTOMER);
      this.customerName = row.getValue(COL_CUSTOMER_NAME);

      this.cargoId = row.getLong(COL_CARGO_ID);
      this.cargoDescription = row.getValue(COL_DESCRIPTION);

      this.loadingPlaceId = row.getLong(COL_LOADING_PLACE);
      this.unloadingPlaceId = row.getLong(COL_UNLOADING_PLACE);

      this.loadingDate = row.getDate(loadingColumnAlias(COL_PLACE_DATE));
      this.loadingCountry = row.getLong(loadingColumnAlias(COL_COUNTRY));
      this.loadingPlace = row.getValue(loadingColumnAlias(COL_PLACE_NAME));
      this.loadingTerminal = row.getValue(loadingColumnAlias(COL_TERMINAL));

      this.unloadingDate = row.getDate(unloadingColumnAlias(COL_PLACE_DATE));
      this.unloadingCountry = row.getLong(unloadingColumnAlias(COL_COUNTRY));
      this.unloadingPlace = row.getValue(unloadingColumnAlias(COL_PLACE_NAME));
      this.unloadingTerminal = row.getValue(unloadingColumnAlias(COL_TERMINAL));
    }

    @Override
    public String toString() {
      return "orderId=" + orderId + ", orderStatus=" + orderStatus + ", orderDate="
          + orderDate + ", orderNo=" + orderNo + ", customerId=" + customerId + ", customerName="
          + customerName + ", cargoId=" + cargoId + ", cargoDescription=" + cargoDescription
          + ", loadingPlaceId=" + loadingPlaceId + ", unloadingPlaceId=" + unloadingPlaceId
          + ", loadingDate=" + loadingDate + ", loadingCountry=" + loadingCountry
          + ", loadingPlace=" + loadingPlace + ", loadingTerminal=" + loadingTerminal
          + ", unloadingDate=" + unloadingDate + ", unloadingCountry=" + unloadingCountry
          + ", unloadingPlace=" + unloadingPlace + ", unloadingTerminal=" + unloadingTerminal;
    }
  }

  static final String supplierKey = "freight_exchange";

  private static final BeeLogger logger = LogUtils.getLogger(FreightExchange.class);

  private static final String STYLE_PREFIX = "bee-tr-fx-";

  private static final String STYLE_CONTAINER = STYLE_PREFIX + "Container";
  private static final String STYLE_CANVAS = STYLE_PREFIX + "Canvas";

  static void open(final Callback<IdentifiableWidget> callback) {
    Assert.notNull(callback);

    BeeKeeper.getRpc().makePostRequest(TransportHandler.createArgs(SVC_GET_FX_DATA),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            FreightExchange fx = new FreightExchange();
            if (fx.setData(response)) {
              callback.onSuccess(fx);
            } else {
              callback.onFailure(fx.getCaption(), "negavo duomenų iš serverio",
                  Global.CONSTANTS.sorry());
            }
          }
        });
  }

  private final HeaderView header;
  private final Complex canvas;

  private final List<HandlerRegistration> registry = Lists.newArrayList();

  private boolean enabled = true;

  private final List<Freight> items = Lists.newArrayList();
  private final List<String> colors = Lists.newArrayList();

  private BeeRowSet settings = null;
  private BeeRowSet countries = null;

  private FreightExchange() {
    super();
    addStyleName(STYLE_CONTAINER);

    this.header = GWT.create(HeaderImpl.class);
    header.create("Užsakymų birža", false, true, EnumSet.of(UiOption.ROOT),
        EnumSet.of(Action.REFRESH, Action.CONFIGURE), null);
    header.setViewPresenter(this);
    add(header);

    this.canvas = new Complex();
    canvas.addStyleName(STYLE_CANVAS);
    add(canvas);
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

    render();
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
    BeeKeeper.getRpc().makePostRequest(TransportHandler.createArgs(SVC_GET_FX_DATA),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            if (setData(response)) {
              render();
            }
          }
        });
  }

  private void render() {
    canvas.clear();
    if (items.isEmpty()) {
      return;
    }

    for (Freight item : items) {
      canvas.add(new BeeLabel(item.toString()));
    }
  }

  private void setCountries(BeeRowSet countries) {
    this.countries = countries;
  }

  private boolean setData(ResponseObject response) {
    if (!Queries.checkResponse(SVC_GET_FX_DATA, null, response, BeeRowSet.class, null)) {
      return false;
    }

    BeeRowSet rowSet = BeeRowSet.restore((String) response.getResponse());
    setSettings(rowSet);

    String serialized = rowSet.getTableProperty(PROP_COUNTRIES);
    if (!BeeUtils.isEmpty(serialized)) {
      setCountries(BeeRowSet.restore(serialized));
    }

    serialized = rowSet.getTableProperty(PROP_COLORS);
    if (!BeeUtils.isEmpty(serialized)) {
      String[] arr = Codec.beeDeserializeCollection(serialized);
      if (arr != null) {
        colors.clear();
        colors.addAll(Arrays.asList(arr));
      }
    }

    serialized = rowSet.getTableProperty(PROP_DATA);
    if (!BeeUtils.isEmpty(serialized)) {
      SimpleRowSet data = SimpleRowSet.restore(serialized);
      items.clear();

      for (SimpleRow row : data) {
        items.add(new Freight(row));
      }
      logger.debug(NameUtils.getName(this), "loaded", items.size(), "items");
    }

    return true;
  }

  private void setSettings(BeeRowSet settings) {
    this.settings = settings;
  }
}
