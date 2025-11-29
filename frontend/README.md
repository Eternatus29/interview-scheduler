# Interview Scheduler - Frontend (Vite + React)

This is a small React frontend for the Interview Scheduler backend.

Quick start

1. From the `frontend` folder install dependencies:

```powershell
cd frontend
npm install
```

2. Start the dev server (proxy is configured to `http://localhost:8080`):

```powershell
npm run dev
```

3. Open http://localhost:5173

Notes

- The Vite config proxies `/api` to `http://localhost:8080` so the frontend can call the backend without CORS changes.
- If your backend runs on a different host/port, update `vite.config.js` or start the backend at `http://localhost:8080`.

Files

- `src/components/SlotList.jsx` - list/paginate slots and open book dialog
- `src/components/GenerateSlots.jsx` - generate slots for an interviewer
- `src/components/BookSlot.jsx` - book a selected slot
- `src/utils/api.js` - small wrapper for backend calls

