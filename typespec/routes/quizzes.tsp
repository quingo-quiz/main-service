import "@typespec/http";
import "../models/main.tsp";

using Http;

namespace MainService;


@route("/quizzes")
interface QuizRoutes {
  @tag("Quiz")
  @summary("Список моих квизов")
  @get
  list(): {
    @statusCode statusCode: 200;
    @body body: SuccessResponse<QuizSummary[]>;
  } | {
    @statusCode statusCode: 401;
    @body body: ErrorResponse;
  };

  @tag("Quiz")
  @summary("Создать квиз")
  @post
  create(
    @body request: CreateQuizRequest,
  ): {
    @statusCode statusCode: 201;
    @body body: SuccessResponse<Quiz>;
  } | {
    @statusCode statusCode: 400 | 401;
    @body body: ErrorResponse;
  };

  @tag("Quiz")
  @summary("Получить квиз целиком")
  @get
  @route("/{id}")
  get(
    @path id: uuid,
  ): {
    @statusCode statusCode: 200;
    @body body: SuccessResponse<Quiz>;
  } | {
    @statusCode statusCode: 401 | 403 | 404;
    @body body: ErrorResponse;
  };

  @tag("Quiz")
  @summary("Изменить мета-данные квиза")
  @patch
  @route("/{id}")
  update(
    @path id: uuid,
    @body request: UpdateQuizRequest,
  ): {
    @statusCode statusCode: 200;
    @body body: SuccessResponse<Quiz>;
  } | {
    @statusCode statusCode: 400 | 401 | 403 | 404;
    @body body: ErrorResponse;
  };

  @tag("Quiz")
  @summary("Удалить квиз")
  @delete
  @route("/{id}")
  remove(
    @path id: uuid,
  ): {
    @statusCode statusCode: 200;
    @body body: SuccessResponse<null>;
  } | {
    @statusCode statusCode: 401 | 403 | 404;
    @body body: ErrorResponse;
  };

  @tag("Draft")
  @summary("Создать черновик для опубликованного квиза.")
  @post
  @route("/{id}/draft")
  startEditing(
    @path id: uuid,
  ): {
    @statusCode statusCode: 201;
    @body body: SuccessResponse<Quiz>;
  } | {
    @doc("409 — черновик уже существует")
    @statusCode statusCode: 401 | 403 | 404 | 409;
    @body body: ErrorResponse;
  };

  @tag("Draft")
  @summary("Сохранить черновик.")
  @put
  @route("/{id}/draft")
  saveDraft(
    @path id: uuid,
    @body request: SaveDraftRequest,
  ): {
    @statusCode statusCode: 200;
    @body body: SuccessResponse<Quiz>;
  } | {
    @doc("400 — нарушены требования сохранения")
    @statusCode statusCode: 400 | 401 | 403 | 404;
    @body body: ErrorResponse;
  };

  @tag("Draft")
  @summary("Опубликовать черновик. Строгая валидация содержимого")
  @post
  @route("/{id}/publish")
  publish(
    @path id: uuid,
  ): {
    @statusCode statusCode: 200;
    @body body: SuccessResponse<Quiz>;
  } | {
    @doc("422 — черновик не проходит строгую валидацию")
    @statusCode statusCode: 401 | 403 | 404 | 422;
    @body body: ErrorResponse;
  };

  @tag("Draft")
  @summary("Удалить черновик полностью.")
  @delete
  @route("/{id}/draft")
  discardDraft(
    @path id: uuid,
  ): {
    @statusCode statusCode: 200;
    @body body: SuccessResponse<null>;
  } | {
    @doc("409 — черновика нет")
    @statusCode statusCode: 401 | 403 | 404 | 409;
    @body body: ErrorResponse;
  };
}
