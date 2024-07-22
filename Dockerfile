FROM 893963170360.dkr.ecr.eu-central-1.amazonaws.com/spi-amazon-corretto:17.0.7

ENV LANG C.UTF-8

ARG BUILD_NUMBER
ENV BUILD_NUMBER ${BUILD_NUMBER:-dev}

WORKDIR /app
COPY ["build/libs/spi-value-analysis.jar", "app.jar"]
COPY ["src/main/resources/application-deployed.conf", "application.conf"]
CMD java \
    -XX:+UseContainerSupport \
    -jar app.jar -config=application.conf
