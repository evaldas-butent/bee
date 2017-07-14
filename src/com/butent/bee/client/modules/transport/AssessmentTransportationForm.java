package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AssessmentTransportationForm extends PrintFormInterceptor {

  @Override
  public FormInterceptor getInstance() {
    return new AssessmentTransportationForm();
  }

  @Override
  protected void getReportData(Consumer<BeeRowSet[]> dataConsumer) {
    Queries.getRowSet(VIEW_TRIP_CARGO, null, Filter.equals(COL_TRIP, getActiveRowId()),
        result -> {
          if (!DataUtils.isEmpty(result)) {
            Queries.getRowSet(TBL_ASSESSMENT_FORWARDERS,
                Arrays.asList(COL_CARGO, ALS_CARGO_DESCRIPTION, COL_NOTES),
                Filter.and(Filter.any(COL_CARGO, result.getDistinctLongs(
                    result.getColumnIndex(COL_CARGO))), Filter.equals(COL_FORWARDER,
                    result.getLong(0, COL_FORWARDER))),
                frdRowSet -> {
                  Map<Long, Pair<String, String>> frdMap = new HashMap<>();

                  if (!DataUtils.isEmpty(frdRowSet)) {
                    for (BeeRow row : frdRowSet) {
                      frdMap.put(row.getLong(0), Pair.of(row.getString(1), row.getString(2)));
                    }
                  }

                  TransportUtils.getCargoPlaces(Filter.any(COL_CARGO_TRIP, result.getRowIds()),
                      (loading, unloading) -> {

                        int cargoIdx = result.getColumnIndex(COL_CARGO);

                        for (BeeRowSet places : new BeeRowSet[] {loading, unloading}) {
                          for (BeeRow cargoRow : result) {
                            BeeRowSet current = TransportUtils.copyCargoPlaces(places,
                                DataUtils.filterRows(places, COL_CARGO_TRIP, cargoRow.getId()));

                            cargoRow.setProperty(places.getViewName(), current.serialize());

                            Long cargo = cargoRow.getLong(cargoIdx);

                            if (frdMap.containsKey(cargo)) {
                              cargoRow.setProperty(ALS_CARGO_DESCRIPTION, frdMap.get(cargo).getA());
                              cargoRow.setProperty(COL_NOTES, frdMap.get(cargo).getB());
                            }
                          }
                        }
                        dataConsumer.accept(new BeeRowSet[] {result});
                      });
                });
          }
        });
  }

  @Override
  protected void getReportParameters(Consumer<Map<String, String>> parametersConsumer) {
    Map<String, Long> companies = new HashMap<>();
    companies.put(COL_CUSTOMER, BeeKeeper.getUser().getCompany());
    companies.put(COL_FORWARDER, getLongValue(COL_FORWARDER));

    super.getReportParameters(defaultParameters ->
        ClassifierUtils.getCompaniesInfo(companies, companiesInfo ->
            Queries.getRowSet(VIEW_COMPANY_CONTACTS, Arrays.asList(COL_ADDRESS, COL_POST_INDEX,
            ALS_CITY_NAME, ALS_COUNTRY_NAME),
            Filter.equals(COL_COMPANY, getLongValue(COL_FORWARDER)),
            result -> {
              if (!DataUtils.isEmpty(result)) {
                defaultParameters.put(COL_FORWARDER + VIEW_COMPANY_CONTACTS, result.serialize());
              }

              defaultParameters.putAll(companiesInfo);
              parametersConsumer.accept(defaultParameters);
            })));
  }
}