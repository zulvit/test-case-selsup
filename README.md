# CRPT API Java Client

Этот проект представляет собой Java клиент для работы с API Честного Знака, реализованный в соответствии с предоставленным тестовым заданием. Он поддерживает ограничение по количеству запросов и является потокобезопасным.

## Требования к заданию

- [x] Реализован на Java (версия 11 разрешена)
- [x] Класс является потокобезопасным
- [x] Поддерживается ограничение на количество запросов к API
- [x] Ограничение на запросы конфигурируется через конструктор
- [x] Предусмотрено блокирование запроса при превышении лимита
- [x] Реализован метод для создания документа
- [x] Документ и подпись передаются в метод в виде Java объекта и строки
- [x] Использованы библиотеки для HTTP клиента и JSON сериализации
- [x] Решение удобно для расширения функционала
- [x] Весь код оформлен в одном файле `CrptApi.java`

## Использование

```
String apiUrl = "https://api.example.com"; // URL API
TimeUnit timeUnit = TimeUnit.MINUTES; // Интервал времени для ограничения
int requestLimit = 100; // Максимальное количество запросов в заданный интервал времени
CrptApi apiClient = new CrptApi(apiUrl, timeUnit, requestLimit);

Document document = new Document();
// Заполнение документа данными

String signature = "Ваша подпись";

CompletableFuture<String> responseFuture = apiClient.createDocument(document, signature);
responseFuture.thenAccept(response -> System.out.println(response));
```