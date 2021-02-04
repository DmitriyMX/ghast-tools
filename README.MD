# GHAST TOOLS

![version: 1.12](https://img.shields.io/badge/version-1.12-0a0.svg?style=flat)
![bukkit-api: 1.12](https://img.shields.io/badge/bukkit--api-1.12-d50.svg?style=flat)

Набор вспомогательных инструментов для Bukkit API.  
_Основан на версии Bukkit API 1.12._

## Содержание

1. [Перед использованием](#перед-использованием)
2. [GhastTools](#ghasttools)
3. [AssetsManager](#assetsmanager)
4. [BuildHelper](#buildhelper)
5. [CommandManager](#commandmanager)
6. [EffectsHelper](#effectshelper)
7. [EventContext](#eventcontext)
8. [JdbcTemplate](#jdbctemplate)
9. [ScheduleManager](#schedulemanager)
10. [ScheduleTask](#scheduletask)
11. [Messages](#messages)
12. [XLog](#xlog)
13. [Подключение](#подключение)
    1. [Gradle](#gradle)
    2. [Maven](#maven)

---

## Перед использованием

Перед началом использования, необходимо в вашем Bukkit-плагине прописать подобный код:

```java
void onLoad() {
    GhastTools.setPlugin(this);
}
```

Это необходимо сделать, т.к. весь инструментарий основан на статических (static) методах.

Все методы данного набора инструментов объеденены в классы, выполняющие роль группировщиков. 

## GhastTools

Методы общего назначения или без определённой группировки.

### loadConfig

Загрузка файла настроек плагина - `config.yml`.

```java
YamlConfiguration config = GhastTools.loadConfig();
```

По-умолчанию "гаст" пытается файл найти в папке плагина - `getDataFolder()`.
Если файла там нет, то выгружает встроенный (имеющийся в `.jar` файле плагина) в эту папку и загружает его.

```java
YamlConfiguration config = GhastTools.loadConfig(false);
```

Если передать параметр `false`, то при отсутствии файла `config.yml` в папке плагина,
будет загруден исключительно встроенный файл настроек.

### copyLocation

Копирования объекта `Location`

```java
Location location = ...;
Location copyLoc = GhastTools.copyLocation(location); 
```

## AssetsManager

Методы по работе с файлами плагина (_"ассетами"_).

У каждой группы методов один и тот же набор входных параметров:

- `resourceName` - наименование и путь к файлу в папке плагина
- `defaultResourceName` - наименование и путь к файлу в плагине.  
  _Опционально. По-умолчанию равнен `resourceName`_
- `saveDefault` - необходимость скопировать содержимое файла из `defaultResourceName` в файл `resourceName`.  
  _Опционально. По-умолчанию равен `true`_
  
Правила поиска файлов так же одинаков для каждой группы:

- В начале файл ищется в папке плагина
- Если файл отсутствует в папке плагина, то...
    - если `defaultResourceName` не равен `null`...
        - если `saveDefault` равен `true`, то файл из плагина будет выгружен в папку плагина и от туда загружен в память.
        - если `saveDefault` равен `false`, то данные будут взяты из файлв в плагине.
    - если `defaultResourceName` равен `null`, то будет брошено исключение `AssetsException` с описанием ошибки вида "Файл X не найден".

### getAsInputStream

```java
InputStream inputStream = AssetsManager.getAsInputStream("translate.ru.yml", "translate.yml", false);
```

### getAsReader

```java
Reader reader = AssetsManager.getAsReader("translate.ru.yml", "translate.yml", false);
```

### getAsString

```java
String string = AssetsManager.getAsString("readme.txt", "readme.txt", StandardCharsets.UTF_8, false);
```

У этой группы есть дополнительный _опциональный_ параметр - `charset` - в котором указывается кодиривка получаемой строки.  
_По-умолчанию равен `StandardCharsets.UTF_8`_

## BuildHelper

Набор методов облегчающих размецение объектов на карте.

### placeSkull

Установка черепа.

```java
Location location = ...;
Skull skull = BuildHelper.placeSkull(location, BlockFace.NORTH);
skull.update(true); // иначе изменения на карте не применятся и череп будет висеть в воздухе
```

### placePlayerHead

Установка головы игрока.

```java
Location location = ...;
Skull playerHead = BuildHelper.placePlayerHead(location, BlockFace.NORTH);
playerHead.update(true); // иначе изменения на карте не применятся и голова будет висеть в воздухе
```

Если третьим параметром передать URL текстуры, то голова будет текстурирована.

```java
Location location = ...;
BuildHelper.placePlayerHead(location, BlockFace.NORTH, "http://...");
```

### setPlayerHeadSkin

Установка текстуры для головы игрока.

```java
Location location = ...;
Skull playerHead = BuildHelper.placePlayerHead(location, BlockFace.NORTH);
playerHead.update(true); // иначе изменения на карте не применятся и голова будет висеть в воздухе
setPlayerHeadSkin(playerHead);
```

Порядок выполнения методов в приведённом выше примере **важен**. Если `playerHead.update(true)` вызвать после
установки текстуры, она собъётся на стандартную.

### placeSignWall

```java
Location location = ...;
Sign signWall = BuildHelper.placeSignWall(location, BlockFace.NORTH)
```

## CommandManager

Регистрация команд.

Имеется два варианта использования: упрощённый

```java
CommandManager.register("start", (sender, args) -> sender.sendMessage("hello!"));
```

и подробный:

```java
CommandManager.create("start")
        .useOnlyPlayer()
        .executer((sender, args) -> sender.sendMessage("hello!"))
        .register();
```

### register

Упрощенная регистрация команды. Указывается лишь название команды и исполнитель.

```java
CommandManager.register("start", (sender, args) -> sender.sendMessage("hello!"));
```

### create

Конструктор для подробного варианта регистрации команды.

```java
CommandManager.Builder builder = CommandManager.create("start");
```

### executer

Указание исполнителя для команды

```java
CommandManager.Builder builder = CommandManager.create("start")
        .executer((sender, args) -> sender.sendMessage("hello!"));
```

### onError

Обработчик исключений

```java
CommandManager.Builder builder = CommandManager.create("start")
        .onError((sender, commandName, args, exception) -> {
            sender.sendMessage(ChatColor.RED + "Произошла ошибка при выполнении команды '" + commandName + "'.");
            exception.printStackTrace();
        });
```

### useOnlyPlayer

Указание, что данную команду могут использовать только Игроки.
Опционально можно указать сообщение, которое будет выводиться в консоль.  
_Отменяет действие указателя `useOnlyConsole`_

```java
CommandManager.Builder builder = CommandManager.create("start")
        .useOnlyPlayer("Команду могут использовать только игроки");
```

### useOnlyConsole

Указание, что данную команду можно использовать только в консоле.
Опционально можно указать сообщение, которое будет выводиться Игроку.  
_Отменяет действие указателя `useOnlyPlayer`_

```java
CommandManager.Builder builder = CommandManager.create("start")
        .useOnlyConsole(ChatColor.RED + "Команду можно использовать только в консоли");
```

### register

Регистрация описанной в Конструкторе команды.

```java
CommandManager.create("start")
        .useOnlyPlayer()
        .executer((sender, args) -> sender.sendMessage("hello!"))
        .register();
```

## EffectsHelper

Набор методов для работы с эффектами.

### playSound

Воспроизвести звук.

```java
Location location = ...;
EffectsHelper.playSound(location, Sound.AMBIENT_CAVE, 1.0f);
```

### particle

Создание частиц.

```java
Location location = ...;
EffectsHelper.particle(location, Particle.REDSTONE, 1.0d, 1.0d, 1.0d, 1.0d, 5);
```

## EventContext

Регистрация группы обработчиков событий, объединённых общим условием выполнения.

```java
EventContext.create()
        .filter(() -> Bukkit.getOnlinePlayers().size() > 10)
        .onEvent(PlayerJoinEvent.class, event -> { event.getPlayer().kickPlayer("Max players"); });
```

### create

Создание контекста событий.

```java
EventContext eventContext = EventContext.create();
```

### filter

Условие, при котором будут срабатывать обработчики событий в данном контексте.

```java
EventContext.create()
        .filter(() -> Bukkit.getOnlinePlayers().size() > 10)
```

### onEvent

Указание события и его обработчика.  
_Обработчик события регистрируется сразу же._

```java
EventContext.create()
        .filter(() -> Bukkit.getOnlinePlayers().size() > 10)
        .onEvent(PlayerJoinEvent.class, event -> event.getPlayer().kickPlayer("Max players"));
```

### cancelEvent

Отменить событие.  
_Обработчик события регистрируется сразу же._

```java
EventContext.create()
        .cancelEvent(BlockPlaceEvent.class);
```

эквивалентен коду:

```java
EventContext.create()
        .onEvent(BlockPlaceEvent.class, event -> event.setCancelled(true))
```

## JdbcTemplate

Инструмент для упрощения работы с SQL базами данных, работающими через JDBC.

Для начала потребуется создать объект `DataSource`

```java
// На примере MySQL
MysqlDataSource dataSource = new MysqlDataSource();
dataSource.setServerName("localhost");
dataSource.setPort(3306);
dataSource.setCharacterEncoding(StandardCharsets.UTF_8.name());
dataSource.setDatabaseName("MyDataBase");
dataSource.setUser("root");
dataSource.setPassword("secret");
```

После чего создать `JdbcTemplate`

```java
JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
```

### execute

Выполнение SQL запроса без последующей обработки результатов выполнения.

```java
jdbcTemplate.execute("CREATE TABLE my_table (id int, name varchar(16));");
```

### query

Выполнение SQL запроса и обработка его результатов.  
Может возвращать любой тип объектов. 

```java
String name = jdbcTemplate.query("SELECT name FROM my_table LIMIT 0,1", resultSet -> {
        if (resultSet.next()) {
            return resultSet.getString("name");
        } else {
            return null;
        }
});
```

```java
List<String> names = jdbcTemplate.query("SELECT name FROM my_table", resultSet -> {
        if (resultSet.next()) {
            List<String> list = new ArrayList<>();
            do {
                list.add(resultSet.getString("name"));
            } while (resultSet.next());
            return list;
        } else {
            return Collections.emptyList();
        }
});
```

### queryOne

Выполнение SQL запроса с расчетом, что результат будет единичным либо не будет вовсе.  
Возвращает `Optional`.

```java
Optional<String> optName = jdbcTemplate.queryOne("SELECT name FROM my_table WHERE name LIKE 'dmitriymx'", rs -> rs.getString("name"));
```

### queryList

Выполнение SQL запроса и обработка результата как списка данных.  
Возвращает `List`.

```java
List<String> names = jdbcTemplate.queryList("SELECT name FROM my_table", (resultSet, rowNum) -> resultSet.getString("name"));
```

### queryForMap

Выполнение SQL запроса с расчетом, что результат будет единичным либо не будет вовсе.  
Возвращает `Map<String, Object>`, где ключ — это наименование колонок таблицы, а значения — это значения в ячейках таблицы.

```java
Map<String, Object> map = jdbcTemplate.queryForMap("SELECT * FROM my_table LIMIT 0,1;");
```

### queryForMapList

Выполнение SQL запроса и обработка результата как списка данных.  
Возвращает `List<Map<String, Object>>`, где ключ — это наименование колонок таблицы, а значения — это значения в ячейках таблицы.

```java
List<Map<String, Object>> mapList = jdbcTemplate.queryForMapList("SELECT * FROM my_table");
```

### update

Выполнение SQL запроса где будет происходить обновление данных в таблице.
Под "обновлением" подразумеваются любые изменения в таблице: `UPDATE`, `DELETE`, `INSERT`.  
Возвращает число строк, которые были _по факту обновлены_ в таблице.

```java
int rows = jdbcTemplate.update("DELETE FROM my_table WHERE name LIKE 'dmitriymx';");
```

## ScheduleManager

Набор методов для создания параллельных задач, выполняющихся один раз или по рассписанию.

### createTask

Создание конструктора задачи.

```java
ScheduleManager.Builder builder = ScheduleManager.createTask();
```

### useBukkitScheduler

Если задача будет взаимодействоватьс **Bukkit API** или необходима привязка задачи к _тикам_,
то необходимо использовать данный указатель.

В ином случае, указатель не нужен.

```java
ScheduleManager.Builder builder = ScheduleManager.createTask()
        .useBukkitScheduler();
```

### after

Указание, что задачу нужно выполнить не сразу, а с некоторой задержкой перед запуском.

```java
ScheduleManager.Builder builder = ScheduleManager.createTask()
        .after(5, TimeUnit.MINUTES);
```

### every

Указание, что задачу нужно повторять через указанное время.

```java
ScheduleManager.Builder builder = ScheduleManager.createTask()
        .every(5, TimeUnit.MINUTES);
```

### create

Создание описанной задачи.

```java
ScheduleTask scheduleTask = ScheduleManager.createTask()
        .every(1, TimeUnit.SECONDS)
        .create(() -> Bukkit.getServer().getLogger().info("TimeMS: " + System.currentTimeMillis()));
```

_Задача будет только создана. Для её выполнения нужно вызвать `scheduleTask.start()`._

### execute

Создание и выполнение описанной задачи.

```java
ScheduleTask scheduleTask = ScheduleManager.createTask()
        .every(1, TimeUnit.SECONDS)
        .execute(() -> Bukkit.getServer().getLogger().info("TimeMS: " + System.currentTimeMillis()));
```

## ScheduleTask

Вспомогательный объект, созданный через `ScheduleManager`. Позволяет управлять созданной задачей.

### start

Запускает задачу, если она еще не запущена.

```java
ScheduleTask scheduleTask = ...;
scheduleTask.start();
```

### isCanceled

Возвращает состояние задачи. Если `true`, значит задача была или _отменена/остоновлена_ или была завершена.

```java
ScheduleTask scheduleTask = ...;
boolean status = scheduleTask.isCanceled();
```

### cancel

Отменяет/Остонавливает выполнение задачи.

```java
ScheduleTask scheduleTask = ...;
scheduleTask.cancel();
```

## Messages

Инструмент для работы с параметизированными сообщениями или просто сообщениями, которые храняться в отдельном файле.

Параметизированные сообщения имеют следующий вид: `Привет, {player}!`.

### load

Загрузка сообщений в инструмент.

Есть три варианта: через `Properties`

```java
Properties properties = ...;
Messages.load(properties);
```

через `Map<String, String>`

```java
Map<String, String> map = ...;
Messages.load(map);
```

через `Reader`

```java
Reader reader = ...;
Messages.load(reader);
```

Следует учесть, про при работе через `Reader`, **Messages** ожидает там обнаружить список строк в формате `key=value`.

### get

Получение обычноего или параметизированного сообщения.

Для примера, пусть у нас будут такие сообщения:

```properties
simple=Простое сообщение
welcome=Приветствуем, {player}!
```

Для получения простого сообщения, просто указываем его ключ:

```java
String message = Messages.get("simple");
// Простое сообщение
```

Для получения параметизированного сообщения, нужно помимо ключа передать параметры.
Есть два способа: через `Map<String, Object>`

```java
Map<String, Object> = map = new HashMap<>(1);
map.put("player", "David");

String message = Messages.get("welcome", map);
// Приветствуем, David!
```

через попарное перечисление параметров

```java
String message = Messages.get("welcome", "player", "David");
// Приветствуем, David!
```

Если по указанному ключу сообщение отсутствует, то **Messages** вернёт значение самого ключа

```java
String message = Messages.get("not_exists_key");
// not_exists_key

message = Messages.get("not_exists_key", "player", "David");
// not_exists_key
```

Если параметр, который указан в шаблоне не был указан/определён, то параметр останется как есть

```java
String message = Messages.get("welcome");
// Приветствуем, {player}!

message = Messages.get("welcome", "unknown_param_key", 123);
// Приветствуем, {player}!
```



## XLog

Замена стандартному `getLogger()`, который использует `java.utils.Logger` и не всегда удобен для логирования.

Имеет 4 уровня логирования: `debug`, `info`, `warning`, `error`.

Сообщения могут быть шаблонизированными. Синтаксис шаблонов — `java.text.MessageFormat`.

Примеры:

```java
XLog.info("Hello");
XLog.info("Player {0} join game", event.getPlayer().getName());

XLog.error("ERROR!", exception);
XLog.error("ERROR: {0}", exception.getMessage());
// Для экранирования "{" нужно перед ней поставить "'".
// А для использования "'" нужно их дублировать.
XLog.error("ERROR ''{0}'' in Event '{{1}'}: {2}", exception.getClass(), event.getClass(), exception.getMessage());
XLog.error("ERROR: {0}", exception.getMessage(), exception);
```

---

## Подключение

### Gradle

```groovy
repositories {
    maven { url 'https://dmx-mc-project.gitlab.io/maven-repository/' }
}
```

```groovy
implementation group: 'ghast', name: 'ghast-tools', version: '1.12'
```

### Maven

```xml
<repositories>
    <repository>
        <id>dmx-mc-project</id>
        <url>https://dmx-mc-project.gitlab.io/maven-repository/</url>
    </repository>
</repositories>
```

```xml
<dependencies>
    <dependency>
        <groupId>ghast</groupId>
        <artifactId>ghast-tools</artifactId>
        <version>1.12</version>
    </dependency>
</dependencies>
```