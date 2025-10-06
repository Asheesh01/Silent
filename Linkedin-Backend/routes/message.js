const express=require("express");
const router=express.Router();
const authenticatins=require("../authenticatins/auth");
const messagecontroller=require("../controller/message");

router.post("/",authenticatins.auth,messagecontroller.sendMessage);
router.get('/:convId',authenticatins.auth,messagecontroller.getMessage);


module.exports=router;