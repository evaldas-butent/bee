package com.butent.bee.server.modules.projects;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.rights.Module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Server-side Projects module bean.
 */
@Stateless
@LocalBean
public class ProjectsModuleBean implements BeeModule {

  @EJB
  SystemBean sys;

  @EJB
  QueryServiceBean qs;

  @Override
  public List<SearchResult> doSearch(String query) {
    List<SearchResult> result = new ArrayList<>();
    // TODO: implement global search in this module
    return result;
  }

  @Override
  public ResponseObject doService(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    return null;
  }

  @Override
  public Module getModule() {
    return Module.PROJECTS;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  @Override
  public void init() {

    // sys.registerDataEventHandler(new DataEventHandler() {
    //
    // @Subscribe
    // public void fillProjectsViewProperties(ViewQueryEvent event) {
    // if (event.isBefore()) {
    // return;
    // }
    //
    // if (!BeeUtils.same(event.getTargetName(), VIEW_PROJECTS)) {
    // return;
    // }
    //
    // BeeRowSet rowSet = event.getRowset();
    //
    // if (rowSet.isEmpty()) {
    // return;
    // }
    //
    // BeeRowSet users = getProjectUsers(rowSet.getRowIds());
    // int idxProject = users.getColumnIndex(COL_PROJECT);
    // int idxUser = users.getColumnIndex(AdministrationConstants.COL_USER);
    //
    // for (IsRow user : users) {
    // IsRow row = rowSet.getRowById(BeeUtils.unbox(user.getLong(idxProject)));
    //
    // if (row == null) {
    // continue;
    // }
    //
    // if (!DataUtils.isId(user.getLong(idxUser))) {
    // continue;
    // }
    //
    // row.setProperty(PROP_USERS,
    // BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR, row.getProperty(PROP_USERS),
    // BeeUtils.toString(user.getLong(idxUser))));
    // }
    //
    // }
    // });

  }

  // private BeeRowSet getProjectUsers(List<Long> projectIds) {
  // Filter filter = Filter.any(COL_PROJECT, projectIds);
  //
  // BeeRowSet users = qs.getViewData(VIEW_PROJECT_USERS, filter);
  //
  // return users;
  // }
}
