const API = '/api'

export async function fetchSlots(page = 0, size = 10) {
    const res = await fetch(`${API}/slots/available`)
    if (!res.ok) throw new Error('Failed to fetch slots')
    return res.json()
}

export async function generateSlots(interviewerId, slotTimes) {
    const opts = { method: 'POST', headers: {} }
    if (slotTimes && slotTimes.length) {
        opts.body = JSON.stringify(slotTimes)
        opts.headers['Content-Type'] = 'application/json'
    }
    const res = await fetch(`${API}/slots/generate/${interviewerId}`, opts)
    if (!res.ok) throw new Error('Failed to generate slots')
    return res.text()
}

export async function bookSlot(slotId, candidateId) {
    const res = await fetch(`${API}/slots/book?slotId=${slotId}&candidateId=${candidateId}`, { method: 'POST' })
    if (!res.ok) {
        const text = await res.text()
        throw new Error(text || 'Failed to book')
    }
    return res.text()
}

export async function cursorSlots(cursor = 0, limit = 10) {
    const res = await fetch(`${API}/slots/cursor?cursor=${cursor}&limit=${limit}`)
    if (!res.ok) throw new Error('Failed to fetch cursor slots')
    return res.json()
}

export async function addInterviewer(payload) {
    const res = await fetch(`${API}/interviewers`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
    if (!res.ok) throw new Error('Failed to add interviewer')
    return res.json()
}

export async function listInterviewers() {
    const res = await fetch(`${API}/interviewers`)
    if (!res.ok) throw new Error('Failed to fetch interviewers')
    return res.json()
}
