![Backend](https://github.com/ScherbachenyaMIK/Course-project/actions/workflows/backend.yml/badge.svg)
![Scrapper](https://github.com/ScherbachenyaMIK/Course-project/actions/workflows/scrapper.yml/badge.svg)


# AI-Powered Content Hub

Author: Scherbachenya Mikhail

A web application for publishing original articles with a commenting system.
AI provides analytics and support at all stages of content creation and publication.

The project is written in `Java 21` using `Spring Boot 3`.

The project consists of two applications:
* backend
* scrapper

The application relies on the following services to function properly:
* `Kafka 7.3.*` (at least 3 brokers)
* `Zookeeper 7.3.*`
* `Postgres 16`
* `Liquibase 4.25`
* `Prometheus *`
* `Grafana *`