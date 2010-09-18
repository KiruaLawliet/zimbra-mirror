/* Generated By:JJTree&JavaCC: Do not edit this line. Parser.java */
package com.zimbra.cs.index.query.parser;

final class Parser/*@bgen(jjtree)*/implements ParserTreeConstants, ParserConstants {/*@bgen(jjtree)*/
  protected JJTParserState jjtree = new JJTParserState();

  final public SimpleNode parse() throws ParseException {
                            /*@bgen(jjtree) Root */
  SimpleNode jjtn000 = new SimpleNode(JJTROOT);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      Query();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 0:
      case 72:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 72:
          jj_consume_token(72);
          break;
        case 0:
          jj_consume_token(0);
          break;
        default:
          jj_la1[0] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
      default:
        jj_la1[1] = jj_gen;
        ;
      }
                             jjtree.closeNodeScope(jjtn000, true);
                             jjtc000 = false;
                             jjtn000.jjtSetLastToken(getToken(0));
                             {if (true) return jjtn000;}
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
    throw new Error("Missing return statement in function");
  }

  final private void Query() throws ParseException {
                        /*@bgen(jjtree) Query */
  SimpleNode jjtn000 = new SimpleNode(JJTQUERY);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SORTBY:
      case SORT:
        SortBy();
        break;
      default:
        jj_la1[2] = jj_gen;
        ;
      }
      Clause();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SORTBY:
      case SORT:
        SortBy();
        break;
      default:
        jj_la1[3] = jj_gen;
        ;
      }
      label_1:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case AND:
        case OR:
        case NOT:
        case LPAREN:
        case PLUS:
        case MINUS:
        case TERM:
        case QUOTED_TERM:
        case CONTENT:
        case SUBJECT:
        case MSGID:
        case ENVTO:
        case ENVFROM:
        case CONTACT:
        case TO:
        case FROM:
        case CC:
        case TOFROM:
        case TOCC:
        case FROMCC:
        case TOFROMCC:
        case IN:
        case UNDER:
        case INID:
        case UNDERID:
        case HAS:
        case FILENAME:
        case TYPE:
        case ATTACHMENT:
        case IS:
        case DATE:
        case DAY:
        case WEEK:
        case MONTH:
        case YEAR:
        case AFTER:
        case BEFORE:
        case SIZE:
        case BIGGER:
        case SMALLER:
        case TAG:
        case PRIORITY:
        case MESSAGE:
        case MY:
        case MODSEQ:
        case CONV:
        case CONV_COUNT:
        case CONV_MINM:
        case CONV_MAXM:
        case CONV_START:
        case CONV_END:
        case APPT_START:
        case APPT_END:
        case AUTHOR:
        case TITLE:
        case KEYWORDS:
        case COMPANY:
        case METADATA:
        case ITEM:
        case FIELD:
          ;
          break;
        default:
          jj_la1[4] = jj_gen;
          break label_1;
        }
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case AND:
        case OR:
          Conjunction();
          break;
        default:
          jj_la1[5] = jj_gen;
          ;
        }
        Clause();
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case SORTBY:
        case SORT:
          SortBy();
          break;
        default:
          jj_la1[6] = jj_gen;
          ;
        }
      }
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
  }

  final private void Clause() throws ParseException {
                         /*@bgen(jjtree) Clause */
  SimpleNode jjtn000 = new SimpleNode(JJTCLAUSE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
      case PLUS:
      case MINUS:
        Modifier();
        break;
      default:
        jj_la1[7] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LPAREN:
        jj_consume_token(LPAREN);
        Query();
        jj_consume_token(RPAREN);
        break;
      case CONTENT:
      case SUBJECT:
      case MSGID:
      case ENVTO:
      case ENVFROM:
      case CONTACT:
      case TO:
      case FROM:
      case CC:
      case TOFROM:
      case TOCC:
      case FROMCC:
      case TOFROMCC:
      case IN:
      case UNDER:
      case INID:
      case UNDERID:
      case HAS:
      case FILENAME:
      case TYPE:
      case ATTACHMENT:
      case IS:
      case SIZE:
      case BIGGER:
      case SMALLER:
      case TAG:
      case PRIORITY:
      case MESSAGE:
      case MY:
      case MODSEQ:
      case CONV:
      case CONV_COUNT:
      case CONV_MINM:
      case CONV_MAXM:
      case AUTHOR:
      case TITLE:
      case KEYWORDS:
      case COMPANY:
      case METADATA:
      case FIELD:
        TextClause();
        break;
      case ITEM:
        ItemClause();
        break;
      case DATE:
      case DAY:
      case WEEK:
      case MONTH:
      case YEAR:
      case AFTER:
      case BEFORE:
      case CONV_START:
      case CONV_END:
      case APPT_START:
      case APPT_END:
        DateClause();
        break;
      case TERM:
      case QUOTED_TERM:
        DefaultClause();
        break;
      default:
        jj_la1[8] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
  }

  final private void DefaultClause() throws ParseException {
                                /*@bgen(jjtree) DefaultClause */
  SimpleNode jjtn000 = new SimpleNode(JJTDEFAULTCLAUSE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case TERM:
        jj_consume_token(TERM);
        break;
      case QUOTED_TERM:
        jj_consume_token(QUOTED_TERM);
        break;
      default:
        jj_la1[9] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
  }

  final private void TextClause() throws ParseException {
                             /*@bgen(jjtree) TextClause */
  SimpleNode jjtn000 = new SimpleNode(JJTTEXTCLAUSE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      TextField();
      TextTerm();
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
  }

  final private void ItemClause() throws ParseException {
                             /*@bgen(jjtree) ItemClause */
  SimpleNode jjtn000 = new SimpleNode(JJTITEMCLAUSE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      jj_consume_token(ITEM);
      ItemTerm();
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
  }

  final private void DateClause() throws ParseException {
                             /*@bgen(jjtree) DateClause */
  SimpleNode jjtn000 = new SimpleNode(JJTDATECLAUSE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      DateField();
      DateTerm();
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
  }

  final private void TextField() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case FIELD:
      jj_consume_token(FIELD);
      break;
    case MSGID:
      jj_consume_token(MSGID);
      break;
    case MODSEQ:
      jj_consume_token(MODSEQ);
      break;
    case TYPE:
      jj_consume_token(TYPE);
      break;
    case MY:
      jj_consume_token(MY);
      break;
    case IS:
      jj_consume_token(IS);
      break;
    case TAG:
      jj_consume_token(TAG);
      break;
    case PRIORITY:
      jj_consume_token(PRIORITY);
      break;
    case FROM:
      jj_consume_token(FROM);
      break;
    case TO:
      jj_consume_token(TO);
      break;
    case CC:
      jj_consume_token(CC);
      break;
    case ENVFROM:
      jj_consume_token(ENVFROM);
      break;
    case ENVTO:
      jj_consume_token(ENVTO);
      break;
    case TOFROM:
      jj_consume_token(TOFROM);
      break;
    case TOCC:
      jj_consume_token(TOCC);
      break;
    case FROMCC:
      jj_consume_token(FROMCC);
      break;
    case TOFROMCC:
      jj_consume_token(TOFROMCC);
      break;
    case SUBJECT:
      jj_consume_token(SUBJECT);
      break;
    case MESSAGE:
      jj_consume_token(MESSAGE);
      break;
    case CONTENT:
      jj_consume_token(CONTENT);
      break;
    case IN:
      jj_consume_token(IN);
      break;
    case INID:
      jj_consume_token(INID);
      break;
    case UNDER:
      jj_consume_token(UNDER);
      break;
    case UNDERID:
      jj_consume_token(UNDERID);
      break;
    case ATTACHMENT:
      jj_consume_token(ATTACHMENT);
      break;
    case HAS:
      jj_consume_token(HAS);
      break;
    case FILENAME:
      jj_consume_token(FILENAME);
      break;
    case CONTACT:
      jj_consume_token(CONTACT);
      break;
    case AUTHOR:
      jj_consume_token(AUTHOR);
      break;
    case TITLE:
      jj_consume_token(TITLE);
      break;
    case KEYWORDS:
      jj_consume_token(KEYWORDS);
      break;
    case COMPANY:
      jj_consume_token(COMPANY);
      break;
    case METADATA:
      jj_consume_token(METADATA);
      break;
    case CONV:
      jj_consume_token(CONV);
      break;
    case CONV_COUNT:
      jj_consume_token(CONV_COUNT);
      break;
    case CONV_MINM:
      jj_consume_token(CONV_MINM);
      break;
    case CONV_MAXM:
      jj_consume_token(CONV_MAXM);
      break;
    case SIZE:
      jj_consume_token(SIZE);
      break;
    case BIGGER:
      jj_consume_token(BIGGER);
      break;
    case SMALLER:
      jj_consume_token(SMALLER);
      break;
    default:
      jj_la1[10] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final private void DateField() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case DATE:
      jj_consume_token(DATE);
      break;
    case DAY:
      jj_consume_token(DAY);
      break;
    case MONTH:
      jj_consume_token(MONTH);
      break;
    case WEEK:
      jj_consume_token(WEEK);
      break;
    case YEAR:
      jj_consume_token(YEAR);
      break;
    case AFTER:
      jj_consume_token(AFTER);
      break;
    case BEFORE:
      jj_consume_token(BEFORE);
      break;
    case CONV_START:
      jj_consume_token(CONV_START);
      break;
    case CONV_END:
      jj_consume_token(CONV_END);
      break;
    case APPT_START:
      jj_consume_token(APPT_START);
      break;
    case APPT_END:
      jj_consume_token(APPT_END);
      break;
    default:
      jj_la1[11] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final private void Conjunction() throws ParseException {
                              /*@bgen(jjtree) Conjunction */
  SimpleNode jjtn000 = new SimpleNode(JJTCONJUNCTION);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
        jj_consume_token(AND);
        break;
      case OR:
        jj_consume_token(OR);
        break;
      default:
        jj_la1[12] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
  }

  final private void Modifier() throws ParseException {
                           /*@bgen(jjtree) Modifier */
  SimpleNode jjtn000 = new SimpleNode(JJTMODIFIER);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
        jj_consume_token(PLUS);
        break;
      case MINUS:
        jj_consume_token(MINUS);
        break;
      case NOT:
        jj_consume_token(NOT);
        break;
      default:
        jj_la1[13] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
  }

  final private void DateModifier() throws ParseException {
                               /*@bgen(jjtree) DateModifier */
  SimpleNode jjtn000 = new SimpleNode(JJTDATEMODIFIER);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      jj_consume_token(NOT);
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
  }

  final private void SortBy() throws ParseException {
                         /*@bgen(jjtree) SortBy */
  SimpleNode jjtn000 = new SimpleNode(JJTSORTBY);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SORTBY:
        jj_consume_token(SORTBY);
        break;
      case SORT:
        jj_consume_token(SORT);
        break;
      default:
        jj_la1[14] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      jj_consume_token(TERM);
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
  }

  final private void TextTerm() throws ParseException {
                           /*@bgen(jjtree) TextTerm */
  SimpleNode jjtn000 = new SimpleNode(JJTTEXTTERM);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LPAREN:
        jj_consume_token(LPAREN);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case NOT:
        case PLUS:
        case MINUS:
          Modifier();
          break;
        default:
          jj_la1[15] = jj_gen;
          ;
        }
        TextTerm();
        label_2:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case AND:
          case OR:
          case NOT:
          case LPAREN:
          case PLUS:
          case MINUS:
          case TERM:
          case QUOTED_TERM:
            ;
            break;
          default:
            jj_la1[16] = jj_gen;
            break label_2;
          }
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case AND:
          case OR:
            Conjunction();
            break;
          default:
            jj_la1[17] = jj_gen;
            ;
          }
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case NOT:
          case PLUS:
          case MINUS:
            Modifier();
            break;
          default:
            jj_la1[18] = jj_gen;
            ;
          }
          TextTerm();
        }
        jj_consume_token(RPAREN);
        break;
      case TERM:
        jj_consume_token(TERM);
        break;
      case QUOTED_TERM:
        jj_consume_token(QUOTED_TERM);
        break;
      default:
        jj_la1[19] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
  }

  final private void ItemTerm() throws ParseException {
                           /*@bgen(jjtree) ItemTerm */
  SimpleNode jjtn000 = new SimpleNode(JJTITEMTERM);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LPAREN:
        jj_consume_token(LPAREN);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case NOT:
        case PLUS:
        case MINUS:
          Modifier();
          break;
        default:
          jj_la1[20] = jj_gen;
          ;
        }
        ItemTerm();
        label_3:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case AND:
          case OR:
          case NOT:
          case LPAREN:
          case PLUS:
          case MINUS:
          case TERM:
          case QUOTED_TERM:
          case BRACED_TERM:
            ;
            break;
          default:
            jj_la1[21] = jj_gen;
            break label_3;
          }
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case AND:
          case OR:
            Conjunction();
            break;
          default:
            jj_la1[22] = jj_gen;
            ;
          }
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case NOT:
          case PLUS:
          case MINUS:
            Modifier();
            break;
          default:
            jj_la1[23] = jj_gen;
            ;
          }
          ItemTerm();
        }
        jj_consume_token(RPAREN);
        break;
      case TERM:
        jj_consume_token(TERM);
        break;
      case QUOTED_TERM:
        jj_consume_token(QUOTED_TERM);
        break;
      case BRACED_TERM:
        jj_consume_token(BRACED_TERM);
        break;
      default:
        jj_la1[24] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
  }

  final private void DateTerm() throws ParseException {
                           /*@bgen(jjtree) DateTerm */
  SimpleNode jjtn000 = new SimpleNode(JJTDATETERM);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
  jjtn000.jjtSetFirstToken(getToken(1));
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LPAREN:
        jj_consume_token(LPAREN);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case NOT:
          DateModifier();
          break;
        default:
          jj_la1[25] = jj_gen;
          ;
        }
        DateTerm();
        label_4:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case AND:
          case OR:
          case NOT:
          case LPAREN:
          case PLUS:
          case MINUS:
          case TERM:
          case QUOTED_TERM:
            ;
            break;
          default:
            jj_la1[26] = jj_gen;
            break label_4;
          }
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case AND:
          case OR:
            Conjunction();
            break;
          default:
            jj_la1[27] = jj_gen;
            ;
          }
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case NOT:
            DateModifier();
            break;
          default:
            jj_la1[28] = jj_gen;
            ;
          }
          DateTerm();
        }
        jj_consume_token(RPAREN);
        break;
      case PLUS:
      case MINUS:
      case TERM:
      case QUOTED_TERM:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case PLUS:
        case MINUS:
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case MINUS:
            jj_consume_token(MINUS);
            break;
          case PLUS:
            jj_consume_token(PLUS);
            break;
          default:
            jj_la1[29] = jj_gen;
            jj_consume_token(-1);
            throw new ParseException();
          }
          break;
        default:
          jj_la1[30] = jj_gen;
          ;
        }
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case TERM:
          jj_consume_token(TERM);
          break;
        case QUOTED_TERM:
          jj_consume_token(QUOTED_TERM);
          break;
        default:
          jj_la1[31] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
      default:
        jj_la1[32] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
        jjtn000.jjtSetLastToken(getToken(0));
      }
    }
  }

  /** Generated Token Manager. */
  public ParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[33];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static private int[] jj_la1_2;
  static {
      jj_la1_init_0();
      jj_la1_init_1();
      jj_la1_init_2();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x1,0x1,0x0,0x0,0xffff2778,0x18,0x0,0x320,0xffff2440,0x2400,0xffff0000,0x0,0x18,0x320,0x0,0x320,0x2778,0x18,0x320,0x2440,0x320,0xa778,0x18,0x320,0xa440,0x20,0x2778,0x18,0x20,0x300,0x300,0x2400,0x2740,};
   }
   private static void jj_la1_init_1() {
      jj_la1_1 = new int[] {0x0,0x0,0x0,0x0,0xffffffff,0x0,0x0,0x0,0xffffffff,0x0,0xe1ffe03f,0x1e001fc0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_init_2() {
      jj_la1_2 = new int[] {0x100,0x100,0x18,0x18,0x27,0x0,0x18,0x0,0x27,0x0,0x23,0x0,0x0,0x0,0x18,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,};
   }

  /** Constructor with InputStream. */
  public Parser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public Parser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 33; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 33; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public Parser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 33; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 33; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public Parser(ParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 33; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(ParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 33; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[73];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 33; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
          if ((jj_la1_2[i] & (1<<j)) != 0) {
            la1tokens[64+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 73; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
