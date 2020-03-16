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
  rawBodyOptions: { limit: '500kb', },
  busboyOptions: { limits: { fields: 1 } }
}));

admin.initializeApp();
const bucket = admin.storage().bucket();

async function upload (request, response) {
  try {
    const tf = tempWrite.sync(request.files[0].buffer);
    const loc = "diffs/" + nanoid() + ".html"

    await bucket.upload(tf, {
      destination: loc,
      metadata: {contentType: 'text/html'}
    });

    const url = await storage.child(loc).getDownloadURL();
    response.send(url)
  } catch (e) {
    console.error(e.message)
    response.status(500).send("error: " + e.message);
  }
}

app.post('/upload', upload);

exports.api = functions.https.onRequest(app);
