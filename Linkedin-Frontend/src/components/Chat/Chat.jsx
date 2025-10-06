import React from "react";

export default function Chat() {
    return (
        <div>
            < div className='flex w-full cursor-pointer  border-gray-300 gap-3 p-4'>
                <div className='shrink-0'>
                    <img className='w-8 h-8 cursor-pointer rounded-full' src="https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?cs=srgb&dl=pexels-pixabay-220453.jpg&fm=jpg" alt="" />
                </div>
                <div className='w-full mb-2'>
                    <div className='text-md'>User 1</div>

                    <div className='text-sm mt-6 hover:bg-gray-200 w-full'>This is text message</div>
                    <div>
                        <div className='my-2'> <img className='w-[240px] h-[180px] rounded-md ' src="https://wallpaperaccess.com/full/90977.jpg" alt="" /></div>

                    </div>


                </div>
            </div>
            < div className='flex w-full cursor-pointer  border-gray-300 gap-3 p-4'>
                <div className='shrink-0'>
                    <img className='w-8 h-8 cursor-pointer rounded-full' src="https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?cs=srgb&dl=pexels-pixabay-220453.jpg&fm=jpg" alt="" />
                </div>
                <div className='w-full mb-2'>
                    <div className='text-md'>User 1</div>

                    <div className='text-sm mt-6 hover:bg-gray-200 w-full'>This is text message</div>
                    <div>
                        <div className='my-2'> <img className='w-[240px] h-[180px] rounded-md ' src="https://wallpaperaccess.com/full/90977.jpg" alt="" /></div>

                    </div>

                </div>
            </div>
        </div>

    )
}