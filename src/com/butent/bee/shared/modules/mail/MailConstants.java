package com.butent.bee.shared.modules.mail;

public class MailConstants {

  public static enum Protocol {
    POP3, SMTP
  }

  public static final String MAIL_MODULE = "Mail";
  public static final String MAIL_METHOD = MAIL_MODULE + "Method";

  public static final String SVC_RESTART_PROXY = "restart_proxy";

  private MailConstants() {
  }
}
