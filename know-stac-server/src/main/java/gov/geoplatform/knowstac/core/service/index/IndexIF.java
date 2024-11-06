/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.knowstac.core.service.index;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import gov.geoplatform.knowstac.core.model.QueryCriteria;
import gov.geoplatform.knowstac.core.model.StacItem;

@Component
public interface IndexIF
{
  public boolean startup();

  public void shutdown();

  public void createIndex();

  public void put(StacItem item);

  public void removeStacItem(String id);

  public void clear();

  public List<StacItem> getItems(QueryCriteria criteria);

  public Optional<StacItem> getItem(String id);

  public List<StacItem> getItems(Map<String, String> params);

  public Set<String> values(String field, String value);
}
