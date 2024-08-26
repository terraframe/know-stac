package gov.geoplatform.knowstac.core.model;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Provider
{
  // REQUIRED. The name of the organization or the individual.
  @JsonInclude(Include.NON_NULL)
  private String       name;

  // Multi-line description to add further provider information such as
  // processing details for processors and producers, hosting details for hosts
  // or basic contact information. CommonMark 0.29 syntax MAY be used for rich
  // text representation.
  private String       description;

  // Roles of the provider. Any of licensor, producer, processor or host.
  private List<String> roles;

  // Homepage on which the provider describes the dataset and publishes contact
  // information..
  private String       url;

  public Provider()
  {
    this.roles = new LinkedList<>();
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public List<String> getRoles()
  {
    return roles;
  }

  public void setRoles(List<String> roles)
  {
    this.roles = roles;
  }

  public void addRole(String role)
  {
    this.roles.add(role);
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public static Provider build(String name, String description, String url, String... roles)
  {
    Provider link = new Provider();
    link.setName(name);
    link.setDescription(description);
    link.setUrl(url);
    link.addRole(url);

    return link;
  }
}
