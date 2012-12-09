package com.butent.bee.server.modules.mail;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.mail.MailConstants.Protocol;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Map;

public class MailAccount {

  private String error = null;

  private final Protocol storeProtocol;
  private final String storeHost;
  private final Integer storePort;
  private final String storeLogin;
  private final String storePassword;

  private final Protocol transportProtocol = Protocol.SMTP;
  private final String transportHost;
  private final Integer transportPort;

  private final Long accountId;
  private final Long addressId;

  MailAccount(Map<String, String> data) {
    if (data == null) {
      error = "Unknown account";
      storeProtocol = null;
      storeHost = null;
      storePort = null;
      storeLogin = null;
      storePassword = null;

      transportHost = null;
      transportPort = null;

      accountId = null;
      addressId = null;
    } else {
      storeProtocol = NameUtils.getEnumByName(Protocol.class, data.get(COL_STORE_STYPE));
      storeHost = data.get(COL_STORE_SERVER);
      storePort = BeeUtils.toIntOrNull(data.get(COL_STORE_SPORT));
      storeLogin = BeeUtils.notEmpty(data.get(COL_STORE_LOGIN),
          data.get(CommonsConstants.COL_EMAIL));
      storePassword = data.get(COL_STORE_PASSWORD);

      transportHost = data.get(COL_TRANSPORT_SERVER);
      transportPort = BeeUtils.toIntOrNull(data.get(COL_TRANSPORT_PORT));

      accountId = BeeUtils.toLongOrNull(data.get(COL_ACCOUNT));
      addressId = BeeUtils.toLongOrNull(data.get(CommonsConstants.COL_ADDRESS));
    }
    if (BeeUtils.isEmpty(error) && !DataUtils.isId(addressId)) {
      error = "Unknown account address";
    }
  }

  public Long getAccountId() {
    return accountId;
  }

  public Long getAddressId() {
    return addressId;
  }

  public String getStoreErrorMessage() {
    String err = error;

    if (BeeUtils.isEmpty(err)) {
      if (storeProtocol == null) {
        err = "Unknown store protocol";

      } else if (BeeUtils.isEmpty(storeHost)) {
        err = "Unknown store host";

      } else if (BeeUtils.isEmpty(storeLogin)) {
        err = "Unknown store login";
      }
    }
    return err;
  }

  public String getStoreHost() {
    return storeHost;
  }

  public String getStoreLogin() {
    return storeLogin;
  }

  public String getStorePassword() {
    return storePassword;
  }

  public int getStorePort() {
    return BeeUtils.isPositive(storePort) ? storePort : -1;
  }

  public Protocol getStoreProtocol() {
    return storeProtocol;
  }

  public String getTransportErrorMessage() {
    String err = error;

    if (BeeUtils.isEmpty(err)) {
      if (transportProtocol == null) {
        err = "Unknown transport protocol";

      } else if (BeeUtils.isEmpty(transportHost)) {
        err = "Unknown transport host";
      }
    }
    return err;
  }

  public String getTransportHost() {
    return transportHost;
  }

  public Integer getTransportPort() {
    return BeeUtils.isPositive(transportPort) ? transportPort : -1;
  }

  public Protocol getTransportProtocol() {
    return transportProtocol;
  }

  public boolean isValidStoreAccount() {
    return BeeUtils.isEmpty(getStoreErrorMessage());
  }

  public boolean isValidTransportAccount() {
    return BeeUtils.isEmpty(getTransportErrorMessage());
  }
}
