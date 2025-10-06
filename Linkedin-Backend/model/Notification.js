const mongoose = require('mongoose');

const NotificationSchema = new mongoose.Schema({
    sender: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "user",
        required: true
    },
    receiver: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "user",
        required: true
    },
    content: {
        type: String,
        required: true
    },
    type: {
        type: String,
        required: true,
        enum: ['friendrequest', 'comment']
    },

    isRead: {
        type: Boolean,
        default: false
    },
    postId: {
        type: String,
        default: ""
    }
}, { timestamps: true });

const NotificationModal = mongoose.model('Notification', NotificationSchema);
module.exports =NotificationModal;