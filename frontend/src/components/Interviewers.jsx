import React, { useEffect, useState } from 'react'
import { createInterviewer, listInterviewers, addWeeklyAvailability } from '../utils/api'

const DAYS_OF_WEEK = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']

export default function Interviewers() {
    const [interviewers, setInterviewers] = useState([])
    const [loading, setLoading] = useState(false)
    const [showForm, setShowForm] = useState(false)

    // New interviewer form
    const [name, setName] = useState('')
    const [email, setEmail] = useState('')
    const [maxInterviewsPerWeek, setMaxInterviewsPerWeek] = useState(10)
    const [slotDurationMinutes, setSlotDurationMinutes] = useState(60)
    const [availabilities, setAvailabilities] = useState([
        { dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00' }
    ])

    useEffect(() => {
        loadInterviewers()
    }, [])

    async function loadInterviewers() {
        setLoading(true)
        try {
            const res = await listInterviewers()
            setInterviewers(res.data || [])
        } catch (err) {
            console.error('Failed to load interviewers:', err)
        } finally {
            setLoading(false)
        }
    }

    function addAvailability() {
        setAvailabilities([...availabilities, { dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00' }])
    }

    function removeAvailability(index) {
        setAvailabilities(availabilities.filter((_, i) => i !== index))
    }

    function updateAvailability(index, field, value) {
        const updated = [...availabilities]
        updated[index][field] = value
        setAvailabilities(updated)
    }

    async function handleSubmit(e) {
        e.preventDefault()
        setLoading(true)

        try {
            const payload = {
                name,
                email,
                maxInterviewsPerWeek,
                slotDurationMinutes,
                weeklyAvailabilities: availabilities.map(a => ({
                    dayOfWeek: a.dayOfWeek,
                    startTime: a.startTime + ':00',
                    endTime: a.endTime + ':00'
                }))
            }

            await createInterviewer(payload)

            // Reset form
            setName('')
            setEmail('')
            setMaxInterviewsPerWeek(10)
            setSlotDurationMinutes(60)
            setAvailabilities([{ dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00' }])
            setShowForm(false)

            loadInterviewers()
        } catch (err) {
            alert(err.message || 'Failed to create interviewer')
        } finally {
            setLoading(false)
        }
    }

    function formatAvailability(avails) {
        if (!avails || avails.length === 0) return 'No availability set'
        return avails.map(a =>
            `${a.dayOfWeek.slice(0, 3)} ${a.startTime?.slice(0, 5) || ''}-${a.endTime?.slice(0, 5) || ''}`
        ).join(', ')
    }

    return (
        <div className="card">
            <div className="flex-between">
                <h2>Interviewers</h2>
                <button
                    className="btn-primary btn-sm"
                    onClick={() => setShowForm(!showForm)}
                >
                    {showForm ? 'Cancel' : '+ Add Interviewer'}
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} style={{ marginTop: '20px', marginBottom: '24px', padding: '20px', background: 'var(--gray-50)', borderRadius: 'var(--radius)' }}>
                    <h3 style={{ marginTop: 0 }}>New Interviewer</h3>

                    <div className="form-row">
                        <div className="form-group">
                            <label>Name</label>
                            <input
                                type="text"
                                value={name}
                                onChange={e => setName(e.target.value)}
                                placeholder="John Smith"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label>Email</label>
                            <input
                                type="email"
                                value={email}
                                onChange={e => setEmail(e.target.value)}
                                placeholder="john@company.com"
                                required
                            />
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label>Max Interviews/Week</label>
                            <input
                                type="number"
                                value={maxInterviewsPerWeek}
                                onChange={e => setMaxInterviewsPerWeek(Number(e.target.value))}
                                min="1"
                                max="50"
                            />
                        </div>
                        <div className="form-group">
                            <label>Slot Duration (minutes)</label>
                            <select
                                value={slotDurationMinutes}
                                onChange={e => setSlotDurationMinutes(Number(e.target.value))}
                            >
                                <option value={30}>30 minutes</option>
                                <option value={45}>45 minutes</option>
                                <option value={60}>60 minutes</option>
                                <option value={90}>90 minutes</option>
                            </select>
                        </div>
                    </div>

                    <h3>Weekly Availability</h3>
                    {availabilities.map((avail, index) => (
                        <div key={index} className="form-row" style={{ alignItems: 'end' }}>
                            <div className="form-group">
                                <label>Day</label>
                                <select
                                    value={avail.dayOfWeek}
                                    onChange={e => updateAvailability(index, 'dayOfWeek', e.target.value)}
                                >
                                    {DAYS_OF_WEEK.map(day => (
                                        <option key={day} value={day}>{day}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group">
                                <label>Start Time</label>
                                <input
                                    type="time"
                                    value={avail.startTime}
                                    onChange={e => updateAvailability(index, 'startTime', e.target.value)}
                                />
                            </div>
                            <div className="form-group">
                                <label>End Time</label>
                                <input
                                    type="time"
                                    value={avail.endTime}
                                    onChange={e => updateAvailability(index, 'endTime', e.target.value)}
                                />
                            </div>
                            <div className="form-group">
                                <button
                                    type="button"
                                    className="btn-danger btn-sm"
                                    onClick={() => removeAvailability(index)}
                                    disabled={availabilities.length === 1}
                                >
                                    Remove
                                </button>
                            </div>
                        </div>
                    ))}

                    <button type="button" className="btn-secondary btn-sm" onClick={addAvailability} style={{ marginBottom: '20px' }}>
                        + Add Another Day
                    </button>

                    <div>
                        <button type="submit" className="btn-primary" disabled={loading}>
                            {loading ? 'Creating...' : 'Create Interviewer'}
                        </button>
                    </div>
                </form>
            )}

            {loading && !showForm ? (
                <div className="empty-state">Loading...</div>
            ) : interviewers.length === 0 ? (
                <div className="empty-state">
                    <p>No interviewers found.</p>
                    <p>Add an interviewer to get started.</p>
                </div>
            ) : (
                <table>
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Max/Week</th>
                            <th>Duration</th>
                            <th>Availability</th>
                        </tr>
                    </thead>
                    <tbody>
                        {interviewers.map(interviewer => (
                            <tr key={interviewer.id}>
                                <td><strong>{interviewer.name}</strong></td>
                                <td>{interviewer.email}</td>
                                <td>{interviewer.maxInterviewsPerWeek}</td>
                                <td>{interviewer.slotDurationMinutes} min</td>
                                <td style={{ fontSize: '13px', color: 'var(--gray-600)' }}>
                                    {formatAvailability(interviewer.weeklyAvailabilities)}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    )
}
