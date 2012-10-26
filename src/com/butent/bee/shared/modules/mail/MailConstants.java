package com.butent.bee.shared.modules.mail;

public class MailConstants {

  public static enum AddressType {
    TO, CC, BCC
  }

  public static enum Protocol {
    POP3, IMAP, SMTP
  }

  public static final String MAIL_MODULE = "Mail";
  public static final String MAIL_METHOD = MAIL_MODULE + "Method";

  public static final int STATUS_NEUTRAL = 0;
  public static final int STATUS_DRAFT = 1;
  public static final int STATUS_DELETED = 2;
  public static final int STATUS_PURGED = 3;

  public static final String SVC_RESTART_PROXY = "restart_proxy";
  public static final String SVC_GET_MESSAGE = "get_message";
  public static final String SVC_GET_ACCOUNTS = "get_accounts";
  public static final String SVC_CHECK_MAIL = "check_mail";
  public static final String SVC_REMOVE_MESSAGES = "remove_messages";

  public static final String TBL_ACCOUNTS = "Accounts";
  public static final String TBL_ADDRESSES = "Addresses";

  public static final String TBL_MESSAGES = "Messages";
  public static final String TBL_HEADERS = "Headers";
  public static final String TBL_RECIPIENTS = "Recipients";
  public static final String TBL_PARTS = "Parts";
  public static final String TBL_ATTACHMENTS = "Attachments";

  public static final String COL_MESSAGE = "Message";
  public static final String COL_FILE = "File";
  public static final String COL_ADDRESS = "Address";
  public static final String COL_USER = "User";

  private MailConstants() {
  }
}
