/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
 * 
 *  This file is part of Rseslib.
 *
 *  Rseslib is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rseslib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package rseslib.structure.attribute.formats.rses;

/**
 * Rough Set Library
 *
 * @author Jan Bazan
 *
 */


import java.util.*;
import java.io.*;
import java.text.*;

public class Element
{
  //Wspolne
  public static final int UN_KNOWN = 0;
  public static final int TABLE = 1;
  public static final int RED_SET = 2;
  public static final int RUL_SET = 3;
  public static final int DECOMP_TREE = 4;



  public static final int ROOT = 11;
  public static final int TEMPL_SET = 12;

  //Dla RSES-a
  public static final int CUT_SET = 21;
  public static final int DIRECTION_SET = 23;
  public static final int LTFC = 24;
  public static final int MTDC = 25;
  public static final int RESULTS = 26;

  public static final int SUBTABLE_STATISTICS = 26;


  int type;
  String name;


  public Element()
  {
    type = UN_KNOWN;
    name = new String("no_name");
  }

  public Element(int newType,String newName)
  {
    type = newType;
    name = new String(newName);
  }

  public void setType(int t)
  {
    type = t;
  }

  public int getType() { return type; }

  public void setName(String n) { name = new String(n); }
  public String getName() { return name;}

  static public String convertCodeToText(int code)
  {
    String TEXT = null;
    switch(code)
    {
      case Element.TABLE:  TEXT = new String("TABLE"); break;
      case Element.RED_SET: TEXT = new String("REDUCT_SET"); break;
      case Element.RUL_SET: TEXT = new String("RULE_SET"); break;
      case Element.CUT_SET: TEXT = new String("CUT_SET"); break;
      case Element.DIRECTION_SET: TEXT = new String("DIRECTION_SET"); break;
      case Element.RESULTS: TEXT = new String("TEST_RESULTS"); break;
      case Element.DECOMP_TREE: TEXT = new String("DECOMPOSITION_TREE"); break;
      case Element.LTFC: TEXT = new String("LTF_CLASSIFIER"); break;
      case Element.MTDC: TEXT = new String("MTD_CLASSIFIER"); break;
      default: TEXT = new String("Unknown code of element: "+code);
    }
    return TEXT;
  }

  static public int convertTextToCode(String TYPE)
  {
    if (TYPE.compareTo("TABLE")==0) { return Element.TABLE; }
    else
     if (TYPE.compareTo("REDUCT_SET")==0) { return Element.RED_SET; }
     else
      if (TYPE.compareTo("RULE_SET")==0) { return Element.RUL_SET; }
      else
       if (TYPE.compareTo("CUT_SET")==0) { return Element.CUT_SET; }
       else
        if (TYPE.compareTo("DIRECTION_SET")==0) { return Element.DIRECTION_SET; }
	else
	 if (TYPE.compareTo("TEST_RESULTS")==0) { return Element.RESULTS; }
	 else
	  if (TYPE.compareTo("DECOMPOSITION_TREE")==0) { return Element.DECOMP_TREE; }
          else
           if (TYPE.compareTo("LTF_CLASSIFIER")==0) { return Element.LTFC; }
           else
             if (TYPE.compareTo("MTD_CLASSIFIER")==0) { return Element.MTDC; }
      	     else return Integer.MAX_VALUE;

  }
  //==================================

  RSLibProgress rsLibProgress = null;

  public void setMessageOutput(RSLibProgress p) { rsLibProgress = p; };
  public RSLibProgress getMessageOutput() { return rsLibProgress; };


  public boolean getTaskTerminatedInfo()
  {
    if (rsLibProgress!=null)
    {
      return rsLibProgress.getTaskTerminated();
    }
    else return false;
  }

  public void progress(int p) throws InterruptedException
  {
    if (rsLibProgress!=null)
    {
      rsLibProgress.progress(p);
    }
  }

  public void progress(String s)
  {
    if (rsLibProgress!=null)
    {
      rsLibProgress.progress(s);
    }
  }


