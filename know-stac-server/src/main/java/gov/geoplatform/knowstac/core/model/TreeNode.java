package gov.geoplatform.knowstac.core.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public class TreeNode<T>
{
  @Schema( //
      description = "Parent item", //
      requiredMode = RequiredMode.REQUIRED //
  )
  private T                       object;

  @Schema( //
      description = "Paginated list of child items", //
      requiredMode = RequiredMode.REQUIRED, //
      example = "{\"count\": 100,\"pageNumber\": 1,\"pageSize\": 20,\"resultSet\": []}"
  )
  private ResultPage<TreeNode<T>> children;

  public TreeNode()
  {
  }

  public TreeNode(T object)
  {
    this.object = object;
  }

  public T getObject()
  {
    return object;
  }

  public void setObject(T object)
  {
    this.object = object;
  }

  public ResultPage<TreeNode<T>> getChildren()
  {
    return children;
  }

  public void setChildren(ResultPage<TreeNode<T>> children)
  {
    this.children = children;
  }

  @Override
  public int hashCode()
  {
    return this.getObject().hashCode();
  }

}
