# currency-rates

![CI](https://github.com/xoAge/currency-rates/actions/workflows/ci.yml/badge.svg)

REST-сервис курсов валют на основе данных ЦБ РФ: курсы на сегодня, 
курс одной валюты и конвертация между любыми двумя валютами через рубль.

## Стек

- **Java 26**, **Spring Boot 4** (Spring MVC, Validation, Cache)
- **RestClient** - HTTP-клиент к API ЦБ с таймаутами
- **Jackson 3** - разбор JSON
- **Caffeine** - кеш курсов в памяти
- **JUnit 5, Mockito, AssertJ, MockMvc, MockRestServiceServer** - тесты
- **Docker** (многоэтапная сборка), **GitHub Actions** (CI)

## Возможности

- Курсы всех валют ЦБ на текущую дату
- Курс одной валюты (код в любом регистре: `usd`, `USD`)
- Конвертация между любыми двумя валютами
- Точные расчёты на `BigDecimal`, банковское округление результата
- Кеширование ответа ЦБ на 1 час с защитой от «давки» (`sync = true`)
- Таймауты на запросы к ЦБ
- Ошибки в едином формате **ProblemDetail**

## API

| Метод | Адрес | Описание |
|---|---|---|
| `GET` | `/rates` | Все курсы на дату, отсортированы по коду |
| `GET` | `/rates/{code}` | Курс одной валюты |
| `GET` | `/convert?from=USD&to=EUR&amount=100` | Конвертация суммы |

Курс `rubPerUnit` - сколько рублей стоит **одна** единица валюты (номинал ЦБ уже учтён: например, 
ЦБ публикует курс за 100 иен, а сервис отдаёт курс за 1 иену).

### Примеры

```
GET /rates/usd
```
```json
{
  "code": "USD",
  "name": "Доллар США",
  "rubPerUnit": 92.50320000
}
```

```
GET /convert?from=USD&to=EUR&amount=100
```
```json
{
  "from": "USD",
  "to": "EUR",
  "amount": 100,
  "result": 92.31,
  "date": "2026-11-21"
}
```

### Ошибки

| Код | Когда | Пример |
|---|---|---|
| `400` | Неверные параметры: код не из трёх латинских букв, сумма ≤ 0 или не число, нет параметра | `/rates/US`, `amount=-5` |
| `404` | Валюты нет в курсах ЦБ | `/rates/XYZ` |
| `503` | API ЦБ недоступен или ответил некорректно | таймаут, ошибка 5xx |

```json
{
  "type": "about:blank",
  "title": "Валюта не найдена",
  "status": 404,
  "detail": "Неизвестная валюта: XYZ",
  "instance": "/rates/XYZ"
}
```

## Архитектура

```
api   -> RateController, DTO ответов, ApiExceptionHandler   - HTTP
rate  -> RateService, Rate, DailyRates, Conversion          - своя модель и логика
cbr   -> CbrClient, CbrMapper, DTO ответа ЦБ                - интеграция с ЦБ
config-> CbrProperties, CbrClientConfig, CacheConfig        - настройки
```

## Запуск

### В Docker

```bash
docker build -t currency-rates .
docker run --rm -p 8082:8082 currency-rates
```

## Настройки

| Свойство | Переменная окружения | По умолчанию | Описание |
|---|---|---|---|
| `cbr.base-url` | `CBR_BASE_URL` | `https://www.cbr-xml-daily.ru` | Адрес API курсов |
| `cbr.connect-timeout` | `CBR_CONNECT_TIMEOUT` | `2s` | Таймаут подключения |
| `cbr.read-timeout` | `CBR_READ_TIMEOUT` | `5s` | Таймаут чтения ответа |
| `spring.cache.caffeine.spec` | - | `maximumSize=10,expireAfterWrite=1h` | Размер и время жизни кеша |

## Тесты

```bash
./mvnw test
```

- **Unit:** `CbrMapper` (учёт номинала, рубль), 
`RateService` (кросс-курс, регистр кодов, банковское округление, неизвестная валюта)
- **HTTP-клиент:** `CbrClient` с `MockRestServiceServer` - разбор ответа с типом `application/javascript`, 
обработка ошибки 500 и битого JSON
- **Веб-слой:** `RateController` с `@WebMvcTest` и `MockMvc` - коды 200/400/404/503 и формат ProblemDetail

Тесты не обращаются к настоящему API ЦБ.

## Источник данных

Курсы ЦБ РФ в формате JSON — [cbr-xml-daily.ru](https://www.cbr-xml-daily.ru).
