package com.butent.bee.client.modules.orders.ec;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Settings;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.widget.ItemDetails;
import com.butent.bee.client.ui.AutocompleteProvider;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HtmlEditor;
import com.butent.bee.client.view.ViewCallback;
import com.butent.bee.client.view.ViewFactory;
import com.butent.bee.client.view.ViewSupplier;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.menu.MenuHandler;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.orders.ec.OrdEcItem;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OrdEcKeeper {

  private static final OrdEcPictures pictures = new OrdEcPictures();
  private static OrdEcCommandWidget activeCommand;
  private static String stockLabel;
  private static final InputText searchBox = new InputText();
  private static final Map<String, String> configuration = new HashMap<>();

  private static final String KEY_EC_CONTACTS = MenuService.EDIT_EC_CONTACTS.name().toLowerCase();

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

    requestItems(SVC_GLOBAL_SEARCH, params, new Consumer<List<OrdEcItem>>() {
      @Override
      public void accept(List<OrdEcItem> items) {
        AutocompleteProvider.retainValue(inputWidget);
        resetActiveCommand();

        OrdEcItemPanel widget = new OrdEcItemPanel();
        BeeKeeper.getScreen().show(widget);
        renderItems(widget, items);
      }
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

  public static void openItem(final OrdEcItem item) {
    Assert.notNull(item);

    OrdEcItemDetails widget = new OrdEcItemDetails(item);

    DialogBox dialog = DialogBox.create(item.getName(),
        EcStyles.name(ItemDetails.STYLE_PRIMARY, "dialog"));
    dialog.setWidget(widget);

    dialog.setHideOnEscape(true);
    dialog.setAnimationEnabled(true);

    dialog.cascade();
  }

  public static void register() {
    MenuService.EDIT_EC_CONTACTS.setHandler(new MenuHandler() {
      @Override
      public void onSelection(String parameters) {
        editEcContacts(null);
      }
    });

    ViewFactory.registerSupplier(KEY_EC_CONTACTS, new ViewSupplier() {
      @Override
      public void create(ViewCallback callback) {
        editEcContacts(callback);
      }
    });
  }

  public static void renderItems(final OrdEcItemPanel panel, final List<OrdEcItem> items) {
    Assert.notNull(panel);
    Assert.notNull(items);

    ensureStockLabel(new Consumer<Boolean>() {
      @Override
      public void accept(Boolean input) {
        if (BeeUtils.isTrue(input)) {
          panel.render(items);
        }
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
          }
        }
      }
    });
  }

  public static void searchItems(boolean byCategory, String service, String query,
      final Consumer<List<OrdEcItem>> callback) {

    if (!byCategory) {
      if (!checkSearchQuery(query)) {
        return;
      }
    }

    ParameterList params = createArgs(service);
    params.addDataItem(VAR_QUERY, query);
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

  static void getConfiguration(final Consumer<Map<String, String>> callback) {
    if (configuration.isEmpty()) {
      ParameterList params = createArgs(SVC_GET_CONFIGURATION);
      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          dispatchMessages(response);

          Map<String, String> map = Codec.deserializeMap(response.getResponseAsString());
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

  static InputText getSearchBox() {
    return searchBox;
  }

  static void saveConfiguration(final String key, final String value) {
    ParameterList params;

    if (BeeUtils.isEmpty(value)) {
      params = createArgs(SVC_CLEAR_CONFIGURATION);
      params.addDataItem(Service.VAR_COLUMN, key);
    } else {
      params = createArgs(SVC_SAVE_CONFIGURATION);
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

    getConfiguration(new Consumer<Map<String, String>>() {
      @Override
      public void accept(Map<String, String> input) {
        final String url = input.get(urlColumn);
        final String html = input.get(htmlColumn);

        HtmlEditor editor =
            new HtmlEditor(supplierKey, caption, url, html, new BiConsumer<String, String>() {
              @Override
              public void accept(String newUrl, String newHtml) {
                if (!BeeUtils.equalsTrim(url, newUrl)) {
                  saveConfiguration(urlColumn, newUrl);
                }
                if (!BeeUtils.equalsTrim(html, newHtml)) {
                  saveConfiguration(htmlColumn, newHtml);
                }
              }
            });

        if (callback == null) {
          BeeKeeper.getScreen().show(editor);
        } else {
          callback.onSuccess(editor);
        }
      }
    });
  }

  private static void editEcContacts(ViewCallback callback) {
    editConfigurationHtml(KEY_EC_CONTACTS, Localized.dictionary().ecContacts(),
        EcConstants.COL_CONFIG_CONTACTS_URL, EcConstants.COL_CONFIG_CONTACTS_HTML, callback);
  }

  private static void resetActiveCommand() {
    if (activeCommand != null) {
      activeCommand.deactivate();
      activeCommand = null;
    }
  }

  private OrdEcKeeper() {
  }
}
