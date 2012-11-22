package com.butent.bee.client.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.event.HandlesDeleteEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.view.ColumnNamesProvider;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.Map;

public class DataInfoProvider implements HandlesDeleteEvents, RowInsertEvent.Handler,
    DataInfo.Provider, ColumnNamesProvider {

  private static final BeeLogger logger = LogUtils.getLogger(DataInfoProvider.class);
  
  private final Map<String, DataInfo> views = Maps.newHashMap();

  public DataInfoProvider() {
    super();
  }

  public ImmutableList<String> getColumnNames(String viewName) {
    DataInfo dataInfo = getDataInfo(viewName, true);
    return (dataInfo == null) ? null : ImmutableList.copyOf(dataInfo.getColumnNames(false));
  }

  public DataInfo getDataInfo(String viewName, boolean warn) {
    DataInfo dataInfo = views.get(BeeUtils.normalize(viewName));
    if (dataInfo == null && warn) {
      logger.severe("view", viewName, "data info not found");
    }
    return dataInfo;
  }
  
  public Collection<DataInfo> getViews() {
    return views.values();
  }

  public void load(final Callback<Integer> callback) {
    BeeKeeper.getRpc().makeGetRequest(Service.GET_DATA_INFO, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);
        String[] info = Codec.beeDeserializeCollection((String) response.getResponse());

        views.clear();
        for (String s : info) {
          DataInfo dataInfo = DataInfo.restore(s);
          if (dataInfo != null) {
            views.put(BeeUtils.normalize(dataInfo.getViewName()), dataInfo);
          }
        }
        logger.info("data info provider loaded", views.size(), "items");
        
        if (callback != null) {
          callback.onSuccess(views.size());
        }
      }
    });
  }

  public void onMultiDelete(MultiDeleteEvent event) {
    DataInfo dataInfo = getDataInfo(event.getViewName(), false);
    if (dataInfo != null) {
      dataInfo.setRowCount(dataInfo.getRowCount() - event.getRows().size());
    }
  }

  public void onRowDelete(RowDeleteEvent event) {
    DataInfo dataInfo = getDataInfo(event.getViewName(), false);
    if (dataInfo != null) {
      dataInfo.setRowCount(dataInfo.getRowCount() - 1);
    }
  }

  public void onRowInsert(RowInsertEvent event) {
    DataInfo dataInfo = getDataInfo(event.getViewName(), false);
    if (dataInfo != null) {
      dataInfo.setRowCount(dataInfo.getRowCount() + 1);
    }
  }
}
