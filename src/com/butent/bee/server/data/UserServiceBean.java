package com.butent.bee.server.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.server.i18n.I18nUtils;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.LocalizableMessages;
import com.butent.bee.shared.i18n.SupportedLocale;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.io.IOException;
import java.io.StringReader;
import java.security.Principal;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

/**
 * Responsible for users system, their login status, localization, user and roles cache etc.
 */

@Singleton
@Lock(LockType.READ)
public class UserServiceBean {

  private final class UserInfo {

    private final UserData userData;
    private final String password;
    private Collection<Long> userRoles;

    private SupportedLocale userLocale = SupportedLocale.DEFAULT;
    private UserInterface userInterface;

    private DateTime blockAfter;
    private DateTime blockBefore;

    private boolean online;

    private UserInfo(UserData userData, String password) {
      this.userData = userData;
      this.password = password;
    }

    private DateTime getBlockAfter() {
      return blockAfter;
    }

    private DateTime getBlockBefore() {
      return blockBefore;
    }

    private Long getCompany() {
      return userData.getCompany();
    }

    private Long getCompanyPerson() {
      return userData.getCompanyPerson();
    }

    private String getLanguage() {
      return getUserLocale().getLanguage();
    }

    private String getPassword() {
      return password;
    }

    private Long getPerson() {
      return userData.getPerson();
    }

    private Collection<Long> getRoles() {
      return userRoles;
    }

    private UserData getUserData() {
      return userData;
    }

    private UserInterface getUserInterface() {
      return userInterface;
    }

    private SupportedLocale getUserLocale() {
      return userLocale;
    }

    private boolean isBlocked(long time) {
      if (getBlockAfter() != null && getBlockBefore() != null) {
        return getBlockAfter().getTime() <= time && getBlockBefore().getTime() > time;
      } else if (getBlockAfter() != null) {
        return getBlockAfter().getTime() <= time;
      } else if (getBlockBefore() != null) {
        return getBlockBefore().getTime() > time;
      } else {
        return false;
      }
    }

    private boolean isOnline() {
      return online;
    }

    private UserInfo setBlockAfter(DateTime after) {
      this.blockAfter = after;
      return this;
    }

    private UserInfo setBlockBefore(DateTime before) {
      this.blockBefore = before;
      return this;
    }

    private UserInfo setOnline(boolean isOnline) {
      this.online = isOnline;
      return this;
    }

    private UserInfo setProperties(String properties) {
      Map<String, String> props = null;

      if (!BeeUtils.isEmpty(properties)) {
        props = Maps.newHashMap();
        Properties prp = new Properties();

        try {
          prp.load(new StringReader(properties));
        } catch (IOException e) {
          logger.error(e, properties);
        }
        for (String p : prp.stringPropertyNames()) {
          props.put(p, prp.getProperty(p));
        }
      }
      userData.setProperties(props);
      return this;
    }

    private UserInfo setRoles(Collection<Long> roles) {
      this.userRoles = roles;
      return this;
    }

    private UserInfo setUserInterface(UserInterface ui) {
      this.userInterface = ui;
      return this;
    }

    private UserInfo setUserLocale(SupportedLocale sl) {
      this.userLocale = BeeUtils.nvl(sl, SupportedLocale.DEFAULT);
      return this;
    }
  }

  private static BeeLogger logger = LogUtils.getLogger(UserServiceBean.class);

  private static String key(String value) {
    return (value == null) ? null : value.toLowerCase();
  }

  @Resource
  EJBContext ctx;
  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;

  private final Map<Long, String> roleCache = Maps.newHashMap();
  private final BiMap<Long, String> userCache = HashBiMap.create();
  private Map<String, UserInfo> infoCache = Maps.newHashMap();

  private final Map<RightsObjectType, Map<String, Multimap<RightsState, Long>>> rightsCache =
      Maps.newHashMap();

  @Lock(LockType.WRITE)
  public boolean authenticateUser(String user, String password) {
    if (BeeUtils.isEmpty(userCache)) {
      long company = qs.insertData(new SqlInsert(TBL_COMPANIES)
          .addConstant(COL_COMPANY_NAME, user));

      long person = qs.insertData(new SqlInsert(TBL_PERSONS)
          .addConstant(COL_FIRST_NAME, user));

      long companyPerson = qs.insertData(new SqlInsert(TBL_COMPANY_PERSONS)
          .addConstant(COL_COMPANY, company)
          .addConstant(COL_PERSON, person));

      qs.insertData(new SqlInsert(TBL_USERS)
          .addConstant(COL_LOGIN, user)
          .addConstant(COL_PASSWORD, password)
          .addConstant(COL_COMPANY_PERSON, companyPerson));
    }
    UserInfo info = getUserInfo(getUserId(user));
    return info != null && Objects.equals(password, info.getPassword());
  }

