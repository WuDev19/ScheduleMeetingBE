import random
import threading
from locust import HttpUser, task, between
from datetime import datetime, timedelta, timezone

NUM_USERS = 10
USERS = [
    {"username": f"user{i}", "password": "Aa123456@"}
    for i in range(1, NUM_USERS + 1)
]

# Token cache to reuse across virtual users and avoid login rate limiting
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

class EquipmentRaceUser(HttpUser):
    # Short wait time to execute requests close to each other
    wait_time = between(0.1, 0.5)
    _token    = None
    _username = ""
    _user_id  = 1

    def on_start(self):
        """Executed when a virtual user starts: log in and get token."""
        user = _get_next_user()
        self._username = user["username"]
        try:
            self._user_id = int(self._username.replace("user", ""))
        except ValueError:
            self._user_id = 1
        
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
                    resp.failure(f"Login succeeded but token not found in response: {data}")
            else:
                resp.failure(f"Login failed: {resp.status_code} | {resp.text}")

    @task(1)
    def create_booking_with_equipment(self):
        """
        Task 1: Concurrently create a booking with equipment.
        To avoid room overlaps (which would mask equipment race conditions),
        each user requests a different room (IDs 1-10) and a unique time slot.
        """
        if not self._token:
            return

        # Room ID 1-10 based on user ID
        room_id = (self._user_id - 1) % 10 + 1
        
        # Unique date offset to prevent room conflicts
        _tz_offset = "+07:00"
        random_days = random.randint(10, 1000)
        target_date = (datetime.now(tz=timezone.utc) + timedelta(days=random_days)).strftime("%Y-%m-%d")
        
        start_time = f"{target_date} 09:00:00{_tz_offset}"
        end_time   = f"{target_date} 10:00:00{_tz_offset}"

        payload = {
            "roomId": room_id,
            "title": f"Locust Create with Equip - {self._username}",
            "description": "Locust concurrent booking with equipment",
            "start": start_time,
            "end": end_time,
            "attendee": 1,
            "equipments": [
                {
                    "equipmentId": 1,  # Projector ID
                    "quantity": 15     # Total available Projectors
                }
            ]
        }
        
        headers = {
            "Authorization": f"Bearer {self._token}",
            "Content-Type": "application/json"
        }

        with self.client.post("/api/v1/booking", json=payload, headers=headers, catch_response=True, name="[Booking] Create with Equipment") as resp:
            if resp.status_code in (200, 201):
                resp.success()
                print(f"[SUCCESS] {self._username} created booking with 15 Projectors in Room {room_id}")
            elif resp.status_code == 409:
                # 409 is the expected behavior for other concurrent users since Projector quantity is limited to 15
                resp.success()
                print(f"[REJECTED 409] {self._username} booking rejected (Projector limit reached)")
            else:
                resp.failure(f"Unexpected response code: {resp.status_code} | {resp.text}")

    @task(1)
    def add_equipment_to_existing_booking(self):
        """
        Task 2: First create a base booking, and then concurrently request to add Projector (qty=15).
        """
        if not self._token:
            return

        # 1. Create a base booking first
        room_id = (self._user_id - 1) % 10 + 1
        _tz_offset = "+07:00"
        random_days = random.randint(10, 1000)
        target_date = (datetime.now(tz=timezone.utc) + timedelta(days=random_days)).strftime("%Y-%m-%d")
        
        start_time = f"{target_date} 14:00:00{_tz_offset}"
        end_time   = f"{target_date} 15:00:00{_tz_offset}"

        base_payload = {
            "roomId": room_id,
            "title": f"Locust Base Booking - {self._username}",
            "description": "Locust base booking to test equipment addition",
            "start": start_time,
            "end": end_time,
            "attendee": 1
        }
        
        headers = {
            "Authorization": f"Bearer {self._token}",
            "Content-Type": "application/json"
        }

        booking_id = None
        with self.client.post("/api/v1/booking", json=base_payload, headers=headers, catch_response=True, name="[Booking] Create Base for Equip Add") as resp:
            if resp.status_code in (200, 201):
                data = resp.json().get("data", {})
                booking_id = data.get("id") or data.get("bookingId")
                resp.success()
            else:
                resp.failure(f"Failed to create base booking: {resp.status_code} | {resp.text}")
                return

        if not booking_id:
            return

        # 2. Add equipment to the created booking
        equip_payload = [
            {
                "equipmentId": 1,  # Projector ID
                "quantity": 15,    # Projector limit
                "action": "ADD"
            }
        ]

        url = f"/api/v1/booking/{booking_id}/equipment"
        with self.client.post(url, json=equip_payload, headers=headers, catch_response=True, name="[Booking] Add Equipment") as resp:
            if resp.status_code in (200, 201):
                resp.success()
                print(f"[SUCCESS] {self._username} added 15 Projectors to Booking {booking_id}")
            elif resp.status_code == 409:
                resp.success()
                print(f"[REJECTED 409] {self._username} failed to add Projector to Booking {booking_id} (Limit reached)")
            else:
                resp.failure(f"Unexpected response code for add equipment: {resp.status_code} | {resp.text}")
