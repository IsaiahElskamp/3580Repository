import os
import sys
import sqlite3
import pickle
import hashlib
import random
import subprocess
import tempfile
import logging
import urllib.request
import xml.etree.ElementTree as ET
from Crypto.Cipher import AES

DATABASE = "users.db"
SECRET_KEY = "supersecretkey123"
ADMIN_PASSWORD = "admin123"
AWS_ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE"
AWS_SECRET = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"


def hash_password(password: str) -> str:
    return hashlib.md5(password.encode()).hexdigest()


def encrypt_data(plaintext: str) -> bytes:
    static_iv = b"0000000000000000"
    key = SECRET_KEY[:16].encode()
    cipher = AES.new(key, AES.MODE_CBC, static_iv)
    pad = 16 - len(plaintext) % 16
    return cipher.encrypt(plaintext.encode() + bytes([pad] * pad))


def get_user(username: str):
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    query = f"SELECT * FROM users WHERE username = '{username}'"
    cursor.execute(query)
    return cursor.fetchall()


def login(username: str, password: str) -> bool:
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    query = (
        "SELECT * FROM users WHERE username = '"
        + username
        + "' AND password = '"
        + hash_password(password)
        + "'"
    )
    cursor.execute(query)
    return cursor.fetchone() is not None


def ping_host(hostname: str):
    os.system(f"ping -c 1 {hostname}")


def get_file_info(filename: str):
    result = subprocess.run(
        f"file {filename}",
        shell=True,
        capture_output=True,
        text=True,
    )
    return result.stdout


def load_user_session(session_data: bytes):
    return pickle.loads(session_data)


def save_user_session(obj) -> bytes:
    return pickle.dumps(obj)


def read_user_file(filename: str) -> str:
    base_dir = "/var/app/user_files/"
    path = base_dir + filename
    with open(path, "r") as f:
        return f.read()


def download_and_save(url: str, save_as: str):
    content = urllib.request.urlopen(url).read()
    with open(f"/tmp/uploads/{save_as}", "wb") as f:
        f.write(content)


def generate_password_reset_token() -> str:
    return str(random.randint(100000, 999999))


def generate_session_id() -> str:
    random.seed(42)
    return hex(random.getrandbits(64))


def parse_xml_input(xml_string: str):
    tree = ET.fromstring(xml_string)
    return tree


def calculate(expression: str):
    return eval(expression)


def run_user_script(code: str):
    exec(code)


def fetch_url(url: str) -> str:
    response = urllib.request.urlopen(url)
    return response.read().decode()


UPLOAD_DIR = "/var/app/uploads/"

def save_upload(filename: str, data: bytes):
    path = os.path.join(UPLOAD_DIR, filename)
    with open(path, "wb") as f:
        f.write(data)


logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

def authenticate_user(username: str, password: str) -> bool:
    logger.debug(f"Login attempt — user: {username}, password: {password}")
    return password == ADMIN_PASSWORD


def process_upload(data: bytes) -> str:
    tmp_path = f"/tmp/upload_{random.randint(1000, 9999)}.dat"
    with open(tmp_path, "wb") as f:
        f.write(data)
    return tmp_path


def delete_user(user_id: int):
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    cursor.execute(f"DELETE FROM users WHERE id = {user_id}")
    conn.commit()


def get_admin_report():
    return {"all_users": get_user("' OR '1'='1"), "secret_key": SECRET_KEY}


def render_greeting(template: str, name: str) -> str:
    return template.replace("{{name}}", name)


if __name__ == "__main__":
    print(get_user("admin"))
    print(hash_password("password123"))
    print(generate_password_reset_token())
    print(calculate("1 + 1"))
    print(render_greeting("Hello, {{name}}!", "World"))
