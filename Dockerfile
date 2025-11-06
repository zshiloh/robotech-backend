# Imagen base con JDK 17 (no 21)
FROM openjdk:17-jdk-slim

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el proyecto completo
COPY . .

# Dar permisos de ejecución al Maven Wrapper y compilar el proyecto
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Exponer el puerto dinámico que Render asigna
EXPOSE 8080

# Comando para ejecutar la app
CMD ["java", "-jar", "target/autoland-0.0.1-SNAPSHOT.jar"]
