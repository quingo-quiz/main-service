import "@typespec/http";
import "../models/main.tsp";

using Http;

namespace MainService;


@route("/catalog")
interface CatalogRoutes {
  @tag("Catalog")
  @summary("Выбрать квизы из каталога")
  @get
  list(
      @query q?: string,
      @query page?: int32 = 0,
      @query size?: int32 = 20,
      ): {
    @statusCode statusCode: 200;
    @body body: SuccessResponse<Page<CatalogItem>>;
  };

  @tag("Catalog")
  @summary("Получить квиз из каталога по ID")
  @get
  @route("/{id}")
  getById(
    @path id: uuid,
  ): {
    @statusCode statusCode: 200;
    @body body: SuccessResponse<CatalogItem>;
  } | {
    @statusCode statusCode: 404;
    @body body: ErrorResponse;
  };
}
