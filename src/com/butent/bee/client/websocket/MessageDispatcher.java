package com.butent.bee.client.websocket;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.shared.data.PropertiesData;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.websocket.InfoMessage;
import com.butent.bee.shared.websocket.Message;
import com.butent.bee.shared.websocket.TextMessage;

import java.util.List;

class MessageDispatcher {

  private static BeeLogger logger = LogUtils.getLogger(MessageDispatcher.class);
  
  MessageDispatcher() {
  }
  
  void dispatch(Message message) {
    switch (message.getType()) {
      case INFO:
        String caption = ((InfoMessage) message).getCaption();
        List<Property> info = ((InfoMessage) message).getInfo();
        
        if (BeeUtils.isEmpty(info)) {
          logger.warning(caption, "message is empty");
        } else {
          Global.showGrid(caption, new PropertiesData(info));
        }
        break;

      case TEXT:
        BeeKeeper.getScreen().notifyInfo(((TextMessage) message).getText());
        break;
    }
  }
}
