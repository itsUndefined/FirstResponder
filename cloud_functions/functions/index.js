const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();

const geofire = require('geofire');

const {CloudTasksClient} = require('@google-cloud/tasks');

exports.goingBackToNotBusy = functions.region('europe-west3').firestore.document('users/{userId}').onUpdate(async (change, context) => {
   const after = change.after.get('busy');
   if (after === true) {

       const project = 'firstresponder-1f0df';
       const queue = 'alert';
       const location = 'europe-west1';
       const url = 'https://europe-west3-firstresponder-1f0df.cloudfunctions.net/alertCallback';
       const payload = {
           id: change.after.id
       };

       console.log('goingBackToNotBusy: ', payload.id);

       const client = new CloudTasksClient();

       const task = {
            httpRequest: {
                httpMethod: 'POST',
                url,
                body: Buffer.from(JSON.stringify(payload)).toString('base64'),
                headers: {
                    'Content-Type': 'application/json',
                }
            },
            executeTime: Date.now() / 1000 + 1
       };

       const parent = client.queuePath(project, location, queue);

       await client.createTask({ parent: parent, task });
   }
});

exports.alertCallback = functions.region('europe-west3').https.onRequest(async (req, res) => {
    await db.collection('users').doc(req.body.id).update("busy", false);
    res.end();
});

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

    const alertDoc = await db.collection('alertResponders').doc(data.alertId).get();
    if (!alertDoc.exists) {
        return;
    }
    const responders = alertDoc.data().respondersStatus;
    for (const uid in responders) {
        if (uid === context.auth.uid) {

            const updatedData = {};

            if (data.status === 'awaiting') {
                updatedData.knownLocation = new admin.firestore.GeoPoint(data.knownLocation.latitude, data.knownLocation.longitude);
            }

            if (data.status === 'accepted') {
                var currentTime = (Date.now() / 1000);
                if (currentTime - alertDoc.createTime.seconds > 90) {
                    return false;
                }
            }

            updatedData.status = data.status;

            alertDoc.ref.update(`respondersStatus.${uid}`, updatedData);
            break;
        }
    }

    if (data.status === "too_far" || data.status === "rejected") {
        await db.collection('users').doc(context.auth.uid).update("busy", false);
    } else {
        await db.collection('users').doc(context.auth.uid).update("busy", true);
    }
    return true;

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
