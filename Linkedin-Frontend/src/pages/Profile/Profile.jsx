import { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import Advertisment from "../../components/Advertisment/Advertisment";
import Card from "../../components/card/card";
import EditIcon from '@mui/icons-material/Edit';
import Post from "../../components/Post/Post";
import AddIcon from '@mui/icons-material/Add';
import Model from "../../components/Model/Model";
import ImageModal from "../../components/ImageModel/ImageModal";
import EditInfoModel from "../../components/EditInfoModel/EditInfoModel";
import AboutModal from "../../components/AboutModal/AboutModal";
import ExpModal from "../../components/expModal/expModal";
import MessageModal from "../../components/MesssageModel/MessageModel";
import ArrowRightAltIcon from '@mui/icons-material/ArrowRightAlt';
import { Link, useParams } from "react-router-dom";
import axios from "axios";
import { ToastContainer, toast } from "react-toastify";

export default function Profile() {

    const { id } = useParams()
    const [ImageModal1, setImageModal] = useState(false);

    const [circularImage, setCircularImage] = useState(true);
    const [infoModal, setInfoModal] = useState(false);
    const [aboutModal, setAboutModal] = useState(false);
    const [expModal, setExpModal] = useState(false);
    const [messageModel, setMessageModal] = useState(false);

    const [userData, setUserData] = useState(null);
    const [postData, setPostdata] = useState([]);
    const [ownData, setOwnData] = useState(null);

    const [updateExp, setUpdateExp] = useState({ clicked: "", id: "", datas: {} });
    const updateExpEdit = (id, data) => {
        setUpdateExp({
            ...updateExp,
            clicked: true, id: id, data: data
        })
        setExpModal(prev => !prev)
    }

    useEffect(() => {
        fetchDataonLoad()
    }, [id])


    const fetchDataonLoad = async () => {
        try {
            const [userDatas, postDatas, ownDatas] = await Promise.all([
                axios.get(`${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/user/${id}`),
                axios.get(`${import.meta.env.VITE_APP_BACKEND_URL}/api/post/getTop5Post/${id}`),
                axios.get(`${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/self`, { withCredentials: true })
            ])

            setUserData(userDatas.data.user);
            setPostdata(postDatas.data.post);
            setOwnData(ownDatas.data.user);

            localStorage.setItem('userInfo', JSON.stringify(ownDatas.data.user));
        }
        catch (err) {
            console.log(err);
            alert("something went wrong")
        }
    }

    const messagemodel = () => {
        setMessageModal(prev => !prev);
    }


    const exphandleModal = () => {
        if (expModal) {
            setUpdateExp({ clicked: "", id: "", datas: {} })
        }
        setExpModal(prev => !prev);
    }
    const handleAboutModal = () => {
        setAboutModal(prev => !prev);

    }
    const handleimagemodalopenclose = () => {
        setImageModal(prev => !prev);
    }

    const handleoneditecover = () => {
        setImageModal(true);
        setCircularImage(false);

    }



    const handleCircularimageopen = () => {
        setImageModal(true);
        setCircularImage(true);
    }

    const handleinfomodel = () => {
        setInfoModal(prev => !prev);
    }

    const handleEditFunc = async (data) => {
        await axios.put(`${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/update`, { user: data }, { withCredentials: true }).then(res => {
            window.location.reload();

        }).catch(err => {
            console.log(err)
            alert("something went wrong")
        })


    }

    const amIFriend = () => {
        return userData?.friends?.includes(ownData._id) ?? false;
    }


    const isInPendingList = () => {

        let arr = userData?.pending_friends?.filter((item) => { return item === ownData._id })
        return arr?.length;
    }

    const isInSelfPendingList = () => {

        let arr = ownData?.pending_friends?.filter((item) => { return item === userData._id })
        return arr?.length;
    }


    const checkFriendStatus = () => {
        if (amIFriend()) {
            return "Disconnect"
        }
        else if (isInPendingList()) {
            return "Request Sent"
        }
        else if (isInSelfPendingList()) {
            return "Approve Request"
        }
        else {
            return "Connect"
        }

    }

    const handleSendFriendRequest = async () => {
        if (checkFriendStatus() === "Request Sent") return;

        if (checkFriendStatus() === "Connect") {
            await axios.post(`${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/sendFriendReq`, { receiver: userData?._id }, { withCredentials: true }).then(res => {
                toast.success(res.data.message)
                setTimeout(() => {
                    window.location.reload();
                }, 2000)

            }).catch(err => {
                console.log(err)
                toast.error(err?.res?.data?.error)
            })
        }
        else if (checkFriendStatus() == "Approve Request") {
            await axios.post(`${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/acceptFriendRequest`, { friendId: userData?._id }, { withCredentials: true }).then(res => {
                toast.success(res.data.message)
                setTimeout(() => {
                    window.location.reload();
                }, 2000)
            }).catch(err => {
                console.log(err)
                toast.error(err?.res?.data?.error)
            })
        }
        else {
            await axios.delete(`${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/removeFromFriendList/${userData?._id}`, { withCredentials: true }).then(res => {
                toast.success(res.data.message)
                setTimeout(() => {
                    window.location.reload();
                }, 2000)
            }).catch(err => {
                console.log(err)
                toast.error(err?.res?.data?.error)
            })
        }
    }

    const handleLogout = async () => {
        await axios.post(`${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/logout`, {}, { withCredentials: true }).then((res => {
            localStorage.clear();
            window.location.reload();
        })).catch(err => {
            console.log(err)
            toast.error(err?.res?.data?.error)
        })
    }


    const copyToClipBoard = async () => {
        try {
            let string = `${import.meta.env.VITE_APP_BACKEND_URL}/profile/${id}`
            await navigator.clipboard.writeText(string);
            toast.success('copied to Clipboard')
        } catch (err) {
            console.error('Failed to Copy!', err);
        }
    }

    // Animation Variants
    const containerVariants = {
        hidden: { opacity: 0 },
        visible: {
            opacity: 1,
            transition: {
                staggerChildren: 0.1,
                delayChildren: 0.1
            }
        }
    };

    const cardVariants = {
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
                damping: 15
            }
        }
    };

    const coverImageVariants = {
        hidden: { opacity: 0, scale: 1.1 },
        visible: {
            opacity: 1,
            scale: 1,
            transition: {
                duration: 0.6,
                ease: "easeOut"
            }
        }
    };

    const profileImageVariants = {
        hidden: { 
            opacity: 0, 
            scale: 0.5,
            y: 20
        },
        visible: {
            opacity: 1,
            scale: 1,
            y: 0,
            transition: {
                type: "spring",
                stiffness: 150,
                damping: 15,
                delay: 0.3
            }
        }
    };

    const buttonVariants = {
        hover: { 
            scale: 1.05,
            transition: { duration: 0.2 }
        },
        tap: { scale: 0.95 }
    };

    const skillVariants = {
        hidden: { opacity: 0, scale: 0.8 },
        visible: (i) => ({
            opacity: 1,
            scale: 1,
            transition: {
                delay: i * 0.05,
                type: "spring",
                stiffness: 100,
                damping: 15
            }
        })
    };

    const postVariants = {
        hidden: { opacity: 0, x: -20 },
        visible: (i) => ({
            opacity: 1,
            x: 0,
            transition: {
                delay: i * 0.1,
                type: "spring",
                stiffness: 100,
                damping: 15
            }
        })
    };

    const sidebarVariants = {
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
                delay: 0.4
            }
        }
    };


    return (
        <motion.div 
            className="px-5 xl:px-[200px] py-5 flex flex-col gap-5 w-full mt-5 pt-12 bg-purple-200"
            initial="hidden"
            animate="visible"
            variants={containerVariants}
        >
            <div className="flex justify-between">

                {/* Left Side Main Section */}
                <motion.div 
                    className="md:w-[70%] w-full"
                    variants={containerVariants}
                >
                    {/* Profile Header Card */}
                    <motion.div variants={cardVariants}>
                        <Card padding={0}>
                            <div className="w-full h-fit">
                                <div className="relative w-full h-[200px]">
                                    {userData?._id === ownData?._id && (
                                        <motion.div 
                                            className="absolute cursor-pointer top-3 right-3 z-20 w-[35px] flex justify-center items-center h-[35px] rounded-full bg-white" 
                                            onClick={handleoneditecover}
                                            whileHover={{ scale: 1.1, rotate: 15 }}
                                            whileTap={{ scale: 0.9 }}
                                        >
                                            <EditIcon />
                                        </motion.div>
                                    )}
                                    <motion.img 
                                        className="w-full h-[200px] rounded-tr-lg rounded-tl-lg" 
                                        src={userData?.cover_pic} 
                                        alt=""
                                        variants={coverImageVariants}
                                    />
                                    <motion.div 
                                        onClick={handleCircularimageopen} 
                                        className="absolute object-cover top-26 left-9 z-10"
                                        variants={profileImageVariants}
                                    >
                                        <motion.img 
                                            className="rounded-full h-[140px] border-2 cursor-pointer border-white w-[140px]" 
                                            src={userData?.profile_pic} 
                                            alt=""
                                            whileHover={{ scale: 1.1, rotate: 5 }}
                                            transition={{ type: "spring", stiffness: 300, damping: 20 }}
                                        />
                                    </motion.div>
                                </div>

                                <motion.div 
                                    className="mt-10 relative px-8 py-2"
                                    initial={{ opacity: 0, y: 20 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    transition={{ delay: 0.5, duration: 0.5 }}
                                >
                                    <div className="w-full">
                                        <div className="flex justify-between">
                                            <div className="text-gray-700 cursor-pointer">{userData?.f_name}</div>
                                            {userData?._id === ownData?._id && (
                                                <motion.div 
                                                    className="cursor-pointer" 
                                                    onClick={handleinfomodel}
                                                    whileHover={{ scale: 1.1, rotate: 15 }}
                                                    whileTap={{ scale: 0.9 }}
                                                >
                                                    <EditIcon />
                                                </motion.div>
                                            )}
                                        </div>

                                        <div className="text-gray-700">{userData?.headline}</div>
                                        <div className="text-sm text-gray-500">{userData?.curr_location}</div>
                                        <div className="text-md text-blue-800 w-fit cursor-pointer hover:underline">{userData?.friends?.length ?? 0} Connection</div>

                                        <div className="md:flex w-full justify-between">
                                            <div className="my-5 flex gap-5">
                                                <motion.div 
                                                    className="cursor-pointer p-2 border-1 font-semibold text-white bg-purple-900 rounded-lg"
                                                    variants={buttonVariants}
                                                    whileHover="hover"
                                                    whileTap="tap"
                                                >
                                                    Open to
                                                </motion.div>
                                                <motion.div 
                                                    className="cursor-pointer p-2 border-1 font-semibold text-white bg-purple-900 rounded-lg"
                                                    onClick={copyToClipBoard}
                                                    variants={buttonVariants}
                                                    whileHover="hover"
                                                    whileTap="tap"
                                                >
                                                    Share
                                                </motion.div>
                                                {userData?._id === ownData?._id && (
                                                    <motion.div 
                                                        onClick={handleLogout} 
                                                        className="cursor-pointer p-2 border-1 font-semibold text-white bg-purple-900 rounded-lg"
                                                        variants={buttonVariants}
                                                        whileHover="hover"
                                                        whileTap="tap"
                                                    >
                                                        LogOut
                                                    </motion.div>
                                                )}
                                            </div>

                                            <div className="my-5 flex gap-5">
                                                {amIFriend() && (
                                                    <motion.div 
                                                        className="cursor-pointer p-2 border-1 font-semibold text-white bg-purple-900 rounded-lg" 
                                                        onClick={messagemodel}
                                                        variants={buttonVariants}
                                                        whileHover="hover"
                                                        whileTap="tap"
                                                    >
                                                        Message
                                                    </motion.div>
                                                )}
                                                {userData?._id === ownData?._id ? null : (
                                                    <motion.div 
                                                        onClick={handleSendFriendRequest} 
                                                        className="cursor-pointer p-2 border-1 font-semibold text-white bg-purple-900 rounded-lg"
                                                        variants={buttonVariants}
                                                        whileHover="hover"
                                                        whileTap="tap"
                                                    >
                                                        {checkFriendStatus()}
                                                    </motion.div>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </motion.div>
                            </div>
                        </Card>
                    </motion.div>

                    {/* About Section */}
                    <motion.div className="mt-5" variants={cardVariants}>
                        <Card padding={1}>
                            <div className="flex justify-between">
                                <div className="text-xl">About</div>
                                {userData?._id === ownData?._id && (
                                    <motion.div 
                                        className="cursor-pointer" 
                                        onClick={handleAboutModal}
                                        whileHover={{ scale: 1.1, rotate: 15 }}
                                        whileTap={{ scale: 0.9 }}
                                    >
                                        <EditIcon />
                                    </motion.div>
                                )}
                            </div>
                            <div className="text-gray-700 text-md w-[80%]">{userData?.about}</div>
                        </Card>
                    </motion.div>

                    {/* Skills Section */}
                    <motion.div className="mt-5" variants={cardVariants}>
                        <Card padding={1}>
                            <div className="flex justify-between">
                                <div className="text-xl">Skills</div>
                            </div>

                            <div className="text-gray-700 text-md my-2 w-full flex gap-4 flex-wrap">
                                {userData?.skills?.map((item, index) => (
                                    <motion.div 
                                        key={index} 
                                        className="py-1 px-3 cursor-pointer bg-purple-900 text-white rounded-lg"
                                        custom={index}
                                        variants={skillVariants}
                                        whileHover={{ scale: 1.1, y: -5 }}
                                        whileTap={{ scale: 0.95 }}
                                    >
                                        {item}
                                    </motion.div>
                                ))}
                            </div>
                        </Card>
                    </motion.div>

                    {/* Activity Section */}
                    <motion.div className="mt-5" variants={cardVariants}>
                        <Card padding={1}>
                            <div className="flex justify-between items-center">
                                <div className="text-xl">Activity</div>
                            </div>
                            <motion.div 
                                className="border-1 bg-green-900 rounded-4xl font-semibold mt-2 cursor-pointer py-1 px-3 text-white items-center w-fit"
                                whileHover={{ scale: 1.05 }}
                                whileTap={{ scale: 0.95 }}
                            >
                                Post
                            </motion.div>
                            <div className="w-full flex gap-1 overflow-x-auto my-2 overflow-y-hidden">
                                {postData.map((item, index) => (
                                    <motion.div 
                                        key={item._id} 
                                        className="cursor-pointer shrink-0 w-[350px] h-[560px]"
                                        custom={index}
                                        variants={postVariants}
                                        whileHover={{ scale: 1.02, y: -5 }}
                                    >
                                        <Post profile={1} item={item} personalData={ownData} />
                                    </motion.div>
                                ))}
                            </div>

                            <div className="w-full flex justify-center items-center">
                                <Link to={`/profile/${id}/activities`}>
                                    <motion.div
                                        className="p-2 rounded-xl cursor-pointer hover:bg-gray-300"
                                        whileHover={{ scale: 1.05, x: 5 }}
                                        whileTap={{ scale: 0.95 }}
                                    >
                                        Show All Posts
                                        <ArrowRightAltIcon />
                                    </motion.div>
                                </Link>
                            </div>
                        </Card>
                    </motion.div>

                    {/* Experience Section */}
                    <motion.div className="mt-5" variants={cardVariants}>
                        <Card padding={1}>
                            <div className="flex justify-between items-center">
                                <div className="text-xl">Experiance</div>
                                {userData?._id === ownData?._id && (
                                    <motion.div 
                                        className="cursor-pointer" 
                                        onClick={exphandleModal}
                                        whileHover={{ scale: 1.1, rotate: 90 }}
                                        whileTap={{ scale: 0.9 }}
                                    >
                                        <AddIcon />
                                    </motion.div>
                                )}
                            </div>

                            <div className="mt-5">
                                {userData?.experiance?.map((item, index) => (
                                    <motion.div 
                                        key={index} 
                                        className="flex justify-between"
                                        initial={{ opacity: 0, x: -20 }}
                                        animate={{ opacity: 1, x: 0 }}
                                        transition={{ delay: index * 0.1 }}
                                    >
                                        <div className="p-2 border-t-1 border-gray-300">
                                            <div className="text-lg">{item.designation}</div>
                                            <div className="text-sm">{item.company_name}</div>
                                            <div className="text-sm text-gray-500">{item.duration}</div>
                                            <div className="text-sm text-gray-500">{item.location}</div>
                                        </div>
                                        {userData?._id === ownData?._id && (
                                            <motion.div 
                                                onClick={() => { updateExpEdit(item._id, item) }} 
                                                className="cursor-pointer"
                                                whileHover={{ scale: 1.1, rotate: 15 }}
                                                whileTap={{ scale: 0.9 }}
                                            >
                                                <EditIcon />
                                            </motion.div>
                                        )}
                                    </motion.div>
                                ))}
                            </div>
                        </Card>
                    </motion.div>
                </motion.div>

                {/* Right side advertisement */}
                <motion.div 
                    className="hidden md:flex md:w-[28%]"
                    variants={sidebarVariants}
                >
                    <div className="sticky top-[76px]">
                        <Advertisment />
                    </div>
                </motion.div>

                {/* Modals with AnimatePresence */}
                <AnimatePresence>
                    {ImageModal1 && (
                        <motion.div
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            exit={{ opacity: 0 }}
                        >
                            <Model title='Upload Image' closeModel={handleimagemodalopenclose}>
                                <ImageModal handleEditFunc={handleEditFunc} selfData={ownData} isCircular={circularImage} />
                            </Model>
                        </motion.div>
                    )}

                    {infoModal && (
                        <motion.div
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            exit={{ opacity: 0 }}
                        >
                            <Model title="Edit Info" closeModel={handleinfomodel}>
                                <EditInfoModel handleEditFunc={handleEditFunc} selfData={ownData} />
                            </Model>
                        </motion.div>
                    )}

                    {aboutModal && (
                        <motion.div
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            exit={{ opacity: 0 }}
                        >
                            <Model title="Edit About" closeModel={handleAboutModal}>
                                <AboutModal handleEditFunc={handleEditFunc} selfData={ownData} />
                            </Model>
                        </motion.div>
                    )}

                    {expModal && (
                        <motion.div
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            exit={{ opacity: 0 }}
                        >
                            <Model title="Experiance" closeModel={exphandleModal}>
                                <ExpModal handleEditFunc={handleEditFunc} selfData={ownData} updateExp={updateExp} setUpdateExp={updateExpEdit} />
                            </Model>
                        </motion.div>
                    )}

                    {messageModel && (
                        <motion.div
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            exit={{ opacity: 0 }}
                        >
                            <Model title="Messsage" closeModel={messagemodel}>
                                <MessageModal selfData={ownData} userData={userData} />
                            </Model>
                        </motion.div>
                    )}
                </AnimatePresence>
            </div>
            <ToastContainer />
        </motion.div>
    )
}