package aminecraftplugin.aminecraftplugin.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

import static java.lang.Math.pow;

public class Compress {


    public static DecimalFormat df = new DecimalFormat("#");

    public static String returnCompressed(Double number, int decimals){
        ArrayList<String> symbols = new ArrayList<>();
        Integer element = -1;
        symbols.add("K"); symbols.add("M");  symbols.add("B"); symbols.add("T"); symbols.add("Qa"); symbols.add("Qi");  symbols.add("Sx");  symbols.add("Sp");  symbols.add("Oc");  symbols.add("No");  symbols.add("Dc");
        while (number >= 1000) {
            number = number / 1000;
            element++;
        }
        String pattern = "#.";
        for (int i = 0; i < decimals; i++){
            pattern += "#";
        }
        if (element >= 0){
            DecimalFormat df2 = new DecimalFormat(pattern);
            df2.setRoundingMode(RoundingMode.FLOOR);
            return df2.format(number) + symbols.get(element);
        }
        DecimalFormat df3 = new DecimalFormat(pattern);
        df3.setRoundingMode(RoundingMode.FLOOR);
        return df3.format(number);
    }

    public static Double returnDecompressed(String s){

        ArrayList<String> symbols = new ArrayList<>();
        symbols.add("K"); symbols.add("M");  symbols.add("B"); symbols.add("T"); symbols.add("Qa"); symbols.add("Qi");  symbols.add("Sx");  symbols.add("Sp");  symbols.add("Oc");  symbols.add("No");  symbols.add("Dc");

        String value = s;
        for (int i = 0; i < symbols.size(); i++){
            String symbol = symbols.get(i);
            value = value.replaceAll(symbol,"");
            value = value.replaceAll(symbol.toLowerCase(Locale.ROOT), "");
        }

        Double num = Double.valueOf(value);

        for (int i = 0; i < symbols.size(); i++){
            String symbol = symbols.get(i);
            if (s.contains(symbol) || s.contains(symbol.toLowerCase(Locale.ROOT))){
                num *= pow(10.0, (i + 1) * 3.0);
            }
        }
        return num;
    }
}
