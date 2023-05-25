from array import array
from datetime import datetime


def clean(text):
    x = text
    for i in "\n\t\r":
        x = x.replace(i, "")
    return x


def get_key(chs):
    key = ""
    ch = chs.get_ch()
    if ch != '"':
        raise ValueError(f'{ch} is not start of key "')
    while True:
        ch = chs.get_ch()
        if ch != '"':
            key += ch
        else:
            break
    if not key:
        raise ValueError(f"{ch} empty key")
    ch = chs.get_ch()
    if ch == ":":
        return key, "value"
    else:
        raise ValueError(f'{ch} " in key')


def get_value(chs):
    start = False
    value = ""
    while True:
        ch = chs.get_ch()
        if ch == '"' and not start:
            start = True
            continue
        elif ch == '"' and start:
            new_ch = chs.get_ch()
            if new_ch == ",":
                return value, "key"
            elif new_ch == "}":
                return value, "end"
            else:
                value += ch + new_ch
        elif ch == "{" and not start:
            chs.ins()
            value = get_object(chs)
            new_ch = chs.get_ch()
            if new_ch == ",":
                return value, "key"
            elif new_ch == "}":
                return value, "end"
            else:
                raise ValueError("some shit after object")
        else:
            value += ch


def get_object(chs):
    step = "key"
    ch = chs.get_ch()
    if ch != "{":
        raise ValueError("not object")
    out = {"nodes": []}
    while step != "end":
        if step == "key":
            key, step = get_key(chs)
        elif step == "value":
            value, step = get_value(chs)
            if key == "node":
                out["nodes"].append(value)
            else:
                out[key] = value
    return out


class Chars:
    def __init__(self, chars):
        self.counter = 0
        self.chs = array("u", chars)
        self.end = len(self.chs)

    def get_ch(self):
        if self.counter < self.end:
            ch = self.chs[self.counter]
            self.counter += 1
            return ch

    def ins(self):
        self.counter -= 1


def main(path):
    result = None
    with open(path, "r", encoding="cp1251") as file:
        try:
            data = file.read()
            text = clean(data)
            chs = Chars(text)
            obj = get_object(chs)
            result = obj["nodes"]
        except Exception as e:
            print(e)
    return result


if __name__ == "__main__":
    path = "./sdv/Device2.sdv"
    file = open(path, "r", encoding="cp1251")
    try:
        data = file.read()
        data.encode("utf8")

        text = clean(data)

        start = datetime.now()
        for i in range(10):
            chs = Chars(text)
            obj = get_object(chs)
        end = datetime.now()
        interval = (end - start) / 10
        print(interval)

    except Exception as e:
        print(e)
    finally:
        file.close()
