from locust import HttpUser, task, between
from queue import Queue

class ApiUser(HttpUser):
    wait_time = between(1, 3)
    token = None

    # Tạo hàng đợi nạp đủ danh sách 1000 user vừa tạo từ DB vào bộ nhớ Locust
    user_credentials = Queue()
    for i in range(1, 1001):
        user_credentials.put_nowait({
            "username": f"user{i}",
            "password": "Aa123456@"
        })

    def on_start(self):
        try:
            # Mỗi luồng ảo (User) khi sinh ra sẽ bốc duy nhất 1 tài khoản trong hàng đợi
            credentials = self.user_credentials.get_nowait()
        except Exception:
            print("Đã hết tài khoản test sạch trong Queue!")
            return

        response = self.client.post(
            "api/v1/auth/login",
            json=credentials
        )

        if response.status_code == 200:
            try:
                json_data = response.json()
                self.token = json_data["data"]["accessToken"]
                print(f"Login thành công cho tài khoản: {credentials['username']}")
            except KeyError:
                print("Login OK nhưng lỗi bóc tách JSON data.")
        else:
            print(f"Login lỗi cho {credentials['username']}: {response.status_code}")

    @task
    def get_bookings(self):
        if not self.token:
            return

        with self.client.get(
            "api/v1/booking/view",
            headers={
                "Authorization": f"Bearer {self.token}"
            },
            params={
                "viewType": "MONTH",
                "targetDate": "2026-07-01"
            },
            catch_response=True
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"{response.status_code}: {response.text}")