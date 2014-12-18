package com.butent.bee.server.modules.projects;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.BeeUtils;

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

    if (BeeUtils.same(svc, SVC_GET_PROJECT_CHART_DATA)) {
      response = getProjectChartData(reqInfo);
    }
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

  }

  @SuppressWarnings({"unused", "static-method"})
  private ResponseObject getProjectChartData(RequestInfo req) {
    Long projectId = BeeUtils.toLongOrNull(req.getParameter(VAR_PROJECT));

    if (!DataUtils.isId(projectId)) {
      return ResponseObject.error(projectId);
    }

    SimpleRowSet chartData = new SimpleRowSet(new String[] {COL_PROJECT_STAGE, ALS_STAGE_NAME,
        ALS_PROJECT_START_DATE, ALS_PROJECT_END_DATE, ALS_STAGE_START, ALS_STAGE_END,
        TaskConstants.COL_TASK, TaskConstants.ALS_TASK_SUBJECT,
        TaskConstants.ALS_EXECUTOR_FIRST_NAME, TaskConstants.ALS_EXECUTOR_LAST_NAME});

    return ResponseObject.emptyResponse();
  }
}
