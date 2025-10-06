import React, { useEffect, useState } from "react";

export default function Conversation({item,key,ownData, handleSelectedConv,activeConID}) {
   const[memberData,setMemberData]=useState(null)

   useEffect(()=>{
    let ownId=ownData?._id;
    let arr=item?.members?.filter((it)=>it._id !==ownId);
    setMemberData(arr[0]);
   })

   const handleClickFunction=async()=>{
     handleSelectedConv(item?._id,memberData)
   }

    return (
        <div>
            <div onClick={handleClickFunction} key={key} className={`flex items-center w-full cursor-pointer border-b-1 border-gray-300 gap-3 p-4 hover:bg-gray-200 ${activeConID==item?._id?'bg-gray-200':null}`}>
                <div className='shrink-0'>
                    <img className='w-[48px] h-[48px] rounded-[100%]' src={memberData?.profile_pic} alt="" />
                </div>
                <div>
                    <div className='text-md'>{memberData?.f_name}</div>
                    <div className='text-sm text-gray-500'>{memberData?._headline}</div>

                </div>

            </div>;
        </div>
    )
}