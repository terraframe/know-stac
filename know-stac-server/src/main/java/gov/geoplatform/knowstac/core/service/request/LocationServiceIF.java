package gov.geoplatform.knowstac.core.service.request;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import gov.geoplatform.knowstac.core.model.LocationResult;
import gov.geoplatform.knowstac.core.model.ResultPage;
import gov.geoplatform.knowstac.core.model.TreeNode;

@Component
public interface LocationServiceIF
{

  List<LocationResult> search(String sessionId, String synchronizationId, String text);

  LocationResult get(String sessionId, String synchronizationId, String uid);

  ResultPage<LocationResult> getChildren(String sessionId, String synchronizationId, String uid, Integer pageSize, Integer pageNumber);

  TreeNode<LocationResult> getAncestorTree(String sessionId, String synchronizationId, String rootUuid, String uid, Integer pageSize);

}
