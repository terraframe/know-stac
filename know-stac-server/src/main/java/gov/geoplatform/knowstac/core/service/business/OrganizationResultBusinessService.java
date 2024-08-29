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

import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.knowstac.ItemTotal;
import gov.geoplatform.knowstac.core.model.OrganizationResult;
import net.geoprism.registry.graph.GraphOrganization;
import net.geoprism.registry.model.GraphNode;
import net.geoprism.registry.view.Page;

@Service
public class OrganizationResultBusinessService
{
  public List<OrganizationResult> search(String text)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(GraphOrganization.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT @rid AS rid, oid AS oid, code AS code, uuid AS uuid, displayLabel.defaultLocale AS label");
    statement.append(" FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE displayLabel.defaultLocale.toUpperCase() LIKE :text");
    statement.append(" OR code LIKE :text");
    statement.append(" LIMIT 20");

    GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement.toString());
    query.setParameter("text", "%" + text.toUpperCase() + "%");

    List<Map<String, Object>> results = query.getResults();

    return results.stream().map(map -> OrganizationResult.build(map)).collect(Collectors.toList());
  }

  public OrganizationResult get(String code)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(GraphOrganization.CLASS);

    StringBuilder statement = new StringBuilder();

    this.appendSelect(statement);
    statement.append(" FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE code = :code");

    GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement.toString());
    query.setParameter("code", code);

    return OrganizationResult.build(query.getSingleResult());
  }

  private void appendSelect(StringBuilder statement)
  {
    MdEdgeDAOIF mdLocationEdge = MdEdgeDAO.getMdEdgeDAO(GraphOrganization.EDGE_CLASS);
    MdEdgeDAOIF mdTotalEdge = MdEdgeDAO.getMdEdgeDAO(ItemTotal.ORGANIZATION_HAS_TOTAL);

    statement.append("SELECT @rid AS rid");
    statement.append(", oid AS oid");
    statement.append(", code AS code");
    statement.append(", code AS code");
    statement.append(", displayLabel.defaultLocale AS label");
    statement.append(", out('" + mdLocationEdge.getDBClassName() + "').size() AS size");
    statement.append(", first(out('" + mdTotalEdge.getDBClassName() + "')).numberOfItems AS items");
  }

  public List<OrganizationResult> getAncestors(OrganizationResult child, String code)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GraphOrganization.EDGE_CLASS);
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(GraphOrganization.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute("code");

    GraphQuery<Map<String, Object>> query = null;

    if (code != null && code.length() > 0)
    {
      StringBuilder statement = new StringBuilder();
      this.appendSelect(statement);
      statement.append(" FROM (");
      statement.append(" SELECT expand($res)");
      statement.append("  LET $a = (TRAVERSE in(\"" + mdEdge.getDBClassName() + "\") FROM :rid WHILE (" + mdAttribute.getColumnName() + " != :code))");
      statement.append(" , $b = (SELECT FROM " + mdVertex.getDBClassName() + " WHERE " + mdAttribute.getColumnName() + " = :code)");
      statement.append(" , $res = (UNIONALL($a,$b))");
      statement.append(")");

      query = new GraphQuery<Map<String, Object>>(statement.toString());
      query.setParameter("rid", child.getRid());
      query.setParameter("code", code);
    }
    else
    {
      StringBuilder statement = new StringBuilder();
      this.appendSelect(statement);
      statement.append(" FROM (");
      statement.append("  TRAVERSE in(\"" + mdEdge.getDBClassName() + "\") FROM :rid");
      statement.append(")");

      query = new GraphQuery<Map<String, Object>>(statement.toString());
      query.setParameter("rid", child.getRid());
    }

    List<Map<String, Object>> results = query.getResults();
    return results.stream().map(map -> OrganizationResult.build(map)).collect(Collectors.toList());
  }

  public Integer getCount(OrganizationResult child)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GraphOrganization.EDGE_CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT out('" + mdEdge.getDBClassName() + "').size()");
    statement.append(" FROM :rid");

    GraphQuery<Integer> query = new GraphQuery<Integer>(statement.toString());
    query.setParameter("rid", child.getRid());

    return query.getSingleResult();
  }

  public GraphNode<OrganizationResult> getAncestorTree(OrganizationResult child, String rootCode, Integer pageSize)
  {
    List<OrganizationResult> ancestors = this.getAncestors(child, rootCode);
    Integer count = this.getCount(child);

    GraphNode<OrganizationResult> prev = null;

    for (OrganizationResult ancestor : ancestors)
    {
      List<OrganizationResult> results = this.getDirectChildren(ancestor, pageSize, 1);

      List<GraphNode<OrganizationResult>> transform = results.stream().map(r -> {
        return new GraphNode<OrganizationResult>(r);
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

      Page<GraphNode<OrganizationResult>> page = new Page<GraphNode<OrganizationResult>>(count, 1, pageSize, transform);

      GraphNode<OrganizationResult> node = new GraphNode<OrganizationResult>();
      node.setObject(ancestor);
      node.setChildren(page);

      prev = node;
    }

    return prev;
  }

  public Page<OrganizationResult> getChildren(OrganizationResult parent, Integer pageSize, Integer pageNumber)
  {

    if (parent != null)
    {
      Integer count = this.getCount(parent);

      List<OrganizationResult> children = this.getDirectChildren(parent, pageSize, pageNumber);

      return new Page<OrganizationResult>(count, pageNumber, pageSize, children);
    }

    List<OrganizationResult> roots = this.getRoots();

    return new Page<OrganizationResult>(roots.size(), pageNumber, pageSize, roots);
  }

  private List<OrganizationResult> getRoots()
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GraphOrganization.EDGE_CLASS);
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(GraphOrganization.CLASS);

    StringBuilder statement = new StringBuilder();
    this.appendSelect(statement);
    statement.append(" FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE IN('" + mdEdge.getDBClassName() + "').size() = :size");

    GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement.toString());
    query.setParameter("size", 0);

    return query.getResults().stream().map(map -> OrganizationResult.build(map)).collect(Collectors.toList());
  }

  public List<OrganizationResult> getDirectChildren(OrganizationResult parent, Integer pageSize, Integer pageNumber)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GraphOrganization.EDGE_CLASS);

    StringBuilder statement = new StringBuilder();
    this.appendSelect(statement);
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

    return query.getResults().stream().map(map -> OrganizationResult.build(map)).collect(Collectors.toList());
  }

}
