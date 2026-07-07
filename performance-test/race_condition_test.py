"""
Race Condition Test - createBooking & updateBooking
===================================================
Mo phong 1000 requests gui dong thoi den server de tranh gianh cung 1 phong hop
trong cung 1 khung gio. Ho tro 2 che do test:

1. Chế độ "create_only" (mac dinh cu):
   - 1000 requests cung luc tao moi Booking cho ROOM_ID trong cung khung gio.

2. Chế độ "create_vs_update" (moi):
   - Dau tien: Tao 1 booking ban dau (ROOM_ID=2, khung gio ban dau).
   - Sau do: 500 threads dong thoi cap nhat (PATCH) booking ban dau nay sang ROOM_ID=1 (khung gio dich).
             500 threads khac dong thoi tao moi (POST) booking tai ROOM_ID=1 (khung gio dich).
   - Tat ca 1000 requests deu duoc dong bo hoa qua threading.Barrier de FIRE tai cung 1 mili-giay.

Cach chay:
  pip install requests
  python race_condition_test.py
"""

import threading
import time
import requests
import random
from datetime import datetime, timedelta, timezone

# ──────────────────────────────────────────────
# CAU HINH
# ──────────────────────────────────────────────
# Chon che do test: "create_only" hoac "create_vs_update"
TEST_MODE = "create_vs_update"

BASE_URL    = "http://localhost:8080"
LOGIN_URL   = f"{BASE_URL}/api/v1/auth/login"
BOOKING_URL = f"{BASE_URL}/api/v1/booking"

ROOM_ID      = 1       # <- Phong tranh chap (Room dich)
EQUIPMENT_ID = 1       # projector id = 1
EQUIP_QTY    = 12      # so luong projector moi nguoi yeu cau
NUM_USERS    = 10      # So luong user thuc te co trong DB (user1 -> user10)
NUM_THREADS  = 10      # Tong so thread muon test

# Random offset de tranh trung lich cua lan chay truoc
_tz_offset = "+07:00"
random_offset = random.randint(10, 1000)
_tomorrow  = (datetime.now(tz=timezone.utc) + timedelta(days=random_offset)).strftime("%Y-%m-%d")
START_TIME  = f"{_tomorrow} 09:00:00{_tz_offset}"
END_TIME    = f"{_tomorrow} 10:00:00{_tz_offset}"

# Phong va khung gio cho booking ban dau (chi dung cho che do create_vs_update)
INITIAL_ROOM_ID    = 2
INITIAL_START_TIME = f"{_tomorrow} 14:00:00{_tz_offset}"
INITIAL_END_TIME   = f"{_tomorrow} 15:00:00{_tz_offset}"

USERS = [
    {"username": f"user{i}", "password": "Aa123456@"}
    for i in range(1, NUM_USERS + 1)
]

# ──────────────────────────────────────────────
# HELPERS
# ──────────────────────────────────────────────

def login(username, password):
    """Dang nhap va tra ve access token; None neu that bai."""
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
            print(f"  [OK]  Login thanh cong -> {username}")
            return token
        else:
            print(f"  [FAIL] Login that bai  -> {username} | {resp.text[:200]}")
            return None
    except Exception as exc:
        print(f"  [ERR]  Login loi       -> {username} | {exc}")
        return None


