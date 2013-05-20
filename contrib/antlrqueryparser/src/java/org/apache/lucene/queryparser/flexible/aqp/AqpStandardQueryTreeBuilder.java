package org.apache.lucene.queryparser.flexible.aqp;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.BoostQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.MatchAllDocsQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.MatchNoDocsQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.SlopQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.TokenizedPhraseQueryNode;
import org.apache.lucene.queryparser.flexible.standard.builders.BooleanQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.BoostQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.DummyQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.FuzzyQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.GroupQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.MatchAllDocsQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.MatchNoDocsQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.ModifierQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.MultiPhraseQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.NumericRangeQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.PhraseQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.PrefixWildcardQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.RegexpQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.SlopQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.StandardBooleanQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.StandardQueryBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.TermRangeQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.WildcardQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.nodes.MultiPhraseQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.NumericQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.NumericRangeQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.PrefixWildcardQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.RegexpQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.StandardBooleanQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;
import org.apache.lucene.queryparser.flexible.standard.processors.StandardQueryNodeProcessorPipeline;
import org.apache.lucene.queryparser.flexible.aqp.builders.AqpFieldQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.aqp.builders.AqpSlowFuzzyQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.aqp.nodes.SlowFuzzyQueryNode;
import org.apache.lucene.search.Query;

/**
 * This query tree builder only defines the necessary map to build a
 * {@link Query} tree object. It should be used to generate a {@link Query} tree
 * object from a query node tree processed by a
 * {@link AqpStandardQueryNodeProcessorPipeline}. <br/>
 * 
 * @see QueryTreeBuilder
 * @see StandardQueryNodeProcessorPipeline
 */
public class AqpStandardQueryTreeBuilder extends QueryTreeBuilder implements
    StandardQueryBuilder {

  private boolean debug = false;
  private int counter = 0;

  public AqpStandardQueryTreeBuilder(boolean debug) {
    this.debug = debug;
    init();
  }

  public AqpStandardQueryTreeBuilder() {
    init();
  }

  public void init() {
    setBuilder(GroupQueryNode.class, new GroupQueryNodeBuilder());
    setBuilder(FieldQueryNode.class, new AqpFieldQueryNodeBuilder());
    setBuilder(BooleanQueryNode.class, new BooleanQueryNodeBuilder());
    setBuilder(SlowFuzzyQueryNode.class, new AqpSlowFuzzyQueryNodeBuilder());
    setBuilder(FuzzyQueryNode.class, new FuzzyQueryNodeBuilder());
    setBuilder(NumericQueryNode.class, new DummyQueryNodeBuilder());
    setBuilder(NumericRangeQueryNode.class, new NumericRangeQueryNodeBuilder());
    setBuilder(BoostQueryNode.class, new BoostQueryNodeBuilder());
    setBuilder(ModifierQueryNode.class, new ModifierQueryNodeBuilder());
    setBuilder(WildcardQueryNode.class, new WildcardQueryNodeBuilder());
    setBuilder(TokenizedPhraseQueryNode.class, new PhraseQueryNodeBuilder());
    setBuilder(MatchNoDocsQueryNode.class, new MatchNoDocsQueryNodeBuilder());
    setBuilder(PrefixWildcardQueryNode.class,
        new PrefixWildcardQueryNodeBuilder());
    setBuilder(TermRangeQueryNode.class, new TermRangeQueryNodeBuilder());
    setBuilder(RegexpQueryNode.class, new RegexpQueryNodeBuilder());
    setBuilder(SlopQueryNode.class, new SlopQueryNodeBuilder());
    setBuilder(StandardBooleanQueryNode.class,
        new StandardBooleanQueryNodeBuilder());
    setBuilder(MultiPhraseQueryNode.class, new MultiPhraseQueryNodeBuilder());
    setBuilder(MatchAllDocsQueryNode.class, new MatchAllDocsQueryNodeBuilder());
  }

  @Override
  public Query build(QueryNode queryNode) throws QueryNodeException {
    this.counter = 0;
    return (Query) super.build(queryNode);
  }

  public void debug(boolean debug) {
    this.debug = debug;
  }

  public void setBuilder(Class<? extends QueryNode> queryNodeClass,
      QueryBuilder builder) {
    if (this.debug) {
      super.setBuilder(queryNodeClass, new DebuggingNodeBuilder(queryNodeClass,
          builder));
    } else {
      super.setBuilder(queryNodeClass, builder);
    }
  }

  class DebuggingNodeBuilder implements QueryBuilder {
    private Class<? extends QueryNode> clazz = null;
    private QueryBuilder realBuilder = null;

    DebuggingNodeBuilder(Class<? extends QueryNode> queryNodeClass,
        QueryBuilder builder) {
      clazz = queryNodeClass;
      realBuilder = builder;
    }

    public Object build(QueryNode queryNode) throws QueryNodeException {
      System.out.println("--------------------------------------------");
      System.out.println("step     " + counter++ + ".");
      System.out.println("builder: " + realBuilder.getClass().getName());
      System.out.println("node:    " + clazz.getName());
      System.out.println(queryNode.toString());
      System.out.println("   -->");
      Object result = realBuilder.build(queryNode);
      if (result != null) {
        System.out.println(((Query) result).toString() + "  <"
            + result.getClass().getName() + ">");
      } else {
        System.out.println("null");
      }
      System.out.println("--------------------------------------------");
      return result;
    }

  };

}
