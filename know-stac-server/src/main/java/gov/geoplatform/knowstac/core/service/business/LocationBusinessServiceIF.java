package gov.geoplatform.knowstac.core.service.business;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.geoplatform.knowstac.core.model.LocationResult;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.model.GraphNode;
import net.geoprism.registry.view.Page;

@Component
public interface LocationBusinessServiceIF
{

  List<LocationResult> search(String synchronizationId, String text);

  LocationResult get(String synchronizationId, String uuid);

  GraphNode<LocationResult> getAncestorTree(String synchronizationId, LocationResult child, String rootUuid, Integer pageSize);

  Page<LocationResult> getChildren(String synchronizationId, LocationResult parent, Integer pageSize, Integer pageNumber);

  List<LocationResult> getAncestors(LabeledPropertyGraphTypeVersion version, GeoObjectTypeSnapshot rootType, HierarchyTypeSnapshot hierarchyType, LocationResult child, String uuid);

}
