const mongoose = require('mongoose')

const userSchema = new mongoose.Schema({
    googleId: {
        type: String,
    },

    email: {
        type: String,
        required: true
    },
    password: {
        type: String,
    },
    f_name: {
        type: String,
        default: " "

    },
    headline: {
        type: String,
        default: " "
    },
    curr_company: {
        type: String,
        default: " "
    },
    curr_location: {
        type: String,
        default: ""
    },
    profile_pic: {
        type: String,
        default: "https://tse1.mm.bing.net/th/id/OIP.rcmXeqCUOiCg54dfU4v9tgHaHa?rs=1&pid=ImgDetMain&o=7&rm=3"
    },
    cover_pic: {
        type: String,
        default: "https://tse3.mm.bing.net/th/id/OIP.dJUEXCnhzTkASA5j0kKeHwHaGl?rs=1&pid=ImgDetMain&o=7&rm=3"
    },

    about: {
        type: String,
        default: ""
    },
    skills: {
        type: [String],
        default: [],
    },
    experiance: [
        {
            designation: {
                type: String,
            },
            company_name: {
                type: String,

            },
            duration: {
                type: String,
            },
            location: {
                type: String,
            },
        }
    ],
    friends: [
        {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'user',
        }
    ],
    pending_friends: [
        {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'user',
        }
    ],
    resume: {
        type: String,
    },
}, { timestamps: true })

const userModel = mongoose.model('user', userSchema);
module.exports = userModel;