package gov.geoplatform.knowstac.core.model;

import java.util.List;

/*
 * The STAC Collection Specification defines a set of common fields to describe
 * a group of Items that share properties and metadata. The Collection
 * Specification shares all fields with the STAC Catalog Specification (with
 * different allowed values for type and stac_extensions) and adds fields to
 * describe the whole dataset and the included set of Items. Collections can
 * have both parent Catalogs and Collections and child Items, Catalogs and
 * Collections.
 * 
 * A STAC Collection is represented in JSON format. Any JSON object that
 * contains all the required fields is a valid STAC Collection and also a valid
 * STAC Catalog.
 * 
 * STAC Collections are compatible with the Collection JSON specified in OGC API
 * - Features, but they are extended with additional fields.
 */
public class StacCatalog
{

  // string REQUIRED. Set to Catalog if this Catalog only implements the Catalog
  // spec.
  private String         type;

  // string REQUIRED. The STAC version the Catalog implements.
  private String         stac_version;

  // [string] A list of extension identifiers the Catalog implements.
  private List<String>   stac_extensions;

  // string REQUIRED. Identifier for the Catalog.
  private String         id;

  // string A short descriptive one-line title for the Catalog.
  private String         title;

  // string REQUIRED. Detailed multi-line description to fully explain the
  // Catalog. CommonMark 0.29 syntax MAY be used for rich text representation.
  private String         description;

  // [Link Object] REQUIRED. A list of references to other documents.
  private List<StacLink> links;

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getStac_version()
  {
    return stac_version;
  }

  public void setStac_version(String stac_version)
  {
    this.stac_version = stac_version;
  }

  public List<String> getStac_extensions()
  {
    return stac_extensions;
  }

  public void setStac_extensions(List<String> stac_extensions)
  {
    this.stac_extensions = stac_extensions;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public List<StacLink> getLinks()
  {
    return links;
  }

  public void setLinks(List<StacLink> links)
  {
    this.links = links;
  }

}
