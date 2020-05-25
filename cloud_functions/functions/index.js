/* eslint-disable consistent-return */
const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();

const geofire = require('geofire');

const { CloudTasksClient } = require('@google-cloud/tasks');

const secret = 'HCsQZMuzf9qAccYFvMR9%vZ%lZD0i8w&HG8AtK*!mLa!1FgV7Rs%OUyTqquA3OVbfB0zJG%jQk2vWwn10wm8IeX&325rC3QtX0WQO4q2y8uJ4domZ2R8rjuxkIgGjShSzQ2G%X$!YgpCuftGxS6CgqzlV3F1QpLmBLZqkOctSS!s1mBfxxiF^CUN7fSMzk@VJMsPQ!vO8zzDB3NdrnhF7TVcE$nP0B!kJDjwsnQWde70vIzjRUXkSBwr#G!DF19yeGb1b3WOfKrTY2Qtf*@3*iUGAjeVX0Q!V92CE4oY6@$eL*Zle*Y%XZTIgNa4DeKz5@zwa^o3b58glPoqQ@t0FiZB%$YP!AEPXq9PYy57yKOEu*Z2yajQ!INe@w$xWiNn#fZwpqeucnaQna^mks&S1t!pjCa1SXGVmO12Ah1@JJiqFRbUbdeIhSuQag4dFbDLYt^Mohs^Dpt$GB*vkGG5jBzHo6*SG@Eu&SdmKr@RQeYW9IU^2#snqvW3jcEpzMl&B9weLtg@oya8jUfm6P8AevY!5B4RR!lD^Tt1Oc!i!c%^g3Dq&cv6ftlJYKtaED2etXG3cEcCn%#TBQ8k679sYMA#Fe@r7baNiF@ps2mz0Jz3Y3lxAxmo5G7agjBuiKRNmbW5HxVWe!SaOdvBPBGv$M6hn^itnmtRyI9HLOJ7*Nb$W4J@0@BAx@C6tbx5Q*r!PyDkPO8JE@chFHTLUG5#$^h1qMLHV@s#mkFzEXXSkK5U!$fooFrnc6vanP0r05TPCy5GBo1J4aDkPz52YWxq0tBcUQJDZDJbHQd8p6Whp3j&XdV8%i!dO0zk4&z4C1RAL2yuOe53n5EdFS7MHgWNd^0o26U$v^7CO$prQxWzW%ipHu8rn7#CFDssQ6Nn%eMp%TUrYQPIES%zJaoTfoAAGs*!IGz&bYQPpujF0N@xnW@nG1kT%5YtP4gKg6le#pPP8AZXmwmwe71znIQ9NrJmJVqiFsgehsiRu1FDFZmzFKd&2ebk!D!K3bH^OV8w&7pBpgmuI3dAr&a*Ct7bWRksCVsLsFjaI1$qlkaVF@Kw6@W5!$IG4Km8IsFwJuumCSp8w#hifdkrry&V&txOEoI#j9Q^QT0KHA$0HCURzjHji9bvS#0eOlnqC&fhi4wCXCt0K*FeoM1u!7ZLKmksaQp@0iUbh4%eF8B1FR6eYD92A#mVUH6ljGj@KR3^EycxzLjw2EtPLZSfpqM26cla^ahwFUUTGRslR^3zd#xlc#bnpC8LjtPc10az*ts^adzaXpQKHdjEwMuOyy#u@LREv#sBzs7feJPsG%q*nqpzc6Ha!1uCJkzkXl^EbVz52wH74s8YOs20w2nWD*SwM*bL^H&@vCLdx^dSPCE%9bEv8oI@igkOZz95J0UEiMlTj4$noTAD#bHC2x*CR3@%pFY88yucfCn54AfopU0oXTMkPjYT%&vsKS0V@QDrrpN%DN&sx31MePKL6NfcVcQ!0fDz2O3zwBXZs!DpALX9gbGrEYkbgHEcqF6WlAz@qXb0Rl!yaKCNKIc!v*qnlkc42HE6k$A4aZ$Z^15Fb&CcCRP!ZAEz3@wpCEt&WPNh^qf4!vnP%UhHb3LJ6J3PH^cYCoy4!2RzDQ17LfEWuXFa1GEAp@MAzFEBW^I#8NfLG4a6dopZBYT*unoL0nXsN%m$Cv#jEgukWTJFQorYe0&JYn%zw4n^$ZZpBjSl5ugoYHYc%hDaRWV5z3@wKfF^dZ1iYpC##t7cTTTMupH43ZtgSE4TkHGVu9*Wkp7FbiREzMnx3H2$mC180XFrez31$358^xIGF4g@O#SLNgj4@HEZoRwtAJOnZ2G9JGg!rDiaKTQQhtJF&q97d44loTnXY4^iXzAFZdzG^N0ggFPIwb$xdWApur*BQLKGY3I1rqxcpHkyxL!l3@kQ^j9lD7wZzcd1f2s4N8V4fSn2O0&wFzRQa4aUNEWn3pz!LNDmuANag5Ct3Mg0fg7k!5gANs#DKE9Z8^QQH$^I!#bX&*SKC0wWTP!JB07$w1yMHp4Abrm9zHnmFqZL5fT5'

