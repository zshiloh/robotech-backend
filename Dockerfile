# Imagen base de Java
FROM openjdk:17-jdk-slim

# Copia los archivos del proyecto
COPY . /app
WORKDIR /app

# Construir el JAR (usa Maven Wrapper si lo tienes)
RUN ./mvnw clean package -DskipTests

# Exponer el puerto dinámico que Render usa
EXPOSE 8080

# Comando de ejecución (Render asigna $PORT)
CMD ["java", "-jar", "target/robotech-api-0.0.1-SNAPSHOT"]
