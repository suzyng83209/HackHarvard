var firebase = require('firebase');
var axios = require('axios');
var s3 = require('./s3upload');
const moment = require('moment');
const fileType = require('file-type');
const sha1 = require('sha1');
const DETECTION_INTERVAL = 10000; // 10 sec
const config = {
    apiKey: "AIzaSyCwQpL7RWnl9yYU48VNGObXms4O7TUmUzw",
    authDomain: "gundetection.firebaseapp.com",
    databaseURL: "https://gundetection.firebaseio.com",
    projectId: "gundetection",
    storageBucket: "gundetection.appspot.com"
};

class GunDetectionServices {
    constructor() {
        if (!firebase.apps.length) {
            firebase.initializeApp(config);
        }
    }
    getPastGunDection(uuid, timestamp) {
        return firebase.database().ref('/past/' + uuid).once('value').then(function(snapshot) {
            var datetime = snapshot.val().datetime;
            if (parseInt(timestamp) - parseInt(datetime) <= DETECTION_INTERVAL) {
                return true;
            } else {
                return false;
            }
        }).catch(function(error) {
            return false;
        });
    }
    analyzeImage(text) {
        //console.log(text);
        return axios({
            method: 'POST',
            url: 'https://vision.googleapis.com/v1/images:annotate?key=AIzaSyBJ3xU8C6KCSG-8zcAVXwfUEigT0oEEtFc',
            data: {
                "requests":[
                    {
                    "image":{
                        "content":decodeURIComponent(text)
                        },
                        "features":[
                            {
                            "type":"LABEL_DETECTION",
                            "maxResults":10
                            }
                        ]
                    }
                ]
            }
        }).then(function(response) {
            console.log('response:' + response);
            console.log(response.data.responses[0].labelAnnotations);
            return response;
        }).catch(function(error){
            console.log('analyze failed', error.response);
            return error;
        });
    }
    analyzeGCP(response) {
        var labels = response.data.responses[0].labelAnnotations;
        return labels.some((label) => {
            return label.description.includes('gun') || label.description.includes('firearm');
        }); 
    }
    detectGun(uuid, timestamp, lat, lon, encoded_image) {
        console.log(uuid);
        console.log(encoded_image);
        return this.getPastGunDection(uuid, timestamp)
        .then((passDectection) => {
            if (!passDectection) {
                console.log("no past detection");
                return this.analyzeImage(encoded_image)
                .then((response) => {
                    if (this.analyzeGCP(response)){
                        console.log('gun detected');

                        var buffer = new Buffer(decodeURIComponent(encoded_image), 'base64');
                        var fileExt = fileType(buffer).ext;
                        var hash = sha1(new Buffer(new Date().toString()));
                        var now = moment().format('YYYY-MM-DD-HH:mm:ss');
                        var filePath = hash + '/';
                        var fileName = now+'.'+fileExt;
                        var fileFullName = 'shooter-image/' + filePath + fileName;

                        var uuidRef = firebase.database().ref('/past/' + uuid);
                        uuidRef.update({
                            datetime: timestamp,
                            coordinates: [lat, lon],
                            url: 'https://s3-us-west-2.amazonaws.com/shooter-image/' + fileFullName
                        }).then(()=>{
                            s3.upload(decodeURIComponent(encoded_image), fileFullName);
                            return null;
                        }).catch(err => console.error('update failed', err));
                        return {'success':true, 'url':'https://s3-us-west-2.amazonaws.com/shooter-image/' + fileFullName};
                    } else {
                        return {'success':false, 'url':''};
                    }
                })
            } else {
                return {'success':false, 'url':''};
            }
        }).catch(function (err) {
            console.log('detect gun failed', err);
            return {'success':false, 'url':''};
        });
    }
}

module.exports = GunDetectionServices