  public String getCurrentUser() {
    Principal p = ctx.getCallerPrincipal();
    Assert.notNull(p);
    return p.getName().toLowerCase();
  }

  public UserData getCurrentUserData() {
    UserInfo userInfo = getCurrentUserInfo();
    return (userInfo == null) ? null : userInfo.getUserData();
  }

  public Filter getCurrentUserFilter(String column) {
    return ComparisonFilter.isEqual(column, new LongValue(getCurrentUserId()));
  }

  public Long getCurrentUserId() {
    return getUserId(getCurrentUser());
  }

  public Long getEmailId(Long userId) {
    if (userId == null) {
      return null;
    }

    UserInfo userInfo = getUserInfo(userId);
    if (userInfo == null) {
      return null;
    }

    if (DataUtils.isId(userInfo.getCompanyPerson())) {
      Long id =
          qs.getLong(new SqlSelect().addFields(TBL_CONTACTS, COL_EMAIL)
              .addFrom(TBL_CONTACTS)
              .addFromLeft(TBL_COMPANY_PERSONS,
                  sys.joinTables(TBL_CONTACTS, TBL_COMPANY_PERSONS, COL_CONTACT))
              .setWhere(sys.idEquals(TBL_COMPANY_PERSONS, userInfo.getCompanyPerson())));

      if (DataUtils.isId(id)) {
        return id;
      }
    }

    if (DataUtils.isId(userInfo.getPerson())) {
      Long id = qs.getLong(new SqlSelect().addFields(TBL_CONTACTS, COL_EMAIL)
          .addFrom(TBL_CONTACTS)
          .addFromLeft(TBL_PERSONS, sys.joinTables(TBL_CONTACTS, TBL_PERSONS, COL_CONTACT))
          .setWhere(sys.idEquals(TBL_PERSONS, userInfo.getPerson())));

      if (DataUtils.isId(id)) {
        return id;
      }
    }

    if (DataUtils.isId(userInfo.getCompany())) {
      Long id = qs.getLong(new SqlSelect().addFields(TBL_CONTACTS, COL_EMAIL)
          .addFrom(TBL_CONTACTS)
          .addFromLeft(TBL_COMPANIES, sys.joinTables(TBL_CONTACTS, TBL_COMPANIES, COL_CONTACT))
          .setWhere(sys.idEquals(TBL_COMPANIES, userInfo.getCompany())));

      if (DataUtils.isId(id)) {
        return id;
      }
    }

    return null;
  }

  public String getLanguage() {
    return getLanguage(getCurrentUserId());
  }

  public String getLanguage(Long userId) {
    UserInfo userInfo = (userId == null) ? null : getUserInfo(userId);
    return (userInfo == null) ? SupportedLocale.DEFAULT.getLanguage() : userInfo.getLanguage();
  }

  public Locale getLocale() {
    return BeeUtils.nvl(I18nUtils.toLocale(getLanguage(getCurrentUserId())),
        Localizations.getDefaultLocale());
  }

  public LocalizableConstants getLocalizableConstants() {
    return getLocalizableConstants(getCurrentUserId());
  }

  public LocalizableConstants getLocalizableConstants(Long userId) {
    return Localizations.getPreferredConstants(getLanguage(userId));
  }

  public LocalizableMessages getLocalizableMesssages() {
    return getLocalizableMesssages(getCurrentUserId());
  }

  public LocalizableMessages getLocalizableMesssages(Long userId) {
    return Localizations.getPreferredMessages(getLanguage(userId));
  }

  public String getRoleName(Long roleId) {
    Assert.contains(roleCache, roleId);
    return roleCache.get(roleId);
  }

  public Set<Long> getRoles() {
    return roleCache.keySet();
  }

  public Long getUserId(String user) {
    if (userCache.inverse().containsKey(key(user))) {
      return userCache.inverse().get(key(user));
    } else {
      return null;
    }
  }

  public UserInterface getUserInterface(String user) {
    UserInfo userInfo = getUserInfo(getUserId(user));
    return (userInfo == null) ? null : userInfo.getUserInterface();
  }

