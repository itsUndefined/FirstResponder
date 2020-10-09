const admin = require('firebase-admin');
const client = require('firebase');

const serviceAccount = require('./admin-creds.json');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://firstresponder-1f0df.firebaseio.com"
});

client.initializeApp({
    // Firebase credentials go here
});

(async function() {

    // const user = await admin.app().auth().createUser({
    //     email: 'responder@responder.com',
    //     emailVerified: true,
    //     password: 'testtest',
    //     displayName: 'John Doe'
    // });

    // await admin.messaging().sendToDevice(receiver, {
    //     data: {
    //         alert: "alert_id"
    //     }
    // }, {
    //     priority: "high",
    //     timeToLive: 0
    // });

    // await admin.auth().setCustomUserClaims(user.uid, {
    //     role: 'dispatcher'
    // });
    
    // console.log(user);
})()