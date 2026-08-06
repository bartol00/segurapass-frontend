#!/usr/bin/env python3

import base64
import hashlib
import json
import sys
from collections import OrderedDict
from datetime import datetime, timezone
from pathlib import Path

import requests

from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric.ed25519 import (
    Ed25519PrivateKey,
    Ed25519PublicKey,
)
from cryptography.exceptions import InvalidSignature

from upload import upload_file


# --------------------------------------------------
# CONFIG
# --------------------------------------------------

VERSION = sys.argv[1]
PROTOCOL = int(sys.argv[2])
INSTALLER = Path(sys.argv[3])

PRIVATE_KEY = Path("keys/update-signing-private.pem")
PUBLIC_KEY = Path("keys/update-signing-public.pem")

METADATA = Path("Output/metadata.json")
METADATA_SIG = Path("Output/metadata.sig")

VERSIONS = Path("Output/versions.json")
VERSIONS_SIG = Path("Output/versions.sig")

VERSIONS_URL = (
    f"https://downloads.segurapass.xyz/protocols/{PROTOCOL}/versions.json"
)

# --------------------------------------------------
# SHA256
# --------------------------------------------------

sha256 = hashlib.sha256(INSTALLER.read_bytes()).hexdigest()

print("SHA256:", sha256)

# --------------------------------------------------
# metadata.json
# --------------------------------------------------

metadata = OrderedDict()

metadata["appVersion"] = VERSION
metadata["protocolVersion"] = PROTOCOL
metadata["sha256"] = sha256
metadata["releaseDate"] = (
    datetime.now(timezone.utc)
    .replace(microsecond=0)
    .isoformat()
    .replace("+00:00", "Z")
)

with open(METADATA, "w", encoding="utf-8", newline="\n") as f:
    json.dump(metadata, f, indent=2)
    f.write("\n")

print("Generated metadata.json")

# --------------------------------------------------
# versions.json
# --------------------------------------------------

try:
    response = requests.get(VERSIONS_URL, timeout=10)

    if response.status_code == 200:
        versions = response.json()
        print("Downloaded existing versions.json")
    else:
        raise Exception()
except Exception:

    versions = OrderedDict()

    versions["latestVersion"] = VERSION
    versions["protocolVersion"] = PROTOCOL
    versions["versions"] = []

    print("Creating new versions.json")

versions["latestVersion"] = VERSION

versions["versions"] = [
    v
    for v in versions["versions"]
    if v["appVersion"] != VERSION
]

versions["versions"].insert(0, metadata)

with open(VERSIONS, "w", encoding="utf-8", newline="\n") as f:
    json.dump(versions, f, indent=2)
    f.write("\n")

print("Generated versions.json")

# --------------------------------------------------
# SIGNING
# --------------------------------------------------

private_key = serialization.load_pem_private_key(
    PRIVATE_KEY.read_bytes(),
    password=None,
)
public_key = serialization.load_pem_public_key(
    PUBLIC_KEY.read_bytes()
)

assert isinstance(private_key, Ed25519PrivateKey)
assert isinstance(public_key, Ed25519PublicKey)

for src, dst in [
    (METADATA, METADATA_SIG),
    (VERSIONS, VERSIONS_SIG),
]:
    payload = src.read_bytes()
    signature = private_key.sign(payload)

    try:
        public_key.verify(signature, payload)
    except InvalidSignature:
        raise RuntimeError(f"Signature verification failed for {src.name}")
    
    dst.write_bytes(signature)
    print(f"Signed {src.name}")

print("Generated signatures")

upload_file(
    "Output/SeguraPass-Setup.exe",
    f"protocols/{PROTOCOL}/{VERSION}/SeguraPass-Setup.exe"
)
upload_file(
    "Output/metadata.json",
    f"protocols/{PROTOCOL}/{VERSION}/metadata.json"
)
upload_file(
    "Output/metadata.sig",
    f"protocols/{PROTOCOL}/{VERSION}/metadata.sig"
)
upload_file(
    "Output/versions.json",
    f"protocols/{PROTOCOL}/versions.json"
)
upload_file(
    "Output/versions.sig",
    f"protocols/{PROTOCOL}/versions.sig"
)

print("Uploaded all files")
