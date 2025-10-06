import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import Advertisment from "../../components/Advertisment/Advertisment";

export default function Resume() {
  const [userData, setUserData] = useState(null);
  
  useEffect(() => {
    let userData = localStorage.getItem('userInfo');
    setUserData(userData ? JSON.parse(userData) : null);
  }, []);

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.2,
        delayChildren: 0.1
      }
    }
  };

  const resumeVariants = {
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

  const adVariants = {
    hidden: { 
      opacity: 0, 
      x: 50,
      y: 20
    },
    visible: {
      opacity: 1,
      x: 0,
      y: 0,
      transition: {
        type: "spring",
        stiffness: 100,
        damping: 15,
        duration: 0.6
      }
    }
  };

  return (
    <motion.div 
      className="w-full px-5 xl:px-[200px] py-9 bg-purple-200 gap-5 flex mt-5"
      variants={containerVariants}
      initial="hidden"
      animate="visible"
    >
      <motion.div 
        className="w-[100%] py-5 sm:w-[74%]"
        variants={resumeVariants}
      >
        <motion.img 
          src={userData?.resume} 
          className="rounded-lg w-full h-full" 
          alt="Resume"
          whileHover={{ 
            scale: 1.02,
            boxShadow: "0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)"
          }}
          transition={{ type: "spring", stiffness: 300, damping: 20 }}
        />
      </motion.div>
      
      <motion.div 
        className="w-[26%] py-5 hidden md:block"
        variants={adVariants}
      >
        <motion.div 
          className="sticky top-19"
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.4, duration: 0.5 }}
        >
          <Advertisment />
        </motion.div>
      </motion.div>
    </motion.div>
  );
}