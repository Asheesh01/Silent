import React, { useState } from "react";
import { Form, Link, useNavigate } from "react-router-dom";
import GoogleLoginComp from "../../components/GoogleLogin/GoogleLogin";
import { ToastContainer, toast } from 'react-toastify'
import axios from 'axios'

export default function Login(props) {
    const navigate = useNavigate();
    const [loginField, setLoginField] = useState({ email: "", password: "" })

    const onChangeInput = (event, key) => {
        setLoginField({ ...loginField, [key]: event.target.value })
    }
    console.log(loginField);
    const handleLogin = async () => {
        if (loginField.email.trim().length === 0 || loginField.password.trim().length === 0) {
            return toast.error("please fill all credentials")
        }
        console.log("Sending to backend:", loginField);

        await axios.post( `${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/login`, loginField, { withCredentials: true }).then((res) => {
            props.changeLoginValue(true);
            localStorage.setItem('isLogin', 'true');
            localStorage.setItem("userInfo", JSON.stringify(res.data.userExists));
            localStorage.setItem("token", res.data.token);

            navigate('/feed')
        }).catch(err => {
            console.log(err)
            toast.error(err?.response?.data?.error)
        })
    }

    return (
        <div className="flex flex-col min-h-screen bg-purple-200 w-full items-center justify-center px-4 py-6 sm:py-8 md:py-10">
            <div className="w-full sm:w-[90%] md:w-[60%] lg:w-[40%] xl:w-[28%] shadow-xl box p-6 sm:p-8 md:p-10">
                <div className="text-2xl sm:text-3xl md:text-3xl font-semibold mb-4">
                    Sign In
                </div>
                <div className="my-5 w-full">
                    <GoogleLoginComp changeLoginValue={props.changeLoginValue} />
                </div>
                <div className="flex items-center gap-2 my-4">
                    <div className="border-b-1 border-gray-400 w-[45%]"></div><div className="text-sm sm:text-base">or</div><div className="border-b-1 border-gray-400 w-[45%]"></div>
                </div>
                <label htmlFor="Email" className="block mb-1 text-sm sm:text-base">Email</label>
                <input type="text" value={loginField.email} onChange={(e) => { onChangeInput(e, 'email') }} id="Email" placeholder="Email or Phone" className="text-gray-500 border-2 w-full text-base sm:text-lg md:text-xl py-2 px-3 rounded-xl border-black"></input>
                <div className="mt-3">
                    <label htmlFor="Password" className="block mb-1 text-sm sm:text-base">Password</label>
                    <input type="Password" value={loginField.password} onChange={(e) => { onChangeInput(e, 'password') }} id="Password" placeholder="Password" className="text-gray-500 border-2 w-full text-base sm:text-lg md:text-xl py-2 px-3 rounded-xl border-black"></input>
                </div>
                <div onClick={handleLogin} className="bg-blue-800 hover:bg-blue-900 rounded-xl w-full px-4 py-3 text-white text-center cursor-pointer mt-4 text-sm sm:text-base md:text-lg font-medium">
                    Log in
                </div>
            </div>
            <div className="mt-4 mb-6 sm:mb-8 md:mb-10 text-sm sm:text-base text-center px-4"> New to LinkedIn? <Link to={'/signUp'} className="text-blue-800 cursor-pointer hover:underline font-medium">Join Now</Link></div>
            <ToastContainer />
        </div>
    )
}