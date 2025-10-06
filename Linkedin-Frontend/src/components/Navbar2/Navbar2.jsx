import React, { useState, useEffect, useRef } from "react";
import './Navbar2.css'
import HomeIcon from '@mui/icons-material/Home';
import PeopleAltIcon from '@mui/icons-material/PeopleAlt';
import WorkIcon from '@mui/icons-material/Work';
import MessageIcon from '@mui/icons-material/Message';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { Link, useLocation } from "react-router-dom";
import axios from "axios";
export default function Navbar2() {
  // const [dropdown, Setdropdown] = useState(false);

  const location = useLocation();
  const [userData, setUserData] = useState(null);

  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedTerm, setDebouncedTerm] = useState('')
  const [searchUser, setSearchUser] = useState([])

  const [notificationcount, setNotificationcount] = useState(" ");
  const searchRef = useRef(null); //  Reference for input box

  // Listen for event from Advertisment
  useEffect(() => {
    const handleFocusEvent = () => {
      if (searchRef.current) {
        searchRef.current.focus();
      }
    };
    window.addEventListener("focusSearch", handleFocusEvent);
    return () => window.removeEventListener("focusSearch", handleFocusEvent);
  }, []);
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedTerm(searchTerm);
    }, 1000)
    return () => {
      clearTimeout(handler)
    }
  }, [searchTerm])


  useEffect(() => {
    if (debouncedTerm) {
      searchAPICall();
    }
  }, [debouncedTerm])



  const searchAPICall = async () => {
    await axios.get(`http://localhost:5000/api/auth/findUser?query=${debouncedTerm}`, { withCredentials: true }).then((res => {
      console.log(res);
      setSearchUser(res.data.user)
    })).catch(err => {
      console.log(err)
      alert(err?.response?.data?.error)
    })
  }

  const fetchNotification = async () => {
    await axios.get('http://localhost:5000/api/Notification/aciveNotification', { withCredentials: true }).then((res) => {
      var count = (res.data.count)
      setNotificationcount(count);
    }).catch(err => {
      console.log(err)
      alert(err?.response?.data?.error)
    })
  }

  useEffect(() => {
    try {
      const userDataString = localStorage.getItem('userInfo');

      // Check if data exists and is not the string "undefined"
      if (userDataString && userDataString !== "undefined") {
        setUserData(JSON.parse(userDataString));
      }
      fetchNotification();
    } catch (error) {
      console.error('Error parsing user data from localStorage:', error);
      localStorage.removeItem('userInfo'); // Clean up corrupted data
    }
  }, []);
  return (
    <div className="bg-white h-[52px] flex justify-between py-1 xl:px-[208px] fixed top-0 w-[100%] z-[1000]">
      <div className="flex gap-2 items-center" >
        <Link to={'/feed'}>
          <img className="w-7 h-8" src="https://cdn.pixabay.com/photo/2020/05/25/17/21/link-5219567_640.jpg" alt="logo" />
        </Link>
        <div className="relative">
          <input ref={searchRef} value={searchTerm} onChange={(e) => { setSearchTerm(e.target.value) }} className="searchInput w-[300px]  bg-gray-100 rounded-sm h-[35px] px-4" type="text" placeholder="Search" />
          {
            searchUser.length > 0 && debouncedTerm.length !== 0 && <div className="absolute w-[calc(100vw-2rem)] sm:w-[352px]     transition-colors hover:bg-gray-100  left-0 bg-gray-200 rounded-2xl">
              {
                searchUser.map((item, index) => {
                  return (
                    <Link to={`/profile/${item._id}`} className="flex gap-2 items-center cursor-pointer " onClick={() => setSearchTerm("")}>
                      <div><img className="w[40px] h-[40px] rounded-full" src={item?.profile_pic} /></div>
                      <div className="rounded-2xl">
                        {item?.f_name}
                      </div>
                    </Link>
                  );
                })
              }
            </div>
          }
        </div>

      </div>

      <Link to={'/feed'} className="hidden gap-[40px] md:flex">
        <div className="flex flex-col items-center cursor-pointer">
          <HomeIcon sx={{ color: location.pathname === '/feed' ? "black" : "gray" }} />
          <div className={`text-sm text-gray-500 ${location.pathname === '/feed' ? "border-b-3" : ""}`}>
            Home
          </div>
        </div>
      </Link>
      <Link to={'/mynetwork'} className="flex flex-col items-center cursor-pointer">
        < PeopleAltIcon sx={{ color: location.pathname === '/mynetwork' ? "black" : "gray" }} />
        <div className={`text-sm text-gray-500 ${location.pathname === '/mynetwork' ? "border-b-3" : ""}`}>
          My network
        </div>
      </Link>

      <Link to={'/resume'} className="flex flex-col items-center cursor-pointer">
        < WorkIcon sx={{ color: location.pathname === '/resume' ? "black" : "gray" }} />
        <div className={`text-sm text-gray-500 ${location.pathname === '/resume' ? "border-b-3" : ""}`}>
          Resume
        </div>
      </Link>

      <Link to={'/message'} className="flex flex-col items-center cursor-pointer">
        <MessageIcon sx={{ color: location.pathname === '/message' ? "black" : "gray" }} />
        <div className={`text-sm text-gray-500 ${location.pathname === '/message' ? "border-b-3" : " "}`}>
          Message
        </div>
      </Link>

      <Link to={'/Notifications'} className="flex flex-col items-center cursor-pointer">
        <div> <NotificationsIcon sx={{ color: location.pathname === '/Notifications' ? "black" : "gray" }} />
          {
            notificationcount && <span className="p-1 rounded-full text-sm bg-red-700 text-white">{notificationcount}</span>

          } </div>
        <div className={`text-sm text-gray-500 ${location.pathname === '/Notifications' ? "border-b-3" : ""}`}>
          Notification
        </div>
      </Link>


      <Link to={`/profile/${userData?._id}`} className="flex flex-col items-center cursor-pointer">
        <img className="w-[30px] h-[30px] rounded-full" src={userData?.profile_pic} alt="Profile" />
        <div className="text-sm text-gray-500">
          Me
        </div>
      </Link>


    </div>
  )
}