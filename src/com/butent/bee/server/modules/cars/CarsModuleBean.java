package com.butent.bee.server.modules.cars;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import static com.butent.bee.shared.modules.cars.CarsConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsQuery;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.cars.Bundle;
import com.butent.bee.shared.modules.cars.Configuration;
import com.butent.bee.shared.modules.cars.Dimension;
import com.butent.bee.shared.modules.cars.Option;
import com.butent.bee.shared.modules.cars.Specification;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class CarsModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(CarsModuleBean.class);

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  @Override
  public ResponseObject doService(String service, RequestInfo reqInfo) {
    ResponseObject response;

    switch (service) {
      case SVC_GET_CONFIGURATION:
        response = getConfiguration(reqInfo.getParameterLong(COL_BRANCH));
        break;

      case SVC_SAVE_DIMENSIONS:
        Pair<String, String> pair = Pair.restore(reqInfo.getParameter(TBL_CONF_DIMENSIONS));

        response = saveDimensions(reqInfo.getParameterLong(COL_BRANCH),
            Codec.deserializeIdList(pair.getA()), Codec.deserializeIdList(pair.getB()));
        break;

      case SVC_SET_BUNDLE:
        response = setBundle(reqInfo.getParameterLong(COL_BRANCH),
            Bundle.restore(reqInfo.getParameter(COL_BUNDLE)),
            Configuration.DataInfo.restore(reqInfo.getParameter(Service.VAR_DATA)),
            Codec.unpack(reqInfo.getParameter(COL_BLOCKED)));
        break;

      case SVC_DELETE_BUNDLES:
        qs.updateData(new SqlDelete(TBL_CONF_BRANCH_BUNDLES)
            .setWhere(SqlUtils.and(SqlUtils.equals(TBL_CONF_BRANCH_BUNDLES, COL_BRANCH,
                reqInfo.getParameterLong(COL_BRANCH)),
                SqlUtils.in(TBL_CONF_BRANCH_BUNDLES, COL_BUNDLE,
                    new SqlSelect()
                        .addFields(TBL_CONF_BUNDLES, sys.getIdName(TBL_CONF_BUNDLES))
                        .addFrom(TBL_CONF_BUNDLES)
                        .setWhere(SqlUtils.inList(TBL_CONF_BUNDLES, COL_KEY, (Object[])
                            Codec.beeDeserializeCollection(reqInfo.getParameter(COL_KEY))))))));

        response = ResponseObject.emptyResponse();
        break;

      case SVC_DELETE_OPTION:
        qs.updateData(new SqlDelete(TBL_CONF_BRANCH_OPTIONS)
            .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS,
                COL_BRANCH, reqInfo.getParameterLong(COL_BRANCH),
                COL_OPTION, reqInfo.getParameterLong(COL_OPTION))));

        response = ResponseObject.emptyResponse();
        break;

      case SVC_SET_OPTION:
        response = setOption(reqInfo.getParameterLong(COL_BRANCH),
            reqInfo.getParameterLong(COL_OPTION),
            Configuration.DataInfo.restore(reqInfo.getParameter(Service.VAR_DATA)));
        break;

      case SVC_SET_RELATION:
        response = setRelation(reqInfo.getParameterLong(COL_BRANCH), reqInfo.getParameter(COL_KEY),
            reqInfo.getParameterLong(COL_OPTION),
            Configuration.DataInfo.restore(reqInfo.getParameter(Service.VAR_DATA)));
        break;

      case SVC_DELETE_RELATION:
        qs.updateData(new SqlDelete(TBL_CONF_RELATIONS)
            .setWhere(sys.idEquals(TBL_CONF_RELATIONS, qs.getLong(new SqlSelect()
                .addFields(TBL_CONF_RELATIONS, sys.getIdName(TBL_CONF_RELATIONS))
                .addFrom(TBL_CONF_BRANCH_BUNDLES)
                .addFromInner(TBL_CONF_BUNDLES, SqlUtils.and(sys.joinTables(TBL_CONF_BUNDLES,
                    TBL_CONF_BRANCH_BUNDLES, COL_BUNDLE),
                    SqlUtils.equals(TBL_CONF_BUNDLES, COL_KEY, reqInfo.getParameter(COL_KEY))))
                .addFromInner(TBL_CONF_BRANCH_OPTIONS,
                    SqlUtils.and(SqlUtils.joinUsing(TBL_CONF_BRANCH_BUNDLES,
                        TBL_CONF_BRANCH_OPTIONS, COL_BRANCH),
                        SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS, COL_OPTION,
                            reqInfo.getParameterLong(COL_OPTION))))
                .addFromInner(TBL_CONF_RELATIONS,
                    SqlUtils.and(sys.joinTables(TBL_CONF_BRANCH_BUNDLES, TBL_CONF_RELATIONS,
                        COL_BRANCH_BUNDLE), sys.joinTables(TBL_CONF_BRANCH_OPTIONS,
                        TBL_CONF_RELATIONS, COL_BRANCH_OPTION)))
                .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_BUNDLES, COL_BRANCH,
                    reqInfo.getParameterLong(COL_BRANCH)))))));

        response = ResponseObject.emptyResponse();
        break;

      case SVC_SET_RESTRICTIONS:
        Map<Long, Map<Long, Boolean>> data = new HashMap<>();

        Codec.deserializeHashMap(reqInfo.getParameter(TBL_CONF_RESTRICTIONS)).forEach((k, v) -> {
          Map<Long, Boolean> map = new HashMap<>();

          Codec.deserializeHashMap(v).forEach((k2, v2) ->
              map.put(BeeUtils.toLong(k2), BeeUtils.toBoolean(v2)));

          data.put(BeeUtils.toLong(k), map);
        });
        response = setRestrictions(reqInfo.getParameterLong(COL_BRANCH), data);
        break;

      case SVC_SAVE_OBJECT:
        response = saveObject(Specification.restore(reqInfo.getParameter(COL_OBJECT)));
        break;

      case SVC_GET_OBJECT:
        response = getObject(reqInfo.getParameterLong(COL_OBJECT));
        break;

      default:
        String msg = BeeUtils.joinWords("Cars service not recognized:", service);
        logger.warning(msg);
        response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public Module getModule() {
    return Module.CARS;
  }

  private ResponseObject getConfiguration(Long branchId) {
    Configuration configuration = new Configuration();

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_CONF_DIMENSIONS, COL_GROUP, COL_ORDINAL)
        .addFields(TBL_CONF_GROUPS, COL_GROUP_NAME, COL_REQUIRED)
        .addFrom(TBL_CONF_DIMENSIONS)
        .addFromInner(TBL_CONF_GROUPS,
            sys.joinTables(TBL_CONF_GROUPS, TBL_CONF_DIMENSIONS, COL_GROUP))
        .setWhere(SqlUtils.equals(TBL_CONF_DIMENSIONS, COL_BRANCH, branchId)));

    for (SimpleRowSet.SimpleRow row : data) {
      configuration.addDimension(new Dimension(row.getLong(COL_GROUP),
              row.getValue(COL_GROUP_NAME)).setRequired(row.getBoolean(COL_REQUIRED)),
          row.getInt(COL_ORDINAL));
    }
    data = qs.getData(new SqlSelect()
        .addFields(TBL_CONF_BRANCH_BUNDLES, COL_PRICE, COL_BLOCKED)
        .addField(TBL_CONF_BRANCH_BUNDLES, COL_DESCRIPTION, COL_BUNDLE + COL_DESCRIPTION)
        .addFields(TBL_CONF_BUNDLE_OPTIONS, COL_BUNDLE, COL_OPTION)
        .addFields(TBL_CONF_OPTIONS, COL_GROUP, COL_OPTION_NAME, COL_CODE, COL_DESCRIPTION,
            COL_PHOTO)
        .addFields(TBL_CONF_GROUPS, COL_GROUP_NAME, COL_REQUIRED)
        .addFrom(TBL_CONF_BRANCH_BUNDLES)
        .addFromInner(TBL_CONF_BUNDLE_OPTIONS,
            SqlUtils.joinUsing(TBL_CONF_BRANCH_BUNDLES, TBL_CONF_BUNDLE_OPTIONS, COL_BUNDLE))
        .addFromInner(TBL_CONF_OPTIONS,
            sys.joinTables(TBL_CONF_OPTIONS, TBL_CONF_BUNDLE_OPTIONS, COL_OPTION))
        .addFromInner(TBL_CONF_GROUPS, sys.joinTables(TBL_CONF_GROUPS, TBL_CONF_OPTIONS, COL_GROUP))
        .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_BUNDLES, COL_BRANCH, branchId)));

    Multimap<Long, Option> bundleOptions = HashMultimap.create();
    Map<Long, Pair<Bundle, Pair<Configuration.DataInfo, Boolean>>> bundles = new HashMap<>();

    for (SimpleRowSet.SimpleRow row : data) {
      Long id = row.getLong(COL_BUNDLE);

      bundleOptions.put(id, new Option(row.getLong(COL_OPTION),
          row.getValue(COL_OPTION_NAME), new Dimension(row.getLong(COL_GROUP),
          row.getValue(COL_GROUP_NAME)).setRequired(row.getBoolean(COL_REQUIRED)))
          .setCode(row.getValue(COL_CODE))
          .setDescription(row.getValue(COL_DESCRIPTION))
          .setPhoto(row.getLong(COL_PHOTO)));

      if (!bundles.containsKey(id)) {
        bundles.put(id, Pair.of(null, Pair.of(Configuration.DataInfo.of(row.getValue(COL_PRICE),
            row.getValue(COL_BUNDLE + COL_DESCRIPTION)), row.getBoolean(COL_BLOCKED))));
      }
    }
    for (Long bundleId : bundles.keySet()) {
      Bundle bundle = new Bundle(bundleOptions.get(bundleId));
      Pair<Bundle, Pair<Configuration.DataInfo, Boolean>> pair = bundles.get(bundleId);
      pair.setA(bundle);
      configuration.setBundleInfo(bundle, pair.getB().getA(), pair.getB().getB());
    }
    data = qs.getData(new SqlSelect()
        .addField(TBL_CONF_BRANCH_OPTIONS, sys.getIdName(TBL_CONF_BRANCH_OPTIONS),
            COL_BRANCH_OPTION)
        .addFields(TBL_CONF_BRANCH_OPTIONS, COL_OPTION)
        .addField(TBL_CONF_BRANCH_OPTIONS, COL_PRICE, COL_OPTION + COL_PRICE)
        .addField(TBL_CONF_BRANCH_OPTIONS, COL_DESCRIPTION, COL_OPTION + COL_DESCRIPTION)
        .addFields(TBL_CONF_OPTIONS, COL_GROUP, COL_OPTION_NAME, COL_CODE, COL_DESCRIPTION,
            COL_PHOTO)
        .addFields(TBL_CONF_GROUPS, COL_GROUP_NAME, COL_REQUIRED)
        .addFields(TBL_CONF_RELATIONS, COL_PRICE)
        .addField(TBL_CONF_RELATIONS, COL_DESCRIPTION, TBL_CONF_RELATIONS + COL_DESCRIPTION)
        .addFields(TBL_CONF_BRANCH_BUNDLES, COL_BUNDLE)
        .addFrom(TBL_CONF_BRANCH_OPTIONS)
        .addFromInner(TBL_CONF_OPTIONS,
            sys.joinTables(TBL_CONF_OPTIONS, TBL_CONF_BRANCH_OPTIONS, COL_OPTION))
        .addFromInner(TBL_CONF_GROUPS, sys.joinTables(TBL_CONF_GROUPS, TBL_CONF_OPTIONS, COL_GROUP))
        .addFromLeft(TBL_CONF_RELATIONS,
            sys.joinTables(TBL_CONF_BRANCH_OPTIONS, TBL_CONF_RELATIONS, COL_BRANCH_OPTION))
        .addFromLeft(TBL_CONF_BRANCH_BUNDLES,
            sys.joinTables(TBL_CONF_BRANCH_BUNDLES, TBL_CONF_RELATIONS, COL_BRANCH_BUNDLE))
        .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS, COL_BRANCH, branchId)));

    Map<Long, Option> branchOptions = new HashMap<>();

    for (SimpleRowSet.SimpleRow row : data) {
      Long branchOption = row.getLong(COL_BRANCH_OPTION);

      if (!branchOptions.containsKey(branchOption)) {
        Option option = new Option(row.getLong(COL_OPTION),
            row.getValue(COL_OPTION_NAME), new Dimension(row.getLong(COL_GROUP),
            row.getValue(COL_GROUP_NAME)).setRequired(row.getBoolean(COL_REQUIRED)))
            .setCode(row.getValue(COL_CODE))
            .setDescription(row.getValue(COL_DESCRIPTION))
            .setPhoto(row.getLong(COL_PHOTO));

        branchOptions.put(branchOption, option);
        configuration.setOptionInfo(option,
            Configuration.DataInfo.of(row.getValue(COL_OPTION + COL_PRICE),
                row.getValue(COL_OPTION + COL_DESCRIPTION)));
      }
      if (DataUtils.isId(row.getLong(COL_BUNDLE))) {
        configuration.setRelationInfo(branchOptions.get(branchOption),
            bundles.get(row.getLong(COL_BUNDLE)).getA(),
            Configuration.DataInfo.of(row.getValue(COL_PRICE),
                row.getValue(TBL_CONF_RELATIONS + COL_DESCRIPTION)));
      }
    }
    data = qs.getData(new SqlSelect()
        .addFields(TBL_CONF_RESTRICTIONS, COL_BRANCH_OPTION, COL_OPTION, COL_DENIED)
        .addFields(TBL_CONF_OPTIONS, COL_GROUP, COL_OPTION_NAME, COL_CODE, COL_DESCRIPTION,
            COL_PHOTO)
        .addFields(TBL_CONF_GROUPS, COL_GROUP_NAME, COL_REQUIRED)
        .addFrom(TBL_CONF_RESTRICTIONS)
        .addFromInner(TBL_CONF_OPTIONS,
            sys.joinTables(TBL_CONF_OPTIONS, TBL_CONF_RESTRICTIONS, COL_OPTION))
        .addFromInner(TBL_CONF_GROUPS, sys.joinTables(TBL_CONF_GROUPS, TBL_CONF_OPTIONS, COL_GROUP))
        .addFromInner(TBL_CONF_BRANCH_OPTIONS,
            sys.joinTables(TBL_CONF_BRANCH_OPTIONS, TBL_CONF_RESTRICTIONS, COL_BRANCH_OPTION))
        .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS, COL_BRANCH, branchId)));

    for (SimpleRowSet.SimpleRow row : data) {
      Option option = new Option(row.getLong(COL_OPTION),
          row.getValue(COL_OPTION_NAME), new Dimension(row.getLong(COL_GROUP),
          row.getValue(COL_GROUP_NAME)).setRequired(row.getBoolean(COL_REQUIRED)))
          .setCode(row.getValue(COL_CODE))
          .setDescription(row.getValue(COL_DESCRIPTION))
          .setPhoto(row.getLong(COL_PHOTO));

      configuration.setRestriction(branchOptions.get(row.getLong(COL_BRANCH_OPTION)), option,
          BeeUtils.unbox(row.getBoolean(COL_DENIED)));
    }
    return ResponseObject.response(configuration);
  }

  private ResponseObject getObject(Long objectId) {
    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(TBL_CONF_OBJECTS, COL_BRANCH, COL_BRANCH_NAME)
        .addField(TBL_CONF_OBJECTS, COL_DESCRIPTION, COL_BUNDLE + COL_DESCRIPTION)
        .addField(TBL_CONF_OBJECTS, COL_PRICE, COL_BUNDLE + COL_PRICE)
        .addFields(TBL_CONF_OBJECT_OPTIONS, COL_OPTION, COL_PRICE)
        .addFields(TBL_CONF_OPTIONS, COL_GROUP, COL_OPTION_NAME, COL_CODE, COL_DESCRIPTION,
            COL_PHOTO)
        .addFields(TBL_CONF_GROUPS, COL_GROUP_NAME, COL_REQUIRED)
        .addFrom(TBL_CONF_OBJECTS)
        .addFromInner(TBL_CONF_OBJECT_OPTIONS,
            sys.joinTables(TBL_CONF_OBJECTS, TBL_CONF_OBJECT_OPTIONS, COL_OBJECT))
        .addFromInner(TBL_CONF_OPTIONS,
            sys.joinTables(TBL_CONF_OPTIONS, TBL_CONF_OBJECT_OPTIONS, COL_OPTION))
        .addFromInner(TBL_CONF_GROUPS, sys.joinTables(TBL_CONF_GROUPS, TBL_CONF_OPTIONS, COL_GROUP))
        .setWhere(sys.idEquals(TBL_CONF_OBJECTS, objectId))
        .addOrder(TBL_CONF_OBJECT_OPTIONS, sys.getIdName(TBL_CONF_OBJECT_OPTIONS)));

    Specification specification = null;
    Integer bundlePrice = null;
    List<Option> bundleOptions = new ArrayList<>();

    for (SimpleRowSet.SimpleRow row : rs) {
      if (Objects.isNull(specification)) {
        specification = new Specification();
        specification.setId(objectId);
        specification.setBranch(row.getLong(COL_BRANCH), row.getValue(COL_BRANCH_NAME));
        specification.setDescription(row.getValue(COL_BUNDLE + COL_DESCRIPTION));
        bundlePrice = row.getInt(COL_BUNDLE + COL_PRICE);
      }
      Integer price = row.getInt(COL_PRICE);
      Option option = new Option(row.getLong(COL_OPTION),
          row.getValue(COL_OPTION_NAME), new Dimension(row.getLong(COL_GROUP),
          row.getValue(COL_GROUP_NAME)).setRequired(row.getBoolean(COL_REQUIRED)))
          .setCode(row.getValue(COL_CODE))
          .setDescription(row.getValue(COL_DESCRIPTION))
          .setPhoto(row.getLong(COL_PHOTO));

      if (Objects.isNull(price)) {
        bundleOptions.add(option);
      } else {
        specification.addOption(option, price);
      }
    }
    if (Objects.nonNull(specification)) {
      if (!BeeUtils.isEmpty(bundleOptions)) {
        specification.setBundle(new Bundle(bundleOptions), bundlePrice);
      }
      if (DataUtils.isId(specification.getBranchId())) {
        String idName = sys.getIdName(TBL_CONF_PRICELIST);

        rs = qs.getData(new SqlSelect()
            .addFields(TBL_CONF_PRICELIST, idName, COL_BRANCH, COL_PHOTO)
            .addFrom(TBL_CONF_PRICELIST));

        SimpleRowSet.SimpleRow row = rs.getRowByKey(idName,
            BeeUtils.toString(specification.getBranchId()));

        while (Objects.nonNull(row)) {
          specification.getPhotos().add(0, row.getLong(COL_PHOTO));
          String id = row.getValue(COL_BRANCH);
          row = DataUtils.isId(id) ? rs.getRowByKey(idName, id) : null;
        }
      }
    }
    return ResponseObject.response(specification);
  }

  private ResponseObject saveDimensions(Long branchId, List<Long> rows, List<Long> cols) {
    String idName = sys.getIdName(TBL_CONF_DIMENSIONS);

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_CONF_DIMENSIONS, idName, COL_GROUP, COL_ORDINAL)
        .addFrom(TBL_CONF_DIMENSIONS)
        .setWhere(SqlUtils.equals(TBL_CONF_DIMENSIONS, COL_BRANCH, branchId)));

    Set<Long> usedIds = new HashSet<>();
    List<Pair<Long, Integer>> list = new ArrayList<>();

    for (int i = 0; i < rows.size(); i++) {
      list.add(Pair.of(rows.get(i), i));
    }
    for (int i = 0; i < cols.size(); i++) {
      list.add(Pair.of(cols.get(i), (i + 1) * (-1)));
    }
    for (Pair<Long, Integer> pair : list) {
      boolean found = false;

      for (SimpleRowSet.SimpleRow row : data) {
        Long id = row.getLong(idName);
        found = !usedIds.contains(id) && Objects.equals(pair.getA(), row.getLong(COL_GROUP));

        if (found) {
          if (!Objects.equals(row.getInt(COL_ORDINAL), pair.getB())) {
            qs.updateData(new SqlUpdate(TBL_CONF_DIMENSIONS)
                .addConstant(COL_ORDINAL, pair.getB())
                .setWhere(sys.idEquals(TBL_CONF_DIMENSIONS, id)));
          }
          usedIds.add(id);
          break;
        }
      }
      if (!found) {
        qs.insertData(new SqlInsert(TBL_CONF_DIMENSIONS)
            .addConstant(COL_BRANCH, branchId)
            .addConstant(COL_GROUP, pair.getA())
            .addConstant(COL_ORDINAL, pair.getB()));
      }
    }
    List<Long> unusedIds = Arrays.stream(data.getLongColumn(idName))
        .filter(id -> !usedIds.contains(id))
        .collect(Collectors.toList());

    if (!BeeUtils.isEmpty(unusedIds)) {
      qs.updateData(new SqlDelete(TBL_CONF_DIMENSIONS)
          .setWhere(sys.idInList(TBL_CONF_DIMENSIONS, unusedIds)));
    }
    return ResponseObject.emptyResponse();
  }

  private ResponseObject saveObject(Specification specification) {
    long objectId = qs.insertData(new SqlInsert(TBL_CONF_OBJECTS)
        .addConstant(COL_BRANCH, specification.getBranchId())
        .addConstant(COL_BRANCH_NAME, specification.getBranchName())
        .addConstant(COL_DESCRIPTION, specification.getDescription())
        .addConstant(COL_PRICE, specification.getBundlePrice()));

    if (specification.getBundle() != null) {
      for (Option option : specification.getBundle().getOptions()) {
        qs.insertData(new SqlInsert(TBL_CONF_OBJECT_OPTIONS)
            .addConstant(COL_OBJECT, objectId)
            .addConstant(COL_OPTION, option.getId()));
      }
    }
    for (Option option : specification.getOptions()) {
      qs.insertData(new SqlInsert(TBL_CONF_OBJECT_OPTIONS)
          .addConstant(COL_OBJECT, objectId)
          .addConstant(COL_OPTION, option.getId())
          .addConstant(COL_PRICE, specification.getOptionPrice(option)));
    }
    return ResponseObject.response(objectId);
  }

  private ResponseObject setBundle(Long branchId, Bundle bundle, Configuration.DataInfo info,
      boolean blocked) {
    int c = 0;
    Long bundleId = qs.getLong(new SqlSelect()
        .addFields(TBL_CONF_BUNDLES, sys.getIdName(TBL_CONF_BUNDLES))
        .addFrom(TBL_CONF_BUNDLES)
        .setWhere(SqlUtils.equals(TBL_CONF_BUNDLES, COL_KEY, bundle.getKey())));

    if (!DataUtils.isId(bundleId)) {
      bundleId = qs.insertData(new SqlInsert(TBL_CONF_BUNDLES)
          .addConstant(COL_KEY, bundle.getKey()));

      for (Option option : bundle.getOptions()) {
        qs.insertData(new SqlInsert(TBL_CONF_BUNDLE_OPTIONS)
            .addConstant(COL_BUNDLE, bundleId)
            .addConstant(COL_OPTION, option.getId()));
      }
    } else {
      c = qs.updateData(new SqlUpdate(TBL_CONF_BRANCH_BUNDLES)
          .addConstant(COL_PRICE, info.getPrice())
          .addConstant(COL_DESCRIPTION, info.getDescription())
          .addConstant(COL_BLOCKED, blocked)
          .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_BUNDLES, COL_BRANCH, branchId, COL_BUNDLE,
              bundleId)));
    }
    if (!BeeUtils.isPositive(c)) {
      qs.insertData(new SqlInsert(TBL_CONF_BRANCH_BUNDLES)
          .addConstant(COL_BRANCH, branchId)
          .addConstant(COL_BUNDLE, bundleId)
          .addNotEmpty(COL_PRICE, info.getPrice())
          .addNotEmpty(COL_DESCRIPTION, info.getDescription())
          .addConstant(COL_BLOCKED, blocked));
    }
    return ResponseObject.emptyResponse();
  }

  private ResponseObject setOption(Long branchId, Long optionId, Configuration.DataInfo info) {
    int c = qs.updateData(new SqlUpdate(TBL_CONF_BRANCH_OPTIONS)
        .addConstant(COL_PRICE, info.getPrice())
        .addConstant(COL_DESCRIPTION, info.getDescription())
        .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS, COL_BRANCH, branchId, COL_OPTION,
            optionId)));

    if (!BeeUtils.isPositive(c)) {
      qs.insertData(new SqlInsert(TBL_CONF_BRANCH_OPTIONS)
          .addConstant(COL_BRANCH, branchId)
          .addConstant(COL_OPTION, optionId)
          .addNotEmpty(COL_PRICE, info.getPrice())
          .addNotEmpty(COL_DESCRIPTION, info.getDescription()));
    }
    return ResponseObject.emptyResponse();
  }

  private ResponseObject setRelation(Long branchId, String key, Long optionId,
      Configuration.DataInfo info) {

    SimpleRowSet.SimpleRow row = qs.getRow(new SqlSelect()
        .addField(TBL_CONF_BRANCH_BUNDLES, sys.getIdName(TBL_CONF_BRANCH_BUNDLES),
            COL_BRANCH_BUNDLE)
        .addField(TBL_CONF_BRANCH_OPTIONS, sys.getIdName(TBL_CONF_BRANCH_OPTIONS),
            COL_BRANCH_OPTION)
        .addFields(TBL_CONF_RELATIONS, sys.getIdName(TBL_CONF_RELATIONS))
        .addFrom(TBL_CONF_BRANCH_BUNDLES)
        .addFromInner(TBL_CONF_BUNDLES,
            SqlUtils.and(sys.joinTables(TBL_CONF_BUNDLES, TBL_CONF_BRANCH_BUNDLES, COL_BUNDLE),
                SqlUtils.equals(TBL_CONF_BUNDLES, COL_KEY, key)))
        .addFromLeft(TBL_CONF_BRANCH_OPTIONS,
            SqlUtils.and(SqlUtils.joinUsing(TBL_CONF_BRANCH_BUNDLES, TBL_CONF_BRANCH_OPTIONS,
                COL_BRANCH), SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS, COL_OPTION, optionId)))
        .addFromLeft(TBL_CONF_RELATIONS, SqlUtils.and(sys.joinTables(TBL_CONF_BRANCH_BUNDLES,
            TBL_CONF_RELATIONS, COL_BRANCH_BUNDLE), sys.joinTables(TBL_CONF_BRANCH_OPTIONS,
            TBL_CONF_RELATIONS, COL_BRANCH_OPTION)))
        .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_BUNDLES, COL_BRANCH, branchId)));

    Assert.notNull(row);
    Long relationId = row.getLong(sys.getIdName(TBL_CONF_RELATIONS));

    if (DataUtils.isId(relationId)) {
      qs.updateData(new SqlUpdate(TBL_CONF_RELATIONS)
          .addConstant(COL_PRICE, info.getPrice())
          .addConstant(COL_DESCRIPTION, info.getDescription())
          .setWhere(sys.idEquals(TBL_CONF_RELATIONS, relationId)));
    } else {
      Long branchOptionId = row.getLong(COL_BRANCH_OPTION);

      if (!DataUtils.isId(branchOptionId)) {
        branchOptionId = qs.insertData(new SqlInsert(TBL_CONF_BRANCH_OPTIONS)
            .addConstant(COL_BRANCH, branchId)
            .addConstant(COL_OPTION, optionId));
      }
      qs.insertData(new SqlInsert(TBL_CONF_RELATIONS)
          .addConstant(COL_BRANCH_BUNDLE, row.getLong(COL_BRANCH_BUNDLE))
          .addConstant(COL_BRANCH_OPTION, branchOptionId)
          .addNotEmpty(COL_PRICE, info.getPrice())
          .addNotEmpty(COL_DESCRIPTION, info.getDescription()));
    }
    return ResponseObject.emptyResponse();
  }

  private ResponseObject setRestrictions(Long branchId, Map<Long, Map<Long, Boolean>> data) {
    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(TBL_CONF_BRANCH_OPTIONS, COL_OPTION)
        .addField(TBL_CONF_BRANCH_OPTIONS, sys.getIdName(TBL_CONF_BRANCH_OPTIONS),
            COL_BRANCH_OPTION)
        .addField(TBL_CONF_RESTRICTIONS, COL_OPTION, TBL_CONF_RELATIONS + COL_OPTION)
        .addFields(TBL_CONF_RESTRICTIONS, COL_DENIED)
        .addFrom(TBL_CONF_BRANCH_OPTIONS)
        .addFromLeft(TBL_CONF_RESTRICTIONS,
            sys.joinTables(TBL_CONF_BRANCH_OPTIONS, TBL_CONF_RESTRICTIONS, COL_BRANCH_OPTION))
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS, COL_BRANCH, branchId),
            SqlUtils.inList(TBL_CONF_BRANCH_OPTIONS, COL_OPTION, data.keySet()))));

    Map<Long, Pair<Long, Map<Long, Boolean>>> map = new HashMap<>();

    for (SimpleRowSet.SimpleRow row : rs) {
      Long option = row.getLong(COL_OPTION);

      if (!map.containsKey(option)) {
        map.put(option, Pair.of(row.getLong(COL_BRANCH_OPTION), new HashMap<>()));
      }
      Long relatedOption = row.getLong(TBL_CONF_RELATIONS + COL_OPTION);

      if (DataUtils.isId(relatedOption)) {
        map.get(option).getB().put(relatedOption, BeeUtils.unbox(row.getBoolean(COL_DENIED)));
      }
    }
    for (Long option : map.keySet()) {
      Map<Long, Boolean> restrictions = data.remove(option);
      Long branchOption = map.get(option).getA();

      for (Map.Entry<Long, Boolean> entry : map.get(option).getB().entrySet()) {
        Long opt = entry.getKey();
        Boolean denied = restrictions.remove(opt);

        if (!Objects.equals(denied, entry.getValue())) {
          IsQuery query;
          IsCondition clause = SqlUtils.equals(TBL_CONF_RESTRICTIONS, COL_BRANCH_OPTION,
              branchOption, COL_OPTION, opt);

          if (denied == null) {
            query = new SqlDelete(TBL_CONF_RESTRICTIONS)
                .setWhere(clause);
          } else {
            query = new SqlUpdate(TBL_CONF_RESTRICTIONS)
                .addConstant(COL_DENIED, denied)
                .setWhere(clause);
          }
          qs.updateData(query);
        }
      }
      for (Long opt : restrictions.keySet()) {
        qs.insertData(new SqlInsert(TBL_CONF_RESTRICTIONS)
            .addConstant(COL_BRANCH_OPTION, branchOption)
            .addConstant(COL_OPTION, opt)
            .addConstant(COL_DENIED, restrictions.get(opt)));
      }
    }
    for (Long option : data.keySet()) {
      Map<Long, Boolean> restrictions = data.get(option);

      if (!BeeUtils.isEmpty(restrictions)) {
        Long branchOption = qs.insertData(new SqlInsert(TBL_CONF_BRANCH_OPTIONS)
            .addConstant(COL_BRANCH, branchId)
            .addConstant(COL_OPTION, option));

        for (Long opt : restrictions.keySet()) {
          qs.insertData(new SqlInsert(TBL_CONF_RESTRICTIONS)
              .addConstant(COL_BRANCH_OPTION, branchOption)
              .addConstant(COL_OPTION, opt)
              .addConstant(COL_DENIED, restrictions.get(opt)));
        }
      }
    }
    return ResponseObject.emptyResponse();
  }
}