  public void messageL(String m)
  {
    if (rsLibProgress!=null)
    {
      rsLibProgress.message(m);
    }
    else
    {
      if (RSLibProgress.getMessageLevel()>0) System.out.println(m);
    }
  }

  public void write(String m)
  {
    if (rsLibProgress!=null)
    {
      rsLibProgress.write(m);
    }
    else
    {
      if (RSLibProgress.getMessageLevel()>0) System.out.print(m);
    }
  }

  public void writeln(String m)
  {
    if (rsLibProgress!=null)
    {
      rsLibProgress.writeln(m);
    }
    else
    {
      if (RSLibProgress.getMessageLevel()>0) System.out.println(m);
    }

  }

  //====== ZAPIS I ODCZYT =======================================

  public void saveTypeAndNameToFile(PrintWriter pw) throws IOException
  {
    String TYPE  = Element.convertCodeToText(type);
    pw.println(TYPE + " " + Element.addQuotationMarks(getName()));
  }

  public static String readLine(BufferedReader br,IntWrap LINE_NUMBER)
  throws IOException
  {
      while (true)
      {
        String line = br.readLine();

        if (LINE_NUMBER!=null) LINE_NUMBER.incValue();

        if (line==null) return null;

        StringTokenizer st = new StringTokenizer(line);

        if (st.hasMoreTokens())
        {
          String token = st.nextToken(normalDelim);

          if ((token.charAt(0)!='#')&&(token.charAt(0)!='%'))
          {
            return line;
          }
        }
      }
  }

  public static String readNextLine(BufferedReader br,IntWrap LINE_NUMBER)
  throws IOException
  {
     return readNextLine(br,LINE_NUMBER,false);
  }


  public static String readNextLine(BufferedReader br,
                                    IntWrap LINE_NUMBER,
                                    boolean isPossibleNullLine)
  throws IOException
  {
    while (true)
    {
      String line = br.readLine();

      if (LINE_NUMBER!=null) LINE_NUMBER.incValue();

      if (line==null)
      {
        if (isPossibleNullLine) return null; //dopuszczamy, ze jest koniec pliku
        else
        {
            if (LINE_NUMBER!=null) throw new IOException("Unexpected end of file in line: "+LINE_NUMBER.getValue());
            else throw new IOException("Unexpected end of file!");
        }
      }

      StringTokenizer st = new StringTokenizer(line,normalDelim);

      if (st.hasMoreTokens())
      {
        String token = st.nextToken(normalDelim);

        if ((token.charAt(0)!='#')&&(token.charAt(0)!='$')&&(token.charAt(0)!='%')) //Trzy znaki komentarza: #, $, %
        {
          return line;
        }
      }
    }
  }

  static String normalDelim = new String(" ,\t\n\r\f");
  static String marksDelim = new String("\"'\n\r\f");


  public static String readNextToken(StringTokenizer st,
                                     IntWrap LINE_NUMBER)
  throws IOException
  {
     return readNextToken(st,LINE_NUMBER,null);
  }


  public static String readNextToken(StringTokenizer st,
                                     IntWrap LINE_NUMBER,
                                     String extNormalDelim)

