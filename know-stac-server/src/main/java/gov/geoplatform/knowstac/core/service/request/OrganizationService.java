package gov.geoplatform.knowstac.core.service.request;

import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.knowstac.core.service.business.OrganizationBusinessService;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.request.CacheProviderIF;
import net.geoprism.registry.service.request.OrganizationServiceIF;

@Service(value = "ksOrganizationService")
@Primary
public class OrganizationService extends net.geoprism.registry.service.request.OrganizationService implements OrganizationServiceIF
{
  @Autowired
  private OrganizationBusinessService service;

  @Autowired
  private CacheProviderIF             provider;

  @Request(RequestType.SESSION)
  public List<OrganizationDTO> search(String sessionId, String text)
  {
    List<ServerOrganization> organizations = this.service.search(text);

    return organizations.stream().map(org -> org.toDTO()).collect(Collectors.toList());
  }

  @Request(RequestType.SESSION)
  public OrganizationDTO get(String sessionId, String code)
  {
    Optional<OrganizationDTO> optional = provider.getAdapterCache().getOrganization(code);

    return optional.orElse(null);
  }

}
