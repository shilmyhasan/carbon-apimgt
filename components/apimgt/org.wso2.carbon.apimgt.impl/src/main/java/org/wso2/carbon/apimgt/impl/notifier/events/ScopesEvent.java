/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.ArrayList;

/**
 * This class is used to notify the scopes related events as a consolidated event.
 */
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
