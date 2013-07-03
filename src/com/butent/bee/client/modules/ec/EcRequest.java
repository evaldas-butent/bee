package com.butent.bee.client.modules.ec;

import com.google.common.base.Objects;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class EcRequest {

  private final long startTime = System.currentTimeMillis();

  private final String service;
  private final String query;
  
  private String progressId = null;
  private int requestId = BeeConst.UNDEF;
  
  public EcRequest(String service, String query) {
    this.service = service;
    this.query = query;
  }

  public String elapsedMillis() {
    return TimeUtils.elapsedMillis(startTime);
  }
  
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof EcRequest) ? requestId == ((EcRequest) obj).requestId : false;
  }

  public String getProgressId() {
    return progressId;
  }

  public int getRequestId() {
    return requestId;
  }

  public long getStartTime() {
    return startTime;
  }

  @Override
  public int hashCode() {
    return requestId;
  }
  
  public boolean hasProgress() {
    return !BeeUtils.isEmpty(getProgressId());
  }
  
  public boolean isValid() {
    return !BeeConst.isUndef(getRequestId());
  }
  
  public boolean sameQuery(String qry) {
    return BeeUtils.equalsTrim(query, qry);
  }

  public boolean sameService(String svc) {
    return Objects.equal(service, svc);
  }

  String getQuery() {
    return query;
  }

  String getService() {
    return service;
  }

  void setProgressId(String progressId) {
    this.progressId = progressId;
  }

  void setRequestId(int requestId) {
    this.requestId = requestId;
  }
}
