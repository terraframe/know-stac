package gov.geoplatform.knowstac.core.model;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.geom.Envelope;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.geoplatform.knowstac.core.serialization.EnvelopeDeserializer;
import gov.geoplatform.knowstac.core.serialization.EnvelopeSerializer;

public class QueryCriteria
{
  private Map<String, Object> properties;

  @JsonDeserialize(using = EnvelopeDeserializer.class)
  @JsonSerialize(using = EnvelopeSerializer.class)
  private Envelope            bbox;

  public QueryCriteria()
  {
    this.properties = new HashMap<>();
  }

  public Map<String, Object> getProperties()
  {
    return properties;
  }

  public void setProperties(Map<String, Object> properties)
  {
    this.properties = properties;
  }

  public Envelope getBbox()
  {
    return bbox;
  }

  public void setBbox(Envelope bbox)
  {
    this.bbox = bbox;
  }

  public String toJSON()
  {
    ObjectMapper mapper = new ObjectMapper();

    try
    {
      return mapper.writeValueAsString(this);
    }
    catch (JsonProcessingException e)
    {
      throw new RuntimeException(e);
    }
  }

  public String toEncodedId()
  {
    return Base64.getEncoder().encodeToString(this.toJSON().getBytes());
  }

  public boolean hasConditions()
  {
    return this.bbox != null || this.properties.size() > 0;
  }

}
