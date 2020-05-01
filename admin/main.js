const admin = require('firebase-admin');
const client = require('firebase');

const serviceAccount = require('./admin-creds.json');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://firstresponder-1f0df.firebaseio.com"
});

client.initializeApp({
    apiKey: 'AIzaSyBLj34Y-yTq1fQDeM1XTYT5Fh7oFn6AIrg',
    authDomain: 'firstresponder-1f0df.firebaseapp.com',
    databaseURL: 'https://firstresponder-1f0df.firebaseio.com',
    projectId: 'firstresponder-1f0df',
    storageBucket: 'firstresponder-1f0df.appspot.com',
    messagingSenderId: '824608228880',
    appId: '1:824608228880:web:097e07a21aaad438516c95',
    measurementId: 'G-VL2L61F1WE'
});


const receiver = 'eqfz-nk7Qqa3_5aGHb5EFZ:APA91bEyO3gBxxkvG4hjwoR8gYZ31gs9ZrKLx3bi5yL19lx3M6jSrGz3TuI3tUElhUaEvCdcsDJfRXONiIokPiTR638Ak3qkcrX34Ca7FRMfqxNCH5GvQRQYi9ycXkGIHiUOqmHAqJfb';


(async function() {

    // const user = await admin.app().auth().createUser({
    //     email: 'dispatcher@dispatcher.com',
    //     emailVerified: true,
    //     password: 'testtest',
    //     displayName: 'John Doe'
    // });

    // const user2 = await admin.app().auth().createUser({
    //     email: 'responder@responder.com',
    //     emailVerified: true,
    //     password: 'testtest',
    //     displayName: 'John Doe'
    // });

    await admin.messaging().sendToDevice(receiver, {
        data: {
            alert: "alert_id"
        }
    }, {
        priority: "high",
        timeToLive: 0
    });

    // client.auth().sendPasswordResetEmail('themis.chatzie@gmail.com');

    // console.log(await admin.auth().getUserByEmail('themis.chatzie@gmail.com'));

    // const user = await admin.auth().getUserByEmail('dispatcher@dispatcher.com');

    // await admin.auth().setCustomUserClaims(user.uid, {
    //     role: 'dispatcher'
    // });
    
    // console.log(user);
})()