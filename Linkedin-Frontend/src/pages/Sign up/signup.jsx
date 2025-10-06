import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Login from "../Login/Login";
import GoogleLoginComp from "../../components/GoogleLogin/GoogleLogin";
import { ToastContainer, toast } from "react-toastify";
import axios from "axios";
export default function Signup(props) {
    const navigate = useNavigate();

    const [registerField, setRegisterField] = useState({ email: "", password: "", f_name: "" });
    const handleInputField = (event, key) => {
        setRegisterField({ ...registerField, [key]: event.target.value })
    }

    const handleRegister = async () => {
        if (registerField.email.trim().length === 0 || registerField.password.trim().length === 0 || registerField.f_name.trim().length === 0) {
            return toast.error("Please Fill All Details. ")
        }
        await axios.post("http://localhost:5000/api/auth/register", registerField).then(res => {
            toast.success("you have register successfully");
            setRegisterField({ ...registerField, email: "", password: "", f_name: "" })
            navigate('/login')
        }).catch(err => {
            console.log(err)
            toast.error(err?.response?.data?.error)
        })
    }

    return (
        <div className="w-full  bg-purple-200 flex flex-col items-center justify-center">
            <div className="text-4xl mb-5 pt-4">Make the most of your Professional life</div>
            <div className="w-[85%] md:w-[28%] shadow-xl rounded-sm box p-10">
                <div className="flex flex-col gap-4">
                    <div>
                        <label htmlFor="email">Email</label>
                        <input value={registerField.email} onChange={(e) => handleInputField(e, 'email')} type="text" id="email" className="w-full text-xl px-5 py-1 rounded-lg border-2" placeholder="Email"></input>
                        <div className="mt-3">
                            <label htmlFor="Password">Password</label>
                            <input value={registerField.password} onChange={(e) => handleInputField(e, 'password')} type="password" id="Password" className=" w-full text-xl px-5 py-1 border-2 rounded-lg" placeholder="Password" />
                        </div>
                        <div className="mt-3">
                            <label htmlFor="name">Full name</label>
                            <input value={registerField.f_name} onChange={(e) => handleInputField(e, 'f_name')} type="text" id="name" className="w-[100%] text-xl px-5 py-1 border-2 rounded-lg" placeholder="Full name" />
                        </div>

                        <div onClick={handleRegister} className="w-full hover:bg-blue-900 bg-blue-800 text-white py-3 px-4 text-center rounded-xl cursor-pointer mt-4">
                            register
                        </div>
                    </div>

                </div>

                <div className="flex items-center gap-2">
                    <div className="border-b-1   border-gray-400 w-[45%]"></div><div>or</div><div className="border-b-1 border-gray-400 w-[45%]"></div>
                </div>
                <div className="w-full">
                    <GoogleLoginComp changeLoginValue={props.changeLoginValue}/>
                </div>
            </div>
            <div className="mt-4 mb-10">Already on LinkedIn? <Link to={"/Login"} className="text-blue-800 cursor-pointer hover:underline">Sign in</Link>
            </div>

            <ToastContainer />
        </div>
    )
}