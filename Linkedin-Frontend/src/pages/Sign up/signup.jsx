import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import GoogleLoginComp from "../../components/GoogleLogin/GoogleLogin";
import { ToastContainer, toast } from "react-toastify";
import axios from "axios";

export default function Signup(props) {
    const navigate = useNavigate();
    const [registerField, setRegisterField] = useState({ email: "", password: "", f_name: "" });
    const [loading, setLoading] = useState(false);
    const [showPassword, setShowPassword] = useState(false);

    const handleInputField = (event, key) => {
        setRegisterField({ ...registerField, [key]: event.target.value });
    };

    const handleRegister = async () => {
        if (
            registerField.email.trim().length === 0 ||
            registerField.password.trim().length === 0 ||
            registerField.f_name.trim().length === 0
        ) {
            return toast.error("Please fill all details.");
        }
        if (registerField.password.length < 6) {
            return toast.error("Password must be at least 6 characters.");
        }
        setLoading(true);
        try {
            await axios.post(
                `${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/register`,
                registerField
            );
            toast.success("Account created! Please sign in.");
            setRegisterField({ email: "", password: "", f_name: "" });
            setTimeout(() => navigate("/Login"), 1200);
        } catch (err) {
            console.log(err);
            toast.error(err?.response?.data?.error || "Registration failed. Try again.");
        } finally {
            setLoading(false);
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === "Enter") handleRegister();
    };

    const containerVariants = {
        hidden: { opacity: 0, y: 30 },
        visible: {
            opacity: 1,
            y: 0,
            transition: { duration: 0.6, ease: "easeOut" }
        }
    };

    return (
        <div className="flex flex-col min-h-screen bg-purple-200 w-full items-center justify-center px-4 py-10">
            <motion.div
                variants={containerVariants}
                initial="hidden"
                animate="visible"
                className="w-full sm:w-[90%] md:w-[55%] lg:w-[42%] xl:w-[32%]"
            >
                {/* Logo */}
                <motion.div
                    className="flex items-center gap-2 mb-4 justify-center"
                    initial={{ opacity: 0, scale: 0.8 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ delay: 0.1 }}
                >
                    <span className="text-blue-800 font-bold text-3xl">in</span>
                    <span className="text-gray-700 font-semibold text-xl">LinkedIn</span>
                </motion.div>

                {/* Tagline */}
                <motion.p
                    className="text-center text-gray-600 text-base mb-5"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: 0.2 }}
                >
                    Make the most of your professional life
                </motion.p>

                {/* Card */}
                <motion.div
                    className="bg-white rounded-2xl shadow-2xl p-8"
                    whileHover={{ boxShadow: "0 25px 50px -12px rgba(88, 28, 135, 0.2)" }}
                    transition={{ duration: 0.3 }}
                >
                    <motion.h1
                        className="text-2xl font-bold text-gray-800 mb-5"
                        initial={{ opacity: 0, x: -20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ delay: 0.25 }}
                    >
                        Create account
                    </motion.h1>

                    {/* Fields */}
                    <div className="flex flex-col gap-3">
                        {/* Full Name */}
                        <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}>
                            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">Full name</label>
                            <motion.input
                                type="text"
                                id="name"
                                value={registerField.f_name}
                                onChange={(e) => handleInputField(e, "f_name")}
                                onKeyDown={handleKeyDown}
                                placeholder="Your full name"
                                whileFocus={{ scale: 1.01 }}
                                className="w-full border-2 border-gray-200 focus:border-purple-600 outline-none rounded-xl py-2.5 px-4 text-base transition-colors"
                            />
                        </motion.div>

                        {/* Email */}
                        <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.35 }}>
                            <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                            <motion.input
                                type="text"
                                id="email"
                                value={registerField.email}
                                onChange={(e) => handleInputField(e, "email")}
                                onKeyDown={handleKeyDown}
                                placeholder="Email address"
                                whileFocus={{ scale: 1.01 }}
                                className="w-full border-2 border-gray-200 focus:border-purple-600 outline-none rounded-xl py-2.5 px-4 text-base transition-colors"
                            />
                        </motion.div>

                        {/* Password */}
                        <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.4 }}>
                            <label htmlFor="Password" className="block text-sm font-medium text-gray-700 mb-1">Password</label>
                            <div className="relative">
                                <motion.input
                                    type={showPassword ? "text" : "password"}
                                    id="Password"
                                    value={registerField.password}
                                    onChange={(e) => handleInputField(e, "password")}
                                    onKeyDown={handleKeyDown}
                                    placeholder="6+ characters"
                                    whileFocus={{ scale: 1.01 }}
                                    className="w-full border-2 border-gray-200 focus:border-purple-600 outline-none rounded-xl py-2.5 px-4 text-base transition-colors pr-12"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-blue-700 font-semibold hover:underline cursor-pointer"
                                >
                                    {showPassword ? "Hide" : "Show"}
                                </button>
                            </div>
                        </motion.div>
                    </div>

                    {/* Terms */}
                    <motion.p
                        className="text-xs text-gray-400 mt-4 leading-relaxed"
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        transition={{ delay: 0.45 }}
                    >
                        By clicking Agree & Join, you agree to the LinkedIn{" "}
                        <span className="text-blue-700 cursor-pointer hover:underline">User Agreement</span>,{" "}
                        <span className="text-blue-700 cursor-pointer hover:underline">Privacy Policy</span>, and{" "}
                        <span className="text-blue-700 cursor-pointer hover:underline">Cookie Policy</span>.
                    </motion.p>

                    {/* Register Button */}
                    <motion.button
                        onClick={handleRegister}
                        disabled={loading}
                        className="w-full mt-4 bg-blue-700 hover:bg-blue-800 disabled:bg-blue-400 text-white font-semibold rounded-full py-3 text-base cursor-pointer transition-colors"
                        whileHover={{ scale: loading ? 1 : 1.02, boxShadow: "0 8px 24px rgba(29, 78, 216, 0.35)" }}
                        whileTap={{ scale: 0.98 }}
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ delay: 0.5 }}
                    >
                        <AnimatePresence mode="wait">
                            {loading ? (
                                <motion.div
                                    key="loading"
                                    className="flex items-center justify-center gap-2"
                                    initial={{ opacity: 0 }}
                                    animate={{ opacity: 1 }}
                                    exit={{ opacity: 0 }}
                                >
                                    <motion.span
                                        className="inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"
                                        animate={{ rotate: 360 }}
                                        transition={{ duration: 0.8, repeat: Infinity, ease: "linear" }}
                                    />
                                    Creating account...
                                </motion.div>
                            ) : (
                                <motion.span key="text" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
                                    Agree & Join
                                </motion.span>
                            )}
                        </AnimatePresence>
                    </motion.button>

                    {/* Divider */}
                    <div className="flex items-center gap-3 my-4">
                        <div className="flex-1 border-t border-gray-200" />
                        <span className="text-sm text-gray-400">or</span>
                        <div className="flex-1 border-t border-gray-200" />
                    </div>

                    {/* Google Login */}
                    <GoogleLoginComp changeLoginValue={props.changeLoginValue} />
                </motion.div>

                {/* Footer */}
                <motion.div
                    className="mt-5 text-center text-sm text-gray-600"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: 0.6 }}
                >
                    Already on LinkedIn?{" "}
                    <Link to="/Login" className="text-blue-700 font-semibold hover:underline cursor-pointer">
                        Sign in
                    </Link>
                </motion.div>
            </motion.div>

            <ToastContainer position="top-center" autoClose={3000} />
        </div>
    );
}