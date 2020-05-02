const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();

const geofire = require('geofire');

exports.alertUsers = functions.region('europe-west1').firestore.document('alerts/{alertId}').onCreate(async (change, context) => {

    const maxDistance = 5;
    let mainLocation = [change.data().coordinates.latitude, change.data().coordinates.longitude];
    const idleUsers = await db.collection('users').where('busy', '==', false).get();


    const users = await db.collection('users').get();
    users.forEach(user => {
        console.log(user.data());
    });

    idleUsers.forEach(async user => {
        let userLocation = [user.data().lastKnownLocation.location.latitude, user.data().lastKnownLocation.location.longitude];
        let distance = geofire.GeoFire.distance(mainLocation, userLocation);
        if (distance <= maxDistance) {
            console.log(user.data().token);
            await admin.messaging().sendToDevice(
                user.data().token, 
                {
                    data: {
                        alert: context.params.alertId,
                    }
                }, 
                {
                    priority: 'high',
                    timeToLive: 0
                }
            );
            // let pendingData = {
            //     alertId: change.id,
            //     isActive: false
            // };
            // await db.collection('pending').user(userId).set(pendingData);
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
        .then(async snapshot => {
            // eslint-disable-next-line promise/always-return
            if (snapshot.empty) {
                await db.collection('alerts').doc(change.data()['alertId']).delete();
            }
        })
        .catch();
});