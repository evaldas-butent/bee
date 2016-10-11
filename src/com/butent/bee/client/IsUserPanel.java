package com.butent.bee.client;

import com.google.gwt.user.client.ui.IsWidget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.communication.Presence;
import com.butent.bee.shared.data.UserData;

public interface IsUserPanel extends IsWidget {

  IsWidget create();

  String getName();

  void updateUserData(UserData userData);

  Flow getLogoutContainer();

  Flow getPhotoContainer();

  Flow getPresenceContainer();

  Flow getUserSignatureContainer();

  Image getUserPhotoImage();

  void updateUserPresence(Presence presence);
}
