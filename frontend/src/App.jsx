import React, { useState } from 'react'
import SlotList from './components/SlotList'
import GenerateSlots from './components/GenerateSlots'
import BookSlot from './components/BookSlot'
import Interviewers from './components/Interviewers'

export default function App() {
    const [view, setView] = useState('list')
    const [selectedSlot, setSelectedSlot] = useState(null)
    const [interviewersView, setInterviewersView] = useState(false)

    return (
        <div className="app">
            <header>
                <h1>Interview Scheduler</h1>
                <nav>
                    <button onClick={() => { setInterviewersView(false); setView('list') }}>Slots</button>
                    <button onClick={() => { setInterviewersView(false); setView('generate') }}>Generate</button>
                    <button onClick={() => { setInterviewersView(false); setView('book') }}>Book</button>
                    <button onClick={() => { setInterviewersView(true); setView('interviewers') }}>Interviewers</button>
                </nav>
            </header>

            <main>
                {view === 'list' && (
                    <SlotList onBook={(slot) => { setSelectedSlot(slot); setView('book') }} />
                )}
                {view === 'generate' && <GenerateSlots />}
                {view === 'book' && <BookSlot slot={selectedSlot} onDone={() => setView('list')} />}
                {view === 'interviewers' && interviewersView && <Interviewers />}
            </main>
        </div>
    )
}
