const commentModal = require('../model/comments');
const postmodal = require('../model/post');
const NotificationModa = require('../model/Notification');
const PostModel = require('../model/post');

exports.commentPost = async (req, res) => {
    try {
        const { postId, comment } = req.body;
        const userId = req.user._id;
        const postEXist = await postmodal.findById(postId).populate("user");
        if (!postEXist) {
            return res.status(400).json({ error: 'No Such Post found' });
        }

        postEXist.comments = postEXist.comments + 1;
        await postEXist.save();
        const newcomment = new commentModal({ user: userId, post: postId, comment });
        await newcomment.save();

        const populateComent = await commentModal.findById(newcomment._id).populate('user', 'f_name headline profile_pic');
        const content = `${req.user.f_name} has commented on your post`;
        const notification = new NotificationModa({ sender: userId, receiver: postEXist.user._id, content, type: 'comment', postId: postId.toString() });
        await notification.save();

        return res.status(200).json({
            message: 'commented Successfullly',
            comment: populateComent
        })
    }
    catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });
    }
}


exports.getCommentByPostId = async (req, res) => {
    try {
        const {postId}=req.params;
        const isPostExist=await PostModel.findById(postId);
        if(!isPostExist){
            return res.status(400).json({
                error:"No such Post Found"
            });
        }
            const comments= await commentModal.find({post:postId}).sort({createdAt:-1}).populate("user","f_name headline profile_pic");
            return res.status(200).json({
                message:"Comment Fetched",
                comments:comments
            });
        

    }
    catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });
    }
}