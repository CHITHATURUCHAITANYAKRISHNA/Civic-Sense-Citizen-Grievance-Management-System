# CivicSense – Citizen Grievance Management System

## 📌 Project Overview

CivicSense is a web-based Citizen Grievance Management System developed to help citizens report and track public issues such as garbage problems, water leakage, road damage, street light failures, and other civic complaints. The platform improves communication between citizens and government authorities by enabling efficient complaint management and faster issue resolution.

---

## 🚀 Features

* User Registration and Login
* Secure Authentication and Authorization
* Report Civic Issues with Images/Videos
* Complaint Status Tracking
* Department-wise Complaint Management
* Admin Dashboard
* Department Dashboard
* Notifications System
* Complaint Activity Monitoring
* Responsive User Interface
* File Upload Support
* Real-Time Issue Updates

---

## 🛠️ Technologies Used

### Frontend

* HTML
* CSS
* Thymeleaf
* JavaScript

### Backend

* Java
* Spring Boot
* Spring Security
* Spring MVC

### Database

* MySQL

### Tools & Platforms

* Eclipse IDE / VS Code
* Maven
* Git
* GitHub

---

## 📂 Project Structure

```text
src/
 ├── main/
 │   ├── java/com/civicsense/
 │   │   ├── controller/
 │   │   ├── entity/
 │   │   ├── repository/
 │   │   ├── service/
 │   │   └── config/
 │   ├── resources/
 │   │   ├── templates/
 │   │   ├── static/
 │   │   └── application.properties
 └── test/
```

---

## ⚙️ Installation Steps

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/CHITHATURUCHAITANYAKRISHNA/Civic-Sense-Citizen-Grievance-Management-System.git
```

### 2️⃣ Open the Project

Open the project in Eclipse IDE or VS Code.

### 3️⃣ Configure Database

Update your MySQL username and password inside:

```text
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/civicsense
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### 4️⃣ Run the Project

Using Maven:

```bash
mvn spring-boot:run
```

Or run the main class:

```text
CivicsenseApplication.java
```

### 5️⃣ Open in Browser

```text
http://localhost:8080
```

---

## 👥 User Roles

### Citizen/User

* Register and Login
* Report civic complaints
* Upload images/videos
* Track complaint status
* View notifications

### Department Officer

* View assigned complaints
* Update complaint status
* Monitor department activities

### Admin

* Manage users and departments
* Monitor all complaints
* Create department officers
* Access system reports

---

## 📸 Modules Included

* Authentication Module
* Complaint Management Module
* Department Management Module
* Notification Module
* User Management Module
* Admin Management Module
* Activity Monitoring Module

---

## 🎯 Objectives

* Improve communication between citizens and authorities
* Provide transparent complaint tracking
* Reduce manual complaint handling
* Increase efficiency in civic issue management
* Enable faster issue resolution

---

## 🔮 Future Enhancements

* AI-based issue classification
* Mobile Application Support
* Live Location Tracking
* SMS and Email Alerts
* Analytics Dashboard
* Multi-language Support

---

## 📄 License

This project is developed for educational and learning purposes.

---

## 👨‍💻 Developer

**Chaitanya Krishna Chithaturu**

GitHub Repository:
[https://github.com/CHITHATURUCHAITANYAKRISHNA/Civic-Sense-Citizen-Grievance-Management-System](https://github.com/CHITHATURUCHAITANYAKRISHNA/Civic-Sense-Citizen-Grievance-Management-System)
