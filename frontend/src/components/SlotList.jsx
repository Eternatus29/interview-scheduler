import React, { useEffect, useState } from 'react'
import { fetchSlots } from '../utils/api'

export default function SlotList({ onBook }) {
    const [slots, setSlots] = useState([])
    const [page, setPage] = useState(0)
    const [size] = useState(10)

    // refresh helper in case caller wants to refresh after booking
    const refresh = () => load()

    useEffect(() => {
        load()
    }, [page])

    async function load() {
        try {
            const data = await fetchSlots(page, size)
            // /api/slots/available returns an array
            setSlots(Array.isArray(data) ? data : (data.content || []))
        } catch (err) {
            console.error(err)
            alert('Failed to load slots')
        }
    }

    return (
        <div>
            <h2>Available Slots</h2>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Time</th>
                        <th>Status</th>
                        <th>Interviewer</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    {slots.map((s) => (
                        <tr key={s.id}>
                            <td>{s.id}</td>
                            <td>{new Date(s.slotTime).toLocaleString()}</td>
                            <td>{s.status}</td>
                            <td>{s.interviewerSlot?.interviewerId ?? '-'}</td>
                            <td>
                                {s.status === 'AVAILABLE' ? (
                                    <button onClick={() => onBook(s)}>Book</button>
                                ) : (
                                    <span>Booked</span>
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            <div className="pager">
                <button onClick={() => setPage(p => Math.max(0, p - 1))}>Prev</button>
                <span>Page {page + 1}</span>
                <button onClick={() => setPage(p => p + 1)}>Next</button>
            </div>
        </div>
    )
}
