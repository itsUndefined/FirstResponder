const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();


exports.sendNotification = functions.firestore.document('alerts/{alertsId}').onCreate((change, context) => {
    let payload = {
      notification: {
          title: 'cloud function',
          message: 'cloud function works',
      }
    };
    return admin.messaging().sendToDevice(
        'fX9ekfYDKQg:APA91bHi4Msr8C4FOWcSu-pZ6EOXzPvU0M34H-SxhPJV93pslH1UVTFRftFH3AKYz-xeZqc4KRMRmN2WFgBXbD30062y5I4OZwk7hS10arI-2V9poUtyadUfKEzz4xD5Lg3ocGkYiaeU',
        payload);
});
