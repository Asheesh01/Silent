import React, { useState } from "react";

export default function AboutModal({ handleEditFunc, selfData }) {
    const [data, setData] = useState({ about: selfData?.about, skillInp: selfData?.skills.join(','), resume: selfData?.resume })
    const [loading, setLoading] = useState(false);
    const onChangeHandle = (event, key) => {
        setData({ ...data, [key]: event.target.value })
    }
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
            setData({ ...data, resume: result.secure_url });
        } catch (err) {
            console.error("Upload failed:", err);
        } finally {
            setLoading(false)
        }
    };

    const handleonSave = async () => {
        let arr = data?.skillInp?.split(',');
        let newData = { ...selfData, about: data.about, skills: arr, resume: data.resume };
        handleEditFunc(newData);
    }

    return (
        <div className="my-8" >
            <div className="w-full mb-4">
                <label htmlFor="About">About*</label>
                <br />
                <textarea value={data.about} onChange={(e) => onChangeHandle(e, 'about')} className=" className=p-2 mt-1 w-full border-1 rounded-md" id="About" cols={10} rows={3}></textarea>
            </div>


            <div className="w-full mb-4">
                <label htmlFor="skills">Skills*(Add by Seperating comma)</label>
                <br />
                <textarea value={data.skillInp} onChange={(e) => onChangeHandle(e, 'skillInp')} className="p-2 mt-1 w-full border-1 rounded-md" id="skills" cols={10} rows={3}></textarea>
            </div>

            <div className="w-full mb-2  ">
                <div>
                    <label htmlFor="resumeUpload" className="p-2 bg-purple-800 text-white rounded-lg cursor-pointer">Resume Upload</label>
                </div>

                <input onChange={handleInputImage} type="file" className="hidden" id="resumeUpload" />
                {
                    data.resume && <div className="my-2">{data.resume}</div>
                }
            </div>
            <div className="bg-purple-900 rounded-2xl w-fit p-2 text-white cursor-pointer" onClick={handleonSave}>Save</div>




        </div>
    )
}