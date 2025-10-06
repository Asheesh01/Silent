import React, { useState } from "react";

export default function ExpModal({ handleEditFunc, selfData, updateExp, setUpdateExp }) {

    const [data, setData] = useState({
        designation: updateExp?.clicked ? updateExp?.data?.designation : "",
        company_name: updateExp?.clicked ? updateExp?.data?.company_name : "",
        duration: updateExp?.clicked ? updateExp?.data?.duration : "",
        location: updateExp?.clicked ? updateExp?.data?.location : ""
    })



    const handleOnDelete = async () => {
        let newFilterData = selfData?.experiance?.filter((item) => item._id !== updateExp?.data?._id);
        let newData = { ...selfData, experiance:newFilterData };
        handleEditFunc(newData)
    }

    const onChangeHandle = (event, key) => {
        setData({ ...data, [key]: event.target.value })
    }
    const updateExpSave = () => {
        let newFilterData = selfData?.experiance?.filter((item) => item._id !== updateExp?.data?._id);
        let newArr = [...newFilterData, data];
        let newData = { ...selfData, experiance: newArr };
        handleEditFunc(newData)
    }

    const handleOnSave = () => {
        if (updateExp?.clicked) return updateExpSave();

        const expArr = [...(selfData?.experiance ?? []), data];
        let newData = { ...selfData, experiance: expArr };
        handleEditFunc(newData);
    }
    return (
        <div>
            <div className="mt-8 w-full h-[350px] overflow-auto">
                <div className="w-full mb-4">
                    <label htmlFor="full-name">Role*</label>
                    <br />
                    <input type="text" value={data.designation} onChange={(e) => { onChangeHandle(e, 'designation') }} className="p-2 mt-1 w-full border-1 rounded-md" placeholder="Roll" />
                </div>

                <div className="w-full mb-4">
                    <label htmlFor="Company">Comapny</label>
                    <br />
                    <input type="text" value={data.company_name} onChange={(e) => { onChangeHandle(e, 'company_name') }} className="p-2 mt-1 w-full border-1 rounded-md" placeholder="Company" />
                </div>

                <div className="w-full mb-4">
                    <label htmlFor="Duration">Duration</label>
                    <br />
                    <input type="text" value={data.duration} onChange={(e) => { onChangeHandle(e, 'duration') }} className="p-2 mt-1 w-full border-1 rounded-md" placeholder="RDuration" />
                </div>

                <div className="w-full mb-4">
                    <label htmlFor="Place">Place</label>
                    <br />
                    <input type="text" value={data.location} onChange={(e) => { onChangeHandle(e, 'location') }} className="p-2 mt-1 w-full border-1 rounded-md" placeholder="Place" />
                </div>
                <div className="flex justify-between">
                    <div className="bg-blue-900 rounded-xl w-fit p-2 text-white cursor-pointer" onClick={handleOnSave}>Save</div>
                    {
                        updateExp?.clicked && <div className="bg-blue-900 rounded-xl w-fit p-2 text-white cursor-pointer" onClick={handleOnDelete}>Delete</div>
                    }
                </div>

            </div>
        </div>
    )
}