/**
 * 
 */
package org.apache.solr.analysis.author;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.solr.analysis.PersistingMapTokenFilterFactory;
import org.apache.solr.analysis.WriteableExplicitSynonymMap;
import org.apache.solr.analysis.WriteableSynonymMap;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.StrUtils;

public class AuthorCollectorFactory extends PersistingMapTokenFilterFactory {

  private List<String> tokenTypes;
  private boolean emitTokens;

  public void init(Map<String, String> args) {
    super.init(args);
    if (args.containsKey("tokenTypes")) {
      tokenTypes = StrUtils.splitSmart(args.get("tokenTypes"), ",", false);
    }
    else {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "The tokenType parameter missing");
    }
    emitTokens = false;
    if (args.containsKey("emitTokens")) {
      if (((String) args.get("emitTokens")).equals("true")) {
        emitTokens = true;
      }
    }
  }

  /* (non-Javadoc)
   * @see org.apache.solr.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
   */
  public AuthorCollectorFilter create(TokenStream input) {
    
    AuthorCollectorFilter collector = new AuthorCollectorFilter(input, getSynonymMap());
    collector.setTokenTypes(tokenTypes);
    collector.setEmitTokens(emitTokens);
    return collector;
  }
  
  @Override
  public WriteableSynonymMap createSynonymMap() {
    return new WriteableExplicitSynonymMap() { // no configuration allowed!
      /*
       * This synonym map has ascii forms as a key
       * and the upgraded utf8 values are values, 
       * when it is persisted, it will generate the
       * variation of the names. I.e. 
       * MÜLLER, BILL
       * 
       * will become
       * 
       *    MULLER, BILL=>MÜLLER, BILL
       *    MUELLER, BILL=>MÜLLER, BILL
       *    MULLER, B => MÜLLER, B
       *    MUELLER, B => MÜLLER, B
       *    MULLER, => MÜLLER,
       * 
       * (non-Javadoc)
       * @see org.apache.solr.analysis.WriteableExplicitSynonymMap#add(java.lang.String, java.util.Set)
       */
      
      @Override
      public void populateMap(List<String> rules) {
        HashSet<String> hs = new HashSet<String>();
        for (String rule : rules) {
          List<String> mapping = StrUtils.splitSmart(rule, "=>", false);
          if (mapping.size() != 2) {
            log.error("Invalid Synonym Rule:" + rule);
            continue;
          }
          String key = mapping.get(0).trim().replace("\\,", ",").replace("\\ ", " ");
          hs.clear();
          hs.add(key);
          for (String val: splitValues(mapping.get(1))) {
            add(val, hs);
          }
          
        }
      }

      
      @Override
      public void add(String origName, Set<String> values) {
        // key = the original author input (possibly with utf8 characters)
        // values = set of transliterated values
        Set<String> masterSet = null;
        for (String key: values) {
          if (containsKey(key)) {
            masterSet = get(key);
            break;
          }
        }
        
        if (masterSet==null) { 
          masterSet = new LinkedHashSet<String>();
        }
        masterSet.add(origName);
        
        for (String key: values) {
          put(key, masterSet);
        }
      }
      
      @Override
      public String formatEntry(String key, Set<String>values) {
        StringBuffer out = new StringBuffer();
        //out.append(super.formatEntry(key, values));
        String[] nameParts = key.split(" ");
        if (nameParts.length > 1) {
          nameParts[0] = nameParts[0].replace(",", "\\,");
          String[][] otherNames = new String[values.size()][];
          int n = 0;
          for (String name: values) {
            otherNames[n++] = name.split(" ");
            otherNames[n-1][0] = otherNames[n-1][0].replace(",", "\\,"); 
          }
          int cycle=0;
          do {
            for (n=0;n<nameParts.length;n++) {
              if (cycle>0 && n==0) continue;
              out.append(join(nameParts, n));
              out.append("=>");
              boolean notFirst = false;
              for (String[] other: otherNames) {
                if (notFirst) out.append(",");
                out.append(join(other, n));
                notFirst = true;
              }
              out.append("\n");
            }
            cycle++;
          } while (shortened(nameParts, otherNames));
        }
        return out.toString();
      }
      
      private String join(String[] name, int v) {
        StringBuffer out = new StringBuffer();
        boolean notFirst = false;
        for (int i=0;i<=v;i++) {
          if (notFirst) out.append("\\ ");
          out.append(name[i]);
          notFirst=true;
        }
        return out.toString();
      }
      
      private boolean shortened(String[]nameParts, String[][] otherNames) {
        for (int i=nameParts.length-1;i>0;i--) {
          if (nameParts[i].length() > 1) {
            nameParts[i] = nameParts[i].substring(0, 1);
            for (String[] other: otherNames) {
              other[i] = other[i].substring(0, 1);
            }
            return true;
          }
        }
        return false;
      }
    };
  }

}