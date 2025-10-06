    const express = require("express");
    const router = express.Router();
    const authenticatins = require("../authenticatins/auth");
    const postcontroller = require("../controller/post")

    router.post('/', authenticatins.auth, postcontroller.addPost);
    router.post("/likeDislike", authenticatins.auth, postcontroller.likeDeslikePost);
    router.get('/getAllPost/:userId', postcontroller.getAllPost);
    router.get("/getPostById/:postId", postcontroller.getPostbyId);
    router.get("/getTop5Post/:userId", postcontroller.getTop5Post);
    router.get("/UserPost/:userId", postcontroller.Userposts);

    module.exports = router;