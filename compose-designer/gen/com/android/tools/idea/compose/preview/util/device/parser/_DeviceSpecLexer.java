/* The following code was generated by JFlex 1.7.0 tweaked for IntelliJ platform */

package com.android.tools.idea.compose.preview.util.device.parser;

import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.CHIN_SIZE_KEYWORD;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.COLON;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.COMMA;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.DP;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.DPI_KEYWORD;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.EQUALS;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.FALSE;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.HEIGHT_KEYWORD;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.ID_KEYWORD;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.IS_ROUND_KEYWORD;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.LANDSCAPE_KEYWORD;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.NAME_KEYWORD;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.NUMERIC_T;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.ORIENTATION_KEYWORD;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.PARENT_KEYWORD;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.PORTRAIT_KEYWORD;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.PX;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.SPEC_KEYWORD;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.SQUARE_KEYWORD;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.STRING_T;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.TRUE;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.UNIT_KEYWORD;
import static com.android.tools.idea.compose.preview.util.device.parser.DeviceSpecTypes.WIDTH_KEYWORD;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.7.0
 * from the specification file <tt>_DeviceSpecLexer.flex</tt>
 */
public class _DeviceSpecLexer implements FlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int DIMENSION_PARAM_VALUE = 2;
  public static final int STRING_PARAM = 4;
  public static final int STRING_VALUE = 6;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1,  1,  2,  2,  3, 3
  };

  /** 
   * Translates characters to character classes
   * Chosen bits are [11, 6, 4]
   * Total runtime size is 14112 bytes
   */
  public static int ZZ_CMAP(int ch) {
    return ZZ_CMAP_A[(ZZ_CMAP_Y[(ZZ_CMAP_Z[ch>>10]<<6)|((ch>>4)&0x3f)]<<4)|(ch&0xf)];
  }

  /* The ZZ_CMAP_Z table has 1088 entries */
  static final char ZZ_CMAP_Z[] = zzUnpackCMap(
    "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\2\11\1\12\1\13\6\14\1\15\23\14\1\16"+
    "\1\14\1\17\1\20\12\14\1\21\10\11\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1"+
    "\32\1\33\1\34\1\35\2\11\1\14\1\36\3\11\1\37\10\11\1\40\1\41\5\14\1\42\1\43"+
    "\11\11\1\44\2\11\1\45\5\11\1\46\4\11\1\47\1\50\4\11\51\14\1\51\3\14\1\52\1"+
    "\53\4\14\1\54\12\11\1\55\u0381\11");

  /* The ZZ_CMAP_Y table has 2944 entries */
  static final char ZZ_CMAP_Y[] = zzUnpackCMap(
    "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\1\1\11\1\12\1\13\1\14\1\13\1\14\34"+
    "\13\1\15\1\16\1\17\10\1\1\20\1\21\1\13\1\22\4\13\1\23\10\13\1\24\12\13\1\4"+
    "\1\13\1\25\1\4\1\13\1\26\4\1\1\13\1\27\1\30\2\1\2\13\1\27\1\1\1\31\1\4\5\13"+
    "\1\32\1\33\1\34\1\1\1\35\1\13\1\1\1\36\5\13\1\37\1\40\2\13\1\27\1\41\1\13"+
    "\1\42\1\43\1\1\1\13\1\44\4\1\1\13\1\45\4\1\1\46\2\13\1\47\1\1\1\50\1\15\1"+
    "\4\1\51\1\52\1\53\1\54\1\55\1\56\1\15\1\16\1\57\1\52\1\53\1\60\1\1\1\61\1"+
    "\62\1\63\1\64\1\22\1\53\1\65\1\1\1\66\1\15\1\67\1\70\1\52\1\53\1\65\1\1\1"+
    "\56\1\15\1\40\1\71\1\72\1\73\1\74\1\1\1\66\1\62\1\1\1\75\1\35\1\53\1\47\1"+
    "\1\1\76\1\15\1\1\1\77\1\35\1\53\1\100\1\1\1\55\1\15\1\101\1\75\1\35\1\13\1"+
    "\102\1\55\1\103\1\15\1\104\1\105\1\106\1\13\1\107\1\110\1\1\1\62\1\1\1\4\2"+
    "\13\1\111\1\110\1\74\2\1\1\112\1\113\1\114\1\115\1\116\1\117\2\1\1\66\1\1"+
    "\1\74\1\1\1\120\1\13\1\121\1\1\1\122\7\1\2\13\1\27\1\123\1\74\1\124\1\125"+
    "\1\126\1\127\1\74\2\13\1\130\2\13\1\131\24\13\1\132\1\133\2\13\1\132\2\13"+
    "\1\134\1\135\1\14\3\13\1\135\3\13\1\27\2\1\1\13\1\1\5\13\1\136\1\4\45\13\1"+
    "\34\1\13\1\137\1\27\4\13\1\27\1\140\1\141\1\16\1\13\1\16\1\13\1\16\1\141\1"+
    "\66\3\13\1\142\1\1\1\143\1\74\2\1\1\74\5\13\1\26\1\144\1\13\1\145\4\13\1\37"+
    "\1\13\1\146\2\1\1\62\1\13\1\147\1\150\2\13\1\151\1\13\2\74\2\1\1\13\1\110"+
    "\3\13\1\150\2\1\2\74\1\152\5\1\1\105\2\13\1\142\1\153\1\74\2\1\1\154\1\13"+
    "\1\155\3\13\1\37\1\1\2\13\1\142\1\1\1\156\2\13\1\147\1\44\5\1\1\157\1\160"+
    "\14\13\4\1\21\13\1\136\2\13\1\136\1\161\1\13\1\147\3\13\1\162\1\163\1\164"+
    "\1\121\1\163\1\165\1\1\1\166\2\1\1\167\1\1\1\170\1\1\1\121\6\1\1\171\1\172"+
    "\1\173\1\117\1\174\3\1\1\175\147\1\2\13\1\146\2\13\1\146\10\13\1\176\1\177"+
    "\2\13\1\130\3\13\1\200\1\1\1\13\1\110\4\201\4\1\1\123\35\1\1\202\2\1\1\203"+
    "\1\4\4\13\1\204\1\4\4\13\1\131\1\105\1\13\1\147\1\4\4\13\1\146\1\1\1\13\1"+
    "\27\3\1\1\13\40\1\133\13\1\37\4\1\135\13\1\37\2\1\10\13\1\121\4\1\2\13\1\147"+
    "\20\13\1\121\1\13\1\151\1\1\2\13\1\146\1\123\1\13\1\147\4\13\1\37\2\1\1\205"+
    "\1\206\5\13\1\207\1\13\1\146\1\26\3\1\1\205\1\210\1\13\1\30\1\1\3\13\1\142"+
    "\1\206\2\13\1\142\1\1\1\74\1\1\1\211\2\13\1\37\1\13\1\110\1\1\1\13\1\121\1"+
    "\46\2\13\1\30\1\123\1\74\1\212\1\146\2\13\1\44\1\1\1\213\1\74\1\13\1\214\3"+
    "\13\1\215\1\216\1\217\1\27\1\63\1\220\1\221\1\201\2\13\1\131\1\37\7\13\1\30"+
    "\1\74\72\13\1\142\1\13\1\222\2\13\1\151\20\1\26\13\1\147\6\13\1\74\2\1\1\110"+
    "\1\223\1\53\1\224\1\225\6\13\1\16\1\1\1\154\25\13\1\147\1\1\4\13\1\206\2\13"+
    "\1\26\2\1\1\151\7\1\1\212\7\13\1\121\1\1\1\74\1\4\1\27\1\4\1\27\1\62\4\13"+
    "\1\146\1\226\1\227\2\1\1\230\1\13\1\14\1\231\2\147\2\1\7\13\1\27\30\1\1\13"+
    "\1\121\3\13\1\66\2\1\2\13\1\1\1\13\1\232\2\13\1\37\1\13\1\147\2\13\1\233\3"+
    "\1\11\13\1\147\1\74\2\13\1\233\1\13\1\151\2\13\1\26\3\13\1\142\11\1\23\13"+
    "\1\110\1\13\1\37\1\26\11\1\1\234\2\13\1\235\1\13\1\37\1\13\1\110\1\13\1\146"+
    "\4\1\1\13\1\236\1\13\1\37\1\13\1\74\4\1\3\13\1\237\4\1\1\66\1\240\1\13\1\142"+
    "\2\1\1\13\1\121\1\13\1\121\2\1\1\120\1\13\1\150\1\1\3\13\1\37\1\13\1\37\1"+
    "\13\1\30\1\13\1\16\6\1\4\13\1\44\3\1\3\13\1\30\3\13\1\30\60\1\1\154\2\13\1"+
    "\26\2\1\1\62\1\1\1\154\2\13\2\1\1\13\1\44\1\74\1\154\1\13\1\110\1\62\1\1\2"+
    "\13\1\241\1\154\2\13\1\30\1\242\1\243\2\1\1\13\1\22\1\151\5\1\1\244\1\245"+
    "\1\44\2\13\1\146\1\1\1\74\1\70\1\52\1\53\1\65\1\1\1\246\1\16\11\1\3\13\1\150"+
    "\1\247\1\74\2\1\3\13\1\1\1\250\1\74\12\1\2\13\1\146\2\1\1\251\2\1\3\13\1\1"+
    "\1\252\1\74\2\1\2\13\1\27\1\1\1\74\3\1\1\13\1\74\1\1\1\74\26\1\4\13\1\74\1"+
    "\123\34\1\3\13\1\44\20\1\1\53\1\13\1\146\1\1\1\66\1\74\1\1\1\206\1\13\67\1"+
    "\71\13\1\74\16\1\14\13\1\142\53\1\2\13\1\146\75\1\44\13\1\110\33\1\43\13\1"+
    "\44\1\13\1\146\1\74\6\1\1\13\1\147\1\1\3\13\1\1\1\142\1\74\1\154\1\253\1\13"+
    "\67\1\4\13\1\150\1\66\3\1\1\154\4\1\1\66\1\1\76\13\1\121\1\1\57\13\1\30\20"+
    "\1\1\16\77\1\6\13\1\27\1\121\1\44\1\74\66\1\5\13\1\212\3\13\1\141\1\254\1"+
    "\255\1\256\3\13\1\257\1\260\1\13\1\261\1\262\1\35\24\13\1\263\1\13\1\35\1"+
    "\131\1\13\1\131\1\13\1\212\1\13\1\212\1\146\1\13\1\146\1\13\1\53\1\13\1\53"+
    "\1\13\1\264\17\13\1\150\3\1\4\13\1\142\1\74\112\1\1\256\1\13\1\265\1\266\1"+
    "\267\1\270\1\271\1\272\1\273\1\151\1\274\1\151\24\1\55\13\1\110\2\1\103\13"+
    "\1\150\15\13\1\147\150\13\1\16\25\1\41\13\1\147\36\1");

  /* The ZZ_CMAP_A table has 3024 entries */
  static final char ZZ_CMAP_A[] = zzUnpackCMap(
    "\11\0\1\2\4\1\22\0\1\2\1\0\1\6\11\0\1\23\1\0\1\4\1\0\12\3\1\25\2\0\1\24\3"+
    "\0\21\5\1\33\1\40\7\5\1\0\1\7\4\0\1\15\1\5\1\26\1\22\1\13\1\14\1\37\1\36\1"+
    "\31\2\5\1\16\1\34\1\27\1\30\1\20\1\32\1\11\1\17\1\10\1\12\1\5\1\35\1\21\1"+
    "\5\1\41\12\0\1\1\12\0\1\2\11\0\1\5\12\0\1\5\4\0\1\5\5\0\27\5\1\0\12\5\4\0"+
    "\14\5\16\0\5\5\7\0\1\5\1\0\1\5\1\0\5\5\1\0\2\5\2\0\4\5\1\0\1\5\6\0\1\5\1\0"+
    "\3\5\1\0\1\5\1\0\4\5\1\0\23\5\1\0\13\5\10\0\15\5\2\0\1\5\6\0\10\5\10\0\13"+
    "\5\5\0\3\5\15\0\12\5\4\0\6\5\1\0\1\5\17\0\2\5\7\0\17\5\2\0\2\5\1\0\16\5\15"+
    "\0\11\5\13\0\1\5\22\0\2\5\4\0\1\5\5\0\6\5\4\0\1\5\11\0\1\5\3\0\1\5\7\0\11"+
    "\5\7\0\5\5\1\0\10\5\6\0\26\5\3\0\1\5\2\0\1\5\7\0\11\5\4\0\10\5\2\0\2\5\2\0"+
    "\26\5\1\0\7\5\1\0\1\5\3\0\4\5\3\0\1\5\20\0\1\5\15\0\2\5\1\0\1\5\5\0\6\5\4"+
    "\0\2\5\1\0\2\5\1\0\2\5\1\0\2\5\17\0\4\5\1\0\1\5\7\0\12\5\2\0\3\5\20\0\11\5"+
    "\1\0\2\5\1\0\2\5\1\0\5\5\3\0\1\5\2\0\1\5\30\0\1\5\13\0\10\5\2\0\1\5\3\0\1"+
    "\5\1\0\6\5\3\0\3\5\1\0\4\5\3\0\2\5\1\0\1\5\1\0\2\5\3\0\2\5\3\0\3\5\3\0\14"+
    "\5\13\0\10\5\1\0\2\5\10\0\3\5\5\0\1\5\4\0\10\5\1\0\6\5\1\0\5\5\3\0\1\5\3\0"+
    "\2\5\15\0\13\5\2\0\1\5\6\0\3\5\10\0\1\5\12\0\6\5\5\0\22\5\3\0\10\5\1\0\11"+
    "\5\1\0\1\5\2\0\7\5\11\0\1\5\1\0\2\5\15\0\2\5\1\0\1\5\2\0\2\5\1\0\1\5\2\0\1"+
    "\5\6\0\4\5\1\0\7\5\1\0\3\5\1\0\1\5\1\0\1\5\2\0\2\5\1\0\4\5\1\0\2\5\11\0\1"+
    "\5\2\0\5\5\1\0\1\5\11\0\12\5\2\0\14\5\1\0\24\5\13\0\5\5\22\0\7\5\4\0\4\5\3"+
    "\0\1\5\3\0\2\5\7\0\3\5\4\0\15\5\14\0\1\5\1\0\6\5\1\0\1\5\5\0\1\5\2\0\13\5"+
    "\1\0\15\5\1\0\4\5\2\0\7\5\1\0\1\5\1\0\4\5\2\0\1\5\1\0\4\5\2\0\7\5\1\0\1\5"+
    "\1\0\4\5\2\0\16\5\2\0\6\5\2\0\1\2\17\5\1\0\10\5\7\0\15\5\1\0\6\5\23\0\1\5"+
    "\4\0\1\5\3\0\5\5\2\0\22\5\1\0\1\5\5\0\17\5\1\0\16\5\2\0\5\5\13\0\14\5\13\0"+
    "\1\5\15\0\7\5\7\0\16\5\15\0\14\5\3\0\3\5\11\0\4\5\1\0\4\5\3\0\2\5\11\0\10"+
    "\5\1\0\1\5\1\0\1\5\1\0\1\5\1\0\6\5\1\0\7\5\1\0\1\5\3\0\3\5\1\0\7\5\3\0\4\5"+
    "\2\0\6\5\4\0\13\2\15\0\2\1\5\0\1\2\17\0\1\2\1\0\1\5\15\0\1\5\2\0\1\5\4\0\1"+
    "\5\2\0\12\5\1\0\1\5\3\0\5\5\6\0\1\5\1\0\1\5\1\0\1\5\1\0\4\5\1\0\1\5\5\0\5"+
    "\5\4\0\1\5\4\0\2\5\13\0\5\5\6\0\4\5\3\0\2\5\14\0\10\5\7\0\10\5\1\0\7\5\1\0"+
    "\1\2\4\0\2\5\12\0\5\5\5\0\2\5\3\0\7\5\6\0\3\5\7\0\11\5\2\0\27\5\2\0\7\5\1"+
    "\0\3\5\1\0\4\5\1\0\4\5\2\0\6\5\3\0\1\5\1\0\1\5\2\0\5\5\1\0\15\5\1\0\10\5\4"+
    "\0\7\5\3\0\1\5\3\0\2\5\1\0\1\5\3\0\2\5\2\0\5\5\2\0\1\5\1\0\1\5\30\0\3\5\3"+
    "\0\6\5\2\0\6\5\2\0\6\5\11\0\7\5\4\0\5\5\3\0\5\5\5\0\1\5\1\0\10\5\1\0\5\5\1"+
    "\0\1\5\1\0\2\5\1\0\2\5\1\0\12\5\2\0\6\5\2\0\6\5\2\0\6\5\2\0\3\5\3\0\14\5\1"+
    "\0\16\5\1\0\2\5\1\0\2\5\1\0\10\5\6\0\4\5\4\0\16\5\2\0\1\5\1\0\14\5\1\0\2\5"+
    "\3\0\1\5\2\0\4\5\1\0\2\5\12\0\10\5\6\0\6\5\1\0\3\5\1\0\12\5\3\0\1\5\12\0\4"+
    "\5\13\0\13\5\1\0\1\5\3\0\7\5\1\0\1\5\1\0\4\5\1\0\17\5\1\0\2\5\14\0\3\5\7\0"+
    "\4\5\11\0\2\5\1\0\1\5\20\0\4\5\10\0\1\5\13\0\10\5\5\0\3\5\2\0\1\5\2\0\2\5"+
    "\2\0\4\5\1\0\14\5\1\0\1\5\1\0\7\5\1\0\21\5\1\0\4\5\2\0\10\5\1\0\7\5\1\0\14"+
    "\5\1\0\4\5\1\0\5\5\1\0\1\5\3\0\14\5\2\0\13\5\1\0\10\5\2\0\2\5\1\0\2\5\1\0"+
    "\1\5\2\0\1\5\1\0\12\5\1\0\4\5\1\0\1\5\1\0\1\5\6\0\1\5\4\0\1\5\1\0\1\5\1\0"+
    "\1\5\1\0\3\5\1\0\2\5\1\0\1\5\2\0\1\5\1\0\1\5\1\0\1\5\1\0\1\5\1\0\1\5\1\0\2"+
    "\5\1\0\1\5\2\0\4\5\1\0\7\5\1\0\4\5\1\0\4\5\1\0\1\5\1\0\12\5\1\0\5\5\1\0\3"+
    "\5\1\0\5\5\1\0\5\5");

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\4\0\1\1\1\2\1\3\10\4\1\5\1\6\1\7"+
    "\6\4\1\3\2\1\1\10\4\1\1\11\2\1\1\4"+
    "\1\0\7\4\1\12\1\4\1\13\4\4\1\14\2\4"+
    "\1\0\1\12\1\13\5\0\1\15\1\3\10\4\1\16"+
    "\6\4\1\3\5\0\1\17\1\20\2\4\1\21\10\4"+
    "\4\0\1\22\1\23\7\4\1\24\1\4\1\0\1\25"+
    "\2\0\1\4\1\26\4\4\1\0\1\27\1\30\4\4"+
    "\1\31\1\0\1\4\1\32\1\4\1\33\1\34\2\4"+
    "\1\35";

  private static int [] zzUnpackAction() {
    int [] result = new int[140];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\42\0\104\0\146\0\210\0\252\0\314\0\356"+
    "\0\u0110\0\u0132\0\u0154\0\u0176\0\u0198\0\u01ba\0\u01dc\0\210"+
    "\0\210\0\210\0\u01fe\0\u0220\0\u0242\0\u0264\0\u0286\0\u02a8"+
    "\0\u02ca\0\u02ec\0\u030e\0\210\0\u0330\0\u0352\0\u0374\0\u0396"+
    "\0\210\0\u03b8\0\u03da\0\u03fc\0\u041e\0\u0440\0\u0462\0\u0484"+
    "\0\u04a6\0\u04c8\0\u04ea\0\u050c\0\356\0\u052e\0\u0550\0\u0572"+
    "\0\u0594\0\u05b6\0\u05d8\0\356\0\u05fa\0\u061c\0\u063e\0\210"+
    "\0\210\0\u0660\0\u0682\0\u06a4\0\u06c6\0\u06e8\0\210\0\356"+
    "\0\u070a\0\u072c\0\u074e\0\u0770\0\u0792\0\u07b4\0\u07d6\0\u07f8"+
    "\0\356\0\u081a\0\u083c\0\u085e\0\u0880\0\u08a2\0\u08c4\0\210"+
    "\0\u08e6\0\u0908\0\u092a\0\u094c\0\u096e\0\356\0\356\0\u0990"+
    "\0\u09b2\0\356\0\u09d4\0\u09f6\0\u0a18\0\u0a3a\0\u0a5c\0\u0a7e"+
    "\0\u0aa0\0\u0ac2\0\u0ae4\0\u0b06\0\u0b28\0\u0b4a\0\210\0\356"+
    "\0\u0b6c\0\u0b8e\0\u0bb0\0\u0bd2\0\u0bf4\0\u0c16\0\u0c38\0\356"+
    "\0\u0c5a\0\u0c7c\0\210\0\u0c9e\0\u0cc0\0\u0ce2\0\356\0\u0d04"+
    "\0\u0d26\0\u0d48\0\u0d6a\0\u0d8c\0\210\0\210\0\u0dae\0\u0dd0"+
    "\0\u0df2\0\u0e14\0\356\0\u0e36\0\u0e58\0\356\0\u0e7a\0\210"+
    "\0\356\0\u0e9c\0\u0ebe\0\356";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[140];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\5\2\6\1\7\1\5\1\10\2\5\1\11\1\10"+
    "\1\12\1\10\1\13\1\10\1\14\1\15\1\16\1\10"+
    "\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26"+
    "\3\10\1\27\1\30\3\10\1\5\2\6\1\31\14\5"+
    "\1\32\1\5\1\33\1\34\1\21\1\5\1\35\6\5"+
    "\1\36\1\37\4\5\2\6\15\5\1\40\3\5\2\41"+
    "\1\5\1\42\1\5\1\43\13\5\1\10\1\5\1\10"+
    "\2\5\13\10\1\34\1\21\1\22\14\10\43\0\2\6"+
    "\37\0\3\10\1\7\1\44\1\10\1\0\1\45\13\10"+
    "\3\0\22\10\1\0\1\45\13\10\3\0\22\10\1\0"+
    "\1\45\1\10\1\46\11\10\3\0\22\10\1\0\1\45"+
    "\13\10\3\0\1\10\1\47\20\10\1\0\1\45\5\10"+
    "\1\50\5\10\3\0\22\10\1\0\1\45\5\10\1\51"+
    "\5\10\3\0\22\10\1\0\1\45\10\10\1\52\2\10"+
    "\3\0\4\10\1\53\15\10\1\0\1\45\5\10\1\54"+
    "\3\10\1\55\1\10\3\0\2\10\1\56\17\10\1\0"+
    "\1\45\10\10\1\57\2\10\3\0\22\10\1\0\1\45"+
    "\13\10\3\0\10\10\1\60\11\10\1\0\1\45\5\10"+
    "\1\61\5\10\3\0\22\10\1\0\1\45\1\10\1\62"+
    "\11\10\3\0\22\10\1\0\1\45\7\10\1\63\2\10"+
    "\1\64\3\0\22\10\1\0\1\45\13\10\3\0\3\10"+
    "\1\65\16\10\1\0\1\45\3\10\1\66\7\10\3\0"+
    "\14\10\3\0\1\31\1\67\56\0\1\70\40\0\1\71"+
    "\57\0\1\72\34\0\1\73\23\0\1\74\43\0\1\75"+
    "\41\0\1\76\46\0\1\77\17\0\3\10\1\100\2\10"+
    "\1\0\1\45\13\10\3\0\15\10\1\0\46\10\1\0"+
    "\1\45\2\10\1\101\10\10\3\0\22\10\1\0\1\45"+
    "\13\10\3\0\3\10\1\102\16\10\1\0\1\45\6\10"+
    "\1\103\4\10\3\0\22\10\1\0\1\45\13\10\3\0"+
    "\1\10\1\104\20\10\1\0\1\45\3\10\1\105\7\10"+
    "\3\0\22\10\1\0\1\45\2\10\1\106\10\10\3\0"+
    "\22\10\1\0\1\45\1\10\1\107\11\10\3\0\22\10"+
    "\1\0\1\45\1\10\1\110\11\10\3\0\22\10\1\0"+
    "\1\45\13\10\3\0\3\10\1\111\16\10\1\0\1\45"+
    "\13\10\3\0\3\10\1\112\16\10\1\0\1\45\13\10"+
    "\3\0\6\10\1\113\13\10\1\0\1\45\13\10\3\0"+
    "\3\10\1\114\16\10\1\0\1\45\13\10\3\0\5\10"+
    "\1\115\14\10\1\0\1\45\12\10\1\116\3\0\22\10"+
    "\1\0\1\45\13\10\3\0\3\10\1\117\10\10\3\0"+
    "\1\120\67\0\1\121\32\0\1\122\50\0\1\123\21\0"+
    "\1\124\64\0\1\125\5\0\6\10\1\0\1\45\3\10"+
    "\1\126\7\10\3\0\22\10\1\0\1\45\1\127\12\10"+
    "\3\0\22\10\1\0\1\45\7\10\1\130\3\10\3\0"+
    "\22\10\1\0\1\45\12\10\1\131\3\0\22\10\1\0"+
    "\1\45\13\10\3\0\1\132\21\10\1\0\1\45\5\10"+
    "\1\133\5\10\3\0\22\10\1\0\1\45\3\10\1\134"+
    "\7\10\3\0\22\10\1\0\1\45\1\135\12\10\3\0"+
    "\22\10\1\0\1\45\13\10\3\0\1\10\1\136\20\10"+
    "\1\0\1\45\3\10\1\64\7\10\3\0\22\10\1\0"+
    "\1\45\3\10\1\137\7\10\3\0\22\10\1\0\1\45"+
    "\13\10\3\0\2\10\1\140\17\10\1\0\1\45\1\141"+
    "\12\10\3\0\22\10\1\0\1\45\13\10\3\0\11\10"+
    "\1\142\2\10\27\0\1\143\22\0\1\144\70\0\1\145"+
    "\15\0\1\146\41\0\1\147\26\0\6\10\1\0\1\45"+
    "\3\10\1\150\7\10\3\0\22\10\1\0\1\45\7\10"+
    "\1\151\3\10\3\0\22\10\1\0\1\45\1\10\1\152"+
    "\11\10\3\0\22\10\1\0\1\45\13\10\3\0\1\10"+
    "\1\153\20\10\1\0\1\45\1\10\1\154\11\10\3\0"+
    "\22\10\1\0\1\45\13\10\3\0\12\10\1\155\7\10"+
    "\1\0\1\45\13\10\3\0\1\10\1\156\20\10\1\0"+
    "\1\45\2\10\1\157\10\10\3\0\22\10\1\0\1\45"+
    "\13\10\3\0\10\10\1\160\11\10\1\0\1\45\13\10"+
    "\3\0\10\10\1\161\3\10\40\0\1\162\37\0\1\163"+
    "\41\0\1\164\32\0\1\165\12\0\6\10\1\0\1\45"+
    "\13\10\3\0\1\166\21\10\1\0\1\45\3\10\1\167"+
    "\7\10\3\0\22\10\1\0\1\45\1\64\12\10\3\0"+
    "\22\10\1\0\1\45\5\10\1\170\5\10\3\0\22\10"+
    "\1\0\1\45\13\10\3\0\3\10\1\171\16\10\1\0"+
    "\1\45\1\172\12\10\3\0\22\10\1\0\1\45\13\10"+
    "\3\0\1\10\1\173\20\10\1\0\1\45\1\160\12\10"+
    "\3\0\14\10\31\0\1\174\20\0\1\175\41\0\1\176"+
    "\31\0\6\10\1\0\1\45\5\10\1\177\5\10\3\0"+
    "\22\10\1\0\1\45\13\10\3\0\3\10\1\200\16\10"+
    "\1\0\1\45\13\10\3\0\13\10\1\201\6\10\1\0"+
    "\1\45\5\10\1\202\5\10\3\0\22\10\1\0\1\45"+
    "\12\10\1\203\3\0\14\10\41\0\1\204\6\10\1\0"+
    "\1\45\10\10\1\205\2\10\3\0\22\10\1\0\1\45"+
    "\1\206\12\10\3\0\22\10\1\0\1\45\3\10\1\160"+
    "\7\10\3\0\22\10\1\0\1\45\1\207\12\10\3\0"+
    "\14\10\13\0\1\210\26\0\6\10\1\0\1\45\3\10"+
    "\1\211\7\10\3\0\22\10\1\0\1\45\13\10\3\0"+
    "\3\10\1\212\16\10\1\0\1\45\13\10\3\0\2\10"+
    "\1\213\17\10\1\0\1\45\13\10\3\0\1\10\1\214"+
    "\12\10";

  private static int [] zzUnpackTrans() {
    int [] result = new int[3808];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String[] ZZ_ERROR_MSG = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\4\0\1\11\12\1\3\11\11\1\1\11\4\1\1\11"+
    "\3\1\1\0\21\1\1\0\2\11\5\0\1\11\20\1"+
    "\1\11\5\0\15\1\4\0\1\11\12\1\1\0\1\11"+
    "\2\0\6\1\1\0\2\11\5\1\1\0\3\1\1\11"+
    "\4\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[140];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private CharSequence zzBuffer = "";

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /**
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */
  public _DeviceSpecLexer() {
    this((java.io.Reader)null);
  }


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public _DeviceSpecLexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    int size = 0;
    for (int i = 0, length = packed.length(); i < length; i += 2) {
      size += packed.charAt(i);
    }
    char[] map = new char[size];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < packed.length()) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }

  public final int getTokenStart() {
    return zzStartRead;
  }

  public final int getTokenEnd() {
    return getTokenStart() + yylength();
  }

  public void reset(CharSequence buffer, int start, int end, int initialState) {
    zzBuffer = buffer;
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      {@code false}, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position {@code pos} from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occurred while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advance() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL/*, zzEndReadL*/);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL/*, zzEndReadL*/);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + ZZ_CMAP(zzInput) ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
        return null;
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1: 
            { return BAD_CHARACTER;
            } 
            // fall through
          case 30: break;
          case 2: 
            { return WHITE_SPACE;
            } 
            // fall through
          case 31: break;
          case 3: 
            { return NUMERIC_T;
            } 
            // fall through
          case 32: break;
          case 4: 
            { return STRING_T;
            } 
            // fall through
          case 33: break;
          case 5: 
            { return COMMA;
            } 
            // fall through
          case 34: break;
          case 6: 
            { return EQUALS;
            } 
            // fall through
          case 35: break;
          case 7: 
            { return COLON;
            } 
            // fall through
          case 36: break;
          case 8: 
            { yypushback(yylength()); yybegin(YYINITIAL);
            } 
            // fall through
          case 37: break;
          case 9: 
            { yypushback(yylength()); yybegin(STRING_VALUE);
            } 
            // fall through
          case 38: break;
          case 10: 
            { return PX;
            } 
            // fall through
          case 39: break;
          case 11: 
            { return DP;
            } 
            // fall through
          case 40: break;
          case 12: 
            { yypushback(yylength()); yybegin(STRING_PARAM);
            } 
            // fall through
          case 41: break;
          case 13: 
            { return ID_KEYWORD;
            } 
            // fall through
          case 42: break;
          case 14: 
            { return DPI_KEYWORD;
            } 
            // fall through
          case 43: break;
          case 15: 
            { return TRUE;
            } 
            // fall through
          case 44: break;
          case 16: 
            { return UNIT_KEYWORD;
            } 
            // fall through
          case 45: break;
          case 17: 
            { return SPEC_KEYWORD;
            } 
            // fall through
          case 46: break;
          case 18: 
            { return NAME_KEYWORD;
            } 
            // fall through
          case 47: break;
          case 19: 
            { return FALSE;
            } 
            // fall through
          case 48: break;
          case 20: 
            { yypushback(yylength()); yybegin(DIMENSION_PARAM_VALUE);
            } 
            // fall through
          case 49: break;
          case 21: 
            { return WIDTH_KEYWORD;
            } 
            // fall through
          case 50: break;
          case 22: 
            { return SQUARE_KEYWORD;
            } 
            // fall through
          case 51: break;
          case 23: 
            { return HEIGHT_KEYWORD;
            } 
            // fall through
          case 52: break;
          case 24: 
            { return PARENT_KEYWORD;
            } 
            // fall through
          case 53: break;
          case 25: 
            { return IS_ROUND_KEYWORD;
            } 
            // fall through
          case 54: break;
          case 26: 
            { return PORTRAIT_KEYWORD;
            } 
            // fall through
          case 55: break;
          case 27: 
            { return CHIN_SIZE_KEYWORD;
            } 
            // fall through
          case 56: break;
          case 28: 
            { return LANDSCAPE_KEYWORD;
            } 
            // fall through
          case 57: break;
          case 29: 
            { return ORIENTATION_KEYWORD;
            } 
            // fall through
          case 58: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