    throws IOException
    {
      if (!st.hasMoreTokens())
      {
        if (LINE_NUMBER!=null)
          throw new IOException("Unexpected end of line: "+LINE_NUMBER.getValue());
        else
          throw new IOException("Unexpected end of line!");
      }

      String currentDelim = normalDelim;
      if (extNormalDelim!=null) currentDelim = extNormalDelim;

      String token1 = st.nextToken(currentDelim);

      if (
          ((token1.charAt(0)=='\"')||(token1.charAt(0)=='\'')) &&
           (

              ((token1.charAt(token1.length()-1)!='\"')&&(token1.charAt(token1.length()-1)!='\''))||
              (token1.length()==1)
            )
          )
      {
          token1 = new String(token1.substring(1,token1.length()));

          if (!st.hasMoreTokens())
          {
            if (LINE_NUMBER!=null)
              throw new IOException("Unexpected end of line: "+LINE_NUMBER.getValue());
            else
              throw new IOException("Unexpected end of line!");
          }

          String token2 = st.nextToken(marksDelim);
          st.nextToken(currentDelim);
          token1 = token1 + token2;
      }

      token1 = Element.addQuotationMarks(token1);

      return token1;

  }


/*
  public static String readNextTokenWithDelimAsToken(StringTokenizer st,
                                                     IntWrap LINE_NUMBER,
                                                     String extNormalDelim)
    throws IOException
    {

      if (!st.hasMoreTokens())
      {
        if (LINE_NUMBER!=null)
          throw new IOException("Unexpected end of line: "+LINE_NUMBER.getValue());
        else
          throw new IOException("Unexpected end of line!");
      }

      String currentDelim = normalDelim;
      if (extNormalDelim!=null) currentDelim = extNormalDelim;
      currentDelim = addQuotationMarksAsFirst(currentDelim);

      String token1 = st.nextToken(currentDelim);

      if ((token1.equals("\""))||(token1.equals("'")))
      {
        String token2 = st.nextToken(marksDelim);
        token1 = token2;

        String tokenSecondMarks = st.nextToken(currentDelim);
        if ((!tokenSecondMarks.equals("\""))&&(!tokenSecondMarks.equals("'")))
        {
          if (LINE_NUMBER!=null)
            throw new IOException("Expected \" or ' in line"+LINE_NUMBER.getValue());
          else
            throw new IOException("Expected \" or '!");
        }
      }
      else st.nextToken(currentDelim);

      System.out.println("tokenB: <"+token1+">");

      return token1;

  }
  */

/*
  public static String readNextToken(StringTokenizer st,
                                     IntWrap LINE_NUMBER,
                                     boolean possibleMarks,
                                     String extNormalDelim)
  throws IOException
  {

    if (!st.hasMoreTokens())
    {
      if (LINE_NUMBER!=null)
        throw new IOException("Unexpected end of line: "+LINE_NUMBER.getValue());
      else
      	throw new IOException("Unexpected end of line!");
    }

    String currentDelim = normalDelim;
    if (extNormalDelim!=null) currentDelim = extNormalDelim;

    String token = null;

    if (!possibleMarks) //nie spodziewamy sie cudzyslowow
    {
      token = st.nextToken(currentDelim);
      System.out.println("token1: <"+token+">");
      return token;
    }
    else //spodziewamy sie cudzyslowu lub apostrofu
    {
      currentDelim = addQuotationMarksAsFirst(currentDelim);
      token = st.nextToken(currentDelim);

              System.out.println("    token3: <"+token+">");

      if ((token.equals("\""))||(token.equals("'")))
      {
        token = st.nextToken(marksDelim);
        if ((token.charAt(token.length()-1)!='\"')&&(token.charAt(token.length()-1)!='\''))
        {
          if (LINE_NUMBER!=null)
            throw new IOException("Expected \" or ' in line"+LINE_NUMBER.getValue());
          else
            throw new IOException("Expected \" or '!");
        }

        String tokenSecondMarks = st.nextToken(currentDelim);
        if ((!token.equals("\""))&&(!token.equals("'")))
        {
          if (LINE_NUMBER!=null)
            throw new IOException("Expected \" or ' in line"+LINE_NUMBER.getValue());
          else
            throw new IOException("Expected \" or '!");
        }

        token = new String(token.substring(0,token.length()-1));
      }
      System.out.println("token2: <"+token+">");
      return token;
    }
  }

*/

  public void loadTypeAndNameFromFile(BufferedReader br,
                                      IntWrap LINE_NUMBER,
				      int EXPECTED_CODE)
  throws IOException
  {
//    String line = br.readLine();
    String line = readNextLine(br,LINE_NUMBER);
    StringTokenizer st = new StringTokenizer(line);

    String TYPE = readNextToken(st,null);
    int code = Element.convertTextToCode(TYPE);

    if (code!=EXPECTED_CODE)
    {
      String EXPECTED_TYPE = Element.convertCodeToText(EXPECTED_CODE);
      throw new IOException("Expected word '"+EXPECTED_TYPE+"' in line "+LINE_NUMBER.getValue());
    }

    type = code;

    String NAME = readNextToken(st,null);

    if (NAME.length()<1)
    {
      throw new IOException("The name of element is empty!");
    }

    setName(NAME);

  }


