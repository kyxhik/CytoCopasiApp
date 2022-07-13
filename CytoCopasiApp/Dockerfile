FROM maven:3.6-jdk-11 as maven_build

WORKDIR /home/people/hkaya/eclipse-emre/java-2021-03/eclipse/git/CytoCopasiApp

#copy pom
COPY pom.xml .

#resolve maven dependencies


#copy source
COPY src ./src

# build the app (no dependency download here)
RUN mvn -U package  -Dmaven.test.skip -Djava.library.path= /home/people/hkaya/Cytoscape_v3.9.1/framework/lib

# split the built app into multiple layers to improve layer rebuild
RUN mkdir -p target/docker-packaging && cd target/docker-packaging && jar -xf ../cytocopasiapp.jar

########JRE run stage########
FROM openjdk:11.0-jre
WORKDIR /home/people/hkaya/eclipse-emre/java-2021-03/eclipse/git/CytoCopasiApp

#copy built app layer by layer
ARG DOCKER_PACKAGING_DIR=/app/target/docker-packaging
COPY --from=maven_build ${DOCKER_PACKAGING_DIR}/BOOT-INF/lib /home/people/hkaya/eclipse-emre/java-2021-03/eclipse/git/CytoCopasiApp
COPY --from=maven_build ${DOCKER_PACKAGING_DIR}/BOOT-INF/classes /app/classes
COPY --from=maven_build ${DOCKER_PACKAGING_DIR}/META-INF /app/META-INF

