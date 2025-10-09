import React from "react";
import { GoogleLogin } from '@react-oauth/google';
import axios from "axios";
import {useNavigate} from 'react-router-dom'

export default function GoogleLoginComp(props) {
    const navigate = useNavigate();

    const handleOnSuccess = async (credResponse) => {
        const token=credResponse.credential;
       const res = await axios.post(
  `${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/google`,
  { token },
  { withCredentials: true }
);
        // console.log(res);
        localStorage.setItem('isLogin','true');
        localStorage.setItem('userInfo',JSON.stringify(res.data.user));
        props.changeLoginValue(true)
          navigate('/feed')
    }
    return (
        <div>
            <GoogleLogin
                onSuccess={(credentialResponse) => handleOnSuccess(credentialResponse)}
                onError={() => {
                    console.log('Login Failed');
                }}
            />
        </div>
    )
}