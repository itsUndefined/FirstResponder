const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();

const calculate = require('geofire');

exports.alertUsers = functions.region('europe-west1').firestore.document('alerts/{alertsId}').onCreate(async (change, context) => {
    let payload = {
        // notification: {
        //     "body": "Body of Your Notification",
        //     "title": "Title of Your Notification"
        // }
        data: {
            "mydata": "mydata",
        }
    };

    const maxDistance = 5;
    let mainLocation = [change.data().coordinates.lat, change.data().coordinates.lng];
    const snapshot = await db.collection('users').get()

    snapshot.forEach(async doc => {
        let userId = doc.id;
        let pendingUser = await db.collection('pending').doc(userId).get();
        if (!pendingUser.exists) {
            let userLocation = [doc.data()['lastKnownLocation'].location.latitude, doc.data()['lastKnownLocation'].location.longitude];
            let distance = calculate.GeoFire.distance(mainLocation, userLocation);
            console.log(userLocation);
            console.log('distance: ' + distance);
            if (distance <= maxDistance) {
                console.log(doc.data()['token']);
                await admin.messaging().sendToDevice(doc.data()['token'], payload);
                let pendingData = {
                    alertId: change.id,
                    isActive: false
                };
                await db.collection('pending').doc(userId).set(pendingData);
            }
        }
    });
});

exports.acceptAlert = functions.region('europe-west1').firestore.document('pending/{pendingId}').onUpdate((change, context) => {
    db.collection('pending')
        .where('alertId', '==', change.after.data()['alertId'])
        .where('isActive', '==', false)
        .delete()
        .then()
        .catch();
});

exports.rejectAlert = functions.region('europe-west1').firestore.document('pending/{pendingId}').onDelete((change, context) => {
    db.collection('pending').where('alertId', '==', change.data()['alertId']).get()
        .then(snapshot => {
            // eslint-disable-next-line promise/always-return
            if (snapshot.empty) {
                db.collection('alerts').doc(change.data()['alertId']).delete()
                    .then()
                    .catch();
            }
        })
        .catch();
});