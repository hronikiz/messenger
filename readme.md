# Отчет по проекту Messenger

## 1. Общая информация

**Выполнил:** Иордатий Игнат
 
**Группа:** IA2403

**Название проекта:** Messenger  
**Тип проекта:** backend-приложение для мессенджера  
**Основная идея:** предоставить серверную часть для регистрации пользователей, авторизации, создания личных и групповых чатов, обмена сообщениями и получения сообщений в реальном времени через WebSocket.

Проект реализован как Java/Spring Boot приложение. Он предоставляет REST API для обычных HTTP-запросов и WebSocket/STOMP канал для real-time обмена сообщениями.

Главный класс запуска находится в файле `src/main/java/com/example/messenger/MessengerApplication.java`.

```java
// src/main/java/com/example/messenger/MessengerApplication.java, строки 6-10
@SpringBootApplication
public class MessengerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessengerApplication.class, args);
    }
}
```

Аннотация `@SpringBootApplication` включает автоконфигурацию Spring Boot, сканирование компонентов и регистрацию bean-объектов. Метод `main` запускает приложение.

Примечание по фрагментам кода: в исходных файлах часть русских сообщений и комментариев сейчас отображается с поврежденной кодировкой. В отчете такие строки в некоторых примерах приведены в восстановленном читаемом виде, а номера строк оставлены по фактическим файлам проекта.

---

## 2. Назначение проекта

Проект решает типичную задачу backend-части мессенджера:

- регистрация нового пользователя;
- вход пользователя по email и паролю;
- выдача JWT-токена после успешной регистрации или входа;
- защита API через JWT;
- получение профиля пользователя;
- поиск пользователей по nickname;
- создание личного чата;
- создание группового чата;
- добавление участников в групповой чат;
- получение списка чатов пользователя;
- получение истории сообщений с пагинацией;
- отправка сообщений через WebSocket;
- отметка сообщений как прочитанных;
- отправка real-time событий другим участникам чата.

Иначе говоря, это не просто набор отдельных контроллеров, а слоистое backend-приложение с разделением на контроллеры, сервисы, репозитории, сущности, DTO, security и обработку ошибок.

---

## 3. Использованные технологии и почему они применены

| Технология | Где используется | Зачем используется |
|---|---|---|
| Java 17 | `pom.xml`, строка 21 | Современная LTS-версия Java, совместимая со Spring Boot 3.x. |
| Spring Boot 3.5.12 | `pom.xml`, строки 7-12 | Быстрый запуск backend-приложения, автоконфигурация, встроенный сервер, удобная работа со Spring-модулями. |
| Spring Web | `pom.xml`, строки 26-30 | Создание REST API через `@RestController`, `@GetMapping`, `@PostMapping`. |
| Spring WebSocket | `pom.xml`, строки 32-36 | Real-time обмен сообщениями через WebSocket и STOMP. |
| Spring Security | `pom.xml`, строки 38-42 | Защита API, настройка авторизации и интеграция JWT-фильтра. |
| Spring Validation | `pom.xml`, строки 44-48 | Проверка входящих DTO через `@Valid`, `@NotBlank`, `@Size`, `@Email`. |
| Spring Data JPA | `pom.xml`, строки 50-54 | Работа с базой данных через репозитории и сущности, без ручного SQL для базовых операций. |
| PostgreSQL Driver | `pom.xml`, строки 55-59 | Подключение приложения к PostgreSQL. |
| JJWT | `pom.xml`, строки 61-78 | Генерация и проверка JWT-токенов. |
| SpringDoc OpenAPI / Swagger UI | `pom.xml`, строки 80-85 | Автоматическая документация REST API. |
| Lombok | `pom.xml`, строки 87-92 | Сокращение шаблонного кода: getters, setters, constructors. |
| Maven Wrapper | `.mvn/wrapper/maven-wrapper.properties`, строки 1-3 | Запуск Maven без глобальной установки Maven. |

Фрагмент конфигурации Maven:

```xml
<!-- pom.xml, строки 7-22 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.12</version>
    <relativePath/>
</parent>

<properties>
    <java.version>17</java.version>
    <jjwt.version>0.12.5</jjwt.version>
</properties>
```

Почему выбран Maven: он управляет зависимостями, сборкой, тестами и упаковкой приложения. Все основные библиотеки описаны централизованно в `pom.xml`.

---

## 4. Структура проекта

Основной код расположен в пакете `com.example.messenger`.

```text
src/main/java/com/example/messenger
├── config        - конфигурация Security и WebSocket
├── controller    - REST и WebSocket контроллеры
├── dto           - объекты запросов и ответов API
├── entity        - JPA-сущности базы данных
├── exception     - пользовательские исключения и общий handler ошибок
├── repository    - Spring Data JPA репозитории
├── security      - JWT-сервис и JWT-фильтр
└── service       - бизнес-логика приложения
```

Такая структура соответствует классической layered architecture:

```text
Client / Frontend
       |
       v
Controller layer
       |
       v
Service layer
       |
       v
Repository layer
       |
       v
Database / PostgreSQL
```

