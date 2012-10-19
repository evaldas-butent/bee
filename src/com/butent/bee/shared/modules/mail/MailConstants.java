package com.butent.bee.shared.modules.mail;

public class MailConstants {

  public static enum Protocol {
    POP3, SMTP
  }

  public static final String MAIL_MODULE = "Mail";
  public static final String MAIL_METHOD = MAIL_MODULE + "Method";

  public static final String SVC_RESTART_PROXY = "restart_proxy";
  public static final String SVC_GET_MESSAGE = "get_message";

  public static final String TBL_ADDRESSES = "Addresses";

  public static final String TBL_MESSAGES = "Messages";
  public static final String TBL_HEADERS = "Headers";
  public static final String TBL_RECIPIENTS = "Recipients";
  public static final String TBL_PARTS = "Parts";
  public static final String TBL_ATTACHMENTS = "Attachments";

  public static final String COL_MESSAGE = "Message";
  public static final String COL_FILE = "File";
  public static final String COL_ADDRESS = "Address";

  private MailConstants() {
  }
}
