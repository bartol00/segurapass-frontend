import boto3
import os
from pathlib import Path
from dotenv import load_dotenv


class ProgressPercentage:
    def __init__(self, filename):
        self._filename = Path(filename)
        self._size = self._filename.stat().st_size
        self._seen = 0

    def __call__(self, bytes_amount):
        self._seen += bytes_amount
        percentage = (self._seen / self._size) * 100

        print(
            f"\rUploading {self._filename.name}: "
            f"{percentage:6.2f}% "
            f"({self._seen / 1024 / 1024:.1f} / {self._size / 1024 / 1024:.1f} MB)",
            end="",
            flush=True
        )

def upload_file(local_path: str | Path, remote_key: str):
    load_dotenv("keys/upload.env")

    BUCKET = "segurapass"
    ENDPOINT = os.getenv("ENDPOINT")
    ACCESS_KEY = os.getenv("ACCESS_KEY_ID")
    SECRET_KEY = os.getenv("SECRET_ACCESS_KEY")

    s3 = boto3.client(
        "s3",
        endpoint_url=ENDPOINT,
        aws_access_key_id=ACCESS_KEY,
        aws_secret_access_key=SECRET_KEY,
        region_name="auto"
    )
    
    local_path = Path(local_path)

    s3.upload_file(
        str(local_path),
        BUCKET,
        remote_key,
        Callback=ProgressPercentage(local_path)
    )

    print(f"Uploaded {local_path} -> {remote_key}")