/**
 * Copyright © 2016-2021 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.dao.model.sql;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Cammernum {
    @Id
    private String id;

    @Column(nullable = false)
    private int enter;

    @Column(nullable = false)
    private int exit;

    @Column(nullable = false)
    private long createdTime;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String macAddress;

    public Cammernum() {
    }

    public Cammernum(String id, int enter, int exit, long createdTime, String ipAddress, String macAddress) {
        this.id = id;
        this.enter = enter;
        this.exit = exit;
        this.createdTime = createdTime;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getEnter() {
        return enter;
    }

    public void setEnter(int enter) {
        this.enter = enter;
    }

    public int getExit() {
        return exit;
    }

    public void setExit(int exit) {
        this.exit = exit;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
