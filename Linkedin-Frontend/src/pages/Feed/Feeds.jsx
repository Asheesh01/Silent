import React, { useState, useEffect, useRef } from "react";
import ProfileCard from "../../Profilecard/profilecard";
import Card from "../../components/card/card";
import OndemandVideoIcon from '@mui/icons-material/OndemandVideo';
import AddPhotoAlternateIcon from '@mui/icons-material/AddPhotoAlternate';
import ArticleIcon from '@mui/icons-material/Article';
import Advertisment from "../../components/Advertisment/Advertisment";
import Post from "../../components/Post/Post";
import Model from "../../components/Model/Model";
import AddModel from "../../components/Advertisment/AddModel";
import Loadr from "../../components/Loader/Loader";
import axios from "axios";
import { ToastContainer, toast } from "react-toastify";
import { motion } from "framer-motion";

export default function Feeds() {

  const [personalData, setPersonaltData] = useState(null);
  const [post, setPost] = useState([]);
  const [addPostModel, setAddPostModel] = useState(false);

  const fetchData = async () => {
    try {
      const userData = await axios.get(
        `${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/self`,
        { withCredentials: true }
      );

      const postData = await axios.get(
        `${import.meta.env.VITE_APP_BACKEND_URL}/api/post/getAllPost/${userData.data.user._id}`
      );

      setPersonaltData(userData.data.user);
      localStorage.setItem('userInfo', JSON.stringify(userData.data.user));
      setPost(postData.data.posts);
    } catch (err) {
      console.log(err);
      toast.error(err?.response?.data?.error);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleopenmodel = () => {
    setAddPostModel(prev => !prev);
  };

  return (
    <div className="bg-purple-200 px-[25px] xl:px-[200px] py-[30px] flex gap-[20px] w-full mt-5 min-h-screen">

      {/* Left Side */}
      <motion.div 
        className="w-[21%] sm:block sm:w-[21%] hidden py-[20px]"
        initial={{ x: -300, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        transition={{ duration: 0.8, type: "spring", stiffness: 80 }}
      >
        <div className="h-fit">
          <ProfileCard data={personalData} />
        </div>
      </motion.div>

      {/* Middle Section - With Background */}
      <div 
        className="w-[100%] py-[20px] sm:w-[50%] overflow-y-auto max-h-[90vh] bg-purple-200 rounded-xl pr-2"
      >
        {/* Top Card - Slides from LEFT */}
        <motion.div
          initial={{ x: -400, opacity: 0, rotateY: -45 }}
          animate={{ x: 0, opacity: 1, rotateY: 0 }}
          transition={{ 
            duration: 0.8, 
            type: "spring", 
            stiffness: 70,
            delay: 0.2 
          }}
          style={{ transformStyle: "preserve-3d" }}
        >
          <Card padding={1}>
            <div className="flex gap-2 items-center">
              <motion.img
                whileHover={{ scale: 1.15, rotate: 360 }}
                whileTap={{ scale: 0.9 }}
                transition={{ duration: 0.5 }}
                className="rounded-4xl w-[52px] h-[52px] border-2 border-white cursor-pointer"
                src={personalData?.profile_pic}
                alt=""
              />
              <motion.div
                onClick={() => setAddPostModel(true)}
                whileHover={{ scale: 1.03, x: 5 }}
                whileTap={{ scale: 0.97 }}
                className="w-full border-1 py-3 px-3 rounded-3xl cursor-pointer hover:bg-gray-100"
              >
                Start a post
              </motion.div>
            </div>

            <div className="w-full flex mt-3">
              <motion.div
                onClick={() => setAddPostModel(true)}
                whileHover={{ scale: 1.1, y: -8, rotate: 5 }}
                whileTap={{ scale: 0.9, rotate: -5 }}
                transition={{ type: "spring", stiffness: 300 }}
                className="flex p-2 gap-2 cursor-pointer w-[33%] justify-center rounded-lg hover:bg-gray-100"
              >
                <OndemandVideoIcon sx={{ color: "green" }} /> Video
              </motion.div>
              <motion.div
                onClick={() => setAddPostModel(true)}
                whileHover={{ scale: 1.1, y: -8, rotate: 5 }}
                whileTap={{ scale: 0.9, rotate: -5 }}
                transition={{ type: "spring", stiffness: 300, delay: 0.05 }}
                className="flex p-2 gap-2 cursor-pointer w-[33%] justify-center rounded-lg hover:bg-gray-100"
              >
                <AddPhotoAlternateIcon sx={{ color: "blue" }} /> Photo
              </motion.div>
              <motion.div
                onClick={() => setAddPostModel(true)}
                whileHover={{ scale: 1.1, y: -8, rotate: 5 }}
                whileTap={{ scale: 0.9, rotate: -5 }}
                transition={{ type: "spring", stiffness: 300, delay: 0.1 }}
                className="flex p-2 gap-2 cursor-pointer w-[33%] justify-center rounded-lg hover:bg-gray-100"
              >
                <ArticleIcon sx={{ color: "orange" }} /> Article
              </motion.div>
            </div>
          </Card>
        </motion.div>

        {/* Divider Line - Slides from TOP */}
        <motion.div 
          className="w-[100%] my-5 border-b-1 border-gray-400"
          initial={{ y: -50, opacity: 0, scaleX: 0 }}
          animate={{ y: 0, opacity: 1, scaleX: 1 }}
          transition={{ 
            duration: 0.6, 
            delay: 0.5,
            type: "spring",
            stiffness: 100 
          }}
        ></motion.div>

        {/* Posts Section - Only initial animation, NO hover after appearance */}
        <div className="flex flex-col gap-4">
          {post.length > 0 ? (
            post.map((item, index) => (
              <motion.div
                key={item._id}
                initial={{ 
                  opacity: 0, 
                  y: 150, 
                  rotateX: 45, 
                  scale: 0.5,
                  filter: "blur(10px)"
                }}
                animate={{ 
                  opacity: 1, 
                  y: 0, 
                  rotateX: 0, 
                  scale: 1,
                  filter: "blur(0px)"
                }}
                transition={{ 
                  delay: 1.0 + index * 0.25,
                  duration: 1.2,
                  type: "spring",
                  stiffness: 80,
                  damping: 20
                }}
                style={{ 
                  transformStyle: "preserve-3d",
                  perspective: 1200
                }}
              >
                <Post item={item} personalData={personalData} />
              </motion.div>
            ))
          ) : (
            <motion.div
              className="text-center text-gray-600 font-medium"
              initial={{ opacity: 0, scale: 0.9, y: 50 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 1.0 }}
            >
              No posts available
            </motion.div>
          )}
        </div>
      </div>

      {/* Right Side */}
      <motion.div 
        className="my-5 sticky top-[76px]"
        initial={{ x: 300, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        transition={{ duration: 0.8, type: "spring", stiffness: 80 }}
      >
        <Advertisment />
      </motion.div>

      {/* Add Post Modal */}
      {addPostModel && (
        <Model closeModel={handleopenmodel} title={" "}>
          <AddModel personalData={personalData} />
        </Model>
      )}

      <ToastContainer />
    </div>
  );
}