import React, { useEffect, useState } from 'react'
import { addInterviewer, listInterviewers } from '../utils/api'

export default function Interviewers() {
    const [name, setName] = useState('')
    const [list, setList] = useState([])

    useEffect(() => { load() }, [])

    async function load() {
        try {
            const data = await listInterviewers()
            setList(data)
        } catch (err) {
            console.error(err)
        }
    }

    async function submit(e) {
        e.preventDefault()
        try {
            await addInterviewer({ name })
            setName('')
            load()
        } catch (err) {
            console.error(err)
            alert('Failed to add interviewer')
        }
    }

    return (
        <div>
            <h2>Add Interviewer</h2>
            <form onSubmit={submit}>
                <input value={name} onChange={e => setName(e.target.value)} placeholder="Name" />
                <button type="submit">Add</button>
            </form>

            <h3>Existing</h3>
            <ul>
                {list.map(i => <li key={i.id}>{i.name} (id: {i.id})</li>)}
            </ul>
        </div>
    )
}
