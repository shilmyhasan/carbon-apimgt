package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.ArrayList;

public class ScopesEvent extends Event {

    private ArrayList<ScopeEvent> scopeEvents = new ArrayList<>();

    public ScopesEvent(String eventId, long timestamp, String type, int tenantId, String tenantDomain) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
    }

    public void addScopeEvent(ScopeEvent scopeEvent) {
        scopeEvents.add(scopeEvent);
    }

    public ArrayList<ScopeEvent> getScopeEvents() {
        return scopeEvents;
    }
}