async function checkUserAnswers(alertId) {
    const project = 'firstresponder-1f0df';
    const queue = 'alert';
    const location = 'europe-west1';
    const url = 'https://europe-west3-firstresponder-1f0df.cloudfunctions.net/checkUnanswered';
    const payload = {
        alertId,
        secret
    };

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
        scheduleTime: { seconds: (Date.now() / 1000) + 90 }
    };

    const parent = client.queuePath(project, location, queue);

    await client.createTask({ parent: parent, task });
}

async function unbusyUserAfter(userId, seconds) {
    const project = 'firstresponder-1f0df';
    const queue = 'alert';
    const location = 'europe-west1';
    const url = 'https://europe-west3-firstresponder-1f0df.cloudfunctions.net/alertCallback';
    const payload = {
        userId,
        secret
    };

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
        scheduleTime: { seconds: (Date.now() / 1000) + seconds }
    };

    const parent = client.queuePath(project, location, queue);

    await client.createTask({ parent: parent, task });
}

exports.alertCallback = functions.region('europe-west3').https.onRequest(async (req, res) => {
    if (req.body.secret === secret) {
        await db.collection('users').doc(req.body.userId).update("busy", false);
    }
    res.end();
});

exports.checkUnanswered = functions.region('europe-west3').https.onRequest(async (req, res) => {
    if (req.body.secret === secret) {
        const alertRespondersDoc = db.collection('alertResponders').doc(req.body.alertId);
        const updates = [];
        const responders = (await alertRespondersDoc.get()).data().respondersStatus;
        Object.keys(responders).forEach((responder) => {
            console.log('displaying responder');
            console.log(responder);
            if (responders[responder].status === 'awaiting') {
                updates.push(alertRespondersDoc.update(`respondersStatus.${responder}`, {
                    status: 'ignored'
                }));
                updates.push(unbusyUserAfter(responder, 900));
            }
        });
        await Promise.all(updates);
    }
    res.end();
});

exports.alertUsers = functions.region('europe-west3').firestore.document('alerts/{alertId}').onCreate(async (change, context) => {

    const maxDistance = 5;
    const alertData = change.data();
    let mainLocation = [alertData.coordinates.latitude, alertData.coordinates.longitude];
    let idleUsersQuery = db.collection('users').where('busy', '==', false);

    Object.keys(alertData.requiredSkills).forEach(skill => {
        if(alertData.requiredSkills[skill]) {
            idleUsersQuery = idleUsersQuery.where(`skills.${skill}`, '==', true);
        }
    });

    const idleUsers = await idleUsersQuery.get();

    let userTask = [];
    let respondingUsers = {};


    for (const user of idleUsers.docs) {
        let userLocation = [user.data().lastKnownLocation.location.latitude, user.data().lastKnownLocation.location.longitude];
        let distance = geofire.GeoFire.distance(mainLocation, userLocation);
        if (distance > maxDistance) {
            continue;
        }

        respondingUsers[user.id] = {
            status: "pending_location"
        };

        userTask.push(db.collection('users').doc(user.id).update("busy", true));
        
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
    }

    await Promise.all(userTask);

    await db.collection('alertResponders').doc(context.params.alertId).create({
        respondersStatus: respondingUsers
    });

    await checkUserAnswers(context.params.alertId);

});

exports.updateUserStatus = functions.region('europe-west3').https.onCall(async (data, context) => {

    if (!context.auth) {
        // Throwing an HttpsError so that the client gets the error details.
        throw new functions.https.HttpsError('failed-precondition', 'The function must be called while authenticated.');
    }

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
                // eslint-disable-next-line no-await-in-loop
                await unbusyUserAfter(uid, 3600);
            }

            updatedData.status = data.status;

            // eslint-disable-next-line no-await-in-loop
            await alertDoc.ref.update(`respondersStatus.${uid}`, updatedData);
            break;
        }
    }

    if (data.status === "too_far" || data.status === "rejected") {
        await db.collection('users').doc(context.auth.uid).update("busy", false);
    }
    return true;
});
