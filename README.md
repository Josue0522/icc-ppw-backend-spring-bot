# Práctica 10: Paginación de Productos con Spring Data JPA

## 1. Tema

Frameworks Backend: Spring Boot – Paginación con `Page`, `Slice` y `Pageable`.

En esta práctica se implementó paginación en los endpoints de productos usando Spring Data JPA. El objetivo principal fue evitar que la API devuelva todos los registros en una sola respuesta cuando existen muchos productos almacenados en la base de datos.

También se aplicó paginación al endpoint de productos por categoría, manteniendo los filtros implementados en la práctica anterior.

---

## 2. Objetivo

Implementar paginación en consultas de productos usando:

- `Page`
- `Slice`
- `Pageable`
- `PageRequest`
- `Sort`
- `@ModelAttribute`
- Validación de parámetros de paginación
- Consultas personalizadas con `@Query`

Además, se buscó validar los parámetros recibidos desde la URL y aplicar paginación directamente en el repositorio para mejorar el rendimiento de la API.

---

## 3. Problema identificado

Antes de aplicar paginación, el endpoint:

```txt
GET /api/products