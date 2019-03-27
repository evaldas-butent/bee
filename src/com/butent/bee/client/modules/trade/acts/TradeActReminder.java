package com.butent.bee.client.modules.trade.acts;

import com.butent.bee.client.ReminderDialog;
import com.butent.bee.client.widget.Button;

public class TradeActReminder extends ReminderDialog {
    private final Long tradeActID;
    //private final FaLabel reminder;

    TradeActReminder(Long tradeActID) {
        super();
        this.tradeActID = tradeActID;
    }

    @Override
    protected void afterButtonPanelRender(Button create, Button update, Button suspend, Button cancel) {

    }
}
