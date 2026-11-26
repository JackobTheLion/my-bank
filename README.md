# My Bank

Приложение банка на Java 21, Spring Boot и Spring Cloud. Можно менять данные профиля, пополнять счёт, снимать деньги и делать переводы.

## Сервисы

| Модуль | Что делает | Порт |
| --- | --- | --- |
| `front-ui` | Веб-интерфейс | 8080 |
| `gateway` | Передаёт запросы от интерфейса сервисам | 8081 |
| `accounts` | Хранит профили, счета и балансы | 8082 |
| `cash` | Пополнение и снятие денег | 8083 |
| `transfer` | Переводы между счетами | 8084 |
| `notifications` | Записывает уведомления в лог | 8085 |

Общие DTO находятся в `commons`, версии зависимостей — в `bom`. Для входа используется Keycloak, для настроек и регистрации сервисов — Consul. Accounts и Keycloak хранят данные в PostgreSQL.

## Запуск

Нужен Docker с Compose. Команды выполняются из корня проекта.

```bash
docker compose up --build -d
```

Откройте [приложение](http://localhost:8080/account). Пользователи: `ivanov` и `petrov`, пароль у обоих — `bank-demo-local`.

При первом запуске создаются демосчета и импортируются пользователи Keycloak.

- [Keycloak](http://keycloak.localhost:8180/admin/) — логин `admin`, пароль `keycloak-admin-local`.
- [Consul](http://localhost:8500/ui/) — настройки и список сервисов.

Посмотреть логи:

```bash
docker compose logs -f
```

Остановить приложение:

```bash
docker compose down
```

## Сборка и тесты

Для сборки без Docker нужен JDK 21.

```bash
./mvnw clean verify
```

## Настройки

Настройки каждого сервиса лежат в его `src/main/resources/application.yaml`. Переменные из `docker-compose.yaml` можно задать через окружение или файл `.env` в корне проекта. Значения для локального запуска уже указаны в Compose.

### Общие параметры приложений

Значения ниже указаны для запуска через Compose. Порт каждого приложения задаётся через `SERVER_PORT`, значения по умолчанию есть в таблице сервисов выше.

Gateway, Accounts, Cash, Transfer и Notifications также используют:

| Переменная | Значение в Compose | Для чего |
| --- | --- | --- |
| `CONSUL_HOST` | `consul` | Адрес Consul |
| `CONSUL_PORT` | `8500` | Порт Consul |
| `SPRING_CLOUD_CONSUL_DISCOVERY_HOSTNAME` | Имя сервиса | Адрес, под которым сервис регистрируется в Consul |

### Front UI

| Переменная | Значение в Compose | Для чего |
| --- | --- | --- |
| `GATEWAY_BASE_URL` | `http://gateway:8081` | Адрес Gateway; без Docker — `http://localhost:8081` |
| `KEYCLOAK_ISSUER_URI` | `http://keycloak.localhost:8180/realms/my-bank` | Адрес realm Keycloak |
| `KEYCLOAK_FRONT_UI_CLIENT_SECRET` | `front-ui-local` | Секрет клиента Front UI |

### Gateway

Использует `SERVER_PORT` со значением `8081` и общие параметры Consul. Дополнительных переменных нет.

### Accounts

| Переменная | Значение в Compose | Для чего |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/my_bank` | Подключение к PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Пользователь БД |
| `SPRING_DATASOURCE_PASSWORD` | `postgres-local` | Пароль БД |
| `ACCOUNTS_DATABASE_SCHEMA` | `accounts` | Схема таблиц Accounts и Liquibase |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | `http://keycloak.localhost:8180/realms/my-bank` | Издатель JWT |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI` | `http://keycloak:8180/realms/my-bank/protocol/openid-connect/certs` | Ключи для проверки JWT |
| `KEYCLOAK_TOKEN_URI` | `http://keycloak:8180/realms/my-bank/protocol/openid-connect/token` | Получение токена для отправки уведомлений |
| `ACCOUNTS_NOTIFICATION_CLIENT_ID` | `accounts` | Клиент Keycloak для уведомлений |
| `ACCOUNTS_NOTIFICATION_CLIENT_SECRET` | `accounts-notifications-local` | Секрет этого клиента |
| `ACCOUNTS_NOTIFICATIONS_POLL_DELAY` | `PT5S` | Пауза между отправками уведомлений |


### Cash

| Переменная | Значение в Compose | Для чего |
| --- | --- | --- |
| `KEYCLOAK_ISSUER_URI` | `http://keycloak.localhost:8180/realms/my-bank` | Издатель JWT |
| `KEYCLOAK_JWK_SET_URI` | `http://keycloak:8180/realms/my-bank/protocol/openid-connect/certs` | Ключи для проверки JWT |
| `KEYCLOAK_TOKEN_URI` | `http://keycloak:8180/realms/my-bank/protocol/openid-connect/token` | Получение токена для запросов к сервисам |
| `CASH_ACCOUNTS_CLIENT_ID` | `cash` | Клиент Keycloak |
| `CASH_ACCOUNTS_CLIENT_SECRET` | `cash-accounts-local` | Секрет клиента |

### Transfer

| Переменная | Значение в Compose | Для чего |
| --- | --- | --- |
| `KEYCLOAK_ISSUER_URI` | `http://keycloak.localhost:8180/realms/my-bank` | Издатель JWT |
| `KEYCLOAK_JWK_SET_URI` | `http://keycloak:8180/realms/my-bank/protocol/openid-connect/certs` | Ключи для проверки JWT |
| `KEYCLOAK_TOKEN_URI` | `http://keycloak:8180/realms/my-bank/protocol/openid-connect/token` | Получение токена для запросов к сервисам |
| `TRANSFER_ACCOUNTS_CLIENT_ID` | `transfer` | Клиент Keycloak |
| `TRANSFER_ACCOUNTS_CLIENT_SECRET` | `transfer-accounts-local` | Секрет клиента |

### Notifications

| Переменная | Значение в Compose | Для чего |
| --- | --- | --- |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | `http://keycloak.localhost:8180/realms/my-bank` | Издатель JWT |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI` | `http://keycloak:8180/realms/my-bank/protocol/openid-connect/certs` | Ключи для проверки JWT |
| `NOTIFICATIONS_LOGGING_ENABLED` | `true` | Запись уведомлений в лог |

### PostgreSQL

| Переменная | Значение в Compose | Для чего |
| --- | --- | --- |
| `POSTGRES_DB` | `my_bank` | Имя базы |
| `POSTGRES_USER` | `postgres` | Пользователь БД |
| `POSTGRES_PASSWORD` | `postgres-local` | Пароль БД |
| `PGPASSWORD` | Значение `POSTGRES_PASSWORD` | Пароль для команд psql |
| `ACCOUNTS_DATABASE_SCHEMA` | `accounts` | Схема Accounts |
| `KEYCLOAK_DB_SCHEMA` | `keycloak` | Схема Keycloak |
| `CASH_DATABASE_SCHEMA` | `cash` | Создаваемая схема Cash |
| `TRANSFER_DATABASE_SCHEMA` | `transfer` | Создаваемая схема Transfer |
| `FRONT_UI_DATABASE_SCHEMA` | `front_ui` | Создаваемая схема Front UI |
| `GATEWAY_DATABASE_SCHEMA` | `gateway` | Создаваемая схема Gateway |
| `NOTIFICATIONS_DATABASE_SCHEMA` | `notifications` | Создаваемая схема Notifications |

Для упрощения используем одного ТУЗа для всех сервисов, которым нужна БД.

Адрес возврата после входа — `http://localhost:8080/login/oauth2/code/keycloak`, пароль демопользователей — `bank-demo-local`. Файл realm импортируется при первом запуске.

## Настройки Consul

Файлы из `consul/` загружаются скриптом `consul/init-kv.sh` при запуске `consul-init`. Содержимое каждого YAML-файла записывается в ключ `config/<имя файла>/data`.

Чтобы загрузить настройки повторно:

```bash
docker compose run --rm consul-init
```

Повторная загрузка перезаписывает соответствующие ключи в Consul содержимым файлов.
