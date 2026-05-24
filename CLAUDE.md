# GAP-FYQ - Sistema de Ejercicios de Física y Química

## Contexto del Proyecto
Aplicación local para la generación y resolución de ejercicios de Física y Química para secundaria y bachillerato (Andalucía). El proyecto escalará de forma incremental por cursos.

## Stack Tecnológico Obligatorio
- **Backend:** Java 21, Spring Boot 4.0.x (Usar la versión estable más reciente disponible en esta rama).
- **Frontend:** HTML5, Thymeleaf, HTMX (evitar frameworks pesados de JS).
- **Persistencia:** Spring Data JPA con Base de Datos H2 (en memoria / desarrollo local).
- **Seguridad:** NO implementar Spring Security en esta fase inicial.
- **Construcción:** Maven.

## Arquitectura de Negocio (Escalabilidad por Cursos)
El sistema debe estar preparado para estructurarse por cursos y materias de forma limpia:
1. 2º ESO (Física y Química integrada) <- **Fase Actual**
2. 3º ESO (Física y Química integrada)
3. 4º ESO (Física y Química integrada)
4. 1º Bachillerato (Física y Química integrada)
5. 2º Bachillerato (Separado en: Física / Química)

## Guía de Estilo y Comandos
- Idioma del código (clases, variables, métodos): Inglés.
- Idioma de los contenidos (ejercicios, interfaz, textos HTML): Español.
- Comando para compilar/testear: `./mvnw clean test`
- Comando para ejecutar: `./mvnw spring-boot:run`
