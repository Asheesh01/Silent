const mongoose = require('mongoose');
// LinkedInClone

mongoose.connect('mongodb://127.0.0.1:27017/LinkedInClone').then(res => {
    console.log("Database fully conneted")
}).catch(err => {
    console.log(err)
})