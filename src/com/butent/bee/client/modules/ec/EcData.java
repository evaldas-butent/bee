package com.butent.bee.client.modules.ec;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.DeliveryMethod;
import com.butent.bee.shared.modules.ec.EcCarModel;
import com.butent.bee.shared.modules.ec.EcCarType;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

class EcData {

  private final List<String> carManufacturers = Lists.newArrayList();
  private final Map<String, List<EcCarModel>> carModelsByManufacturer = Maps.newHashMap();
  private final Map<Long, List<EcCarType>> carTypesByModel = Maps.newHashMap();

  private final Map<Long, String> categoryNames = Maps.newHashMap();

  private final Set<Long> categoryRoots = Sets.newHashSet();
  private final Multimap<Long, Long> categoryByParent = HashMultimap.create();
  private final Map<Long, Long> categoryByChild = Maps.newHashMap();

  private final List<String> itemManufacturers = Lists.newArrayList();

  private final List<DeliveryMethod> deliveryMethods = Lists.newArrayList();

  private final Map<String, String> configuration = Maps.newHashMap();

  EcData() {
    super();
  }

  Tree buildCategoryTree(Collection<Long> ids) {
    Set<Long> roots = Sets.newHashSet();
    Multimap<Long, Long> data = HashMultimap.create();
    
    for (long id : ids) {
      Long parent = getParent(id, ids);
      if (parent == null) {
        roots.add(id);
      } else {
        data.put(parent, id);
      }
    }

    Tree tree = new Tree();

    TreeItem rootItem = new TreeItem(Localized.getConstants().ecSelectCategory());
    tree.addItem(rootItem);

    for (long id : roots) {
      TreeItem treeItem = createCategoryTreeItem(id);
      rootItem.addItem(treeItem);
      
      fillTree(data, id, treeItem);
    }

    return tree;
  }
  