  //===== OBSLUGA SLOWNIKA GLOBALNEGO =============================

  private static RSLibDictio globalDictio; //referencja do slownika globalnego

  static //blok tworzacy slownik globalny
  {
      globalDictio = new RSLibDictio();
  }

  public static RSLibDictio getGlobalDictio(){ return globalDictio; };

  static public int addWordToDictio(String word)
  {
    return globalDictio.addWord(word);
  }

  static public int getKeyForWord(String word)
  {
    return globalDictio.getKey(word);
  }

  static public String getWordFromDictio(int key)
  {
    return globalDictio.getWord(key);
  }

  static public int getNoWordInDictio()
  {
    return globalDictio.size();
  }

  static public void saveGlobalDictio(String fName) throws IOException
  {
    globalDictio.saveDictio(fName);
  }

  //operacje czytania pojedynczych wartosci z pilku

  static public String loadStringFromFile(BufferedReader br) throws IOException
  {
    String line = br.readLine();
    StringTokenizer st = new StringTokenizer(line);

    String text = new String();

    while (st.hasMoreTokens())
    {
      text = text + st.nextToken();
    }

    return text;
  }


  static public int loadIntFromFile(BufferedReader br,String localization) throws IOException
  {
    String line = br.readLine();
    StringTokenizer st = new StringTokenizer(line);
    String stringVal = st.nextToken();
    int intVal = 0;
    try
    {
      intVal = Integer.parseInt(stringVal);
    }
    catch (NumberFormatException e)
    {
      System.out.println("Loaded: "+stringVal);
      throw new IOException("Format error with integer value, variable: "+localization);
    }
    return intVal;
  }

  static public float loadFloatFromFile(BufferedReader br,String localization) throws IOException
  {
    String line = br.readLine();
    StringTokenizer st = new StringTokenizer(line);
    String stringVal = st.nextToken();
    float floatVal = 0;
    try
    {
      floatVal = Float.parseFloat(stringVal);
    }
    catch (NumberFormatException e)
    {
      System.out.println("Loaded: "+stringVal);
      throw new IOException("Format error with float value, variable: "+localization);
    }
    return floatVal;
  }

  static public double loadDoubleFromFile(BufferedReader br,String localization) throws IOException
  {
    String line = br.readLine();
    StringTokenizer st = new StringTokenizer(line);
    String stringVal = st.nextToken();
    double doubleVal = 0;
    try
    {
      doubleVal = Double.parseDouble(stringVal);
    }
    catch (NumberFormatException e)
    {
      System.out.println("Loaded: "+stringVal);
      throw new IOException("Format error with double value, variable: "+localization);
    }
    return doubleVal;
  }

  static public boolean loadBooleanFromFile(BufferedReader br,String localization) throws IOException
  {
    String line = br.readLine();
    StringTokenizer st = new StringTokenizer(line);
    String stringVal = st.nextToken();
    boolean booleanVal = true;
    try
    {
      booleanVal = Boolean.valueOf(stringVal).booleanValue();
    }
    catch (NumberFormatException e)
    {
      System.out.println("Loaded: "+stringVal);
      throw new IOException("Format error with boolean value, variable: "+localization);
    }
    return booleanVal;
  }

  //operacje zapisu i czytania tablic z pilku

  //====== 1-wymiarowa INT ===================

  static public void saveIntArrayToFile(int [] tab,
                                        PrintWriter pw)
  throws IOException
  {
    int noElem = tab.length;

    pw.println(noElem);

    for (int i=0; i<noElem; i++)
    {
      pw.println(tab[i]);
    }
  }


  static public int [] loadIntArrayFromFile(BufferedReader br,
                                            String localization)
  throws IOException
  {
    int noElem = loadIntFromFile(br,localization);

    int [] tab = new int [noElem];

    for (int i=0; i<noElem; i++)
    {
      int val = loadIntFromFile(br,localization);
      tab[i] = val;
    }

    return tab;
  }


