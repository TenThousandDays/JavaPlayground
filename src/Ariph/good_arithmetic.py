from dataclasses import dataclass
from tqdm import tqdm

CODE_VALUE_BITS = 16
TOP_VALUE = (1 << CODE_VALUE_BITS) - 1

FIRST_QTR = TOP_VALUE // 4 + 1
HALF = FIRST_QTR * 2
THIRD_QTR = FIRST_QTR * 3

NO_OF_CHARS = 256
EOF_SYMBOL = NO_OF_CHARS
NO_OF_SYMBOLS = NO_OF_CHARS + 1

char_to_index = [0] * NO_OF_CHARS
index_to_char = [0] * (NO_OF_SYMBOLS + 1)

MAX_FREQ = 16383

cum_freq = [0] * (NO_OF_SYMBOLS + 1)
freq = [0] * (NO_OF_SYMBOLS + 1)


@dataclass
class Encoder:
    low: int = 0
    high: int = TOP_VALUE
    bits_to_follow: int = 0

    buffer: int = 0
    bits_to_go: int = 0

    result: bytes = b""

    def start_encoding(self):
        self.low = 0
        self.high = TOP_VALUE
        self.bits_to_follow = 0

    def encode_symbol(self, symbol: int):
        _range = self.high - self.low + 1
        self.high = self.low + (_range * cum_freq[symbol - 1]) // cum_freq[0] - 1
        self.low = self.low + (_range * cum_freq[symbol]) // cum_freq[0]
        while True:
            if self.high < HALF:
                self.bit_plus_follow(0)
            elif self.low >= HALF:
                self.bit_plus_follow(1)
                self.low -= HALF
                self.high -= HALF
            elif self.low >= FIRST_QTR and self.high < THIRD_QTR:
                self.bits_to_follow += 1
                self.low -= FIRST_QTR
                self.high -= FIRST_QTR
            else:
                break
            self.low = 2 * self.low
            self.high = 2 * self.high + 1

    def done_encoding(self):
        self.bits_to_follow += 1
        if self.low < FIRST_QTR:
            self.bit_plus_follow(0)
        else:
            self.bit_plus_follow(1)

    def bit_plus_follow(self, bit: int):
        self.output_bit(bit)
        while self.bits_to_follow > 0:
            if bit > 0:
                self.output_bit(0)
            else:
                self.output_bit(1)
            self.bits_to_follow -= 1

    def start_output_bits(self):
        self.buffer = 0
        self.bits_to_go = 8

    def output_bit(self, bit: int):
        self.buffer >>= 1
        if bit > 0:
            self.buffer |= 0x80
        self.bits_to_go -= 1
        if self.bits_to_go == 0:
            self.result += self.buffer.to_bytes(1, "little", signed=False)
            self.bits_to_go = 8

    def done_output_bits(self):
        self.result += (self.buffer >> self.bits_to_go).to_bytes(1, "little", signed=False)

    @staticmethod
    def start_model():
        for i in range(0, NO_OF_CHARS):
            char_to_index[i] = i + 1
            index_to_char[i+1] = i
        for i in range(0, NO_OF_SYMBOLS + 1):
            freq[i] = 1
            cum_freq[i] = NO_OF_SYMBOLS - i
        freq[0] = 0

    @staticmethod
    def update_model(symbol: int):
        if cum_freq[0] == MAX_FREQ:
            cum = 0
            for i in range(NO_OF_SYMBOLS, -1, -1):
                freq[i] = (freq[i] + 1) // 2
                cum_freq[i] = cum
                cum += freq[i]
        i = symbol
        while freq[i] == freq[i-1]:
            i -= 1
        if i < symbol:
            ch_i = index_to_char[i]
            ch_symbol = index_to_char[symbol]
            index_to_char[i] = ch_symbol
            index_to_char[symbol] = ch_i
            char_to_index[ch_i] = symbol
            char_to_index[ch_symbol] = i
        freq[i] += 1
        while i > 0:
            i -= 1
            cum_freq[i] += 1

    def encode(self, text: bytes):
        self.start_model()
        self.start_output_bits()
        self.start_encoding()
        for c in tqdm(text, ncols=80, desc="Encoding"):
            symbol = char_to_index[c]
            self.encode_symbol(symbol)
            self.update_model(symbol)
        self.encode_symbol(EOF_SYMBOL)
        self.done_encoding()
        self.done_output_bits()


