package com.butent.bee.server.modules.projects;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.BeeParameter;
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

  private ResponseObject getProjectChartData(RequestInfo req) {
    Long projectId = BeeUtils.toLongOrNull(req.getParameter(VAR_PROJECT));

    if (!DataUtils.isId(projectId)) {
      return ResponseObject.error(projectId);
    }

    SimpleRowSet chartData = new SimpleRowSet(new String[] {ALS_VIEW_NAME, COL_PROJECT_STAGE,
        ALS_STAGE_NAME, ALS_PROJECT_START_DATE, ALS_PROJECT_END_DATE, ALS_STAGE_START,
        ALS_STAGE_END, ALS_CHART_FLOW_COLOR});

    BeeRowSet rs = qs.getViewData(VIEW_PROJECT_STAGES, Filter.equals(COL_PROJECT, projectId));

    int idxStageName = rs.getColumnIndex(COL_STAGE_NAME);
    int idxProjectStart = rs.getColumnIndex(ALS_PROJECT_START_DATE);
    int idxProjectEnd = rs.getColumnIndex(ALS_PROJECT_END_DATE);
    int idxStageStart = rs.getColumnIndex(COL_STAGE_START_DATE);
    int idxStageEnd = rs.getColumnIndex(COL_STAGE_END_DATE);

    for (IsRow rsRow : rs) {
      String stage = BeeUtils.toString(rsRow.getId());
      String stageName = BeeUtils.isNegative(idxStageName) ? stage : rsRow.getString(idxStageName);
      String projectStart =
          BeeUtils.isNegative(idxProjectStart) ? null : rsRow.getString(idxProjectStart);
      String projectEnd =
          BeeUtils.isNegative(idxProjectEnd) ? null : rsRow.getString(idxProjectEnd);

      String stageStart =
          BeeUtils.isNegative(idxStageStart) ? projectStart : rsRow.getString(idxStageStart);
      String stageEnd =
          BeeUtils.isNegative(idxStageEnd) ? projectEnd : rsRow.getString(idxStageEnd);

      chartData.addRow(new String[] {
          VIEW_PROJECT_STAGES,
          stage, stageName, projectStart, projectEnd, stageStart, stageEnd, "#eee"
      });
    }

    return ResponseObject.response(chartData);
  }
}
