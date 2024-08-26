package gov.geoplatform.knowstac.web.controller;

import java.util.Base64;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.geoplatform.knowstac.core.model.QueryCriteria;

@Component
public class Base64QueryCriteriaConverter implements Converter<String, QueryCriteria>
{

  @Override
  public QueryCriteria convert(String source)
  {
    QueryCriteria criteria = null;

    try
    {
      byte[] decoded = Base64.getDecoder().decode(source);
      String content = new String(decoded);

      ObjectMapper objectMapper = new ObjectMapper();
      criteria = objectMapper.readValue(content, QueryCriteria.class);
    }
    catch (JsonProcessingException e)
    {
      e.printStackTrace();
    }

    return criteria;
  }

}
