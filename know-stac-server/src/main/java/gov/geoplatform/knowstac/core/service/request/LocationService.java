package gov.geoplatform.knowstac.core.service.request;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.knowstac.core.model.LocationResult;
import gov.geoplatform.knowstac.core.service.business.LocationBusinessServiceIF;
import net.geoprism.registry.model.ServerOrganization;

@Service
public class LocationService implements LocationServiceIF
{
  @Autowired
  private LocationBusinessServiceIF service;

  @Override
  @Request(RequestType.SESSION)
  public List<LocationResult> search(String sessionId, String synchronizationId, String text)
  {
    return this.service.search(synchronizationId, text);
  }

  @Override
  @Request(RequestType.SESSION)
  public LocationResult get(String sessionId, String synchronizationId, String uuid)
  {
    return this.service.get(synchronizationId, uuid);
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonObject getChildren(String sessionId, String synchronizationId, String uuid, Integer pageSize, Integer pageNumber)
  {
    LocationResult parent = this.service.get(synchronizationId, uuid);

    return this.service.getChildren(synchronizationId, parent, pageSize, pageNumber).toJSON();
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonObject getAncestorTree(String sessionId, String synchronizationId, String rootUuid, String uuid, Integer pageSize)
  {
    LocationResult child = this.service.get(synchronizationId, uuid);

    return this.service.getAncestorTree(synchronizationId, child, rootUuid, pageSize).toJSON();
  }

}
