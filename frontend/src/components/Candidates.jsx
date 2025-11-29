import React, { useEffect, useState } from 'react'
import { createCandidate, listCandidates } from '../utils/api'

export default function Candidates() {
    const [candidates, setCandidates] = useState([])
    const [loading, setLoading] = useState(false)
    const [showForm, setShowForm] = useState(false)

    // New candidate form
    const [name, setName] = useState('')
    const [email, setEmail] = useState('')
    const [phoneNumber, setPhoneNumber] = useState('')

    useEffect(() => {
        loadCandidates()
    }, [])

    async function loadCandidates() {
        setLoading(true)
        try {
            const res = await listCandidates()
            setCandidates(res.data || [])
        } catch (err) {
            console.error('Failed to load candidates:', err)
        } finally {
            setLoading(false)
        }
    }

    async function handleSubmit(e) {
        e.preventDefault()
        setLoading(true)

        try {
            await createCandidate({ name, email, phoneNumber })

            // Reset form
            setName('')
            setEmail('')
            setPhoneNumber('')
            setShowForm(false)

            loadCandidates()
        } catch (err) {
            alert(err.message || 'Failed to create candidate')
        } finally {
            setLoading(false)
        }
    }

    function formatDate(dateStr) {
        return new Date(dateStr).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        })
    }

    return (
        <div className="card">
            <div className="flex-between">
                <h2>Candidates</h2>
                <button
                    className="btn-primary btn-sm"
                    onClick={() => setShowForm(!showForm)}
                >
                    {showForm ? 'Cancel' : '+ Add Candidate'}
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} style={{ marginTop: '20px', marginBottom: '24px', padding: '20px', background: 'var(--gray-50)', borderRadius: 'var(--radius)' }}>
                    <h3 style={{ marginTop: 0 }}>New Candidate</h3>

                    <div className="form-row">
                        <div className="form-group">
                            <label>Name</label>
                            <input
                                type="text"
                                value={name}
                                onChange={e => setName(e.target.value)}
                                placeholder="Jane Doe"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label>Email</label>
                            <input
                                type="email"
                                value={email}
                                onChange={e => setEmail(e.target.value)}
                                placeholder="jane@example.com"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label>Phone Number</label>
                            <input
                                type="tel"
                                value={phoneNumber}
                                onChange={e => setPhoneNumber(e.target.value)}
                                placeholder="+1-555-1234"
                            />
                        </div>
                    </div>

                    <button type="submit" className="btn-primary" disabled={loading}>
                        {loading ? 'Creating...' : 'Create Candidate'}
                    </button>
                </form>
            )}

            {loading && !showForm ? (
                <div className="empty-state">Loading...</div>
            ) : candidates.length === 0 ? (
                <div className="empty-state">
                    <p>No candidates found.</p>
                    <p>Add a candidate to get started.</p>
                </div>
            ) : (
                <table>
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Created</th>
                        </tr>
                    </thead>
                    <tbody>
                        {candidates.map(candidate => (
                            <tr key={candidate.id}>
                                <td><strong>{candidate.name}</strong></td>
                                <td>{candidate.email}</td>
                                <td>{candidate.phoneNumber || '-'}</td>
                                <td style={{ color: 'var(--gray-500)', fontSize: '13px' }}>
                                    {formatDate(candidate.createdAt)}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    )
}
