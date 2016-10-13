package com.butent.bee.client.modules.orders.ec;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyCodes;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.cell.AbstractCell;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.orders.OrdersConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class OrdEcPictures {

  public Cache<Long, ImmutableList<String>> getPictures() {
    return cache;
  }

  private static boolean hasPictures(ResponseObject response) {
    return response != null && !response.hasErrors() && response.hasResponse()
        && !response.hasResponse(String.class);
  }

  private static boolean isPicture(String picture) {
    return picture != null;
  }

  private static void setPictures(Collection<OrdEcItemPicture> widgets, List<String> pictures) {
    if (widgets != null && !BeeUtils.isEmpty(pictures)) {
      for (OrdEcItemPicture widget : widgets) {
        if (widget != null) {
          widget.setPictures(ImmutableList.copyOf(pictures));
        }
      }
    }
  }

  private final Cache<Long, ImmutableList<String>> cache =
      CacheBuilder.newBuilder().maximumSize(2000).build();

  private final Set<Long> noPicture = new HashSet<>();

  private BeeRowSet banners;

  OrdEcPictures() {
  }

  static void addCellHandlers(AbstractCell<?> cell, final String primaryStyle) {
    cell.addClickHandler(event -> onCellEvent(event, primaryStyle));

    cell.addKeyDownHandler(event -> {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        onCellEvent(event, primaryStyle);
      }
    });
  }

  BeeRowSet getBanners() {
    return banners;
  }

  List<RowInfo> getCachedBannerInfo() {
    List<RowInfo> result = new ArrayList<>();

    if (!DataUtils.isEmpty(getBanners())) {
      for (BeeRow row : getBanners().getRows()) {
        result.add(new RowInfo(row, false));
      }
    }

    return result;
  }

  static void onCellEvent(DomEvent<?> event, String primaryStyle) {
    Element cellElement = event.getRelativeElement();
    if (cellElement == null) {
      return;
    }

    ImageElement imageElement = DomUtils.getImageElement(cellElement);
    if (imageElement == null) {
      return;
    }

    if (event.getSource() instanceof AbstractCell) {
      ((AbstractCell<?>) event.getSource()).setEventCanceled(true);
    }

    Image image = new Image(imageElement.getSrc());
    if (!BeeUtils.isEmpty(primaryStyle)) {
      EcStyles.add(image, primaryStyle, "picture");
    }

    Global.showModalWidget(image, cellElement);
  }

  void setBackground(final Multimap<Long, OrdEcItemPicture> itemWidgets) {
    final Set<Long> items = new HashSet<>();

    for (Long item : itemWidgets.keySet()) {
      if (noPicture.contains(item)) {
        continue;
      }

      List<String> pictures = cache.getIfPresent(item);
      if (BeeUtils.isEmpty(pictures)) {
        items.add(item);
      } else {
        setPictures(itemWidgets.get(item), pictures);
      }
    }

    if (!items.isEmpty()) {
      ParameterList params = OrdEcKeeper.createArgs(OrdersConstants.SVC_GET_PICTURES);
      params.addDataItem(ClassifierConstants.COL_ITEM, DataUtils.buildIdList(items));

      BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          OrdEcKeeper.dispatchMessages(response);

          if (hasPictures(response)) {
            String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());

            if (arr != null) {
              Long lastItem = null;
              List<String> pictures = new ArrayList<>();

              for (int i = 0; i < arr.length - 1; i += 2) {
                Long item = BeeUtils.toLongOrNull(arr[i]);
                String picture = arr[i + 1];

                if (item != null && isPicture(picture)) {
                  if (lastItem == null) {
                    lastItem = item;
                    pictures.add(picture);

                  } else if (item.equals(lastItem)) {
                    pictures.add(picture);

                  } else {
                    if (itemWidgets.containsKey(lastItem)) {
                      setPictures(itemWidgets.get(lastItem), pictures);
                    }

                    cache.put(lastItem, ImmutableList.copyOf(pictures));
                    items.remove(lastItem);

                    lastItem = item;
                    pictures.clear();
                    pictures.add(picture);
                  }
                }
              }

              if (lastItem != null && !pictures.isEmpty()) {
                if (itemWidgets.containsKey(lastItem)) {
                  setPictures(itemWidgets.get(lastItem), pictures);
                }

                cache.put(lastItem, ImmutableList.copyOf(pictures));
                items.remove(lastItem);
              }
            }
          }

          if (!items.isEmpty() && !response.hasErrors()) {
            noPicture.addAll(items);
          }
        }
      });
    }
  }

  void setBanners(BeeRowSet banners) {
    this.banners = banners;
  }
}