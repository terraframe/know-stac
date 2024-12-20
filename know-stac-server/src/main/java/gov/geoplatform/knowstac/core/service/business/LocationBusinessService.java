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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.knowstac.TotalEdge;
import gov.geoplatform.knowstac.core.model.LocationResult;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.model.GraphNode;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.view.Page;

@Service
public class LocationBusinessService implements LocationBusinessServiceIF
{
  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF gTypeService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF hTypeService;

  @Override
  public List<LocationResult> search(String synchronizationId, String text)
  {
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(synchronizationId);
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();

    GeoObjectTypeSnapshot rootType = this.gTypeService.getRoot(version);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT @rid AS rid, oid AS oid, code AS code, uid AS uid, displayLabel.defaultLocale AS label");
    statement.append(" FROM " + rootType.getGraphMdVertex().getDbClassName());
    statement.append(" WHERE displayLabel.defaultLocale.toUpperCase() LIKE :text");
    statement.append(" OR code LIKE :text");
    statement.append(" LIMIT 20");

    GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement.toString());
    query.setParameter("text", "%" + text.toUpperCase() + "%");

    List<Map<String, Object>> results = query.getResults();

    return results.stream().map(map -> LocationResult.build(map)).collect(Collectors.toList());
  }

  @Override
  public LocationResult get(String synchronizationId, String uid)
  {
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(synchronizationId);
    LabeledPropertyGraphType graphType = synchronization.getGraphType();
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();

    GeoObjectTypeSnapshot type = this.gTypeService.getRoot(version);
    HierarchyTypeSnapshot hierarchyType = this.hTypeService.get(version, graphType.getHierarchy());

    return get(version, type, hierarchyType, uid);
  }

  private LocationResult get(LabeledPropertyGraphTypeVersion version, GeoObjectTypeSnapshot type, HierarchyTypeSnapshot hierarchyType, String uid)
  {
    StringBuilder statement = new StringBuilder();

    this.appendSelect(version, hierarchyType, statement);
    statement.append(" FROM " + type.getGraphMdVertex().getDbClassName());
    statement.append(" WHERE uid = :uid");

    GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement.toString());
    query.setParameter("uid", uid);

    return LocationResult.build(query.getSingleResult());
  }

  private void appendSelect(LabeledPropertyGraphTypeVersion version, HierarchyTypeSnapshot hierarchyType, StringBuilder statement)
  {
    MdEdgeDAOIF mdLocationEdge = MdEdgeDAO.get(hierarchyType.getGraphMdEdgeOid());

    TotalEdge totalEdge = TotalEdge.get(version);
    MdEdgeDAOIF mdTotalEdge = MdEdgeDAO.get(totalEdge.getGraphEdgeOid());

    statement.append("SELECT @rid AS rid,");
    statement.append(" oid AS oid,");
    statement.append(" code AS code,");
    statement.append(" uid AS uid,");
    statement.append(" displayLabel.defaultLocale AS label,");
    statement.append(" out('" + mdLocationEdge.getDBClassName() + "').size() AS size,");
    statement.append(" first(out('" + mdTotalEdge.getDBClassName() + "')).numberOfItems AS items");
  }

  @Override
  public List<LocationResult> getAncestors(LabeledPropertyGraphTypeVersion version, GeoObjectTypeSnapshot rootType, HierarchyTypeSnapshot hierarchyType, LocationResult child, String uid)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.get(hierarchyType.getGraphMdEdgeOid());
    MdVertexDAOIF mdVertex = MdVertexDAO.get(rootType.getGraphMdVertexOid());
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute("uid");

    GraphQuery<Map<String, Object>> query = null;

    if (uid != null && uid.length() > 0)
    {
      StringBuilder statement = new StringBuilder();
      this.appendSelect(version, hierarchyType, statement);
      statement.append(" FROM (");
      statement.append(" SELECT expand($res)");
      statement.append("  LET $a = (TRAVERSE in(\"" + mdEdge.getDBClassName() + "\") FROM :rid WHILE (" + mdAttribute.getColumnName() + " != :uid))");
      statement.append(" , $b = (SELECT FROM " + mdVertex.getDBClassName() + " WHERE " + mdAttribute.getColumnName() + " = :uid)");
      statement.append(" , $res = (UNIONALL($a,$b))");
      statement.append(")");

      query = new GraphQuery<Map<String, Object>>(statement.toString());
      query.setParameter("rid", child.getRid());
      query.setParameter("uid", uid);
    }
    else
    {
      StringBuilder statement = new StringBuilder();
      this.appendSelect(version, hierarchyType, statement);
      statement.append(" FROM (");
      statement.append("  TRAVERSE in(\"" + mdEdge.getDBClassName() + "\") FROM :rid");
      statement.append(")");

      query = new GraphQuery<Map<String, Object>>(statement.toString());
      query.setParameter("rid", child.getRid());
    }

    List<Map<String, Object>> results = query.getResults();
    return results.stream().map(map -> LocationResult.build(map)).collect(Collectors.toList());
  }

  public Integer getCount(HierarchyTypeSnapshot hierarchyType, LocationResult child)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.get(hierarchyType.getGraphMdEdgeOid());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT out('" + mdEdge.getDBClassName() + "').size()");
    statement.append(" FROM :rid");

    GraphQuery<Integer> query = new GraphQuery<Integer>(statement.toString());
    query.setParameter("rid", child.getRid());

    return query.getSingleResult();
  }

  @Override
  public GraphNode<LocationResult> getAncestorTree(String synchronizationId, LocationResult child, String rootUid, Integer pageSize)
  {
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(synchronizationId);
    LabeledPropertyGraphType graphType = synchronization.getGraphType();
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();

    GeoObjectTypeSnapshot rootType = this.gTypeService.getRoot(version);
    HierarchyTypeSnapshot hierarchyType = this.hTypeService.get(version, graphType.getHierarchy());

    List<LocationResult> ancestors = this.getAncestors(version, rootType, hierarchyType, child, rootUid);
    Integer count = this.getCount(hierarchyType, child);

    GraphNode<LocationResult> prev = null;

    for (LocationResult ancestor : ancestors)
    {
      List<LocationResult> results = this.getChildren(version, hierarchyType, ancestor, pageSize, 1);

      List<GraphNode<LocationResult>> transform = results.stream().map(r -> {
        return new GraphNode<LocationResult>(r);
      }).collect(Collectors.toList());

      if (prev != null)
      {
        int index = transform.indexOf(prev);

        if (index != -1)
        {
          transform.set(index, prev);
        }
        else
        {
          transform.add(prev);
        }
      }

      Page<GraphNode<LocationResult>> page = new Page<GraphNode<LocationResult>>(count, 1, pageSize, transform);

      GraphNode<LocationResult> node = new GraphNode<LocationResult>();
      node.setObject(ancestor);
      node.setChildren(page);

      prev = node;
    }

    return prev;
  }

  @Override
  public Page<LocationResult> getChildren(String synchronizationId, LocationResult parent, Integer pageSize, Integer pageNumber)
  {
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(synchronizationId);
    LabeledPropertyGraphType graphType = synchronization.getGraphType();
    String hierarchy = graphType.getHierarchy();

    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();

    GeoObjectTypeSnapshot rootType = this.gTypeService.getRoot(version);
    HierarchyTypeSnapshot hierarchyType = this.hTypeService.get(version, hierarchy);

    if (parent != null)
    {
      Integer count = this.getCount(hierarchyType, parent);

      List<LocationResult> children = this.getChildren(version, hierarchyType, parent, pageSize, pageNumber);

      return new Page<LocationResult>(count, pageNumber, pageSize, children);
    }

    List<LocationResult> roots = this.getRoots(version, rootType, hierarchyType);

    return new Page<LocationResult>(roots.size(), pageNumber, pageSize, roots);
  }

  private List<LocationResult> getRoots(LabeledPropertyGraphTypeVersion version, GeoObjectTypeSnapshot rootType, HierarchyTypeSnapshot hierarchyType)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.get(hierarchyType.getGraphMdEdgeOid());
    MdVertexDAOIF mdVertex = MdVertexDAO.get(rootType.getGraphMdVertexOid());

    StringBuilder statement = new StringBuilder();
    this.appendSelect(version, hierarchyType, statement);
    statement.append(" FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE IN('" + mdEdge.getDBClassName() + "').size() = :size");

    GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement.toString());
    query.setParameter("size", 0);

    return query.getResults().stream().map(map -> LocationResult.build(map)).collect(Collectors.toList());
  }

  public List<LocationResult> getChildren(LabeledPropertyGraphTypeVersion version, HierarchyTypeSnapshot hierarchyType, LocationResult parent, Integer pageSize, Integer pageNumber)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.get(hierarchyType.getGraphMdEdgeOid());

    StringBuilder statement = new StringBuilder();
    this.appendSelect(version, hierarchyType, statement);
    statement.append(" FROM (");
    statement.append("  SELECT EXPAND(out('" + mdEdge.getDBClassName() + "')) FROM :rid");
    statement.append("  ORDER BY displayLabel.defaultLocale");

    if (pageSize != null && pageNumber != null)
    {
      int first = pageSize * ( pageNumber - 1 );
      int rows = pageSize;

      statement.append(" SKIP " + first + " LIMIT " + rows);
    }

    statement.append(")");

    GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement.toString());
    query.setParameter("rid", parent.getRid());

    return query.getResults().stream().map(map -> LocationResult.build(map)).collect(Collectors.toList());
  }
}
