import React, { useState, useEffect } from 'react'
import { generateSlots, listInterviewers } from '../utils/api'

export default function GenerateSlots() {
    const [interviewers, setInterviewers] = useState([])
    const [selectedInterviewer, setSelectedInterviewer] = useState('')
    const [weeksToGenerate, setWeeksToGenerate] = useState(2)
    const [loading, setLoading] = useState(false)
    const [result, setResult] = useState(null)

    useEffect(() => {
        loadInterviewers()
    }, [])

    async function loadInterviewers() {
        try {
            const res = await listInterviewers()
            setInterviewers(res.data || [])
            if (res.data && res.data.length > 0) {
                setSelectedInterviewer(res.data[0].id.toString())
            }
        } catch (err) {
            console.error('Failed to load interviewers:', err)
        }
    }

    async function handleSubmit(e) {
        e.preventDefault()
        if (!selectedInterviewer) {
            alert('Please select an interviewer')
            return
        }

        setLoading(true)
        setResult(null)

        try {
            const res = await generateSlots(Number(selectedInterviewer), weeksToGenerate)
            const slotsGenerated = res.data?.length || 0
            setResult({
                success: true,
                message: `Successfully generated ${slotsGenerated} slots for the next ${weeksToGenerate} week(s).`
            })
        } catch (err) {
            setResult({
                success: false,
                message: err.message || 'Failed to generate slots'
            })
        } finally {
            setLoading(false)
        }
    }

    const selectedInterviewerData = interviewers.find(i => i.id.toString() === selectedInterviewer)

    return (
        <div className="card">
            <h2>Generate Interview Slots</h2>
            <p style={{ color: 'var(--gray-500)', marginBottom: '24px' }}>
                Generate time slots based on an interviewer's weekly availability pattern.
            </p>

            {result && (
                <div className={`alert ${result.success ? 'alert-success' : 'alert-error'}`}>
                    {result.message}
                </div>
            )}

            <form onSubmit={handleSubmit}>
                <div className="form-row">
                    <div className="form-group">
                        <label>Interviewer</label>
                        <select
                            value={selectedInterviewer}
                            onChange={(e) => setSelectedInterviewer(e.target.value)}
                            required
                        >
                            <option value="">Select an interviewer</option>
                            {interviewers.map(i => (
                                <option key={i.id} value={i.id}>
                                    {i.name} ({i.email})
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="form-group">
                        <label>Weeks to Generate</label>
                        <select
                            value={weeksToGenerate}
                            onChange={(e) => setWeeksToGenerate(Number(e.target.value))}
                        >
                            <option value={1}>1 week</option>
                            <option value={2}>2 weeks</option>
                            <option value={3}>3 weeks</option>
                            <option value={4}>4 weeks</option>
                        </select>
                    </div>
                </div>

                {selectedInterviewerData && (
                    <div className="alert alert-info" style={{ marginBottom: '20px' }}>
                        <strong>Interviewer Details:</strong><br />
                        Max interviews per week: {selectedInterviewerData.maxInterviewsPerWeek}<br />
                        Slot duration: {selectedInterviewerData.slotDurationMinutes} minutes<br />
                        Weekly availability: {selectedInterviewerData.weeklyAvailabilities?.length || 0} day(s) configured
                    </div>
                )}

                <button
                    type="submit"
                    className="btn-primary"
                    disabled={loading || !selectedInterviewer}
                >
                    {loading ? 'Generating...' : 'Generate Slots'}
                </button>
            </form>
        </div>
    )
}
