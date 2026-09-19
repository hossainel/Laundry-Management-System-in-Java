# Laundry Management System

A comprehensive point-of-sale (POS) system built with **JavaFX** for managing laundry operations, including customer management, service pricing, product inventory, order tracking, and payment processing.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Key Modules](#key-modules)
- [Database Tables](#database-tables)
- [Contributing](#contributing)

## 🎯 Overview

This is a desktop application designed to manage all aspects of a laundry business, from customer interactions to inventory and financial tracking. The system provides an intuitive UI for staff and administrators to process orders, track services, manage inventory, and handle payments.

**Main Application Class:** `com.lms.laundry.view.LaundryApplication`

## ✨ Features

- **User Management**
  - Admin and Staff roles
  - User authentication and login system
  - Active/inactive user status management

- **Customer Management**
  - Create and manage customer profiles
  - Track phone numbers, email, and address
  - Customer balance view

- **Service Management**
  - Service categories (Men, Women, Kids, Household)
  - Service types (Wash, Iron, Dry Clean, Wash & Iron)
  - Dynamic pricing based on service and type
  - Service availability control

- **Product Inventory**
  - Product categories (Cleaning, Packaging, Accessories, Laundry Supplies)
  - Barcode support for quick product lookup
  - Cost price and selling price tracking
  - Stock management

- **Order Processing**
  - Create orders with services and products
  - Multiple order statuses (RECEIVED, WASHING, READY, DELIVERED)
  - Flexible delivery date scheduling
  - Order item tracking with snapshot pricing

- **Payment Management**
  - Multiple payment methods (Cash, Card, Bkash, Nagad, Rocket, Bank Transfer)
  - Payment tracking and history
  - Customer due amount calculation

- **Financial Tracking**
  - Expense management with categories
  - Expense categorization (Electricity, Water Bill, Salary, etc.)
  - Payment method tracking for expenses

- **System Settings**
  - Configurable shop information (name, phone, address)
  - Currency and language settings
  - Invoice generation with custom prefix
  - Tax configuration
  - Terms and conditions

## 🛠 Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **UI Framework** | JavaFX | 21.0.6 |
| **Build Tool** | Maven | 3.x |
| **Database** | MySQL | 8.x+ |
| **JDBC Driver** | MySQL Connector/J | 9.3.0 |
| **Connection Pooling** | HikariCP | 5.1.0 |
| **Logging** | SLF4J | 1.7.36 |
| **Testing** | JUnit Jupiter | 5.12.1 |
| **Java Version** | JDK 21 |

### Additional Libraries

- **ControlsFX** (11.2.1) - JavaFX controls and dialogs
- **FormsFX** (11.6.0) - Form building framework
- **ValidatorFX** (0.6.1) - Input validation
- **Ikonli** (12.3.1) - Icon library with FontAwesome
- **BootstrapFX** (0.4.0) - Bootstrap CSS for JavaFX
- **TilesFX** (21.0.9) - Tile-based layouts

## 📁 Project Structure

```
Laundry-Management-System/
├── src/
│   └── main/
│       ├── java/
│       │   ├── com/lms/laundry/          # Main application package
│       │   ├── module-info.java          # Module configuration
│       │   └── ...
│       ├── resources/                    # FXML files, CSS, images
│       ├── .env                          # Environment configuration
│       └── db.sql                        # Database schema and seed data
├── pom.xml                               # Maven configuration
├── mvnw / mvnw.cmd                       # Maven wrapper scripts
└── README.md                             # This file
```

## 🗄 Database Schema

The system uses MySQL with the following main tables:

### User Management
- `fx_users` - System users with roles (Admin/Staff)

### Customer & Service
- `fx_customers` - Customer information and contact
- `fx_service_categories` - Service categories
- `fx_service_types` - Service types
- `fx_services` - Available services
- `fx_service_prices` - Pricing for service/type combinations

### Inventory
- `fx_product_categories` - Product categories
- `fx_products` - Inventory products with barcode and stock tracking

### Orders & Transactions
- `fx_orders` - Customer orders with status and dates
- `fx_order_items` - Line items in orders (services or products)
- `fx_payments` - Payment records
- `fx_payment_methods` - Available payment methods
- `fx_expenses` - Business expenses
- `fx_expense_categories` - Expense categorization

### Configuration
- `fx_settings` - System settings and configuration

### Views
- `fx_view_customer_balances` - Customer balance summary view

## 📦 Installation

### Prerequisites

- **Java Development Kit (JDK) 21** or higher
- **MySQL Server** 8.0 or higher
- **Maven** 3.6+ (or use the included Maven wrapper)

### Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/hossainel/Laundry-Management-System.git
   cd Laundry-Management-System
   ```

2. **Set up the database:**
   ```bash
   mysql -u root -p < src/main/db.sql
   ```

3. **Install dependencies:**
   ```bash
   mvn clean install
   ```

## ⚙️ Configuration

Configure your database connection in `src/main/.env`:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=fx_laundry
DB_USER=root
DB_PASSWORD=your_password
```

### Default Credentials

- **Username:** admin
- **Password:** admin123
- **Role:** Administrator

**⚠️ Important:** Change the default password after first login.

## ▶️ Running the Application

### Using Maven

```bash
# Using the JavaFX Maven plugin
mvn javafx:run

# Or compile and package
mvn clean package
```

### From IDE

- Open the project in IntelliJ IDEA, Eclipse, or NetBeans
- Run `LaundryApplication.java` as a JavaFX Application

## 🔧 Key Modules

| Module | Purpose |
|--------|---------|
| **view** | JavaFX UI components and controllers |
| **model** | Data models and entity classes |
| **dao** | Data Access Objects for database operations |
| **util** | Utility functions and helpers |
| **service** | Business logic and service layer |
| **config** | Configuration and environment setup |

## 📊 Database Tables Reference

### fx_users
Stores user accounts with authentication and role management.

### fx_customers
Customer contact and profile information.

### fx_orders
Order details including status, dates, and payment information:
- Status: RECEIVED → WASHING → READY → DELIVERED

### fx_order_items
Line items in orders supporting both services and products with snapshot pricing.

### fx_service_prices
Dynamic pricing matrix for services based on type (e.g., Shirt - Wash: ৳50, Iron: ৳30).

### fx_products
Inventory items with barcode, stock levels, and pricing.

### fx_payments
Payment transaction records linked to customers and payment methods.

### fx_expenses
Business expense tracking with categories and payment methods.

## 📝 License

This project is open source. Check the repository for license information.

## 🤝 Contributing

Contributions are welcome! Please feel free to:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Support

For issues, questions, or suggestions, please open an issue in the repository.

---

**Last Updated:** September 2026  
**Version:** 1.0-SNAPSHOT  
**Repository:** [Laundry Management System](https://github.com/hossainel/Laundry-Management-System-in-Java)
