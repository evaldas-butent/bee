package com.butent.bee.shared.modules.mail;

public class MailConstants {

  public enum AddressType {
    TO, CC, BCC
  }

  public enum Protocol {
    POP3, IMAP, SMTP
  }

  public static enum MessageStatus {
    NEUTRAL, DRAFT, DELETED, PURGED
  }

  public static final String MAIL_MODULE = "Mail";
  public static final String MAIL_METHOD = MAIL_MODULE + "Method";

  public static final String SVC_RESTART_PROXY = "restart_proxy";
  public static final String SVC_GET_MESSAGE = "get_message";
  public static final String SVC_GET_ACCOUNTS = "get_accounts";
  public static final String SVC_CHECK_MAIL = "check_mail";
  public static final String SVC_SEND_MAIL = "send_mail";
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
  public static final String COL_SENDER = "Sender";
  public static final String COL_SUBJECT = "Subject";
  public static final String COL_CONTENT = "Content";
  public static final String COL_STATUS = "Status";

  private MailConstants() {
  }
}
