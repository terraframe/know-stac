package gov.geoplatform.knowstac.core.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public class ResultPage<T>
{
  @Schema( //
      description = "Total number of items in the result", //
      requiredMode = RequiredMode.REQUIRED, //
      example = "100" //
  )
  private Integer count;

  @Schema( //
      description = "Page number", //
      requiredMode = RequiredMode.REQUIRED, //
      example = "1" //
  )
  private Integer pageNumber;

  @Schema( //
      description = "Page size", //
      requiredMode = RequiredMode.REQUIRED, //
      example = "20" //
  )
  private Integer pageSize;

  @Schema( //
      description = "Items in the page", //
      requiredMode = RequiredMode.REQUIRED //
  )
  private List<T> resultSet;

  public ResultPage()
  {
  }

  public Integer getCount()
  {
    return count;
  }

  public void setCount(Integer count)
  {
    this.count = count;
  }

  public Integer getPageNumber()
  {
    return pageNumber;
  }

  public void setPageNumber(Integer pageNumber)
  {
    this.pageNumber = pageNumber;
  }

  public Integer getPageSize()
  {
    return pageSize;
  }

  public void setPageSize(Integer pageSize)
  {
    this.pageSize = pageSize;
  }

  public List<T> getResultSet()
  {
    return resultSet;
  }

  public void setResultSet(List<T> resultSet)
  {
    this.resultSet = resultSet;
  }
}
