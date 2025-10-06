import React, { useEffect, useState, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import Card from '../../components/card/card';
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown';
import Conversation from '../../components/Conersatiosn/Converstion';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import ImageIcon from '@mui/icons-material/Image';
import Advertisment from '../../components/Advertisment/Advertisment';
import axios from 'axios';
import { toast, ToastContainer } from 'react-toastify';
import socket from '../../../socket';

export default function Message() {
  const [conversations, setConversation] = useState([]);
  const [ownData, setOwnData] = useState(null);
  const [activeConID, setActiveConId] = useState(null);
  const [selectedConvDetails, setselectedConvDetails] = useState(null);
  const [mesages, setMeesages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [imageLink, setImageLink] = useState(null);
  const [messagetext, setMessagetext] = useState('');

  const ref = useRef();

  useEffect(() => {
    ref?.current?.scrollIntoView({ behavior: 'smooth' });
  }, [mesages]);

  const handleSelectedConv = (id, userData) => {
    setActiveConId(id);
    socket.emit('joinConversation', id);
    setselectedConvDetails(userData);
  };

  useEffect(() => {
    if (activeConID) {
      fetchMessages();
    }
  }, [activeConID]);

  const fetchMessages = async () => {
    try {
      const res = await axios.get(
        `http://localhost:5000/api/message/${activeConID}`,
        { withCredentials: true }
      );
      setMeesages(res.data.message);
    } catch (err) {
      console.log(err);
      toast.error(err?.response?.data?.error);
    }
  };

  useEffect(() => {
    const userData = localStorage.getItem('userInfo');
    setOwnData(userData ? JSON.parse(userData) : null);
    fetchConversationonLoad();
  }, []);

  const fetchConversationonLoad = async () => {
    try {
      const res = await axios.get(
        'http://localhost:5000/api/conversation/get-conversation',
        { withCredentials: true }
      );
      setConversation(res.data.conversastion);
      setActiveConId(res.data.conversastion[0]?._id);
      socket.emit('joinConversation', res.data.conversastion[0]?._id);

      const ownId = ownData?._id;
      const arr = res.data.conversastion[0]?.members?.filter(
        (it) => it._id !== ownId
      );
      setselectedConvDetails(arr[0]);
    } catch (err) {
      console.log(err);
      alert('Something Went Wrong');
    }
  };

  const handleInputImage = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', 'my_unsigned_preset');

    setLoading(true);
    try {
      const res = await fetch(
        'https://api.cloudinary.com/v1_1/dmlz1qzzr/image/upload',
        { method: 'POST', body: formData }
      );

      const result = await res.json();
      console.log('Upload result:', result);
      setImageLink(result.secure_url);
    } catch (err) {
      console.error('Upload failed:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    socket.on('recieveMessage', (response) => {
      setMeesages((prev) => [...prev, response]);
    });
  }, []);

  const haandleSendMessage = async () => {
    if (!messagetext.trim() && !imageLink) return;

    try {
      const res = await axios.post(
        `http://localhost:5000/api/message`,
        {
          conversation: activeConID,
          message: messagetext,
          picture: imageLink,
        },
        { withCredentials: true }
      );

      socket.emit('sendMessage', activeConID, res.data);
      setMessagetext('');
      setImageLink(null);
    } catch (err) {
      console.log(err);
      alert('Something Went Wrong');
    }
  };

  // Animation Variants
  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.12,
        delayChildren: 0.1
      }
    }
  };

  const slideFromLeft = {
    hidden: { x: -100, opacity: 0 },
    visible: {
      x: 0,
      opacity: 1,
      transition: { type: 'spring', stiffness: 100, damping: 15 }
    }
  };

  const slideFromRight = {
    hidden: { x: 100, opacity: 0 },
    visible: {
      x: 0,
      opacity: 1,
      transition: { type: 'spring', stiffness: 100, damping: 15 }
    }
  };

  const messageVariants = {
    hidden: { scale: 0.8, opacity: 0, y: 20 },
    visible: {
      scale: 1,
      opacity: 1,
      y: 0,
      transition: { type: 'spring', stiffness: 200, damping: 20 }
    }
  };

  const imagePreviewVariants = {
    hidden: { scale: 0, rotate: -180 },
    visible: {
      scale: 1,
      rotate: 0,
      transition: { type: 'spring', stiffness: 200, damping: 15 }
    },
    exit: {
      scale: 0,
      rotate: 180,
      transition: { duration: 0.3 }
    }
  };

  const pulseVariants = {
    initial: { scale: 1 },
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
      className="px-5 py-9 xl:px-[200px] bg-purple-200 flex gap-5 w-full mt-5"
      initial="hidden"
      animate="visible"
      variants={containerVariants}
    >
      <div className="flex justify-between w-full pt-5">
        {/* Left side */}
        <motion.div 
          className="w-full md:w-[70%]"
          variants={slideFromLeft}
        >
          <Card padding={0}>
            <motion.div 
              className="border-b-2 border-gray-300 px-5 py-2 font-semibold text-lg"
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2, duration: 0.5 }}
            >
              Messaging
            </motion.div>

            <motion.div 
              className="border-b-1 border-gray-300 px-5 py-2"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.3 }}
            >
              <motion.div 
                className="py-1 px-3 cursor-pointer hover:bg-green-900 bg-green-800 font-semibold flex gap-2 w-fit rounded-2xl text-white"
                whileHover={{ scale: 1.05, x: 5 }}
                whileTap={{ scale: 0.95 }}
                transition={{ type: 'spring', stiffness: 300 }}
              >
                Focused <ArrowDropDownIcon />
              </motion.div>
            </motion.div>

            <div className="w-full md:flex">
              {/* Conversations list */}
              <motion.div 
                className="h-[590px] w-full md:w-[40%] overflow-auto border-r-1 border-gray-400"
                initial={{ opacity: 0, x: -50 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.4, duration: 0.5 }}
              >
                <AnimatePresence>
                  {conversations?.map((item, index) => (
                    <motion.div
                      key={item._id || index}
                      initial={{ opacity: 0, x: -30 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: index * 0.05, type: 'spring' }}
                      whileHover={{ 
                        backgroundColor: 'rgba(0,0,0,0.03)', 
                        x: 8,
                        transition: { duration: 0.2 }
                      }}
                    >
                      <Conversation
                        activeConID={activeConID}
                        handleSelectedConv={handleSelectedConv}
                        item={item}
                        ownData={ownData}
                      />
                    </motion.div>
                  ))}
                </AnimatePresence>
              </motion.div>

              {/* Chat section */}
              <motion.div 
                className="w-full md:[60%] border-gray-400"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.5 }}
              >
                {/* Header */}
                <motion.div 
                  className="sticky border-gray-300 py-2 px-4 border-b-1 flex justify-between items-center"
                  initial={{ y: -30, opacity: 0 }}
                  animate={{ y: 0, opacity: 1 }}
                  transition={{ delay: 0.6, type: 'spring', stiffness: 120 }}
                >
                  <div>
                    <motion.p 
                      className="text-sm font-semibold"
                      initial={{ opacity: 0, x: -10 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.7 }}
                    >
                      {selectedConvDetails?.f_name}
                    </motion.p>
                    <motion.p 
                      className="text-sm text-gray-400"
                      initial={{ opacity: 0, x: -10 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.8 }}
                    >
                      {selectedConvDetails?.headline}
                    </motion.p>
                  </div>
                  <motion.div
                    whileHover={{ rotate: 90, scale: 1.2 }}
                    transition={{ duration: 0.2 }}
                    style={{ cursor: 'pointer' }}
                  >
                    <MoreHorizIcon />
                  </motion.div>
                </motion.div>

                {/* Messages */}
                <div className="h-[360px] w-full overflow-auto border-b-1 border-gray-300">
                  <motion.div 
                    className="w-full border-b-1 border-gray-300 gap-3 p-4"
                    initial={{ opacity: 0, scale: 0.9 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ delay: 0.7, duration: 0.4 }}
                  >
                    <motion.img
                      className="w-[40px] h-[40px] cursor-pointer rounded-full"
                      src={selectedConvDetails?.profile_pic}
                      alt=""
                      whileHover={{ scale: 1.15, rotate: 5 }}
                      transition={{ type: 'spring', stiffness: 300 }}
                    />
                    <div className="my-2">
                      <div className="text-sm">
                        {selectedConvDetails?.f_name}
                      </div>
                      <div className="text-sm text-gray-500">
                        {selectedConvDetails?.headline}
                      </div>
                    </div>
                  </motion.div>

                  <div className="w-full">
                    <AnimatePresence>
                      {mesages?.map((item, index) => (
                        <motion.div
                          ref={index === mesages.length - 1 ? ref : null}
                          key={item._id || index}
                          className="flex w-full cursor-pointer border-gray-300 gap-3 p-4"
                          variants={messageVariants}
                          initial="hidden"
                          animate="visible"
                          exit={{ opacity: 0, x: -30, transition: { duration: 0.3 } }}
                          whileHover={{ 
                            backgroundColor: 'rgba(0,0,0,0.02)',
                            scale: 1.01,
                            transition: { duration: 0.2 }
                          }}
                        >
                          <div className="shrink-0">
                            <motion.img
                              className="w-8 h-8 cursor-pointer rounded-full"
                              src={item?.sender?.profile_pic}
                              alt=""
                              whileHover={{ scale: 1.2, rotate: 360 }}
                              transition={{ duration: 0.5 }}
                            />
                          </div>
                          <div className="w-full mb-2">
                            <div className="text-md">{item?.sender?.f_name}</div>
                            <motion.div 
                              className="text-sm mt-6 hover:bg-gray-200 w-full p-2 rounded"
                              initial={{ opacity: 0, y: 5 }}
                              animate={{ opacity: 1, y: 0 }}
                              transition={{ delay: 0.1 }}
                            >
                              {item?.message}
                            </motion.div>
                            {item?.picture && (
                              <motion.div 
                                className="my-2"
                                initial={{ opacity: 0, scale: 0.8 }}
                                animate={{ opacity: 1, scale: 1 }}
                                transition={{ delay: 0.2 }}
                                whileHover={{ scale: 1.05 }}
                              >
                                <img
                                  className="w-[240px] h-[180px] rounded-md"
                                  src={item?.picture}
                                  alt=""
                                />
                              </motion.div>
                            )}
                          </div>
                        </motion.div>
                      ))}
                    </AnimatePresence>
                  </div>
                </div>

                {/* Typing + Image Preview */}
                <motion.div 
                  className="p-2 w-full border-b-2 border-gray-200"
                  initial={{ y: 50, opacity: 0 }}
                  animate={{ y: 0, opacity: 1 }}
                  transition={{ delay: 0.8, type: 'spring' }}
                >
                  <motion.div 
                    className="bg-gray-200 rounded-xl p-3 relative"
                    whileHover={{ boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                    transition={{ duration: 0.2 }}
                  >
                    <div className="flex flex-wrap items-start gap-2">
                      {/* Image Preview */}
                      <AnimatePresence>
                        {imageLink && (
                          <motion.div 
                            className="relative w-[90px] h-[90px]"
                            variants={imagePreviewVariants}
                            initial="hidden"
                            animate="visible"
                            exit="exit"
                          >
                            <img
                              src={imageLink}
                              alt="preview"
                              className="w-full h-full object-cover rounded-lg border border-gray-300"
                            />
                            <motion.button
                              onClick={() => setImageLink(null)}
                              className="absolute top-1 right-1 bg-black bg-opacity-60 text-white text-xs rounded-full px-1.5 py-0.5"
                              whileHover={{ scale: 1.3, rotate: 90 }}
                              whileTap={{ scale: 0.9 }}
                              transition={{ duration: 0.2 }}
                            >
                              ✕
                            </motion.button>
                          </motion.div>
                        )}
                      </AnimatePresence>

                      {/* Text Area */}
                      <motion.textarea
                        value={messagetext}
                        onChange={(e) => setMessagetext(e.target.value)}
                        rows={3}
                        className="flex-1 bg-transparent outline-none text-sm resize-none min-h-[80px]"
                        placeholder="Write a message..."
                        whileFocus={{ scale: 1.005 }}
                        transition={{ duration: 0.2 }}
                      />
                    </div>
                  </motion.div>
                </motion.div>

                {/* Send controls */}
                <motion.div 
                  className="p-1 flex justify-between items-center"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ delay: 0.9 }}
                >
                  <motion.div
                    whileHover={{ scale: 1.3, rotate: 15 }}
                    whileTap={{ scale: 0.9, rotate: -15 }}
                    transition={{ type: 'spring', stiffness: 300 }}
                  >
                    <label htmlFor="messageImage" className="cursor-pointer">
                      <ImageIcon />
                    </label>
                    <input
                      id="messageImage"
                      type="file"
                      onChange={handleInputImage}
                      className="hidden"
                    />
                  </motion.div>

                  <AnimatePresence mode="wait">
                    {!loading ? (
                      <motion.div
                        key="send-button"
                        className="bg-purple-900 px-3 py-1 rounded-2xl cursor-pointer text-white"
                        onClick={haandleSendMessage}
                        initial={{ scale: 0, rotate: -180 }}
                        animate={{ scale: 1, rotate: 0 }}
                        exit={{ scale: 0, rotate: 180 }}
                        whileHover={{ 
                          scale: 1.1, 
                          boxShadow: '0 8px 20px rgba(88, 28, 135, 0.4)',
                          y: -3
                        }}
                        whileTap={{ scale: 0.95 }}
                        transition={{ type: 'spring', stiffness: 300 }}
                      >
                        Send
                      </motion.div>
                    ) : (
                      <motion.div 
                        key="loading"
                        className="text-gray-500 text-sm"
                        variants={pulseVariants}
                        initial="initial"
                        animate="animate"
                      >
                        Uploading...
                      </motion.div>
                    )}
                  </AnimatePresence>
                </motion.div>
              </motion.div>
            </div>
          </Card>
        </motion.div>

        {/* Right side */}
        <motion.div 
          className="hidden md:flex md:w-[25%]"
          variants={slideFromRight}
        >
          <div className="sticky top-[90px]">
            <Advertisment />
          </div>
        </motion.div>
      </div>
      <ToastContainer />
    </motion.div>
  );
}