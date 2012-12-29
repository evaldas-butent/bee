package com.butent.bee.server.modules.mail;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.modules.mail.MailConstants.AddressType;
import com.butent.bee.shared.modules.mail.MailConstants.MessageFlag;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Enumeration;

import javax.mail.Address;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class MailEnvelope {

  private static BiMap<Flag, MessageFlag> flags = HashBiMap
      .create(ImmutableMap.of(Flag.ANSWERED, MessageFlag.ANSWERED,
          Flag.DELETED, MessageFlag.DELETED, Flag.FLAGGED, MessageFlag.FLAGGED,
          Flag.SEEN, MessageFlag.SEEN, Flag.USER, MessageFlag.USER));

  public static Flag getFlag(MessageFlag flag) {
    return flags.inverse().get(flag);
  }

  public static Integer getFlagMask(Message message) throws MessagingException {
    Assert.notNull(message);
    Flag[] systemFlags = message.getFlags().getSystemFlags();

    if (ArrayUtils.length(systemFlags) == 0) {
      return null;
    }
    int mask = 0;

    for (Flag flag : systemFlags) {
      if (flags.containsKey(flag)) {
        mask |= BeeUtils.unbox(flags.get(flag).getMask());
      }
    }
    return mask;
  }

  private final String messageId;
  private final DateTime date;
  private final Address sender;
  private final String subject;
  private final Multimap<AddressType, Address> recipients = HashMultimap.create();
  private final String header;
  private final Integer flagMask;

  private final String uniqueId;

  public MailEnvelope(Message message) throws MessagingException {
    Assert.state(message instanceof MimeMessage,
        "Unknown message type: " + message.getClass().getName());

    MimeMessage msg = (MimeMessage) message;
    messageId = msg.getMessageID();
    date = new DateTime(msg.getSentDate());
    sender = ArrayUtils.getQuietly(msg.getFrom(), 0);
    subject = msg.getSubject();

    for (RecipientType type : new RecipientType[] {
        RecipientType.TO, RecipientType.CC, RecipientType.BCC}) {

      Address[] addresses = msg.getRecipients(type);

      if (addresses != null) {
        for (Address address : addresses) {
          recipients.put(NameUtils.getEnumByName(AddressType.class, type.toString()), address);
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
    flagMask = getFlagMask(message);
    uniqueId = Codec.md5(BeeUtils.joinWords(messageId, date, sender, subject));
  }

  public DateTime getDate() {
    return date;
  }

  public Integer getFlagMask() {
    return flagMask;
  }

  public String getHeader() {
    return header;
  }

  public String getMessageId() {
    return messageId;
  }

  public Multimap<AddressType, Address> getRecipients() {
    return recipients;
  }

  public Address getSender() {
    return sender;
  }

  public String getSubject() {
    return subject;
  }

  public String getUniqueId() {
    return uniqueId;
  }
}
