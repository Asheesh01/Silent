import React from "react";
import ProfileCard from "../../Profilecard/profilecard";
import Card from "../../components/card/card";
import Post from "../../components/Post/Post";
import Advertisment from "../../components/Advertisment/Advertisment";

export default function SingleActivity() {
    return (
        <div>
            <div className="flex px-5 xl:px-[250px] py-9 gap-5  w-full mt-5 bg-gray-100 ">
                <div className="w-[21%] sm:block sm:w-[23%] hidden py-[20px]">
                    <div className="h-fit">
                        <ProfileCard />
                    </div>
                </div>

                {/* Middle side */}
                <div className="w-[100%] py-5 sm:w-[50%]" >
                    

                    <div>
                        <Post></Post>
                    </div>

                </div>
                {/* Right side */}
                <div className="w-[26%]  hidden md:block">

                    <div className="my-5 sticky top-[76px]">
                        <Advertisment />
                    </div>


                </div>


            </div>
        </div>
    )
}