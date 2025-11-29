import React, { useEffect, useState } from 'react'
import { fetchAvailableSlots, listInterviewers } from '../utils/api'

export default function SlotList({ onBookSlot }) {
    const [slots, setSlots] = useState([])
    const [interviewers, setInterviewers] = useState([])
    const [page, setPage] = useState(0)
    const [size] = useState(10)
    const [totalPages, setTotalPages] = useState(0)
    const [selectedInterviewer, setSelectedInterviewer] = useState('')
    const [loading, setLoading] = useState(false)

    useEffect(() => {
        loadInterviewers()
    }, [])

    useEffect(() => {
        loadSlots()
    }, [page, selectedInterviewer])

    async function loadInterviewers() {
        try {
            const res = await listInterviewers()
            setInterviewers(res.data || [])
        } catch (err) {
            console.error('Failed to load interviewers:', err)
        }
    }

    async function loadSlots() {
        setLoading(true)
        try {
            const interviewerId = selectedInterviewer || null
            const res = await fetchAvailableSlots(page, size, interviewerId)
            const data = res.data || {}
            setSlots(data.data || [])
            setTotalPages(data.totalPages || 0)
        } catch (err) {
            console.error('Failed to load slots:', err)
            setSlots([])
        } finally {
            setLoading(false)
        }
    }

    function formatDateTime(dateStr) {
        const date = new Date(dateStr)
        return date.toLocaleDateString('en-US', {
            weekday: 'short',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        })
    }

    function getStatusBadge(status) {
        const statusClasses = {
            'AVAILABLE': 'badge-available',
            'BOOKED': 'badge-booked',
            'CONFIRMED': 'badge-confirmed',
            'EXPIRED': 'badge-expired',
            'CANCELLED': 'badge-cancelled'
        }
        return `badge ${statusClasses[status] || ''}`
    }

    return (
        <div className="card">
            <div className="flex-between">
                <h2>Available Interview Slots</h2>
                <div className="flex gap-2">
                    <select
                        value={selectedInterviewer}
                        onChange={(e) => {
                            setSelectedInterviewer(e.target.value)
                            setPage(0)
                        }}
                        style={{ width: 'auto' }}
                    >
                        <option value="">All Interviewers</option>
                        {interviewers.map(i => (
                            <option key={i.id} value={i.id}>{i.name}</option>
                        ))}
                    </select>
                    <button className="btn-secondary btn-sm" onClick={loadSlots}>
                        Refresh
                    </button>
                </div>
            </div>

            {loading ? (
                <div className="empty-state">Loading...</div>
            ) : slots.length === 0 ? (
                <div className="empty-state">
                    <p>No available slots found.</p>
                    <p>Generate slots for an interviewer to get started.</p>
                </div>
            ) : (
                <>
                    <table>
                        <thead>
                            <tr>
                                <th>Date & Time</th>
                                <th>Duration</th>
                                <th>Interviewer</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {slots.map(slot => (
                                <tr key={slot.id}>
                                    <td>{formatDateTime(slot.startTime)}</td>
                                    <td>
                                        {Math.round((new Date(slot.endTime) - new Date(slot.startTime)) / 60000)} min
                                    </td>
                                    <td>{slot.interviewerName || '-'}</td>
                                    <td>
                                        <span className={getStatusBadge(slot.status)}>
                                            {slot.status}
                                        </span>
                                    </td>
                                    <td>
                                        {slot.status === 'AVAILABLE' ? (
                                            <button
                                                className="btn-primary btn-sm"
                                                onClick={() => onBookSlot(slot)}
                                            >
                                                Book
                                            </button>
                                        ) : (
                                            <span style={{ color: 'var(--gray-400)' }}>—</span>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>

                    <div className="pagination">
                        <button
                            className="btn-secondary btn-sm"
                            onClick={() => setPage(p => Math.max(0, p - 1))}
                            disabled={page === 0}
                        >
                            Previous
                        </button>
                        <span>Page {page + 1} of {totalPages || 1}</span>
                        <button
                            className="btn-secondary btn-sm"
                            onClick={() => setPage(p => p + 1)}
                            disabled={page >= totalPages - 1}
                        >
                            Next
                        </button>
                    </div>
                </>
            )}
        </div>
    )
}