  //====== 1-wymiarowa DOUBLE ===================

  static public void saveDoubleArrayToFile(double [] tab,
                                           PrintWriter pw)
  throws IOException
  {
    if (tab==null)
    {
      pw.println(0);
    }
    else
    {
      int noElem = tab.length;

      pw.println(noElem);

      for (int i=0; i<noElem; i++)
      {
        pw.println(tab[i]);
      }
    }
  }


  static public double [] loadDoubleArrayFromFile(BufferedReader br,
                                                  String localization)
  throws IOException
  {
    int noElem = loadIntFromFile(br,localization);

    if (noElem==0) return null;

    double [] tab = new double [noElem];

    for (int i=0; i<noElem; i++)
    {
      double val = loadDoubleFromFile(br,localization);
      tab[i] = val;
    }

    return tab;
  }

  //====== 2-wymiarowa INT ===================

  static public void saveInt2DArrayToFile(int [][] tab2D,
                                          PrintWriter pw)
  throws IOException
  {
    int noRow = tab2D.length;
    int noCol = tab2D[0].length;

    pw.println(noRow);
    pw.println(noCol);

    for (int i=0; i<noRow; i++)
    {
      for (int j=0; j<noCol; j++)
      {
        pw.println(tab2D[i][j]);
      }
    }
  }


  static public int [][] loadInt2DArrayFromFile(BufferedReader br,
                                                String localization)
  throws IOException
  {
    int noRow = loadIntFromFile(br,localization);
    int noCol = loadIntFromFile(br,localization);

    int [][] tab = new int [noRow][noCol];

    for (int i=0; i<noRow; i++)
    {
      for (int j=0; j<noCol; j++)
      {
        int val = loadIntFromFile(br,localization);
        tab[i][j] = val;
      }
    }
    return tab;
  }

  //====== 2-wymiarowa DOUBLE ===================

  static public void saveDouble2DArrayToFile(double [][] tab2D,
                                             PrintWriter pw)
  throws IOException
  {
    int noRow = tab2D.length;
    int noCol = tab2D[0].length;

    pw.println(noRow);
    pw.println(noCol);

    for (int i=0; i<noRow; i++)
    {
      for (int j=0; j<noCol; j++)
      {
        pw.println(tab2D[i][j]);
      }
    }
  }


  static public double [][] loadDouble2DArrayFromFile(BufferedReader br,
                                                      String localization)
  throws IOException
  {
    int noRow = loadIntFromFile(br,localization);
    int noCol = loadIntFromFile(br,localization);

    double [][] tab = new double [noRow][noCol];

    for (int i=0; i<noRow; i++)
    {
      for (int j=0; j<noCol; j++)
      {
        double val = loadDoubleFromFile(br,localization);
        tab[i][j] = val;
      }
    }
    return tab;
  }


  //============ USUNIECIE CUDZYSLOWOW Z TEKSTU =====================

  static public String removeQuotationMarks(String oldText)
  {
      if ((oldText==null)||(oldText.length()==0)||(oldText.length()<=2)) return oldText;

      int i = 0;
      while (oldText.charAt(i)==' ')
      {
        i++;
        if (i==oldText.length()) return oldText;
      }

      char firstChar = oldText.charAt(i);

      int j = oldText.length() - 1;
      while (oldText.charAt(j)==' ')
      {
        j--;
        if ((j==0)||(i>j)) return oldText;
      }

      char lastChar = oldText.charAt(j);

      String newText = null;

      if ((firstChar=='\"')&&(lastChar=='\"'))
      {
        newText = oldText.substring(i+1,j);
      }
      else newText = new String(oldText);

      return newText;
  }

  //============ BEZWARUNKOWE USUNIECIE WSZYSTKICH CUDZYSLOWOW Z TEKSTU =====================

