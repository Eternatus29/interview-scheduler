const API = '/api';

// Helper function to handle API responses
async function handleResponse(res) {
    const data = await res.json();
    if (!res.ok) {
        throw new Error(data.message || 'Request failed');
    }
    return data;
}

// ==================== Interviewers ====================

export async function listInterviewers() {
    const res = await fetch(`${API}/interviewers`);
    return handleResponse(res);
}

export async function getInterviewer(id) {
    const res = await fetch(`${API}/interviewers/${id}`);
    return handleResponse(res);
}

export async function createInterviewer(payload) {
    const res = await fetch(`${API}/interviewers`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    return handleResponse(res);
}

export async function addWeeklyAvailability(interviewerId, availability) {
    const res = await fetch(`${API}/interviewers/${interviewerId}/availability`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(availability)
    });
    return handleResponse(res);
}

// ==================== Candidates ====================

export async function listCandidates() {
    const res = await fetch(`${API}/candidates`);
    return handleResponse(res);
}

export async function getCandidate(id) {
    const res = await fetch(`${API}/candidates/${id}`);
    return handleResponse(res);
}

export async function createCandidate(payload) {
    const res = await fetch(`${API}/candidates`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    return handleResponse(res);
}

// ==================== Slots ====================

export async function generateSlots(interviewerId, weeksToGenerate = 2) {
    const res = await fetch(`${API}/slots/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ interviewerId, weeksToGenerate })
    });
    return handleResponse(res);
}

export async function fetchAvailableSlots(page = 0, size = 10, interviewerId = null) {
    let url = `${API}/slots/available?page=${page}&size=${size}`;
    if (interviewerId) {
        url += `&interviewerId=${interviewerId}`;
    }
    const res = await fetch(url);
    return handleResponse(res);
}

export async function fetchSlotsCursor(cursor = 0, limit = 10, interviewerId = null) {
    let url = `${API}/slots/available/cursor?cursor=${cursor}&limit=${limit}`;
    if (interviewerId) {
        url += `&interviewerId=${interviewerId}`;
    }
    const res = await fetch(url);
    return handleResponse(res);
}

export async function getSlot(id) {
    const res = await fetch(`${API}/slots/${id}`);
    return handleResponse(res);
}

// ==================== Bookings ====================

export async function bookSlot(slotId, candidateId, bookingNotes = '') {
    const res = await fetch(`${API}/bookings`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ slotId, candidateId, bookingNotes })
    });
    return handleResponse(res);
}

export async function confirmBooking(bookingId) {
    const res = await fetch(`${API}/bookings/${bookingId}/confirm`, {
        method: 'POST'
    });
    return handleResponse(res);
}

export async function updateBooking(bookingId, payload) {
    const res = await fetch(`${API}/bookings/${bookingId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    return handleResponse(res);
}

export async function cancelBooking(bookingId) {
    const res = await fetch(`${API}/bookings/${bookingId}`, {
        method: 'DELETE'
    });
    return handleResponse(res);
}

export async function getBooking(id) {
    const res = await fetch(`${API}/bookings/${id}`);
    return handleResponse(res);
}

export async function getBookingsByCandidate(candidateId) {
    const res = await fetch(`${API}/bookings/candidate/${candidateId}`);
    return handleResponse(res);
}

export async function getBookingsByInterviewer(interviewerId) {
    const res = await fetch(`${API}/bookings/interviewer/${interviewerId}`);
    return handleResponse(res);
}

// ==================== Health ====================

export async function healthCheck() {
    const res = await fetch(`${API}/slots/health`);
    return handleResponse(res);
}
