TODO
Проверить TODOs
Рефакторинг

Git:
пересоздать git-репозиторий
gitignore

Скрипты:
Рефакторинг скриптов. Сделать от root, чтобы в докере и на хосте было одинаково. не делить permissions на каждый файл, сделать общие key_permissions, crtpermissions. Абстрагирова общие функции. Абстрагировать скрипт подиси сертификатов. ПОфиксить скрипты проверки соединения. Если я проверяю соединение к БД внутри контейнера, то я могу это сделать только изнгутри контейнера, потому что на хосте предполагается, что нету psql.
sudo -E передача всех env vars
Проверить, что env vars вынесенные в файлы .env действительно используются в нескольких местах
docker скрипты был баг с изменением конфига pg_hba. Нужно добавить строчку с postgres. Перезапустить бота.
docker база данных. скрипты чтобы не один раз запускались, а каждый раз при создании контейнера
Проверить все .env, что всё это используется в скриптах
Абстрагировать permissions
Автоматизировать установку docker см установка docker для ubuntu на оф сайте через bash
Проверить, что все env vars передаются в скриптах. TIME_ZONE и HOUR_TO_REMIND не передаётся?

Java:
синглтоны защищённые мьютексами
access modifiers. мб где-то можно сделать abstract
добавить ссылку на репозиторий в about
maybe move telegram messages in files

DB:
DAO and repository per entity
вынести SQL команды в .sql файл? Удалить пробелы и мб комменты и new lines. Сделать map

maybe map result to java object (java partial object of just two columns) if i need ti return multiple columns (fileIds)
mapper classes separate
return object not list of string when multiple columns

как сделать бэкап базы данных. протестировать в контейнере.



Docker:
docker compose внимательно прочитать документацию
kubernetes
dockerignore

README:
readme. инструкция по заполнению env vars
Нарисовать архитектуру
Схему DB погуглить инстурменты для рисования
State machine
Multithreading arch
см тетрадь