def create_initial_booking(token):
    """Tao booking ban dau o phong khac, gio khac de test Update."""
    payload = {
        "roomId":      INITIAL_ROOM_ID,
        "title":       "Booking ban dau de test Update",
        "description": "Se bi thay doi thoi gian va phong sang ROOM_ID / START_TIME",
        "start":       INITIAL_START_TIME,
        "end":         INITIAL_END_TIME,
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
                print(f"  [INIT OK] Tao booking ban dau thanh cong! ID={booking_id}")
                return booking_id
        print(f"  [INIT FAIL] Khong the tao booking ban dau. Status: {resp.status_code}, Body: {resp.text}")
        return None
    except Exception as exc:
        print(f"  [INIT ERR] Loi khi tao booking ban dau: {exc}")
        return None


def create_booking(token, username, results, barrier):
    """
    Cho tat ca cac thread san sang roi dong loat goi createBooking (POST).
    """
    payload = {
        "roomId":      ROOM_ID,
        "title":       f"Race Test - {username}",
        "description": "Kiem tra race condition - tao boi script tu dong",
        "start":       START_TIME,
        "end":         END_TIME,
        "attendee":    1
    }
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type":  "application/json",
    }

    # Barrier: tat ca thread phai den day truoc khi goi API
    barrier.wait()

    sent_at = time.time()
    try:
        resp = requests.post(BOOKING_URL, json=payload, headers=headers, timeout=15)
        elapsed = (time.time() - sent_at) * 1000  # ms
        results.append({
            "user":       username,
            "type":       "CREATE",
            "status":     resp.status_code,
            "elapsed_ms": round(elapsed, 1),
            "body":       resp.text[:300],
        })
    except Exception as exc:
        results.append({
            "user":       username,
            "type":       "CREATE",
            "status":     "ERROR",
            "elapsed_ms": -1,
            "body":       str(exc),
        })


def update_booking(token, username, booking_id, results, barrier):
    """
    Cho tat ca cac thread san sang roi dong loat goi updateBooking (PATCH).
    """
    payload = {
        "roomId":      ROOM_ID,       # Dung de checkOverlap trong Service
        "newRoomId":   ROOM_ID,       # Room id dich
        "start":       START_TIME,
        "end":         END_TIME
    }
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type":  "application/json",
    }

    # Barrier: tat ca thread phai den day truoc khi goi API
    barrier.wait()

    sent_at = time.time()
    try:
        url = f"{BOOKING_URL}/{booking_id}"
        resp = requests.patch(url, json=payload, headers=headers, timeout=15)
        elapsed = (time.time() - sent_at) * 1000  # ms
        results.append({
            "user":       username,
            "type":       "UPDATE",
            "status":     resp.status_code,
            "elapsed_ms": round(elapsed, 1),
            "body":       resp.text[:300],
        })
    except Exception as exc:
        results.append({
            "user":       username,
            "type":       "UPDATE",
            "status":     "ERROR",
            "elapsed_ms": -1,
            "body":       str(exc),
        })


def print_summary(results):
    """In bang ket qua va tom tat."""
    print("\n" + "=" * 85)
    print(f"  KET QUA RACE CONDITION TEST (MODE: {TEST_MODE.upper()})")
    print("=" * 85)
    print(f"  {'User':<22} {'Type':<8} {'HTTP Status':<14} {'Elapsed (ms)':<16} Snippet")
    print("-" * 85)

    success_codes  = {200, 201}
    
    create_success = 0
    create_reject  = 0
    create_error   = 0
    
    update_success = 0
    update_reject  = 0
    update_error   = 0

    for r in sorted(results, key=lambda x: x["user"]):
        st = r["status"]
        op_type = r.get("type", "CREATE")
        snippet = r["body"][:40].replace("\n", " ")
        snippet = snippet.encode("ascii", "ignore").decode("ascii")
        print(f"  {r['user']:<22} {op_type:<8} {str(st):<14} {str(r['elapsed_ms']):<16} {snippet}")
        
        if op_type == "CREATE":
            if st in success_codes:
                create_success += 1
            elif isinstance(st, int) and st >= 400:
                create_reject += 1
            else:
                create_error += 1
        else: # UPDATE
            if st in success_codes:
                update_success += 1
            elif isinstance(st, int) and st >= 400:
                update_reject += 1
            else:
                update_error += 1

    total_success = create_success + update_success

    print("=" * 85)
    print(f"\n  [CREATE API] Thanh cong: {create_success} | Bi tu choi: {create_reject} | Loi: {create_error}")
    print(f"  [UPDATE API] Thanh cong: {update_success} | Bi tu choi: {update_reject} | Loi: {update_error}")
    print(f"  --> Tong so requests thanh cong (tranh gianh ROOM_ID={ROOM_ID}): {total_success}")
    print()

    if total_success > 1:
        print(f"  [CANH BAO] CO THE XAY RA RACE CONDITION! ({total_success} booking/update thanh cong tren cung 1 phong/khung gio)")
        print("             Kiem tra lai DB de xac nhan xem co bi trung overlap thuc te khong.")
    elif total_success == 1:
        print("  [TOT]  Race condition duoc kiem soat hoan hao (chi duy nhat 1 request thanh cong).")
    else:
        print("  [?]  Khong co request nao thanh cong - kiem tra lai server/roomId/config.")
    print()


