import React, { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import Card from "../../components/card/card";
import Advertisment from "../../components/Advertisment/Advertisment";
import ProfileCard from "../../Profilecard/profilecard";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function Notification() {
    const navigate = useNavigate();
    const [ownData, setOwnData] = useState(null);
    const [notification, setNotification] = useState([]);
    const [loading, setLoading] = useState(true);

    const fetchNotificationData = async () => {
        try {
            const res = await axios.get('http://localhost:5000/api/Notification', { 
                withCredentials: true 
            });
            setNotification(res.data.notification);
        } catch (err) {
            console.error("Fetch error:", err);
            alert("Failed to load notifications");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        let userData = localStorage.getItem('userInfo');
        setOwnData(userData ? JSON.parse(userData) : null);
        fetchNotificationData();
    }, []);

    const handleOnClickNotification = async (item) => {
        try {
            await axios.put(
                'http://localhost:5000/api/Notification/isRead',
                { notificationId: item._id },
                { withCredentials: true }
            );
            
            setNotification(prev => 
                prev.map(notif => 
                    notif._id === item._id 
                        ? { ...notif, isRead: true } 
                        : notif
                )
            );
            
            if (item.type === "comment" && item.postId) {
                navigate(`/profile/${item.receiver._id}/activities/${item.postId}`);
            } else if (item.type === "friendrequest") {
                navigate('/mynetwork');
            }
            
        } catch (err) {
            console.error("Error:", err);
            alert("Failed to process notification");
        }
    }

    // Animation variants
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

    const sidebarVariants = {
        hidden: { 
            opacity: 0, 
            x: -30 
        },
        visible: {
            opacity: 1,
            x: 0,
            transition: {
                type: "spring",
                stiffness: 100,
                damping: 15
            }
        }
    };

    const cardVariants = {
        hidden: { 
            opacity: 0, 
            y: 20,
            scale: 0.95
        },
        visible: {
            opacity: 1,
            y: 0,
            scale: 1,
            transition: {
                type: "spring",
                stiffness: 100,
                damping: 15
            }
        }
    };

    const notificationItemVariants = {
        hidden: { 
            opacity: 0, 
            x: -20,
            scale: 0.95
        },
        visible: (index) => ({
            opacity: 1,
            x: 0,
            scale: 1,
            transition: {
                type: "spring",
                stiffness: 100,
                damping: 15,
                delay: index * 0.05
            }
        }),
        exit: {
            opacity: 0,
            x: 20,
            scale: 0.95,
            transition: {
                duration: 0.2
            }
        }
    };

    const adVariants = {
        hidden: { 
            opacity: 0, 
            x: 30 
        },
        visible: {
            opacity: 1,
            x: 0,
            transition: {
                type: "spring",
                stiffness: 100,
                damping: 15,
                delay: 0.3
            }
        }
    };

    const loadingVariants = {
        hidden: { opacity: 0 },
        visible: {
            opacity: 1,
            transition: {
                duration: 0.3
            }
        }
    };

    const emptyStateVariants = {
        hidden: { opacity: 0, scale: 0.9 },
        visible: {
            opacity: 1,
            scale: 1,
            transition: {
                type: "spring",
                stiffness: 100,
                damping: 15
            }
        }
    };

    return (
        <motion.div 
            className="flex mt-5 py-9 w-full bg-purple-200 px-[50px] xl-px-[250px]"
            variants={containerVariants}
            initial="hidden"
            animate="visible"
        >
            {/* Left Sidebar - Profile Card */}
            <motion.div 
                className="w-[21%] px-10 sm:block sm:w-[23%] hidden py-5"
                variants={sidebarVariants}
            >
                <motion.div 
                    className="h-fit"
                    whileHover={{ scale: 1.02 }}
                    transition={{ type: "spring", stiffness: 300, damping: 20 }}
                >
                    <ProfileCard data={ownData} />
                </motion.div>
            </motion.div>
            
            {/* Main Content - Notifications */}
            <motion.div 
                className="w-[100%] py-5 sm:w-[50%]"
                variants={cardVariants}
            >
                <Card padding={0}>
                    <div className="w-full">
                        <AnimatePresence mode="wait">
                            {loading ? (
                                <motion.div 
                                    className="p-8 text-center text-gray-500"
                                    variants={loadingVariants}
                                    initial="hidden"
                                    animate="visible"
                                    exit="hidden"
                                    key="loading"
                                >
                                    <motion.div
                                        animate={{ rotate: 360 }}
                                        transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                                        className="inline-block w-8 h-8 border-4 border-purple-500 border-t-transparent rounded-full"
                                    />
                                    <p className="mt-4">Loading notifications...</p>
                                </motion.div>
                            ) : notification.length > 0 ? (
                                <motion.div key="notifications">
                                    {notification.map((item, index) => (
                                        <motion.div 
                                            key={item._id}
                                            custom={index}
                                            variants={notificationItemVariants}
                                            initial="hidden"
                                            animate="visible"
                                            exit="exit"
                                            onClick={() => handleOnClickNotification(item)}
                                            className={`border-b border-gray-200 cursor-pointer flex gap-4 items-center p-4 transition-colors ${
                                                item?.isRead ? 'bg-white' : 'bg-blue-50'
                                            }`}
                                            whileHover={{ 
                                                backgroundColor: item?.isRead ? "#f9fafb" : "#dbeafe",
                                                x: 4,
                                                transition: { duration: 0.2 }
                                            }}
                                            whileTap={{ scale: 0.98 }}
                                        >
                                            <motion.img 
                                                src={item?.sender?.profile_pic} 
                                                className="rounded-full w-12 h-12 object-cover flex-shrink-0"
                                                alt={item?.sender?.f_name || "User"}
                                                whileHover={{ scale: 1.1 }}
                                                transition={{ type: "spring", stiffness: 300, damping: 20 }}
                                            />
                                            <div className="flex-1 min-w-0">
                                                <p className="text-sm text-gray-800">
                                                    {item?.content}
                                                </p>
                                                <p className="text-xs text-gray-500 mt-1">
                                                    {new Date(item?.createdAt).toLocaleString()}
                                                </p>
                                            </div>
                                            {!item?.isRead && (
                                                <motion.div 
                                                    className="w-2 h-2 bg-blue-600 rounded-full flex-shrink-0"
                                                    initial={{ scale: 0 }}
                                                    animate={{ scale: [1, 1.2, 1] }}
                                                    transition={{ 
                                                        duration: 2,
                                                        repeat: Infinity,
                                                        ease: "easeInOut"
                                                    }}
                                                />
                                            )}
                                        </motion.div>
                                    ))}
                                </motion.div>
                            ) : (
                                <motion.div 
                                    className="p-8 text-center text-gray-500"
                                    variants={emptyStateVariants}
                                    initial="hidden"
                                    animate="visible"
                                    key="empty"
                                >
                                    <motion.div
                                        animate={{ y: [0, -10, 0] }}
                                        transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
                                    >
                                        <svg className="w-16 h-16 mx-auto mb-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                                        </svg>
                                    </motion.div>
                                    <p>No notifications yet</p>
                                </motion.div>
                            )}
                        </AnimatePresence>
                    </div>
                </Card>
            </motion.div>
            
            {/* Right Sidebar - Advertisement */}
            <motion.div 
                className="px-10"
                variants={adVariants}
            >
                <Advertisment />
            </motion.div>
        </motion.div>
    );
}