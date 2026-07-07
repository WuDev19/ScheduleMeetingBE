"""
Race Condition Test - Booking Equipment (Create & Add)
======================================================
Mô phỏng tranh chấp tài nguyên thiết bị (Equipment) giữa nhiều luồng đồng thời.
Thiết bị kiểm tra: Projector có ID = 1, tổng số lượng khả dụng trong hệ thống = 15.

Hỗ trợ 2 chế độ test (chọn qua biến TEST_MODE hoặc đối số dòng lệnh):
1. "create_with_equipment":
   - 10 threads đồng thời tạo mới 10 Booking tại 10 phòng khác nhau (để tránh trùng phòng)
     nhưng đều yêu cầu thuê Projector (ID=1) với số lượng = 15.
   - Do số lượng Projector tối đa là 15, hệ thống chỉ cho phép DUY NHẤT 1 booking thành công.
     9 booking còn lại phải bị từ chối (trả về lỗi HTTP 409).

2. "add_equipment_to_existing":
   - Đầu tiên: 10 users tạo sẵn 10 booking thành công tại 10 phòng khác nhau (không có thiết bị).
   - Sau đó: 10 threads đồng thời gửi yêu cầu bổ sung thiết bị Projector (ID=1) với số lượng = 15
     vào booking tương ứng của họ.
   - Chỉ duy nhất 1 yêu cầu bổ sung thành công, 9 yêu cầu còn lại bị từ chối (HTTP 409).

Cách chạy:
  pip install requests
  python race_condition_equipment_test.py --mode create
  python race_condition_equipment_test.py --mode add
"""

import argparse
import threading
import time
import requests
import random
from datetime import datetime, timedelta, timezone

# ──────────────────────────────────────────────
# DEFAULT CONFIGURATION
# ──────────────────────────────────────────────
TEST_MODE = "create_with_equipment"  # "create_with_equipment" or "add_equipment_to_existing"

BASE_URL    = "http://localhost:8080"
LOGIN_URL   = f"{BASE_URL}/api/v1/auth/login"
BOOKING_URL = f"{BASE_URL}/api/v1/booking"

EQUIPMENT_ID = 1   # Projector ID
EQUIP_QTY    = 15  # Requested quantity (equals total Projectors in system)
NUM_USERS    = 10  # Number of DB users (user1 -> user10)
NUM_THREADS  = 10  # Number of concurrent threads

# Random offset to avoid time overlaps with past bookings
_tz_offset = "+07:00"
random_offset = random.randint(10, 1000)
_tomorrow = (datetime.now(tz=timezone.utc) + timedelta(days=random_offset)).strftime("%Y-%m-%d")
START_TIME = f"{_tomorrow} 09:00:00{_tz_offset}"
END_TIME   = f"{_tomorrow} 10:00:00{_tz_offset}"

USERS = [
    {"username": f"user{i}", "password": "Aa123456@"}
    for i in range(1, NUM_USERS + 1)
]

# ──────────────────────────────────────────────
# HELPERS
# ──────────────────────────────────────────────

def login(username, password):
    """Log in and return the access token."""
    try:
        resp = requests.post(
            LOGIN_URL,
            json={"username": username, "password": password},
            timeout=10,
        )
        data = resp.json()
        token = (
            data.get("data", {}).get("accessToken")
            or data.get("data", {}).get("access_token")
            or data.get("result", {}).get("accessToken")
            or data.get("accessToken")
            or data.get("access_token")
        )
        if token:
            print(f"  [OK]  Login successful -> {username}")
            return token
        else:
            print(f"  [FAIL] Login failed -> {username} | {resp.text[:200]}")
            return None
    except Exception as exc:
        print(f"  [ERR]  Login error -> {username} | {exc}")
        return None


def create_base_booking(token, username, room_id):
    """Create a base booking without equipment (used for add_equipment mode)."""
    payload = {
        "roomId":      room_id,
        "title":       f"Base Booking for Add Equip - {username}",
        "description": "Base booking for concurrent equipment addition test",
        "start":       START_TIME,
        "end":         END_TIME,
        "attendee":    1
    }
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type":  "application/json",
    }
    try:
        resp = requests.post(BOOKING_URL, json=payload, headers=headers, timeout=10)
        if resp.status_code in (200, 201):
            data = resp.json().get("data", {})
            booking_id = data.get("id") or data.get("bookingId")
            if booking_id:
                return booking_id
        print(f"  [FAIL] Cannot create base booking for {username}. Status: {resp.status_code}, Body: {resp.text[:200]}")
        return None
    except Exception as exc:
        print(f"  [ERR] Error creating base booking for {username}: {exc}")
        return None


