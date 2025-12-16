<div align="center">

# 🏢 Room Booking Application

![Room Booking Banner](https://readme-typing-svg.herokuapp.com?font=Fira+Code&size=40&pause=1000&color=2E86AB&center=true&vCenter=true&width=800&height=100&lines=🏢+Room+Booking+System;Modern+Spring+Boot+%26+Kotlin+App;Streamline+Your+Workspace)

![Header](https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=12&height=200&section=header&text=Room%20Booking%20System&fontSize=50&fontColor=ffffff&animation=fadeIn&fontAlignY=35&desc=Spring%20Boot%20%7C%20Kotlin%20%7C%20Android&descAlignY=55&descAlign=50)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.4-brightgreen?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2024+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)

[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-success?style=for-the-badge)](https://github.com/RohithCherukuri816/RoomBookingApp-using-Kotlin)
[![Version](https://img.shields.io/badge/Version-1.0.0-blue?style=for-the-badge)](https://github.com/RohithCherukuri816/RoomBookingApp-using-Kotlin/releases)

</div>

---

## 📋 Table of Contents

- [🎯 Overview](#-overview)
- [✨ Features](#-features)
- [🏗️ Architecture](#️-architecture)
- [🛠️ Technologies](#️-technologies)
- [🚀 Getting Started](#-getting-started)
- [📱 Frontend Setup](#-frontend-setup)
- [🖥️ Backend Setup](#️-backend-setup)
- [📊 API Documentation](#-api-documentation)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)

---

## 🎯 Overview

<div align="center">
  <img src="https://media.giphy.com/media/3oKIPEqDGUULpEU0aQ/giphy.gif" width="300" alt="Room Booking Animation">
</div>

A comprehensive **Room Booking System** built with modern technologies, featuring a robust Spring Boot backend and an intuitive Android frontend. This application streamlines the process of booking meeting rooms, managing schedules, and optimizing workspace utilization.

### 🌟 Key Highlights

- 🔐 **Secure Authentication** with Spring Security
- 📱 **Modern Android UI** with Jetpack Compose
- 🔄 **Real-time Updates** via WebSocket connections
- 🗄️ **Robust Data Management** with JPA and Room Database
- 🎨 **Material Design 3** for exceptional user experience

---

## ✨ Features

<div align="center">
  <img src="https://media.giphy.com/media/26tn33aiTi1jkl6H6/giphy.gif" width="250" alt="Features Animation">
</div>

### 🏢 Room Management
- ✅ View available rooms in real-time
- 📅 Schedule bookings with calendar integration
- 🔍 Advanced search and filtering options
- 📊 Room utilization analytics

### 👥 User Management
- 🔐 Secure user authentication and authorization
- 👤 User profile management
- 🎭 Role-based access control
- 📧 Email notifications for bookings

### 📱 Mobile Experience
- 🎨 Modern Material Design 3 interface
- 🌙 Dark/Light theme support
- 📲 Offline capability with local caching
- 🔄 Real-time synchronization

---

## 🏗️ Architecture

<div align="center">
  <img src="https://via.placeholder.com/600x400/34495E/FFFFFF?text=System+Architecture+Diagram" alt="Architecture Diagram">
</div>

### Backend Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Presentation  │    │    Business     │    │   Data Access   │
│     Layer       │◄──►│     Layer       │◄──►│     Layer       │
│  (Controllers)  │    │   (Services)    │    │ (Repositories)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST APIs     │    │  Business Logic │    │   MySQL DB      │
│   WebSocket     │    │   Validation    │    │   JPA/Hibernate │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Frontend Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│       UI        │    │   ViewModel     │    │   Repository    │
│   (Compose)     │◄──►│    (MVVM)       │◄──►│   (Data Layer)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Material UI    │    │   LiveData      │    │   Room DB       │
│  Navigation     │    │   Coroutines    │    │   Retrofit      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## 🛠️ Technologies

### Backend Technologies
<div align="center">

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-data-jpa)
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge&logo=lombok&logoColor=white)](https://projectlombok.org/)
[![Gson](https://img.shields.io/badge/Gson-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://github.com/google/gson)

</div>

### Frontend Technologies
<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=material-design&logoColor=white)](https://material.io/)
[![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=for-the-badge&logo=square&logoColor=white)](https://square.github.io/retrofit/)
[![Room](https://img.shields.io/badge/Room%20DB-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Coroutines](https://img.shields.io/badge/Coroutines-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/coroutines-overview.html)

</div>

### Development Tools
<div align="center">

[![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)](https://git-scm.com/)
[![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)](https://developer.android.com/studio)
[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=for-the-badge&logo=intellij-idea&logoColor=white)](https://www.jetbrains.com/idea/)
[![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)

</div>

---

## 🚀 Getting Started

<div align="center">
  <img src="https://media.giphy.com/media/JIX9t2j0ZTN9S/giphy.gif" width="300" alt="Getting Started Animation">
</div>

### Prerequisites

Before you begin, ensure you have the following installed:

- ☕ **Java 17** or higher
- 🐘 **MySQL 8.0** or higher
- 📱 **Android Studio** (latest version)
- 🔧 **Maven 3.6** or higher
- 📦 **Git**

### Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/RohithCherukuri816/RoomBookingApp-using-Kotlin.git
   cd RoomBookingApp-using-Kotlin
   ```

2. **Set up the database**
   ```sql
   CREATE DATABASE room_booking;
   CREATE USER 'room_user'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON room_booking.* TO 'room_user'@'localhost';
   ```

3. **Configure application properties**
   ```properties
   # Backend - Spring Boot/src/main/resources/application.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/room_booking
   spring.datasource.username=room_user
   spring.datasource.password=your_password
   ```

---

## 📱 Frontend Setup

<div align="center">

[![Android Setup](https://img.shields.io/badge/Setup-Android%20App-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)

</div>

### 🔧 Installation Steps

1. **Open Android Studio**
   ```bash
   # Navigate to the frontend directory
   cd "Frontend - Kotlin"
   ```

2. **Sync Gradle Dependencies**
   - Open the project in Android Studio
   - Wait for Gradle sync to complete
   - Resolve any dependency conflicts

3. **Configure API Endpoints**
   ```kotlin
   // Update base URL in your network configuration
   const val BASE_URL = "http://your-backend-url:8080/api/"
   ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

### 📋 Key Features Implemented

- ✅ **Jetpack Compose UI** with Material Design 3
- ✅ **MVVM Architecture** with ViewModels and LiveData
- ✅ **Room Database** for offline data persistence
- ✅ **Retrofit** for API communication
- ✅ **Coroutines** for asynchronous operations
- ✅ **WebSocket** for real-time updates

---

## 🖥️ Backend Setup

<div align="center">

[![Spring Boot Setup](https://img.shields.io/badge/Setup-Spring%20Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](#)

</div>

### 🔧 Installation Steps

1. **Navigate to Backend Directory**
   ```bash
   cd "Backend - Spring Boot"
   ```

2. **Install Dependencies**
   ```bash
   mvn clean install
   ```

3. **Configure Database**
   ```properties
   # src/main/resources/application.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/room_booking
   spring.datasource.username=room_user
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```

4. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

   Or using the wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```

### 🌐 API Endpoints

The backend will be available at `http://localhost:8080`

#### 🔐 Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/logout` - User logout

#### 🏢 Room Management Endpoints
- `GET /api/rooms` - Get all rooms
- `GET /api/rooms/{id}` - Get room by ID
- `POST /api/rooms` - Create new room
- `PUT /api/rooms/{id}` - Update room
- `DELETE /api/rooms/{id}` - Delete room

#### 📅 Booking Endpoints
- `GET /api/bookings` - Get all bookings
- `POST /api/bookings` - Create new booking
- `PUT /api/bookings/{id}` - Update booking
- `DELETE /api/bookings/{id}` - Cancel booking

---

## 📊 API Documentation

<div align="center">
  <img src="https://media.giphy.com/media/3oKIPnAiaMCws8nOsE/giphy.gif" width="250" alt="API Documentation">
</div>

### 📋 Request/Response Examples

#### Create Room Booking
```http
POST /api/bookings
Content-Type: application/json
Authorization: Bearer {token}

{
  "roomId": 1,
  "userId": 123,
  "startTime": "2024-01-15T09:00:00",
  "endTime": "2024-01-15T10:00:00",
  "purpose": "Team Meeting",
  "attendees": 5
}
```

#### Response
```json
{
  "id": 456,
  "roomId": 1,
  "userId": 123,
  "startTime": "2024-01-15T09:00:00",
  "endTime": "2024-01-15T10:00:00",
  "purpose": "Team Meeting",
  "attendees": 5,
  "status": "CONFIRMED",
  "createdAt": "2024-01-14T15:30:00"
}
```

---

## 🤝 Contributing

<div align="center">
  <img src="https://media.giphy.com/media/du3J3cXyzhj75IOgvA/giphy.gif" width="300" alt="Contributing Animation">
</div>

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add some amazing feature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request**

### 📝 Contribution Guidelines

- Follow the existing code style
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed
- Ensure all tests pass before submitting

---

## 📄 License

<div align="center">

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

</div>

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

### 🌟 Show Your Support

If you found this project helpful, please give it a ⭐️!

[![GitHub stars](https://img.shields.io/github/stars/RohithCherukuri816/RoomBookingApp-using-Kotlin?style=social)](https://github.com/RohithCherukuri816/RoomBookingApp-using-Kotlin/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/RohithCherukuri816/RoomBookingApp-using-Kotlin?style=social)](https://github.com/RohithCherukuri816/RoomBookingApp-using-Kotlin/network)

---

**Made with ❤️ by [Rohith Cherukuri](https://github.com/RohithCherukuri816)**

<img src="https://media.giphy.com/media/LnQjpWaON8nhr21vNW/giphy.gif" width="60"> <em><b>Happy Coding!</b></em>

</div>