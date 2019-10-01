const functions = require("firebase-functions");
const gcs = require('@google-cloud/storage')();
const os = require('os');
const path = require('path');
const spawn = require('child-process-promise').spawn;
const fs = require('fs');
const url = require('url');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

const resizeImageRuntimeOpts = {
  timeoutSeconds: 90,
  memory: '512MB'
}

exports.resizeImage = functions.runWith(resizeImageRuntimeOpts).storage.object().onFinalize((object) => {

  if (object.metadata['output_image_res'] == null) {
    console.log("EXIT....");
    return null;
  }
  console.log("starting resizing");
  
  const bucket = object.bucket;
  const contentType = object.contentType;
  const filePath = object.name;
  const resolutionSetting = object.metadata['output_image_res'];
  const sender = object.metadata['sender'];
  const messageId = object.metadata['messageId'];
  const threadId = object.metadata['threadId'];
  
  const splittedFilePath = filePath.split('/');
  const fileName = path.basename(filePath);

  const imageFolderName = splittedFilePath[0];
  const usernameFolder = splittedFilePath[1];
  const destinationLow = path.join(imageFolderName, usernameFolder, 'low', fileName);
  const destinationHigh = path.join(imageFolderName, usernameFolder, 'high', fileName);
  
  if (object.resourceState === 'not_exists') {
    console.log('We deleted a file, exit...');
    return;
  }

  const destBucket = gcs.bucket(bucket);
  const tmpFilePath = path.join(os.tmpdir(), fileName);
  const tmpFileLowPath = path.join(os.tmpdir(), 'low', fileName);
  const tmpFileHighPath = path.join(os.tmpdir(), 'high', fileName);

  var lowImageUrl = "gs://" + bucket + "/" + destinationLow;
  var highImageUrl = "gs://" + bucket + "/" + destinationHigh;
  var fullImageUrl = "gs://" + bucket + "/" + filePath;

  // set the url for the full image url first
  let fullImageDataRef = admin.database().ref(`/Threads/${threadId}/messages/chats/${messageId}`);
  fullImageDataRef.child('imageUrlFull').set(fullImageUrl);

  // placeholder for gallery
  var galleryPushId = admin.database().ref(`/Threads/${threadId}/messages/gallery`).push({
    sender: sender,
    imageUrlLow: 'https://raw.githubusercontent.com/adikabintang/kuliah/master/mobile_cloud_computing/proj_resource/loading_small.gif',
    imageUrlHigh: 'https://raw.githubusercontent.com/adikabintang/kuliah/master/mobile_cloud_computing/proj_resource/loading_small.gif',
    imageUrlFull: 'https://raw.githubusercontent.com/adikabintang/kuliah/master/mobile_cloud_computing/proj_resource/loading_small.gif',
    message: "",
    timestamp: admin.database.ServerValue.TIMESTAMP
  }).key;
  
  return destBucket.file(filePath).download({
    destination: tmpFilePath
  }).then(() => {
    return spawn('mkdir', ['-p', path.join(os.tmpdir(), 'high')]);
  }, (error) => {
    console.error("error mkdir: " + error);
  })
  .then(() => {
    return spawn('mkdir', ['-p', path.join(os.tmpdir(), 'low')]);
  }, (error) => {
    console.error("error mkdir: " + error);
  }).then(() => {
    if (resolutionSetting === 'LOW') {
      var convertingHighTask = spawn('cp', [tmpFilePath, tmpFileHighPath]);
      var convertingLowTask = spawn('cp', [tmpFilePath, tmpFileLowPath]);
    } else if (resolutionSetting === 'HIGH') {
      var convertingHighTask = spawn('cp', [tmpFilePath, tmpFileHighPath]);
      var convertingLowTask = spawn('convert', [tmpFilePath, '-resize', '640x480', tmpFileLowPath]); 
    } else if (resolutionSetting === 'FULL') {
      var convertingHighTask = spawn('convert', [tmpFilePath, '-resize', '1280x960', tmpFileHighPath]);
      var convertingLowTask = spawn('convert', [tmpFilePath, '-resize', '640x480', tmpFileLowPath]);
    } else {
      return null;
    }

    return Promise.all([convertingHighTask, convertingLowTask]);
  }).then(snapshotResult => {
    console.log("uploading low res");
    let theMetadata = { 
      contentType: contentType,
      metadata: {
        "owners": object.metadata['owners']
      }
    };

    var uploadTaskLow = destBucket.upload(tmpFileLowPath, {
      destination: destinationLow,
      metadata: theMetadata
    });

    var uploadTaskHigh = destBucket.upload(tmpFileHighPath, {
      destination: destinationHigh,
      metadata: theMetadata
    });

    return Promise.all([uploadTaskHigh, uploadTaskLow]);
  }).then(resultTask => {
    console.log("cleaning...");
    // Once the image has been converted delete the local files to free up disk space.
    fs.unlinkSync(tmpFilePath);
    fs.unlinkSync(tmpFileHighPath);
    fs.unlinkSync(tmpFileLowPath);

    return;
  }).then(() => {
    var chatDataRef = admin.database().ref(`/Threads/${threadId}/messages/chats/${messageId}`);
    var galleryDataRef = admin.database().ref(`/Threads/${threadId}/messages/gallery/${galleryPushId}`);

    var objectMessage = {
      sender: sender,
      imageUrlLow: lowImageUrl,
      imageUrlHigh: highImageUrl,
      imageUrlFull: fullImageUrl,
      message: "",
      timestamp: admin.database.ServerValue.TIMESTAMP
    };

    chatDataRef.set(objectMessage);
    galleryDataRef.set(objectMessage);

    return;
  }).catch(error => {
       console.log("something bad happened somewhere: " + error);
   });
});

