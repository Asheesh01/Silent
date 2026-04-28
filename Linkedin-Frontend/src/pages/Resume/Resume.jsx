import React, { useEffect, useState, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";
import Advertisment from "../../components/Advertisment/Advertisment";
import Card from "../../components/card/card";
import axios from "axios";
import { toast, ToastContainer } from "react-toastify";
import UploadFileIcon from "@mui/icons-material/UploadFile";
import DescriptionIcon from "@mui/icons-material/Description";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";

const CLOUDINARY_UPLOAD_URL = "https://api.cloudinary.com/v1_1/dmlz1qzzr/image/upload";
const UPLOAD_PRESET = "my_unsigned_preset";

export default function Resume() {
  const [userData, setUserData] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [dragOver, setDragOver] = useState(false);
  const fileInputRef = useRef(null);

  useEffect(() => {
    let stored = localStorage.getItem("userInfo");
    setUserData(stored ? JSON.parse(stored) : null);
  }, []);

  const handleFileUpload = async (file) => {
    if (!file) return;

    const allowedTypes = ["image/jpeg", "image/png", "image/jpg", "application/pdf"];
    const isImage = file.type.startsWith("image/");
    const isPDF = file.type === "application/pdf";

    if (!allowedTypes.includes(file.type)) {
      return toast.error("Only JPG, PNG, or PDF files are allowed.");
    }

    if (file.size > 10 * 1024 * 1024) {
      return toast.error("File must be under 10MB.");
    }

    setUploading(true);
    setUploadProgress(0);

    try {
      const formData = new FormData();
      formData.append("file", file);
      formData.append("upload_preset", UPLOAD_PRESET);
      // For PDFs use raw resource type
      if (isPDF) {
        formData.append("resource_type", "raw");
      }

      // Simulate progress
      const progressInterval = setInterval(() => {
        setUploadProgress((prev) => (prev < 85 ? prev + 10 : prev));
      }, 200);

      const uploadURL = isPDF
        ? "https://api.cloudinary.com/v1_1/dmlz1qzzr/raw/upload"
        : CLOUDINARY_UPLOAD_URL;

      const res = await fetch(uploadURL, { method: "POST", body: formData });
      const result = await res.json();
      clearInterval(progressInterval);
      setUploadProgress(100);

      if (!result.secure_url) throw new Error("Upload failed");

      // Save URL to backend
      await axios.put(
        `${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/update`,
        { user: { resume: result.secure_url } },
        { withCredentials: true }
      );

      const updatedUser = { ...userData, resume: result.secure_url };
      setUserData(updatedUser);
      localStorage.setItem("userInfo", JSON.stringify(updatedUser));
      toast.success("Resume uploaded successfully!");
    } catch (err) {
      console.error(err);
      toast.error("Upload failed. Please try again.");
    } finally {
      setUploading(false);
      setUploadProgress(0);
    }
  };

  const handleFileInputChange = (e) => {
    const file = e.target.files[0];
    if (file) handleFileUpload(file);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragOver(false);
    const file = e.dataTransfer.files[0];
    if (file) handleFileUpload(file);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    setDragOver(true);
  };

  const handleDragLeave = () => setDragOver(false);

  const handleRemoveResume = async () => {
    try {
      await axios.put(
        `${import.meta.env.VITE_APP_BACKEND_URL}/api/auth/update`,
        { user: { resume: "" } },
        { withCredentials: true }
      );
      const updatedUser = { ...userData, resume: "" };
      setUserData(updatedUser);
      localStorage.setItem("userInfo", JSON.stringify(updatedUser));
      toast.success("Resume removed.");
    } catch (err) {
      toast.error("Failed to remove resume.");
    }
  };

  const isImageResume = userData?.resume && !userData.resume.includes("/raw/");
  const isPDFResume = userData?.resume && userData.resume.includes("/raw/");

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: { staggerChildren: 0.15, delayChildren: 0.1 }
    }
  };

  const leftVariants = {
    hidden: { opacity: 0, x: -50, scale: 0.95 },
    visible: {
      opacity: 1, x: 0, scale: 1,
      transition: { type: "spring", stiffness: 100, damping: 15 }
    }
  };

  const rightVariants = {
    hidden: { opacity: 0, x: 50, y: 20 },
    visible: {
      opacity: 1, x: 0, y: 0,
      transition: { type: "spring", stiffness: 100, damping: 15 }
    }
  };

  return (
    <motion.div
      className="w-full px-5 xl:px-[200px] py-9 bg-purple-200 gap-5 flex mt-5 min-h-screen"
      variants={containerVariants}
      initial="hidden"
      animate="visible"
    >
      {/* Left: Resume Display / Upload */}
      <motion.div className="w-[100%] py-5 sm:w-[74%]" variants={leftVariants}>
        <Card padding={1}>
          {/* Header */}
          <div className="flex justify-between items-center mb-4">
            <div className="flex items-center gap-2">
              <DescriptionIcon sx={{ color: "#6b21a8", fontSize: 28 }} />
              <span className="text-xl font-semibold text-gray-800">Resume</span>
            </div>
            {userData?.resume && (
              <div className="flex gap-2">
                <motion.button
                  onClick={() => fileInputRef.current?.click()}
                  className="text-sm text-blue-700 border border-blue-700 px-3 py-1 rounded-full hover:bg-blue-50 cursor-pointer transition-colors"
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  Replace
                </motion.button>
                <motion.button
                  onClick={handleRemoveResume}
                  className="text-sm text-red-600 border border-red-400 px-3 py-1 rounded-full hover:bg-red-50 cursor-pointer transition-colors flex items-center gap-1"
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  <DeleteOutlineIcon sx={{ fontSize: 16 }} /> Remove
                </motion.button>
              </div>
            )}
          </div>

          <AnimatePresence mode="wait">
            {userData?.resume ? (
              /* Resume Preview */
              <motion.div
                key="resume-preview"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -20 }}
                transition={{ duration: 0.4 }}
              >
                <div className="flex items-center gap-2 mb-3 text-green-700">
                  <CheckCircleOutlineIcon sx={{ fontSize: 20 }} />
                  <span className="text-sm font-medium">Resume uploaded successfully</span>
                </div>

                {isPDFResume ? (
                  <div className="bg-gray-50 rounded-xl border-2 border-dashed border-gray-200 p-8 flex flex-col items-center justify-center gap-3">
                    <DescriptionIcon sx={{ fontSize: 64, color: "#dc2626" }} />
                    <p className="text-gray-600 font-medium">PDF Resume</p>
                    <a
                      href={userData.resume}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-blue-700 text-sm hover:underline"
                    >
                      View / Download PDF
                    </a>
                  </div>
                ) : (
                  <motion.img
                    src={userData.resume}
                    className="rounded-xl w-full border border-gray-200 shadow-md"
                    alt="Resume"
                    whileHover={{
                      scale: 1.01,
                      boxShadow: "0 20px 25px -5px rgba(0,0,0,0.1)"
                    }}
                    transition={{ type: "spring", stiffness: 300, damping: 20 }}
                  />
                )}
              </motion.div>
            ) : (
              /* Upload Zone */
              <motion.div
                key="upload-zone"
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.95 }}
                transition={{ duration: 0.4 }}
              >
                <motion.div
                  className={`border-2 border-dashed rounded-2xl p-12 flex flex-col items-center justify-center gap-4 cursor-pointer transition-colors ${
                    dragOver
                      ? "border-purple-500 bg-purple-50"
                      : "border-gray-300 hover:border-purple-400 hover:bg-purple-50/50"
                  }`}
                  onDrop={handleDrop}
                  onDragOver={handleDragOver}
                  onDragLeave={handleDragLeave}
                  onClick={() => !uploading && fileInputRef.current?.click()}
                  whileHover={{ scale: 1.01 }}
                  whileTap={{ scale: 0.99 }}
                >
                  <AnimatePresence mode="wait">
                    {uploading ? (
                      <motion.div
                        key="uploading"
                        className="flex flex-col items-center gap-4 w-full max-w-xs"
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                      >
                        <motion.div
                          className="w-16 h-16 border-4 border-purple-200 border-t-purple-700 rounded-full"
                          animate={{ rotate: 360 }}
                          transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                        />
                        <p className="text-gray-600 font-medium">Uploading...</p>
                        <div className="w-full bg-gray-200 rounded-full h-2">
                          <motion.div
                            className="bg-purple-700 h-2 rounded-full"
                            initial={{ width: 0 }}
                            animate={{ width: `${uploadProgress}%` }}
                            transition={{ duration: 0.3 }}
                          />
                        </div>
                        <p className="text-sm text-gray-500">{uploadProgress}%</p>
                      </motion.div>
                    ) : (
                      <motion.div
                        key="upload-prompt"
                        className="flex flex-col items-center gap-3"
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                      >
                        <motion.div
                          animate={{ y: [0, -8, 0] }}
                          transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
                        >
                          <UploadFileIcon sx={{ fontSize: 64, color: "#7c3aed" }} />
                        </motion.div>
                        <p className="text-lg font-semibold text-gray-700">
                          {dragOver ? "Drop your file here!" : "Upload Resume"}
                        </p>
                        <p className="text-sm text-gray-500 text-center">
                          Drag & drop or click to select
                        </p>
                        <p className="text-xs text-gray-400">
                          Supported: JPG, PNG, PDF · Max 10MB
                        </p>
                        <motion.div
                          className="mt-2 px-6 py-2.5 bg-purple-700 text-white rounded-full text-sm font-semibold"
                          whileHover={{ scale: 1.05, boxShadow: "0 8px 20px rgba(109, 40, 217, 0.4)" }}
                          whileTap={{ scale: 0.95 }}
                        >
                          Choose File
                        </motion.div>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </motion.div>

                <p className="text-xs text-gray-400 mt-3 text-center">
                  Upload your resume to let recruiters and connections view it on your profile.
                </p>
              </motion.div>
            )}
          </AnimatePresence>

          {/* Hidden file input */}
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/jpg,application/pdf"
            onChange={handleFileInputChange}
            className="hidden"
          />
        </Card>
      </motion.div>

      {/* Right: Advertisement */}
      <motion.div className="w-[26%] py-5 hidden md:block" variants={rightVariants}>
        <motion.div
          className="sticky top-20"
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.4, duration: 0.5 }}
        >
          <Advertisment />
        </motion.div>
      </motion.div>

      <ToastContainer position="top-center" autoClose={3000} />
    </motion.div>
  );
}