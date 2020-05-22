const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();

const geofire = require('geofire');

exports.alertUsers = functions.region('europe-west3').firestore.document('alerts/{alertId}').onCreate(async (change, context) => {

    const maxDistance = 5;
    const alertData = change.data();
    let mainLocation = [alertData.coordinates.latitude, alertData.coordinates.longitude];
    let idleUsersQuery = db.collection('users');//.where('busy', '==', false);

    Object.keys(alertData.requiredSkills).forEach(skill => {
        console.log('skill: ' + skill);
        console.log(alertData.requiredSkills[skill]);
        if(alertData.requiredSkills[skill]) {
            idleUsersQuery = idleUsersQuery.where(`skills.${skill}`, '==', true);
        }
    });

    const idleUsers = await idleUsersQuery.get();

    let userTask = [];
    let respondingUsers = {};


    for(const user of idleUsers.docs) {
        console.log(user.data());

        let userLocation = [user.data().lastKnownLocation.location.latitude, user.data().lastKnownLocation.location.longitude];
        let distance = geofire.GeoFire.distance(mainLocation, userLocation);
        if (distance > maxDistance) {
            continue;
        }


        respondingUsers[user.id] = {
            status: "pending_location"
        };
        
        userTask.push(
            admin.messaging().sendToDevice(
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
            )
        );
            // let pendingData = {
            //     alertId: change.id,
            //     isActive: false
            // };
            // await db.collection('pending').user(userId).set(pendingData);
    }

    await Promise.all(userTask);

    await db.collection('alertResponders').doc(context.params.alertId).create({
        respondersStatus: respondingUsers
    });

    await change.ref.update("isExpired", false);

});

exports.updateUserStatus = functions.region('europe-west3').https.onCall(async (data, context) => {
    console.log(data);
    const alertDoc = await db.collection('alertResponders').doc(data.alertId).get();
    if (!alertDoc.exists) {
        return;
    }
    const responders = alertDoc.data().respondersStatus;
    for (const uid in responders) {
        if (uid === context.auth.uid) {
            alertDoc.ref.update(`respondersStatus.${uid}`, {
                knownLocation: new admin.firestore.GeoPoint(data.knownLocation.latitude, data.knownLocation.longitude),
                status: data.status
            });
            break;
        }
    }

    if (data.status === "too_far" || data.status === "rejected") {
        await db.collection('users').doc(context.auth.uid).update("busy", false);
    } else {
        await db.collection('users').doc(context.auth.uid).update("busy", false);
    }


});

// exports.acceptAlert = functions.region('europe-west3').firestore.document('pending/{pendingId}').onUpdate((change, context) => {
//     db.collection('pending')
//         .where('alertId', '==', change.after.data()['alertId'])
//         .where('isActive', '==', false)
//         .delete()
//         .then()
//         .catch();
// });

// exports.rejectAlert = functions.region('europe-west3').firestore.document('pending/{pendingId}').onDelete((change, context) => {
//     db.collection('pending').where('alertId', '==', change.data()['alertId']).get()
//         .then(async snapshot => {
//             // eslint-disable-next-line promise/always-return
//             if (snapshot.empty) {
//                 await db.collection('alerts').doc(change.data()['alertId']).delete();
//             }
//         })
//         .catch();
// });