# ──────────────────────────────────────────────
# MAIN
# ──────────────────────────────────────────────
def main():
    print("=" * 75)
    print("  RACE CONDITION TEST - CONCURRENT CREATE & UPDATE")
    print(f"  Test Mode : {TEST_MODE}")
    print(f"  Room ID   : {ROOM_ID}")
    print(f"  Time slot : {START_TIME}  ->  {END_TIME}")
    print(f"  DB Users  : user1 -> user{NUM_USERS}")
    print(f"  Threads   : {NUM_THREADS} concurrent threads")
    print("=" * 75)

    # Buoc 1: Dang nhap tat ca user song song
    print("\n[1/3] Dang nhap tat ca user thuc te trong DB...")
    tokens = {}
    login_threads = []
    login_lock    = threading.Lock()

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

    if not tokens:
        print("\n[STOP] Khong dang nhap duoc bat ky user nao. Dung test.")
        return

    token_list = list(tokens.items())
    print(f"\n  -> Dang nhap thanh cong: {len(token_list)}/{NUM_USERS} users thuc te")

    # Buoc 2.1: Chuan bi booking ban dau neu la che do create_vs_update
    booking_id = None
    if TEST_MODE == "create_vs_update":
        print("\n[2.1] Dang tao booking ban dau cho viec cap nhat...")
        # Su dung token cua user dau tien
        booking_id = create_initial_booking(token_list[0][1])
        if not booking_id:
            print("\n[STOP] Khong the chay test vi thieu booking ban dau.")
            return

    # Buoc 2.2: Tao barrier va spawn cac thread
    print(f"\n[2.2] Chuan bi {NUM_THREADS} thread dong thoi...")
    results = []
    barrier = threading.Barrier(NUM_THREADS)

    owner_username, owner_token = token_list[0]

    booking_threads = []
    for i in range(NUM_THREADS):
        username, token = token_list[i % len(token_list)]
        
        if TEST_MODE == "create_vs_update" and i % 2 == 0:
            # 50% Update threads - phai dung token chu so huu booking de co quyen update
            thread_username = f"req-{i+1:04d}-{owner_username}-UPD"
            t = threading.Thread(
                target=update_booking,
                args=(owner_token, thread_username, booking_id, results, barrier),
                daemon=True,
            )
        else:
            # 50% Create threads (hoac 100% neu create_only)
            thread_username = f"req-{i+1:04d}-{username}-CRT"
            t = threading.Thread(
                target=create_booking,
                args=(token, thread_username, results, barrier),
                daemon=True,
            )
        booking_threads.append(t)

    # Khoi dong tat ca thread gan nhu cung luc
    print(f"  -> Dang khoi dong {NUM_THREADS} thread...")
    for t in booking_threads:
        t.start()

    # Cho tat ca xong
    print("  -> Dang cho tat ca thread hoan thanh...")
    for t in booking_threads:
        t.join(timeout=60)

    # Buoc 3: In ket qua
    print("\n[3/3] Tong hop ket qua...")
    print_summary(results)


if __name__ == "__main__":
    main()