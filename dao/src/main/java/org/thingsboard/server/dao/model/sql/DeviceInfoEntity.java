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

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.server.common.data.DeviceInfo;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceInfoEntity extends AbstractDeviceEntity<DeviceInfo> {

    public static final Map<String,String> deviceInfoColumnMap = new HashMap<>();
    static {
        deviceInfoColumnMap.put("customerTitle", "c.title");
        deviceInfoColumnMap.put("deviceProfileName", "p.name");
        deviceInfoColumnMap.put("deviceStatus", "g.booleanValue");
        deviceInfoColumnMap.put("deviceStatusStr", "离线");
    }

    private String customerTitle;
    private boolean customerIsPublic;
    private String deviceProfileName;
    private boolean deviceStatus=false;//设备状态
    private String  deviceStatusStr="离线";

    public DeviceInfoEntity() {
        super();
    }

    public DeviceInfoEntity(DeviceEntity deviceEntity,
                            String customerTitle,
                            Object customerAdditionalInfo,
                            String deviceProfileName,
                            boolean deviceStatus) {
        super(deviceEntity);
        this.customerTitle = customerTitle;
        if (customerAdditionalInfo != null && ((JsonNode)customerAdditionalInfo).has("isPublic")) {
            this.customerIsPublic = ((JsonNode)customerAdditionalInfo).get("isPublic").asBoolean();
        } else {
            this.customerIsPublic = false;
        }
        this.deviceProfileName = deviceProfileName;
        this.deviceStatus = deviceStatus;
        if(deviceStatus){
            this.deviceStatusStr="在线";
        }else{
            this.deviceStatusStr="离线";
        }
        System.out.println("**********自己获取的设备状态************:"+this.deviceStatus);
    }

    @Override
    public DeviceInfo toData() {
        return new DeviceInfo(super.toDevice(), customerTitle, customerIsPublic, deviceProfileName,deviceStatus);
    }
}
