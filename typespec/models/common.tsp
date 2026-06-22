namespace MainService;

model SuccessResponse<T> {
  status: int32;
  statusMessage: string;
  message: string;
  data: T;
  timestamp: utcDateTime;
}

@doc("Страница результатов с пагинацией")
model Page<T> {
  items: T[];

  @doc("Текущая страница")
  page: int32;

  @doc("Размер страницы")
  size: int32;

  @doc("Всего элементов")
  total: int64;

  @doc("Всего страниц")
  totalPages: int32;
}

@error
model ErrorResponse {
  status: int32;
  statusMessage: string;
  message: string;
  path: string;
  method: string;
  timestamp: utcDateTime;

  fieldErrors?: Record<string>;
  rejectedValues?: Record<unknown>;
  details?: Record<unknown>;
}

@format("uuid")
scalar uuid extends string;
