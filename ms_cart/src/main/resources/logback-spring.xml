<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- Nombre del servicio -->
    <springProperty scope="context"
                    name="APP_NAME"
                    source="spring.application.name"/>

    <!-- Consola JSON -->
    <appender name="JSON_CONSOLE"
              class="ch.qos.logback.core.ConsoleAppender">

        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>
                {"service":"${APP_NAME}"}
            </customFields>
        </encoder>
    </appender>

    <!-- Archivo JSON -->
    <appender name="JSON_FILE"
              class="ch.qos.logback.core.rolling.RollingFileAppender">

        <file>logs/${APP_NAME}.json</file>

        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/${APP_NAME}.%d{yyyy-MM-dd}.json</fileNamePattern>
            <maxHistory>7</maxHistory>
        </rollingPolicy>

        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>
                {"service":"${APP_NAME}"}
            </customFields>
        </encoder>
    </appender>

    <!-- Root logger -->
    <root level="INFO">
        <appender-ref ref="JSON_CONSOLE"/>
        <appender-ref ref="JSON_FILE"/>
    </root>

</configuration>
