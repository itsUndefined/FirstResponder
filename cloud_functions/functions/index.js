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
    let usersRef = db.collection('users');
    let respondersList = usersRef.get()
        // eslint-disable-next-line promise/always-return
        .then(snapshot => {
            let list = [];
            snapshot.forEach((doc) => {
                let userLocation = [doc.data()['location'].latitude, doc.data()['location'].longitude];
                let distance = calculate.GeoFire.distance(mainLocation, userLocation);
                if (distance <= change.data()['maxDistance']) {
                    admin.messaging().sendToDevice(doc.data()['token'], payload)
                        .then((res) => {
                            return console.log('Successfully sent message:', res);
                        })
                        .catch((err) => {
                            return console.log('Error sending message:', err);
                        });
                    counter++;
                }
                return counter > change.data()['maxUsers'];
            });
        })
        .catch(err => {
            console.log('Error getting documents', err);
        });
});