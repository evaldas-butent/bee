package com.butent.bee.egg.client.communication;

import com.google.gwt.http.client.RequestBuilder;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.ui.CompositeService;
import com.butent.bee.egg.client.utils.BeeXml;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ParameterList extends ArrayList<RpcParameter> {
  private boolean ready = false;
  private List<RpcParameter> dataItems, headerItems, queryItems;

  private String service;

  public ParameterList(String svc) {
    super();
    this.service = svc;

    addQueryItem(BeeService.RPC_FIELD_QNM, CompositeService.extractService(svc));
    
    String dsn = BeeKeeper.getRpc().getDsn();
    if (!BeeUtils.isEmpty(dsn)) {
      addQueryItem(BeeService.RPC_FIELD_DSN, dsn);
    }

    String opt = BeeKeeper.getRpc().getOptions();
    if (!BeeUtils.isEmpty(opt)) {
      addHeaderItem(BeeService.RPC_FIELD_OPT, opt);
    }
  }

  public void addDataItem(String value) {
    addItem(new RpcParameter(RpcParameter.SECTION.DATA, value));
  }

  public void addDataItem(Object value) {
    addItem(new RpcParameter(RpcParameter.SECTION.DATA, value));
  }

  public void addDataItem(String name, String value) {
    addItem(new RpcParameter(RpcParameter.SECTION.DATA, name, value));
  }

  public void addDataItem(String name, Object value) {
    addItem(new RpcParameter(RpcParameter.SECTION.DATA, name, value));
  }

  public void addHeaderItem(String value) {
    addItem(new RpcParameter(RpcParameter.SECTION.HEADER, value));
  }

  public void addHeaderItem(Object value) {
    addItem(new RpcParameter(RpcParameter.SECTION.HEADER, value));
  }

  public void addHeaderItem(String name, String value) {
    addItem(new RpcParameter(RpcParameter.SECTION.HEADER, name, value));
  }

  public void addHeaderItem(String name, Object value) {
    addItem(new RpcParameter(RpcParameter.SECTION.HEADER, name, value));
  }

  public void addQueryItem(String value) {
    addItem(new RpcParameter(RpcParameter.SECTION.QUERY, value));
  }

  public void addQueryItem(Object value) {
    addItem(new RpcParameter(RpcParameter.SECTION.QUERY, value));
  }

  public void addQueryItem(String name, String value) {
    addItem(new RpcParameter(RpcParameter.SECTION.QUERY, name, value));
  }

  public void addQueryItem(String name, Object value) {
    addItem(new RpcParameter(RpcParameter.SECTION.QUERY, name, value));
  }

  public String getData() {
    prepare();
    if (BeeUtils.isEmpty(dataItems)) {
      return null;
    }

    int n = dataItems.size();
    Object[] nodes = new Object[n * 2];
    RpcParameter item;

    for (int i = 0; i < n; i++) {
      item = dataItems.get(i);
      nodes[i * 2] = item.getName();
      nodes[i * 2 + 1] = item.getValue();
    }

    return BeeXml.createString(BeeService.XML_TAG_DATA, nodes);
  }

  public void getHeaders(RequestBuilder bld) {
    Assert.notNull(bld);
    prepare();
    if (BeeUtils.isEmpty(headerItems)) {
      return;
    }

    for (RpcParameter item : headerItems) {
      if (item.isReady()) {
        bld.setHeader(item.getName(), item.getValue());
      }
    }
  }

  public String getQuery() {
    prepare();
    if (BeeUtils.isEmpty(queryItems)) {
      return BeeConst.STRING_EMPTY;
    }

    StringBuilder sb = new StringBuilder();

    for (RpcParameter item : queryItems) {
      if (item.isReady()) {
        if (sb.length() > 0) {
          sb.append(BeeService.QUERY_STRING_PAIR_SEPARATOR);
        }

        sb.append(item.getName().trim());
        sb.append(BeeService.QUERY_STRING_VALUE_SEPARATOR);
        sb.append(item.getValue().trim());
      }
    }

    return sb.toString();
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  private void addItem(RpcParameter item) {
    Assert.state(item.isValid());
    add(item);
  }

  private void prepare() {
    if (ready) {
      return;
    }

    dataItems = new ArrayList<RpcParameter>();
    headerItems = new ArrayList<RpcParameter>();
    queryItems = new ArrayList<RpcParameter>();
    ready = true;

    if (isEmpty()) {
      return;
    }

    int n = 0;
    for (RpcParameter item : this) {
      if (!item.isValid()) {
        continue;
      }
      if (!item.isNamed()) {
        item.setName(BeeService.rpcParamName(n));
        n++;
      }

      switch (item.getSection()) {
        case DATA:
          dataItems.add(item);
          break;
        case HEADER:
          headerItems.add(item);
          break;
        case QUERY:
          queryItems.add(item);
          break;
      }
    }

    if (n > 0) {
      queryItems.add(new RpcParameter(RpcParameter.SECTION.QUERY,
          BeeService.RPC_FIELD_PAR_CNT, n));
    }
  }

}
