/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.knowstac.core.serialization;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import gov.geoplatform.knowstac.core.model.PropertyType;
import gov.geoplatform.knowstac.core.model.StacLocation;
import gov.geoplatform.knowstac.core.model.StacOrganization;
import gov.geoplatform.knowstac.core.model.StacProperty;
import gov.geoplatform.knowstac.core.service.business.StacPropertyBusinessService;
import net.geoprism.spring.core.ApplicationContextHolder;

public class StacPropertySerializer extends JsonSerializer<Map<String, Object>>
{
  private StacPropertyBusinessService service;

  public StacPropertySerializer()
  {
    this.service = ApplicationContextHolder.getBean(StacPropertyBusinessService.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void serialize(Map<String, Object> map, JsonGenerator gen, SerializerProvider serializers) throws IOException
  {
    gen.writeStartObject();

    Map<String, StacProperty> properties = this.service.getAll().stream().collect(Collectors.toMap(v -> v.getName(), v -> v));

    Set<Entry<String, Object>> entries = map.entrySet();

    for (Entry<String, Object> entry : entries)
    {
      String key = entry.getKey();
      Object value = entry.getValue();

      if (properties.containsKey(key))
      {
        StacProperty property = properties.get(key);

        if (property.getType().equals(PropertyType.DATE_TIME))
        {
          gen.writeStringField(key, new DateTimeSerializer().serialize((Date) value));
        }
        else if (property.getType().equals(PropertyType.DATE))
        {
          gen.writeStringField(key, new DateSerializer().serialize((Date) value));
        }
        else if (property.getType().equals(PropertyType.NUMBER))
        {
          gen.writeNumberField(key, (Double) value);
        }
        else if (property.getType().equals(PropertyType.LOCATION))
        {
          gen.writeArrayFieldStart(key);

          List<StacLocation> list = (List<StacLocation>) value;

          for (StacLocation location : (Iterable<StacLocation>) list::iterator)
          {
            gen.writeObject(location);
          }

          gen.writeEndArray();
        }
        else if (property.getType().equals(PropertyType.ORGANIZATION))
        {
          gen.writeArrayFieldStart(key);

          List<StacOrganization> list = (List<StacOrganization>) value;

          for (StacOrganization organization : (Iterable<StacOrganization>) list::iterator)
          {
            gen.writeObject(organization);
          }

          gen.writeEndArray();
        }
        else if (property.getType().equals(PropertyType.STRING))
        {
          gen.writeStringField(key, value.toString());
        }
      }
      else
      {
        gen.writeObjectField(key, value);
      }
    }

    gen.writeEndObject();
  }
}