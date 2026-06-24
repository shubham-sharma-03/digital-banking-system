# Enterprise Digital Banking System

A production-style Digital Banking Platform built using Spring Boot Microservices Architecture.

## Features

### Authentication & Security
- JWT Authentication
- Role Based Access Control
- Secure Login & Registration

### Account Management
- Create Accounts
- View Account Details
- Balance Tracking
- Customer Profile Management

### Fund Transfers
- Account-to-Account Transfers
- Transaction Validation
- Real-Time Balance Updates

### Card Services
- VISA / Mastercard / RuPay Cards
- Card Transaction History
- Card Blocking & Unblocking
- PIN Change Functionality
- Spending Limits & Available Balance

### Loan Management
- Home Loan
- Personal Loan
- Car Loan
- Education Loan
- Loan Status Tracking
- EMI Information

### Transaction Service
- Complete Transaction History
- Debit/Credit Tracking
- Transfer Auditing

---

## Architecture

```text
Client
   |
API Gateway
   |
------------------------------------------------
| Account Service                              |
| Auth Service                                 |
| Card Service                                 |
| Loan Service                                 |
| Transaction Service                          |
| Email Service                                |
------------------------------------------------
   |
PostgreSQL

echnology Stack
Java 17
Spring Boot
Spring Security
JWT
Spring Cloud Gateway
Eureka Server
PostgreSQL
Maven
REST APIs
HTML
CSS
JavaScript
Microservices
Account Service
Authentication Service
Card Service
Loan Service
Transaction Service
Email Service
API Gateway
Eureka Discovery Server

Project Highlights
8+ Microservices
25+ REST APIs
JWT Security
PostgreSQL Database
Service Discovery using Eureka
API Gateway Routing
Enterprise Banking Workflow

GitHub:https://github.com/shubham-sharma-03/digital-banking-system/
Linkedin:https://www.linkedin.com/in/shubhamsharma2004/
