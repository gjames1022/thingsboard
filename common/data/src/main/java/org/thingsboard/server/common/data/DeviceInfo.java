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
package org.thingsboard.server.common.data;

import lombok.Data;
import org.thingsboard.server.common.data.id.DeviceId;

@Data
public class DeviceInfo extends Device {

    private String customerTitle;
    private boolean customerIsPublic;
    private String deviceProfileName;
    private boolean deviceStatus=false;
    private String  deviceStatusStr="离线";

    public DeviceInfo() {
        super();
    }

    public DeviceInfo(DeviceId deviceId) {
        super(deviceId);
    }

    public DeviceInfo(Device device, String customerTitle, boolean customerIsPublic, String deviceProfileName,boolean deviceStatus) {
        super(device);
        this.customerTitle = customerTitle;
        this.customerIsPublic = customerIsPublic;
        this.deviceProfileName = deviceProfileName;
        this.deviceStatus = deviceStatus;
        if(deviceStatus){
            this.deviceStatusStr="在线";
        }else{
            this.deviceStatusStr="离线";
        }
    }
}
