package com.sctjsj.lazyhost.event.btEvent;

import android.content.Context;
import android.content.Intent;

/**
 * Created by mayikang on 17/3/8.
 */

public class BTStateChangeEvent {
    private Context context;
    private Intent intent;

    public BTStateChangeEvent(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }
}
