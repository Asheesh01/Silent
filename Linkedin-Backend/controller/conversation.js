const { default: mongoose } = require('mongoose');
const conversationModal = require('../model/conversastion');
const messageModal = require('../model/message');

exports.addConversation = async (req, res) => {
    try {
        const senderId = req.user._id;
        const { recieverId, message } = req.body;
         if (!senderId || !recieverId) {
            return res.status(400).json({ error: "Sender or Receiver ID missing" });
        }

       
        let isConvExist = await conversationModal.findOne({
            members: { $all: [senderId, recieverId] }
        });
        if (!isConvExist) {
            let newConversation = new conversationModal({
                members: [senderId, recieverId]
            })
            await newConversation.save();
            let addmessage = new messageModal({ sender: req.user._id, conversation: newConversation._id, message });
            await addmessage.save();
        }
        else {
            let addmessage = new messageModal({ sender: req.user._id, conversation: isConvExist._id, message });
            await addmessage.save();
        }
        return res.status(200).json({ message: "message sent" })

    }

    catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });

    }
}

exports.getConversation=async(req,res)=>{
    try{
        let loggedinId=req.user._id;
        let conversastion=await conversationModal.find({
            members:{$in:[loggedinId]}
        }).populate("members","-password").sort({createdAt:-1});
        return res.status(200).json({
            message:"Fetched Successfully",
            conversastion:conversastion
        })

    }
    catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });

    }
}