@dataclass
class Decoder:
    value: int = 0
    low: int = 0
    high: int = TOP_VALUE
    bits_to_follow: int = 0

    buffer: int = 0
    bits_to_go: int = 0
    garbage_bits: int = 0

    enc_text: bytes = b""
    i: int = 0
    result: bytes = b""

    @staticmethod
    def start_model():
        for i in range(0, NO_OF_CHARS):
            char_to_index[i] = i + 1
            index_to_char[i + 1] = i
        for i in range(0, NO_OF_SYMBOLS + 1):
            freq[i] = 1
            cum_freq[i] = NO_OF_SYMBOLS - i
        freq[0] = 0

    @staticmethod
    def update_model(symbol: int):
        if cum_freq[0] == MAX_FREQ:
            cum = 0
            for i in range(NO_OF_SYMBOLS, -1, -1):
                freq[i] = (freq[i] + 1) // 2
                cum_freq[i] = cum
                cum += freq[i]
        i = symbol
        while freq[i] == freq[i - 1]:
            i -= 1
        if i < symbol:
            ch_i = index_to_char[i]
            ch_symbol = index_to_char[symbol]
            index_to_char[i] = ch_symbol
            index_to_char[symbol] = ch_i
            char_to_index[ch_i] = symbol
            char_to_index[ch_symbol] = i
        freq[i] += 1
        while i > 0:
            i -= 1
            cum_freq[i] += 1

    def start_input_bits(self):
        self.bits_to_go = 0
        self.garbage_bits = 0

    def input_bit(self) -> int:
        if self.bits_to_go == 0:
            try:
                self.buffer = self.enc_text[self.i]
                self.i += 1
            except IndexError:
                self.garbage_bits += 1
                if self.garbage_bits > CODE_VALUE_BITS - 2:
                    print("Bad input data")
                    exit(-1)
            self.bits_to_go = 8
        t = self.buffer & 1
        self.buffer >>= 1
        self.bits_to_go -= 1
        return t

    def start_decoding(self):
        self.value = 0
        for i in range(1, CODE_VALUE_BITS + 1):
            self.value = 2 * self.value + self.input_bit()
        self.low = 0
        self.high = TOP_VALUE

    def decode_symbol(self) -> int:
        _range = self.high - self.low + 1
        cum = ((self.value - self.low + 1) * cum_freq[0] - 1) // _range
        symbol = 1
        while cum_freq[symbol] > cum:
            symbol += 1
        self.high = self.low + (_range * cum_freq[symbol-1]) // cum_freq[0] - 1
        self.low = self.low + (_range * cum_freq[symbol]) // cum_freq[0]
        while True:
            if self.high < HALF:
                pass
            elif self.low >= HALF:
                self.value -= HALF
                self.low -= HALF
                self.high -= HALF
            elif self.low >= FIRST_QTR and self.high < THIRD_QTR:
                self.value -= FIRST_QTR
                self.low -= FIRST_QTR
                self.high -= FIRST_QTR
            else:
                break
            self.low = 2 * self.low
            self.high = 2 * self.high + 1
            self.value = 2 * self.value + self.input_bit()
        return symbol

    def decode(self, enc_text: bytes):
        total = len(enc_text)
        self.enc_text = enc_text
        self.start_model()
        self.start_input_bits()
        self.start_decoding()
        while True:
            print(f"Decoding: [{self.i}/{total}]", end="\r")
            symbol = self.decode_symbol()
            if symbol == EOF_SYMBOL:
                break
            ch = index_to_char[symbol]
            self.result += ch.to_bytes(1, "little", signed=False)
            self.update_model(symbol)


def readFromFile(filename: str) -> str:
    with open(filename, 'r', encoding="utf-8") as f:
        return f.read()


def writeToFile(filename: str, data: str) -> None:
    with open(filename, 'w', encoding="utf-8") as f:
        f.write(data)


def readBytesFromFile(filename: str) -> bytes:
    with open(filename, 'rb') as f:
        return f.read()


def writeBytesToFile(filename: str, data: bytes) -> None:
    with open(filename, 'wb') as f:
        f.write(data)


if __name__ == "__main__":
    filename = "book1.txt"
    base_fn = filename.split(".")[0]

    text = readBytesFromFile(filename)

    enc = Encoder()
    enc.encode(text)
    writeBytesToFile(f"{base_fn}.ariph", enc.result)
    print(f"Encoded data saved to: {base_fn}.ariph")

    dec = Decoder()
    encoded_bytes = readBytesFromFile(f"{base_fn}.ariph")
    dec.decode(encoded_bytes)
    writeBytesToFile(f"{base_fn}.ariph.decoded", dec.result)
    print(f"\nDecoded data saved to: {base_fn}.ariph.decoded")