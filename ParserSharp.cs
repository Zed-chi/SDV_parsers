using System;
using System.IO;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Timers;

namespace ConsoleApplication3
{
    class Program
    {
        
        static void Main(string[] args)
        {
            String path = "C:\\Users\\Дом\\Desktop\\САУ\\OPC_imp\\sdv\\Device2.sdv";
            StreamReader reader = new StreamReader(path);
            try{
                String text = reader.ReadToEnd();
                String cleaned_text = clean_text(text);
                

                DateTime start = DateTime.Now;
                for (int i = 0; i < 10; i++) {
                    Chars chs = new Chars(cleaned_text);
                    Dictionary<string, List<Result>> obj = get_object(chs);                
                }
                
                DateTime end = DateTime.Now;
                TimeSpan dur = end - start;                
                Console.WriteLine(dur);
            } 
            catch(Exception e){
                Console.WriteLine(e);
            }                 
        }

        public static Dictionary<string, List<Result>> get_object(Chars chs)
        {
            String step = "key";
            char ch = (char) chs.get_ch();
            if (ch != '{'){
                throw new Exception("not object");
            }
            var output = new Dictionary<string, List<Result>>();            
            output["nodes"] = new List<Result>();
            
            while(step != "end"){
                String key = "";
                if (step == "key"){
                    var tuple = get_key(chs);
                    key = tuple[0];
                    step = tuple[1];
                } else if (step == "value"){
                    Tuple<Result, string> tuple = get_value(chs);                    
                    Result value = tuple.Item1;
                    step = tuple.Item2;
                    if (key == "node"){                        
                        output["nodes"].Add(value);
                    } else{
                        output[key] = new List<Result>();
                        output[key].Add(value);
                    }
                }        
            }            
            return output;            
        }

        public static List<string> get_key(Chars chs)
        {
            String key = "";
            char ch = (char)chs.get_ch();            
            if (ch != '"')
            {
                throw new Exception("["+ ch + "] is not start of key");
            }

            while (true)
            {                
                ch = (char)chs.get_ch();
                if (ch != '"')
                {
                    key += ch;
                }
                else { break; }
            }

            if (key.Count() == 0){
                throw new Exception("empty key");
            }
            ch = (char)chs.get_ch();
            if (ch == ':'){
                var x = new List<string>();
                x.Add(key);
                x.Add("value");
                return x;
            }
            else{throw new Exception("["+ ch + "] in key");}            
        }


        public static Tuple<Result, string> get_value(Chars chs)
        {          
            bool q = false;
            String Svalue = "";
            Dictionary<string, List<Result>> Dvalue = new Dictionary<string,List<Result>>();
            while (true){
                char ch = (char)chs.get_ch();
                if (ch == '"' && !q){
                    q = true; continue;
                } else if (ch == '"' && q){
                    char newChar = (char)chs.get_ch();
                    if(newChar == ','){
                        return Tuple.Create(new Result(Svalue), "key");
                    }
                    else if (newChar == '}')
                    {
                        if (Svalue.Count() > 0) { return Tuple.Create(new Result(Svalue), "end"); }
                        else {return Tuple.Create(new Result(Dvalue), "end"); }                                                
                    }
                    else { Svalue += ch + newChar; }
                } else if (ch == '{' && !q){
                    chs.ins();
                    Dvalue = get_object(chs);
                    char newChar = (char)chs.get_ch();
                    if (newChar == ',')
                    {
                        if (Svalue.Count() > 0) { return Tuple.Create(new Result(Svalue), "key"); }
                        else { return Tuple.Create(new Result(Dvalue), "key"); } 
                    }
                    else if (newChar == '}')
                    {
                        if (Svalue.Count() > 0) { return Tuple.Create(new Result(Svalue), "end"); }
                        else { return Tuple.Create(new Result(Dvalue), "end"); } 
                    }
                    else {
                        continue;
                        throw new Exception("some shit after object <" + newChar+">"+Svalue); 
                    }
                }
                else {
                    Svalue += ch;
                }
            }            
        }


        public static String clean_text(String text)
        {
            String data = text.Replace("\n", "");
            data = data.Replace("\t", "");
            data = data.Replace("\r", "");
            return data;
        }
    }


    class Chars {  
        private List<char> _chs;
        private int counter=0;
        private int end=0;

        public Chars(String text){
            _chs = new List<char>(text);
            this.end = this._chs.Count();
        }

        public Nullable<char> get_ch(){
            if (this.counter < this.end) {
                char ch = this._chs[this.counter];
                this.counter += 1;
                return ch;
            }            
            return null;
        }                
        
        public void ins(){
            this.counter -= 1;
        }
    }

    class Result {
        private Dictionary<string, List<Result>> Dict_result;
        private String Str_result;        

        public Result(String data) {
            Str_result = data;
        }

        public Result(Dictionary<string, List<Result>> Dvalue)
        {
            Dict_result = Dvalue;
        }
    }
}
