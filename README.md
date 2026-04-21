# 📝 NoteGraph

A **connected note-taking application** that lets you organize notes into workspaces, tag them with sections, and visualize their relationships through an interactive Note Graph.

Built as an OOP Project demonstrating clean architecture, CRUD operations, JWT authentication, and modern full-stack development.

---

## ✨ Features

### 📂 Workspaces
- Create multiple workspaces to organize projects separately
- Rename and delete workspaces with full cascading cleanup
- Switch between workspaces via the sidebar dropdown

### 📝 Rich Text Editor
- Full formatting toolbar — Bold, Italic, Underline, Headings, Lists, Checklists, Links, Images
- Auto-save as you type
- Assign notes to workspaces and sections via custom styled dropdowns
- Toggle notes as favorites with a star

### 🏷️ Tags & Sections
- Create color-coded tags to categorize notes
- Add/remove tags directly from the editor
- Notes can belong to multiple sections, creating rich cross-connections
- Rename and delete sections from the Note Graph page

### 🔗 Note Graph
- **Org Chart View** — hierarchical visualization of workspace → tags → notes
- **Tree List View** — collapsible tree with sections and sub-notes
- Connection details sidebar showing link density and connected notes

### ⭐ Favorites
- Star important notes for quick access
- Dedicated Favorites page with search

### 📤 Export
- Export individual notes as **HTML, Markdown, PDF, or Word (.doc)**
- Export entire **workspaces** or individual **sections** as styled HTML documents
- One-click download buttons on the Note Graph page

### 📖 User Guide
- Built-in interactive guide with 6 collapsible sections
- Quick-start banner and step-by-step instructions
- Covers all features: Workspaces, Notes, Tags, Graph, Favorites, and Pro Tips

### 🔐 Authentication
- JWT-based authentication with Spring Security
- User registration and login
- Data isolation — each user sees only their own data

---

## 🛠️ Tech Stack

### Frontend
| Technology | Version | Purpose |
|-----------|---------|---------|
| React | 19.2 | UI framework |
| Vite | 7.3 | Build tool & dev server |
| React Router | 7.13 | Client-side routing |
| Axios | 1.13 | HTTP client |
| Lucide React | 0.577 | Icon library |

### Backend
| Technology | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 4.0.3 | REST API framework |
| Spring Security | — | Authentication & authorization |
| Spring Data JPA | — | Database ORM |
| JWT (jjwt) | 0.11.5 | Token-based auth |
| MySQL | 8.0 | Database |
| Lombok | — | Boilerplate reduction |
| Java | 17 | Language |

---

## 🏗️ Project Structure

```
NoteGraph-OOPS-Project/
├── Backend/                          # Spring Boot API
│   ├── src/main/java/com/notegraph/api/
│   │   ├── controller/               # REST Controllers
│   │   │   ├── AuthController.java
│   │   │   ├── NoteController.java
│   │   │   ├── TagController.java
│   │   │   ├── WorkspaceController.java
│   │   │   ├── GraphController.java
│   │   │   └── ActivityController.java
│   │   ├── service/                  # Business Logic
│   │   ├── repository/              # JPA Repositories
│   │   ├── domain/                  # Entity Models
│   │   │   ├── User.java
│   │   │   ├── Workspace.java
│   │   │   ├── Note.java
│   │   │   ├── Tag.java
│   │   │   └── ActivityLog.java
│   │   ├── dto/                     # Data Transfer Objects
│   │   └── security/               # JWT & Security Config
│   └── pom.xml
│
├── Frontend/                         # React SPA
│   ├── src/
│   │   ├── pages/                   # Page Components
│   │   │   ├── Login.jsx
│   │   │   ├── DashboardOverview.jsx
│   │   │   ├── Editor.jsx
│   │   │   ├── NoteHierarchy.jsx
│   │   │   ├── TagsPage.jsx
│   │   │   ├── FavoritesPage.jsx
│   │   │   └── GuidePage.jsx
│   │   ├── components/              # Reusable Components
│   │   │   ├── layout/              # Sidebar, DashboardLayout
│   │   │   └── shared/              # Modals, ConfirmDialog
│   │   ├── context/                 # Auth Context
│   │   └── services/                # API Service (Axios)
│   └── package.json
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- **Java 17** or later
- **Node.js 18+** and npm
- **MySQL 8.0** database (or a cloud-hosted instance)

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/NoteGraph-OOPS-Project.git
cd NoteGraph-OOPS-Project
```

### 2. Setup Backend

Configure your MySQL connection in `Backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://your-host:port/your-database
spring.datasource.username=your-username
spring.datasource.password=your-password
spring.jpa.hibernate.ddl-auto=update
```

Start the backend server:
```bash
cd Backend
./mvnw spring-boot:run
```
> The API will start on `http://localhost:8080`

### 3. Setup Frontend

Install dependencies and start the dev server:
```bash
cd Frontend
npm install
npm run dev
```
> The app will open at `http://localhost:5173`

### 4. Open the App

Navigate to `http://localhost:5173` in your browser. Register a new account and start creating notes!

---

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login & get JWT token |

### Notes
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notes/workspace/{id}` | Get all notes in workspace |
| GET | `/api/notes/{id}` | Get note by ID |
| POST | `/api/notes` | Create a new note |
| PUT | `/api/notes/{id}` | Update a note |
| PUT | `/api/notes/{id}/favorite` | Toggle favorite status |
| POST | `/api/notes/{noteId}/tags/{tagId}` | Add tag to note |
| DELETE | `/api/notes/{noteId}/tags/{tagId}` | Remove tag from note |
| DELETE | `/api/notes/{id}` | Delete a note |

### Tags
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tags/workspace/{id}` | Get all tags in workspace |
| POST | `/api/tags` | Create a new tag |
| PUT | `/api/tags/{id}` | Update a tag |
| DELETE | `/api/tags/{id}` | Delete a tag |

### Workspaces
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/workspaces` | Get user's workspaces |
| POST | `/api/workspaces` | Create a workspace |
| PUT | `/api/workspaces/{id}` | Rename a workspace |
| DELETE | `/api/workspaces/{id}` | Delete a workspace |

### Graph
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/graph/{workspaceId}` | Get graph data for workspace |

---

## 🧪 OOP Principles Demonstrated

| Principle | Implementation |
|-----------|---------------|
| **Encapsulation** | Entity classes with private fields and public getters/setters via Lombok |
| **Inheritance** | Spring's component model — `@Service`, `@Repository`, `@Controller` |
| **Polymorphism** | `UserDetailsService` implementation for Spring Security |
| **Abstraction** | Repository interfaces abstracting database operations |
| **Composition** | `Note` contains `Set<Tag>`, `Workspace` contains `User` owner |
| **SRP** | Separate Controller → Service → Repository layers |

---

## 📄 License

This project is for educational purposes as part of an OOP course project.
