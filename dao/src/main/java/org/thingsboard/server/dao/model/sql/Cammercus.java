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

import lombok.Data;
import org.hibernate.annotations.TypeDef;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.util.mapping.JsonStringType;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class Cammercus {

 @Id
 private String id;

 @Column(nullable = false)
 private long createdTime;

 @Column(nullable = false)
 private String ipAddress;

 @Column(nullable = false)
 private String macAddress;

 @Column(nullable = false)
 private String direction;

 @Column(nullable = false)
 private Long eventTime;

 @Column(nullable = false)
 private String enter;

 @Column(nullable = false)
 private String exit;

 @Column(nullable = false)
 private String duplicatePeople;

 @Column(nullable = false)
 private String isSend;

 @Column(nullable = false)
 private String eventType;

 public Cammercus() {
 }

 public Cammercus(String id, long createdTime, String ipAddress, String macAddress, String direction, Long eventTime, String enter, String exit, String duplicatePeople, String isSend, String eventType) {
  this.id = id;
  this.createdTime = createdTime;
  this.ipAddress = ipAddress;
  this.macAddress = macAddress;
  this.direction = direction;
  this.eventTime = eventTime;
  this.enter = enter;
  this.exit = exit;
  this.duplicatePeople = duplicatePeople;
  this.isSend = isSend;
  this.eventType = eventType;
 }

 public String getId() {
  return id;
 }

 public void setId(String id) {
  this.id = id;
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

 public String getDirection() {
  return direction;
 }

 public void setDirection(String direction) {
  this.direction = direction;
 }

 public Long getEventTime() {
  return eventTime;
 }

 public void setEventTime(Long eventTime) {
  this.eventTime = eventTime;
 }

 public String getEnter() {
  return enter;
 }

 public void setEnter(String enter) {
  this.enter = enter;
 }

 public String getExit() {
  return exit;
 }

 public void setExit(String exit) {
  this.exit = exit;
 }

 public String getDuplicatePeople() {
  return duplicatePeople;
 }

 public void setDuplicatePeople(String duplicatePeople) {
  this.duplicatePeople = duplicatePeople;
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
