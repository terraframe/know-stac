package gov.geoplatform.knowstac.core.service.request;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.knowstac.core.model.OrganizationResult;
import gov.geoplatform.knowstac.core.service.business.OrganizationResultBusinessService;

@Service
public class OrganizationResultService
{
  @Autowired
  private OrganizationResultBusinessService service;

  @Request(RequestType.SESSION)
  public List<OrganizationResult> search(String sessionId, String text)
  {
    return this.service.search(text);
  }

  @Request(RequestType.SESSION)
  public OrganizationResult get(String sessionId, String code)
  {
    return this.service.get(code);
  }

  @Request(RequestType.SESSION)
  public JsonObject getChildren(String sessionId, String code, Integer pageSize, Integer pageNumber)
  {
    OrganizationResult parent = this.service.get(code);

    return this.service.getChildren(parent, pageSize, pageNumber).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject getAncestorTree(String sessionId, String rootCode, String code, Integer pageSize)
  {
    OrganizationResult child = this.service.get(code);

    return this.service.getAncestorTree(child, rootCode, pageSize).toJSON();
  }

}
