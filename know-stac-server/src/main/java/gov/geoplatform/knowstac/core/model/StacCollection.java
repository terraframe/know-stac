package gov.geoplatform.knowstac.core.model;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class StacCollection
{

  // REQUIRED. Must be set to Collection to be a valid Collection.
  private String         type;

  // REQUIRED. The STAC version the Collection implements.
  @JsonProperty("stac_version")
  private String         stacVersion;

  // A list of extension identifiers the Collection implements.
  @JsonProperty("stac_extensions")
  private List<String>   stacExtensions;

  // string REQUIRED. Identifier for the Catalog.
  private String         id;

  // string A short descriptive one-line title for the Catalog.
  private String         title;

  // string REQUIRED. Detailed multi-line description to fully explain the
  // Catalog. CommonMark 0.29 syntax MAY be used for rich text representation.
  private String         description;

  // List of keywords describing the Collection.
  private List<String>   keywords;

  // REQUIRED License(s) of the data collection as SPDX License identifier, SPDX
  // License expression, or other (see below).
  private String         license;

  // A list of providers, which may include all organizations capturing or
  // processing the data or the hosting provider.
  private List<Provider> providers;

  // REQUIRED. Spatial and temporal extents.
  private Extent         extent;

  // [Link Object] REQUIRED. A list of references to other documents.
  private List<StacLink> links;

  public StacCollection()
  {
    this.type = "Feature";
    this.stacVersion = "1.0.0";
    this.stacExtensions = new LinkedList<String>();
    this.links = new LinkedList<StacLink>();
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getStacVersion()
  {
    return stacVersion;
  }

  public void setStacVersion(String stacVersion)
  {
    this.stacVersion = stacVersion;
  }

  public List<String> getStacExtensions()
  {
    return stacExtensions;
  }

  public void setStacExtensions(List<String> stacExtensions)
  {
    this.stacExtensions = stacExtensions;
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

  public List<String> getKeywords()
  {
    return keywords;
  }

  public void setKeywords(List<String> keywords)
  {
    this.keywords = keywords;
  }

  public String getLicense()
  {
    return license;
  }

  public void setLicense(String license)
  {
    this.license = license;
  }

  public List<Provider> getProviders()
  {
    return providers;
  }

  public void setProviders(List<Provider> providers)
  {
    this.providers = providers;
  }

  public Extent getExtent()
  {
    return extent;
  }

  public void setExtent(Extent extent)
  {
    this.extent = extent;
  }

  public List<StacLink> getLinks()
  {
    return links;
  }

  public void setLinks(List<StacLink> links)
  {
    this.links = links;
  }

  public void addLink(StacLink link)
  {
    this.links.add(link);
  }

}
