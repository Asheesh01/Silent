import React from "react";
import { Link } from "react-router-dom";

export default function Navbar() {
    return <nav className="w-[100%] bg-gray-100 md:px-[100px] px-[20px] flex justify-between py-4 box-border  items-center">
            <Link to={"/"} className="flex gap-1 items-center cursor-pointer">
                <h3 className="text-blue-800 font-bold text-3xl">Linked</h3>
                <img src="https://th.bing.com/th/id/R.abdb36b128f0cfcee1329ddb1365a99b?rik=Q8UtGzuevu7ZBw&riu=http%3a%2f%2flofrev.net%2fwp-content%2fphotos%2f2017%2f04%2flinkedin_logo.jpg&ehk=WX0fSjGgisCu4YfNc2IBnr7nLADE%2f06resHyt%2fqG1pg%3d&risl=&pid=ImgRaw&r=0"
                    alt="linkidlogo" className="h-6 w-6"></img>
            </Link>
        <div className="flex box-border md:gap-4 gap-2 justify-center items-center">
            
        <Link to={"/signUp"} className="md:px-4 md:py-2 box-border text-black rounded-3xl text-xl hover:bg-gray-200 cursor-pointer">
            Join Now  
            </Link>
            <Link to={"/Login"} className="md:px-4 md:py-2 box-border border-1 border-blue-800 rounded-3xl text-xl hover:bg-blue-100 cursor-pointer ml-0"> Sign in</Link>  
            </div>
         
    </nav>
}  