  public SupportedLocale getUserLocale(String user) {
    UserInfo userInfo = getUserInfo(getUserId(user));
    return (userInfo == null) ? null : userInfo.getUserLocale();
  }

  public String getUserName(Long userId) {
    return userCache.get(userId);
  }

  public long[] getUserRoles(Long userId) {
    UserInfo userInfo = getUserInfo(userId);

    if (userInfo != null) {
      return Longs.toArray(getUserInfo(userId).getRoles());
    }
    return new long[0];
  }

  public Set<Long> getUsers() {
    return userCache.keySet();
  }

  public String getUserSign(Long userId) {
    UserInfo userInfo = getUserInfo(userId);

    if (userInfo != null) {
      return userInfo.getUserData().getUserSign();
    }
    return null;
  }

  public boolean hasEventRight(String object, RightsState state) {
    UserInfo info = getCurrentUserInfo();

    if (info != null) {
      return info.getUserData().hasEventRight(object, state);
    }
    return false;
  }

  public boolean hasFormRight(String object, RightsState state) {
    UserInfo info = getCurrentUserInfo();

    if (info != null) {
      return info.getUserData().hasFormRight(object, state);
    }
    return false;
  }

  public boolean hasGridRight(String object, RightsState state) {
    UserInfo info = getCurrentUserInfo();

    if (info != null) {
      return info.getUserData().hasGridRight(object, state);
    }
    return false;
  }

  public boolean hasMenuRight(String object, RightsState state) {
    UserInfo info = getCurrentUserInfo();

    if (info != null) {
      return info.getUserData().hasMenuRight(object, state);
    }
    return false;
  }

  public boolean hasModuleRight(String object, RightsState state) {
    UserInfo info = getCurrentUserInfo();

    if (info != null) {
      return info.getUserData().hasModuleRight(object, state);
    }
    return false;
  }

  @Lock(LockType.WRITE)
  public void initRights() {
    rightsCache.clear();

    SqlSelect ss = new SqlSelect()
        .addFields(TBL_OBJECTS, COL_OBJECT_NAME)
        .addFields(TBL_RIGHTS, COL_ROLE, COL_STATE)
        .addFrom(TBL_OBJECTS)
        .addFromInner(TBL_RIGHTS, sys.joinTables(TBL_OBJECTS, TBL_RIGHTS, COL_OBJECT));

    for (RightsObjectType tp : RightsObjectType.values()) {
      if (BeeUtils.isEmpty(tp.getRegisteredStates())) {
        continue;
      }
      HasConditions cl = SqlUtils.or();
      IsCondition wh =
          SqlUtils.and(SqlUtils.equals(TBL_OBJECTS, COL_OBJECT_TYPE, tp.ordinal()), cl);

      for (RightsState state : tp.getRegisteredStates()) {
        cl.add(SqlUtils.equals(TBL_RIGHTS, COL_STATE, state.ordinal()));
      }
      SimpleRowSet res = qs.getData(ss.setWhere(wh));

      if (res.getNumberOfRows() > 0) {
        Map<String, Multimap<RightsState, Long>> rightsObjects = rightsCache.get(tp);

        if (rightsObjects == null) {
          rightsObjects = Maps.newHashMap();
          rightsCache.put(tp, rightsObjects);
        }
        for (int i = 0; i < res.getNumberOfRows(); i++) {
          RightsState state = NameUtils.getEnumByIndex(RightsState.class, res.getInt(i, COL_STATE));
          String objectName = BeeUtils.normalize(res.getValue(i, COL_OBJECT_NAME));
          Multimap<RightsState, Long> objectStates = rightsObjects.get(objectName);

          if (objectStates == null) {
            objectStates = HashMultimap.create();
            rightsObjects.put(objectName, objectStates);
          }
          objectStates.put(state, res.getLong(i, COL_ROLE));
        }
      }
    }
  }

  @Lock(LockType.WRITE)
  public void initUsers() {
    roleCache.clear();
    userCache.clear();
    Map<String, UserInfo> expiredCache = infoCache;
    infoCache = Maps.newHashMap();

    String userIdName = sys.getIdName(TBL_USERS);
    String roleIdName = sys.getIdName(TBL_ROLES);

    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(TBL_ROLES, roleIdName, COL_ROLE_NAME)
        .addFrom(TBL_ROLES));

