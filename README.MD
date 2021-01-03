# GHAST TOOLS

Набор вспомогательных инструментов для Bukkit API.  
_Основан на версии Bukkit API 1.12._

## Использование

Перед началом использования, необходимо в вашем Bukkit-плагине прописать подобный код:

```java
void onLoad() {
    GhastTools.setPlugin(this);
}
```

Это необходимо сделать, т.к. весь инструментарий основан на статических (static) методах.

Все методы данного набора инструментов объеденены в классы, выполняющие роль группировщиков. 

### GhastTools

Методы общего назначения или без определённой группировки.

#### loadConfig

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

### AssetsManager

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

#### getAsInputStream

```java
InputStream inputStream = AssetsManager.getAsInputStream("translate.ru.yml", "translate.yml", false);
```

#### getAsReader

```java
Reader reader = AssetsManager.getAsReader("translate.ru.yml", "translate.yml", false);
```

#### getAsString

```java
String string = AssetsManager.getAsString("readme.txt", "readme.txt", StandardCharsets.UTF_8, false);
```

У этой группы есть дополнительный _опциональный_ параметр - `charset` - в котором указывается кодиривка получаемой строки.  
_По-умолчанию равен `StandardCharsets.UTF_8`_

### BuildHelper

Набор методов облегчающих размецение объектов на карте.

#### placeSkull

```java
Location location = ...;
Skull skull = BuildHelper.placeSkull(location, BlockFace.NORTH)
```

#### placeSignWall

```java
Location location = ...;
Sign signWall = BuildHelper.placeSignWall(location, BlockFace.NORTH)
```

### CommandManager

Регистрация комманд.

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

_TODO добавить описание подробного режима_

## Подключение

### Gradle

```groovy
repositories {
    maven { url 'https://dmx-mc-project.gitlab.io/maven-repository/' }
}
```

```groovy
implementation group: 'ghast', name: 'ghast-tools', version: '1.9'
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
        <version>1.9</version>
    </dependency>
</dependencies>
```