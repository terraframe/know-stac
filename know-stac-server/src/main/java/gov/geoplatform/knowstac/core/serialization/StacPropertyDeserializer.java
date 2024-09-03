package gov.geoplatform.knowstac.core.serialization;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.runwaysdk.session.Request;

import gov.geoplatform.knowstac.core.model.PropertyType;
import gov.geoplatform.knowstac.core.model.StacLocation;
import gov.geoplatform.knowstac.core.model.StacOrganization;
import gov.geoplatform.knowstac.core.model.StacProperty;
import gov.geoplatform.knowstac.core.service.business.StacPropertyBusinessService;
import net.geoprism.spring.ApplicationContextHolder;

public class StacPropertyDeserializer extends JsonDeserializer<Map<String, Object>>
{
  private StacPropertyBusinessService service;

  public StacPropertyDeserializer()
  {
    this.service = ApplicationContextHolder.getBean(StacPropertyBusinessService.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  @Request
  public Map<String, Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
  {
    Map<String, StacProperty> properties = this.service.getAll().stream().collect(Collectors.toMap(v -> v.getName(), v -> v));

    Map<String, Object> result = new HashMap<String, Object>();

    HashMap<String, Object> deserialized = p.readValueAs(HashMap.class);

    Set<Entry<String, Object>> entries = deserialized.entrySet();

    for (Entry<String, Object> entry : entries)
    {
      String key = entry.getKey();
      Object value = entry.getValue();

      if (properties.containsKey(key))
      {
        StacProperty property = properties.get(key);

        if (property.getType().equals(PropertyType.DATE))
        {
          result.put(key, new DateDeserializer().deserialize(p, value.toString()));
        }
        else if (property.getType().equals(PropertyType.DATE_TIME))
        {
          result.put(key, new DateTimeDeserializer().deserialize(p, value.toString()));
        }
        else if (property.getType().equals(PropertyType.LOCATION))
        {
          List<Map<String, Object>> list = (List<Map<String, Object>>) value;

          List<StacLocation> values = list.stream().map(map -> StacLocation.build((String) map.get("uuid"), (String) map.get("label"))).collect(Collectors.toList());

          result.put(key, values);
        }
        else if (property.getType().equals(PropertyType.ORGANIZATION))
        {
          List<Map<String, Object>> list = (List<Map<String, Object>>) value;

          List<StacOrganization> values = list.stream().map(map -> StacOrganization.build((String) map.get("code"), (String) map.get("label"))).collect(Collectors.toList());

          result.put(key, values);
        }
        else
        {
          result.put(key, value);
        }
      }
      else
      {
        result.put(key, value);
      }

    }

    return result;
  }

}
