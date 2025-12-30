# Department Management Platform

A full-stack department and workforce management solution composed of a Spring Boot backend and a Next.js/Tailwind CSS frontend. The project provides department CRUD operations, staffing oversight, and employee lifecycle management with an interface that is ready to deploy to Vercel and backed by an in-memory H2 database for rapid iteration.

## Project Layout

```
backend/   # Spring Boot 3.3 REST API with JPA, validation, and seed data
frontend/  # Next.js 16 app router UI with Tailwind CSS and API integration
```

## Requirements

- Java 17+
- Node.js 18+ with npm

## Running Locally

### Backend API

```bash
cd backend
./gradlew bootRun
```

The API is served on `http://localhost:8080` with the H2 console exposed at `/h2-console`. Seed data provides Engineering, HR, and Finance departments plus sample employees.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The UI is available on `http://localhost:3000`. It expects the backend at `http://localhost:8080`. Override the endpoint using the `NEXT_PUBLIC_API_BASE_URL` environment variable if needed.

## Testing & Quality

- `cd backend && ./gradlew test` – runs JPA slice and integration tests covering core workflows.
- `cd frontend && npm run lint` – executes ESLint with Next.js defaults.
- `cd frontend && npm run build` – validates the production build.

## Deployment Notes

Deploy the frontend to Vercel by running `vercel deploy --prod --yes --token <token> --name agentic-7e0059d8` from the `frontend` directory. Ensure the backend is hosted separately (e.g., Fly.io, Railway, Render, or traditional hosting) and expose its URL via the `NEXT_PUBLIC_API_BASE_URL` environment variable.

---

This project was assembled autonomously by an AI coding agent to satisfy the requirement: “Make a Java based Department management app.” Feel free to extend it with authentication, reporting dashboards, or persistence beyond the bundled H2 database.
