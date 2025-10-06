
const postmodal = require("../model/post");
const { post } = require("../routes/post");

exports.addPost = async (req, res) => {
    try {
        const { desc, image } = req.body;
        let userId = req.user._id;
        const addpost = new postmodal({ user: userId, desc, image });
        if (!addpost) {
            res.status(400).json({ error: "Something went wromg" });
        }
        await addpost.save()
        res.status(200).json({
            message: "Post succesfully",
            post: addpost
        })
    }
    catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });
    }

}

exports.likeDeslikePost = async (req, res) => {
    try {
        const selfId = req.user._id;
        const { postId } = req.body;
        let post = await postmodal.findById(postId);
        if (!post) {
            return res.status(400).json({ error: "No such post found" });

        }
        const index = post.likes.findIndex(id => id.equals(selfId));
        if (index != -1) {
            //User already Liked the post , remove like
            post.likes.splice(index, 1);
        }
        else {
            post.likes.push(selfId)
        }
        await post.save()
        return res.status(200).json({
            message: index !== -1 ? 'Post unliked' : 'Post liked',
            likes: post.likes
        });
    }
    catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });

    }
}

exports.getAllPost = async (req, res) => {
    try {
        let posts = await postmodal.find().sort({ createdAt: -1 }).populate("user", "-password");
        res.status(200).json({
            message: 'fetched data',
            posts: posts
        })


    }
    catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });

    }
}

exports.getPostbyId = async (req, res) => {
    try {
        const { postId } = req.params;




        const post = await postmodal.findById(postId).populate("user", "-password");
        if (!post) {
            return res.status(404).json({ error: "No such post found" });
        }

        return res.status(200).json({
            message: "Fetched data",
            post: post
        });
    }
    catch (err) {
        return res.status(500).json({ error: "server error", message: err.message }).populate("user").limit(5);
        
    }
}

exports.getTop5Post = async (req, res) => {
    try {
        const { userId } = req.params;
        const post = await postmodal.find({ user: userId }).sort({ createdAt: -1 }).populate("user", "-password").limit(5);

        if (!post) {
            return res.status(400).json({
                error: "No Such Post Found"
            })
        }
        return res.status(200).json({
            message: "Fetched Data",
            post: post
        })


    }
    catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });
    }
}

exports.Userposts = async (req, res) => {
    try {

        const { userId } = req.params;
        const posts = await postmodal.find({ user: userId }).sort({ createdAt: -1 }).populate("user", "-password");

         if (!posts || posts.length === 0) {
            return res.status(404).json({
                error: "No posts found for this user"
            })
        }
        return res.status(200).json({
            message: "Fetched Data",
            posts: posts
        })
    }
    catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });

    }
}
