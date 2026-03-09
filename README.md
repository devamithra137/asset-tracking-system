## Asset Tracking System

A web-based Asset Tracking System developed using Java Servlets, Maven, and MySQL to help organizations register, allocate, and monitor assets efficiently.
The project demonstrates full-stack development, database integration, and role-based access control.

## Motivation

Organizations often struggle with tracking physical and digital assets, leading to inefficiencies and accountability gaps.
This project aims to provide a centralized system for asset registration, allocation, and monitoring, improving transparency and operational efficiency.

## Features

Asset registration and management

Role-based dashboards (Admin, Manager, User)

Asset allocation tracking

Request management system

JSON-based API communication using Gson

Database integration with MySQL

## Tech Stack

Backend

Java Servlets

Maven

Frontend

HTML

CSS

JavaScript

Database

MySQL

Libraries

Gson

Server

Apache Tomcat

🏗 Project Architecture

Frontend: HTML, CSS, JavaScript
Backend: Java Servlets
Database: MySQL
Build Tool: Maven
Server: Apache Tomcat

📂 Project Structure
src/main/java       → Backend logic (services, servlets, utilities)
src/main/webapp     → Frontend pages and static resources
src/main/resources  → Database scripts and configuration
pom.xml             → Maven dependencies and build configuration

## How to Run
1️⃣ Clone the Repository
git clone https://github.com/yourusername/asset-trackingsystem.git
2️⃣ Build the Project
mvn clean install
mvn package
3️⃣ Deploy to Tomcat

Copy the generated WAR file into the webapps folder of your Apache Tomcat installation.

Example:

C:/apache-tomcat/webapps
4️⃣ Run the Application

Start Tomcat and open:

http://localhost:8080/assettrackingsystem

## Known Issues

Some UI buttons may not respond consistently.

Requires manual database setup before deployment.

Limited error handling in request management.


## Future Improvements

Transition to REST API architecture

Microservices-based deployment

Cloud hosting support

Enhanced authentication and role-based security


Screenshots

## System Architecture

![Architecture Diagram](docs/mermaid-diagram(1).png)

(docs/mermaid-diagram.png)

## Key Takeaways

This project highlights:

Full-stack development with Java Servlets and MySQL

Practical use of Maven for build automation

Problem-solving mindset through identified issues and planned improvements

Growth orientation with a roadmap for scalability and security