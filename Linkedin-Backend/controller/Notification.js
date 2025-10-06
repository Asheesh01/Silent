
const NotificationModal = require('../model/Notification')
exports.getNotification = async (req, res) => {
    try {
        let ownId = req.user._id;
        let notification = await NotificationModal.find({ receiver: ownId }).sort({ createdAt: -1 }).populate("sender receiver");
        return res.status(200).json({
            message: "Notification Fetched Succesfully",
            notification: notification
        })

    }
    catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });
    }
}


exports.updateRead = async (req, res) => {
    try {
        const { notificationId } = req.body;
        const notification = await NotificationModal.findByIdAndUpdate(notificationId, { isRead: true }, { new: true });

        if (!notification) {
            return res.status(400).json({ error: "Notification not found" });

        }
         // CRITICAL: Add this success response
        return res.status(200).json({
            message: "Notification marked as read",
            notification: notification
        });
    }

    catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });
    }

}

exports.activeNotify = async (req, res) => {
    try {
        let ownId=req.user._id;
        let notifications=await NotificationModal.find({receiver:ownId,isRead:false});

        return res.status(200).json({
            message:"Notification Number Fetched Successfully",
            count:notifications.length
        })

    }
    catch (err) {
        return res.status(500).json({ error: "server error", message: err.message });
    }

}


