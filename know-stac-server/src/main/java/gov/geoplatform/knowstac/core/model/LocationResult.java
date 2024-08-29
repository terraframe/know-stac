package gov.geoplatform.knowstac.core.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.geoprism.registry.view.JsonSerializable;

public class LocationResult implements JsonSerializable
{
  @JsonIgnore
  private Object  rid;

  private String  oid;

  private String  uuid;

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

  public String getUuid()
  {
    return uuid;
  }

  public void setUuid(String uuid)
  {
    this.uuid = uuid;
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

  public static LocationResult build(Map<String, Object> map)
  {
    if (map != null)
    {
      LocationResult result = new LocationResult();
      result.setRid(map.get("rid"));
      result.setOid((String) map.get("oid"));
      result.setUuid((String) map.get("uuid"));
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
    if (obj instanceof LocationResult)
    {
      return this.uuid.equals( ( (LocationResult) obj ).uuid);
    }

    return super.equals(obj);
  }

}