def create_booking_with_equipment_worker(token, username, room_id, results, barrier):
    """Thread worker: Create booking with equipment concurrently."""
    payload = {
        "roomId":      room_id,
        "title":       f"Race Equip Create - {username}",
        "description": f"Equipment race test - {username}",
        "start":       START_TIME,
        "end":         END_TIME,
        "attendee":    1,
        "equipments": [
            {
                "equipmentId": EQUIPMENT_ID,
                "quantity": EQUIP_QTY
            }
        ]
    }
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type":  "application/json",
    }

    # Wait for all threads to be ready
    barrier.wait()

    sent_at = time.time()
    try:
        resp = requests.post(BOOKING_URL, json=payload, headers=headers, timeout=15)
        elapsed = (time.time() - sent_at) * 1000
        results.append({
            "user":       username,
            "type":       "CREATE_WITH_EQUIP",
            "status":     resp.status_code,
            "elapsed_ms": round(elapsed, 1),
            "body":       resp.text[:200],
        })
    except Exception as exc:
        results.append({
            "user":       username,
            "type":       "CREATE_WITH_EQUIP",
            "status":     "ERROR",
            "elapsed_ms": -1,
            "body":       str(exc),
        })


def add_equipment_worker(token, username, booking_id, results, barrier):
    """Thread worker: Add equipment to existing booking concurrently."""
    payload = [
        {
            "equipmentId": EQUIPMENT_ID,
            "quantity": EQUIP_QTY,
            "action": "ADD"
        }
    ]
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type":  "application/json",
    }

    # Wait for all threads to be ready
    barrier.wait()

    sent_at = time.time()
    try:
        url = f"{BOOKING_URL}/{booking_id}/equipment"
        resp = requests.post(url, json=payload, headers=headers, timeout=15)
        elapsed = (time.time() - sent_at) * 1000
        results.append({
            "user":       username,
            "type":       "ADD_EQUIP",
            "status":     resp.status_code,
            "elapsed_ms": round(elapsed, 1),
            "body":       resp.text[:200],
        })
    except Exception as exc:
        results.append({
            "user":       username,
            "type":       "ADD_EQUIP",
            "status":     "ERROR",
            "elapsed_ms": -1,
            "body":       str(exc),
        })


# ──────────────────────────────────────────────
# PRINT SUMMARY
# ──────────────────────────────────────────────

def print_summary(results, mode):
    print("\n" + "=" * 90)
    print(f"  RACE CONDITION TEST RESULTS (MODE: {mode.upper()})")
    print("=" * 90)
    print(f"  {'User':<15} {'Type':<22} {'HTTP Status':<12} {'Elapsed (ms)':<15} Server Response (Snippet)")
    print("-" * 90)

    success_codes = {200, 201}
    success_count = 0
    reject_count  = 0
    error_count   = 0

    for r in sorted(results, key=lambda x: x["user"]):
        st = r["status"]
        op_type = r["type"]
        body_snippet = r["body"][:50].replace("\n", " ")
        body_snippet = body_snippet.encode("ascii", "ignore").decode("ascii")
        
        print(f"  {r['user']:<15} {op_type:<22} {str(st):<12} {str(r['elapsed_ms']):<15} {body_snippet}")

        if st in success_codes:
            success_count += 1
        elif isinstance(st, int) and st >= 400:
            reject_count += 1
        else:
            error_count += 1

    print("=" * 90)
    print(f"  -> Total SUCCESS requests (Projector allocated): {success_count}")
    print(f"  -> Total REJECTED requests (exceed quantity): {reject_count}")
    print(f"  -> Total SYSTEM/CONNECTION ERROR requests: {error_count}")
    print("-" * 90)

    if success_count > 1:
        print("  [WARNING] CO-ALLOCATION DETECTED! RACE CONDITION OCCURRED!")
        print(f"            Multiple requests ({success_count}) were allocated Projector (ID=1, quantity=15).")
        print("            Total quantity exceeds limit.")
    elif success_count == 1:
        print("  [OK] Race condition prevented successfully!")
        print("       Only exactly one request succeeded. Others were rejected.")
    elif success_count == 0:
        print("  [?] No request succeeded.")
        print("      Projector might be fully allocated already.")
        print("      Please check DB state or release Projector before running test.")
    print()


