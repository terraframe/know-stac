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
  private Object rid;

  private String oid;

  private String uuid;

  private String code;

  private String label;

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

  public static LocationResult build(Map<String, Object> map)
  {
    LocationResult result = new LocationResult();
    result.setRid(map.get("rid"));
    result.setOid((String) map.get("oid"));
    result.setUuid((String) map.get("uuid"));
    result.setCode((String) map.get("code"));
    result.setLabel((String) map.get("label"));

    return result;
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

}
