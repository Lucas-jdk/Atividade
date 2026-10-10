# Usa uma imagem oficial do Java (OpenJDK 17)
FROM eclipse-temurin:25-jdk

# Define a pasta de trabalho dentro do servidor
WORKDIR /app

# Copia todos os ficheiros do seu projeto para lá
COPY . .

# Compila o ficheiro Server.java
RUN javac -d bin Server.java

# Expõe a porta do servidor
EXPOSE 8085

# Executa o servidor Java
CMD ["java", "-cp", "bin", "br.fortline.mestres.Server"]