const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();

const calculate = require('geofire');

exports.alertUsers = functions.region('europe-west1').firestore.document('alerts/{alertsId}').onCreate((change, context) => {
    let payload = {
        data: {
            title: 'Alert',
            message: 'Cloud function works'
        }
    };
    let counter = 0;
    let mainLocation = [change.data()['location'].latitude, change.data()['location'].longitude];
    db.collection('users').get()
        // eslint-disable-next-line promise/always-return
        .then(snapshot => {
            snapshot.forEach(doc => {
                let userId = doc.id;
                db.collection('pending').doc(userId).get()
                    // eslint-disable-next-line promise/always-return
                    .then(pendingUser => {
                        // eslint-disable-next-line promise/always-return
                        if (!pendingUser.exists) {
                            let userLocation = [doc.data()['location'].latitude, doc.data()['location'].longitude];
                            let distance = calculate.GeoFire.distance(mainLocation, userLocation);
                            if (distance <= change.data()['maxDistance']) {
                                admin.messaging().sendToDevice(doc.data()['token'], payload)
                                    // eslint-disable-next-line promise/always-return
                                    .then(() => {
                                        let pendingData = {
                                            alertId: change.id,
                                            isActive: false
                                        };
                                        db.collection('pending').doc(userId).set(pendingData)
                                            .then()
                                            .catch();
                                    })
                                    .catch(err => {
                                        return console.log('Error sending message:', err);
                                    });
                                counter++;
                            }
                        }
                    })
                    .catch();
                return counter > change.data()['maxUsers'];
            });
        })
        .catch(err => {
            console.log('Error getting documents', err);
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