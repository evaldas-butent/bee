package com.butent.bee.client.modules.orders.ec;

import com.google.common.cache.Cache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.gwt.user.client.ui.Panel;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.Settings;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.grid.cell.AbstractCell;
import com.butent.bee.client.modules.classifiers.ClassifierKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.widget.ItemDetails;
import com.butent.bee.client.ui.AutocompleteProvider;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HtmlEditor;
import com.butent.bee.client.view.ViewCallback;
import com.butent.bee.client.view.ViewFactory;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.orders.OrdersConstants;
import com.butent.bee.shared.modules.orders.ec.OrdEcCart;
import com.butent.bee.shared.modules.orders.ec.OrdEcCartItem;
import com.butent.bee.shared.modules.orders.ec.OrdEcItem;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class OrdEcKeeper {

  private static final OrdEcPictures pictures = new OrdEcPictures();
  private static OrdEcCommandWidget activeCommand;
  private static String stockLabel;
  private static final InputText searchBox = new InputText();
  private static final Map<String, String> configuration = new HashMap<>();
  private static final OrdEcCartList cartList = new OrdEcCartList();

  private static final String KEY_EC_CONTACTS = MenuService.EDIT_EC_CONTACTS.name().toLowerCase();

  public static void addToCart(OrdEcItem ecItem, int quantity) {
    cartList.addToCart(ecItem, quantity);
  }

  public static void addPictureCellHandlers(AbstractCell<?> cell, String primaryStyle) {
    Assert.notNull(cell);
    OrdEcPictures.addCellHandlers(cell, primaryStyle);
  }

  public static void closeView(IdentifiableWidget view) {
    BeeKeeper.getScreen().closeWidget(view);
    showPromo(true);
  }

  public static ParameterList createArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.ORDERS, method);
  }

  public static void dispatchMessages(ResponseObject response) {
    if (response != null) {
      response.notify(BeeKeeper.getScreen());
    }
  }

  public static void doGlobalSearch(String query, final IdentifiableWidget inputWidget) {
    if (!checkSearchQuery(query)) {
      return;
    }

    ParameterList params = createArgs(SVC_GLOBAL_SEARCH);
    params.addDataItem(VAR_QUERY, query);
    params.addDataItem(ClassifierConstants.COL_COMPANY, BeeKeeper.getUser().getCompany());
    params.addDataItem("Clicked", 0);

    requestItems(SVC_GLOBAL_SEARCH, params, items -> {
      AutocompleteProvider.retainValue(inputWidget);
      resetActiveCommand();

      OrdEcItemPanel widget = new OrdEcItemPanel(false, SVC_GLOBAL_SEARCH);
      widget.setQuery(query);
      BeeKeeper.getScreen().show(widget);
      renderItems(widget, items);
    });
  }

  public static void ensureStockLabel(Consumer<Boolean> callback) {
    ParameterList params = createArgs(SVC_GET_CLIENT_STOCK_LABELS);
    params.addDataItem(ClassifierConstants.COL_COMPANY, BeeKeeper.getUser().getCompany());

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        OrdEcKeeper.dispatchMessages(response);

        if (!BeeUtils.isEmpty(response.getResponseAsString())) {
          stockLabel = response.getResponseAsString();
          callback.accept(true);
        } else {
          callback.accept(true);
        }
      }
    });
  }

  public static OrdEcCart getCart() {
    return cartList.getCart();
  }

  public static Cache<Long, ImmutableList<String>> getPictures() {
    return pictures.getPictures();
  }

  public static List<OrdEcItem> getResponseItems(ResponseObject response) {
    if (response == null) {
      return new ArrayList<>();
    } else {
      return deserializeItems(response.getResponseAsString());
    }
  }

  public static String getStockLabel() {
    return stockLabel;
  }

  public static int getQuantityInCart(long itemId) {
    return cartList.getQuantity(itemId);
  }

  public static void maybeRecalculatePrices(OrdEcItem item, int quantity,
      Consumer<Boolean> consumer) {
    Map<String, Long> options = new HashMap<>();
    Map<Long, Double> quantities = new HashMap<>();
    Map<Long, ItemPrice> priceNames = new HashMap<>();

    quantities.put(item.getId(), (double) quantity);
    options.put(ClassifierConstants.COL_DISCOUNT_COMPANY, BeeKeeper.getUser().getCompany());

    ClassifierKeeper.getPricesAndDiscounts(options, quantities.keySet(), quantities, priceNames,
        result -> {
          Pair<Double, Double> prices = result.get(item.getId());

          if (prices != null && BeeUtils.isDouble(prices.getA())
              && BeeUtils.isDouble(prices.getB())) {
            item.setPrice(prices.getA() - prices.getA() * prices.getB() / 100.0);
          } else {
            item.setPrice((double) item.getDefPrice() / 100);
          }

          if (consumer != null) {
            consumer.accept(true);
          }
        });
  }

  public static void openCart() {

    OrdEcCart cart = getCart();
    OrdEcShoppingCart widget = new OrdEcShoppingCart(cart);

    resetActiveCommand();
    searchBox.clearValue();

    BeeKeeper.getScreen().show(widget);
  }

  public static void openItem(final OrdEcItem item, boolean allowAddToCart) {
    Assert.notNull(item);

    OrdEcItemDetails widget = new OrdEcItemDetails(item, allowAddToCart);

    DialogBox dialog = DialogBox.create(item.getName(),
        EcStyles.name(ItemDetails.STYLE_PRIMARY, "dialog"));
    dialog.setWidget(widget);

    dialog.setHideOnEscape(true);
    dialog.setAnimationEnabled(true);

    dialog.cascade();
  }

  public static void persistCartItem(OrdEcCartItem cartItem) {
    Assert.notNull(cartItem);
    persistCartItem(cartItem.getEcItem().getId(), cartItem.getQuantity());
  }

  public static OrdEcCart refreshCart() {
    cartList.refresh();
    return getCart();
  }

  public static void resetCart() {
    OrdEcCart cart = getCart();
    if (cart != null) {
      cart.reset();
      cartList.refresh();
    }
  }

  public static void restoreShoppingCarts(Consumer<Boolean> consumer) {
    BeeKeeper.getRpc().makeGetRequest(createArgs(SVC_GET_SHOPPING_CARTS), new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse()) {
          String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());

          if (arr != null) {

            for (String s : arr) {
              OrdEcCartItem cartItem = OrdEcCartItem.restore(s);
              cartList.getCart().add(cartItem.getEcItem(), cartItem.getQuantity());
            }
          }
          cartList.refresh();

          if (consumer != null && cartList.getCart().getItems().size() > 0) {
            consumer.accept(true);
          }
        }
      }
    });
  }

  public static void register() {
    MenuService.EDIT_ORD_EC_CONTACTS.setHandler(parameters -> editEcContacts(null));

    ViewFactory.registerSupplier(KEY_EC_CONTACTS, OrdEcKeeper::editEcContacts);
  }

  public static OrdEcCart removeFromCart(OrdEcItem ecItem) {
    if (cartList.removeFromCart(ecItem)) {
      persistCartItem(ecItem.getId(), 0);
    }
    return getCart();
  }

  public static void renderItems(final OrdEcItemPanel panel, final List<OrdEcItem> items) {
    Assert.notNull(panel);
    Assert.notNull(items);

    ensureStockLabel(input -> {
      if (BeeUtils.isTrue(input)) {
        panel.render(items);
      }
    });
  }

  public static void requestItems(String service, ParameterList params,
      final Consumer<List<OrdEcItem>> callback) {

    Assert.notEmpty(service);
    Assert.notNull(params);
    Assert.notNull(callback);

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        if (!response.hasErrors()) {
          List<OrdEcItem> items = getResponseItems(response);
          if (!items.isEmpty()) {
            callback.accept(items);
          } else {
            BeeKeeper.getScreen().notifyWarning(Localized.dictionary().nothingFound());
          }
        }
      }
    });
  }

  public static void searchItems(boolean byCategory, String service, String query, int clickedTimes,
      final Consumer<List<OrdEcItem>> callback) {

    if (!byCategory) {
      if (!checkSearchQuery(query)) {
        return;
      }
    }

    ParameterList params = createArgs(service);
    params.addDataItem(VAR_QUERY, query);
    params.addDataItem("Clicked", clickedTimes);
    params.addDataItem(ClassifierConstants.COL_COMPANY, BeeKeeper.getUser().getCompany());

    requestItems(service, params, callback);
  }

  public static void setBackgroundPicture(Long itemId, OrdEcItemPicture widget) {
    Assert.notNull(itemId);
    Assert.notNull(widget);

    Multimap<Long, OrdEcItemPicture> itemWidgets = ArrayListMultimap.create();
    itemWidgets.put(itemId, widget);

    setBackgroundPictures(itemWidgets);
  }

  public static void setBackgroundPictures(Multimap<Long, OrdEcItemPicture> itemWidgets) {
    Assert.notNull(itemWidgets);
    pictures.setBackground(itemWidgets);
  }

  public static boolean showGlobalSearch() {
    return Settings.getBoolean("showGlobalSearch");
  }

  public static void showPromo(final boolean checkView) {
    ParameterList params = createArgs(SVC_GET_PROMO);

    List<RowInfo> cachedBannerInfo = pictures.getCachedBannerInfo();
    if (!BeeUtils.isEmpty(cachedBannerInfo)) {
      params.addDataItem(EcConstants.VAR_BANNERS, Codec.beeSerialize(cachedBannerInfo));
    }

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        dispatchMessages(response);

        if (response.hasResponse()) {
          String banner = response.getResponseAsString();
          if (!BeeUtils.isEmpty(banner)) {
            pictures.setBanners(BeeRowSet.restore(banner));
          }
        }

        if (checkView && BeeKeeper.getScreen().getActiveWidget() instanceof Panel) {
          return;
        }
        if (DataUtils.isEmpty(pictures.getBanners())) {
          return;
        }

        resetActiveCommand();
        OrdEcBanner widget = new OrdEcBanner(pictures.getBanners());

        BeeKeeper.getScreen().show(widget);
      }
    });
  }

  static void doCommand(OrdEcCommandWidget commandWidget) {
    OrdEcView ecView = OrdEcView.create(commandWidget.getService());
    if (ecView != null) {
      searchBox.clearValue();
      BeeKeeper.getScreen().show(ecView);
    }

    if (activeCommand == null || !activeCommand.getService().equals(commandWidget.getService())) {
      if (activeCommand != null) {
        activeCommand.deactivate();
      }

      activeCommand = commandWidget;
      activeCommand.activate();
    }
  }

  static OrdEcCartList getCartlist() {
    return cartList;
  }

  static void getConfiguration(final Consumer<Map<String, String>> callback) {
    if (configuration.isEmpty()) {
      ParameterList params = createArgs(SVC_EC_GET_CONFIGURATION);
      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          dispatchMessages(response);

          Map<String, String> map = Codec.deserializeHashMap(response.getResponseAsString());
          if (!map.isEmpty()) {
            configuration.clear();
            configuration.putAll(map);

            callback.accept(map);
          }
        }
      });

    } else {
      callback.accept(configuration);
    }
  }

  static void getDocuments(Long itemId, Consumer<Multimap<String, Pair<String, String>>> consumer) {
    ParameterList params = createArgs(SVC_EC_GET_DOCUMENTS);
    params.addDataItem(ClassifierConstants.COL_ITEM, itemId);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (!response.hasErrors()) {
          Multimap<String, Pair<String, String>> documents = HashMultimap.create();
          Map<String, String> map = Codec.deserializeHashMap(response.getResponseAsString());
          for (String key : map.keySet()) {
            for (String value : Codec.beeDeserializeCollection(map.get(key))) {
              documents.put(key, Pair.restore(value));
            }
          }
          consumer.accept(documents);
        }
      }
    });
  }

  static InputText getSearchBox() {
    return searchBox;
  }

  static void saveConfiguration(final String key, final String value) {
    ParameterList params;

    if (BeeUtils.isEmpty(value)) {
      params = createArgs(SVC_EC_CLEAR_CONFIGURATION);
      params.addDataItem(Service.VAR_COLUMN, key);
    } else {
      params = createArgs(SVC_EC_SAVE_CONFIGURATION);
      params.addQueryItem(Service.VAR_COLUMN, key);
      params.addDataItem(Service.VAR_VALUE, value);
    }

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        dispatchMessages(response);
        if (BeeUtils.same(key, response.getResponseAsString())) {
          configuration.put(key, value);
        }
      }
    });
  }

  private static boolean checkSearchQuery(String query) {
    if (BeeUtils.hasLength(BeeUtils.trim(query), MIN_SEARCH_QUERY_LENGTH)) {
      return true;
    } else {
      BeeKeeper.getScreen().notifyWarning(
          Localized.dictionary().searchQueryRestriction(MIN_SEARCH_QUERY_LENGTH));
      return false;
    }
  }

  private static List<OrdEcItem> deserializeItems(String serialized) {
    List<OrdEcItem> items = new ArrayList<>();

    if (serialized != null) {

      String[] arr = Codec.beeDeserializeCollection(serialized);
      if (arr != null) {
        for (String s : arr) {
          items.add(OrdEcItem.restore(s));
        }
      }
    }

    return items;
  }

  private static void editConfigurationHtml(final String supplierKey, final String caption,
      final String urlColumn, final String htmlColumn, final ViewCallback callback) {

    getConfiguration(input -> {
      final String url = input.get(urlColumn);
      final String html = input.get(htmlColumn);

      HtmlEditor editor =
          new HtmlEditor(supplierKey, caption, url, html, (newUrl, newHtml) -> {
            if (!BeeUtils.equalsTrim(url, newUrl)) {
              saveConfiguration(urlColumn, newUrl);
            }
            if (!BeeUtils.equalsTrim(html, newHtml)) {
              saveConfiguration(htmlColumn, newHtml);
            }
          });

      if (callback == null) {
        BeeKeeper.getScreen().show(editor);
      } else {
        callback.onSuccess(editor);
      }
    });
  }

  private static void editEcContacts(ViewCallback callback) {
    editConfigurationHtml(KEY_EC_CONTACTS, Localized.dictionary().ecContacts(),
        EcConstants.COL_CONFIG_CONTACTS_URL, EcConstants.COL_CONFIG_CONTACTS_HTML, callback);
  }

  public static void openShoppinCart(Consumer<Boolean> consumer) {
    restoreShoppingCarts(consumer);
  }

  private static void persistCartItem(long itemId, int quantity) {
    ParameterList params = createArgs(SVC_UPDATE_SHOPPING_CART);

    params.addDataItem(COL_SHOPPING_CART_ITEM, itemId);
    params.addDataItem(COL_SHOPPING_CART_QUANTITY, quantity);

    BeeKeeper.getRpc().makeRequest(params);
  }

  private static void resetActiveCommand() {
    if (activeCommand != null) {
      activeCommand.deactivate();
      activeCommand = null;
    }
  }

  private OrdEcKeeper() {
  }

  public static void saveOrder(String comment, Object object, Consumer<Boolean> consumer) {
    Global.inputString(Localized.dictionary().ordShoppingCartName(), new StringCallback() {

      @Override
      public void onSuccess(String value) {
        if (!BeeUtils.isEmpty(value)) {
          ParameterList params = OrdEcKeeper.createArgs(OrdersConstants.SVC_SAVE_ORDER);
          params.addDataItem(COL_SHOPPING_CART_NAME, value);
          params.addDataItem(COL_SHOPPING_CART_COMMENT, comment);

          BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

            @Override
            public void onResponse(ResponseObject response) {
              OrdEcKeeper.dispatchMessages(response);

              if (!response.hasErrors()) {
                OrdEcKeeper.resetCart();
                if (object instanceof OrdEcShoppingCart) {
                  OrdEcKeeper.closeView((OrdEcShoppingCart) object);
                } else if (object instanceof NotSubmittedOrders) {
                  OrdEcKeeper.closeView((NotSubmittedOrders) object);
                }

                if (consumer != null) {
                  consumer.accept(true);
                }
              }
            }
          });
        } else {
          super.setRequired(true);
        }
      }
    }, BeeConst.CSS_CLASS_PREFIX + "ec-saveOrder");
  }
}
