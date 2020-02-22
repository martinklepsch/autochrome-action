const functions = require('firebase-functions');
const admin = require('firebase-admin');
const {Storage} = require('@google-cloud/storage');
const nanoid = require('nanoid');
const express = require('express');
const { fileParser } = require('express-multipart-file-parser')
const tempWrite = require('temp-write');

const storage = new Storage();
const app = express();

app.use(fileParser({
  rawBodyOptions: { limit: '1mb', },
  busboyOptions: { limits: { fields: 1 } }
}));

admin.initializeApp();
const bucket = storage.bucket('autochrome-service.appspot.com');

exports.helloWorld = functions.https.onRequest((request, response) => {
 response.send("Hello from Firebase!");
});

async function upload (request, response) {
  console.log(request.files[0]);

  try {
    const tf = tempWrite.sync(request.files[0].buffer);
    console.log(tf);

    await bucket.upload(tf + ".html", {
      destination: nanoid(),
      metadata: {}
    });

    response.send("done");
  } catch (e) {
    console.error(e.message)
    response.status(500).send("error: " + e.message);
  }
}

app.post('/upload', upload);

exports.api = functions.https.onRequest(app);

