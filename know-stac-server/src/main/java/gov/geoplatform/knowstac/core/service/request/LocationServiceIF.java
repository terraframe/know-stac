package gov.geoplatform.knowstac.core.service.request;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import gov.geoplatform.knowstac.core.model.LocationResult;

@Component
public interface LocationServiceIF
{

  List<LocationResult> search(String sessionId, String synchronizationId, String text);

  LocationResult get(String sessionId, String synchronizationId, String uid);

  JsonObject getChildren(String sessionId, String synchronizationId, String uid, Integer pageSize, Integer pageNumber);

  JsonObject getAncestorTree(String sessionId, String synchronizationId, String rootUuid, String uid, Integer pageSize);

}
