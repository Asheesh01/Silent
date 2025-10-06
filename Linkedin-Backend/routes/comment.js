const express=require("express");
const router=express.Router();
const authenticatins=require('../authenticatins/auth');
const commentcontroller=require('../controller/comment');

router.post('/',authenticatins.auth,commentcontroller.commentPost);
router.get('/:postId',commentcontroller.getCommentByPostId)


module.exports=router;