FROM eclipse-temurin:25-jdk

WORKDIR /app


COPY . .


RUN javac -d bin backend/src/br/fortline/mestres/Server.java

EXPOSE 8085

CMD ["java", "-cp", "bin", "br.fortline.mestres.Server"]