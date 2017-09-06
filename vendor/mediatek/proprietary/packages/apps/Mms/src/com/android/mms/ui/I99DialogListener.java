package com.android.mms.ui;

import android.content.Intent;

public interface I99DialogListener {
	void getI99DialogListItemId(int position, MessageItem messageItem, String number);
	void doI99DialogAction(MessageItem message);
}
