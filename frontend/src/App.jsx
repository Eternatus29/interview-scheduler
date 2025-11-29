import React, { useState } from 'react'
import SlotList from './components/SlotList'
import GenerateSlots from './components/GenerateSlots'
import Bookings from './components/Bookings'
import Interviewers from './components/Interviewers'
import Candidates from './components/Candidates'

const TABS = [
    { id: 'slots', label: 'Available Slots' },
    { id: 'generate', label: 'Generate Slots' },
    { id: 'bookings', label: 'My Bookings' },
    { id: 'interviewers', label: 'Interviewers' },
    { id: 'candidates', label: 'Candidates' }
]

export default function App() {
    const [activeTab, setActiveTab] = useState('slots')
    const [bookingSlot, setBookingSlot] = useState(null)

    const handleBookSlot = (slot) => {
        setBookingSlot(slot)
        setActiveTab('bookings')
    }

    return (
        <div className="app">
            <header>
                <h1>Interview Scheduler</h1>
                <nav>
                    {TABS.map(tab => (
                        <button
                            key={tab.id}
                            className={activeTab === tab.id ? 'active' : ''}
                            onClick={() => {
                                setActiveTab(tab.id)
                                if (tab.id !== 'bookings') setBookingSlot(null)
                            }}
                        >
                            {tab.label}
                        </button>
                    ))}
                </nav>
            </header>

            <main>
                {activeTab === 'slots' && (
                    <SlotList onBookSlot={handleBookSlot} />
                )}
                {activeTab === 'generate' && <GenerateSlots />}
                {activeTab === 'bookings' && (
                    <Bookings
                        slotToBook={bookingSlot}
                        onClearSlot={() => setBookingSlot(null)}
                    />
                )}
                {activeTab === 'interviewers' && <Interviewers />}
                {activeTab === 'candidates' && <Candidates />}
            </main>
        </div>
    )
}
