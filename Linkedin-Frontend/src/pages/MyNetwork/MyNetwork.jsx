import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import ProfileCard from "../../Profilecard/profilecard";
import axios from "axios";

export default function MyNetwork() {
    const [text, setText] = useState("Catch Up With Friends");
    const [data, setData] = useState([]);

    const handleFriends = async () => {
        setText("Catch Up With Friends");
    };

    const handlePending = async () => {
        setText("Pending Request");
    };

    const fetchfriendList = async () => {
        await axios.get(`${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/friendList`, { withCredentials: true })
            .then((res) => {
                console.log(res);
                setData(res.data.friends);
            })
            .catch(err => {
                console.log(err);
                alert("Something went Wrong");
            });
    };

    const fetchPendingRequest = async () => {
        await axios.get(`${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/pendingfriendList`, { withCredentials: true })
            .then((res) => {
                console.log(res);
                setData(res.data.pending_friends);
            })
            .catch(err => {
                console.log(err);
                alert("Something went Wrong");
            });
    };

    useEffect(() => {
        if (text === "Catch Up With Friends") {
            fetchfriendList();
        } else {
            fetchPendingRequest();
        }
    }, [text]);

    // Animation Variants
    const containerVariants = {
        hidden: { opacity: 0 },
        visible: {
            opacity: 1,
            transition: {
                staggerChildren: 0.1,
                delayChildren: 0.2
            }
        }
    };

    const headerVariants = {
        hidden: { y: -50, opacity: 0 },
        visible: {
            y: 0,
            opacity: 1,
            transition: {
                type: 'spring',
                stiffness: 100,
                damping: 15
            }
        }
    };

    const titleVariants = {
        hidden: { x: -30, opacity: 0 },
        visible: {
            x: 0,
            opacity: 1,
            transition: {
                type: 'spring',
                stiffness: 120
            }
        },
        exit: {
            x: 30,
            opacity: 0,
            transition: { duration: 0.2 }
        }
    };

    // Cards slide from alternating sides
    const getCardVariants = (index) => ({
        hidden: { 
            x: index % 2 === 0 ? -100 : 100,
            opacity: 0
        },
        visible: {
            x: 0,
            opacity: 1,
            transition: {
                type: 'spring',
                stiffness: 100,
                damping: 15,
                delay: index * 0.05
            }
        },
        exit: {
            x: index % 2 === 0 ? -100 : 100,
            opacity: 0,
            transition: { duration: 0.3 }
        }
    });

    const emptyStateVariants = {
        hidden: { 
            scale: 0.5,
            opacity: 0,
            y: 50
        },
        visible: {
            scale: 1,
            opacity: 1,
            y: 0,
            transition: {
                type: 'spring',
                stiffness: 150,
                damping: 15
            }
        }
    };

    const pulseVariants = {
        animate: {
            scale: [1, 1.05, 1],
            transition: {
                duration: 2,
                repeat: Infinity,
                ease: "easeInOut"
            }
        }
    };

    return (
        <motion.div 
            className="px-[25px] xl:px-[200px] py-[36px] flex flex-col gap-[20px] w-full bg-purple-200 mt-5"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.5 }}
        >
            <motion.div 
                className="py-4 px-10 rounded-xl border-2 border-gray-400 w-full mt-5 flex bg-white justify-between"
                variants={headerVariants}
                initial="hidden"
                animate="visible"
                whileHover={{ 
                    boxShadow: '0 10px 30px rgba(0,0,0,0.1)',
                    y: -2
                }}
                transition={{ duration: 0.3 }}
            >
                <AnimatePresence mode="wait">
                    <motion.div 
                        key={text}
                        className="text-xl"
                        variants={titleVariants}
                        initial="hidden"
                        animate="visible"
                        exit="exit"
                    >
                        {text}
                    </motion.div>
                </AnimatePresence>

                <div className="flex gap-3">
                    <motion.button 
                        onClick={handleFriends} 
                        className={`rounded-sm border-2 cursor-pointer border-gray-200 p-1 ${text === "Catch Up With Friends" ? `bg-purple-900 text-white` : ``}`}
                        whileHover={{ 
                            scale: 1.05,
                            boxShadow: '0 5px 15px rgba(88, 28, 135, 0.3)'
                        }}
                        whileTap={{ scale: 0.95 }}
                        animate={text === "Catch Up With Friends" ? {
                            boxShadow: ['0 0 0 0 rgba(88, 28, 135, 0.4)', '0 0 0 10px rgba(88, 28, 135, 0)'],
                        } : {}}
                        transition={{ duration: 0.6 }}
                    >
                        Friends
                    </motion.button>
                    <motion.button 
                        onClick={handlePending} 
                        className={`border-2 border-gray-200 rounded-sm cursor-pointer p-1 ${text === "Pending Request" ? `bg-purple-900 text-white` : ``}`}
                        whileHover={{ 
                            scale: 1.05,
                            boxShadow: '0 5px 15px rgba(88, 28, 135, 0.3)'
                        }}
                        whileTap={{ scale: 0.95 }}
                        animate={text === "Pending Request" ? {
                            boxShadow: ['0 0 0 0 rgba(88, 28, 135, 0.4)', '0 0 0 10px rgba(88, 28, 135, 0)'],
                        } : {}}
                        transition={{ duration: 0.6 }}
                    >
                        Pending Request
                    </motion.button>
                </div>
            </motion.div>

            <motion.div 
                className="flex h-[80vh] w-full gap-7 flex-wrap items-start justify-center"
                variants={containerVariants}
                initial="hidden"
                animate="visible"
            >
                <AnimatePresence mode="wait">
                    {data?.length > 0 ? (
                        data.map((item, index) => (
                            <motion.div 
                                key={item._id || index} 
                                className="md:w-[23%] h-[270px] sm:w-full"
                                variants={getCardVariants(index)}
                                initial="hidden"
                                animate="visible"
                                exit="exit"
                                whileHover={{ 
                                    scale: 1.05,
                                    y: -5,
                                    boxShadow: '0 15px 40px rgba(0,0,0,0.15)',
                                    transition: { duration: 0.3 }
                                }}
                            >
                                <ProfileCard data={item} />
                            </motion.div>
                        ))
                    ) : (
                        <motion.div
                            key="empty-state"
                            variants={emptyStateVariants}
                            initial="hidden"
                            animate="visible"
                            className="flex flex-col items-center justify-center gap-4 mt-20"
                        >
                            <motion.div
                                variants={pulseVariants}
                                animate="animate"
                                className="text-6xl"
                            >
                                👥
                            </motion.div>
                            <motion.div 
                                className="text-xl text-gray-600"
                                initial={{ opacity: 0, y: 20 }}
                                animate={{ opacity: 1, y: 0 }}
                                transition={{ delay: 0.3 }}
                            >
                                {text === "Catch Up With Friends" ? "No Friends Yet" : "No Pending Requests"}
                            </motion.div>
                            <motion.div 
                                className="text-sm text-gray-400"
                                initial={{ opacity: 0, y: 20 }}
                                animate={{ opacity: 1, y: 0 }}
                                transition={{ delay: 0.5 }}
                            >
                                Start connecting with people to grow your network
                            </motion.div>
                        </motion.div>
                    )}
                </AnimatePresence>
            </motion.div>
        </motion.div>
    );
}