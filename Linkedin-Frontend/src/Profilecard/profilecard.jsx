import React from "react";
import Card from "../components/card/card";
import { Link } from "react-router-dom";

export default function ProfileCard(props) {

    return (
        <div>
            <Card padding={0}>
                <div className="relative h-[100px]">
                    <Link to={`/profile/${props.data?._id}`} className="relative w-full h-[88px] rounded-md">
                        <img className="rounded-t-md  h-full w-full" src={props.data?.cover_pic} alt="" />
                    </Link>


                    <Link to={`/profile/${props.data?._id}`} className="absolute top-[56px] left-[24px]  z-[10]">
                        <img className="rounded-4xl h-[55px] w-[55px]  border-2 border-white cursor-pointer" src={props?.data?.profile_pic} alt="" />
                    </Link>
                </div>

                <div className="p-5 ">
                    <div className="text-xl">{props?.data?.f_name}</div>
                    <div className="text-sm my-1">{props?.data?.headline}</div>
                    <div className="text-sm my-1">{props?.data?.curr_location}</div>
                    <div className="text-sm my-1">{props?.data?.curr_company}</div>
                </div>
            </Card>
        </div>
    )
}