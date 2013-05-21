package org.odk.collect.android.services;

import org.odk.collect.android.services.ITMServiceCallback;
/**
 * Created by hoang on 5/20/13.
 */
interface ITMService {
    void registerCallback(ITMServiceCallback callback);
    void unregisterCallback(ITMServiceCallback callback);
    void setTrigger(String qid);
}
