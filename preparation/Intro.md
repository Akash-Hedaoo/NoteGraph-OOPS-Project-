# Three-Minute Project Introduction

## Why
I built NoteGraph to solve a common problem I faced with personal knowledge management: the tension between strict hierarchical organization and lateral, network-based discovery. Traditional tools force you into silos, making it difficult to see how ideas across different domains intersect. NoteGraph exists to remove that friction, allowing users to organize knowledge systematically in folders while visualizing cross-connections through tags in an interactive graph.

## What
NoteGraph is a connected, full-stack note-taking application. The core user journey revolves around creating workspaces, categorizing rich-text notes into folders, and applying cross-cutting tags. The most important feature is the Knowledge Graph—a 3D interactive visualization that dynamically maps the relationships between your folders, tags, and notes. I also incorporated an experimental AI feature that evaluates note complexity in the background and schedules them for spaced-repetition review. Currently, the project is a fully functional MVP with comprehensive CRUD operations, JWT-based authentication, and a robust relational data model.

## How
The high-level architecture is a monolithic React single-page application communicating with a Node.js and Express backend, backed by a MySQL database using Prisma as the ORM. On the frontend, I leveraged Three.js to render the complex graph visualizations smoothly. 

One of my strongest engineering decisions was selecting Prisma alongside MySQL. Because the core value of the application relies on deeply relational data—workspaces owning folders and tags, and notes having complex many-to-many relationships—Prisma provided the type-safe relational integrity I needed while keeping developer velocity high. 

A meaningful technical challenge was integrating the AI revision scheduler. I initially implemented this using `node-cron` running directly inside the Express server to poll for new notes and call the Mistral or Gemini APIs. While this kept the architecture simple and avoided external dependencies like Redis, the trade-off is that it tightly couples background processing to the API lifecycle, preventing horizontal scaling.

## What Now
Currently, NoteGraph is a robust portfolio project, but it has a few gaps before it can be considered production-grade. The largest limitation is exactly that background job architecture—running `node-cron` in-process means background tasks could be lost if the server restarts, and we can't scale the API instances without running duplicate cron jobs. 

My most valuable next improvement would be extracting the AI scheduler into a dedicated background worker using a message queue like BullMQ and Redis. Over the next six months, I’d like to see NoteGraph evolve to support real-time collaborative editing via WebSockets and introduce paginated graph queries to handle massive, knowledge-dense workspaces efficiently.

***

# Thirty-Second Version

Tell me briefly about your project:

"NoteGraph is a full-stack, connected note-taking app that bridges hierarchical folders with network-based tags. I built it to solve the problem of information silos in personal knowledge management. It features a React and Three.js frontend for 3D graph visualization, communicating with a Node.js and Express backend. I used Prisma and MySQL to enforce complex relational data integrity, and I integrated Mistral and Gemini APIs for background spaced-repetition scheduling. While it’s currently a robust MVP, my next major step for production readiness is decoupling the in-process cron jobs into a dedicated Redis message queue to allow the API to scale horizontally."

***

# Key Points to Remember

- **The Problem:** Breaking down information silos in traditional note-taking.
- **The Solution:** Combining strict folder hierarchies with cross-cutting tags and 3D graph visualization.
- **Tech Stack:** React, Vite, Three.js, Node.js, Express, Prisma, MySQL.
- **Strongest Decision:** Using Prisma/MySQL to guarantee data integrity for highly relational graph data.
- **Major Limitation:** Background AI jobs run in-process via `node-cron`, blocking horizontal scaling.
- **Future Roadmap:** Extract background tasks to Redis/BullMQ and add real-time collaboration.