Смысл разделения:

- контроллеры принимают HTTP/WebSocket запросы;
- сервисы выполняют бизнес-логику;
- репозитории работают с базой данных;
- сущности описывают таблицы;
- DTO отделяют внешний API от внутренней структуры базы;
- security отвечает за JWT и авторизацию;
- exception слой делает ответы об ошибках единообразными.

---

## 5. Конфигурация приложения

Основные настройки находятся в `src/main/resources/application.properties`.

```properties
# src/main/resources/application.properties, строки 1-17
spring.application.name=messenger

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/messenger_db}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASS:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

jwt.secret=${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
jwt.expiration=86400000
```

Важные моменты:

- `DB_URL`, `DB_USER`, `DB_PASS` можно передать через переменные окружения;
- если переменные окружения не заданы, используются значения по умолчанию;
- `ddl-auto=update` позволяет Hibernate обновлять структуру таблиц по JPA-сущностям;
- `jwt.expiration=86400000` означает срок действия токена 24 часа;
- Swagger UI доступен по пути `/swagger-ui.html`, что задано в строках 19-22.

---

## 6. Модель данных

В проекте есть три основные сущности:

- `User` - пользователь;
- `Chat` - чат;
- `Message` - сообщение.

### 6.1. Сущность User

```java
// src/main/java/com/example/messenger/entity/User.java, строки 12-44
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    private boolean online = false;

    private LocalDateTime lastSeen = LocalDateTime.now();

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany(mappedBy = "participants")
    @JsonIgnore
    private List<Chat> chats;
}
```

Объяснение:

- `@Entity` означает, что класс связан с таблицей базы данных;
- `@Table(name = "users")` задает имя таблицы;
- `id` является первичным ключом;
- `nickname` и `email` уникальны, потому что один email или nickname не должны принадлежать двум пользователям;
- `password` помечен `@JsonIgnore`, чтобы пароль не попадал в JSON-ответы;
- `online` и `lastSeen` позволяют хранить статус пользователя;
- связь `@ManyToMany` показывает, что пользователь может состоять во многих чатах.

### 6.2. Сущность Chat

```java
// src/main/java/com/example/messenger/entity/Chat.java, строки 12-40
@Entity
@Table(name = "chats")
@Getter
@Setter
@NoArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false)
    private boolean isGroup = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
        name = "chat_users",
        joinColumns = @JoinColumn(name = "chat_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Message> messages;
}
```

Объяснение:

- `isGroup` определяет тип чата: личный или групповой;
- участники связаны с чатом через промежуточную таблицу `chat_users`;
- один чат может содержать много сообщений;
- `cascade = CascadeType.ALL` и `orphanRemoval = true` означают, что сообщения управляются вместе с чатом.

### 6.3. Сущность Message

```java
// src/main/java/com/example/messenger/entity/Message.java, строки 17-44
public enum MessageType {
    TEXT, IMAGE, FILE, SYSTEM
}

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "chat_id", nullable = false)
private Chat chat;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "sender_id", nullable = false)
private User sender;

@Column(nullable = false, columnDefinition = "TEXT")
private String text;

@Column(nullable = false)
private boolean isRead = false;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private MessageType type = MessageType.TEXT;

@Column(nullable = false)
private LocalDateTime timestamp = LocalDateTime.now();
```

Объяснение:

- каждое сообщение принадлежит одному чату;
- каждое сообщение имеет одного отправителя;
- `MessageType` позволяет расширять проект: текст, изображения, файлы, системные сообщения;
- `isRead` нужен для статуса прочтения;
- `timestamp` хранит время создания сообщения.

---

## 7. Слой DTO

DTO нужны для того, чтобы API не отдавал напрямую JPA-сущности. Это повышает безопасность и делает формат запросов/ответов стабильным.

### 7.1. Регистрация

```java
// src/main/java/com/example/messenger/dto/RegisterRequest.java, строки 7-21
public record RegisterRequest(
    @NotBlank(message = "Username не может быть пустым")
    String username,

    @NotBlank(message = "Nickname не может быть пустым")
    @Size(min = 3, max = 32, message = "Nickname: от 3 до 32 символов")
    String nickname,

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть корректным")
    String email,

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль: минимум 6 символов")
    String password
) {}
```

В коде проекта эти сообщения сейчас отображаются поврежденной кодировкой, но логически здесь используются validation-аннотации:

- `@NotBlank` - поле не может быть пустым;
- `@Size` - ограничение длины;
- `@Email` - проверка email.

### 7.2. Ответ авторизации

```java
// src/main/java/com/example/messenger/dto/AuthResponse.java, строки 3-6
public record AuthResponse(
    String token,
    UserDTO user
) {}
```

После регистрации или входа клиент получает JWT-токен и данные пользователя.

### 7.3. Пользователь без пароля

```java
// src/main/java/com/example/messenger/dto/UserDTO.java, строки 5-12
public record UserDTO(
    Long id,
    String username,
    String nickname,
    String email,
    boolean online,
    LocalDateTime lastSeen
) {}
```

