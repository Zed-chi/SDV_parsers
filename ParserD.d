import std.stdio;
import std.file;
import std.utf;
import std.encoding;
import std.array;
import std.typecons;
import std.datetime.systime;




/* Classes */
class Result {
    Result[] [string] dict_result;
    string str_result;

    this(string data) {
        str_result = data;
    }

    this(Result[][string] Dvalue){
        dict_result = Dvalue;
    }
}


class Chars {
    string chs;
    int counter=0;
    int end;
    this(string data) {
        this.chs = data;
        this.end = cast(int)data.length;
    }

    char get_ch(){
        if (this.counter < this.end){
            auto x = this.chs[this.counter];
            this.counter+=1;
            return x;
        } else {
            return this.chs[this.end - 1];
        }
    }

    void ins(){
        this.counter-=1;
    }
}


/* funcs */
string clean_text(string text){
    string data = text.replace("\n", "");
    data = data.replace("\t", "");
    data = data.replace("\r", "");
    return data;
}


auto get_value(Chars chs){
    bool q = false;
    string Svalue = "";
    Result[] [string] Dvalue;
    while (true){
        char ch = cast(char)chs.get_ch();
        if (ch == '"' && !q){
            q = true; continue;
        } else if (ch == '"' && q){
            char newChar = cast(char)chs.get_ch();
            if(newChar == ','){
                return tuple(new Result(Svalue), "key");
            } else if (newChar == '}'){
                if (Svalue.length > 0) { return tuple(new Result(Svalue), "end"); }
                else {return tuple(new Result(Dvalue), "end");}
            } else { Svalue ~= ch + newChar; }
        } else if (ch == '{' && !q){
            chs.ins();
            Dvalue = get_object(chs);
            char newChar = cast(char)chs.get_ch();
            if (newChar == ','){
                if (Svalue.length > 0) { return tuple(new Result(Svalue), "key"); }
                else { return tuple(new Result(Dvalue), "key"); }
            } else if (newChar == '}'){
                if (Svalue.length > 0) { return tuple(new Result(Svalue), "end"); }
                else { return tuple(new Result(Dvalue), "end"); }
            } else {
                continue;
                //throw new Exception("some shit after object <" ~ newChar~">"~Svalue);
            }
        } else {Svalue ~= ch;}
    }
}


auto get_object(Chars chs){
    string step = "key";
    char ch = cast(char) chs.get_ch();
    if (ch != '{'){throw new Exception("not object");}
    Result[] [string] output = ["nodes":[]];
    while(step != "end"){
        string key = "";
        if (step == "key"){
            auto pair = get_key(chs);
            key = pair[0];
            step = pair[1];
        } else if (step == "value"){
            auto pair = get_value(chs);
            Result value = pair[0];
            step = pair[1];
            if (key == "node"){
                output["nodes"]~=(value);
            } else{
                output[key] ~= value;
            }
        }
    }
    return output;
}


auto get_key(Chars chs){
    string key = "";
    char ch = cast(char)chs.get_ch();
    if (ch != '"'){throw new Exception("["~ ch ~ "] is not start of key");}

    while (true){
        ch = cast(char)chs.get_ch();
        if (ch != '"'){key ~= ch;}
        else {break;}
    }
    if (key.length == 0){throw new Exception("empty key");}
    ch = cast(char)chs.get_ch();
    if (ch == ':'){
        //string[] x;x ~= key;x ~= "value";return x;
        return tuple(key, "value");
    }
    else{throw new Exception("[" ~ ch ~ "] in key");}
}


int main(){
    //string path = "C:\\Users\\Дом\\Desktop\\САУ\\OPC_imp\\sdv\\2.sdv";
    string path = ".\\sdv\\Device2.sdv";

    if (exists(path)) {
        writeln("reading file...");

        Windows1251String ws = cast(Windows1251String)read(path);
        string s;
        transcode(ws, s);
        string cleaned_text = clean_text(s);
        SysTime start = Clock.currTime();
        for (int i=0;i<10;i++){
            Chars chs = new Chars(cleaned_text);    
            auto obj = get_object(chs);
        }
                
        //writeln(chs);
        SysTime finish = Clock.currTime();

        writeln((finish-start)/10);
        //writeln(obj["nodes"].length);
        writeln("done.");
    } else {
        writeln("fuck");
    }

    return 0;
}
