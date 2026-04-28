import React from "react";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";

export default function Navbar() {
    return (
        <motion.nav
            className="w-full fixed top-0 left-0 z-50 bg-white/90 backdrop-blur-md shadow-sm md:px-[100px] px-[20px] flex justify-between py-3 box-border items-center border-b border-gray-100"
            initial={{ y: -80, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ type: "spring", stiffness: 120, damping: 20, duration: 0.6 }}
        >
            {/* Logo */}
            <Link to="/" className="flex gap-1 items-center cursor-pointer">
                <motion.div
                    className="flex items-center gap-1"
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                    transition={{ type: "spring", stiffness: 300 }}
                >
                    <div className="bg-blue-700 text-white font-bold text-xl w-9 h-9 flex items-center justify-center rounded-md">
                        in
                    </div>
                    <span className="text-gray-800 font-bold text-xl hidden sm:block">Linked<span className="text-blue-700">In</span></span>
                </motion.div>
            </Link>

            {/* Nav Actions */}
            <motion.div
                className="flex box-border gap-3 justify-center items-center"
                initial={{ opacity: 0, x: 30 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.2, duration: 0.5 }}
            >
                <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
                    <Link
                        to="/signUp"
                        className="md:px-5 md:py-2 px-3 py-1.5 box-border text-gray-700 font-medium rounded-full text-sm sm:text-base hover:bg-gray-100 cursor-pointer transition-colors"
                    >
                        Join now
                    </Link>
                </motion.div>
                <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
                    <Link
                        to="/Login"
                        className="md:px-5 md:py-2 px-3 py-1.5 box-border border-2 border-blue-700 text-blue-700 font-semibold rounded-full text-sm sm:text-base hover:bg-blue-50 cursor-pointer transition-colors"
                    >
                        Sign in
                    </Link>
                </motion.div>
            </motion.div>
        </motion.nav>
    );
}