В `UserDTO` нет поля `password`, поэтому API не раскрывает пароль пользователя.

### 7.4. Чат и сообщение

```java
// src/main/java/com/example/messenger/dto/ChatResponse.java, строки 6-14
public record ChatResponse(
    Long id,
    String name,
    boolean isGroup,
    List<UserDTO> participants,
    MessageResponse lastMessage,
    long unreadCount,
    LocalDateTime createdAt
) {}
```

```java
// src/main/java/com/example/messenger/dto/MessageResponse.java, строки 7-15
public record MessageResponse(
    Long id,
    Long chatId,
    UserDTO sender,
    String text,
    boolean isRead,
    Message.MessageType type,
    LocalDateTime timestamp
) {}
```

DTO `ChatResponse` удобен для списка чатов, потому что сразу содержит последнее сообщение и количество непрочитанных сообщений.

---

## 8. Репозитории

Репозитории наследуются от `JpaRepository`, поэтому Spring Data JPA автоматически дает CRUD-операции:

- `save`;
- `findById`;
- `findAll`;
- `delete`;
- `existsById` и другие.

### 8.1. UserRepository

```java
// src/main/java/com/example/messenger/repository/UserRepository.java, строки 11-21
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT u FROM User u WHERE LOWER(u.nickname) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY u.nickname")
    List<User> searchByNickname(@Param("q") String q);
}
```

Зачем это нужно:

- `findByEmail` используется при логине и получении текущего пользователя;
- `existsByEmail` и `existsByNickname` проверяют уникальность перед регистрацией;
- `searchByNickname` позволяет искать пользователей на уровне базы данных.

### 8.2. ChatRepository

```java
// src/main/java/com/example/messenger/repository/ChatRepository.java, строки 15-25
@Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.id = :userId")
List<Chat> findAllByParticipantId(@Param("userId") Long userId);

@Query("""
    SELECT c FROM Chat c
    JOIN c.participants p1
    JOIN c.participants p2
    WHERE p1.id = :userId1 AND p2.id = :userId2
    AND c.isGroup = false
""")
Optional<Chat> findPrivateChat(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
```

Зачем это нужно:

- `findAllByParticipantId` получает все чаты пользователя;
- `findPrivateChat` помогает не создавать дубликат личного чата между двумя пользователями.

### 8.3. MessageRepository

```java
// src/main/java/com/example/messenger/repository/MessageRepository.java, строки 17-25
Page<Message> findByChatIdOrderByTimestampDesc(Long chatId, Pageable pageable);

List<Message> findByChatIdAndIsReadFalseAndSenderIdNot(Long chatId, Long currentUserId);

long countByChatIdAndIsReadFalseAndSenderIdNot(Long chatId, Long currentUserId);

@Modifying
@Query("UPDATE Message m SET m.isRead = true WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.isRead = false")
void markAllAsRead(@Param("chatId") Long chatId, @Param("userId") Long userId);
```

Зачем это нужно:

- получать историю сообщений постранично;
- считать непрочитанные сообщения;
- массово отмечать сообщения как прочитанные.

---

## 9. Security и JWT

Проект использует stateless-аутентификацию:

1. пользователь отправляет email и пароль;
2. сервер проверяет данные;
3. сервер выдает JWT;
4. клиент отправляет JWT в заголовке `Authorization: Bearer <token>`;
5. JWT-фильтр проверяет токен и помещает пользователя в `SecurityContext`.

### 9.1. Настройка Security

