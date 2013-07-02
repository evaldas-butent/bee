package com.butent.bee.client.modules.ec;

import com.google.common.collect.Lists;

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
import com.butent.bee.client.view.HtmlEditor;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseMessage;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.ec.Cart;
import com.butent.bee.shared.modules.ec.DeliveryMethod;
import com.butent.bee.shared.modules.ec.EcCarModel;
import com.butent.bee.shared.modules.ec.EcCarType;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Captions;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EcKeeper {

  private static final BeeLogger logger = LogUtils.getLogger(EcKeeper.class);
  
  private static final EcData data = new EcData();

  private static final InputText searchBox = new InputText();

  private static final CartList cartList = new CartList();

  private static EcCommandWidget activeCommand = null;

  public static void addToCart(EcItem ecItem, int quantity) {
    cartList.addToCart(ecItem, quantity);
  }

  public static Tree buildCategoryTree(Collection<Integer> categoryIds) {
    Assert.notEmpty(categoryIds);
    return data.buildCategoryTree(categoryIds);
  }

  public static ParameterList createArgs(String method) {
    ParameterList args = BeeKeeper.getRpc().createParameters(EC_MODULE);
    args.addQueryItem(EC_METHOD, method);
    return args;
  }

  public static void dispatchMessages(ResponseObject response) {
    if (response != null && response.hasMessages()) {
      for (ResponseMessage message : response.getMessages()) {
        LogLevel level = message.getLevel();

        if (level == LogLevel.ERROR) {
          BeeKeeper.getScreen().notifySevere(message.getMessage());
        } else if (level == LogLevel.WARNING) {
          BeeKeeper.getScreen().notifyWarning(message.getMessage());
        } else {
          BeeKeeper.getScreen().notifyInfo(message.getMessage());
        }
      }
    }
  }

  public static void doGlobalSearch(String query) {
    Assert.notEmpty(query);

    ParameterList params = createArgs(SVC_GLOBAL_SEARCH);
    params.addDataItem(VAR_QUERY, query);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        long millis = System.currentTimeMillis();
        if (Global.isDebug()) {
          logger.debug(SVC_GLOBAL_SEARCH, "response received");
        }

        dispatchMessages(response);
        List<EcItem> items = getResponseItems(response);

        if (Global.isDebug()) {
          logger.debug("deserialized", items.size(), TimeUtils.elapsedMillis(millis));
        }

        if (!BeeUtils.isEmpty(items)) {
          resetActiveCommand();

          ItemPanel widget = new ItemPanel();
          BeeKeeper.getScreen().updateActivePanel(widget);
          renderItems(widget, items);
        }
      }
    });
  }

  public static void ensureCategoeries(final Consumer<Boolean> callback) {
    Assert.notNull(callback);
    data.ensureCategoeries(callback);
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
      String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());
      if (arr != null) {
        for (String s : arr) {
          items.add(EcItem.restore(s));
        }
      }
    }
    return items;
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

  public static void resetCart(CartType cartType) {
    Cart cart = getCart(cartType);
    if (cart != null) {
      cart.reset();
      cartList.refresh(cartType);
    }
  }

  public static void searchItems(String service, String query, final Consumer<List<EcItem>> callback) {
    Assert.notEmpty(service);
    Assert.notEmpty(query);
    Assert.notNull(callback);

    ParameterList params = createArgs(service);
    params.addDataItem(VAR_QUERY, query);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (!response.hasErrors()) {
          dispatchMessages(response);
          List<EcItem> items = getResponseItems(response);
          if (items != null) {
            callback.accept(items);
          }
        }
      }
    });
  }

  public static void showFeaturedAndNoveltyItems() {
    BeeKeeper.getRpc().makeGetRequest(createArgs(SVC_FEATURED_AND_NOVELTY), new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        dispatchMessages(response);
        List<EcItem> items = getResponseItems(response);

        if (!BeeUtils.isEmpty(items)) {
          resetActiveCommand();

          FeaturedAndNovelty widget = new FeaturedAndNovelty(items);
          BeeKeeper.getScreen().updateActivePanel(widget);
        }
      }
    });
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
