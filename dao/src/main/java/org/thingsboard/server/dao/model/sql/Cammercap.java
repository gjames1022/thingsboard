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
public class Cammercap {
    @Id
    private String id;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String mask;

    @Column(nullable = false)
    private String glass;

    @Column(nullable = false)
    private String hat;

    @Column(nullable = false)
    private String beard;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String macAddress;

    @Column(nullable = false)
    private Long eventTime;

    @Column(nullable = false)
    private Long createdTime;

    @Column(nullable = false)
    private String isSend;

    @Column(nullable = false)
    private String eventType;

    public Cammercap() {
    }

    public Cammercap(String id, String gender, int age, String mask, String glass, String hat, String beard, String ipAddress, String macAddress, Long eventTime, Long createdTime, String isSend, String eventType) {
        this.id = id;
        this.gender = gender;
        this.age = age;
        this.mask = mask;
        this.glass = glass;
        this.hat = hat;
        this.beard = beard;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.eventTime = eventTime;
        this.createdTime = createdTime;
        this.isSend = isSend;
        this.eventType = eventType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getGlass() {
        return glass;
    }

    public void setGlass(String glass) {
        this.glass = glass;
    }

    public String getHat() {
        return hat;
    }

    public void setHat(String hat) {
        this.hat = hat;
    }

    public String getBeard() {
        return beard;
    }

    public void setBeard(String beard) {
        this.beard = beard;
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

    public Long getEventTime() {
        return eventTime;
    }

    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public String getIsSend() {
        return isSend;
    }

    public void setIsSend(String isSend) {
        this.isSend = isSend;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
