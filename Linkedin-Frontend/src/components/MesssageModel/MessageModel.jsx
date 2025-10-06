import axios from "axios";
import React, { useState } from "react";

export default function MessageModal({ selfData, userData }) {
    const [message, setMessage] = useState('')
    const handleSendMessage = async () => {
        await axios.post('http://localhost:5000/api/conversation/add-conversation', { recieverId: userData?._id, message }, { withCredentials: true }).then((res => {
            window.location.reload();
        })).catch(err => {
            console.log(err)
            alert(err?.res?.data?.error)
        })
    }

    return (
        <div className="my-5">
            <div className="w-full mb-4">

                <textarea value={message} onChange={(e) => setMessage(e.target.value)} className="p-2 mt-1 w-full border-1 rounded-md" id="skills" placeholder="Enter Message" cols={10} rows={10}></textarea>
            </div>
            <div onClick={handleSendMessage} className="bg-blue-900 rounded-xl w-fit p-2 text-white cursor-pointer">Send</div>

        </div>
    )
}