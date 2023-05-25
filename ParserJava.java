package Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class App {
    public static String readFile(String path, Charset encoding) throws IOException {
        File file = new File(path);
        InputStream in = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];

        int offset = 0;
        while (offset < bytes.length) {
            int result = in.read(bytes, offset, bytes.length - offset);
            if (result == -1) {
                break;
            }
            offset += result;
        }
        in.close();
        return new String(bytes, encoding);
    }

    public static void main(String[] args) {
        String path = "./Device2.sdv";
        String content = null;
        try {
            content = readFile(path, Charset.forName("Cp1251"));
            String cleaned_text = cleanText(content);

            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                Chars chs = new Chars(cleaned_text);
                HashMap<String, List<Result>> obj = get_object(chs);
            }

            long endTime = System.currentTimeMillis();
            long dur = (endTime - startTime) / 10;
            System.out.println("That took " + dur + " milliseconds");

        } catch (IOException e) {
            System.out.println(e);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

    public static HashMap<String, List<Result>> get_object(Chars chs) throws Exception {
        String step = "key";
        char ch = (char) chs.get_ch();
        if (ch != '{') {
            throw new Exception("not object");
        }
        HashMap<String, List<Result>> output = new HashMap<String, List<Result>>();
        output.put("nodes", new ArrayList<Result>());

        while (step != "end") {
            String key = "";
            if (step == "key") {
                var tuple = get_key(chs);
                key = tuple.get(0);
                step = tuple.get(1);
            } else if (step == "value") {
                Tuple tuple = get_value(chs);
                Result value = tuple.getResult();
                step = tuple.getStr();
                if (key == "node") {
                    output.get("nodes").add(value);
                } else {
                    output.put(key, new ArrayList<Result>());
                    output.get(key).add(value);
                }
            }
        }
        return output;
    }

    public static ArrayList<String> get_key(Chars chs) throws Exception {
        String key = "";
        char ch = (char) chs.get_ch();
        if (ch != '"') {
            throw new Exception("[" + ch + "] is not start of key");
        }

        while (true) {
            ch = (char) chs.get_ch();
            if (ch != '"') {
                key += ch;
            } else {
                break;
            }
        }

        if (key.length() == 0) {
            throw new Exception("empty key");
        }
        ch = (char) chs.get_ch();
        if (ch == ':') {
            var x = new ArrayList<String>();
            x.add(key);
            x.add("value");
            return x;
        } else {
            throw new Exception("[" + ch + "] in key");
        }
    }

    public static Tuple get_value(Chars chs) throws Exception {
        boolean q = false;
        String Svalue = "";
        HashMap<String, List<Result>> Dvalue = new HashMap<String, List<Result>>();
        while (true) {
            char ch = (char) chs.get_ch();
            if (ch == '"' && !q) {
                q = true;
                continue;
            } else if (ch == '"' && q) {
                char newChar = (char) chs.get_ch();
                if (newChar == ',') {
                    return new Tuple(new Result(Svalue), "key");
                } else if (newChar == '}') {
                    if (Svalue.length() > 0) {
                        return new Tuple(new Result(Svalue), "end");
                    } else {
                        return new Tuple(new Result(Dvalue), "end");
                    }
                } else {
                    Svalue += ch + newChar;
                }
            } else if (ch == '{' && !q) {
                chs.ins();
                Dvalue = get_object(chs);
                char newChar = (char) chs.get_ch();
                if (newChar == ',') {
                    if (Svalue.length() > 0) {
                        return new Tuple(new Result(Svalue), "key");
                    } else {
                        return new Tuple(new Result(Dvalue), "key");
                    }
                } else if (newChar == '}') {
                    if (Svalue.length() > 0) {
                        return new Tuple(new Result(Svalue), "end");
                    } else {
                        return new Tuple(new Result(Dvalue), "end");
                    }
                } else {
                    // continue;
                    throw new Exception("some shit after object <" + newChar + ">" + Svalue);
                }
            } else {
                Svalue += ch;
            }
        }
    }

    public static String cleanText(String text) {
        String data = text.replaceAll("\n", "");
        data = data.replaceAll("\t", "");
        data = data.replaceAll("\r", "");
        return data;
    }
}

class Tuple {
    private Result result;
    private String str;

    public Tuple(Result r, String s) {
        result = r;
        str = s;
    }

    public String getStr() {
        return str;
    }

    public Result getResult() {
        return result;
    }

}

class Result {
    private HashMap<String, List<Result>> resultDict;
    private String resultStr;

    public Result(String text) {
        resultStr = text;
    }

    public Result(HashMap<String, List<Result>> Dvalue) {
        resultDict = Dvalue;
    }

    public boolean isStr() {
        return resultStr != null;
    }

    public String getResultStr() {
        return resultStr;
    }

    public HashMap<String, List<Result>> getResultDict() {
        return resultDict;
    }
}

class Chars {
    private String _chs;
    private int counter = 0;
    private int end = 0;

    public Chars(String text) {
        _chs = text;
        end = this._chs.length();
        // System.out.printf("Length %d \n", end);
    }

    public char get_ch() throws Exception {
        if (counter < end) {
            char ch = _chs.charAt(counter);
            this.counter += 1;
            // System.out.printf("Count %d of %d\n", counter, end);
            return ch;
        } else {
            return _chs.charAt(end - 1);
        }
    }

    public void ins() {
        counter -= 1;
    }
}