  void ensureCategoeries(final Consumer<Boolean> callback) {
    if (categoryNames.isEmpty()) {
      ParameterList params = EcKeeper.createArgs(SVC_GET_CATEGORIES);
      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          EcKeeper.dispatchMessages(response);
          String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());

          if (arr != null) {
            categoryNames.clear();
            categoryRoots.clear();
            categoryByParent.clear();
            categoryByChild.clear();

            for (int i = 0; i < arr.length; i += 3) {
              long id = BeeUtils.toLong(arr[i]);
              long parent = BeeUtils.toLong(arr[i + 1]);
              String name = arr[i + 2];

              categoryNames.put(id, name);

              if (parent > 0) {
                categoryByParent.put(parent, id);
                categoryByChild.put(id, parent);
              } else {
                categoryRoots.add(id);
              }
            }

            callback.accept(true);
          }
        }
      });

    } else {
      callback.accept(true);
    }
  }

  void getCarManufacturers(final Consumer<List<String>> callback) {
    if (carManufacturers.isEmpty()) {
      ParameterList params = EcKeeper.createArgs(SVC_GET_CAR_MANUFACTURERS);
      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          EcKeeper.dispatchMessages(response);
          String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());

          if (arr != null) {
            carManufacturers.clear();
            for (String manufacturer : arr) {
              if (!BeeUtils.isEmpty(manufacturer)) {
                carManufacturers.add(manufacturer);
              }
            }

            callback.accept(carManufacturers);
          }
        }
      });

    } else {
      callback.accept(carManufacturers);
    }
  }
  
  void getCarModels(final String manufacturer, final Consumer<List<EcCarModel>> callback) {
    if (carModelsByManufacturer.containsKey(manufacturer)) {
      callback.accept(carModelsByManufacturer.get(manufacturer));

    } else {
      ParameterList params = EcKeeper.createArgs(SVC_GET_CAR_MODELS);
      params.addDataItem(VAR_MANUFACTURER, manufacturer);

      BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          EcKeeper.dispatchMessages(response);
          String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());

          if (arr != null) {
            List<EcCarModel> carModels = Lists.newArrayList();
            for (String s : arr) {
              carModels.add(EcCarModel.restore(s));
            }
            carModelsByManufacturer.put(manufacturer, carModels);

            callback.accept(carModels);
          }
        }
      });
    }
  }

  void getCarTypes(final long modelId, final Consumer<List<EcCarType>> callback) {
    if (carTypesByModel.containsKey(modelId)) {
      callback.accept(carTypesByModel.get(modelId));

    } else {
      ParameterList params = EcKeeper.createArgs(SVC_GET_CAR_TYPES);
      params.addQueryItem(VAR_MODEL, modelId);

      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          EcKeeper.dispatchMessages(response);
          String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());

          if (arr != null) {
            List<EcCarType> carTypes = Lists.newArrayList();
            for (String s : arr) {
              carTypes.add(EcCarType.restore(s));
            }
            carTypesByModel.put(modelId, carTypes);

            callback.accept(carTypes);
          }
        }
      });
    }
  }

  String getCategoryName(long categoryId) {
    return categoryNames.get(categoryId);
  }

  List<String> getCategoryNames(EcItem item) {
    List<String> names = Lists.newArrayList();

    List<Long> categoryIds = item.getCategoryList();
    for (Long categoryId : categoryIds) {
      String name = categoryNames.get(categoryId);
      if (name != null) {
        names.add(name);
      }
    }

    return names;
  }
  
  void saveConfiguration(final String key, final String value) {
    ParameterList params;

    if (BeeUtils.isEmpty(value)) {
      params = EcKeeper.createArgs(SVC_CLEAR_CONFIGURATION);
      params.addDataItem(Service.VAR_COLUMN, key);
    } else {
      params = EcKeeper.createArgs(SVC_SAVE_CONFIGURATION);
      params.addQueryItem(Service.VAR_COLUMN, key);
      params.addDataItem(Service.VAR_VALUE, value);
    }

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        EcKeeper.dispatchMessages(response);
        if (BeeUtils.same(key, response.getResponseAsString())) {
          configuration.put(key, value);
        }
      }
    });
  }

  void getConfiguration(final Consumer<Map<String, String>> callback) {
    if (configuration.isEmpty()) {
      ParameterList params = EcKeeper.createArgs(SVC_GET_CONFIGURATION);
      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          EcKeeper.dispatchMessages(response);

          Map<String, String> map = Codec.beeDeserializeMap(response.getResponseAsString());
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
  
  void getDeliveryMethods(final Consumer<List<DeliveryMethod>> callback) {
    if (!deliveryMethods.isEmpty()) {
      callback.accept(deliveryMethods);

    } else {
      ParameterList params = EcKeeper.createArgs(SVC_GET_DELIVERY_METHODS);

      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          EcKeeper.dispatchMessages(response);
          String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());

          if (arr != null) {
            deliveryMethods.clear();
            for (String s : arr) {
              deliveryMethods.add(DeliveryMethod.restore(s));
            }

            callback.accept(deliveryMethods);
          }
        }
      });
    }
  }
  
  void getItemManufacturers(final Consumer<List<String>> callback) {
    if (itemManufacturers.isEmpty()) {
      ParameterList params = EcKeeper.createArgs(SVC_GET_ITEM_MANUFACTURERS);
      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          EcKeeper.dispatchMessages(response);
          String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());

          if (arr != null) {
            itemManufacturers.clear();
            for (String manufacturer : arr) {
              if (!BeeUtils.isEmpty(manufacturer)) {
                itemManufacturers.add(manufacturer);
              }
            }

            callback.accept(itemManufacturers);
          }
        }
      });

    } else {
      callback.accept(itemManufacturers);
    }
  }

  private TreeItem createCategoryTreeItem(long id) {
    TreeItem treeItem = new TreeItem(categoryNames.get(id));
    treeItem.setUserObject(id);
    
    return treeItem;
  }

  private void fillTree(Multimap<Long, Long> data, long parent, TreeItem parentItem) {
    if (data.containsKey(parent)) {
      for (long id : data.get(parent)) {
        TreeItem childItem = createCategoryTreeItem(id);
        parentItem.addItem(childItem);
        
        fillTree(data, id, childItem);
      }
    }
  }

  private Long getParent(long categoryId, Collection<Long> filter) {
    for (Long parent = categoryByChild.get(categoryId); parent != null; parent =
        categoryByChild.get(parent)) {
      if (filter.contains(parent)) {
        return parent;
      }
    }
    return null;
  }
}
