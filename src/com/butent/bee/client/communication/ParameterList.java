package com.butent.bee.client.communication;

import com.google.gwt.http.client.RequestBuilder;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeService;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ParameterList extends ArrayList<RpcParameter> implements
    Transformable {
  private boolean ready = false;
  private List<RpcParameter> dataItems, headerItems, queryItems;

  private String service;

  public ParameterList(String svc) {
    super();
    this.service = svc;

    addQueryItem(BeeService.RPC_VAR_SVC, svc);

    String dsn = BeeKeeper.getRpc().getDsn();
    if (!BeeUtils.isEmpty(dsn)) {
      addQueryItem(BeeService.RPC_VAR_DSN, dsn);
    }

    String opt = BeeKeeper.getRpc().getOptions();
    if (!BeeUtils.isEmpty(opt)) {
      addHeaderItem(BeeService.RPC_VAR_OPT, opt);
    }
  }

  public void addDataItem(Object value) {
    addItem(new RpcParameter(RpcParameter.SECTION.DATA, value));
  }

  public void addDataItem(String value) {
    addItem(new RpcParameter(RpcParameter.SECTION.DATA, value));
  }

  public void addDataItem(String name, Object value) {
    addItem(new RpcParameter(RpcParameter.SECTION.DATA, name, value));
  }

  public void addDataItem(String name, String value) {
    addItem(new RpcParameter(RpcParameter.SECTION.DATA, name, value));
  }

  public void addHeaderItem(Object value) {
    addItem(new RpcParameter(RpcParameter.SECTION.HEADER, value));
  }

  public void addHeaderItem(String value) {
    addItem(new RpcParameter(RpcParameter.SECTION.HEADER, value));
  }

  public void addHeaderItem(String name, Object value) {
    addItem(new RpcParameter(RpcParameter.SECTION.HEADER, name, value));
  }

  public void addHeaderItem(String name, String value) {
    addItem(new RpcParameter(RpcParameter.SECTION.HEADER, name, value));
  }

  public void addPositionalData(Object... values) {
    Assert.parameterCount(values.length, 1);
    for (Object v : values) {
      addDataItem(v);
    }
  }

  public void addPositionalData(String... values) {
    Assert.parameterCount(values.length, 1);
    for (Object v : values) {
      addDataItem(v);
    }
  }

  public void addPositionalHeader(Object... values) {
    Assert.parameterCount(values.length, 1);
    for (Object v : values) {
      addHeaderItem(v);
    }
  }

  public void addPositionalHeader(String... values) {
    Assert.parameterCount(values.length, 1);
    for (Object v : values) {
      addHeaderItem(v);
    }
  }

  public void addPositionalQuery(Object... values) {
    Assert.parameterCount(values.length, 1);
    for (Object v : values) {
      addQueryItem(v);
    }
  }

  public void addPositionalQuery(String... values) {
    Assert.parameterCount(values.length, 1);
    for (Object v : values) {
      addQueryItem(v);
    }
  }

  public void addQueryItem(Object value) {
    addItem(new RpcParameter(RpcParameter.SECTION.QUERY, value));
  }

  public void addQueryItem(String value) {
    addItem(new RpcParameter(RpcParameter.SECTION.QUERY, value));
  }

  public void addQueryItem(String name, Object value) {
    addItem(new RpcParameter(RpcParameter.SECTION.QUERY, name, value));
  }

  public void addQueryItem(String name, String value) {
    addItem(new RpcParameter(RpcParameter.SECTION.QUERY, name, value));
  }

  public ContentType getContentType() {
    ContentType ctp = CommUtils.getContentType(getParameter(BeeService.RPC_VAR_CTP));

    if (ctp == null) {
      prepare();
      ctp = BeeUtils.isEmpty(dataItems) ? null : ContentType.XML;
    }

    return ctp;
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

    return XmlUtils.createString(BeeService.XML_TAG_DATA, nodes);
  }

  public void getHeadersExcept(RequestBuilder bld, String... ignore) {
    Assert.notNull(bld);
    prepare();
    if (BeeUtils.isEmpty(headerItems)) {
      return;
    }

    int n = ignore.length;

    for (RpcParameter item : headerItems) {
      if (item.isReady()) {
        if (n > 0 && BeeUtils.inListSame(item.getName(), ignore)) {
          continue;
        }
        bld.setHeader(item.getName(), item.getValue());
      }
    }
  }

  public String getParameter(String name) {
    Assert.notEmpty(name);
    String value = null;

    for (RpcParameter item : this) {
      if (BeeUtils.same(item.getName(), name)) {
        value = item.getValue();
        break;
      }
    }

    return value;
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
          sb.append(CommUtils.QUERY_STRING_PAIR_SEPARATOR);
        }

        sb.append(item.getName().trim());
        sb.append(CommUtils.QUERY_STRING_VALUE_SEPARATOR);
        sb.append(item.getValue().trim());
      }
    }

    return sb.toString();
  }

  public String getService() {
    return service;
  }

  public boolean hasParameter(String name) {
    Assert.notEmpty(name);
    boolean ok = false;

    for (RpcParameter item : this) {
      if (BeeUtils.same(item.getName(), name)) {
        ok = true;
        break;
      }
    }

    return ok;
  }

  public void setService(String service) {
    this.service = service;
  }

  public String transform() {
    return BeeUtils.transformCollection(this, BeeConst.DEFAULT_LIST_SEPARATOR);
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
        item.setName(CommUtils.rpcParamName(n));
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
      queryItems.add(new RpcParameter(RpcParameter.SECTION.QUERY, BeeService.RPC_VAR_PRM_CNT, n));
    }
  }
}
