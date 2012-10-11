package com.butent.bee.server.modules.mail;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.ArrayUtils;

import java.util.Enumeration;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class MailEnvelope {

  private final String messageId;
  private final DateTime date;
  private final Address sender;
  private final String subject;
  private final Multimap<RecipientType, Address> recipients = HashMultimap.create();
  private final String header;

  public MailEnvelope(MimeMessage msg) throws MessagingException {
    messageId = msg.getMessageID();
    date = new DateTime(msg.getReceivedDate() == null ? msg.getSentDate() : msg.getReceivedDate());
    sender = ArrayUtils.getQuietly(msg.getFrom(), 0);
    subject = msg.getSubject();

    for (RecipientType type : new RecipientType[] {
        RecipientType.TO, RecipientType.CC, RecipientType.BCC}) {

      Address[] addresses = msg.getRecipients(type);

      if (addresses != null) {
        for (Address address : addresses) {
          recipients.put(type, address);
        }
      }
    }
    StringBuilder hdr = new StringBuilder();

    Enumeration<?> aa = msg.getAllHeaderLines();

    if (aa != null) {
      while (aa.hasMoreElements()) {
        hdr.append(aa.nextElement()).append("\r\n");
      }
    }
    header = hdr.toString();
  }

  public DateTime getDate() {
    return date;
  }

  public String getHeader() {
    return header;
  }

  public String getMessageId() {
    return messageId;
  }

  public Multimap<RecipientType, Address> getRecipients() {
    return recipients;
  }

  public Address getSender() {
    return sender;
  }

  public String getSubject() {
    return subject;
  }
}
