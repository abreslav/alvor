package ee.stacc.productivity.edsl.testproject;

/*
 * Copied partly from EArved, StringUtil.java
 */

import java.util.Collection;

/**
 * @author Anton Kuzmin <anton.kuzmin@webmedia.ee>
 */
public class EArvedStringUtil {

  /**
   * Returns String, which contains interrogation marks('?') separated with commas.
   * 
   * @param number of marks
   * @ResultForSQLChecker (?, ?)
   */
  public static String getQuestionMarksForQuery(int number) {
    StringBuffer sb = new StringBuffer();
    if (number > 0) {
      sb.append(" (");
      // @EffectForSQLChecker: sb.append("?, ?, ?");
      for (int i = 0; i < number; i++) {
        sb.append('?');
        if (i != number - 1) {
          sb.append(", ");
        }
      }
      sb.append(") ");
    }
    return sb.toString();
  }

  /**
   * Returns String, which contains groups of interrogation marks separated with commas. Example: ((?,?), (?,?))
   * 
   * @param groupCount number of groups
   * @param number number of ? inside each group
   * @ResultForSQLChecker (?, ?)
   */
  public static String getQuestionMarksForQuery(int groupCount, int number) {
    if (groupCount <= 0 || number <= 0) {
      return "";
    }

    StringBuffer sb = new StringBuffer();
    sb.append('(');

    for (int i = 0; i < groupCount; i++) {
      sb.append(getQuestionMarksForQuery(number));
      sb.append(", ");
    }
    sb.delete(sb.length() - 2, sb.length());

    sb.append(')');

    return sb.toString();
  }

  /**
   * Returns String, which contains interrogation marks('?') separated with commas. The number of marks is determined
   * from the size of the collection
   * 
   * @param collection collection
   * @ResultForSQLChecker (?, ?)
   */
  public static String getQuestionMarksForQuery(Collection<?> collection) {
    return getQuestionMarksForQuery(collection.size());
  }

  public static void appendInStatementWithQuestionMarks(StringBuilder buffer, String column, int questionMarksCount) {
    if (questionMarksCount <= 1000) {
      buffer.append(" ");
      buffer.append(column);
      buffer.append(" in ");
      buffer.append(getQuestionMarksForQuery(questionMarksCount));
      return;
    }

    buffer.ensureCapacity(100000);

    int times = questionMarksCount / 1000 + 1;

    for (int i = 1; i <= times; i++) {
      buffer.append(column);
      buffer.append(" in ");

      if (i == times) {
        buffer.append(getQuestionMarksForQuery(questionMarksCount % 1000));
      } else {
        buffer.append(getQuestionMarksForQuery(1000));
        buffer.append(" or ");
      }
    }
  }

  public static void appendInStatementWithQuestionMarks(StringBuilder buffer, String column, Collection<?> collection) {
    appendInStatementWithQuestionMarks(buffer, column, collection.size());
  }

  /**
   * Returns String, which contains interrogation marks('?') separated with commas. The number of marks is determined
   * from the size of the array
   * 
   * @param array array
   */
  public static String getQuestionMarksForQuery(Object[] array) {
    return getQuestionMarksForQuery(array.length);
  }

  /**
   * Does the same thing as String.format but formats null-s as empty strings
   * 
   * @param format format string
   * @param args Arguments referenced by format specifiers
   * @return
   */
}

