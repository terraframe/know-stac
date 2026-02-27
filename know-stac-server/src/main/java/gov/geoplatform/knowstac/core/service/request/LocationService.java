package gov.geoplatform.knowstac.core.service.request;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.knowstac.core.model.LocationResult;
import gov.geoplatform.knowstac.core.model.ResultPage;
import gov.geoplatform.knowstac.core.model.TreeNode;
import gov.geoplatform.knowstac.core.service.business.LocationBusinessServiceIF;

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
  public LocationResult get(String sessionId, String synchronizationId, String uid)
  {
    return this.service.get(synchronizationId, uid);
  }

  @Override
  @Request(RequestType.SESSION)
  public ResultPage<LocationResult> getChildren(String sessionId, String synchronizationId, String uid, Integer pageSize, Integer pageNumber)
  {
    LocationResult parent = this.service.get(synchronizationId, uid);

    return this.service.getChildren(synchronizationId, parent, pageSize, pageNumber);
  }

  @Override
  @Request(RequestType.SESSION)
  public TreeNode<LocationResult> getAncestorTree(String sessionId, String synchronizationId, String rootUuid, String uid, Integer pageSize)
  {
    LocationResult child = this.service.get(synchronizationId, uid);

    return this.service.getAncestorTree(synchronizationId, child, rootUuid, pageSize);
  }

}
