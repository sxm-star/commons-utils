package com.songxm.commons.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Inflector {
    protected static final Inflector INSTANCE = new Inflector();
    private LinkedList<Inflector.Rule> plurals = new LinkedList<>();
    private LinkedList<Inflector.Rule> singulars = new LinkedList<>();
    private final Set<String> uncountables = new HashSet<>();

    public static final Inflector getInstance() {
        return INSTANCE;
    }

    public Inflector() {
        this.initialize();
    }

    protected Inflector(Inflector original) {
        this.plurals.addAll(original.plurals);
        this.singulars.addAll(original.singulars);
        this.uncountables.addAll(original.uncountables);
    }

    @Override
    public Inflector clone() {
        return new Inflector(this);
    }

    public String pluralize(Object word) {
        if(word == null) {
            return null;
        } else {
            String wordStr = word.toString().trim();
            if(wordStr.length() == 0) {
                return wordStr;
            } else if(this.isUncountable(wordStr)) {
                return wordStr;
            } else {
                Iterator var3 = this.plurals.iterator();

                String result;
                do {
                    if(!var3.hasNext()) {
                        return wordStr;
                    }

                    Inflector.Rule rule = (Inflector.Rule)var3.next();
                    result = rule.apply(wordStr);
                } while(result == null);

                return result;
            }
        }
    }

    public String pluralize(Object word, int count) {
        return word == null?null:(count != 1 && count != -1?this.pluralize(word):word.toString());
    }

    public String singularize(Object word) {
        if(word == null) {
            return null;
        } else {
            String wordStr = word.toString().trim();
            if(wordStr.length() == 0) {
                return wordStr;
            } else if(this.isUncountable(wordStr)) {
                return wordStr;
            } else {
                Iterator var3 = this.singulars.iterator();

                String result;
                do {
                    if(!var3.hasNext()) {
                        return wordStr;
                    }

                    Inflector.Rule rule = (Inflector.Rule)var3.next();
                    result = rule.apply(wordStr);
                } while(result == null);

                return result;
            }
        }
    }

    public String lowerCamelCase(String lowerCaseAndUnderscoredWord, char... delimiterChars) {
        return this.camelCase(lowerCaseAndUnderscoredWord, false, delimiterChars);
    }

    public String upperCamelCase(String lowerCaseAndUnderscoredWord, char... delimiterChars) {
        return this.camelCase(lowerCaseAndUnderscoredWord, true, delimiterChars);
    }

    public String camelCase(String lowerCaseAndUnderscoredWord, boolean uppercaseFirstLetter, char... delimiterChars) {
        if(lowerCaseAndUnderscoredWord == null) {
            return null;
        } else {
            lowerCaseAndUnderscoredWord = lowerCaseAndUnderscoredWord.trim();
            if(lowerCaseAndUnderscoredWord.length() == 0) {
                return "";
            } else if(!uppercaseFirstLetter) {
                return lowerCaseAndUnderscoredWord.length() < 2?lowerCaseAndUnderscoredWord:"" + Character.toLowerCase(lowerCaseAndUnderscoredWord.charAt(0)) + this.camelCase(lowerCaseAndUnderscoredWord, true, delimiterChars).substring(1);
            } else {
                String result = lowerCaseAndUnderscoredWord;
                if(delimiterChars != null) {
                    char[] var5 = delimiterChars;
                    int var6 = delimiterChars.length;

                    for(int var7 = 0; var7 < var6; ++var7) {
                        char delimiterChar = var5[var7];
                        result = result.replace(delimiterChar, '_');
                    }
                }

                return replaceAllWithUppercase(result, "(^|_)(.)", 2);
            }
        }
    }

    public String underscore(String camelCaseWord, char... delimiterChars) {
        if(camelCaseWord == null) {
            return null;
        } else {
            String result = camelCaseWord.trim();
            if(result.length() == 0) {
                return "";
            } else {
                result = result.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2");
                result = result.replaceAll("([a-z\\d])([A-Z])", "$1_$2");
                result = result.replace('-', '_');
                if(delimiterChars != null) {
                    char[] var4 = delimiterChars;
                    int var5 = delimiterChars.length;

                    for(int var6 = 0; var6 < var5; ++var6) {
                        char delimiterChar = var4[var6];
                        result = result.replace(delimiterChar, '_');
                    }
                }

                return result.toLowerCase();
            }
        }
    }

    public String capitalize(String words) {
        if(words == null) {
            return null;
        } else {
            String result = words.trim();
            return result.length() == 0?"":(result.length() == 1?result.toUpperCase():"" + Character.toUpperCase(result.charAt(0)) + result.substring(1).toLowerCase());
        }
    }

    public String humanize(String lowerCaseAndUnderscoredWords, String... removableTokens) {
        if(lowerCaseAndUnderscoredWords == null) {
            return null;
        } else {
            String result = lowerCaseAndUnderscoredWords.trim();
            if(result.length() == 0) {
                return "";
            } else {
                result = result.replaceAll("_id$", "");
                if(removableTokens != null) {
                    String[] var4 = removableTokens;
                    int var5 = removableTokens.length;

                    for(int var6 = 0; var6 < var5; ++var6) {
                        String removableToken = var4[var6];
                        result = result.replaceAll(removableToken, "");
                    }
                }

                result = result.replaceAll("_+", " ");
                return this.capitalize(result);
            }
        }
    }

    public String titleCase(String words, String... removableTokens) {
        String result = this.humanize(words, removableTokens);
        result = replaceAllWithUppercase(result, "\\b([a-z])", 1);
        return result;
    }

    public String ordinalize(int number) {
        int remainder = number % 100;
        String numberStr = Integer.toString(number);
        if(11 <= number && number <= 13) {
            return numberStr + "th";
        } else {
            remainder = number % 10;
            return remainder == 1?numberStr + "st":(remainder == 2?numberStr + "nd":(remainder == 3?numberStr + "rd":numberStr + "th"));
        }
    }

    public boolean isUncountable(String word) {
        if(word == null) {
            return false;
        } else {
            String trimmedLower = word.trim().toLowerCase();
            return this.uncountables.contains(trimmedLower);
        }
    }

    public Set<String> getUncountables() {
        return this.uncountables;
    }

    public void addPluralize(String rule, String replacement) {
        Inflector.Rule pluralizeRule = new Inflector.Rule(rule, replacement);
        this.plurals.addFirst(pluralizeRule);
    }

    public void addSingularize(String rule, String replacement) {
        Inflector.Rule singularizeRule = new Inflector.Rule(rule, replacement);
        this.singulars.addFirst(singularizeRule);
    }

    public void addIrregular(String singular, String plural) {
        String singularRemainder = singular.length() > 1?singular.substring(1):"";
        String pluralRemainder = plural.length() > 1?plural.substring(1):"";
        this.addPluralize("(" + singular.charAt(0) + ")" + singularRemainder + "$", "$1" + pluralRemainder);
        this.addSingularize("(" + plural.charAt(0) + ")" + pluralRemainder + "$", "$1" + singularRemainder);
    }

    public void addUncountable(String... words) {
        if(words != null && words.length != 0) {
            String[] var2 = words;
            int var3 = words.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String word = var2[var4];
                if(word != null) {
                    this.uncountables.add(word.trim().toLowerCase());
                }
            }

        }
    }

    protected static String replaceAllWithUppercase(String input, String regex, int groupNumberToUppercase) {
        Pattern underscoreAndDotPattern = Pattern.compile(regex);
        Matcher matcher = underscoreAndDotPattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while(matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(groupNumberToUppercase).toUpperCase());
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    public void clear() {
        this.uncountables.clear();
        this.plurals.clear();
        this.singulars.clear();
    }

    protected void initialize() {
        this.addPluralize("$", "s");
        this.addPluralize("s$", "s");
        this.addPluralize("(ax|test)is$", "$1es");
        this.addPluralize("(octop|vir)us$", "$1i");
        this.addPluralize("(octop|vir)i$", "$1i");
        this.addPluralize("(alias|status)$", "$1es");
        this.addPluralize("(bu)s$", "$1ses");
        this.addPluralize("(buffal|tomat)o$", "$1oes");
        this.addPluralize("([ti])um$", "$1a");
        this.addPluralize("([ti])a$", "$1a");
        this.addPluralize("sis$", "ses");
        this.addPluralize("(?:([^f])fe|([lr])f)$", "$1$2ves");
        this.addPluralize("(hive)$", "$1s");
        this.addPluralize("([^aeiouy]|qu)y$", "$1ies");
        this.addPluralize("(x|ch|ss|sh)$", "$1es");
        this.addPluralize("(matr|vert|ind)ix|ex$", "$1ices");
        this.addPluralize("([m|l])ouse$", "$1ice");
        this.addPluralize("([m|l])ice$", "$1ice");
        this.addPluralize("^(ox)$", "$1en");
        this.addPluralize("(quiz)$", "$1zes");
        this.addPluralize("(people|men|children|sexes|moves|stadiums)$", "$1");
        this.addPluralize("(oxen|octopi|viri|aliases|quizzes)$", "$1");
        this.addSingularize("s$", "");
        this.addSingularize("(s|si|u)s$", "$1s");
        this.addSingularize("(n)ews$", "$1ews");
        this.addSingularize("([ti])a$", "$1um");
        this.addSingularize("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "$1$2sis");
        this.addSingularize("(^analy)ses$", "$1sis");
        this.addSingularize("(^analy)sis$", "$1sis");
        this.addSingularize("([^f])ves$", "$1fe");
        this.addSingularize("(hive)s$", "$1");
        this.addSingularize("(tive)s$", "$1");
        this.addSingularize("([lr])ves$", "$1f");
        this.addSingularize("([^aeiouy]|qu)ies$", "$1y");
        this.addSingularize("(s)eries$", "$1eries");
        this.addSingularize("(m)ovies$", "$1ovie");
        this.addSingularize("(x|ch|ss|sh)es$", "$1");
        this.addSingularize("([m|l])ice$", "$1ouse");
        this.addSingularize("(bus)es$", "$1");
        this.addSingularize("(o)es$", "$1");
        this.addSingularize("(shoe)s$", "$1");
        this.addSingularize("(cris|ax|test)is$", "$1is");
        this.addSingularize("(cris|ax|test)es$", "$1is");
        this.addSingularize("(octop|vir)i$", "$1us");
        this.addSingularize("(octop|vir)us$", "$1us");
        this.addSingularize("(alias|status)es$", "$1");
        this.addSingularize("(alias|status)$", "$1");
        this.addSingularize("^(ox)en", "$1");
        this.addSingularize("(vert|ind)ices$", "$1ex");
        this.addSingularize("(matr)ices$", "$1ix");
        this.addSingularize("(quiz)zes$", "$1");
        this.addIrregular("person", "people");
        this.addIrregular("man", "men");
        this.addIrregular("child", "children");
        this.addIrregular("sex", "sexes");
        this.addIrregular("move", "moves");
        this.addIrregular("stadium", "stadiums");
        this.addUncountable(new String[]{"equipment", "information", "rice", "money", "species", "series", "fish", "sheep"});
    }

    protected static class Rule {
        protected final String expression;
        protected final Pattern expressionPattern;
        protected final String replacement;

        protected Rule(String expression, String replacement) {
            this.expression = expression;
            this.replacement = replacement != null?replacement:"";
            this.expressionPattern = Pattern.compile(this.expression, 2);
        }

        protected String apply(String input) {
            Matcher matcher = this.expressionPattern.matcher(input);
            return !matcher.find()?null:matcher.replaceAll(this.replacement);
        }

        @Override
        public int hashCode() {
            return this.expression.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == this) {
                return true;
            } else {
                if(obj != null && obj.getClass() == this.getClass()) {
                    Inflector.Rule that = (Inflector.Rule)obj;
                    if(this.expression.equalsIgnoreCase(that.expression)) {
                        return true;
                    }
                }

                return false;
            }
        }

        @Override
        public String toString() {
            return this.expression + ", " + this.replacement;
        }
    }
}
