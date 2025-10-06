import React from "react";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import GoogleLoginComp from "../../components/GoogleLogin/GoogleLogin";

export default function Landing(props) {
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

    const itemVariants = {
        hidden: { opacity: 0, y: 20 },
        visible: {
            opacity: 1,
            y: 0,
            transition: {
                duration: 0.5,
                ease: "easeOut"
            }
        }
    };

    const imageVariants = {
        hidden: { opacity: 0, x: 50 },
        visible: {
            opacity: 1,
            x: 0,
            transition: {
                duration: 0.8,
                ease: "easeOut"
            }
        }
    };

    return (
        <div className="my-4 py-[50px] md:pl-[120px] px-5 md:flex justify-between mt-[92px] bg-purple-200">
            <motion.div 
                className="md:w-[40%]"
                variants={containerVariants}
                initial="hidden"
                animate="visible"
            >
                <motion.div 
                    className="text-4xl text-gray-500"
                    variants={itemVariants}
                >
                    Welcome To Your Professional Community
                </motion.div>

                <motion.div 
                    className="mx-auto my-7 w-[70%] text-black"
                    variants={itemVariants}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                >
                    <GoogleLoginComp changeLoginValue={props.changeLoginValue}/>
                </motion.div>

                <motion.div
                    variants={itemVariants}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                >
                    <Link 
                        to={"/Login"} 
                        className="flex mx-auto mt-[20px] py-2 px-2 bg-white w-[70%] items-center justify-center rounded-3xl border-2 hover:bg-gray-100 cursor-pointer"
                    >
                        Sign in with email
                    </Link>
                </motion.div>

                <motion.div 
                    className="mx-auto mb-4 text-sm w-[70%] mt-6"
                    variants={itemVariants}
                >
                    By clicking Continue to join or sign in, you agree to
                    <span className="pl-1 text-blue-800 cursor-pointer hover:underline">LinkidIn's User Agreement</span>,
                    <span className="pl-1 text-blue-800 cursor-pointer hover:underline">Privacy policy</span>, and
                    <span className="pl-1 text-blue-800 cursor-pointer hover:underline">Cookie Policy</span>
                </motion.div>

                <motion.div 
                    className="mx-auto text-center mb-4 text-lg w-[70%] mt-4"
                    variants={itemVariants}
                >
                    New to LinkedIn? <Link to={"/signUp"} className="text-blue-800 cursor-pointer hover:underline">Join Now</Link>
                </motion.div>
            </motion.div>

            <motion.div 
                className="md:w-[50%] h-120"
                variants={imageVariants}
                initial="hidden"
                animate="visible"
            >
                <img 
                    className="h-full w-full" 
                    src="https://media.licdn.com/media//AAYAAgSrAAgAAQAAAAAAAGM6w-NyPk-_SVikYiCJ6V3Z-Q.png" 
                    alt="image"
                />
            </motion.div>
        </div>
    );
}