```java
// src/main/java/com/example/messenger/config/SecurityConfig.java, строки 31-50
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/ws/**").permitAll()
            .requestMatchers(
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

Объяснение:

- CSRF отключен, потому что API работает stateless и использует JWT;
- `/api/auth/**`, `/ws/**` и Swagger доступны без обязательной авторизации;
- остальные запросы требуют авторизации;
- сессии не хранятся на сервере;
- JWT-фильтр запускается до стандартного фильтра логина/пароля.

### 9.2. PasswordEncoder и UserDetailsService

```java
// src/main/java/com/example/messenger/config/SecurityConfig.java, строки 55-72
@Bean
public UserDetailsService userDetailsService() {
    return email -> userRepository.findByEmail(email)
            .map(user -> org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .build())
            .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
}

@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Зачем это нужно:

- `UserDetailsService` говорит Spring Security, как находить пользователя;
- `BCryptPasswordEncoder` хранит не простой пароль, а хеш пароля;
- это защищает пользователей при утечке базы данных.

### 9.3. Генерация и проверка JWT

```java
// src/main/java/com/example/messenger/security/JwtService.java, строки 24-39
public String generateToken(UserDetails userDetails) {
    return Jwts.builder()
            .subject(userDetails.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())
            .compact();
}

public String extractEmail(String token) {
    return extractClaim(token, Claims::getSubject);
}

public boolean isTokenValid(String token, UserDetails userDetails) {
    final String email = extractEmail(token);
    return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
}
```

`subject` токена хранит email пользователя. При каждом защищенном запросе email извлекается из токена, затем пользователь загружается из базы.

```java
// src/main/java/com/example/messenger/security/JwtService.java, строки 55-57
private SecretKey getSigningKey() {
    byte[] keyBytes = Base64.getDecoder().decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
}
```

Ключ подписи берется из `jwt.secret`. Благодаря подписи сервер может проверить, что токен не был изменен клиентом.

### 9.4. JWT-фильтр

```java
// src/main/java/com/example/messenger/security/JwtAuthenticationFilter.java, строки 34-61
final String authHeader = request.getHeader("Authorization");

if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    filterChain.doFilter(request, response);
    return;
}

final String jwt = authHeader.substring(7);
final String userEmail = jwtService.extractEmail(jwt);

if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    UserDetailsService userDetailsService =
            applicationContext.getBean(UserDetailsService.class);
    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

    if (jwtService.isTokenValid(jwt, userDetails)) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}

filterChain.doFilter(request, response);
```

Фильтр делает центральную работу авторизации: читает Bearer-токен, проверяет его и устанавливает авторизованного пользователя в контекст Spring Security.

---

## 10. WebSocket и real-time сообщения

WebSocket нужен для мгновенной доставки сообщений без постоянного опроса сервера через HTTP.

### 10.1. Настройка брокера сообщений

```java
// src/main/java/com/example/messenger/config/WebSocketConfig.java, строки 28-40
@Override
public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/queue");
    registry.setApplicationDestinationPrefixes("/app");
    registry.setUserDestinationPrefix("/user");
}

@Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
}
```

Объяснение:

- клиент подключается к `/ws`;
- сообщения от клиента идут на адреса с префиксом `/app`;
- рассылка всем подписчикам идет через `/topic`;
- индивидуальные сообщения можно отправлять через `/user`;
- SockJS добавляет fallback-механизмы для клиентов, где WebSocket ограничен.

### 10.2. Авторизация WebSocket-подключения

```java
// src/main/java/com/example/messenger/config/WebSocketConfig.java, строки 50-65
if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
    String authHeader = accessor.getFirstNativeHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);

        if (email != null) {
            var userDetails = userDetailsService.loadUserByUsername(email);
            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                accessor.setUser(auth);
            }
        }
    }
}
```

Это позволяет использовать тот же JWT-механизм не только для REST API, но и для WebSocket-соединений.

---

## 11. Контроллеры и API

Контроллеры принимают запросы, достают данные из `RequestBody`, `PathVariable`, `RequestParam` и передают работу сервисам.

### 11.1. AuthController

Файл: `src/main/java/com/example/messenger/controller/AuthController.java`

| Метод | URL | Строки | Назначение |
|---|---|---:|---|
| `POST` | `/api/auth/register` | 22-25 | Регистрация пользователя. |
| `POST` | `/api/auth/login` | 27-30 | Вход пользователя и выдача JWT. |
| `POST` | `/api/auth/logout` | 32-36 | Выход пользователя и обновление online-статуса. |

```java
// src/main/java/com/example/messenger/controller/AuthController.java, строки 22-35
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
}

@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(userService.login(request));
}

@PostMapping("/logout")
public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
    userService.logout(userDetails.getUsername());
    return ResponseEntity.noContent().build();
}
```

### 11.2. UserController

Файл: `src/main/java/com/example/messenger/controller/UserController.java`

| Метод | URL | Строки | Назначение |
|---|---|---:|---|
| `GET` | `/api/users/me` | 21-24 | Получить профиль текущего пользователя. |
| `GET` | `/api/users/{id}` | 27-30 | Получить пользователя по ID. |
| `GET` | `/api/users/search?nickname=...` | 33-36 | Найти пользователей по nickname. |

```java
// src/main/java/com/example/messenger/controller/UserController.java, строки 21-36
@GetMapping("/me")
public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(userService.getCurrentUser(userDetails.getUsername()));
}

@GetMapping("/{id}")
public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getUserById(id));
}

@GetMapping("/search")
public ResponseEntity<List<UserDTO>> search(@RequestParam String nickname) {
    return ResponseEntity.ok(userService.searchByNickname(nickname));
}
```

### 11.3. ChatController

Файл: `src/main/java/com/example/messenger/controller/ChatController.java`

| Метод | URL | Строки | Назначение |
|---|---|---:|---|
| `POST` | `/api/chats/private` | 25-33 | Создать личный чат или вернуть существующий. |
| `POST` | `/api/chats/group` | 36-44 | Создать групповой чат. |
| `GET` | `/api/chats` | 47-53 | Получить все свои чаты. |
| `GET` | `/api/chats/{chatId}` | 56-63 | Получить конкретный чат. |
| `POST` | `/api/chats/{chatId}/participants/{userId}` | 66-74 | Добавить участника в групповой чат. |

```java
// src/main/java/com/example/messenger/controller/ChatController.java, строки 25-33
@PostMapping("/private")
public ResponseEntity<ChatResponse> createPrivateChat(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody CreatePrivateChatRequest request) {

    Long currentUserId = userService.getEntityByEmail(userDetails.getUsername()).getId();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(chatService.createPrivateChat(currentUserId, request));
}
```

Контроллер получает текущего пользователя из JWT-authentication principal, затем передает его ID в сервис.

### 11.4. MessageController

Файл: `src/main/java/com/example/messenger/controller/MessageController.java`

| Тип | URL / destination | Строки | Назначение |
|---|---|---:|---|
| REST `GET` | `/api/chats/{chatId}/messages` | 27-37 | Получить историю сообщений с пагинацией. |
| REST `PUT` | `/api/chats/{chatId}/messages/read` | 40-47 | Отметить сообщения как прочитанные. |
| WebSocket | `/app/chat.{chatId}` | 53-61 | Отправить сообщение в чат. |

```java
// src/main/java/com/example/messenger/controller/MessageController.java, строки 27-36
@GetMapping("/api/chats/{chatId}/messages")
public ResponseEntity<Page<MessageResponse>> getMessages(
        @PathVariable Long chatId,
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size) {

    return ResponseEntity.ok(
        messageService.getMessages(chatId, userDetails.getUsername(), page, size)
    );
}
```

```java
// src/main/java/com/example/messenger/controller/MessageController.java, строки 53-60
@MessageMapping("/chat.{chatId}")
public void sendMessage(
        @DestinationVariable Long chatId,
        @Payload @Valid SendMessageRequest request,
        Principal principal) {

    messageService.sendMessage(chatId, principal.getName(), request);
}
```

REST используется для истории сообщений, а WebSocket - для отправки новых сообщений в реальном времени.

---

## 12. Бизнес-логика сервисов

### 12.1. UserService: регистрация

```java
// src/main/java/com/example/messenger/service/UserService.java, строки 28-52
@Transactional
public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
        throw new ConflictException("Email уже используется");
    }
    if (userRepository.existsByNickname(request.nickname())) {
        throw new ConflictException("Nickname уже занят");
    }

    User user = new User();
    user.setUsername(request.username());
    user.setNickname(request.nickname());
    user.setEmail(request.email());
    user.setPassword(passwordEncoder.encode(request.password()));

    userRepository.save(user);

    String token = jwtService.generateToken(
        org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword())
            .build()
    );

    return new AuthResponse(token, toDTO(user));
}
```

Логика:

1. проверяется, что email свободен;
2. проверяется, что nickname свободен;
3. создается объект `User`;
4. пароль хешируется через BCrypt;
5. пользователь сохраняется в базе;
6. генерируется JWT;
7. клиенту возвращается токен и безопасный DTO пользователя.

### 12.2. UserService: вход

```java
// src/main/java/com/example/messenger/service/UserService.java, строки 55-74
@Transactional
public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password())
    );

    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

    user.setOnline(true);
    userRepository.save(user);

    String token = jwtService.generateToken(
        org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword())
            .build()
    );

    return new AuthResponse(token, toDTO(user));
}
```

Spring Security сам проверяет пароль через `AuthenticationManager`. Если данные неверные, будет выброшено `BadCredentialsException`.

### 12.3. ChatService: личный чат

```java
// src/main/java/com/example/messenger/service/ChatService.java, строки 31-52
@Transactional
public ChatResponse createPrivateChat(Long currentUserId, CreatePrivateChatRequest request) {
    if (currentUserId.equals(request.targetUserId())) {
        throw new ConflictException("Нельзя создать чат с самим собой");
    }

    return chatRepository.findPrivateChat(currentUserId, request.targetUserId())
        .map(existingChat -> toDTO(existingChat, currentUserId))
        .orElseGet(() -> {
            User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
            User targetUser = userRepository.findById(request.targetUserId())
                .orElseThrow(() -> new NotFoundException("Целевой пользователь не найден"));

            Chat chat = new Chat();
            chat.setGroup(false);
            chat.setParticipants(List.of(currentUser, targetUser));
            chatRepository.save(chat);

            return toDTO(chat, currentUserId);
        });
}
```

Особенность: сервис сначала ищет уже существующий личный чат. Это правильно, потому что между двумя пользователями не должно создаваться много одинаковых личных чатов.

### 12.4. ChatService: групповой чат

```java
// src/main/java/com/example/messenger/service/ChatService.java, строки 56-74
@Transactional
public ChatResponse createGroupChat(Long currentUserId, CreateGroupChatRequest request) {
    List<Long> allIds = new ArrayList<>(request.participantIds());
    if (!allIds.contains(currentUserId)) {
        allIds.add(currentUserId);
    }

    List<User> participants = userRepository.findAllById(allIds);
    if (participants.size() < 2) {
        throw new ConflictException("В групповом чате должно быть минимум 2 участника");
    }

    Chat chat = new Chat();
    chat.setName(request.name());
    chat.setGroup(true);
    chat.setParticipants(participants);
    chatRepository.save(chat);

    return toDTO(chat, currentUserId);
}
```

Текущий пользователь автоматически добавляется в список участников, если его там нет.

### 12.5. ChatService: DTO чата

```java
// src/main/java/com/example/messenger/service/ChatService.java, строки 133-157
private ChatResponse toDTO(Chat chat, Long currentUserId) {
    List<UserDTO> participants = chat.getParticipants().stream()
        .map(userService::toDTO)
        .toList();

    var lastMessages = messageRepository
        .findByChatIdOrderByTimestampDesc(chat.getId(), PageRequest.of(0, 1));
    MessageResponse lastMessage = lastMessages.isEmpty()
        ? null
        : messageService.toDTO(lastMessages.getContent().get(0));

    long unreadCount = messageRepository
        .countByChatIdAndIsReadFalseAndSenderIdNot(chat.getId(), currentUserId);

    return new ChatResponse(
        chat.getId(),
        chat.getName(),
        chat.isGroup(),
        participants,
        lastMessage,
        unreadCount,
        chat.getCreatedAt()
    );
}
```

Здесь формируется удобный ответ для клиента:

- список участников;
- последнее сообщение;
- количество непрочитанных сообщений;
- дата создания чата.

### 12.6. MessageService: отправка сообщения

```java
// src/main/java/com/example/messenger/service/MessageService.java, строки 31-57
@Transactional
public MessageResponse sendMessage(Long chatId, String senderEmail, SendMessageRequest request) {
    Chat chat = chatRepository.findById(chatId)
        .orElseThrow(() -> new NotFoundException("Чат не найден"));

    User sender = userService.getEntityByEmail(senderEmail);

    boolean isMember = chat.getParticipants().stream()
        .anyMatch(p -> p.getId().equals(sender.getId()));
    if (!isMember) {
        throw new ForbiddenException("Вы не являетесь участником этого чата");
    }

    Message message = new Message();
    message.setChat(chat);
    message.setSender(sender);
    message.setText(request.text());
    message.setType(request.type() != null ? request.type() : Message.MessageType.TEXT);

    messageRepository.save(message);

    MessageResponse response = toDTO(message);

    messagingTemplate.convertAndSend("/topic/chat." + chatId, response);

    return response;
}
```

Логика:

1. находится чат;
2. находится отправитель;
3. проверяется, что отправитель является участником чата;
4. создается и сохраняется сообщение;
5. сообщение конвертируется в DTO;
6. DTO отправляется всем подписчикам `/topic/chat.{chatId}`.

### 12.7. MessageService: история и прочтение сообщений

```java
// src/main/java/com/example/messenger/service/MessageService.java, строки 62-76
public Page<MessageResponse> getMessages(Long chatId, String userEmail, int page, int size) {
    Chat chat = chatRepository.findById(chatId)
        .orElseThrow(() -> new NotFoundException("Чат не найден"));

    User user = userService.getEntityByEmail(userEmail);

    boolean isMember = chat.getParticipants().stream()
        .anyMatch(p -> p.getId().equals(user.getId()));
    if (!isMember) {
        throw new ForbiddenException("У вас нет доступа к этому чату");
    }

    return messageRepository
        .findByChatIdOrderByTimestampDesc(chatId, PageRequest.of(page, size))
        .map(this::toDTO);
}
```

```java
// src/main/java/com/example/messenger/service/MessageService.java, строки 80-89
@Transactional
public void markAsRead(Long chatId, String userEmail) {
    User user = userService.getEntityByEmail(userEmail);
    messageRepository.markAllAsRead(chatId, user.getId());

    messagingTemplate.convertAndSend(
        "/topic/chat." + chatId + ".read",
        new ReadStatusEvent(chatId, user.getId())
    );
}
```

История сообщений защищена проверкой участия в чате. При прочтении сообщений отправляется отдельное WebSocket-событие на `/topic/chat.{chatId}.read`.

---

## 13. Обработка ошибок

Общая обработка ошибок находится в `GlobalExceptionHandler`.

```java
// src/main/java/com/example/messenger/exception/GlobalExceptionHandler.java, строки 15-23
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
    ) {}
```

`@RestControllerAdvice` перехватывает исключения из контроллеров и возвращает понятный JSON-ответ.

```java
// src/main/java/com/example/messenger/exception/GlobalExceptionHandler.java, строки 25-43
@ExceptionHandler(NotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
    return error(HttpStatus.NOT_FOUND, ex.getMessage());
}

@ExceptionHandler(ConflictException.class)
public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
    return error(HttpStatus.CONFLICT, ex.getMessage());
}

@ExceptionHandler(ForbiddenException.class)
public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
    return error(HttpStatus.FORBIDDEN, ex.getMessage());
}

@ExceptionHandler(BadCredentialsException.class)
public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
    return error(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
}
```

Пользовательские исключения:

| Исключение | HTTP-статус | Когда используется |
|---|---:|---|
| `NotFoundException` | 404 | Объект не найден. |
| `ConflictException` | 409 | Конфликт данных: занятый email, занятый nickname, дубликат участника. |
| `ForbiddenException` | 403 | Пользователь не имеет доступа к чату. |
| `BadCredentialsException` | 401 | Неверный email или пароль. |

Валидационные ошибки собираются в отдельный объект:

```java
// src/main/java/com/example/messenger/exception/GlobalExceptionHandler.java, строки 45-58
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
        fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    Map<String, Object> body = new HashMap<>();
    body.put("status", 400);
    body.put("error", "Ошибка валидации");
    body.put("fields", fieldErrors);
    body.put("timestamp", LocalDateTime.now());

    return ResponseEntity.badRequest().body(body);
}
```

---

## 14. Основные сценарии работы

### 14.1. Регистрация пользователя

1. Клиент отправляет `POST /api/auth/register`.
2. `AuthController` принимает `RegisterRequest`.
3. `UserService.register` проверяет email и nickname.
4. Пароль хешируется через BCrypt.
5. Пользователь сохраняется в PostgreSQL.
6. Создается JWT.
7. Клиент получает `AuthResponse`.

Пример запроса:

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "Alex",
  "nickname": "alex99",
  "email": "alex@example.com",
  "password": "123456"
}
```

Пример ответа:

```json
{
  "token": "jwt-token",
  "user": {
    "id": 1,
    "username": "Alex",
    "nickname": "alex99",
    "email": "alex@example.com",
    "online": false,
    "lastSeen": "2026-05-26T02:00:00"
  }
}
```

### 14.2. Вход пользователя

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "alex@example.com",
  "password": "123456"
}
```

После успешного входа клиент должен сохранить JWT и отправлять его в следующих запросах:

```http
Authorization: Bearer jwt-token
```

### 14.3. Создание личного чата

```http
POST /api/chats/private
Authorization: Bearer jwt-token
Content-Type: application/json

{
  "targetUserId": 2
}
```

Если личный чат уже существует, сервис вернет существующий чат вместо создания нового.

### 14.4. Создание группового чата

```http
POST /api/chats/group
Authorization: Bearer jwt-token
Content-Type: application/json

{
  "name": "Study group",
  "participantIds": [2, 3]
}
```

Текущий пользователь будет добавлен автоматически.

### 14.5. Получение истории сообщений

```http
GET /api/chats/1/messages?page=0&size=50
Authorization: Bearer jwt-token
```

История возвращается через `Page<MessageResponse>`, что удобно для больших чатов.

### 14.6. Отправка сообщения через WebSocket

Подключение:

```text
WebSocket endpoint: /ws
STOMP connect header: Authorization: Bearer jwt-token
```

Отправка:

```text
SEND /app/chat.1
```

Тело сообщения:

```json
{
  "text": "Привет!",
  "type": "TEXT"
}
```

Подписка на сообщения:

```text
SUBSCRIBE /topic/chat.1
```

Событие прочтения:

```text
SUBSCRIBE /topic/chat.1.read
```

---

## 15. Безопасность проекта

В проекте реализованы важные меры безопасности:

- пароль не хранится в открытом виде, он хешируется через BCrypt;
- JWT подписывается секретным ключом;
- защищенные endpoint-ы требуют авторизации;
- пароль не возвращается в DTO;
- доступ к сообщениям проверяется по участию пользователя в чате;
- попытка доступа к чужому чату приводит к `ForbiddenException`;
- validation-аннотации проверяют входные данные.

Пример проверки доступа к сообщениям:

```java
// src/main/java/com/example/messenger/service/MessageService.java, строки 66-72
User user = userService.getEntityByEmail(userEmail);

boolean isMember = chat.getParticipants().stream()
    .anyMatch(p -> p.getId().equals(user.getId()));
if (!isMember) {
    throw new ForbiddenException("У вас нет доступа к этому чату");
}
```

---

## 16. Текущее состояние тестов и сборки

В проекте есть один тестовый класс:

```java
// src/test/java/com/example/demo/DemoApplicationTests.java, строки 1-13
package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

Особенность: тест находится в пакете `com.example.demo`, а основное приложение находится в пакете `com.example.messenger`. Для Spring Boot тестов это может быть проблемой, потому что тестовый пакет обычно должен находиться внутри корневого пакета приложения или явно указывать класс конфигурации.

Проверка сборки была выполнена командой:

```powershell
.\mvnw.cmd test
```

Результат в текущем окружении:

```text
Cannot start maven from wrapper
```

Дополнительно команда:

```powershell
mvn test
```

не запустилась, потому что Maven не установлен глобально в окружении:

```text
mvn : Имя "mvn" не распознано...
```

То есть на момент составления отчета тесты не были успешно запущены из-за проблемы запуска Maven Wrapper / отсутствия глобального Maven в текущем окружении.

---

## 17. Замечания по текущему состоянию проекта

### 17.1. Поврежденная кодировка русских строк

В нескольких Java-файлах русские сообщения отображаются как `РџРѕР»СЊР·Рѕ...`. Это признак проблемы с кодировкой.

Пример:

```java
// src/main/java/com/example/messenger/config/SecurityConfig.java, строка 62
.orElseThrow(() -> new UsernameNotFoundException("РџРѕР»СЊР·РѕРІР°С‚РµР»СЊ РЅРµ РЅР°Р№РґРµРЅ: " + email));
```

Рекомендуется привести файлы к UTF-8, чтобы сообщения ошибок и комментарии отображались корректно.

### 17.2. UserService не использует готовый метод поиска из UserRepository

В `UserRepository` уже есть поиск по nickname на уровне базы:

```java
// src/main/java/com/example/messenger/repository/UserRepository.java, строки 19-21
@Query("SELECT u FROM User u WHERE LOWER(u.nickname) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY u.nickname")
List<User> searchByNickname(@Param("q") String q);
```

Но `UserService` сейчас делает поиск через `findAll()` и фильтрацию в памяти:

```java
// src/main/java/com/example/messenger/service/UserService.java, строки 100-105
@Transactional(readOnly = true)
public List<UserDTO> searchByNickname(String nickname) {
    return userRepository.findAll().stream()
        .filter(u -> u.getNickname().toLowerCase().contains(nickname.toLowerCase()))
        .map(this::toDTO)
        .toList();
}
```

Для небольшой базы это работает, но для большого количества пользователей лучше использовать `userRepository.searchByNickname(nickname)`.

### 17.3. Logout находится под `/api/auth/**`, который разрешен без авторизации

В `SecurityConfig` путь `/api/auth/**` полностью открыт:

```java
// src/main/java/com/example/messenger/config/SecurityConfig.java, строки 35-37
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/ws/**").permitAll()
```

При этом logout ожидает `@AuthenticationPrincipal`:

```java
// src/main/java/com/example/messenger/controller/AuthController.java, строки 32-35
@PostMapping("/logout")
public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
    userService.logout(userDetails.getUsername());
    return ResponseEntity.noContent().build();
}
```

Если запрос придет без JWT, `userDetails` может быть `null`. Лучше либо отдельно защищать `/api/auth/logout`, либо добавить проверку на `null`.

### 17.4. markAsRead не проверяет участие пользователя в чате

В `getMessages` проверка участия есть, а в `markAsRead` сейчас сразу вызывается обновление:

```java
// src/main/java/com/example/messenger/service/MessageService.java, строки 80-83
@Transactional
public void markAsRead(Long chatId, String userEmail) {
    User user = userService.getEntityByEmail(userEmail);
    messageRepository.markAllAsRead(chatId, user.getId());
```

Для большей безопасности стоит добавить такую же проверку доступа к чату, как в `getMessages`.

### 17.5. Тестовый пакет не совпадает с основным пакетом приложения

Тест лежит в `com.example.demo`, а приложение в `com.example.messenger`.

```java
// src/test/java/com/example/demo/DemoApplicationTests.java, строка 1
package com.example.demo;
```

Лучше перенести тест в пакет `com.example.messenger` или указать:

```java
@SpringBootTest(classes = MessengerApplication.class)
```

---

## 18. Что можно улучшить в дальнейшем

1. Исправить кодировку русских сообщений и комментариев на UTF-8.
2. Добавить полноценные unit-тесты для `UserService`, `ChatService`, `MessageService`.
3. Добавить integration-тесты для REST API через `MockMvc`.
4. Подключить Testcontainers для PostgreSQL в тестах.
5. Исправить запуск Maven Wrapper в текущем окружении.
6. Перенести `DemoApplicationTests` в пакет `com.example.messenger`.
7. Использовать `userRepository.searchByNickname(nickname)` вместо `findAll().stream()`.
8. Защитить `/api/auth/logout` или обработать отсутствие `AuthenticationPrincipal`.
9. Добавить проверку участия пользователя в `markAsRead`.
10. Добавить refresh-токены, если проект будет развиваться в сторону production.
11. Добавить роли пользователей, если появятся администраторы или модераторы.
12. Добавить хранение файлов/изображений для типов сообщений `IMAGE` и `FILE`.
13. Добавить индексы в базе данных для `email`, `nickname`, `chat_id`, `timestamp`.
14. Добавить Docker Compose для PostgreSQL и backend-приложения.

---

## 19. Итог

Проект Messenger представляет собой backend-основу для мессенджера, построенную на Spring Boot. В нем уже реализованы ключевые части современного серверного приложения:

- REST API;
- регистрация и вход;
- JWT-аутентификация;
- BCrypt-хеширование паролей;
- JPA-сущности и связи между таблицами;
- PostgreSQL как основная база данных;
- личные и групповые чаты;
- отправка сообщений;
- WebSocket/STOMP для real-time доставки;
- DTO-слой;
- централизованная обработка ошибок;
- Swagger/OpenAPI документация.

Главная сильная сторона проекта - правильное разделение ответственности между слоями: контроллеры не содержат бизнес-логику, сервисы управляют правилами приложения, репозитории отвечают за доступ к базе, а DTO защищают внешний API от лишних внутренних данных.

Проект уже можно использовать как основу для полноценного мессенджера. Чтобы довести его до более надежного состояния, в первую очередь стоит исправить кодировку, улучшить тесты, усилить проверки доступа и привести поиск пользователей к варианту, который выполняется на уровне базы данных.
