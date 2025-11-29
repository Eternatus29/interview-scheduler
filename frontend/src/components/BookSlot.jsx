import React, { useState } from 'react'
import { bookSlot } from '../utils/api'

export default function BookSlot({ slot, onDone }) {
    const [candidateId, setCandidateId] = useState('2001')
    const [loading, setLoading] = useState(false)

    async function submit(e) {
        e.preventDefault()
        if (!slot || !slot.id) {
            alert('No slot selected')
            return
        }
        setLoading(true)
        try {
            const res = await bookSlot(slot.id, Number(candidateId))
            alert(res)
            onDone()
        } catch (err) {
            console.error(err)
            alert('Failed to book')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div>
            <h2>Book Slot</h2>
            {slot ? (
                <div>
                    <p>Slot ID: {slot.id}</p>
                    <p>Time: {new Date(slot.slotTime).toLocaleString()}</p>
                    <form onSubmit={submit}>
                        <label>
                            Candidate ID:
                            <input value={candidateId} onChange={(e) => setCandidateId(e.target.value)} />
                        </label>
                        <button type="submit" disabled={loading}>{loading ? 'Booking...' : 'Book'}</button>
                    </form>
                </div>
            ) : (
                <p>No slot selected. Choose one from the Slots view.</p>
            )}
        </div>
    )
}
