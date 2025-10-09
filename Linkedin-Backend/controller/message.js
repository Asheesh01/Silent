const messageModal=require("../model/message");


exports.sendMessage=async(req,res)=>{


try{
    let{conversation,message,picture}=req.body;

    let addMessage=new messageModal({sender:req.user._id,conversation,message,picture});
    await addMessage.save();
    let populateMessage=await addMessage.populate("sender");
  return res.status(200).json(populateMessage);
}
 catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });
    }
}  
    exports.getMessage=async(req,res)=>{
        try{  
                let{convId}=req.params;
                let message=await messageModal.find({
                    conversation:convId
                }).populate("sender")
                return res.status(200).json({message:"Fetched Message Successfully",message})
        }
         catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });
    }
    }

    exports.deleteMessage = async (req, res) => {
  try {
    const { messageId } = req.params;

    // Use the correct model name
    const message = await messageModal.findByIdAndDelete(messageId);

    if (!message) {
      return res.status(404).json({ error: "Message not found" });
    }

    res.status(200).json({ success: true, message: "Message deleted successfully" });
  } catch (err) {
    console.error('Delete failed:', err);
    res.status(500).json({ error: "Server error", message: err.message });
  }
};
