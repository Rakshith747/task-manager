# Team Task Manager

A full-stack web app built with **Spring Boot + React + MySQL**.
This is a BTech-level project demonstrating JWT authentication, role-based access, and CRUD operations.

---

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.2, Spring Security, JPA/Hibernate
- **Frontend**: React 18, React Router, Axios
- **Database**: MySQL 8
- **Auth**: JWT (stateless)

---

## Project Structure

```
team-task-manager/
├── backend/               # Spring Boot application
│   └── src/main/java/com/taskmanager/
│       ├── controller/    # REST controllers
│       ├── service/       # Business logic
│       ├── repository/    # JPA repositories
│       ├── model/         # JPA entities
│       ├── dto/           # Data transfer objects
│       ├── security/      # JWT + Spring Security config
│       └── exception/     # Global error handler
├── frontend/              # React app
│   └── src/
│       ├── pages/         # Login, Signup, Dashboard, Projects, ProjectDetail
│       ├── components/    # Navbar
│       ├── context/       # Auth context (React Context API)
│       └── services/      # Axios API config
└── database_setup.sql     # SQL schema (optional - Hibernate auto-creates)
```

---

## Setup Instructions

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

---

### Step 1 – Database Setup

```sql
-- In MySQL shell or workbench
CREATE DATABASE taskmanagerdb;
```

Or just run:
```bash
mysql -u root -p < database_setup.sql
```

---

### Step 2 – Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/taskmanagerdb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD   # <-- change this
```

---

### Step 3 – Run Backend

```bash
cd backend
mvn spring-boot:run
```

Backend runs at: `http://localhost:8080`

---

### Step 4 – Run Frontend

```bash
cd frontend
npm install
npm start
```

Frontend runs at: `http://localhost:3000`

The `"proxy": "http://localhost:8080"` in package.json handles API routing automatically.

---

## API Endpoints

### Auth
| Method | URL | Description |
|--------|-----|-------------|
| POST | /api/auth/signup | Register new user |
| POST | /api/auth/login | Login and get JWT |

### Projects
| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/projects | Get all projects |
| POST | /api/projects | Create project (ADMIN) |
| DELETE | /api/projects/{id} | Delete project (ADMIN) |
| POST | /api/projects/{id}/members | Add member (ADMIN) |
| DELETE | /api/projects/{id}/members/{uid} | Remove member (ADMIN) |
| GET | /api/projects/users | List all users (ADMIN) |

### Tasks
| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/projects/{id}/tasks | Get tasks for project |
| POST | /api/projects/{id}/tasks | Create task (ADMIN) |
| PUT | /api/tasks/{id}/status | Update task status |
| PUT | /api/tasks/{id} | Edit task (ADMIN) |
| DELETE | /api/tasks/{id} | Delete task (ADMIN) |
| GET | /api/dashboard | Dashboard stats |

---

## Features by Role

### ADMIN
- Create and delete projects
- Add/remove project members
- Create, edit, delete tasks
- Assign tasks to members
- View all projects and tasks
- Dashboard with stats

### MEMBER
- View assigned projects only
- View own assigned tasks
- Update status of own tasks (TODO → IN_PROGRESS → DONE)
- Dashboard showing personal stats

---

## Notes
- Passwords are hashed using BCrypt
- JWT tokens expire in 24 hours
- Hibernate auto-creates/updates tables on startup
- CORS is configured for localhost:3000

---

## Known Limitations (by design)
- No pagination
- No email verification
- No password reset feature
- Basic error messages
- No unit tests included
