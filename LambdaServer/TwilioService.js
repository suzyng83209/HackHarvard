var twilio = require('twilio');
var axios = require('axios');

var accountSid = 'ACb170dd66462caa17dab2b7848d176c11';
var authToken = '36d56c923a4048d9af2a473aee4023d2';
var receiver = '+14153167998';
var sender = '+16397392544';

var geoCodingAPI = 'AIzaSyDEtpXuQTPIjRriZDytcqOtD0aUE8kHN2M';
var t = new twilio(accountSid, authToken);;

exports.generateMessage = function(lat, lng, date) {
    var url = `https://maps.googleapis.com/maps/api/geocode/json?latlng=${parseFloat(lat)},${parseFloat(lng)}&key=${geoCodingAPI}`;

    return axios.get(url).then(response => {
        console.log(response);
        var localAddress = response.data.results[0].formatted_address;

        return `Emergency! Potentially armed and hostile indivial spotted at ${localAddress} at ${date}.`;
    }).catch(err => console.error(err));
}

exports.text = function(message, imgURls) {
    console.log("sending msg");
    console.log(message);
    console.log(imgURls);
    if (typeof message !== 'string') {
        console.log("not string")
        throw new Error('Invalid message format');
    }

    return t.messages
        .create({
            body: message,
            mediaUrl: imgURls,    
            to: receiver,
            from: sender
        })
        .then(response => {
            console.log('Twilio Response: ', response);
        })
        .catch(err => {
            console.error('error: ', err);
        });
}

// class TwilioService {
//     constructor() {
//         if (!t) {
//             t = new twilio(accountSid, authToken);
//         }
//     }

//     generateMessage(data) {
//         var { lat, lng, date } = data;
//         var url = `https://maps.googleapis.com/maps/api/geocode/json?latlng=${lat},${lng}&key=${geoCodingApi}`;

//         return axios.get(url).then(({ results }) => {
//             var localAddress = results[0].formatted_address;

//             return `Emergency! Potentially armed and hostile indivial spotted at ${localAddress} at ${date}.`;
//         });
//     }

//     text(message) {
//         // if (typeof imgURls !== 'object') {
//         //     throw new Error('Invalid data format');
//         // }
//         console.log("sending msg")
//         if (typeof message !== 'string') {
//             throw new Error('Invalid message format');
//         }

//         return t.messages
//             .create({
//                 body: message,
//                 //mediaUrl: imgURls,
//                 to: receiver,
//                 from: sender
//             })
//             .then(response => {
//                 console.log('Twilio Response: ', response);
//             })
//             .catch(err => {
//                 console.error('error: ', err);
//             });
//     }
// }

// module.exports = TwilioService