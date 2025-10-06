import React from "react";
import CloseIcon from '@mui/icons-material/Close';

export default function Model(props){
    return(
        <div className="bg-black/50 fixed inset-0 flex justify-center items-center" >
            <div className="w-[95%] md:w-[50%]  h-[500px] bg-white rounded-xl p-10">
                <div className="flex justify-between">
                    <div className="flex gap-4 items-center">
                        <div className="text-2xl">
                           {props.title}
                        </div>
                        
                    </div>
                    <div onClick={props.closeModel} className="cursor-pointer"><CloseIcon /></div>
                </div>
                 {props.children}
            </div>
           
        </div>
    )
}