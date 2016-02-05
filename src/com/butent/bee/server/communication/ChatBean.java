package com.butent.bee.server.communication;

import static com.butent.bee.shared.communication.ChatConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class ChatBean {

  private static BeeLogger logger = LogUtils.getLogger(ChatBean.class);

  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  public ResponseObject getChats() {
    Long userId = usr.getCurrentUserId();
    if (!DataUtils.isId(userId)) {
      String message = BeeUtils.joinWords(NameUtils.getName(this), "current user not available");
      logger.warning(message);

      return ResponseObject.warning(message);
    }

    SqlSelect chatQuery = new SqlSelect()
        .addFields(TBL_CHATS, COL_CHAT_NAME, COL_CHAT_CREATED, COL_CHAT_CREATOR)
        .addFields(TBL_CHAT_USERS, COL_CHAT, COL_CHAT_USER_REGISTERED, COL_CHAT_USER_LAST_ACCESS)
        .addFrom(TBL_CHATS)
        .addFromInner(TBL_CHAT_USERS, sys.joinTables(TBL_CHATS, TBL_CHAT_USERS, COL_CHAT))
        .setWhere(SqlUtils.equals(TBL_CHAT_USERS, COL_CHAT_USER, userId));

    SimpleRowSet chatData = qs.getData(chatQuery);
    if (DataUtils.isEmpty(chatData)) {
      logger.info("no chats found for user", userId);
      return ResponseObject.emptyResponse();
    }

    return ResponseObject.emptyResponse();
  }
}
