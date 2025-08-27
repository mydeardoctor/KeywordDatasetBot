TODO
Проверить TODOs
Рефакторинг

Git:
пересоздать git-репозиторий
gitignore

Скрипты:
host/ca docker/ca/ generate_ca_crt.sh
host/database/ docker/ca/ generate_database_server_csr.sh
host/database docker/ca generate_database_admin_csr
host/app docker/ca generate_app_csr
host/ca docker/ca sign_database_servers_csr
host/ca docker/ca sign_database_admins_csr
host_ca docker/ca sign_app_csr

обобщить скрипты т.е. одни и теже для docker и host
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
синглтоны
access modifiers. мб где-то можно сделать abstract
добавить ссылку на репозиторий в about

DB:
DAO and repository рефакторинг
как сделать бэкап базы данных. протестировать в контейнере.

Docker:
kubernetes
dockerignore

README:
readme. инструкция по заполнению env vars
Нарисовать архитектуру см тетрадь
-Схему DB погуглить инстурменты для рисования
-State machine
-Multithreading arch