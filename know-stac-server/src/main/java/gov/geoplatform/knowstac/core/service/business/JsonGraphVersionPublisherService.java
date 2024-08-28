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
package gov.geoplatform.knowstac.core.service.business;

import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.metadata.MdVertex;

import gov.geoplatform.knowstac.LocationTotal;
import gov.geoplatform.knowstac.TotalEdge;
import net.geoprism.registry.service.request.JsonGraphVersionPublisherServiceIF;

@Service(value = "ksJsonGraphVersionPublisherService")
@Primary
public class JsonGraphVersionPublisherService extends net.geoprism.registry.service.request.JsonGraphVersionPublisherService implements JsonGraphVersionPublisherServiceIF
{
  @Override
  @Transaction
  protected VertexObject publish(State state, MdVertex mdVertex, GeoObject geoObject)
  {
    VertexObject vertex = super.publish(state, mdVertex, geoObject);

    // Create the total items node for the corresponding
    Map<String, Object> cache = state.getCache();

    if (!cache.containsKey("total-edge"))
    {
      TotalEdge totalEdge = TotalEdge.get(state.version);
      MdEdgeDAOIF mdEdge = MdEdgeDAO.get(totalEdge.getGraphEdgeOid());

      cache.put("total-edge", mdEdge);
    }

    MdEdgeDAOIF mdEdge = (MdEdgeDAOIF) cache.get("total-edge");

    LocationTotal total = new LocationTotal();
    total.setNumberOfItems(0);
    total.apply();

    vertex.addChild(total, mdEdge).apply();

    return vertex;
  }
}
