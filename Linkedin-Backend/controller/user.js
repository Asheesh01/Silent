const express = require("express");
const User = require("../model/User");
const bcrypt = require("bcrypt");
const NotificationModal = require('../model/Notification');
const { OAuth2Client } = require('google-auth-library');
const jwt = require("jsonwebtoken");

const cookieOptions = {
    httpOnly: true,
    secure: true,
    sameSite: 'None'

};

const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);
exports.loginThroughGmail = async (req, res) => {
    try {
        const { token } = req.body;
        const ticket = await client.verifyIdToken({
            idToken: token,
            audience: process.env.GOOGLE_CLIENT_ID
        })
        const payload = ticket.getPayload();

        const { sub, email, name, picture } = payload;

        let userExists = await User.findOne({ email });
        if (!userExists) {
            userExists = await User.create({

                googleId: sub,
                email,
                f_name: name,
                profile_pic: picture
            })
        }

        let jwttoken = jwt.sign({ userId: userExists._id }, process.env.JWT_PRIVATE_KEY);
        // console.log(token);
        res.cookie('token', jwttoken, cookieOptions)
        return res.status(200).json({ user: userExists });

    }
    catch (err) {
        console.error(err);
        res.status(500).json({ error: 'server error', message: err.message });
    }
}


exports.register = async (req, res) => {
    try {
        let { email, password, f_name } = req.body;

        if (!email || !password || !f_name) {
            return res.status(400).json({ error: "Please fill all fields" });
        }

        let isUserExist = await User.findOne({ email });
        if (isUserExist) {
            return res.status(400).json({ error: "Email already in use" });
        }

        const hashpassword = await bcrypt.hash(password, 10);

        const newUser = new User({ email, password: hashpassword, f_name });
        await newUser.save();

        // never send password back
        return res.status(201).json({
            message: "User registered successfully",
            success: true,
            data: {
                _id: newUser._id,
                email: newUser.email,
                f_name: newUser.f_name
            }
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Server error", message: err.message });
    }
};

exports.login = async (req, res) => {
    try {
        let { email, password } = req.body;
        const userExists = await User.findOne({ email });
        // console.log(userExists);

        if(userExists && !userExists.password){
            return res.status(400).json({error:'Please Login through email'});
        }
        if (userExists && await bcrypt.compare(password, userExists.password)) {
            let token = jwt.sign({ userId: userExists._id }, process.env.JWT_PRIVATE_KEY);
            // console.log(token);
            res.cookie('token', token, cookieOptions)

            return res.json({ message: "Logged in succesfully", success: "true", token, user: userExists });
        }
        else {
            res.status(400).json({ error: "Invalid Credentials" });
        }
    }
    catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Server error', message: err.message });

    }
}
exports.updateUser = async (req, res) => {
    try {
        const { user } = req.body;
        const isExist = await User.findById(req.user._id);
        if (!isExist) {
            res.status(400).json({ error: "User does not exist" })
        }
        const UpdateData = await User.findByIdAndUpdate(isExist._id, user);
        console.log(UpdateData);

        const userData = await User.findById(req.user._id);
        res.status(200).json({
            message: "User Updated successfully",
            user: userData
        });

    }
    catch (err) {
        console.error(err);
        res.status(500).json({ error: 'server error', message: err.message });
    }
}

exports.getProfileById = async (req, res) => {
    try {
        const { id } = req.params;
        const isExist = await User.findById(id);
        if (!isExist) {
            return res.status(400).json({ error: "No Such User Exists" });
        }
        return res.status(200).json({
            message: "User fetched successfully",
            user: isExist
        });

    }
    catch (err) {
        res.status(500).json({ error: "Server Error", message: err.message });
    }
}

exports.logout = async (req, res) => {
    res.clearCookie('token', cookieOptions).json({ message: 'Loged out successfully' });

}


exports.findUser = async (req, res) => {
    try {
        let { query } = req.query;
        const users = await User.find({
            $and: [
                { _id: { $ne: req.user._id } },
                {
                    $or: [
                        { f_name: { $regex: new RegExp(query, 'i') } },  // match first name
                        { email: { $regex: new RegExp(query, 'i') } }
                    ]
                
    }
            ]
});
return res.status(201).json({
    message: 'Fetched Member',
    user: users
})

    }
    catch (err) {
    res.status(500).json({ error: "Server Error", message: err.message });
}
}

exports.sendFriendRequest = async (req, res) => {
    try {
        const sender = req.user._id;
        const { receiver } = req.body;
        const userExists = await User.findById(receiver);
        if (!userExists) {
            return res.status(400).json({
                error: "No Such user Exists."
            });
        };
        const index = req.user.friends.findIndex(id => id.equals(receiver));
        if (index !== -1) {
            return res.status(400).json({
                error: "Already Friends"
            });
        }

        const lastIndex = userExists.pending_friends.findIndex(id => id.equals(req.user._id));
        if (lastIndex !== -1) {
            return res.status(400).json({
                error: "Already Sent Request"
            });
        }

        userExists.pending_friends.push(sender);
        const content = (`${req.user.f_name} has sent you friend request`);
        const Notification = new NotificationModal({ sender, receiver, content, type: "friendrequest" });
        await Notification.save();
        await userExists.save();

        return res.status(200).json({
            message: "Friend Request Sent",

        })

    }
    catch (err) {
        res.status(500).json({ error: "Server Error", message: err.message });
    }
}

exports.acceptFriendReq = async (req, res) => {
    try {
        const { friendId } = req.body;
        const selfId = req.user._id;
        const friendData = await User.findById(friendId);
        if (!friendData) {
            return res.status(400).json({
                error: "No Such user Exists."
            });

        };
        const index = req.user.pending_friends.findIndex(id => id.equals(friendId));
        if (index != -1) {
            req.user.pending_friends.splice(index, 1);
        }
        else {
            return res.status(400).json({
                error: "No any request from such server"
            })
        }

        req.user.friends.push(friendId);
        friendData.friends.push(req.user._id);

        let content = `${req.user.f_name} has accepted your friend request`;
        const notification = new NotificationModal({ sender: req.user._id, receiver: friendId, content, type: "friendrequest" });
        await notification.save();
        await friendData.save();
        await req.user.save();

        return res.status(200).json({
            message: "You both are connected now."
        })

    }
    catch (err) {
        res.status(500).json({ error: "Server Error", message: err.message });
    }
}


exports.getFriendList = async (req, res) => {
    try {
        let friendList = await req.user.populate('friends');
        return res.status(200).json({
            friends: friendList.friends
        })
    }

    catch (err) {
        res.status(500).json({ error: "Server Error", message: err.message });
    }
}


exports.pendingFriendsList = async (req, res) => {
    try {
        let friendList = await req.user.populate('pending_friends');
        return res.status(200).json({
            pending_friends: friendList.pending_friends
        })
    }

    catch (err) {
        res.status(500).json({ error: "Server Error", message: err.message });
    }
}

exports.removeFromFriend = async (req, res) => {

    try {
        let selfId = req.user._id;
        let { friendId } = req.params;

        const friendData = await User.findById(friendId);
        if (!friendData) {
            return res.status(200).json({
                error: "No such user exist"
            })
        }

        const index = req.user.friends.findIndex(id => id.equals(friendId));
        const friendIndex = friendData.friends.findIndex(id => id.equals(selfId));
        if (index !== -1) {
            req.user.friends.splice(index, 1);
        }
        else {
            return res.status(400).json({
                error: "User is not in your friend list"
            })
        }

        if (friendIndex !== -1) {
            friendData.friends.splice(friendIndex, 1);
        }
        await req.user.save();
        await friendData.save();

        return res.status(200).json({
            message: "You both are disconnected now"
        })
    }

    catch (err) {
        res.status(500).json({ error: "Server Error", message: err.message });
    }

}