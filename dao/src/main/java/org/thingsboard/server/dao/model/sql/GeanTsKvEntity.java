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

import org.thingsboard.server.common.data.geantskv.TsKv;

import org.thingsboard.server.dao.model.ToData;
import org.thingsboard.server.dao.model.sqlts.ts.TsKvCompositeKey;


import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

import static org.thingsboard.server.dao.model.ModelConstants.*;
import static org.thingsboard.server.dao.model.ModelConstants.JSON_VALUE_COLUMN;

@Data
@Entity
@Table(name = "ts_kv")
@IdClass(TsKvCompositeKey.class)
public class GeanTsKvEntity implements ToData<TsKv>{

    @Id
    @Column(name = ENTITY_ID_COLUMN, columnDefinition = "uuid")
    protected UUID entityId;

    @Id
    @Column(name = KEY_COLUMN)
    protected int key;

    @Id
    @Column(name = TS_COLUMN)
    protected Long ts;

    @Column(name = BOOLEAN_VALUE_COLUMN)
    protected Boolean booleanValue;

    @Column(name = STRING_VALUE_COLUMN)
    protected String strValue;

    @Column(name = LONG_VALUE_COLUMN)
    protected Long longValue;

    @Column(name = DOUBLE_VALUE_COLUMN)
    protected Double doubleValue;

    @Column(name = JSON_VALUE_COLUMN)
    protected String jsonValue;

    public GeanTsKvEntity(){super();}

    public GeanTsKvEntity(TsKv tsKv){
        this.entityId = tsKv.getEntityId();
        this.key = tsKv.getKey();
        this.ts = tsKv.getTs();
        this.booleanValue = tsKv.getBooleanValue();
        this.strValue = tsKv.getStrValue();
        this.longValue = tsKv.getLongValue();
        this.doubleValue = tsKv.getDoubleValue();
        this.jsonValue = tsKv.getJsonValue();
    }

    @Override
    public TsKv toData() {
        TsKv tsKv = new TsKv();
        tsKv.setEntityId(this.getEntityId());
        tsKv.setKey(this.getKey());
        tsKv.setTs(this.getTs());
        tsKv.setBooleanValue(this.getBooleanValue());
        tsKv.setStrValue(this.getStrValue());
        tsKv.setLongValue(this.getLongValue());
        tsKv.setJsonValue(this.getJsonValue());
        return tsKv;
    }
}
