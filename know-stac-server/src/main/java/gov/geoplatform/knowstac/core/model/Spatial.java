package gov.geoplatform.knowstac.core.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Envelope;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.geoplatform.knowstac.core.serialization.EnvelopeDeserializer;
import gov.geoplatform.knowstac.core.serialization.EnvelopeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

// The object describes the spatial extents of the Collection.
public class Spatial
{
  // [number] REQUIRED if geometry is not null. Bounding Box of the asset
  // represented by this Item, formatted according to RFC 7946, section 5.
  @JsonInclude(Include.NON_NULL)
  @JsonDeserialize(contentUsing = EnvelopeDeserializer.class)
  @JsonSerialize(contentUsing = EnvelopeSerializer.class)
  @Schema( //
      description = "Potential spatial extents covered by the Collection.", //
      requiredMode = RequiredMode.REQUIRED, //
      example = "[[-111.12481586775012,39.32044813958673,-111.12333351564831,39.32117905850098]]"
  )
  private List<Envelope> bbox;

  public Spatial()
  {
    this.bbox = new LinkedList<>();
  }

  public List<Envelope> getBbox()
  {
    return bbox;
  }

  public void setBbox(List<Envelope> bbox)
  {
    this.bbox = bbox;
  }

  public void addEnvelope(Envelope envelope)
  {
    this.bbox.add(envelope);
  }

  public static Spatial build(List<Envelope> envelopes)
  {
    Optional<Envelope> optional = envelopes.stream().filter(a -> a != null).reduce((a, b) -> {
      Envelope env = new Envelope(a);
      env.expandToInclude(b);

      return env;
    });

    Spatial spatial = new Spatial();

    optional.ifPresent(env -> {
      spatial.addEnvelope(env);
    });

    for (Envelope envelope : envelopes)
    {
      spatial.addEnvelope(envelope);
    }

    return spatial;
  }

}
