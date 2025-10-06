import React from "react";
import './loader.css'
export default function Loadr() {
    return (
        <div className=" fixed top-0 left-0 w-full z-100 h-full bg-gray-200 flex justify-center items-center">
            <div class="spinner">
                <div class="spinnerin"></div>
            </div>
        </div>
    )

}