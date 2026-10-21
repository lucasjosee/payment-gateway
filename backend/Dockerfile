FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# O pom.xml e o wrapper entram antes do código para as dependências ficarem numa camada só delas. Enquanto o pom.xml
# não mudar, o Docker reaproveita essa camada e não baixa tudo de novo a cada alteração no código.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -B -q dependency:go-offline

COPY src src
# Os testes ficam de fora porque precisam do Docker (Testcontainers) e do banco do docker-compose, que não existem
# aqui dentro. Eles rodam antes, com ./mvnw test.
RUN ./mvnw -B -q package -DskipTests

# A imagem final parte do zero com só o JRE e o jar. O JDK, o Maven, o código-fonte e as dependências baixadas
# ficam no estágio de build e não vão junto.
FROM eclipse-temurin:21-jre
WORKDIR /app

# Sem isto a aplicação rodaria como root, e uma falha explorada nela teria controle total do container.
RUN groupadd --system app && useradd --system --gid app app
USER app

COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
