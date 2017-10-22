import sys
sys.path.append('/usr/local/lib/python2.7/site-packages')
import cv2
import time
import datetime
import base64
import uuid
import requests
import json
#from grequests import


myuuid = str(uuid.uuid1())

face_cascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')

send_url = 'http://freegeoip.net/json'
r = requests.get(send_url)
j = json.loads(r.text)
lat = j['latitude']
lon = j['longitude']

file_out = "intruder_log.txt"

def main(mirror=False):
    flag = 0
    cam = cv2.VideoCapture(0)
    cam.set(6,24)
    while True:
        ret_val, img = cam.read()
        resized = cv2.resize(img, (img.shape[1]/3,img.shape[0]/3), interpolation = cv2.INTER_AREA)
        ret_val, buffer = cv2.imencode('.jpg',resized)
        gray = cv2.cvtColor(resized, cv2.COLOR_BGR2GRAY)
        jpg_as_text = base64.b64encode(buffer)
        d = datetime.datetime.utcnow()
        epoch = datetime.datetime(1970, 1, 1)
        faces = face_cascade.detectMultiScale(gray, 1.3, 4)
        for (x, y, w, h) in faces:
            cv2.rectangle(resized, (x, y), (x + w, y + h), (255, 0, 0), 2)
            cv2.rectangle(resized, (x - 65, y - 65), (x + w + 50, y + h + 200), (0, 0, 255), 2)
            roi_color = resized[y - 65:y + h + 200, x - 65:x + w + 50]
            ret_val, buffer2 = cv2.imencode('.jpg', roi_color)
            face_as_text = base64.b64encode(buffer2)
        body = {
            "uuid": myuuid,
            "timestamp": int(round((d - epoch).total_seconds())),
            "lat": lat,
            "lon": lon,
            "encoded_image": jpg_as_text,
        }
        
        response = requests.post(url='https://ia8s1k2mhd.execute-api.us-west-2.amazonaws.com/dev/detect', data=body)
        json_data = json.loads(response.text)
        print(json_data)
        if json_data == False:
            flag = flag-1
        elif json_data == True or flag > 0:
            flag = 80
            resized[:, :, 0] = 0
            resized[:, :, 1] = 0
            cv2.putText(resized, 'Firearm Detected', org=(25, 30), fontFace=cv2.FONT_HERSHEY_SIMPLEX, fontScale=1, color=(255, 255, 255))
            cv2.putText(resized, 'lat :'+str(lat), org=(25, 55), fontFace=cv2.FONT_HERSHEY_SIMPLEX, fontScale=1, color=(255, 255, 255))
            cv2.putText(resized, 'lon:'+str(lon), org=(25, 80), fontFace=cv2.FONT_HERSHEY_SIMPLEX, fontScale=1, color=(255, 255, 255))
            cv2.putText(resized, 'D/T:'+str(datetime.datetime.now()), org=(25, 105), fontFace=cv2.FONT_HERSHEY_SIMPLEX, fontScale=1, color=(255, 255, 255))
        back2big = cv2.resize(resized, (1200,750), interpolation = cv2.INTER_AREA)
        cv2.imshow("security camera", back2big)
        if cv2.waitKey(33) == ord('a'):
            break
    cam.release()
    cv2.destroyAllWindows()

main(mirror=False)
