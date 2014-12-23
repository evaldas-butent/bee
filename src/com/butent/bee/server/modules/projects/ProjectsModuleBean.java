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
import com.butent.bee.shared.data.view.Order;
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

    SimpleRowSet chartData = new SimpleRowSet(new String[] {ALS_VIEW_NAME, ALS_CHART_ID,
        ALS_CHART_CAPTION, ALS_CHART_START, ALS_CHART_END, ALS_CHART_FLOW_COLOR});

    BeeRowSet rs = qs.getViewData(VIEW_PROJECT_DATES, Filter.equals(COL_PROJECT, projectId),
        Order.ascending(COL_DATES_START_DATE));

    int idxColor = rs.getColumnIndex(COL_DATES_COLOR);
    int idxCaption = rs.getColumnIndex(COL_DATES_NOTE);
    int idxStartDate = rs.getColumnIndex(COL_DATES_START_DATE);

    for (IsRow rsRow : rs) {

      chartData.addRow(new String[] {VIEW_PROJECT_DATES,
          null,
          rsRow.getString(idxCaption),
          rsRow.getString(idxStartDate),
          null,
          rsRow.getString(idxColor)});
    }

    rs = qs.getViewData(VIEW_PROJECT_STAGES, Filter.equals(COL_PROJECT, projectId),
        Order.ascending(sys.getIdName(VIEW_PROJECT_STAGES), COL_STAGE_START_DATE));

    int idxStageName = rs.getColumnIndex(COL_STAGE_NAME);
    int idxStageStart = rs.getColumnIndex(COL_STAGE_START_DATE);
    int idxStageEnd = rs.getColumnIndex(COL_STAGE_END_DATE);

    for (IsRow rsRow : rs) {
      String stage = BeeUtils.toString(rsRow.getId());
      String stageName = BeeUtils.isNegative(idxStageName) ? stage : rsRow.getString(idxStageName);

      String stageStart = rsRow.getString(idxStageStart);
      String stageEnd = rsRow.getString(idxStageEnd);

      chartData.addRow(new String[] {
          VIEW_PROJECT_STAGES,
          stage, stageName, stageStart, stageEnd, null
      });
    }

    return ResponseObject.response(chartData);
  }
}