exports.sendNotificationV2 = functions.database.ref('/Threads/{threadId}/messages/chats/{messageId}')
  .onWrite((snapshot, context) => {
  const message = snapshot.after.val();
  const senderUid = message.sender; 
  const messageId = context.params.messageId;
  var senderUsername;

  console.log("message: " + message.message);
  console.log("senderUid: " + senderUid);
  console.log("thread id: " + context.params.threadId);
  var listOfRecevierUids = [];
  var listOfReceiverInstanceIds = [];
  
  var getSenderNamePromise = admin.database().ref(`/Users/${senderUid}/username`).once('value');
  
  var getReceiverUidPromise = admin.database().ref().child('Details')
    .child(context.params.threadId).child("details").child("users").once('value');

  return Promise.all([getReceiverUidPromise, getSenderNamePromise])
  .then(snapshot => {
    senderUsername = snapshot[1].val();
    snapshot[0].forEach(function(child) {
      if (child.key.trim() !== senderUid) {
        console.log(child.key + ": " + child.val());
        listOfRecevierUids.push(child.key.trim());
      }
    });
    return;
  })
  .then(() => {
    var getInstanceIdPromise = listOfRecevierUids.map(userId => {
      return admin.database().ref(`/Users/${userId}/instanceId`).once('value', s => s); 
    }); 
    
    return Promise.all(getInstanceIdPromise);
  }).then(data => {
    var len = data.length;
    for (var i = 0; i < len; i++) {
      console.log("data i: " + data[i].val());
      listOfReceiverInstanceIds.push(data[i].val());
    }
    
    return;
  })
  .then(() => {
    var notifMessage;
    console.log("message: " + message.message);
    if (message.message === "") {
      notifMessage = "[Photo]";
    } else {
      notifMessage = message.message;
    }

    console.log("notifMessage: " + notifMessage);
    // https://stackoverflow.com/questions/37565599/firebase-cloud-messaging-fcm-launch-activity-when-user-clicks-the-notificati
    const payload = {
      data: {
        fromThreadId: context.params.threadId
      },
      notification: {
        title: senderUsername, 
        body: notifMessage,
        clickAction: 'CHATTING_ACIVITY'
      }
    };

    var sendingPromises = listOfReceiverInstanceIds.map(id => {
      return admin.messaging().sendToDevice(id, payload);
    });
    
    return Promise.all(sendingPromises);
  })
  .then(sendingResults => {
    var len = sendingResults.length;
    for (var i = 0; i < len; i++) {
      console.log("sendingResults[" + i + "]: ", sendingResults[i]);
      console.log("results[" + i + "]: ", sendingResults[i].results[0].error);
    }

    return;
  })
  .catch(err => {
    console.error("error return promise: " + err);
    return;
  });
});

exports.sendNotificationOnAddedToGroup = functions.database.ref('/Details/{threadId}/details/users/{userId}')
  .onWrite((snapshot, context) => {
  console.log("userId: " + context.params.userId);
  var getGroupName = admin.database().ref(`/Details/${context.params.threadId}/details/name`).once('value');
  var threadType = admin.database().ref(`/Details/${context.params.threadId}/details/type`).once('value');
  var getInstanceId = admin.database().ref(`/Users/${context.params.userId}/instanceId`).once('value');
  var groupName = "";
  var instanceId = "";

  return Promise.all([getGroupName, getInstanceId, threadType])
  .then(snapshot => {
    groupName = snapshot[0].val();
    instanceId = snapshot[1].val();
    threadType = snapshot[2].val();

    // if it's not a thread from group chat, exit
    if (threadType == 1) {
      return null;
    }

    var notifMessage = "You are added to group \"" + groupName + "\"";
    console.log("notifMessage: " + notifMessage);
    console.log("instanceId: " + instanceId);

    const payload = {
      data: {
        fromThreadId: context.params.threadId
      },
      notification: {
        title: "Chit Chat", 
        body: notifMessage,
        clickAction: 'CHATTING_ACIVITY'
      }
    };

    return admin.messaging().sendToDevice(instanceId, payload);
  })
  .then(sendingResults => {
    console.log("sending results: " + sendingResults);
    return;
  });
});