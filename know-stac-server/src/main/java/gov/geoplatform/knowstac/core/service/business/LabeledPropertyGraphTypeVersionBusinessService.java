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

import java.util.HashMap;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdGraphClassQuery;
import com.runwaysdk.system.metadata.MdVertex;

import gov.geoplatform.knowstac.ItemTotal;
import gov.geoplatform.knowstac.TotalEdge;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeEntry;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeVersionBusinessServiceIF;

@Service(value = "ksLabeledPropertyGraphTypeVersionBusinessService")
@Primary
public class LabeledPropertyGraphTypeVersionBusinessService extends net.geoprism.registry.service.business.LabeledPropertyGraphTypeVersionBusinessService implements LabeledPropertyGraphTypeVersionBusinessServiceIF
{
  public static final String                     PREFIX = "ha_";

  public static final String                     SPLIT  = "__";

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF gTypeService;

  @Override
  @Transaction
  public void delete(LabeledPropertyGraphTypeVersion version)
  {
    TotalEdge edge = TotalEdge.get(version);

    if (edge != null)
    {
      edge.delete();
    }

    super.delete(version);
  }

  @Override
  public void truncate(LabeledPropertyGraphTypeVersion version)
  {
    super.truncate(version);

    // TODO delete all the orphaned item nodes
//    GraphDBService service = GraphDBService.getInstance();
//
//    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ItemTotal.CLASS);
//
//    GraphRequest request = service.getGraphDBRequest();
//    service.command(request, "DELETE VERTEX FROM " + mdVertex.getDBClassName(), new HashMap<>());
  }

  @Override
  @Transaction
  public LabeledPropertyGraphTypeVersion create(LabeledPropertyGraphTypeEntry entry, JsonObject json)
  {
    LabeledPropertyGraphTypeVersion version = super.create(entry, json);

    if (TotalEdge.get(version) == null)
    {
      MdVertex root = this.getRootType(version).getGraphMdVertex();
      MdVertexDAOIF site = MdVertexDAO.getMdVertexDAO(ItemTotal.CLASS);
      LabeledPropertyGraphType type = version.getGraphType();

      String code = type.getCode();
      String viewName = getTableName(code);

      LocalizedValue label = LocalizedValueConverter.convertNoAutoCoalesce(type.getDisplayLabel());

      MdEdgeDAO mdEdgeDAO = MdEdgeDAO.newInstance();
      mdEdgeDAO.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.UNIVERSAL_GRAPH_PACKAGE);
      mdEdgeDAO.setValue(MdEdgeInfo.NAME, viewName);
      mdEdgeDAO.setValue(MdEdgeInfo.DB_CLASS_NAME, viewName);
      mdEdgeDAO.setValue(MdEdgeInfo.PARENT_MD_VERTEX, root.getOid());
      mdEdgeDAO.setValue(MdEdgeInfo.CHILD_MD_VERTEX, site.getOid());
      LocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DISPLAY_LABEL, label);
      mdEdgeDAO.setValue(MdEdgeInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
      mdEdgeDAO.apply();

      this.assignEdgePermissions(mdEdgeDAO);

      TotalEdge edge = new TotalEdge();
      edge.setVersion(version);
      edge.setGraphEdgeId(mdEdgeDAO.getOid());
      edge.apply();
    }

    return version;
  }

  private void assignEdgePermissions(ComponentIF component)
  {
    RoleDAO adminRole = RoleDAO.findRole(RoleConstants.ADMIN).getBusinessDAO();
    adminRole.grantPermission(Operation.CREATE, component.getOid());
    adminRole.grantPermission(Operation.DELETE, component.getOid());
    adminRole.grantPermission(Operation.WRITE, component.getOid());
    adminRole.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO publicRole = RoleDAO.get(RoleDAO.PUBLIC_ROLE_ID).getBusinessDAO();
    publicRole.grantPermission(Operation.READ, component.getOid());
    publicRole.grantPermission(Operation.READ_ALL, component.getOid());
    publicRole.grantPermission(Operation.READ_CHILD, component.getOid());
    publicRole.grantPermission(Operation.READ_PARENT, component.getOid());
  }

  public String getTableName(String className)
  {
    int count = 0;

    String name = PREFIX + count + SPLIT + className;

    if (name.length() > 25)
    {
      name = name.substring(0, 25);
    }

    while (isTableNameInUse(name))
    {
      count++;

      name = PREFIX + count + className;

      if (name.length() > 25)
      {
        name = name.substring(0, 25);
      }
    }

    return name;
  }

  private boolean isTableNameInUse(String name)
  {
    MdGraphClassQuery query = new MdGraphClassQuery(new QueryFactory());
    query.WHERE(query.getDbClassName().EQ(name));

    return query.getCount() > 0;
  }
}
