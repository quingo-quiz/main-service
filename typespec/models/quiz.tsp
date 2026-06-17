namespace MainService;

// =============================================================================
// Модели API
// =============================================================================

@doc("Видимость квиза")
enum Visibility {
  PUBLIC,
  PRIVATE,
}

@doc("""
  Публиковался ли квиз.
  """)
enum QuizStatus {
  @doc("Квиз ни разу не публиковался")
  UNPUBLISHED,

  @doc("Квиз опубликован. Может при этом иметь черновик с правками")
  PUBLISHED,
}

@doc("Тип карточки")
enum CardType {
  SINGLE_CHOICE,
  MULTIPLE_CHOICE,
  TEXT_INPUT,
}

@doc("Какую версию квиза представляет сводка")
enum QuizVersion {
  DRAFT,
  PUBLISHED,
}

// --------------------------------------------------------------------------
// Ответные модели
// --------------------------------------------------------------------------

@doc("Вариант ответа карточки (для SINGLE_CHOICE / MULTIPLE_CHOICE)")
model CardOption {
  @doc("ID варианта ответа внутри карточки")
  id: int32;

  text: string;
  isCorrect: boolean;
}

@doc("Карточка внутри квиза")
model Card {
  @doc("ID карточки")
  id: uuid;

  @doc("Порядковый номер в квизе")
  position: int32;

  type: CardType;
  questionText: string;

  @doc("Время на ответ в секундах")
  timerSeconds: int32;

  @doc("Варианты ответа. Присутствует только для SINGLE_CHOICE / MULTIPLE_CHOICE")
  options?: CardOption[];

  @doc("Допустимые ответы. Присутствует только для TEXT_INPUT")
  acceptedTexts?: string[];
}

@doc("Содержимое одного квиза. Структура общая для черновика и снапшота")
model QuizContent {
  id: uuid;
  title: string;
  description?: string;

  @doc("Карточки в порядке следования")
  cards: Card[];

  @doc("Дата последнего изменения")
  modifiedAt?: utcDateTime;

  @doc("Дата создания.")
  createdAt?: utcDateTime;
}

@doc("Полноценная сущность квиза с идентичностью и контентом")
model Quiz {
  id: uuid;

  @doc("ID владельца")
  ownerId: uuid;

  visibility: Visibility;

  @doc("Производное состояние.")
  status: QuizStatus;

  @doc("Черновик. Виден только владельцу")
  draft: QuizContent | null;

  @doc("Опубликованный квиз.")
  snapshot: QuizContent | null;
}

@doc("""
  Лёгкая сводка черновика или реального квиза.
  """)
model QuizSummary {
  @doc("ID квиза. Совпадает у DRAFT- и PUBLISHED-сводки одного квиза")
  id: uuid;

  @doc("Какую версию квиза представляет эта сводка")
  version: QuizVersion;

  @doc("Заголовок этой версии")
  title: string;

  @doc("Описание этой версии")
  description?: string;

  @doc("Состояние квиза в целом")
  status: QuizStatus;

  visibility: Visibility;

  @doc("Число карточек в этой версии")
  cardCount: int32;

  @doc("Дата изменения этой версии")
  modifiedAt: utcDateTime;

  createdAt: utcDateTime;
}

// --------------------------------------------------------------------------
// Запросные модели
// --------------------------------------------------------------------------

@doc("Создание квиза")
model CreateQuizRequest {
  @minLength(1)
  title: string;

  description?: string;
  visibility: Visibility;
}

@doc("Изменение мета-данных квиза.")
model UpdateQuizRequest {
  @doc("Явное целевое значение")
  visibility: Visibility;
}

@doc("""
  Вариант ответа в запросе сохранения черновика.
  """)
model CardOptionInput {
  text: string;
  isCorrect: boolean;
}

@doc("""
  Карточка в запросе сохранения черновика.
  Клиент НЕ присылает id/position — порядок определяется индексом в массиве,
  id назначает сервер.
  """)
model CardInput {
  type: CardType;
  questionText: string;
  timerSeconds: int32;

  @doc("Для SINGLE_CHOICE / MULTIPLE_CHOICE")
  options?: CardOptionInput[];

  @doc("Для TEXT_INPUT")
  acceptedTexts?: string[];
}

@doc("""
  Полная замена содержимого черновика.
  Мягкая валидация: обязателен только непустой title; карточки могут быть
  недозаполнены.
  """)
model SaveDraftRequest {
  @minLength(1)
  title: string;

  description?: string;

  @doc("Карточки в нужном порядке")
  @maxItems(60)
  cards: CardInput[];
}
