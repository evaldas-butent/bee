package com.butent.bee.shared.modules.cars;

public final class CarsConstants {
  public static final String SVC_GET_CONFIGURATION = "GetConfiguration";
  public static final String SVC_SAVE_DIMENSIONS = "SaveDimensions";
  public static final String SVC_SET_BUNDLE = "SetBundle";
  public static final String SVC_DELETE_BUNDLES = "DeleteBundles";
  public static final String SVC_SET_OPTION = "SetOption";
  public static final String SVC_DELETE_OPTION = "DeleteOption";
  public static final String SVC_SET_RELATION = "SetRelation";
  public static final String SVC_DELETE_RELATION = "DeleteRelation";
  public static final String SVC_SET_RESTRICTIONS = "SetRestrictions";
  public static final String SVC_SAVE_OBJECT = "SaveObject";
  public static final String SVC_GET_OBJECT = "GetObject";

  public static final String TBL_CONF_GROUPS = "ConfGroups";
  public static final String TBL_CONF_OPTIONS = "ConfOptions";
  public static final String TBL_CONF_PRICELIST = "ConfPricelist";
  public static final String TBL_CONF_DIMENSIONS = "ConfDimensions";
  public static final String TBL_CONF_BUNDLES = "ConfBundles";
  public static final String TBL_CONF_BUNDLE_OPTIONS = "ConfBundleOptions";
  public static final String TBL_CONF_BRANCH_BUNDLES = "ConfBranchBundles";
  public static final String TBL_CONF_BRANCH_OPTIONS = "ConfBranchOptions";
  public static final String TBL_CONF_RELATIONS = "ConfRelations";
  public static final String TBL_CONF_RESTRICTIONS = "ConfRestrictions";
  public static final String TBL_CONF_OBJECTS = "ConfObjects";
  public static final String TBL_CONF_OBJECT_OPTIONS = "ConfObjectOptions";

  public static final String TBL_CAR_ORDERS = "CarOrders";
  public static final String TBL_CAR_ORDER_ITEMS = "CarOrderItems";

  public static final String TBL_STAGES = "Stages";
  public static final String TBL_STAGE_CONDITIONS = "StageConditions";
  public static final String TBL_STAGE_ACTIONS = "StageActions";
  public static final String TBL_STAGE_TRIGGERS = "StageTriggers";

  public static final String COL_BRANCH = "Branch";
  public static final String COL_BRANCH_NAME = "BranchName";
  public static final String COL_OPTION = "Option";
  public static final String COL_OPTION_NAME = "OptionName";
  public static final String COL_CODE = "Code";
  public static final String COL_DESCRIPTION = "Description";
  public static final String COL_BUNDLE = "Bundle";
  public static final String COL_KEY = "Key";
  public static final String COL_TYPE = "Type";
  public static final String COL_GROUP = "Group";
  public static final String COL_GROUP_NAME = "GroupName";
  public static final String COL_BLOCKED = "Blocked";
  public static final String COL_ORDINAL = "Ordinal";
  public static final String COL_REQUIRED = "Required";
  public static final String COL_BRANCH_BUNDLE = "BranchBundle";
  public static final String COL_BRANCH_OPTION = "BranchOption";
  public static final String COL_PRICE = "Price";
  public static final String COL_DENIED = "Denied";
  public static final String COL_OBJECT = "Object";

  public static final String COL_ORDER = "Order";

  public static final String COL_STAGE = "Stage";
  public static final String COL_STAGE_VIEW = "ViewName";
  public static final String COL_STAGE_NAME = "StageName";

  public static final String COL_STAGE_FIELD = "Field";
  public static final String COL_STAGE_OPERATOR = "Operator";
  public static final String COL_STAGE_VALUE = "Value";
  public static final String COL_STAGE_ACTION = "Action";
  public static final String COL_STAGE_TRIGGER = "Trigger";

  public static final String FORM_CONF_OPTION = "ConfOption";
  public static final String FORM_CAR_ORDER = "CarOrder";
  public static final String FORM_STAGES = "StageEditor";

  public static final String VAR_PRICE_DEFAULT = "PriceDefault";
  public static final String VAR_PRICE_OPTIONAL = "PriceOptional";
  public static final String VAR_REL_REQUIRED = "RelRequired";
  public static final String VAR_REL_DENIED = "RelDenied";

  public static final String STAGE_ACTION_READONLY = "MakeOrderReadOnly";
  public static final String STAGE_ACTION_LOST = "EnterOrderLostReason";
  public static final String STAGE_TRIGGER_NEW = "OnCreateNewOrder";
  public static final String STAGE_TRIGGER_SENT = "OnSendOrder";

  private CarsConstants() {
  }
}