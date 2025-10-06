const mongoose = require('mongoose');

const commentSchema = new mongoose.Schema({
    user: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'user',
        required: true
    },
    post: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'post',
        required: true
    },
    comment: {
        type: String,
        required: true
    }
}, { timestamps: true })

const PostModel = mongoose.model('comment', commentSchema);
module.exports = PostModel;