import React, { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import ProfileCard from "../../Profilecard/profilecard";
import Card from "../../components/card/card";
import Post from "../../components/Post/Post";
import Advertisment from "../../components/Advertisment/Advertisment";
import axios from "axios";
import { useParams } from "react-router-dom";

export default function SingleActivity() {
    const { id, postId } = useParams();

    const [post, setPost] = useState(null);
    const [ownDeta, setownDeta] = useState(null);
    const [loading, setLoading] = useState(true);

    const fetchDataOnLoad = async () => {
        try {
            const res = await axios.get(`http://localhost:5000/api/post/getPostById/${postId}`);
            console.log(res);
            setPost(res.data.post);
        } catch (err) {
            console.log(err);
            alert('Something went Wrong ');
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        fetchDataOnLoad();
        let ownDeta = localStorage.getItem('userInfo');
        setownDeta(ownDeta ? JSON.parse(ownDeta) : null);
    }, [])

    // Animation Variants
    const pageVariants = {
        hidden: { opacity: 0 },
        visible: {
            opacity: 1,
            transition: {
                duration: 0.3,
                when: "beforeChildren",
                staggerChildren: 0.15
            }
        }
    };

    const sidebarLeftVariants = {
        hidden: {
            opacity: 0,
            x: -50,
            scale: 0.95
        },
        visible: {
            opacity: 1,
            x: 0,
            scale: 1,
            transition: {
                type: "spring",
                stiffness: 100,
                damping: 15,
                duration: 0.6
            }
        }
    };

    const mainContentVariants = {
        hidden: {
            opacity: 0,
            y: 30,
            scale: 0.95
        },
        visible: {
            opacity: 1,
            y: 0,
            scale: 1,
            transition: {
                type: "spring",
                stiffness: 100,
                damping: 15,
                duration: 0.6
            }
        }
    };

    const sidebarRightVariants = {
        hidden: {
            opacity: 0,
            x: 50,
            scale: 0.95
        },
        visible: {
            opacity: 1,
            x: 0,
            scale: 1,
            transition: {
                type: "spring",
                stiffness: 100,
                damping: 15,
                duration: 0.6
            }
        }
    };

    const loadingVariants = {
        hidden: { opacity: 0 },
        visible: {
            opacity: 1,
            transition: { duration: 0.3 }
        },
        exit: {
            opacity: 0,
            scale: 0.9,
            transition: { duration: 0.2 }
        }
    };

    const shimmerVariants = {
        animate: {
            backgroundPosition: ["200% 0", "-200% 0"],
            transition: {
                duration: 2,
                repeat: Infinity,
                ease: "linear"
            }
        }
    };

    return (
        <div>
            <motion.div 
                className="flex px-5 xl:px-[250px] py-9 gap-5 w-full mt-5 bg-purple-200 h-full"
                variants={pageVariants}
                initial="hidden"
                animate="visible"
            >
                {/* Left Sidebar - Profile Card */}
                <motion.div 
                    className="w-[21%] sm:block sm:w-[23%] hidden py-[20px]"
                    variants={sidebarLeftVariants}
                >
                    <motion.div 
                        className="h-fit"
                        whileHover={{ 
                            scale: 1.02,
                            y: -5,
                            transition: { 
                                type: "spring", 
                                stiffness: 300, 
                                damping: 20 
                            }
                        }}
                    >
                        <AnimatePresence mode="wait">
                            {loading ? (
                                <motion.div
                                    key="loading-profile"
                                    variants={loadingVariants}
                                    initial="hidden"
                                    animate="visible"
                                    exit="exit"
                                    className="bg-white rounded-lg p-4 shadow-md"
                                >
                                    <motion.div
                                        className="w-20 h-20 mx-auto rounded-full bg-gradient-to-r from-gray-200 via-gray-300 to-gray-200"
                                        variants={shimmerVariants}
                                        animate="animate"
                                        style={{
                                            backgroundSize: "200% 100%"
                                        }}
                                    />
                                    <div className="mt-4 space-y-2">
                                        <motion.div
                                            className="h-4 bg-gradient-to-r from-gray-200 via-gray-300 to-gray-200 rounded"
                                            variants={shimmerVariants}
                                            animate="animate"
                                            style={{ backgroundSize: "200% 100%" }}
                                        />
                                        <motion.div
                                            className="h-3 w-3/4 bg-gradient-to-r from-gray-200 via-gray-300 to-gray-200 rounded"
                                            variants={shimmerVariants}
                                            animate="animate"
                                            style={{ backgroundSize: "200% 100%" }}
                                        />
                                    </div>
                                </motion.div>
                            ) : (
                                <motion.div
                                    key="profile-card"
                                    initial={{ opacity: 0, scale: 0.9 }}
                                    animate={{ opacity: 1, scale: 1 }}
                                    transition={{ duration: 0.3 }}
                                >
                                    <ProfileCard data={post?.user} />
                                </motion.div>
                            )}
                        </AnimatePresence>
                    </motion.div>
                </motion.div>

                {/* Middle Side - Main Post */}
                <motion.div 
                    className="w-[100%] py-5 sm:w-[50%]"
                    variants={mainContentVariants}
                >
                    <AnimatePresence mode="wait">
                        {loading ? (
                            <motion.div
                                key="loading-post"
                                variants={loadingVariants}
                                initial="hidden"
                                animate="visible"
                                exit="exit"
                                className="bg-white rounded-lg p-6 shadow-md"
                            >
                                <div className="flex items-center space-x-3 mb-4">
                                    <motion.div
                                        className="w-12 h-12 rounded-full bg-gradient-to-r from-gray-200 via-gray-300 to-gray-200"
                                        variants={shimmerVariants}
                                        animate="animate"
                                        style={{ backgroundSize: "200% 100%" }}
                                    />
                                    <div className="flex-1">
                                        <motion.div
                                            className="h-4 w-1/3 bg-gradient-to-r from-gray-200 via-gray-300 to-gray-200 rounded mb-2"
                                            variants={shimmerVariants}
                                            animate="animate"
                                            style={{ backgroundSize: "200% 100%" }}
                                        />
                                        <motion.div
                                            className="h-3 w-1/4 bg-gradient-to-r from-gray-200 via-gray-300 to-gray-200 rounded"
                                            variants={shimmerVariants}
                                            animate="animate"
                                            style={{ backgroundSize: "200% 100%" }}
                                        />
                                    </div>
                                </div>
                                <motion.div
                                    className="h-64 bg-gradient-to-r from-gray-200 via-gray-300 to-gray-200 rounded-lg mb-4"
                                    variants={shimmerVariants}
                                    animate="animate"
                                    style={{ backgroundSize: "200% 100%" }}
                                />
                                <div className="space-y-2">
                                    <motion.div
                                        className="h-3 bg-gradient-to-r from-gray-200 via-gray-300 to-gray-200 rounded"
                                        variants={shimmerVariants}
                                        animate="animate"
                                        style={{ backgroundSize: "200% 100%" }}
                                    />
                                    <motion.div
                                        className="h-3 w-5/6 bg-gradient-to-r from-gray-200 via-gray-300 to-gray-200 rounded"
                                        variants={shimmerVariants}
                                        animate="animate"
                                        style={{ backgroundSize: "200% 100%" }}
                                    />
                                </div>
                            </motion.div>
                        ) : (
                            <motion.div
                                key="post-content"
                                initial={{ opacity: 0, y: 20, scale: 0.95 }}
                                animate={{ opacity: 1, y: 0, scale: 1 }}
                                transition={{
                                    type: "spring",
                                    stiffness: 100,
                                    damping: 15
                                }}
                                whileHover={{
                                    y: -5,
                                    boxShadow: "0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)",
                                    transition: { duration: 0.2 }
                                }}
                            >
                                <Post item={post} personalData={ownDeta} />
                            </motion.div>
                        )}
                    </AnimatePresence>
                </motion.div>

                {/* Right Sidebar - Advertisement */}
                <motion.div 
                    className="w-[26%] hidden md:block"
                    variants={sidebarRightVariants}
                >
                    <motion.div 
                        className="my-5 sticky top-[76px]"
                        whileHover={{
                            scale: 1.02,
                            transition: {
                                type: "spring",
                                stiffness: 300,
                                damping: 20
                            }
                        }}
                    >
                        <AnimatePresence mode="wait">
                            {loading ? (
                                <motion.div
                                    key="loading-ad"
                                    variants={loadingVariants}
                                    initial="hidden"
                                    animate="visible"
                                    exit="exit"
                                    className="bg-white rounded-lg p-4 shadow-md"
                                >
                                    <motion.div
                                        className="h-48 bg-gradient-to-r from-gray-200 via-gray-300 to-gray-200 rounded-lg mb-3"
                                        variants={shimmerVariants}
                                        animate="animate"
                                        style={{ backgroundSize: "200% 100%" }}
                                    />
                                    <motion.div
                                        className="h-4 bg-gradient-to-r from-gray-200 via-gray-300 to-gray-200 rounded mb-2"
                                        variants={shimmerVariants}
                                        animate="animate"
                                        style={{ backgroundSize: "200% 100%" }}
                                    />
                                    <motion.div
                                        className="h-3 w-2/3 bg-gradient-to-r from-gray-200 via-gray-300 to-gray-200 rounded"
                                        variants={shimmerVariants}
                                        animate="animate"
                                        style={{ backgroundSize: "200% 100%" }}
                                    />
                                </motion.div>
                            ) : (
                                <motion.div
                                    key="ad-content"
                                    initial={{ opacity: 0, scale: 0.9 }}
                                    animate={{ opacity: 1, scale: 1 }}
                                    transition={{ duration: 0.3 }}
                                >
                                    <Advertisment />
                                </motion.div>
                            )}
                        </AnimatePresence>
                    </motion.div>
                </motion.div>
            </motion.div>
        </div>
    )
}