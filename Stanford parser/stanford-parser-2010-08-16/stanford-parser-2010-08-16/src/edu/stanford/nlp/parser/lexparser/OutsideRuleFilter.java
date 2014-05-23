package edu.stanford.nlp.parser.lexparser;

import java.util.*;

import edu.stanford.nlp.util.Numberer;

public class OutsideRuleFilter {

  private Numberer tagNumberer = Numberer.getGlobalNumberer("tags");
  private Numberer stateNumberer = Numberer.getGlobalNumberer("states");
  private int numTags;
  private int numFAs;

  protected FA[] leftFA;
  protected FA[] rightFA;

  protected static <A> List<A> reverse(List<A> list) {
    int sz = list.size();
    List<A> reverse = new ArrayList<A>(sz);
    for (int i = sz - 1; i >= 0; i--) {
      reverse.add(list.get(i));
    }
    return reverse;
  }

  protected FA buildFA(List tags) {
    FA fa = new FA(tags.size() + 1, numTags);
    fa.setLoopState(0, true);
    for (int state = 1; state <= tags.size(); state++) {
      Object tagO = tags.get(state - 1);
      if (tagO == null) {
        fa.setLoopState(state, true);
        for (int symbol = 0; symbol < numTags; symbol++) {
          fa.setTransition(state - 1, symbol, state);
        }
      } else {
        int tag = tagNumberer.number(tagO);
        fa.setTransition(state - 1, tag, state);
      }
    }
    return fa;
  }

  protected final void registerRule(List<String> leftTags, List<String> rightTags, int state) {
    leftFA[state] = buildFA(leftTags);
    rightFA[state] = buildFA(reverse(rightTags));
  }

  public void init() {
    for (int rule = 0; rule < numFAs; rule++) {
      leftFA[rule].init();
      rightFA[rule].init();
    }
  }

  public void advanceRight(boolean[] tags) {
    for (int tag = 0; tag < numTags; tag++) {
      if (!tags[tag]) {
        continue;
      }
      for (int rule = 0; rule < numFAs; rule++) {
        leftFA[rule].input(tag);
      }
    }
    for (int rule = 0; rule < numFAs; rule++) {
      leftFA[rule].advance();
    }
  }

  public void leftAccepting(boolean[] result) {
    for (int rule = 0; rule < numFAs; rule++) {
      result[rule] = leftFA[rule].isAccepting();
    }
  }

  public void advanceLeft(boolean[] tags) {
    for (int tag = 0; tag < numTags; tag++) {
      if (!tags[tag]) {
        continue;
      }
      for (int rule = 0; rule < numFAs; rule++) {
        rightFA[rule].input(tag);
      }
    }
    for (int rule = 0; rule < numFAs; rule++) {
      rightFA[rule].advance();
    }
  }

  public void rightAccepting(boolean[] result) {
    for (int rule = 0; rule < numFAs; rule++) {
      result[rule] = rightFA[rule].isAccepting();
    }
  }

  private void allocate(int numFAs) {
    this.numFAs = numFAs;
    leftFA = new FA[numFAs];
    rightFA = new FA[numFAs];
  }

  public OutsideRuleFilter(BinaryGrammar bg) {
    int numStates = stateNumberer.total();
    numTags = tagNumberer.total();
    allocate(numStates);
    for (int state = 0; state < numStates; state++) {
      String stateStr = (String) stateNumberer.object(state);
      List<String> left = new ArrayList<String>();
      List<String> right = new ArrayList<String>();
      if (!bg.isSynthetic(state)) {
        registerRule(left, right, state);
        continue;
      }
      boolean foundSemi = false;
      boolean foundDots = false;
      List<String> array = left;
      StringBuilder sb = new StringBuilder();
      for (int c = 0; c < stateStr.length(); c++) {
        if (stateStr.charAt(c) == ':') {
          foundSemi = true;
          continue;
        }
        if (!foundSemi) {
          continue;
        }
        if (stateStr.charAt(c) == ' ') {
          if (sb.length() > 0) {
            String str = sb.toString();
            if (!tagNumberer.hasSeen(str)) {
              str = null;
            }
            array.add(str);
            sb = new StringBuilder();
          }
          continue;
        }
        if (!foundDots && stateStr.charAt(c) == '.') {
          c += 3;
          foundDots = true;
          array = right;
          continue;
        }
        sb.append(stateStr.charAt(c));
      }
      registerRule(left, right, state);
    }
  }

  /** This is a simple Finite Automaton implementation. */
  static class FA {
    protected boolean[] inStatePrev;
    protected boolean[] inStateNext;
    protected boolean[] loopState;
    protected int acceptingState;
    protected int initialState;
    protected int numStates;
    protected int numSymbols;
    protected int[][] transition; // state x tag

    public void init() {
      Arrays.fill(inStatePrev, false);
      Arrays.fill(inStateNext, false);
      inStatePrev[initialState] = true;
    }

    public void input(int symbol) {
      for (int prevState = 0; prevState < numStates; prevState++) {
        if (inStatePrev[prevState]) {
          inStateNext[transition[prevState][symbol]] = true;
        }
      }
    }

    public void advance() {
      boolean[] temp = inStatePrev;
      inStatePrev = inStateNext;
      inStateNext = temp;
      Arrays.fill(inStateNext, false);
      for (int state = 0; state < numStates; state++) {
        if (inStatePrev[state] && loopState[state]) {
          inStateNext[state] = true;
        }
      }
    }

    public boolean isAccepting() {
      return inStatePrev[acceptingState];
    }

    public void setTransition(int state, int symbol, int result) {
      transition[state][symbol] = result;
    }

    public void setLoopState(int state, boolean loops) {
      loopState[state] = loops;
    }

    public FA(int numStates, int numSymbols) {
      this.numStates = numStates;
      this.numSymbols = numSymbols;
      acceptingState = numStates - 1;
      inStatePrev = new boolean[numStates];
      inStateNext = new boolean[numStates];
      loopState = new boolean[numStates];
      transition = new int[numStates][numSymbols];
    }

  } // end class FA
  
} // end class OutsideRuleFilter
