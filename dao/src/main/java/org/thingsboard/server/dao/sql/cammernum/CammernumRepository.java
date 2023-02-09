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
package org.thingsboard.server.dao.sql.cammernum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dao.model.sql.Cammernum;

public interface CammernumRepository extends JpaRepository<Cammernum, Long> {
    Cammernum findByIpAddressAndMacAddress(String ipAddress, String macAddress);
    Cammernum findByMacAddress(String macAddress);

    @Transactional(timeout = 10)
    @Modifying
    @Query("update Cammernum set enter= ?1, exit=?2 where ipAddress = ?3 and macAddress= ?4 ")
    int modifyByIpAddressAndMacAddress(int enter, int exit, String ipAddress, String macAddress);

    @Transactional(timeout = 10)
    @Modifying
    @Query("update Cammernum set enter= ?1, exit=?2 where macAddress = ?3 ")
    int modifyByMacAddress(int enter, int exit, String macAddress);

}
