package com.butent.bee.client.modules.ec;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.modules.ec.widget.FeaturedAndNovelty;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.modules.ec.EcItemList;

public class EcKeeper {

  public static ParameterList createArgs(String method) {
    ParameterList args = BeeKeeper.getRpc().createParameters(EC_MODULE);
    args.addQueryItem(EC_METHOD, method);
    return args;
  }
  
  public static void register() {
  }
  
  public static void showFeaturedAndNoveltyItems() {
    BeeKeeper.getRpc().makeGetRequest(createArgs(SVC_FEATURED_AND_NOVELTY), new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(EcItemList.class)) {
          EcItemList items = EcItemList.restore(response.getResponseAsString());
          if (!items.isEmpty()) {
            FeaturedAndNovelty widget = new FeaturedAndNovelty(items);
            BeeKeeper.getScreen().updateActivePanel(widget);
          }
        }
      }
    });
  }

  private EcKeeper() {
  }
}