    for (int i = 0; i < rs.getNumberOfRows(); i++) {
      roleCache.put(rs.getLong(i, roleIdName), rs.getValue(i, COL_ROLE_NAME));
    }

    rs = qs.getData(new SqlSelect()
        .addFields(TBL_USER_ROLES, COL_USER, COL_ROLE)
        .addFrom(TBL_USER_ROLES));

    Multimap<Long, Long> userRoles = HashMultimap.create();

    for (int i = 0; i < rs.getNumberOfRows(); i++) {
      userRoles.put(rs.getLong(i, COL_USER), rs.getLong(i, COL_ROLE));
    }

    SqlSelect ss = new SqlSelect()
        .addFields(TBL_USERS, userIdName, COL_LOGIN, COL_PASSWORD, COL_COMPANY_PERSON,
            COL_USER_PROPERTIES, COL_USER_LOCALE, COL_USER_INTERFACE, COL_USER_BLOCK_AFTER,
            COL_USER_BLOCK_BEFORE)
        .addFields(TBL_COMPANY_PERSONS, COL_COMPANY, COL_PERSON)
        .addFields(TBL_PERSONS, COL_FIRST_NAME, COL_LAST_NAME, COL_PHOTO)
        .addField(TBL_COMPANIES, COL_COMPANY_NAME, ALS_COMPANY_NAME)
        .addFrom(TBL_USERS)
        .addFromLeft(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, TBL_USERS, COL_COMPANY_PERSON))
        .addFromLeft(TBL_PERSONS, sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON))
        .addFromLeft(TBL_COMPANIES, sys.joinTables(TBL_COMPANIES, TBL_COMPANY_PERSONS,
            COL_COMPANY));

    for (SimpleRow row : qs.getData(ss)) {
      long userId = row.getLong(userIdName);
      String login = key(row.getValue(COL_LOGIN));

      userCache.put(userId, login);

      UserData userData = new UserData(userId, login, row.getValue(COL_FIRST_NAME),
          row.getValue(COL_LAST_NAME), row.getValue(COL_PHOTO), row.getValue(ALS_COMPANY_NAME),
          row.getLong(COL_COMPANY_PERSON), row.getLong(COL_COMPANY), row.getLong(COL_PERSON));

      UserInfo user = new UserInfo(userData, row.getValue(COL_PASSWORD))
          .setRoles(userRoles.get(userId))
          .setProperties(row.getValue(COL_USER_PROPERTIES))
          .setUserLocale(NameUtils.getEnumByIndex(SupportedLocale.class,
              row.getInt(COL_USER_LOCALE)))
          .setUserInterface(NameUtils.getEnumByIndex(UserInterface.class,
              row.getInt(COL_USER_INTERFACE)))
          .setBlockAfter(TimeUtils.toDateTimeOrNull(row.getLong(COL_USER_BLOCK_AFTER)))
          .setBlockBefore(TimeUtils.toDateTimeOrNull(row.getLong(COL_USER_BLOCK_BEFORE)));

      UserInfo oldInfo = expiredCache.get(login);
      if (oldInfo != null) {
        user.setOnline(oldInfo.isOnline());
      }
      infoCache.put(login, user);
      userData.setRights(getUserRights(userId));
    }
  }

  public Boolean isBlocked(String user) {
    UserInfo userInfo = getUserInfo(getUserId(user));
    return (userInfo == null) ? null : userInfo.isBlocked(System.currentTimeMillis());
  }

  public boolean isRightsTable(String tblName) {
    return BeeUtils.inList(tblName, TBL_OBJECTS, TBL_RIGHTS);
  }

  public boolean isRoleTable(String tblName) {
    return BeeUtils.inList(tblName, TBL_ROLES, TBL_USER_ROLES);
  }

  public boolean isUser(String user) {
    return !BeeUtils.isEmpty(user) && userCache.inverse().containsKey(key(user));
  }

  public boolean isUserTable(String tblName) {
    return BeeUtils.inList(tblName, TBL_USERS, TBL_COMPANY_PERSONS, TBL_PERSONS);
  }

  @Lock(LockType.WRITE)
  public ResponseObject login(String host, String agent) {
    ResponseObject response = new ResponseObject();
    String user = getCurrentUser();

    if (isUser(user)) {
      Long userId = getUserId(user);

      UserInfo info = getUserInfo(userId);
      info.setOnline(true);

      UserData data = info.getUserData();

      data.setProperty("dsn", SqlBuilderFactory.getDsn()).setRights(getUserRights(userId));

      qs.updateData(new SqlUpdate(TBL_USERS)
          .addConstant(COL_REMOTE_HOST, host)
          .addConstant(COL_USER_AGENT, agent)
          .setWhere(sys.idEquals(TBL_USERS, userId)));

      response.setResponse(data);
      logger.info("User logged in:", user, BeeUtils.parenthesize(data.getUserSign()));

    } else if (BeeUtils.isEmpty(getUsers())) {
      response.setResponse(new UserData(-1, user).setProperty("dsn", SqlBuilderFactory.getDsn()));
      logger.warning("Anonymous user logged in:", user);

    } else {
      response.addError("Login attempt by an unauthorized user:", user);
      response.log(logger);
    }

    return response;
  }

  @Lock(LockType.WRITE)
  public void logout(String user) {
    if (isUser(user)) {
      UserInfo info = getUserInfo(getUserId(user));
      String sign = user + " " + BeeUtils.parenthesize(info.getUserData().getUserSign());

      qs.updateData(new SqlUpdate(TBL_USERS)
          .addConstant(COL_REMOTE_HOST, null)
          .setWhere(sys.idEquals(TBL_USERS, getUserId(user))));

      if (info.isOnline()) {
        info.setOnline(false);
        logger.info("User logged out:", sign);
      } else {
        logger.warning("User was not logged in:", sign);
      }
    } else if (BeeUtils.isEmpty(getUsers())) {
      logger.warning("Anonymous user logged out:", user);

    } else {
      logger.severe("Logout attempt by an unauthorized user:", user);
    }
  }

  @Lock(LockType.WRITE)
  public boolean updateUserLocale(String user, SupportedLocale locale) {
    if (!isUser(user) || locale == null) {
      return false;
    }

    Long userId = getUserId(user);
    if (!DataUtils.isId(userId)) {
      return false;
    }

    UserInfo userInfo = getUserInfo(userId);
    if (userInfo == null || userInfo.getUserLocale() == locale) {
      return false;
    }

    userInfo.setUserLocale(locale);

    qs.updateData(new SqlUpdate(TBL_USERS)
        .addConstant(COL_USER_LOCALE, locale.ordinal())
        .setWhere(sys.idEquals(TBL_USERS, userId)));

    logger.info("user", user, "updated locale:", locale.getLanguage());
    return true;
  }

  @PreDestroy
  private void destroy() {
    for (long userId : getUsers()) {
      UserInfo info = getUserInfo(userId);

      if (info != null && info.isOnline()) {
        logout(info.getUserData().getLogin());
      }
    }
  }

  private UserInfo getCurrentUserInfo() {
    return getUserInfo(getCurrentUserId());
  }

  private UserInfo getUserInfo(Long userId) {
    return infoCache.get(getUserName(userId));
  }

  private Map<RightsState, Multimap<RightsObjectType, String>> getUserRights(long userId) {
    Map<RightsState, Multimap<RightsObjectType, String>> rights = Maps.newHashMap();

    for (RightsState state : RightsState.values()) {
      Multimap<RightsObjectType, String> members = HashMultimap.create();
      rights.put(state, members);

      for (RightsObjectType type : rightsCache.keySet()) {
        Set<String> objects = rightsCache.get(type).keySet();

        for (String object : objects) {
          if (hasRight(userId, type, object, state) != state.isChecked()) {
            members.put(type, object);
          }
        }
      }
    }
    return rights;
  }

  private boolean hasRight(Long userId, RightsObjectType type, String object, RightsState state) {
    Assert.notEmpty(object);

    if (state == null) {
      return false;
    }
    boolean checked = state.isChecked();

    Map<String, Multimap<RightsState, Long>> rightsObjects = rightsCache.get(type);

    if (rightsObjects != null) {
      Multimap<RightsState, Long> objectStates = rightsObjects.get(BeeUtils.normalize(object));

      if (objectStates != null && objectStates.containsKey(state)) {
        Collection<Long> roles = objectStates.get(state);
        boolean ok = checked;

        if (roles.contains(null)) {
          ok = !checked;
        } else {
          for (long role : getUserRoles(userId)) {
            ok = checked != roles.contains(role);

            if (ok) {
              break;
            }
          }
        }
        checked = ok;
      }
    }
    return checked;
  }
}
