const express = require('express');
const router = express.Router();
const userController = require('../controller/user');
const authentication = require("../authenticatins/auth")


router.post('/register', userController.register)
router.post("/login", userController.login)
router.post("/google", userController.loginThroughGmail)
router.put("/update", authentication.auth, userController.updateUser)
router.get('/user/:id', userController.getProfileById)
router.post('/logout', authentication.auth, userController.logout);

router.get("/self", authentication.auth, (req, res) => {
    return res.status(200).json({
        user: req.user
    })
})

router.get('/findUser', authentication.auth, userController.findUser);

router.post('/sendFriendReq', authentication.auth, userController.sendFriendRequest);

router.post('/acceptFriendRequest', authentication.auth, userController.acceptFriendReq);

router.get("/friendList", authentication.auth, userController.getFriendList);

router.get("/pendingfriendList", authentication.auth, userController.pendingFriendsList);

router.delete("/removeFromFriendList/:friendId", authentication.auth, userController.removeFromFriend);



module.exports = router;