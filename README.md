# meme-aggregator
Конкурсное задание для https://funcodechallenge.com/

## Стек
* java11
* kotlin
* ktor
* mongodb 4.22
* s3 
* micrometer

## Конфигурация и переменные окружения
основные:
* DB_HOST - хост для подключения к mongodb
* DB_PORT - порт для подключения к mongodb
* DB_NAME - название БД
* SERVER_PORT - порт api сервера
* MAX_SOURCES_COUNT - максимальное количество источников для одной ноды
* S3_ACCESS_KEY - accessKey для s3
* S3_SECRET_KEY - secretKey для s3
* S3_ENDPOINT - адрес сервера s3
* S3_BUCKET - букет для статики (создается автоматически)

дополнительные:
* WRITER_BATCH_SIZE - размер батча для записи в БД
* WRITER_WAIT_TIME_SEС - минимальное время ожидания батча записи в БД
* MAX_CONCURRENT_UPLOADS - максимальное количество одновременных аплоадов в s3 
* MAX_CONCURRENT_DOWNLOADS - максимальное количество одновременных загрузок
* HTTP_SOCKET_TIMEOUT - таймаут TCP пакетов
* HTTP_CONNECT_TIMEOUT - таймаут на установку HTTP соединения
* HTTP_CONNECTION_REQUEST_TIMEOUT - таймаут на ожидание начала HTTP соединения
* HTTP_MAX_CONN_TOTAL - максимальное число HTTP-соединений
* HTTP_MAX_CONN_PER_ROUTE - максимальное число HTTP-соединений на один хост
* CRAWL_LOG_LEVEL - уровень логирования для краулера

## Сборка и запуск
в корне проекта выполнить 
```
docker run --rm -it $(docker build -q .)
```

## Конфигурация источников
Конфигурация лежит config/sources.conf
* 9GAG Tag (type=ngag_tag) - краулинг по тегу
* 9GAG Group (type=ngag_group) - краулинг по группе
* Reddit (type=reddit) - краулинг по сабреддитам
* Debeste (type=debeste) - постраничный краулинг с http://debeste.de/ 
* Orschlurch(type=orschlurch) - постраничный краулинг с https://de.orschlurch.net/ 

## Распределение источников между нодами
Реализовано самым примитивнейшим способом. Вся синхронизация нод через БД.
 
 Перед стартом каждая нода читает свой конфиг и обновляет общий список источников в БД. Затем нода пытается "захватить" максимальное количество источников из общего списка. Максимальное число источников задается переменной MAX_SOURCES_COUNT. Так для конфигурации из 6 источников и 3 нод MAX_SOURCES_COUNT=2.  

## API

Лента контента доступна через GET /feed.
Параметры:
* count - число постов в выдаче (default=10)
* after - курсор для постраничного вывода (значение подставлять из nextAfter в выдаче)

Пост с метаданными доступен через GET /feed/{id}


## Метрики
endpoint для Prometheus доступен через /metrics

Метрики:
* Коробочные micrometer (memory, cpu etc)
* crawler_writer_itemsrate - скорость записи в БД (items per sec)
* crawler_writer_requestsrate - число запросов на запись в БД (requests per rate)
* crawler_downloader_payloadsize - размер скачанного контента в kb
* crawler_downloader_downloadspeed - скорость закачки контента в kb/s
* crawler_uploader_payloadsize - размер закачанного контента в s3 в kb
* crawler_uploader_uploadspeed - скорость аплоада контента в kb/s
* crawler_source_queuesize - размер очереди источника 
* crawler_source_rate - скорость краулинга источника (items per sec)
* crawler_source_itemcount - количество скрауленного контента
