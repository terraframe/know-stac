package gov.geoplatform.knowstac.core.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import net.geoprism.registry.view.JsonSerializable;

public class LocationResult implements JsonSerializable
{
  @JsonIgnore
  private Object  rid;

  @Schema( //
      description = "Local ID of the location", //
      requiredMode = RequiredMode.REQUIRED, //
      example = "9b031c88-6e3a-4420-92ab-8aefe160cb60" //
  )
  private String  oid;

  @Schema( //
      description = "Universal ID of the location", //
      requiredMode = RequiredMode.REQUIRED, //
      example = "29c374b2-03cb-4297-b30e-c334ef31340e" //
  )
  private String  uid;

  @Schema( //
      description = "Location code", //
      requiredMode = RequiredMode.REQUIRED, //
      example = "CO" //
  )
  private String  code;

  @Schema( //
      description = "Location label", //
      requiredMode = RequiredMode.REQUIRED, //
      example = "Colorado" //
  )
  private String  label;

  @Schema( //
      description = "Number of children locations", //
      example = "10" //
  )
  private Integer size;

  @Schema( //
      description = "Number of Stac Items assigned to the location", //
      example = "10" //
  )
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

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
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
      result.setUid((String) map.get("uid"));
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
      return this.uid.equals( ( (LocationResult) obj ).uid);
    }

    return super.equals(obj);
  }

}
