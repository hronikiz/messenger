# Проект: Messenger (Система обмена сообщениями)

## Описание проекта
Проект представляет собой **мини-мессенджер**, разработанный на **Spring Boot** с использованием **PostgreSQL** для хранения данных.  
Цель проекта — научиться создавать полноценный бэкенд для социальной платформы с пользователями и системой обмена сообщениями, а также использовать REST API для взаимодействия с клиентской частью.

---

## Стек технологий
- **Java 25**  
- **Spring Boot 4.0.3**  
- **Spring Data JPA / Hibernate**  
- **PostgreSQL 18.3**  
- **Maven**  
- **Postman / curl** (для тестирования API)

---

## Структура базы данных

### Таблица `users`

| Column   | Type                   | Nullable | Description                  |
|----------|------------------------|---------|------------------------------|
| id       | bigint                 | NO      | Уникальный идентификатор     |
| username | varchar(255)           | YES     | Логин пользователя           |
| nickname | varchar(255)           | YES     | Никнейм                      |
| email    | varchar(255)           | YES     | Электронная почта            |
| password | varchar(255)           | YES     | Пароль                       |

### Таблица `messages`

| Column    | Type         | Nullable | Description                    |
|-----------|-------------|---------|--------------------------------|
| id        | bigint      | NO      | Уникальный идентификатор       |
| chat_id   | bigint      | NO      | Идентификатор чата             |
| sender_id | bigint      | NO      | Отправитель                    |
| text      | varchar(255)| YES     | Текст сообщения                |
| timestamp | timestamp   | NO      | Время отправки                 |

---

## Проделанная работа

1. **Настройка Spring Boot и PostgreSQL**
   - Создано приложение Spring Boot.
   - Настроен `application.properties`:
     ```properties
     spring.datasource.url=jdbc:postgresql://localhost:5432/messenger_db
     spring.datasource.username=messenger_user
     spring.datasource.password=password123
     spring.jpa.hibernate.ddl-auto=update
     spring.jpa.show-sql=true
     ```
   - Подключена зависимость PostgreSQL в `pom.xml`.

2. **Модель пользователя (`User`)**
   - Создан JPA Entity `User` с уникальными полями `email` и `nickname`.
   - Создан DTO для передачи данных пользователя без пароля (`UserDTO`).

3. **Репозиторий пользователей (`UserRepository`)**
   - Интерфейс `JpaRepository<User, Long>` с методами поиска по `email` и `nickname`.

4. **Контроллер пользователей (`UserController`)**
   - Эндпоинты:
     - `POST /users/create` — создание нового пользователя с валидацией.
     - `GET /users/all` — получение всех пользователей.

5. **Модель сообщений (`Message`)**
   - Создана сущность `Message` с привязкой к `chatId` и `senderId`.
   - Поле `timestamp` автоматически проставляется при отправке сообщения.

6. **Репозиторий сообщений (`MessageRepository`)**
   - Интерфейс `JpaRepository<Message, Long>` с методами для поиска сообщений по `chatId`.

7. **Контроллер сообщений (`MessageController`)**
   - Эндпоинты:
     - `POST /messages/send` — отправка сообщения.
     - `GET /messages/chat?senderId=...&receiverId=...` — получение всех сообщений между двумя пользователями (чат).

8. **Тестирование**
   - Использован **Postman** и **curl** для проверки эндпоинтов.
   - Проверена работа создания пользователей и отправки сообщений.
   - В базе данных PostgreSQL отображаются все пользователи и сообщения.

---

## Объяснение кода

- **UserDTO** используется для передачи данных без пароля в ответах API.  
- **MessageDTO** используется для приёма данных при отправке сообщений через POST-запрос.  
- **Spring Data JPA** автоматически создаёт таблицы (`ddl-auto=update`) и методы CRUD.  
- **Контроллеры** обрабатывают HTTP-запросы и выполняют валидацию входящих данных.  
- **PostgreSQL** хранит данные о пользователях и сообщениях, а Hibernate управляет связью между Java-классами и таблицами.

---

## Примеры использования

### Создание пользователя
```bash
POST http://localhost:8080/users/create
Content-Type: application/json

{
  "username": "Alex",
  "nickname": "Alex99",
  "email": "alex@mail.com",
  "password": "123456"
}
````

### Отправка сообщения

```bash
POST http://localhost:8080/messages/send
Content-Type: application/json

{
  "chatId": 1,
  "senderId": 1,
  "text": "Привет!"
}
```

### Получение сообщений чата

```bash
GET http://localhost:8080/messages/chat?senderId=1&receiverId=2
```

---

## Дальнейшие планы на проект

1. Реализовать **чаты между несколькими пользователями** (групповые чаты).
2. Добавить **WebSocket** для получения сообщений в реальном времени.
3. Шифрование паролей (например, BCrypt).
4. Аутентификация и авторизация (JWT).
5. Фронтенд для взаимодействия с API (React или Angular).
6. Возможность отправки изображений и медиафайлов.
7. Улучшить обработку ошибок и валидацию запросов.

---

## Заключение

Проект демонстрирует базовую работу **REST API для мессенджера** с хранением пользователей и сообщений в базе данных PostgreSQL.
Он является фундаментом для создания полноценного мессенджера с авторизацией, WebSocket и фронтендом.
