# API Documentation

**版本**: 1.0.0

**生成时间**: 2026-04-29 21:42:07

**接口总数**: 4

---

## createUser

- **接口路径**: `POST /api/user`
- **所属类**: UserController
- **方法名**: createUser

---

## deleteUser

- **接口路径**: `DELETE /api/user//{id}`
- **所属类**: UserController
- **方法名**: deleteUser

### 请求参数

| 参数名 | 类型 | 位置 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Long | path | 是 | id |

---

## listUsers

- **接口路径**: `GET /api/user//list`
- **所属类**: UserController
- **方法名**: listUsers

### 请求参数

| 参数名 | 类型 | 位置 | 必填 | 说明 |
|--------|------|------|------|------|
| page | int | query | 是 | page |
| size | int | query | 是 | size |

---

## getUser

- **接口路径**: `GET /api/user//{id}`
- **所属类**: UserController
- **方法名**: getUser

### 请求参数

| 参数名 | 类型 | 位置 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Long | path | 是 | id |

---

