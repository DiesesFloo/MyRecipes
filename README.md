# 🍳 MyRecipes

<div align="center">

### Intelligent Recipe Platform powered by Spring Boot Microservices

*A scalable recipe backend featuring personalized recommendations, event-driven architecture, and high-performance data storage.*

</div>

---

## 📖 Overview

**MyRecipes** is a modern recipe platform built with a **microservice architecture** using **Spring Boot**.

The project focuses on delivering personalized recipe recommendations through a recommendation engine that analyzes user cooking behavior, ratings, and recipe completion history.

The system is designed to be highly scalable, event-driven, and cloud-native from the beginning.

---

## 🏗️ Architecture

```text
┌─────────────────┐
│     Client      │
│ Web / Mobile    │
└────────┬────────┘
         │
         │ GraphQL
         ▼
┌─────────────────┐
│ API Layer       │
│ GraphQL Gateway │
└────────┬────────┘
         │
 ┌───────┼───────────────┐
 │       │               │
 ▼       ▼               ▼

User   Recipe    Recommendation
Svc    Svc       Svc

 │       │           │
 └───────┼───────────┘
         │
         ▼

      Kafka

         │
         ▼

  ScyllaDB + Redis
```

### Communication

| Layer              | Technology   |
| ------------------ | ------------ |
| External API       | GraphQL      |
| Service-to-Service | gRPC         |
| Event Streaming    | Apache Kafka |
| Caching            | Redis        |
| Persistence        | ScyllaDB     |

---

## 🧠 Recommendation System

The recommendation service uses **user-based collaborative filtering**.

Recommendations are generated from:

* User cooking history
* Completed recipes
* Recipe ratings
* Similar user behavior patterns

### Example

If User A and User B have cooked and rated many of the same recipes similarly:

```text
User A:
- Pasta ★★★★★
- Pizza ★★★★☆
- Curry ★★★★★

User B:
- Pasta ★★★★★
- Pizza ★★★★☆
- Curry ★★★★★
- Ramen ★★★★★

=> Recommend Ramen to User A
```

Future improvements may include:

* Hybrid recommendation models
* Content-based filtering
* Ingredient preference analysis
* Machine learning models
* Seasonal recommendations

---

## 🛠️ Tech Stack

| Category     | Technology        |
| ------------ | ----------------- |
| Language     | Java (Latest LTS) |
| Framework    | Spring Boot       |
| API          | GraphQL           |
| RPC          | gRPC              |
| Messaging    | Apache Kafka      |
| Database     | ScyllaDB          |
| Cache        | Redis             |
| Build Tool   | Maven             |
| Architecture | Microservices     |


---

## 🤝 Contributing

Contributions, ideas, and feedback are welcome.

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Open a pull request

---

## 📜 License

This project is open-source and available under the **[MIT License](https://github.com/DiesesFloo/MyRecipes/blob/main/LICENSE)**.

---

<div align="center">

Built with ❤️ using Spring Boot, Kafka, GraphQL, gRPC, ScyllaDB, and Redis.

</div>
