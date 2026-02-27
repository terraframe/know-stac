package gov.geoplatform.knowstac.core.service.business;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.geoplatform.knowstac.core.model.LocationResult;
import gov.geoplatform.knowstac.core.model.ResultPage;
import gov.geoplatform.knowstac.core.model.TreeNode;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;

@Component
public interface LocationBusinessServiceIF
{

  List<LocationResult> search(String synchronizationId, String text);

  LocationResult get(String synchronizationId, String uid);

  TreeNode<LocationResult> getAncestorTree(String synchronizationId, LocationResult child, String rootUid, Integer pageSize);

  ResultPage<LocationResult> getChildren(String synchronizationId, LocationResult parent, Integer pageSize, Integer pageNumber);

  List<LocationResult> getAncestors(LabeledPropertyGraphTypeVersion version, GeoObjectTypeSnapshot rootType, HierarchyTypeSnapshot hierarchyType, LocationResult child, String uid);

}
