import random
import threading
from locust import HttpUser, task, between
from datetime import datetime, timedelta, timezone

NUM_USERS = 10
USERS = [
    {"username": f"user{i}", "password": "Aa123456@"}
    for i in range(1, NUM_USERS + 1)
]

# Cache token de cac user virtual tai su dung, tranh lam qua tai api login
_tokens_cache = {}
_tokens_lock  = threading.Lock()
_user_index   = 0
_index_lock   = threading.Lock()

def _get_next_user():
    global _user_index
    with _index_lock:
        idx = _user_index % NUM_USERS
        _user_index += 1
    return USERS[idx]

class ScheduleBookingUser(HttpUser):
    # Thoi gian nghi giua cac request cua moi user virtual (0.5s den 1.5s)
    wait_time = between(0.5, 1.5)
    _token    = None
    _username = ""

    def on_start(self):
        """Khai chay virtual user: lay token va luu vao cache."""
        user = _get_next_user()
        self._username = user["username"]
        
        with _tokens_lock:
            if self._username in _tokens_cache:
                self._token = _tokens_cache[self._username]
                return

        payload = {
            "username": user["username"],
            "password": user["password"]
        }
        with self.client.post("/api/v1/auth/login", json=payload, catch_response=True, name="[Auth] Login") as resp:
            if resp.status_code in (200, 201):
                data = resp.json()
                token = (
                    data.get("data", {}).get("accessToken")
                    or data.get("result", {}).get("accessToken")
                    or data.get("accessToken")
                )
                if token:
                    self._token = token
                    with _tokens_lock:
                        _tokens_cache[self._username] = token
                    resp.success()
                else:
                    resp.failure(f"Login OK nhung khong lay duoc token. JSON: {data}")
            else:
                resp.failure(f"Login that bai: {resp.status_code} | {resp.text}")

    @task(3)
    def create_booking_random(self):
        """Task 1: Tao booking ngau nhien de test tai thuc te (Ty le 75%)"""
        if not self._token:
            return
        
        # Ngau nhien chon room tu 1 den 5
        room_id = random.randint(1, 5)
        # Chon ngay ngau nhien tu 10 den 100 ngay trong tuong lai
        random_days = random.randint(10, 100)
        # Giua 8h sang den 16h chieu
        random_hour = random.randint(8, 16)
        
        _tz_offset = "+07:00"
        target_date = (datetime.now(tz=timezone.utc) + timedelta(days=random_days)).strftime("%Y-%m-%d")
        start_time = f"{target_date} {random_hour:02d}:00:00{_tz_offset}"
        end_time = f"{target_date} {random_hour+1:02d}:00:00{_tz_offset}"

        payload = {
            "roomId": room_id,
            "title": f"Locust Load Test - {self._username}",
            "description": "Tai giap lap boi Locust",
            "start": start_time,
            "end": end_time,
            "attendee": 1
        }
        
        headers = {
            "Authorization": f"Bearer {self._token}",
            "Content-Type": "application/json"
        }

        with self.client.post("/api/v1/booking", json=payload, headers=headers, catch_response=True, name="[Booking] createBooking (Random)") as resp:
            if resp.status_code in (200, 201):
                resp.success()
                print(f"[RANDOM SUCCESS] {self._username} dat thanh cong room {room_id}")
            elif resp.status_code == 409:
                resp.success()
                print(f"[RANDOM REJECTED 409] {self._username} dat room {room_id} bi tu choi do trung lich")
            else:
                resp.failure(f"Loi: {resp.status_code} | {resp.text}")

    @task(1)
    def create_booking_competing(self):
        """Task 2: Tranh chap phong trong cung mot ngay/khung gio (Ty le 25%)"""
        if not self._token:
            return

        _tz_offset = "+07:00"
        # Khung gio co dinh: 30 ngay sau, luc 09:00 - 10:00
        target_date = (datetime.now(tz=timezone.utc) + timedelta(days=30)).strftime("%Y-%m-%d")
        start_time = f"{target_date} 09:00:00{_tz_offset}"
        end_time = f"{target_date} 10:00:00{_tz_offset}"

        payload = {
            "roomId": 1,
            "title": f"Locust Competition - {self._username}",
            "description": "Cang tranh phong co dinh",
            "start": start_time,
            "end": end_time,
            "attendee": 1
        }
        
        headers = {
            "Authorization": f"Bearer {self._token}",
            "Content-Type": "application/json"
        }

        with self.client.post("/api/v1/booking", json=payload, headers=headers, catch_response=True, name="[Booking] createBooking (Competing)") as resp:
            if resp.status_code in (200, 201):
                resp.success()
                print(f"[COMPETING SUCCESS] {self._username} dat thanh cong room 1 luc 09:00")
            elif resp.status_code == 409:
                resp.success()
                print(f"[COMPETING REJECTED 409] {self._username} dat room 1 luc 09:00 bi tu choi")
            else:
                resp.failure(f"Loi he thong: {resp.status_code} | {resp.text}")
