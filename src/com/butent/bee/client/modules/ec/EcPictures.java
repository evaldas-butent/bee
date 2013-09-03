package com.butent.bee.client.modules.ec;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.modules.ec.widget.ItemPicture;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.Set;

class EcPictures {

  private static final BeeLogger logger = LogUtils.getLogger(EcPictures.class);

  private static boolean hasPictures(ResponseObject response) {
    return response != null && !response.hasErrors() && response.hasResponse()
        && !response.hasResponse(String.class);
  }

  private static boolean isPicture(String picture) {
    return picture != null && picture.startsWith(EcConstants.PICTURE_PREFIX);
  }

  private static void setBackgroundImage(Collection<ItemPicture> widgets, String picture) {
    if (widgets != null) {
      for (ItemPicture widget : widgets) {
        if (widget != null) {
          widget.setPicture(picture);
        }
      }
    }
  }

  private final Cache<Long, String> cache = CacheBuilder.newBuilder().maximumSize(2000).build();
  private final Set<Long> noPicture = Sets.newHashSet();

  EcPictures() {
  }

  void setBackground(final Multimap<Long, ItemPicture> articleWidgets) {
    final Set<Long> articles = Sets.newHashSet();

    for (Long article : articleWidgets.keySet()) {
      if (noPicture.contains(article)) {
        continue;
      }

      String picture = cache.getIfPresent(article);
      if (picture == null) {
        articles.add(article);
      } else {
        setBackgroundImage(articleWidgets.get(article), picture);
      }
    }

    if (!articles.isEmpty()) {
      ParameterList params = EcKeeper.createArgs(EcConstants.SVC_GET_PICTURES);
      params.addDataItem(EcConstants.COL_TCD_ARTICLE, DataUtils.buildIdList(articles));

      BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          EcKeeper.dispatchMessages(response);

          if (hasPictures(response)) {
            String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());
            if (arr != null) {
              for (int i = 0; i < arr.length - 1; i += 2) {
                Long article = BeeUtils.toLongOrNull(arr[i]);
                String picture = arr[i + 1];

                if (article != null && isPicture(picture)) {
                  if (articleWidgets.containsKey(article)) {
                    setBackgroundImage(articleWidgets.get(article), picture);
                  }

                  cache.put(article, picture);
                  articles.remove(article);
                }
              }

              logger.debug("picture cache", cache.size());
            }
          }

          if (!articles.isEmpty() && !response.hasErrors()) {
            noPicture.addAll(articles);
            logger.debug("no picture", articles, noPicture.size());
          }
        }
      });
    }
  }
}
