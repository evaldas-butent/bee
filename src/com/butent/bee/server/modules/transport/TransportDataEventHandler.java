package com.butent.bee.server.modules.transport;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.utils.BeeUtils;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class TransportDataEventHandler implements DataEventHandler {

  @EJB SystemBean sys;
  @EJB QueryServiceBean qs;

  @Subscribe
  @AllowConcurrentEvents
  public void fillAssessmentProperties(DataEvent.ViewQueryEvent event) {
    if (event.isAfter(TBL_ASSESSMENTS) && event.hasData()) {
      BeeRowSet rowSet = event.getRowset();

      SqlSelect query = new SqlSelect()
          .addFields(TBL_ASSESSMENT_FORWARDERS, COL_ASSESSMENT)
          .addFields(TBL_TRIPS, COL_FORWARDER_VEHICLE)
          .addFrom(TBL_ASSESSMENT_FORWARDERS)
          .addFromInner(TBL_TRIPS, sys.joinTables(TBL_TRIPS, TBL_ASSESSMENT_FORWARDERS, COL_TRIP))
          .setWhere(SqlUtils.and(SqlUtils.notNull(TBL_TRIPS, COL_FORWARDER_VEHICLE),
              SqlUtils.inList(TBL_ASSESSMENT_FORWARDERS, COL_ASSESSMENT, rowSet.getRowIds())));

      Multimap<Long, String> vehicles = HashMultimap.create();

      qs.getData(query).forEach(row -> vehicles.put(row.getLong(0), row.getValue(1)));

      for (BeeRow row : rowSet.getRows()) {
        if (vehicles.containsKey(row.getId())) {
          row.setProperty(COL_FORWARDER_VEHICLE, BeeUtils.joinItems(vehicles.get(row.getId())));
        }
      }
    }
  }

  public void initConditions() {
    BeeView.registerConditionProvider(TBL_ASSESSMENTS, (view, args) -> {
      String col = BeeUtils.getQuietly(args, 0);
      String val = BeeUtils.getQuietly(args, 1);

      if (BeeUtils.anyEmpty(col, val)) {
        return null;
      }
      IsCondition clause = null;

      switch (col) {
        case COL_FORWARDER_VEHICLE:
          clause = SqlUtils.in(TBL_ASSESSMENTS, sys.getIdName(TBL_ASSESSMENTS),
              new SqlSelect()
                  .addFields(TBL_ASSESSMENT_FORWARDERS, COL_ASSESSMENT)
                  .addFrom(TBL_ASSESSMENT_FORWARDERS)
                  .addFromInner(TBL_TRIPS,
                      sys.joinTables(TBL_TRIPS, TBL_ASSESSMENT_FORWARDERS, COL_TRIP))
                  .setWhere(SqlUtils.contains(TBL_TRIPS, COL_FORWARDER_VEHICLE, val)));
          break;
      }
      return clause;
    });
  }
}
