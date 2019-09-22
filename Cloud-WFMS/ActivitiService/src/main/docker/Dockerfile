#http://dockone.io/article/5932;
# 需要设置让jdk感知到docker的限制选项
#FROM frolvlad/alpine-oraclejdk8:slim
FROM adoptopenjdk/openjdk8:slim
VOLUME /tmp
ADD activiti-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8763
ENV JAVA_OPTS="\
-XX:+UnlockExperimentalVMOptions \
-XX:+UseCGroupMemoryLimitForHeap \
-Xmx100m"
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
ENTRYPOINT java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.jar
