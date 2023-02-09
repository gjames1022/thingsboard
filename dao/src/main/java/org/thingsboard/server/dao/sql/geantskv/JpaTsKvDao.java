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
package org.thingsboard.server.dao.sql.geantskv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.geantskv.TsKv;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.TimePageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.geantskv.TsKvDao;
import org.thingsboard.server.dao.model.sql.GeanTsKvEntity;
import org.thingsboard.server.dao.sql.JpaAbstractDao;

import java.util.List;
import java.util.UUID;

@Component
public class JpaTsKvDao  implements TsKvDao {

    @Autowired
    private GeanTsKvRepository geanTsKvRepository;

    @Override
    public List<TsKv> findTelemeterByEntityId(EntityId entityId, TimePageLink pageLink) {
        return DaoUtil.convertDataList( geanTsKvRepository
                   .findTelemeterByEntityId(
                            entityId.getId()));
    }
}
