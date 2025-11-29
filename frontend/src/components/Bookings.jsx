import React, { useEffect, useState } from 'react'
import {
    bookSlot,
    confirmBooking,
    cancelBooking,
    updateBooking,
    getBookingsByCandidate,
    listCandidates
} from '../utils/api'

export default function Bookings({ slotToBook, onClearSlot }) {
    const [candidates, setCandidates] = useState([])
    const [bookings, setBookings] = useState([])
    const [selectedCandidate, setSelectedCandidate] = useState('')
    const [bookingNotes, setBookingNotes] = useState('')
    const [loading, setLoading] = useState(false)
    const [message, setMessage] = useState(null)

    useEffect(() => {
        loadCandidates()
    }, [])

    useEffect(() => {
        if (selectedCandidate) {
            loadBookings()
        }
    }, [selectedCandidate])

    async function loadCandidates() {
        try {
            const res = await listCandidates()
            setCandidates(res.data || [])
            if (res.data && res.data.length > 0) {
                setSelectedCandidate(res.data[0].id.toString())
            }
        } catch (err) {
            console.error('Failed to load candidates:', err)
        }
    }

    async function loadBookings() {
        if (!selectedCandidate) return
        setLoading(true)
        try {
            const res = await getBookingsByCandidate(selectedCandidate)
            setBookings(res.data || [])
        } catch (err) {
            console.error('Failed to load bookings:', err)
            setBookings([])
        } finally {
            setLoading(false)
        }
    }

    async function handleBookSlot(e) {
        e.preventDefault()
        if (!slotToBook || !selectedCandidate) {
            setMessage({ type: 'error', text: 'Please select a candidate' })
            return
        }

        setLoading(true)
        setMessage(null)

        try {
            await bookSlot(slotToBook.id, Number(selectedCandidate), bookingNotes)
            setMessage({ type: 'success', text: 'Slot booked successfully!' })
            setBookingNotes('')
            onClearSlot()
            loadBookings()
        } catch (err) {
            setMessage({ type: 'error', text: err.message || 'Failed to book slot' })
        } finally {
            setLoading(false)
        }
    }

    async function handleConfirm(bookingId) {
        try {
            await confirmBooking(bookingId)
            setMessage({ type: 'success', text: 'Booking confirmed!' })
            loadBookings()
        } catch (err) {
            setMessage({ type: 'error', text: err.message || 'Failed to confirm booking' })
        }
    }

    async function handleCancel(bookingId) {
        if (!confirm('Are you sure you want to cancel this booking?')) return

        try {
            await cancelBooking(bookingId)
            setMessage({ type: 'success', text: 'Booking cancelled' })
            loadBookings()
        } catch (err) {
            setMessage({ type: 'error', text: err.message || 'Failed to cancel booking' })
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
            'PENDING': 'badge-pending',
            'CONFIRMED': 'badge-confirmed',
            'CANCELLED': 'badge-cancelled',
            'COMPLETED': 'badge-available',
            'NO_SHOW': 'badge-expired'
        }
        return `badge ${statusClasses[status] || ''}`
    }

    return (
        <div>
            {/* Book New Slot Section */}
            {slotToBook && (
                <div className="card" style={{ borderLeft: '4px solid var(--primary)' }}>
                    <h2>Book Selected Slot</h2>

                    <div className="alert alert-info">
                        <strong>Selected Slot:</strong><br />
                        {formatDateTime(slotToBook.startTime)} - {formatDateTime(slotToBook.endTime)}<br />
                        Interviewer: {slotToBook.interviewerName || 'Unknown'}
                    </div>

                    {message && (
                        <div className={`alert ${message.type === 'success' ? 'alert-success' : 'alert-error'}`}>
                            {message.text}
                        </div>
                    )}

                    <form onSubmit={handleBookSlot}>
                        <div className="form-row">
                            <div className="form-group">
                                <label>Candidate</label>
                                <select
                                    value={selectedCandidate}
                                    onChange={(e) => setSelectedCandidate(e.target.value)}
                                    required
                                >
                                    <option value="">Select a candidate</option>
                                    {candidates.map(c => (
                                        <option key={c.id} value={c.id}>
                                            {c.name} ({c.email})
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group">
                                <label>Notes (optional)</label>
                                <input
                                    type="text"
                                    value={bookingNotes}
                                    onChange={(e) => setBookingNotes(e.target.value)}
                                    placeholder="Any special notes..."
                                />
                            </div>
                        </div>

                        <div className="flex gap-2">
                            <button type="submit" className="btn-primary" disabled={loading}>
                                {loading ? 'Booking...' : 'Confirm Booking'}
                            </button>
                            <button type="button" className="btn-secondary" onClick={onClearSlot}>
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Existing Bookings Section */}
            <div className="card">
                <div className="flex-between">
                    <h2>Bookings</h2>
                    <select
                        value={selectedCandidate}
                        onChange={(e) => setSelectedCandidate(e.target.value)}
                        style={{ width: 'auto' }}
                    >
                        <option value="">Select candidate</option>
                        {candidates.map(c => (
                            <option key={c.id} value={c.id}>{c.name}</option>
                        ))}
                    </select>
                </div>

                {!slotToBook && message && (
                    <div className={`alert ${message.type === 'success' ? 'alert-success' : 'alert-error'}`}>
                        {message.text}
                    </div>
                )}

                {!selectedCandidate ? (
                    <div className="empty-state">
                        <p>Select a candidate to view their bookings.</p>
                    </div>
                ) : loading ? (
                    <div className="empty-state">Loading...</div>
                ) : bookings.length === 0 ? (
                    <div className="empty-state">
                        <p>No bookings found for this candidate.</p>
                        <p>Go to "Available Slots" to book an interview.</p>
                    </div>
                ) : (
                    <table>
                        <thead>
                            <tr>
                                <th>Date & Time</th>
                                <th>Interviewer</th>
                                <th>Status</th>
                                <th>Notes</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {bookings.map(booking => (
                                <tr key={booking.id}>
                                    <td>{formatDateTime(booking.slotStartTime)}</td>
                                    <td>{booking.interviewerName || '-'}</td>
                                    <td>
                                        <span className={getStatusBadge(booking.status)}>
                                            {booking.status}
                                        </span>
                                    </td>
                                    <td style={{ color: 'var(--gray-500)', fontSize: '13px' }}>
                                        {booking.bookingNotes || '-'}
                                    </td>
                                    <td>
                                        <div className="actions">
                                            {booking.status === 'PENDING' && (
                                                <>
                                                    <button
                                                        className="btn-success btn-sm"
                                                        onClick={() => handleConfirm(booking.id)}
                                                    >
                                                        Confirm
                                                    </button>
                                                    <button
                                                        className="btn-danger btn-sm"
                                                        onClick={() => handleCancel(booking.id)}
                                                    >
                                                        Cancel
                                                    </button>
                                                </>
                                            )}
                                            {booking.status === 'CONFIRMED' && (
                                                <button
                                                    className="btn-danger btn-sm"
                                                    onClick={() => handleCancel(booking.id)}
                                                >
                                                    Cancel
                                                </button>
                                            )}
                                            {(booking.status === 'CANCELLED' || booking.status === 'COMPLETED') && (
                                                <span style={{ color: 'var(--gray-400)' }}>—</span>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    )
}
