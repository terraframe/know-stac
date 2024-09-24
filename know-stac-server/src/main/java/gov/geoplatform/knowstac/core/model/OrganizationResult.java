package gov.geoplatform.knowstac.core.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationQuery;
import net.geoprism.registry.cache.ServerOrganizationCache;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.view.JsonSerializable;

public class OrganizationResult implements JsonSerializable
{
  @JsonIgnore
  private Object  rid;

  private String  oid;

  private String  code;

  private String  label;

  private Integer size;

  private Integer items;

  public Object getRid()
  {
    return rid;
  }

  public void setRid(Object rid)
  {
    this.rid = rid;
  }

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public Integer getSize()
  {
    return size;
  }

  public void setSize(Integer size)
  {
    this.size = size;
  }

  public Integer getItems()
  {
    return items;
  }

  public void setItems(Integer items)
  {
    this.items = items;
  }

  public static OrganizationResult build(Map<String, Object> map)
  {
    if (map != null)
    {
      OrganizationResult result = new OrganizationResult();
      result.setRid(map.get("rid"));
      result.setOid((String) map.get("oid"));
      result.setCode((String) map.get("code"));
      result.setLabel((String) map.get("label"));

      if (map.containsKey("size"))
      {
        result.setSize((Integer) map.get("size"));
      }

      if (map.containsKey("items"))
      {
        result.setItems((Integer) map.get("items"));
      }
      else
      {
        result.setItems(0);
      }

      return result;
    }

    return null;
  }

  @Override
  public JsonElement toJSON()
  {
    ObjectMapper mapper = new ObjectMapper();

    try
    {
      return JsonParser.parseString(mapper.writeValueAsString(this));
    }
    catch (JsonProcessingException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString()
  {
    return this.code + " - " + this.label;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof OrganizationResult)
    {
      return this.code.equals( ( (OrganizationResult) obj ).code);
    }

    return super.equals(obj);
  }

  @Request
  public static synchronized void populateCache(ServerOrganizationCache cache)
  {
    cache.rebuild();

    try
    {  
      OrganizationQuery oQ = new OrganizationQuery(new QueryFactory()); 

      try (OIterator<? extends Organization> iterator = oQ.getIterator())
      {
        while (iterator.hasNext())
        {
          Organization organization = iterator.next();
 
          cache.addOrganization(ServerOrganization.get(organization));
        }
      }
    }
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e)
    {
      // skip for now
    }
  }

}
