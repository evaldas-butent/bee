package com.butent.bee.client.modules.ec;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Panel;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.MenuManager.MenuCallback;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.modules.ec.view.EcView;
import com.butent.bee.client.modules.ec.view.ShoppingCart;
import com.butent.bee.client.modules.ec.widget.CartList;
import com.butent.bee.client.modules.ec.widget.FeaturedAndNovelty;
import com.butent.bee.client.modules.ec.widget.ItemPanel;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HtmlEditor;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.ec.Cart;
import com.butent.bee.shared.modules.ec.DeliveryMethod;
import com.butent.bee.shared.modules.ec.EcCarModel;
import com.butent.bee.shared.modules.ec.EcCarType;
import com.butent.bee.shared.modules.ec.EcConstants.CartType;
import com.butent.bee.shared.modules.ec.EcConstants.EcClientType;
import com.butent.bee.shared.modules.ec.EcConstants.EcOrderStatus;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Captions;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EcKeeper {

  private static final BeeLogger logger = LogUtils.getLogger(EcKeeper.class);

  private static final EcData data = new EcData();

  private static final InputText searchBox = new InputText();

  private static final CartList cartList = new CartList();

  private static EcCommandWidget activeCommand = null;

  private static boolean debug = false;

  private static Set<EcRequest> pendingRequests = Sets.newHashSet();

  public static void addToCart(EcItem ecItem, int quantity) {
    cartList.addToCart(ecItem, quantity);
  }

  public static Tree buildCategoryTree(Collection<Integer> categoryIds) {
    Assert.notEmpty(categoryIds);
    return data.buildCategoryTree(categoryIds);
  }

  public static boolean checkResponse(EcRequest request, ResponseObject response) {
    boolean pending = pendingRequests.contains(request);
    if (isDebug()) {
      logger.debug(request.getService(), request.getQuery());
      logger.debug("response received", request.elapsedMillis(), pending);
    }

    if (pending) {
      dispatchMessages(response);
    }
    return pending && response.hasResponse();
  }

  public static void closeView(IdentifiableWidget view) {
    BeeKeeper.getScreen().closeWidget(view);
    showFeaturedAndNoveltyItems(true);
  }

  public static ParameterList createArgs(String method) {
    ParameterList args = BeeKeeper.getRpc().createParameters(EC_MODULE);
    args.addQueryItem(EC_METHOD, method);
    return args;
  }

  public static void dispatchMessages(ResponseObject response) {
    if (response != null) {
      response.notify(BeeKeeper.getScreen());
    }
  }

  public static void doGlobalSearch(String query) {
    if (!checkSearchQuery(query)) {
      return;
    }

    ParameterList params = createArgs(SVC_GLOBAL_SEARCH);
    params.addDataItem(VAR_QUERY, query);
    
    requestItems(SVC_GLOBAL_SEARCH, query, params, new Consumer<List<EcItem>>() {
      @Override
      public void accept(List<EcItem> items) {
        resetActiveCommand();

        ItemPanel widget = new ItemPanel();
        BeeKeeper.getScreen().updateActivePanel(widget);
        renderItems(widget, items);
      }
    });
  }

  public static void ensureCategoeries(final Consumer<Boolean> callback) {
    Assert.notNull(callback);
    data.ensureCategoeries(callback);
  }
  
  public static void finalizeRequest(EcRequest request, boolean remove) {
    if (request.hasProgress()) {
      BeeKeeper.getScreen().closeProgress(request.getProgressId());
      request.setProgressId(null);
    }

    if (remove) {
      pendingRequests.remove(request);
    }
  }

  public static void getCarManufacturers(Consumer<List<String>> callback) {
    Assert.notNull(callback);
    data.getCarManufacturers(callback);
  }

  public static void getCarModels(String manufacturer, Consumer<List<EcCarModel>> callback) {
    Assert.notEmpty(manufacturer);
    Assert.notNull(callback);
    data.getCarModels(manufacturer, callback);
  }

  public static Cart getCart(CartType cartType) {
    return cartList.getCart(cartType);
  }

  public static void getCarTypes(Integer modelId, Consumer<List<EcCarType>> callback) {
    Assert.notNull(modelId);
    Assert.notNull(callback);
    data.getCarTypes(modelId, callback);
  }

  public static String getCategoryName(Integer categoryId) {
    Assert.notNull(categoryId);
    return data.getCategoryName(categoryId);
  }

  public static List<String> getCategoryNames(EcItem item) {
    Assert.notNull(item);
    return data.getCategoryNames(item);
  }

  public static void getConfiguration(Consumer<Map<String, String>> callback) {
    Assert.notNull(callback);
    data.getConfiguration(callback);
  }

  public static void getItemManufacturers(Consumer<List<String>> callback) {
    Assert.notNull(callback);
    data.getItemManufacturers(callback);
  }

  public static List<EcItem> getResponseItems(ResponseObject response) {
    List<EcItem> items = Lists.newArrayList();

    if (response != null) {
      long millis = System.currentTimeMillis();

      String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());
      if (arr != null) {
        for (String s : arr) {
          items.add(EcItem.restore(s));
        }
      }

      if (isDebug()) {
        logger.debug("deserialized items", items.size(), TimeUtils.elapsedMillis(millis));
      }
    }

    return items;
  }

  public static boolean isDebug() {
    return debug;
  }

  public static EcRequest maybeCreateRequest(String service, String query) {
    if (!pendingRequests.isEmpty()) {
      for (EcRequest request : pendingRequests) {
        if (request.sameService(service) && request.sameQuery(query)) {
          return null;
        }
      }

      for (EcRequest request : pendingRequests) {
        finalizeRequest(request, false);
      }
      pendingRequests.clear();
    }

    return new EcRequest(service, query);
  }

  public static void onRequestStart(final EcRequest request, int requestId) {
    request.setRequestId(requestId);

    Image cancel = new Image(Global.getImages().closeSmall());
    cancel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        cancelRequest(request);
      }
    });

    String progressId = BeeKeeper.getScreen().createProgress(request.getQuery(), null, cancel);
    request.setProgressId(progressId);

    pendingRequests.add(request);
  }

  public static void openCart(final CartType cartType) {
    data.getDeliveryMethods(new Consumer<List<DeliveryMethod>>() {
      @Override
      public void accept(List<DeliveryMethod> input) {
        Cart cart = getCart(cartType);
        ShoppingCart widget = new ShoppingCart(cartType, cart, input);

        resetActiveCommand();
        searchBox.clearValue();

        BeeKeeper.getScreen().updateActivePanel(widget);
      }
    });
  }

  public static Cart refreshCart(CartType cartType) {
    cartList.refresh(cartType);
    return getCart(cartType);
  }

  public static void register() {
    String key = Captions.register(EcClientType.class);
    Captions.registerColumn(VIEW_REGISTRATIONS, COL_REGISTRATION_TYPE, key);
    Captions.registerColumn(VIEW_CLIENTS, COL_CLIENT_TYPE, key);

    key = Captions.register(EcOrderStatus.class);
    Captions.registerColumn(VIEW_ORDERS, COL_ORDER_STATUS, key);

    BeeKeeper.getMenu().registerMenuCallback("edit_terms_of_delivery", new MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        editConfigurationHtml(Localized.constants.ecTermsOfDelivery(), COL_CONFIG_TOD_URL,
            COL_CONFIG_TOD_HTML);
      }
    });

    BeeKeeper.getMenu().registerMenuCallback("edit_ec_contacts", new MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        editConfigurationHtml(Localized.constants.ecContacts(), COL_CONFIG_CONTACTS_URL,
            COL_CONFIG_CONTACTS_HTML);
      }
    });
  }

  public static Cart removeFromCart(CartType cartType, EcItem ecItem) {
    cartList.removeFromCart(cartType, ecItem);
    return getCart(cartType);
  }

  public static void renderItems(final ItemPanel panel, final List<EcItem> items) {
    Assert.notNull(panel);
    Assert.notNull(items);

    ensureCategoeries(new Consumer<Boolean>() {
      @Override
      public void accept(Boolean input) {
        if (BeeUtils.isTrue(input)) {
          panel.render(items);
        }
      }
    });
  }

  public static void requestItems(String service, String query, ParameterList params,
      final Consumer<List<EcItem>> callback) {

    Assert.notEmpty(service);
    Assert.notNull(params);
    Assert.notNull(callback);

    final EcRequest request = maybeCreateRequest(service, query);
    if (request == null) {
      return;
    }

    int requestId = BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (checkResponse(request, response)) {
          List<EcItem> items = getResponseItems(response);
          if (!items.isEmpty()) {
            callback.accept(items);
          }
        }

        finalizeRequest(request, true);
      }
    });

    onRequestStart(request, requestId);
  }

  public static void resetCart(CartType cartType) {
    Cart cart = getCart(cartType);
    if (cart != null) {
      cart.reset();
      cartList.refresh(cartType);
    }
  }

  public static void searchItems(String service, String query,
      final Consumer<List<EcItem>> callback) {

    if (!checkSearchQuery(query)) {
      return;
    }

    ParameterList params = createArgs(service);
    params.addDataItem(VAR_QUERY, query);

    requestItems(service, query, params, callback);
  }

  public static void showFeaturedAndNoveltyItems(final boolean checkView) {
    ParameterList params = createArgs(SVC_FEATURED_AND_NOVELTY);

    requestItems(SVC_FEATURED_AND_NOVELTY, null, params, new Consumer<List<EcItem>>() {
      @Override
      public void accept(List<EcItem> items) {
        if (checkView && BeeKeeper.getScreen().getActiveWidget() instanceof Panel) {
          return;
        }
        resetActiveCommand();
        
        FeaturedAndNovelty widget = new FeaturedAndNovelty(items);
        BeeKeeper.getScreen().updateActivePanel(widget);
      }
    });
  }

  public static boolean toggleDebug() {
    return debug = !isDebug();
  }

  static void doCommand(EcCommandWidget commandWidget) {
    EcView ecView = EcView.create(commandWidget.getService());
    if (ecView != null) {
      searchBox.clearValue();
      BeeKeeper.getScreen().updateActivePanel(ecView);
    }

    if (activeCommand == null || !activeCommand.getService().equals(commandWidget.getService())) {
      if (activeCommand != null) {
        activeCommand.deactivate();
      }

      activeCommand = commandWidget;
      activeCommand.activate();
    }
  }

  static CartList getCartlist() {
    return cartList;
  }

  static InputText getSearchBox() {
    return searchBox;
  }

  private static void cancelRequest(EcRequest request) {
    BeeKeeper.getRpc().cancelRequest(request.getRequestId());
    finalizeRequest(request, true);
  }

  private static boolean checkSearchQuery(String query) {
    if (BeeUtils.hasLength(BeeUtils.trim(query), MIN_SEARCH_QUERY_LENGTH)) {
      return true;
    } else {
      BeeKeeper.getScreen().notifyWarning(
          Localized.messages.minSearchQueryLength(MIN_SEARCH_QUERY_LENGTH));
      return false;
    }
  }

  private static void editConfigurationHtml(final String caption, final String urlColumn,
      final String htmlColumn) {

    data.getConfiguration(new Consumer<Map<String, String>>() {
      @Override
      public void accept(Map<String, String> input) {
        final String url = input.get(urlColumn);
        final String html = input.get(htmlColumn);

        HtmlEditor editor = new HtmlEditor(caption, url, html, new BiConsumer<String, String>() {
          @Override
          public void accept(String newUrl, String newHtml) {
            if (!BeeUtils.equalsTrim(url, newUrl)) {
              data.saveConfiguration(urlColumn, newUrl);
            }
            if (!BeeUtils.equalsTrim(html, newHtml)) {
              data.saveConfiguration(htmlColumn, newHtml);
            }
          }
        });

        BeeKeeper.getScreen().updateActivePanel(editor);
      }
    });
  }

  private static void resetActiveCommand() {
    if (activeCommand != null) {
      activeCommand.deactivate();
      activeCommand = null;
    }
  }

  private EcKeeper() {
  }
}
