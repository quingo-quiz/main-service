namespace MainService;

model CatalogItem {
  id: uuid;
  title: string;
  description?: string;

  @doc("Число карточек в этой версии")
  cardCount: int32;

  @doc("Дата изменения последнего изменения")
  modifiedAt: utcDateTime;

  @doc("Дата создания квиза")
  createdAt: utcDateTime;

  @doc("ID владельца")
  ownerId: uuid;
}