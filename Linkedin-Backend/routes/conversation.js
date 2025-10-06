const express=require("express");
const router=express.Router();
const authenticatins=require('../authenticatins/auth');
const conversationController=require("../controller/conversation");


router.post("/add-conversation",authenticatins.auth,conversationController.addConversation);

router.get('/get-conversation',authenticatins.auth,conversationController.getConversation);



module.exports=router;