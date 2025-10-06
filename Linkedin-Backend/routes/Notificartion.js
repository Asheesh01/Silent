const express = require("express");
const router = express.Router();
const authenticatins = require("../authenticatins/auth");
const NotificationController = require("../controller/Notification");


router.get('/', authenticatins.auth, NotificationController.getNotification);
router.put('/isRead', authenticatins.auth, NotificationController.updateRead);
router.get('/aciveNotification',authenticatins.auth,NotificationController.activeNotify);

module.exports = router;