# ──────────────────────────────────────────────
# MAIN
# ──────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="Test race condition for booking equipment.")
    parser.add_argument(
        "--mode", 
        choices=["create", "add"], 
        help="Choose test mode: 'create' (create booking with equipment) or 'add' (add equipment to existing booking)"
    )
    args = parser.parse_args()

    # Determine mode
    mode = TEST_MODE
    if args.mode == "create":
        mode = "create_with_equipment"
    elif args.mode == "add":
        mode = "add_equipment_to_existing"

    print("=" * 75)
    print("  RACE CONDITION TEST - BOOKING EQUIPMENT CONCURRENCY")
    print(f"  Test Mode    : {mode}")
    print(f"  Equipment ID : {EQUIPMENT_ID} (Projector)")
    print(f"  Request Qty  : {EQUIP_QTY}")
    print(f"  Time Window  : {START_TIME} -> {END_TIME}")
    print(f"  Concurrency  : {NUM_THREADS} concurrent threads")
    print("=" * 75)

    # Step 1: Login all users in parallel
    print("\n[1/3] Logging in users (user1 -> user10)...")
    tokens = {}
    login_threads = []
    login_lock = threading.Lock()

    def _login_worker(u):
        tok = login(u["username"], u["password"])
        if tok:
            with login_lock:
                tokens[u["username"]] = tok

    for user in USERS:
        t = threading.Thread(target=_login_worker, args=(user,))
        login_threads.append(t)
        t.start()
    for t in login_threads:
        t.join()

    if len(tokens) < NUM_THREADS:
        print(f"\n[STOP] Only logged in {len(tokens)}/{NUM_THREADS} users. Stopping test.")
        return

    token_list = [tokens[u["username"]] for u in USERS]
    print(f"  -> Successfully logged in {len(token_list)}/{NUM_USERS} users.")

    results = []
    barrier = threading.Barrier(NUM_THREADS)
    threads = []

    # Step 2: Run according to chosen mode
    if mode == "create_with_equipment":
        print("\n[2/3] Running Test: Concurrently creating bookings with equipment...")
        for i in range(NUM_THREADS):
            username = f"user{i+1}"
            token = token_list[i]
            # Use different rooms (IDs 1 to 10) to avoid room overlaps
            room_id = i + 1
            t = threading.Thread(
                target=create_booking_with_equipment_worker,
                args=(token, username, room_id, results, barrier),
                daemon=True
            )
            threads.append(t)

    elif mode == "add_equipment_to_existing":
        print("\n[2/3] Preparing: Pre-creating 10 base bookings for 10 users...")
        booking_ids = []
        for i in range(NUM_THREADS):
            username = f"user{i+1}"
            token = token_list[i]
            room_id = i + 1
            bid = create_base_booking(token, username, room_id)
            if not bid:
                print(f"[STOP] Failed to prepare all base bookings. Stopping test.")
                return
            booking_ids.append(bid)
            print(f"  -> Created base booking for {username}: ID={bid}")

        print("\n[2.5/3] Running Test: Concurrently adding Projector to existing bookings...")
        for i in range(NUM_THREADS):
            username = f"user{i+1}"
            token = token_list[i]
            booking_id = booking_ids[i]
            t = threading.Thread(
                target=add_equipment_worker,
                args=(token, username, booking_id, results, barrier),
                daemon=True
            )
            threads.append(t)

    # Launch threads concurrently
    print(f"  -> Launching {NUM_THREADS} threads...")
    for t in threads:
        t.start()

    # Wait for completion
    print("  -> Waiting for threads to finish...")
    for t in threads:
        t.join(timeout=60)

    # Step 3: Print summary
    print("\n[3/3] Summarizing results...")
    print_summary(results, mode)


if __name__ == "__main__":
    main()
