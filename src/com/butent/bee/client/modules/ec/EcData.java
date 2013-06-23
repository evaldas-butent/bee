package com.butent.bee.client.modules.ec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.ec.EcCarModel;
import com.butent.bee.shared.modules.ec.EcCarType;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.Map;

class EcData {
  
  private final List<String> carManufacturers = Lists.newArrayList();
  private final Map<String, List<EcCarModel>> carModelsByManufacturer = Maps.newHashMap();
  private final Map<Integer, List<EcCarType>> carTypesByModel = Maps.newHashMap();

  EcData() {
    super();
  }
  
  void getCarManufacturers(final Consumer<List<String>> callback) {
    if (carManufacturers.isEmpty()) {
      ParameterList params = EcKeeper.createArgs(SVC_GET_CAR_MANUFACTURERS);
      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          EcKeeper.dispatchMessages(response);
          LogUtils.getRootLogger().debug(response.getType(), response.getResponseAsString().length());
          
          String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());

          if (arr != null) {
            carManufacturers.clear();
            for (String manufacturer : arr) {
              carManufacturers.add(manufacturer);
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
          LogUtils.getRootLogger().debug(response.getType(), response.getResponseAsString().length());
          
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
  
  void getCarTypes(final int modelId, final Consumer<List<EcCarType>> callback) {
    if (carTypesByModel.containsKey(modelId)) {
      callback.accept(carTypesByModel.get(modelId));

    } else {
      ParameterList params = EcKeeper.createArgs(SVC_GET_CAR_TYPES);
      params.addQueryItem(VAR_MODEL, modelId);

      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          EcKeeper.dispatchMessages(response);
          LogUtils.getRootLogger().debug(response.getType(), response.getResponseAsString().length());
          
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
  
}
