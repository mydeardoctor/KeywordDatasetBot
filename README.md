TODO
Проверить TODOs

Git:
пересоздать git-репозиторий

Скрипты:
Рефакторинг скриптов. Сделать от root, чтобы в докере и на хосте было одинаково. не делить permissions на каждый файл, сделать общие key_permissions, crtpermissions. Абстрагирова общие функции. Абстрагировать скрипт подиси сертификатов. ПОфиксить скрипты проверки соединения. Если я проверяю соединение к БД внутри контейнера, то я могу это сделать только изнгутри контейнера, потому что на хосте предполагается, что нету psql.
sudo -E передача всех env vars
Проверить, что env vars вынесенные в файлы .env действительно используются в нескольких местах
docker скрипты был баг с изменением конфига pg_hba. Нужно добавить строчку с postgres. Перезапустить бота.
docker база данных. скрипты чтобы не один раз запускались, а каждый раз при создании контейнера
Проверить все .env, что всё это используется в скриптах
Абстрагировать permissions
Автоматизировать установку docker см установка docker для ubuntu на оф сайте через bash
Папка для логов в скриптах

Java:
синглтоны защищённые мьютексами
java keystore
добавить команду /about, которая будет показывать git version, автора
макс время жирным шрифтом, emoji
абстрагировать мапперы через <T>?
Сделать команду /about с коммитом версии и автором
В дескрипшн в botfather добавить "для подробной информации используйте команду /help"
Логировать в папку для логов (/var/logs или /opt/logs), а не в папку с проектом
access modifiers. мб где-то можно сделать abstract
добавить emoji
в статистике юзер ридабл нейм

Thread pool:
thread pool bechmark

DB:
пробовать соединиться с базой данных при инициализации, пока не получится
в базе данных duration поменять на duration_rounded_up_seconds
вынести SQL команды в .sql файл?
database connection pool hikari
приложение ждёт пока получится соединение с БД. Если не получается, то выход.
connection pool benchmark
как сделать бэкап базы данных. протестировать в контейнере.

Docker:
docker compose внимательно прочитать документацию
docker compose приложение запускается только после успешного health check базы данных, т.е. проверки соединения на уровне docker compose.
docker/docker compose secrets.
kubernetes

Unit tests:
Сделать

CI/CD:
сравнить md5 прошивки внутри контейнера и на хосте

README:
readme. инструкция по заполнению env vars
Нарисовать архитектуру
Схему DB погуглить инстурменты для рисования
State machine
Multithreading arch
см тетрадь

























из вордовского файла:
MAVEN JAR
Maven убрать SNAPSHOT когда закончу

Application properties? Do they work in jar?

Поставить postgresql 17
Как в sql запускать скрипт из файла .sql
Сделать таблицы, чтобы было O(1)

В бот фазер настроить команды.Global commands and custom commands
Сделать

Как в телеграм боте сохранить состояния диалога?
В базе данных сделать таблицу юзер и таблицу войс мессадж метадата
Оптимизировать SQL запросы, как работает SQL база данных в плане алгоритмов
Операции с базой данных в другом потоке? Каждый апдейт в другом потоке?

Параллелизм
Сделать executorservice.newfixedthreadpool. Класть в очередь задачи. Сделать сет с обрабатываемыми юзерами, который защищён мьютексом/synchronized. thread-safe data structures concurrentset? Либо сделать кастомную обёртку с synchronized методами
Что предлагает spring? TaskExecutor, ThreadPoolTaskExecutor 

Как реализован параллелизм в postgresql? Как поддерживает параллелизм jdbctemplate? Нужно ли создавать один объект или несколько?

Как реализован параллелизм в telegram client? Нужно ли создавать okhttpclient несколько раз или достаточно одного?

Send chat action потому что бот долго думоет
https://core.telegram.org/bots/api#sendchataction
Эмоджи
https://rubenlagus.github.io/TelegramBotsDocumentation/lesson-4.html

Перегитировать проект, чтобы убрать из истории токены

Докер?
Запустить на raspberry pi и сохранять аудио на sd карту!!!


TODOs

docker
Unit tests

Как завершить прогу gracefully? Springapp exit, system,exit()

Мониторить http чтобы понять как рабоатет

Maven

Добавить shutdown hook где закрываются все ресурсы и проверить работает ли он при Ctrl+C

Postgresql соединения и транзакции



Теория. Как работает JVM?
Написать код
Автоформаттер. Какой? IntelliJ? Или есть другой форматте для Java?
Unit-тестирование. Какое? JUnit?
Статический анализатор. Какой? PVS-Studio?
gitignore
REAME с описанием порядка действий. Архитектура.
Docker. Для портативности.

Теория. Как работает Postgresql.
Практика. Leetcode SQL.
Написать код.
README.
Docker. ДЛя портативности. База данных как volume? В другом контейнере или в том же, что и приложение? Можно ли запустить докер на распберри, а главное нужно ли?

C++ port


README:
- git clone. repository is needed for maven build because tag and version.
- build with intellij. select configuration

Будет ли IntelliJ сохранять настройки конфигураций запсука, если я туда пропишу енв переменные?

----------------------------------------------

okhttp
connection pooling?
async?
----------------------------------------------

TODOS
-----------------------------------------------
jdk 23?


properties?
what if env vars in intellij run configurations



queue for producer so that dont block updates?
thread pool cant i bock on queue?












there can be suppressed exceptions
catch runtime exceptions at least to log them


unhandled exception kills only one thread. 
Thread.setDefaultUncaughtExceptionHandler. Логирование. kill all threads? System.exit? graceful shutdown

В пуле если поток завершается нормально, то поток переиспользуется.
В пуле если происходит exception, то поток умирает, а вместо него создаётся новый. Если execute, то сработает общий хэндлер. Если submit, не сработает. Wrapper runnable to handle 
Если в пуле не запущен ни один поток, то программа заканчивается.
В пуле если потоки заканчивают работу нормально, то программа не заканчивается. Потому что сами потоки остались.
exceptions. Extend thread pool and afterexecute. terminated.
Что делать, если в пуле произошло exception. Убивать все потоки неправильно. А если произошло прерывание ctrl+c? То стоит сделать общий executor.shutdown?


System.exit initiates JVM shutdown.
Ctrl+C = SIGINT initiated JVM shutdown.
Если начался shutdown не по нормальному (т.е. когда нажали команду System.exit или CTRL+C), то начинают выполнятся shutdown hooks, но потоки приложения тоже продолжают выполняться.
Runtime.getRuntime.addShutdownHook. Must be threadsafe. synchronized?
is it thread safe to threadpool.shutdown in jvm shutdown hook?
Нельзя вызывать System.exit внутри shutdown hook, потому что метод блокирующий, т.е. shutdown hook не выполнится, т.е. shutdown не произойдёт.





THREAD
ARE THEY PLATFORM (kernel) OR VIRTUAL (user)
what are they in thread pool?
https://docs.oracle.com/en/java/javase/21/core/concurrency.html
virtual threads?
thread poll that blocks on queue should just use a counting semaphore
