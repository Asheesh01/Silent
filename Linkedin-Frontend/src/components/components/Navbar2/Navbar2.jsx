import React, { useState } from "react";
import './Navbar2.css'
import HomeIcon from '@mui/icons-material/Home';
import PeopleAltIcon from '@mui/icons-material/PeopleAlt';
import WorkIcon from '@mui/icons-material/Work';
import MessageIcon from '@mui/icons-material/Message';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { Link, useLocation } from "react-router-dom";
export default function Navbar2() {
  const [dropdown, Setdropdown] = useState(false);

  const location = useLocation();
  return (
    <div className="bg-white h-[52px] flex justify-between py-1 xl:px-[208px] fixed top-0 w-[100%] z-[1000]">
      <div className="flex gap-2 items-center" >
        <Link to={'/feed'}>
          <img className="w-7 h-8" src="https://th.bing.com/th/id/R.abdb36b128f0cfcee1329ddb1365a99b?rik=Q8UtGzuevu7ZBw&riu=http%3a%2f%2flofrev.net%2fwp-content%2fphotos%2f2017%2f04%2flinkedin_logo.jpg&ehk=WX0fSjGgisCu4YfNc2IBnr7nLADE%2f06resHyt%2fqG1pg%3d&risl=&pid=ImgRaw&r=0" alt="logo" />
        </Link>
        <div className="relative">
          <input className="searchInput w-[300px]  bg-gray-100 rounded-sm h-[35px] px-4" type="text" placeholder="Search" />
          {
            dropdown && <div className="absolute w-[352px]  left-0 bg-gray-200">
              <div className="flex gap-2 items-center cursor-pointer">
                <div><img className="w[40px] h-[40px] rounded-full" src="" /></div>
                <div>
                  Asheesh
                </div>
              </div>
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
            <span className="p-1 rounded-full text-sm bg-red-700 text-white">1</span>
          </div>
          <div className={`text-sm text-gray-500 ${location.pathname === '/Notifications' ? "border-b-3" : ""}`}>
            Notification
          </div>
        </Link>


        <Link to={'/profile/noihf'} className="flex flex-col items-center cursor-pointer">
          <img className="w-[30px] h-[30px] rounded-full" src="https://images.pexels.com/photos/712513/pexels-photo-712513.jpeg?cs=srgb&dl=pexels-olly-712513.jpg&fm=jpg" alt="Profile" />
          <div className="text-sm text-gray-500">
            Me
          </div>
        </Link>
      </Link>

    </div>
  )
}