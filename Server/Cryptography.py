import base64
from hashlib import sha256

from Cryptodome import Util
from Cryptodome.Cipher import AES


class AESCipher(object):
    def __init__(self):
        self.iv = '0' * 16
        self.mode = AES.MODE_CBC
        self.p = Util.number.getPrime(1024)
        self.g = Util.number.getPrime(1024)
        self.server_private_key = Util.number.getRandomInteger(1024)
        self.server_public_key = pow(self.g, self.server_private_key, self.p)
        self.shared_key = None
        self.local_sha256 = sha256()

    def get_dh_params(self):
        return {
            "p": str(self.p),
            "g": str(self.g),
            "public_key": str(self.server_public_key)
        }

    def calc_shared_key(self, client_public_key_bytes):
        client_public_key = int(client_public_key_bytes)
        num = pow(client_public_key, self.server_private_key, self.p)
        # self.local_sha256.update(str(num).encode('utf-8'))
        # self.shared_key = self.local_sha256.digest()
        self.shared_key = str(num)[0: 32].encode("utf-8")
        return self.shared_key

    def encrypt_data(self, data):
        cryptor = AES.new(self.shared_key, self.mode, self.iv.encode("utf8"))
        self.ciphertext = cryptor.encrypt(self.pad(data))
        return base64.b64encode(self.ciphertext)

    def decrypt_data(self, data):
        decode = base64.b64decode(data)
        cryptor = AES.new(self.shared_key, self.mode, self.iv.encode("utf8"))
        plain_text = cryptor.decrypt(decode)
        return self.unpad(plain_text)

    def pad(self, s):
        l = AES.block_size
        return s + (l - len(s) % l) * chr(l - len(s) % l).encode("utf-8")

    def unpad(self, s):
        return s[0:-ord(s[-1:])]