  static public String removeAbsolutelyQuotationMarks(String oldText)
  {
      if ((oldText==null)||(oldText.length()==0)) return oldText;

      String newText = new String();

      for (int i=0; i<oldText.length(); i++)
      {
        if (oldText.charAt(i)!='\"')
        {
          newText = newText + oldText.charAt(i);
        }
      }
      return newText;
  }


  //============ DODANIE CUDZYSLOWOW DO TEKSTU =====================

  static public String addQuotationMarks(String oldText)
  {
    if (oldText==null) return oldText;

    boolean isSeparator = false;
    for (int i=0; i<oldText.length(); i++)
    {
      if ((oldText.charAt(i)==' ')||
          (oldText.charAt(i)==','))
      {
        isSeparator = true; break;
      }
    }

    String newText = null;

    if (isSeparator)
    {
      if ((oldText.charAt(0)!='\"')&&(oldText.charAt(oldText.length()-1)!='\"'))
      {
        newText = new String("\""+oldText+"\"");
      }
      else
       if ((oldText.charAt(0)=='\"')&&(oldText.charAt(oldText.length()-1)!='\"'))
       {
         newText = new String(oldText+"\"");
       }
       else
        if ((oldText.charAt(0)!='\"')&&(oldText.charAt(oldText.length()-1)=='\"'))
        {
           newText = new String("\""+oldText);
        }
        else newText = oldText;
    }
    else //nie powinno byc separatora
    {
      if ((oldText.charAt(0)=='\"')&&(oldText.charAt(oldText.length()-1)=='\"'))
      {
        newText = oldText.substring(1,oldText.length()-1);
      }
      else newText = oldText;
    }

    return newText;
  }

  //========== USTALENIE NAZWY TEMPORALNEGO PLIKU ================

  static private String TEMP_PATH = null;

  static public String getTempPathFileName(String name) throws IOException
  {
    String SEPARATOR = File.separator;

    if (TEMP_PATH==null)
    {
      String userHomeDir = System.getProperty("user.home");

      File file = new File(userHomeDir+SEPARATOR+"temp_rses");

      try
      {
        if (file.isDirectory())
        {
          TEMP_PATH = new String(userHomeDir+SEPARATOR+"temp_rses");
          return new String(userHomeDir+SEPARATOR+"temp_rses"+SEPARATOR+name);
        }
        else
        {
           if (file.mkdir())
           {
             TEMP_PATH = new String(userHomeDir+SEPARATOR+"temp_rses");
             return new String(userHomeDir+SEPARATOR+"temp_rses"+SEPARATOR+name);
           }
           else
           {
             throw new IndexOutOfBoundsException("Cannot save temporary-file!");
           }
        }
      }
      catch (Exception e)
      {
        throw new IndexOutOfBoundsException("Cannot save temporary-file!");
      }
    }
    else return new String(TEMP_PATH+SEPARATOR+name);
  }

  /**************** USTAWIANIE LICZBY MIEJSC ZNACZACYCH ***********************
  * Ponizsza metoda umozliwia ustawienie ilosci liczb znaczacych po przecinku,
  * przy reprezentowaniu liczb zmiennoprzecinkowych jako tekstow (wartosci typu String).
  * Podana liczba (pierwszy parametr metody) jest zamieniana na wartosc typu String,
  * przy czym jako drugi parametr tej metody podaje sie liczbe znaczacych miejsc
  * po przecinku, jakie beda uwzglednione przy zamianie liczby na wartosc typu String
  * (liczba zostanie zookraglona).
  *****************************************************************************/


  private static NumberFormat numberFormat;

  static
  {
     numberFormat = NumberFormat.getInstance(Locale.US);
  }

  public static String formatDouble(double number,int precision)
  {
     numberFormat.setMaximumFractionDigits(precision);
     return numberFormat.format(number);
  }

  static public String upDateSeparators(String fName)
  {
    String newFName = new String();

    for (int i=0; i<fName.length(); i++)
    {
      char ch = fName.charAt(i);
      if (ch=='\\')
      {
        newFName = newFName + File.separatorChar;
      }
      else newFName = newFName + ch;
    }
    return newFName;
  }


  public static void main(String [] a)
  {

  }


}
