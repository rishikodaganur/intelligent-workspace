# ⚡ Intelligent Workspace (NEW_NET)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_pgvector-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![WebSockets](https://img.shields.io/badge/WebSockets-010101?style=for-the-badge&logo=socket.io&logoColor=white)
![Gemini](https://img.shields.io/badge/Google_Gemini-8E75B2?style=for-the-badge&logo=google-gemini&logoColor=white)
![Groq](https://img.shields.io/badge/Groq_Llama_3.3-f55036?style=for-the-badge&logo=groq&logoColor=white)

A production-ready, real-time AI collaboration environment. This application bridges a premium glassmorphic frontend with a highly concurrent Spring Boot backend, featuring intelligent dual-model routing, live WebSockets, and a custom Retrieval-Augmented Generation (RAG) pipeline powered by PostgreSQL's `pgvector`.

**[🔗 Access the Live Server]** *(<- Link your Railway/Render deployment here once live)*

---

## 🧠 Core Architecture & Features

### 1. Dual-Model AI Routing & Fallback System
Engineered for 100% uptime and bypass rate-limiting. The backend dynamically routes natural language queries and image processing tasks between **Groq (Llama 3.3)** for ultra-low latency inference and **Google Gemini Flash** for complex multimodal processing.

### 2. Custom RAG Engine (Vectorized Memory)
Documents (PDFs) uploaded to the workspace are instantly parsed, chunked, and vectorized. Embeddings are stored in a **PostgreSQL** database using the `pgvector` extension, allowing the AI to perform semantic similarity searches and answer complex questions based strictly on user-uploaded context.

### 3. Real-Time WebSocket Infrastructure
Built on **STOMP over SockJS**, the application handles concurrent user sessions with zero-latency data transmission.
* **Live Presence Tracking:** Real-time updates of active users in a specific room.
* **Typing Indicators & Read Receipts:** Broadcasted asynchronously via dedicated socket channels.

### 4. Sliding-Window Conversational Memory
The backend maintains contextual state without overwhelming the LLM context window. It dynamically queries the database for the most relevant recent interactions before dispatching payloads to the AI models.

### 5. Premium, Responsive UI/UX
A custom-built, dependency-free vanilla frontend leveraging modern CSS architecture.
* Adaptive glassmorphism with `backdrop-filter` rendering.
* Dynamic viewport scaling and mathematical optical centering for flawless mobile and split-screen rendering.
* Client-side image compression (HTML5 Canvas API) before server upload to optimize bandwidth.

---

## 🛠️ Technical Stack

**Frontend:**
* HTML5, CSS3 (Custom Variables, Flexbox, CSS Grid)
* Vanilla JavaScript (ES6+)
* Highlight.js (Syntax Highlighting), Marked.js (Markdown Parsing)

**Backend:**
* Java 17+
* Spring Boot (Spring Web, Spring Data JPA, Spring WebSocket)
* Groq API, Google Gemini API

**Database & Infrastructure:**
* PostgreSQL + `pgvector` extension
* Amazon S3 (or compatible object storage for assets)

---

## 🚀 Local Setup & Installation

### Prerequisites
* Java 17 or higher
* Maven
* PostgreSQL running locally with the `pgvector` extension enabled.

### 1. Clone the Repository
```bash
git clone [https://github.com/yourusername/intelligent-workspace.git](https://github.com/rishikodaganur/intelligent-workspace.git)
cd intelligent-workspace
