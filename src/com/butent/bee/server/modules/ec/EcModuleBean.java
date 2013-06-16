package com.butent.bee.server.modules.ec;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.ec.EcItemList;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class EcModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(EcModuleBean.class);

  @EJB
  UserServiceBean usr;
  
  @Override
  public Collection<String> dependsOn() {
    return Lists.newArrayList(CommonsConstants.COMMONS_MODULE);
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    return null;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(EC_METHOD);

    if (BeeUtils.same(svc, SVC_FEATURED_AND_NOVELTY)) {
      response = getFeaturedAndNoveltyItems();

    } else if (BeeUtils.same(svc, SVC_GLOBAL_SEARCH)) {
      response = doGlobalSearch(reqInfo);

    } else if (BeeUtils.same(svc, SVC_SEARCH_BY_ITEM_CODE)) {
      response = searchByItemCode(reqInfo);

    } else if (BeeUtils.same(svc, SVC_SEARCH_BY_OE_NUMBER)) {
      response = searchByOeNumber(reqInfo);
      
    } else {
      String msg = BeeUtils.joinWords("e-commerce service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    return null;
  }

  @Override
  public String getName() {
    return EC_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  @Override
  public void init() {
  }

  private ResponseObject doGlobalSearch(RequestInfo reqInfo) {
    String query = reqInfo.getParameter(VAR_QUERY);
    if (BeeUtils.isEmpty(query)) {
      return ResponseObject.parameterNotFound(SVC_GLOBAL_SEARCH, VAR_QUERY);
    }
    
    int count = BeeUtils.randomInt(0, Math.max((10 - query.length()) * 3, 2));
    if (count > 0) {
      return ResponseObject.response(generateItems(count));
    } else {
      return ResponseObject.warning(usr.getLocalizableMesssages().ecSearchDidNotMatch(query)); 
    }
  }

  private EcItemList generateItems(int count) {
    SimpleRowSet rowSet = new SimpleRowSet(new String[] {"id"});

    for (int i = 0; i < count; i++) {
      rowSet.addRow(new String[] {Integer.toString(i)});
    }

    return new EcItemList(rowSet);
  }

  private ResponseObject getFeaturedAndNoveltyItems() {
    return ResponseObject.response(generateItems(BeeUtils.randomInt(1, 30)));
  }
  
  private ResponseObject searchByItemCode(RequestInfo reqInfo) {
    return doGlobalSearch(reqInfo);
  }

  private ResponseObject searchByOeNumber(RequestInfo reqInfo) {
    return doGlobalSearch(reqInfo);
  }
}
