const mongoose = require('mongoose');
const Conversationchema = new mongoose.Schema({
    members: [
        {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'user'
        }
    ]
}, { timestamps: true })

const conversationModal = mongoose.model('conversation', Conversationchema);
module.exports = conversationModal;