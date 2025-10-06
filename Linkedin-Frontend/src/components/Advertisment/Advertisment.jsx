import React, { useEffect, useState } from "react";
import Card from "../card/card";
export default function Advertisment() {

    const [userData, setUserData] = useState(null);
    useEffect(() => {
        let userData = localStorage.getItem('userInfo')
        setUserData(userData ? JSON.parse(userData) : null)
    }, [])
    return (
        <div className="sticky top-[72px]">
            <Card padding={0}>
                <div className="relative h-[100px]">
                    <div className="relative w-full h-[88px] rounded-md">
                        <img className="rounded-t-md  h-full w-full" src="https://cdn.dribbble.com/userupload/43593554/file/original-b5f90a6a1a213d1bf79e0152927d9325.png?format=webp&resize=1200x900&vertical=center" alt="" />
                    </div>

                    <div className="absolute top-[56px] left-[40%]  z-[10]">
                        <img className="rounded-4xl h-[55px] w-[55px]  border-2 border-white cursor-pointer" src={userData?.profile_pic} alt="" />
                    </div>
                </div>

                <div className="px-5 my-5 mx-auto">
                    <div className="text-sm font-semibold text-center">{userData?.f_name}</div>
                    <div className="text-sm my-3 text-center">Get the latest jobs and industry news</div>
                    <div className="text-sm my-1 border-2 text-center p-2 font-bold border-blue-950 text-white bg-purple-900 rounded-2xl cursor-pointer" onClick={() => window.dispatchEvent(new Event("focusSearch"))}>Explore</div>
                </div>
            </Card>
        </div>
    )
}