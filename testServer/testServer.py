import tornado.ioloop
import tornado.web
import os


class FileUploadHandler(tornado.web.RequestHandler):
    def post(self):
        file_dict = self.request.files['file'][0]
        deviceID = self.get_argument("deviceID")
        battery = self.get_argument("battery")
        received_ts = self.get_argument("timestamp")
        filter_beginning = str(file_dict['body']).replace("b'", "")
        filter_mid = filter_beginning.replace("\\r\\", "\n")
        filter_last = filter_mid.replace("'", "")

        file_name = file_dict['filename']

        print('Data received at:', received_ts, ' from:', deviceID)
        print('File received:', file_name)
        print('battery_percent:', battery)
        print('--------------------------')
        print(deviceID)

        file_body = file_dict['body']
        file_path = os.path.join(os.path.dirname(__file__), file_name)
        with open(file_path, 'wb') as f:
            f.write(file_body)
        self.write('File uploaded successfully')


if __name__ == '__main__':
    app = tornado.web.Application([(r'/csv', FileUploadHandler)])
    app.listen(8000)
    tornado.ioloop.IOLoop.current().start()
