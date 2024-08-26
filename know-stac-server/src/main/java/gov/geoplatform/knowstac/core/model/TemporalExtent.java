package gov.geoplatform.knowstac.core.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

// The object describes the spatial extents of the Collection.
public class TemporalExtent
{
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private List<Date> interval;

  public TemporalExtent()
  {
    this.interval = new LinkedList<>();
  }

  public List<Date> getInterval()
  {
    return interval;
  }

  public void setInterval(List<Date> interval)
  {
    this.interval = interval;
  }

  public void addDate(Date date)
  {
    this.interval.add(date);
  }

  public static TemporalExtent build(List<Date> dates)
  {
    TemporalExtent extent = new TemporalExtent();

    // Calculate the start date
    dates.stream().reduce((a, b) -> {
      if (a.before(b))
      {
        return a;
      }

      return b;
    }).ifPresent(date -> {
      extent.addDate(date);
    });

    // Calculate the end date
    dates.stream().reduce((a, b) -> {
      if (a.after(b))
      {
        return a;
      }

      return b;
    }).ifPresent(date -> {
      extent.addDate(date);
    });

    return extent;
  }

}
