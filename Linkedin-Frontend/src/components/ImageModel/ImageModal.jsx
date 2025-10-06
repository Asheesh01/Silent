import axios from "axios";
import CircularProgress from '@mui/material/CircularProgress';
import Box from '@mui/material/Box';
import React, { useState } from "react";
import { ToastContainer, toast } from "react-toastify";

export default function ImageModal({ isCircular, selfData ,handleEditFunc}) {
    const [imageLink, setImageLink] = useState(isCircular ? selfData.profile_pic : selfData?.cover_pic)
    const [loading, setLoading] = useState(false);


    const handleInputImage = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append("file", file);
        formData.append("upload_preset", "my_unsigned_preset"); // EXACT name from dashboard
        setLoading(true);
        try {
            const res = await fetch(
                "https://api.cloudinary.com/v1_1/dmlz1qzzr/image/upload", // <== your real cloud name here
                { method: "POST", body: formData }
            );

            const result = await res.json();
            console.log("Upload result:", result);

            setImageLink(result.secure_url);
        } catch (err) {
            console.error("Upload failed:", err);
        } finally {
            setLoading(false)
        }
    };

    const handlesubmitButton=async()=>{
        let {data}={...selfData};
        if(isCircular){
            data={...data,['profile_pic']:imageLink}
        }
        else{
            data={...data,['cover_pic']:imageLink}
        }
        handleEditFunc(data)

    }

    return (
        <div className="p-5 relative flex items-center flex-col h-full">
            {
                isCircular ? (
                    <img className="rounded-full w-[150px] h-[150px]" src={imageLink} alt="" />
                ) : (
                    <img className="rounded-xl w-full h-[200px] object-cover" src={imageLink} alt="" />
                )
            }
            <label htmlFor="btn-submit" className="absolute bottom-10 p-3 bg-blue-900 left-0 text-white rounded-2xl cursor-pointer">Upload</label>
            <input onChange={handleInputImage} type="file" className="hidden" id="btn-submit" />


            {
                loading ? <Box sx={{ display: 'flex' }}>
                    <CircularProgress />
                </Box> : <div className="absolute bottom-10 p-3 bg-blue-900 right-0 text-white rounded-2xl cursor-pointer" onClick={handlesubmitButton}>Submit</div>
            }

            <ToastContainer />
        </div>
    )
}