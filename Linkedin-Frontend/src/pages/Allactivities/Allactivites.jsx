import React, { useEffect, useState } from "react";
import { motion, AnimatePresence } from 'framer-motion';
import ProfileCard from "../../Profilecard/profilecard";
import Advertisment from "../../components/Advertisment/Advertisment";
import { useParams } from "react-router-dom";
import Card from "../../components/card/card";
import Post from "../../components/Post/Post";
import axios from "axios";
import "./Allactivities.css"; // <-- added CSS file import

export default function Allactivities() {
    const { id } = useParams();
    
    const [post, setPost] = useState([]);
    const [ownData, setOwnData] = useState(null);

    const fetchDataonLoad = async () => {
        await axios.get(`http://localhost:5000/api/post/UserPost/${id}`).then(res => {
            console.log(res);
            setPost(res.data.posts);
        }).catch(err => {
            console.log(err);
            alert(err?.res?.data?.error);
        });
    };

    useEffect(() => {
        fetchDataonLoad();
        let userData = localStorage.getItem('userInfo');
        setOwnData(userData ? JSON.parse(userData) : null);
    }, [id]);

    return (
        <motion.div 
            className="flex px-5 xl:px-[250px] py-9 gap-5 w-full mt-5 bg-purple-200"
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ 
                duration: 0.8, 
                ease: [0.43, 0.13, 0.23, 0.96]
            }}
        >
            {/* Left side - Profile Card with Bounce In */}
            <motion.div 
                className="w-[21%] sm:block sm:w-[23%] hidden py-[20px]"
                initial={{ scale: 0, rotate: -180 }}
                animate={{ scale: 1, rotate: 0 }}
                transition={{ 
                    delay: 0.3,
                    duration: 0.8,
                    type: "spring",
                    stiffness: 200,
                    damping: 15
                }}
            >
                <motion.div 
                    className="h-fit"
                    whileHover={{ 
                        scale: 1.05,
                        rotate: [0, -2, 2, -2, 0],
                        transition: { 
                            duration: 0.5,
                            rotate: {
                                repeat: Infinity,
                                duration: 2
                            }
                        }
                    }}
                >
                    <ProfileCard data={post[0]?.user}/>
                </motion.div>
            </motion.div>

            {/* Middle side - Posts */}
            <motion.div 
                className="w-[100%] py-5 sm:w-[50%]"
                initial={{ y: 100, opacity: 0 }}
                animate={{ y: 0, opacity: 1 }}
                transition={{ 
                    delay: 0.4,
                    duration: 0.7,
                    ease: "easeOut"
                }}
            >
                <motion.div
                    initial={{ rotateY: 90, opacity: 0 }}
                    animate={{ rotateY: 0, opacity: 1 }}
                    transition={{ 
                        delay: 0.6,
                        duration: 0.8,
                        type: "spring",
                        stiffness: 100
                    }}
                    style={{ transformStyle: "preserve-3d", perspective: "1200px" }}
                >
                    <Card padding={1}>
                        <motion.div 
                            className="text-xl"
                            initial={{ x: -100, opacity: 0 }}
                            animate={{ x: 0, opacity: 1 }}
                            transition={{ delay: 0.8, duration: 0.5 }}
                        >
                            All Activity
                        </motion.div>

                        <motion.div 
                            className="bg-green-800 w-fit p-2 border-1 rounded-4xl text-white font-semibold my-2 cursor-pointer"
                            initial={{ scale: 0, rotate: 360 }}
                            animate={{ scale: 1, rotate: 0 }}
                            transition={{ 
                                delay: 1,
                                duration: 0.6,
                                type: "spring",
                                stiffness: 200
                            }}
                            whileHover={{ 
                                scale: 1.1,
                                rotate: [0, 5, -5, 0],
                                boxShadow: "0 5px 15px rgba(0,128,0,0.3)",
                                transition: { duration: 0.3 }
                            }}
                            whileTap={{ scale: 0.9, rotate: -5 }}
                        >
                            Posts
                        </motion.div>

                        <div className="my-2 flex flex-col gap-2">
                            <AnimatePresence mode="wait">
                                {post && post.length > 0 ? (
                                    post.map((item, index) => (
                                        <motion.div 
                                            key={item._id}
                                            initial={{ 
                                                x: index % 2 === 0 ? -200 : 200,
                                                opacity: 0,
                                                rotateZ: index % 2 === 0 ? -20 : 20,
                                                scale: 0.5
                                            }}
                                            animate={{ 
                                                x: 0,
                                                opacity: 1,
                                                rotateZ: 0,
                                                scale: 1
                                            }}
                                            exit={{
                                                x: index % 2 === 0 ? 200 : -200,
                                                opacity: 0,
                                                scale: 0.5
                                            }}
                                            transition={{ 
                                                delay: 1.2 + (index * 0.15),
                                                duration: 0.6,
                                                type: "spring",
                                                stiffness: 120,
                                                damping: 20
                                            }}
                                            whileHover={{ 
                                                scale: 1.03,
                                                x: index % 2 === 0 ? 10 : -10,
                                                boxShadow: "0 10px 30px rgba(0,0,0,0.2)",
                                                transition: { duration: 0.3 }
                                            }}
                                        >
                                            <div className="post-wrapper">
                                                <Post profile={0} item={item} personalData={ownData} />
                                            </div>
                                        </motion.div>
                                    ))
                                ) : (
                                    <motion.div 
                                        className="text-gray-500 text-center py-4"
                                        initial={{ scale: 0, rotate: 180 }}
                                        animate={{ scale: 1, rotate: 0 }}
                                        transition={{ 
                                            delay: 1.2,
                                            duration: 0.6,
                                            type: "spring"
                                        }}
                                    >
                                        No posts available
                                    </motion.div>
                                )}
                            </AnimatePresence>
                        </div>
                    </Card>
                </motion.div>
            </motion.div>

            {/* Right side - Advertisement with Swing */}
            <motion.div 
                className="w-[26%] hidden md:block"
                initial={{ 
                    x: 300,
                    opacity: 0,
                    rotate: 45,
                    scale: 0.5
                }}
                animate={{ 
                    x: 0,
                    opacity: 1,
                    rotate: 0,
                    scale: 1
                }}
                transition={{ 
                    delay: 0.5,
                    duration: 1,
                    type: "spring",
                    stiffness: 80,
                    damping: 15
                }}
            >
                <motion.div 
                    className="my-5 sticky top-[76px]"
                    whileHover={{ 
                        scale: 1.05,
                        rotate: [0, -3, 3, -3, 0],
                        transition: { 
                            duration: 0.5,
                            repeat: Infinity,
                            repeatDelay: 1
                        }
                    }}
                >
                    <Advertisment />
                </motion.div>
            </motion.div>
        </motion.div>
    );
}
