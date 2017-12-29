const AWS = require('aws-sdk');
AWS.config.update({
});
const s3 = new AWS.S3();
const moment = require('moment');
const fileType = require('file-type');
const sha1 = require('sha1');


var getFile = function(fileMime, buffer, fileFullName) {
  var params = {
    Bucket: 'shooter-image',
    Key: fileFullName,
    Body: buffer,
    Metadata: {
        'Content-Type': 'image/jpeg'
    }
  };

  var uploadFile = {
    size: buffer.toString('ascii').length,
    type: fileMime.mime,
    full_path: fileFullName
  };

  return {
    'params': params,
    'uploadFile': uploadFile
  }
};

exports.upload = function(text, fileName) {
  var buffer = new Buffer(text, 'base64');
  var fileMime = fileType(buffer);
  console.log(fileMime);

  var f= getFile(fileMime, buffer, fileName);
  var params = f.params;
  console.log(params);

  return s3.putObject(params, function(err, data) {
    if (err) {
      console.log("err" + err);
      return false;
    }
    console.log("success");
    return true;
  })
}
