import React from "react";
export default function Footer() {
    return (
        <div className="w-[100%] bg-gray-200 flex justify-center mb-1">
            <div className=" md:p-0.5 w-[100%] flex flex-col items-center py-4">
                <div className=" flex gap-0 items-center cursor-pointer">
                    <h3 className=" text-blue-800 font-bold text-xl">Linked</h3>
                    <img src="https://freelogopng.com/images/all_img/1656994981linkedin-icon-png.png"
                        alt="LinkedIn" className="w-[5px] h-[5px]"></img>
                </div>
                <div className="text-sm">
                    @copyright 2025
                </div>
            </div>
        </div>
    )
}