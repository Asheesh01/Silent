const mongoose = require('mongoose')

const PostSchema = new mongoose.Schema({
    user: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'user',
    },
    desc: {
        type: String,
    },
    image: {
        type: String
    },
    likes: [
        {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'user',
        }
    ],
    comments: {
        type: Number,
        default: 0
    }
}, { timestamps: true })

const PostModel = mongoose.model('posts', PostSchema);
module.exports = PostModel;