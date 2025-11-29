import React, { useState } from 'react'
import { generateSlots } from '../utils/api'

export default function GenerateSlots() {
    const [interviewerId, setInterviewerId] = useState('1001')
    const [loading, setLoading] = useState(false)
    const [timesInput, setTimesInput] = useState('')

    async function submit(e) {
        e.preventDefault()
        setLoading(true)
        try {
            const times = timesInput.split(',').map(s => s.trim()).filter(Boolean)
            // try to parse ISO date-times; otherwise leave strings to backend parse
            const parsed = times.map(t => t)
            await generateSlots(Number(interviewerId), parsed)
            alert('Slots generated')
        } catch (err) {
            console.error(err)
            alert('Failed to generate')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div>
            <h2>Generate Slots</h2>
            <form onSubmit={submit}>
                <label>
                    Interviewer ID:
                    <input value={interviewerId} onChange={(e) => setInterviewerId(e.target.value)} />
                </label>
                <label>
                    Slot times (comma-separated ISO datetimes):
                    <input value={timesInput} onChange={(e) => setTimesInput(e.target.value)} placeholder="2025-12-01T09:00:00,2025-12-01T10:00:00" />
                </label>
                <button type="submit" disabled={loading}>{loading ? 'Generating...' : 'Generate'}</button>
            </form>
        </div>
    )
}
